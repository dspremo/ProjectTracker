package com.spremodesign.projecttracker.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "troskovi",
    foreignKeys = [ForeignKey(
        entity = Projekat::class,
        parentColumns = ["id"],
        childColumns = ["projekatId"],
        onDelete = ForeignKey.CASCADE
    )]
)
data class Trosak(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val projekatId: Long,
    val datum: Long, // EDITABILAN
    val iznos: Double,
    val opis: String,
    val kategorija: String,
    val putanjaDoSlike: String? = null,
    val sortOrder: Int = 0
)