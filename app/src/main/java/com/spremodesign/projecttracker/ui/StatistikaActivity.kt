package com.spremodesign.projecttracker.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.spremodesign.projecttracker.data.Projekat
import com.spremodesign.projecttracker.ui.theme.*
import com.kizitonwose.calendar.compose.HorizontalCalendar
import com.kizitonwose.calendar.compose.rememberCalendarState
import com.kizitonwose.calendar.core.*
import java.text.NumberFormat
import java.time.*
import java.time.format.TextStyle
import java.util.*

class StatistikaActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val viewModel: StatistikaViewModel = viewModel()
            StatistikaScreen(
                onBack = { finish() },
                viewModel = viewModel
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatistikaScreen(
    onBack: () -> Unit,
    viewModel: StatistikaViewModel
) {
    var selectedMode by remember { mutableStateOf(0) } // 0 = Po projektu, 1 = Po mesecu
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Statistika",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, "Nazad", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Surface,
                    titleContentColor = TextPrimary
                )
            )
        },
        containerColor = SurfaceDark
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Mode selector
            SegmentedButton(
                selectedMode = selectedMode,
                onModeChange = { selectedMode = it }
            )

            when (selectedMode) {
                0 -> StatistikaPoProjetkuScreen(
                    uiState = uiState,
                    onProjectClick = { viewModel.onProjectSelected(it) },
                    onDateSelected = { viewModel.onDateSelected(it) }
                )

                1 -> StatistikaPoMesecuScreen(
                    uiState = uiState,
                    viewModel = viewModel
                )
            }
        }
    }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatistikaPoProjetkuScreen(
    uiState: StatistikaUiState,
    onProjectClick: (Projekat) -> Unit,
    onDateSelected: (LocalDate) -> Unit
) {
    val currencyFormat = remember { NumberFormat.getCurrencyInstance(Locale("sr", "RS")) }
    var showProjectPicker by remember { mutableStateOf(false) }
    var selectedTab by remember { mutableStateOf(0) } // 0 = Sati, 1 = Troškovi, 2 = Uplate
    var swipeOffset by remember { mutableStateOf(0f) }

    // ----- BOTTOM SHEET ZA PROJEKTE -----
    if (showProjectPicker) {
        ModalBottomSheet(
            onDismissRequest = { showProjectPicker = false },
            containerColor = Surface,
            dragHandle = { BottomSheetDefaults.DragHandle() }
        ) {
            Text(
                text = "Izaberi projekat",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            if (uiState.projekti.isEmpty()) {
                Text(
                    text = "Nema projekata.",
                    color = TextDisabled,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(bottom = 32.dp)
                ) {
                    items(uiState.projekti) { projekat ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onProjectClick(projekat)
                                    showProjectPicker = false
                                }
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = projekat.naziv,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = TextPrimary
                                )
                                projekat.klijent?.takeIf { it.isNotBlank() }?.let { klijent ->
                                    Text(
                                        text = klijent,
                                        fontSize = 13.sp,
                                        color = TextDisabled
                                    )
                                }
                            }
                            if (uiState.selectedProject?.id == projekat.id) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = GoldPrimary
                                )
                            }
                        }
                    }
                }
            }
        }
    }
    // ----- /BOTTOM SHEET -----

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Projekat selector
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Surface),
            onClick = {
                if (uiState.projekti.isNotEmpty()) {
                    showProjectPicker = true
                }
            }
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = uiState.selectedProject?.naziv ?: "Izaberi projekat",
                    fontSize = 16.sp,
                    color = TextPrimary
                )
                Icon(
                    Icons.Default.ArrowDropDown,
                    contentDescription = null,
                    tint = GoldPrimary
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Calendar view
        CalendarView(
            selectedDate = uiState.selectedDate,
            daysWithWork = uiState.daysWithWork,
            onDateSelected = onDateSelected
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Tab selector
        ProjectTabSelector(
            selectedTab = selectedTab,
            onTabChange = { selectedTab = it }
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Swipeable content
        Box(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectHorizontalDragGestures { change, dragAmount ->
                        change.consume()
                        swipeOffset += dragAmount
                        
                        if (swipeOffset < -100 && selectedTab < 2) {
                            selectedTab++
                            swipeOffset = 0f
                        } else if (swipeOffset > 100 && selectedTab > 0) {
                            selectedTab--
                            swipeOffset = 0f
                        }
                    }
                }
        ) {
            when (selectedTab) {
                0 -> ProjectStatsCard(
                    label = "Ukupno sati",
                    value = String.format("%.1fh", uiState.projectDayHours),
                    color = BrownLight
                )
                1 -> ProjectStatsCard(
                    label = "Ukupni troškovi",
                    value = currencyFormat.format(uiState.projectDayCosts),
                    color = Color(0xFFFF5722)
                )
                2 -> ProjectStatsCard(
                    label = "Ukupne uplate",
                    value = currencyFormat.format(uiState.projectDayIncome),
                    color = Color(0xFF4CAF50)
                )
            }
        }
    }
}


