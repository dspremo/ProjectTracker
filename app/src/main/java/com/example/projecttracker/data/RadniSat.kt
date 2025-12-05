package com.example.projecttracker.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "radni_sati",
    foreignKeys = [ForeignKey(
        entity = Projekat::class,
        parentColumns = ["id"],
        childColumns = ["projekatId"],
        onDelete = ForeignKey.CASCADE
    )]
)
data class RadniSat(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val projekatId: Long,
    val datum: Long, // EDITABILAN
    val brojSati: Double,
    val opis: String = "",
    val sortOrder: Int = 0
)
