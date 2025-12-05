package com.spremodesign.projecttracker.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "uplate",
    foreignKeys = [ForeignKey(
        entity = Projekat::class,
        parentColumns = ["id"],
        childColumns = ["projekatId"],
        onDelete = ForeignKey.CASCADE
    )]
)
data class Uplata(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val projekatId: Long,
    val datum: Long,
    val iznos: Double,
    val opis: String,
    val sortOrder: Int = 0
)