@Composable
fun CalendarView(
    selectedDate: LocalDate,
    daysWithWork: Set<LocalDate>,
    onDateSelected: (LocalDate) -> Unit
) {
    val currentMonth = remember { YearMonth.now() }
    val startMonth = remember { currentMonth.minusMonths(12) }
    val endMonth = remember { currentMonth.plusMonths(12) }

    val state = rememberCalendarState(
        startMonth = startMonth,
        endMonth = endMonth,
        firstVisibleMonth = currentMonth,
        firstDayOfWeek = DayOfWeek.MONDAY
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Surface)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            HorizontalCalendar(
                state = state,
                dayContent = { day ->
                    Day(
                        day = day,
                        isSelected = day.date == selectedDate,
                        hasWork = daysWithWork.contains(day.date),
                        onClick = { onDateSelected(day.date) }
                    )
                }
            )
        }
    }
}

@Composable
fun Day(
    day: CalendarDay,
    isSelected: Boolean,
    hasWork: Boolean,
    onClick: () -> Unit
) {
    val bgColor = when {
        isSelected -> GoldPrimary
        hasWork -> GoldPrimary.copy(alpha = 0.3f)
        else -> Color.Transparent
    }

    val textColor = when {
        isSelected -> SurfaceDark
        hasWork -> GoldPrimary
        else -> TextSecondary
    }

    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .padding(4.dp)
            .clip(CircleShape)
            .background(bgColor)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = day.date.dayOfMonth.toString(),
            fontSize = 12.sp,
            color = textColor,
            fontWeight = if (hasWork || isSelected) FontWeight.Bold else FontWeight.Normal
        )
    }
}

@Composable
fun StatistikaPoMesecuScreen(
    uiState: StatistikaUiState,
    viewModel: StatistikaViewModel
) {
    val currentMonth = uiState.selectedMonth
    val currencyFormat = remember { NumberFormat.getCurrencyInstance(Locale("sr", "RS")) }
    var swipeOffset by remember { mutableStateOf(0f) }
    
    // Svi meseci za grafikon - poslednja 4
    val displayedMonthsList = generateLast4Months(currentMonth)

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectHorizontalDragGestures { change, dragAmount ->
                    change.consume()
                    swipeOffset += dragAmount
                    
                    // Ako je korisnik sveznuo dovoljno levo, kreni na sledeći mesec
                    if (swipeOffset < -100) {
                        viewModel.onMonthChanged(currentMonth.plusMonths(1))
                        swipeOffset = 0f
                    }
                    // Ako je korisnik svezao dovoljno desno, kreni na prethodni mesec
                    else if (swipeOffset > 100) {
                        viewModel.onMonthChanged(currentMonth.minusMonths(1))
                        swipeOffset = 0f
                    }
                }
            },
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Naslov sa mesecom i godinom + Dropdown
        item {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = currentMonth.month.getDisplayName(TextStyle.FULL, Locale("sr")).replaceFirstChar { it.uppercase() },
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = GoldPrimary
                )
                Text(
                    text = currentMonth.year.toString(),
                    fontSize = 16.sp,
                    color = TextDisabled
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // Dropdown za izbor meseca
                MonthDropdown(
                    selectedMonth = currentMonth,
                    onMonthSelected = { viewModel.onMonthChanged(it) }
                )
            }
        }

        // Month summary card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Surface)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp)
                ) {
                    MetricRow(
                        label = "Ukupno sati",
                        value = String.format("%.1fh", uiState.monthHours),
                        icon = Icons.Default.Schedule,
                        color = BrownLight
                    )
                    MetricRow(
                        label = "Ukupne uplate",
                        value = currencyFormat.format(uiState.monthIncome),
                        icon = Icons.Default.TrendingUp,
                        color = Color(0xFF4CAF50)
                    )
                    MetricRow(
                        label = "Ukupni troškovi",
                        value = currencyFormat.format(uiState.monthCosts),
                        icon = Icons.Default.TrendingDown,
                        color = Color(0xFFFF5722)
                    )

                    Divider(
                        modifier = Modifier.padding(vertical = 12.dp),
                        color = BrownLight.copy(alpha = 0.3f)
                    )

                    MetricRow(
                        label = "Neto zarada",
                        value = currencyFormat.format(uiState.monthNet),
                        icon = Icons.Default.Paid,
                        color = GoldPrimary,
                        isTotal = true
                    )
                }
            }
        }

        // Grafikon - Poslednja 4 meseca (Sati i Zarada)
        item {
            Text(
                text = "Pregled sati i zarade",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
                modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
            )
        }

        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Surface)
            ) {
                MonthlyChartView(months = displayedMonthsList, uiState = uiState)
            }
        }

        // Detaljni pregled meseci
        item {
            Text(
                text = "Detaljni pregled meseci",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
                modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
            )
        }

        items(uiState.last4Months) { monthData ->
            MonthDetailCard(monthData)
        }

        // Projekti ovog meseca
        item {
            Text(
                text = "Projekti ovog meseca",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }

        items(uiState.monthProjects) { stat ->
            MonthProjectCard(
                projectName = stat.projekat.naziv,
                hours = stat.hours,
                earnings = stat.earnings
            )
        }

        item {
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun generateLast4Months(currentMonth: YearMonth): List<YearMonth> {
    return listOf(
        currentMonth.minusMonths(3),
        currentMonth.minusMonths(2),
        currentMonth.minusMonths(1),
        currentMonth
    )
}



@Composable
fun MetricRow(
    label: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    isTotal: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(if (isTotal) 28.dp else 24.dp)
            )
            Text(
                text = label,
                fontSize = if (isTotal) 18.sp else 16.sp,
                color = TextPrimary,
                fontWeight = if (isTotal) FontWeight.Bold else FontWeight.Normal
            )
        }
        Text(
            text = value,
            fontSize = if (isTotal) 20.sp else 16.sp,
            fontWeight = FontWeight.Bold,
            color = color
        )
    }
}

