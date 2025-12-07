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

    /**
     * Generiši mjesečni izvještaj za sve projekte
     */
    suspend fun exportMonthlyReport(
        year: Int,
        month: Int,
        database: AppDatabase
    ): File {
        val workbook = XSSFWorkbook()
        
        val calendar = Calendar.getInstance()
        calendar.set(year, month - 1, 1, 0, 0, 0)
        val startOfMonth = calendar.timeInMillis
        
        calendar.set(year, month - 1, calendar.getActualMaximum(Calendar.DAY_OF_MONTH), 23, 59, 59)
        val endOfMonth = calendar.timeInMillis

        val monthName = java.text.SimpleDateFormat("MMMM yyyy", Locale("sr")).format(Date(startOfMonth))

        // Sheet 1: Sumarni pregled
        createMonthlySummarySheet(workbook, database, startOfMonth, endOfMonth, monthName)

        // Sheet 2: Detalji po projektu
        createMonthlyProjectDetailsSheet(workbook, database, startOfMonth, endOfMonth)

        // Sheet 3: Svi radni sati
        createMonthlyHoursSheet(workbook, database, startOfMonth, endOfMonth)

        // Sheet 4: Svi troškovi
        createMonthlyCostsSheet(workbook, database, startOfMonth, endOfMonth)

        // Sheet 5: Sve uplate
        createMonthlyPaymentsSheet(workbook, database, startOfMonth, endOfMonth)

        val fileName = "Izvještaj_${year}_${String.format("%02d", month)}.xlsx"
        val file = File(context.getExternalFilesDir(null), fileName)

        FileOutputStream(file).use { outputStream ->
            workbook.write(outputStream)
        }

        workbook.close()
        return file
    }

    private suspend fun createMonthlySummarySheet(
        workbook: Workbook,
        database: AppDatabase,
        startOfMonth: Long,
        endOfMonth: Long,
        monthName: String
    ) {
        val sheet = workbook.createSheet("Sumarni Pregled")
        val headerStyle = createHeaderStyle(workbook)
        val goldStyle = createGoldStyle(workbook)
        val titleStyle = createTitleStyle(workbook)

        var rowNum = 0

        // Naslov
        val titleRow = sheet.createRow(rowNum++)
        val titleCell = titleRow.createCell(0)
        titleCell.setCellValue("Mjesečni Izvještaj - $monthName")
        titleCell.cellStyle = titleStyle

        rowNum++

        // Dohvati sve podatke
        val allHours = database.projekatDao().sviSatiZaPeriod(startOfMonth, endOfMonth)
        val allCosts = database.projekatDao().sviTroskoviZaPeriod(startOfMonth, endOfMonth)
        val allPayments = database.projekatDao().sveUplateZaPeriod(startOfMonth, endOfMonth)

        val totalHours = allHours.sumOf { it.brojSati }
        val totalCosts = allCosts.sumOf { it.iznos }
        val totalPayments = allPayments.sumOf { it.iznos }
        val netProfit = totalPayments - totalCosts

        // Sumarni podaci
        createInfoRow(sheet, rowNum++, "Ukupno radnih sati:", String.format("%.2f h", totalHours), headerStyle)
        createInfoRow(sheet, rowNum++, "Ukupni troškovi:", currencyFormat.format(totalCosts), headerStyle)
        createInfoRow(sheet, rowNum++, "Ukupne uplate:", currencyFormat.format(totalPayments), goldStyle)
        createInfoRow(sheet, rowNum++, "Neto zarada:", currencyFormat.format(netProfit), goldStyle)

        if (totalHours > 0) {
            val hourlyRate = netProfit / totalHours
            createInfoRow(sheet, rowNum++, "Prosječna satnica:", currencyFormat.format(hourlyRate), goldStyle)
        }

        rowNum++
        
        // Broj projekata
        val projectIds = (allHours.map { it.projekatId } + allCosts.map { it.projekatId } + allPayments.map { it.projekatId }).distinct()
        createInfoRow(sheet, rowNum++, "Aktivnih projekata:", projectIds.size.toString(), headerStyle)

        sheet.autoSizeColumn(0)
        sheet.autoSizeColumn(1)
    }

    private suspend fun createMonthlyProjectDetailsSheet(
        workbook: Workbook,
        database: AppDatabase,
        startOfMonth: Long,
        endOfMonth: Long
    ) {
        val sheet = workbook.createSheet("Po Projektu")
        val headerStyle = createHeaderStyle(workbook)

        // Header
        val headerRow = sheet.createRow(0)
        val headers = arrayOf("Projekat", "Klijent", "Sati", "Troškovi", "Uplate", "Neto")
        headers.forEachIndexed { index, header ->
            val cell = headerRow.createCell(index)
            cell.setCellValue(header)
            cell.cellStyle = headerStyle
        }

        val projekti = database.projekatDao().dohvatiSve()
        var rowNum = 1

        for (projekat in projekti) {
            val sati = database.projekatDao().radniSatiZaPeriod(projekat.id, startOfMonth, endOfMonth)
            val troskovi = database.projekatDao().troskoviZaPeriod(projekat.id, startOfMonth, endOfMonth)
            val uplate = database.projekatDao().uplateZaPeriod(projekat.id, startOfMonth, endOfMonth)

            val totalHours = sati.sumOf { it.brojSati }
            val totalCosts = troskovi.sumOf { it.iznos }
            val totalPayments = uplate.sumOf { it.iznos }

            // Preskoči projekte bez aktivnosti
            if (totalHours == 0.0 && totalCosts == 0.0 && totalPayments == 0.0) continue

            val row = sheet.createRow(rowNum++)
            row.createCell(0).setCellValue(projekat.naziv)
            row.createCell(1).setCellValue(projekat.klijent)
            row.createCell(2).setCellValue(totalHours)
            row.createCell(3).setCellValue(totalCosts)
            row.createCell(4).setCellValue(totalPayments)
            row.createCell(5).setCellValue(totalPayments - totalCosts)
        }

        for (i in 0..5) sheet.autoSizeColumn(i)
    }

    private suspend fun createMonthlyHoursSheet(
        workbook: Workbook,
        database: AppDatabase,
        startOfMonth: Long,
        endOfMonth: Long
    ) {
        val sheet = workbook.createSheet("Radni Sati")
        val headerStyle = createHeaderStyle(workbook)

        val headerRow = sheet.createRow(0)
        val headers = arrayOf("Datum", "Projekat", "Sati", "Opis")
        headers.forEachIndexed { index, header ->
            val cell = headerRow.createCell(index)
            cell.setCellValue(header)
            cell.cellStyle = headerStyle
        }

        val allHours = database.projekatDao().sviSatiZaPeriod(startOfMonth, endOfMonth)
        val projekti = database.projekatDao().dohvatiSve().associateBy { it.id }

        allHours.sortedByDescending { it.datum }.forEachIndexed { index, sat ->
            val row = sheet.createRow(index + 1)
            row.createCell(0).setCellValue(dateFormat.format(Date(sat.datum)))
            row.createCell(1).setCellValue(projekti[sat.projekatId]?.naziv ?: "Nepoznat")
            row.createCell(2).setCellValue(sat.brojSati)
            row.createCell(3).setCellValue(sat.opis)
        }

        for (i in 0..3) sheet.autoSizeColumn(i)
    }

    private suspend fun createMonthlyCostsSheet(
        workbook: Workbook,
        database: AppDatabase,
        startOfMonth: Long,
        endOfMonth: Long
    ) {
        val sheet = workbook.createSheet("Troškovi")
        val headerStyle = createHeaderStyle(workbook)

        val headerRow = sheet.createRow(0)
        val headers = arrayOf("Datum", "Projekat", "Iznos", "Kategorija", "Opis")
        headers.forEachIndexed { index, header ->
            val cell = headerRow.createCell(index)
            cell.setCellValue(header)
            cell.cellStyle = headerStyle
        }

        val allCosts = database.projekatDao().sviTroskoviZaPeriod(startOfMonth, endOfMonth)
        val projekti = database.projekatDao().dohvatiSve().associateBy { it.id }

        allCosts.sortedByDescending { it.datum }.forEachIndexed { index, trosak ->
            val row = sheet.createRow(index + 1)
            row.createCell(0).setCellValue(dateFormat.format(Date(trosak.datum)))
            row.createCell(1).setCellValue(projekti[trosak.projekatId]?.naziv ?: "Nepoznat")
            row.createCell(2).setCellValue(trosak.iznos)
            row.createCell(3).setCellValue(trosak.kategorija)
            row.createCell(4).setCellValue(trosak.opis)
        }

        for (i in 0..4) sheet.autoSizeColumn(i)
    }

    private suspend fun createMonthlyPaymentsSheet(
        workbook: Workbook,
        database: AppDatabase,
        startOfMonth: Long,
        endOfMonth: Long
    ) {
        val sheet = workbook.createSheet("Uplate")
        val headerStyle = createHeaderStyle(workbook)

        val headerRow = sheet.createRow(0)
        val headers = arrayOf("Datum", "Projekat", "Iznos", "Opis")
        headers.forEachIndexed { index, header ->
            val cell = headerRow.createCell(index)
            cell.setCellValue(header)
            cell.cellStyle = headerStyle
        }

        val allPayments = database.projekatDao().sveUplateZaPeriod(startOfMonth, endOfMonth)
        val projekti = database.projekatDao().dohvatiSve().associateBy { it.id }

        allPayments.sortedByDescending { it.datum }.forEachIndexed { index, uplata ->
            val row = sheet.createRow(index + 1)
            row.createCell(0).setCellValue(dateFormat.format(Date(uplata.datum)))
            row.createCell(1).setCellValue(projekti[uplata.projekatId]?.naziv ?: "Nepoznat")
            row.createCell(2).setCellValue(uplata.iznos)
            row.createCell(3).setCellValue(uplata.opis)
        }

        for (i in 0..3) sheet.autoSizeColumn(i)
    }
}