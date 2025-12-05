package com.spremodesign.projecttracker.ui


import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.spremodesign.projecttracker.data.AppDatabase
import com.spremodesign.projecttracker.data.Projekat
import kotlinx.coroutines.flow.MutableStateFlow
import java.time.LocalDate
import java.time.YearMonth
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.ZoneId
data class MonthProjectStat(
    val projekat: Projekat,
    val hours: Double,
    val earnings: Double
)

data class MonthDataStat(
    val month: YearMonth,
    val hours: Double,
    val net: Double
)

data class StatistikaUiState(
    val projekti: List<Projekat> = emptyList(),
    val selectedProject: Projekat? = null,
    val selectedDate: LocalDate = LocalDate.now(),
    val selectedMonth: YearMonth = YearMonth.now(),

    // po projektu + danu
    val projectDayHours: Double = 0.0,
    val projectDayIncome: Double = 0.0,
    val projectDayCosts: Double = 0.0,

    // po mesecu – globalno, SVI projekti
    val monthHours: Double = 0.0,
    val monthIncome: Double = 0.0,
    val monthCosts: Double = 0.0,
    val monthNet: Double = 0.0,
    val monthProjects: List<MonthProjectStat> = emptyList(),

    // za kalendar – dani kada postoji rad (bilo koji projekat)
    val daysWithWork: Set<LocalDate> = emptySet(),
    
    // za grafikon - poslednja 4 meseca
    val last4Months: List<MonthDataStat> = emptyList()
)

class StatistikaViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val dao = AppDatabase.getDatabase(application).projekatDao()

    private val _uiState = MutableStateFlow(StatistikaUiState())
    val uiState: StateFlow<StatistikaUiState> = _uiState

    // pratimo selektovani projekat po id-ju
    private val selectedProjectId = MutableStateFlow<Long?>(null)

    init {
        observeProjects()
    }

    private fun observeProjects() {
        viewModelScope.launch {
            dao.sviProjekti().collectLatest { projekti ->
                _uiState.update { old ->
                    val currentSelected = old.selectedProject
                    val selected: Projekat? = when {
                        currentSelected != null && projekti.any { it.id == currentSelected.id } ->
                            projekti.first { it.id == currentSelected.id }

                        else -> projekti.firstOrNull()
                    }

                    selectedProjectId.value = selected?.id

                    old.copy(
                        projekti = projekti,
                        selectedProject = selected
                    )
                }
                recalcAll()
            }
        }
    }

    fun onProjectSelected(projekat: Projekat) {
        _uiState.update { it.copy(selectedProject = projekat) }
        selectedProjectId.value = projekat.id
        recalcAll()
    }

    fun onDateSelected(date: LocalDate) {
        _uiState.update {
            it.copy(
                selectedDate = date,
                selectedMonth = YearMonth.from(date)
            )
        }
        recalcAll()
    }

    fun onMonthChanged(month: YearMonth) {
        _uiState.update { it.copy(selectedMonth = month) }
        recalcAll()
    }

    private fun recalcAll() {
        viewModelScope.launch(Dispatchers.IO) {
            val state = _uiState.value
            val projectId = selectedProjectId.value
            val zone = ZoneId.systemDefault()

            val day = state.selectedDate
            val dayStart = day.atStartOfDay(zone).toInstant().toEpochMilli()
            val dayEnd = day.plusDays(1).atStartOfDay(zone).toInstant().toEpochMilli() - 1

            val month = state.selectedMonth
            val monthStartDate = month.atDay(1)
            val monthEndDate = month.atEndOfMonth()
            val monthStart = monthStartDate.atStartOfDay(zone).toInstant().toEpochMilli()
            val monthEnd =
                monthEndDate.plusDays(1).atStartOfDay(zone).toInstant().toEpochMilli() - 1

            // --- po PROJEKTU + DANU (ako imamo selektovan projekat)
            val dayHours: Double
            val dayIncome: Double
            val dayCosts: Double

            if (projectId != null) {
                val daySati = dao.radniSatiZaPeriod(projectId, dayStart, dayEnd)
                val dayTroskovi = dao.troskoviZaPeriod(projectId, dayStart, dayEnd)
                val dayUplate = dao.uplateZaPeriod(projectId, dayStart, dayEnd)

                dayHours = daySati.sumOf { it.brojSati }
                dayIncome = dayUplate.sumOf { it.iznos }
                dayCosts = dayTroskovi.sumOf { it.iznos }
            } else {
                dayHours = 0.0
                dayIncome = 0.0
                dayCosts = 0.0
            }

            // --- po MESECU (svi projekti) ---
            val monthSatiAll = dao.sviSatiZaPeriod(monthStart, monthEnd)
            val monthTroskoviAll = dao.sviTroskoviZaPeriod(monthStart, monthEnd)
            val monthUplateAll = dao.sveUplateZaPeriod(monthStart, monthEnd)

            val monthHours = monthSatiAll.sumOf { it.brojSati }
            val monthCosts = monthTroskoviAll.sumOf { it.iznos }
            val monthIncome = monthUplateAll.sumOf { it.iznos }

            val monthNet = monthIncome - monthCosts

            // dani sa radom (za kalendar)
            val daysWithWork: Set<LocalDate> = monthSatiAll
                .map {
                    Instant.ofEpochMilli(it.datum)
                        .atZone(zone)
                        .toLocalDate()
                }
                .toSet()

            // projekti po mesecu (grupisani)
            val projekti = _uiState.value.projekti

            val hoursByProject = monthSatiAll.groupBy { it.projekatId }
                .mapValues { (_, list) -> list.sumOf { it.brojSati } }

            val incomeByProject = monthUplateAll.groupBy { it.projekatId }
                .mapValues { (_, list) -> list.sumOf { it.iznos } }

            val monthProjects = projekti.mapNotNull { p ->
                val h = hoursByProject[p.id] ?: 0.0
                val e = incomeByProject[p.id] ?: 0.0
                if (h == 0.0 && e == 0.0) null
                else MonthProjectStat(
                    projekat = p,
                    hours = h,
                    earnings = e
                )
            }

            // --- Poslednja 4 meseca ---
            val last4MonthsList = mutableListOf<MonthDataStat>()
            for (i in 3 downTo 0) {
                val m = month.minusMonths(i.toLong())
                val mStartDate = m.atDay(1)
                val mEndDate = m.atEndOfMonth()
                val mStart = mStartDate.atStartOfDay(zone).toInstant().toEpochMilli()
                val mEnd = mEndDate.plusDays(1).atStartOfDay(zone).toInstant().toEpochMilli() - 1

                val mSati = dao.sviSatiZaPeriod(mStart, mEnd)
                val mTroskovi = dao.sviTroskoviZaPeriod(mStart, mEnd)
                val mUplate = dao.sveUplateZaPeriod(mStart, mEnd)

                val mHours = mSati.sumOf { it.brojSati }
                val mCosts = mTroskovi.sumOf { it.iznos }
                val mIncome = mUplate.sumOf { it.iznos }
                val mNet = mIncome - mCosts

                last4MonthsList.add(MonthDataStat(m, mHours, mNet))
            }

            _uiState.update {
                it.copy(
                    projectDayHours = dayHours,
                    projectDayIncome = dayIncome,
                    projectDayCosts = dayCosts,
                    monthHours = monthHours,
                    monthIncome = monthIncome,
                    monthCosts = monthCosts,
                    monthNet = monthNet,
                    monthProjects = monthProjects,
                    daysWithWork = daysWithWork,
                    last4Months = last4MonthsList
                )
            }
        }
    }
}