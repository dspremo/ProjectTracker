package com.example.projecttracker.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "projekti")
data class Projekat(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val naziv: String,
    val klijent: String,
    val datumPocetka: Long,
    val dogovorenaSuma: Double, // NOVA - umesto satnice
    val opis: String = "",
    val aktivan: Boolean = false,
    val sortOrder: Int = 0 // Za custom sortiranje
)