@Composable
fun MiniStatCard(
    label: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceLight)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = label,
                fontSize = 12.sp,
                color = TextDisabled
            )
            Text(
                text = value,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = color,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

@Composable
fun MonthProjectCard(
    projectName: String,
    hours: Double,
    earnings: Double
) {
    val currencyFormat = remember { NumberFormat.getCurrencyInstance(Locale("sr", "RS")) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = projectName,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Text(
                    text = "${String.format("%.1f", hours)}h",
                    fontSize = 14.sp,
                    color = BrownLight,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
            Text(
                text = currencyFormat.format(earnings),
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = GoldPrimary
            )
        }
    }
}

@Composable
fun SegmentedButton(
    selectedMode: Int,
    onModeChange: (Int) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(Surface),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        listOf("Po Projektu", "Po Mesecu").forEachIndexed { index, label ->
            Button(
                onClick = { onModeChange(index) },
                modifier = Modifier
                    .weight(1f)
                    .padding(4.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (selectedMode == index) GoldPrimary else Color.Transparent,
                    contentColor = if (selectedMode == index) SurfaceDark else TextSecondary
                ),
                shape = RoundedCornerShape(12.dp),
                elevation = ButtonDefaults.buttonElevation(
                    defaultElevation = if (selectedMode == index) 4.dp else 0.dp
                )
            ) {
                Text(
                    text = label,
                    fontWeight = if (selectedMode == index) FontWeight.Bold else FontWeight.Normal,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
        }
    }
}

@Composable
fun MonthlyChartView(
    months: List<YearMonth>,
    uiState: StatistikaUiState
) {
    val currencyFormat = remember { NumberFormat.getCurrencyInstance(Locale("sr", "RS")) }
    
    if (uiState.last4Months.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Nema podataka za prikazivanje",
                color = TextDisabled,
                fontSize = 14.sp
            )
        }
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Chart sa visokim zaradama
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(horizontal = 4.dp)
        ) {
            items(uiState.last4Months) { monthData ->
                MonthBarChart(monthData = monthData)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Legend
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(GoldPrimary)
                )
                Text(
                    text = "Neto",
                    fontSize = 11.sp,
                    color = TextSecondary
                )
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(BrownLight)
                )
                Text(
                    text = "Sati",
                    fontSize = 11.sp,
                    color = TextSecondary
                )
            }
        }
    }
}

