package com.example.projecttracker.data

import androidx.lifecycle.LiveData
import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ProjekatDao {
    // Projekti
    @Query("SELECT * FROM projekti ORDER BY sortOrder ASC, datumPocetka DESC")
    fun sviProjekti(): Flow<List<Projekat>>

    @Query("SELECT * FROM projekti WHERE id = :id")
    suspend fun projekatPoId(id: Long): Projekat?

    @Insert
    suspend fun dodajProjekat(projekat: Projekat): Long

    @Update
    suspend fun azurirajProjekat(projekat: Projekat)

    @Delete
    suspend fun obrisiProjekat(projekat: Projekat)

    @Query("UPDATE projekti SET sortOrder = :order WHERE id = :id")
    suspend fun azurirajSortOrder(id: Long, order: Int)

    // Radni sati
    @Query("SELECT * FROM radni_sati WHERE projekatId = :projekatId ORDER BY sortOrder ASC, datum DESC")
    fun radniSatiZaProjekat(projekatId: Long): Flow<List<RadniSat>>

    @Query("SELECT * FROM radni_sati WHERE projekatId = :projekatId AND datum BETWEEN :start AND :end")
    suspend fun radniSatiZaPeriod(projekatId: Long, start: Long, end: Long): List<RadniSat>

    @Query("SELECT SUM(brojSati) FROM radni_sati WHERE projekatId = :projekatId")
    suspend fun ukupnoSati(projekatId: Long): Double?

    @Insert
    suspend fun dodajRadniSat(radniSat: RadniSat)

    @Update
    suspend fun azurirajRadniSat(radniSat: RadniSat)

    @Delete
    suspend fun obrisiRadniSat(radniSat: RadniSat)

    // Troškovi
    @Query("SELECT * FROM troskovi WHERE projekatId = :projekatId ORDER BY sortOrder ASC, datum DESC")
    fun troskoviZaProjekat(projekatId: Long): Flow<List<Trosak>>

    @Query("SELECT * FROM troskovi WHERE projekatId = :projekatId AND datum BETWEEN :start AND :end")
    suspend fun troskoviZaPeriod(projekatId: Long, start: Long, end: Long): List<Trosak>

    @Query("SELECT SUM(iznos) FROM troskovi WHERE projekatId = :projekatId")
    suspend fun ukupniTroskovi(projekatId: Long): Double?

    @Insert
    suspend fun dodajTrosak(trosak: Trosak)

    @Update
    suspend fun azurirajTrosak(trosak: Trosak)

    @Delete
    suspend fun obrisiTrosak(trosak: Trosak)

    // Uplate - NOVO!
    @Query("SELECT * FROM uplate WHERE projekatId = :projekatId ORDER BY sortOrder ASC, datum DESC")
    fun uplateZaProjekat(projekatId: Long): Flow<List<Uplata>>

    @Query("SELECT * FROM uplate WHERE projekatId = :projekatId AND datum BETWEEN :start AND :end")
    suspend fun uplateZaPeriod(projekatId: Long, start: Long, end: Long): List<Uplata>

    @Query("SELECT SUM(iznos) FROM uplate WHERE projekatId = :projekatId")
    suspend fun ukupneUplate(projekatId: Long): Double?

    @Insert
    suspend fun dodajUplatu(uplata: Uplata)

    @Update
    suspend fun azurirajUplatu(uplata: Uplata)

    @Delete
    suspend fun obrisiUplatu(uplata: Uplata)

    // Statistika po mesecu - SVE projekti
    @Query("SELECT * FROM radni_sati WHERE datum BETWEEN :start AND :end")
    suspend fun sviSatiZaPeriod(start: Long, end: Long): List<RadniSat>

    @Query("SELECT * FROM troskovi WHERE datum BETWEEN :start AND :end")
    suspend fun sviTroskoviZaPeriod(start: Long, end: Long): List<Trosak>

    @Query("SELECT * FROM uplate WHERE datum BETWEEN :start AND :end")
    suspend fun sveUplateZaPeriod(start: Long, end: Long): List<Uplata>
}