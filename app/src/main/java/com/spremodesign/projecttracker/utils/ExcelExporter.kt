package com.example.projecttracker.utils

import android.content.Context
import com.example.projecttracker.data.*
import org.apache.poi.ss.usermodel.*
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

class ExcelExporter(private val context: Context) {

    private val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
    private val currencyFormat = java.text.NumberFormat.getCurrencyInstance(Locale("sr", "RS"))

    suspend fun exportProjekat(
        projekat: Projekat,
        sati: List<RadniSat>,
        troskovi: List<Trosak>,
        uplate: List<Uplata>,
        database: AppDatabase
    ): File {
        val workbook = XSSFWorkbook()

        // Sheet 1: Pregled projekta
        createPregledSheet(workbook, projekat, sati, troskovi, uplate, database)

        // Sheet 2: Radni sati
        createSatiSheet(workbook, sati)

        // Sheet 3: Troškovi
        createTroskoviSheet(workbook, troskovi)

        // Sheet 4: Uplate
        createUplateSheet(workbook, uplate)

        // Sačuvaj fajl
        val fileName = "${projekat.naziv}_${System.currentTimeMillis()}.xlsx"
        val file = File(context.getExternalFilesDir(null), fileName)

        FileOutputStream(file).use { outputStream ->
            workbook.write(outputStream)
        }

        workbook.close()
        return file
    }

    private suspend fun createPregledSheet(
        workbook: Workbook,
        projekat: Projekat,
        sati: List<RadniSat>,
        troskovi: List<Trosak>,
        uplate: List<Uplata>,
        database: AppDatabase
    ) {
        val sheet = workbook.createSheet("Pregled")
        val headerStyle = createHeaderStyle(workbook)
        val goldStyle = createGoldStyle(workbook)

        var rowNum = 0

        // Naziv projekta
        val titleRow = sheet.createRow(rowNum++)
        val titleCell = titleRow.createCell(0)
        titleCell.setCellValue(projekat.naziv)
        titleCell.cellStyle = createTitleStyle(workbook)

        rowNum++

        // Info o projektu
        createInfoRow(sheet, rowNum++, "Klijent:", projekat.klijent, headerStyle)
        createInfoRow(sheet, rowNum++, "Datum početka:", dateFormat.format(Date(projekat.datumPocetka)), headerStyle)
        createInfoRow(sheet, rowNum++, "Dogovorena suma:", currencyFormat.format(projekat.dogovorenaSuma), goldStyle)

        rowNum++

        // Statistika
        val ukupnoSati = database.projekatDao().ukupnoSati(projekat.id) ?: 0.0
        val ukupniTroskovi = database.projekatDao().ukupniTroskovi(projekat.id) ?: 0.0
        val ukupneUplate = database.projekatDao().ukupneUplate(projekat.id) ?: 0.0
        val zarada = projekat.dogovorenaSuma - ukupniTroskovi
        val zaradaPoSatu = if (ukupnoSati > 0) zarada / ukupnoSati else 0.0

        createInfoRow(sheet, rowNum++, "Ukupno sati:", String.format("%.2f h", ukupnoSati), headerStyle)
        createInfoRow(sheet, rowNum++, "Ukupni troškovi:", currencyFormat.format(ukupniTroskovi), headerStyle)
        createInfoRow(sheet, rowNum++, "Ukupne uplate:", currencyFormat.format(ukupneUplate), goldStyle)
        createInfoRow(sheet, rowNum++, "Zarada:", currencyFormat.format(zarada), goldStyle)
        createInfoRow(sheet, rowNum++, "Zarada po satu:", currencyFormat.format(zaradaPoSatu), goldStyle)

        // Auto-size kolone
        sheet.autoSizeColumn(0)
        sheet.autoSizeColumn(1)
    }

