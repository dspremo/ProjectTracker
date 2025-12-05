package com.spremodesign.projecttracker.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.spremodesign.projecttracker.data.AppDatabase
import com.spremodesign.projecttracker.data.Projekat
import com.spremodesign.projecttracker.data.RadniSat
import com.spremodesign.projecttracker.data.Trosak
import com.spremodesign.projecttracker.data.Uplata
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class ProjekatStats(
    val ukupnoSati: Double,
    val ukupniTroskovi: Double,
    val ukupneUplate: Double,
    val zarada: Double,
    val zaradaPoSatu: Double
)

class ProjekatDetaljiViewModel(
    application: Application,
    private val projekatId: Long
) : AndroidViewModel(application) {

    val scope = CoroutineScope(Dispatchers.IO)

    private val database = AppDatabase.getDatabase(application)

    val projekat: StateFlow<Projekat?> =
        flow {
            val p = database.projekatDao().projekatPoId(projekatId)
            emit(p)
        }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.Eagerly,
                initialValue = null
            )

    val sati = database.projekatDao().radniSatiZaProjekat(projekatId)
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val troskovi = database.projekatDao().troskoviZaProjekat(projekatId)
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val uplate = database.projekatDao().uplateZaProjekat(projekatId)
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val stats: StateFlow<ProjekatStats?> = combine(
        projekat,
        sati,
        troskovi,
        uplate
    ) { proj, s, t, u ->
        proj?.let {
            val ukupnoSati = s.sumOf { it.brojSati }
            val ukupniTroskovi = t.sumOf { it.iznos }
            val ukupneUplate = u.sumOf { it.iznos }
            val zarada = proj.dogovorenaSuma - ukupniTroskovi
            val zaradaPoSatu = if (ukupnoSati > 0) zarada / ukupnoSati else 0.0

            ProjekatStats(
                ukupnoSati = ukupnoSati,
                ukupniTroskovi = ukupniTroskovi,
                ukupneUplate = ukupneUplate,
                zarada = zarada,
                zaradaPoSatu = zaradaPoSatu
            )
        }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, null)

    fun dodajSate(brojSati: Double, opis: String, datum: Long) {
        viewModelScope.launch {
            val sat = RadniSat(
                projekatId = projekatId,
                datum = datum,
                brojSati = brojSati,
                opis = opis
            )
            database.projekatDao().dodajRadniSat(sat)
        }
    }

    fun dodajTrosak(
        iznos: Double,
        opis: String,
        kategorija: String,
        putanjaDoSlike: String?,
        datum: Long
    ) {
        viewModelScope.launch {
            val trosak = Trosak(
                projekatId = projekatId,
                datum = datum,
                iznos = iznos,
                opis = opis,
                kategorija = kategorija,
                putanjaDoSlike = putanjaDoSlike
            )
            database.projekatDao().dodajTrosak(trosak)
        }
    }

    fun dodajUplatu(iznos: Double, opis: String, datum: Long) {
        viewModelScope.launch {
            val uplata = Uplata(
                projekatId = projekatId,
                datum = datum,
                iznos = iznos,
                opis = opis
            )
            database.projekatDao().dodajUplatu(uplata)
        }
    }

    fun obrisiSat(sat: RadniSat) {
        viewModelScope.launch {
            database.projekatDao().obrisiRadniSat(sat)
        }
    }

    fun obrisiTrosak(trosak: Trosak) {
        viewModelScope.launch {
            database.projekatDao().obrisiTrosak(trosak)
        }
    }

    fun obrisiUplatu(uplata: Uplata) {
        viewModelScope.launch {
            database.projekatDao().obrisiUplatu(uplata)
        }
    }


    fun azurirajSate(sat: RadniSat, brojSati: Double, opis: String, datum: Long) {
        viewModelScope.launch {
            val azuriranSat = sat.copy(
                brojSati = brojSati,
                opis = opis,
                datum = datum
            )
            database.projekatDao().azurirajRadniSat(azuriranSat)
        }
    }

    fun azurirajTrosak(
        trosak: Trosak,
        iznos: Double,
        opis: String,
        kategorija: String,
        putanjaDoSlike: String?,
        datum: Long
    ) {
        viewModelScope.launch {
            val azuriranTrosak = trosak.copy(
                iznos = iznos,
                opis = opis,
                kategorija = kategorija,
                putanjaDoSlike = putanjaDoSlike,
                datum = datum
            )
            database.projekatDao().azurirajTrosak(azuriranTrosak)
        }
    }

    fun azurirajUplatu(uplata: Uplata, iznos: Double, opis: String, datum: Long) {
        viewModelScope.launch {
            val azuriranUplata = uplata.copy(
                iznos = iznos,
                opis = opis,
                datum = datum
            )
            database.projekatDao().azurirajUplatu(azuriranUplata)
        }
    }

    fun azurirajProjekat(
        naziv: String,
        klijent: String,
        dogovorenaSuma: Double,
        opis: String,
        aktivan: Boolean
    ) {
        viewModelScope.launch {
            projekat.value?.let { proj ->
                val azuriranProjekat = proj.copy(
                    naziv = naziv,
                    klijent = klijent,
                    dogovorenaSuma = dogovorenaSuma,
                    opis = opis,
                    aktivan = aktivan
                )
                database.projekatDao().azurirajProjekat(azuriranProjekat)
            }
        }
    }
}

class ProjekatDetaljiViewModelFactory(
    private val application: Application,
    private val projekatId: Long
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ProjekatDetaljiViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ProjekatDetaljiViewModel(application, projekatId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}