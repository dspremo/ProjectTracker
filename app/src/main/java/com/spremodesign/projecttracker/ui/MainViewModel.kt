package com.example.projecttracker.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.projecttracker.data.AppDatabase
import com.example.projecttracker.data.Projekat
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val database = AppDatabase.getDatabase(application)
    val projekti: Flow<List<Projekat>> = database.projekatDao().sviProjekti()

    fun dodajProjekat(projekat: Projekat) {
        viewModelScope.launch {
            database.projekatDao().dodajProjekat(projekat)
        }
    }

    fun azurirajProjekat(projekat: Projekat) {
        viewModelScope.launch {
            database.projekatDao().azurirajProjekat(projekat)
        }
    }
}