    private fun createSatiSheet(workbook: Workbook, sati: List<RadniSat>) {
        val sheet = workbook.createSheet("Radni Sati")
        val headerStyle = createHeaderStyle(workbook)

        // Header
        val headerRow = sheet.createRow(0)
        val headers = arrayOf("Datum", "Broj sati", "Opis")
        headers.forEachIndexed { index, header ->
            val cell = headerRow.createCell(index)
            cell.setCellValue(header)
            cell.cellStyle = headerStyle
        }

        // Podaci
        sati.forEachIndexed { index, sat ->
            val row = sheet.createRow(index + 1)
            row.createCell(0).setCellValue(dateFormat.format(Date(sat.datum)))
            row.createCell(1).setCellValue(sat.brojSati)
            row.createCell(2).setCellValue(sat.opis)
        }

        // Auto-size
        for (i in 0..2) sheet.autoSizeColumn(i)
    }

    private fun createTroskoviSheet(workbook: Workbook, troskovi: List<Trosak>) {
        val sheet = workbook.createSheet("Troškovi")
        val headerStyle = createHeaderStyle(workbook)

        val headerRow = sheet.createRow(0)
        val headers = arrayOf("Datum", "Iznos", "Kategorija", "Opis", "Račun")
        headers.forEachIndexed { index, header ->
            val cell = headerRow.createCell(index)
            cell.setCellValue(header)
            cell.cellStyle = headerStyle
        }

        troskovi.forEachIndexed { index, trosak ->
            val row = sheet.createRow(index + 1)
            row.createCell(0).setCellValue(dateFormat.format(Date(trosak.datum)))
            row.createCell(1).setCellValue(trosak.iznos)
            row.createCell(2).setCellValue(trosak.kategorija)
            row.createCell(3).setCellValue(trosak.opis)
            row.createCell(4).setCellValue(if (trosak.putanjaDoSlike != null) "DA" else "NE")
        }

        for (i in 0..4) sheet.autoSizeColumn(i)
    }

    private fun createUplateSheet(workbook: Workbook, uplate: List<Uplata>) {
        val sheet = workbook.createSheet("Uplate")
        val headerStyle = createHeaderStyle(workbook)

        val headerRow = sheet.createRow(0)
        val headers = arrayOf("Datum", "Iznos", "Opis")
        headers.forEachIndexed { index, header ->
            val cell = headerRow.createCell(index)
            cell.setCellValue(header)
            cell.cellStyle = headerStyle
        }

        uplate.forEachIndexed { index, uplata ->
            val row = sheet.createRow(index + 1)
            row.createCell(0).setCellValue(dateFormat.format(Date(uplata.datum)))
            row.createCell(1).setCellValue(uplata.iznos)
            row.createCell(2).setCellValue(uplata.opis)
        }

        for (i in 0..2) sheet.autoSizeColumn(i)
    }

    private fun createInfoRow(sheet: Sheet, rowNum: Int, label: String, value: String, style: CellStyle) {
        val row = sheet.createRow(rowNum)
        row.createCell(0).setCellValue(label)
        val valueCell = row.createCell(1)
        valueCell.setCellValue(value)
        valueCell.cellStyle = style
    }

    private fun createHeaderStyle(workbook: Workbook): CellStyle {
        val style = workbook.createCellStyle()
        val font = workbook.createFont()
        font.bold = true
        font.color = IndexedColors.WHITE.index
        style.setFont(font)
        style.fillForegroundColor = IndexedColors.DARK_BLUE.index
        style.fillPattern = FillPatternType.SOLID_FOREGROUND
        return style
    }

    private fun createGoldStyle(workbook: Workbook): CellStyle {
        val style = workbook.createCellStyle()
        val font = workbook.createFont()
        font.bold = true
        font.color = IndexedColors.GOLD.index
        style.setFont(font)
        return style
    }

    private fun createTitleStyle(workbook: Workbook): CellStyle {
        val style = workbook.createCellStyle()
        val font = workbook.createFont()
        font.bold = true
        font.fontHeightInPoints = 18
        style.setFont(font)
        return style
    }
}