@Composable
fun MonthBarChart(monthData: MonthDataStat) {
    val maxHeight = 180f
    val maxValue = 100000 // Max zarada za skaliranje
    
    val netHeight = (monthData.net / maxValue * maxHeight).coerceIn(0f, maxHeight).dp
    val hoursHeight = ((monthData.hours / 20) * maxHeight).coerceIn(0f, maxHeight).dp // 20 sati = max
    
    Column(
        modifier = Modifier
            .width(80.dp)
            .height(200.dp),
        verticalArrangement = Arrangement.Bottom,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Bars
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.Bottom
        ) {
            // Net bar
            Box(
                modifier = Modifier
                    .width(24.dp)
                    .height(netHeight as Dp)
                    .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                    .background(GoldPrimary)
            )
            
            // Hours bar
            Box(
                modifier = Modifier
                    .width(24.dp)
                    .height(hoursHeight as Dp)
                    .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                    .background(BrownLight)
            )
        }
        
        // Month label
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = monthData.month.month.getDisplayName(TextStyle.SHORT, Locale("sr")).take(3),
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = TextSecondary
        )
    }
}

@Composable
fun MonthDropdown(
    selectedMonth: YearMonth,
    onMonthSelected: (YearMonth) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    
    val currentYear = LocalDate.now().year
    val months = mutableListOf<YearMonth>()
    for (i in 0..23) {
        months.add(YearMonth.of(currentYear, (12 - i % 12).let { if (it == 0) 12 else it }))
    }
    
    Box(modifier = Modifier.fillMaxWidth()) {
        Button(
            onClick = { expanded = true },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = Surface
            ),
            shape = RoundedCornerShape(12.dp),
            border = androidx.compose.foundation.BorderStroke(
                width = 1.dp,
                color = GoldPrimary.copy(alpha = 0.3f)
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${selectedMonth.month.getDisplayName(TextStyle.SHORT, Locale("sr"))} ${selectedMonth.year}",
                    color = TextPrimary,
                    fontSize = 16.sp
                )
                Icon(
                    Icons.Default.ArrowDropDown,
                    contentDescription = null,
                    tint = GoldPrimary
                )
            }
        }
        
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.fillMaxWidth(0.9f)
        ) {
            months.take(12).forEachIndexed { _, month ->
                DropdownMenuItem(
                    text = {
                        Text(
                            text = "${month.month.getDisplayName(TextStyle.FULL, Locale("sr"))} ${month.year}",
                            color = if (month == selectedMonth) GoldPrimary else TextPrimary
                        )
                    },
                    onClick = {
                        onMonthSelected(month)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
fun MonthDetailCard(monthData: MonthDataStat) {
    val currencyFormat = remember { NumberFormat.getCurrencyInstance(Locale("sr", "RS")) }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Surface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = monthData.month.month.getDisplayName(TextStyle.FULL, Locale("sr")).replaceFirstChar { it.uppercase() },
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = monthData.month.year.toString(),
                        fontSize = 13.sp,
                        color = TextDisabled,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
                
                Column(
                    horizontalAlignment = Alignment.End
                ) {
                    Text(
                        text = currencyFormat.format(monthData.net),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (monthData.net > 0) GoldPrimary else Color(0xFFFF5722)
                    )
                    Text(
                        text = "${String.format("%.1f", monthData.hours)}h",
                        fontSize = 13.sp,
                        color = BrownLight,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun ProjectTabSelector(
    selectedTab: Int,
    onTabChange: (Int) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Surface),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        listOf("Sati", "Troškovi", "Uplate").forEachIndexed { index, label ->
            Button(
                onClick = { onTabChange(index) },
                modifier = Modifier
                    .weight(1f)
                    .padding(4.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (selectedTab == index) GoldPrimary else Color.Transparent,
                    contentColor = if (selectedTab == index) SurfaceDark else TextSecondary
                ),
                shape = RoundedCornerShape(10.dp),
                elevation = ButtonDefaults.buttonElevation(
                    defaultElevation = if (selectedTab == index) 4.dp else 0.dp
                )
            ) {
                Text(
                    text = label,
                    fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(vertical = 6.dp)
                )
            }
        }
    }
}

@Composable
fun ProjectStatsCard(
    label: String,
    value: String,
    color: Color
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Surface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = label,
                fontSize = 16.sp,
                color = TextSecondary,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = value,
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
    }
}