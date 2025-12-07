package com.example.projecttracker.ui

import android.content.Intent
import android.os.Bundle
import android.os.Environment
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import com.example.projecttracker.data.AppDatabase
import com.example.projecttracker.ui.theme.*
import com.example.projecttracker.utils.ExcelExporter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

class SettingsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ProjectTrackerTheme {
                SettingsScreen(
                    onBackClick = { finish() }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var isExporting by remember { mutableStateOf(false) }
    var isBackingUp by remember { mutableStateOf(false) }
    var showMonthPicker by remember { mutableStateOf(false) }
    var selectedYear by remember { mutableStateOf(Calendar.getInstance().get(Calendar.YEAR)) }
    var selectedMonth by remember { mutableStateOf(Calendar.getInstance().get(Calendar.MONTH)) }

    Scaffold(
        topBar = {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Surface.copy(alpha = 0.95f),
                shadowElevation = 8.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = onBackClick) {
                            Icon(
                                Icons.Default.ArrowBack,
                                contentDescription = "Nazad",
                                tint = TextPrimary
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Podešavanja",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    }
                }
            }
        },
        containerColor = Background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Export Section
            SettingsSection(title = "Export") {
                SettingsCard(
                    icon = Icons.Default.TableChart,
                    title = "Izvezi mesečni izveštaj",
                    description = "Kreiraj Excel fajl sa podacima za izabrani mesec",
                    isLoading = isExporting,
                    onClick = { showMonthPicker = true }
                )
            }

            // Backup Section
            SettingsSection(title = "Backup") {
                SettingsCard(
                    icon = Icons.Default.Backup,
                    title = "Napravi backup",
                    description = "Sačuvaj bazu podataka lokalno",
                    isLoading = isBackingUp,
                    onClick = {
                        scope.launch {
                            isBackingUp = true
                            try {
                                val result = createLocalBackup(context as ComponentActivity)
                                withContext(Dispatchers.Main) {
                                    Toast.makeText(context, result, Toast.LENGTH_LONG).show()
                                }
                            } catch (e: Exception) {
                                withContext(Dispatchers.Main) {
                                    Toast.makeText(context, "Greška: ${e.message}", Toast.LENGTH_LONG).show()
                                }
                            }
                            isBackingUp = false
                        }
                    }
                )

                Spacer(modifier = Modifier.height(8.dp))

                SettingsCard(
                    icon = Icons.Default.Restore,
                    title = "Vrati backup",
                    description = "Učitaj prethodno sačuvanu bazu",
                    onClick = {
                        scope.launch {
                            try {
                                val result = restoreLocalBackup(context as ComponentActivity)
                                withContext(Dispatchers.Main) {
                                    Toast.makeText(context, result, Toast.LENGTH_LONG).show()
                                }
                            } catch (e: Exception) {
                                withContext(Dispatchers.Main) {
                                    Toast.makeText(context, "Greška: ${e.message}", Toast.LENGTH_LONG).show()
                                }
                            }
                        }
                    }
                )
            }

            // Info Section
            SettingsSection(title = "Informacije") {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = CardBackground),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "Project Tracker",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = "Verzija 1.0",
                            fontSize = 14.sp,
                            color = TextSecondary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Aplikacija za praćenje projekata, radnih sati, troškova i uplata.",
                            fontSize = 14.sp,
                            color = TextSecondary
                        )
                    }
                }
            }
        }
    }

    // Month Picker Dialog
    if (showMonthPicker) {
        AlertDialog(
            onDismissRequest = { showMonthPicker = false },
            containerColor = CardBackground,
            title = {
                Text("Izaberi mesec", color = TextPrimary)
            },
            text = {
                Column {
                    // Year selector
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = { selectedYear-- }) {
                            Icon(Icons.Default.ChevronLeft, "Prethodna godina", tint = GoldPrimary)
                        }
                        Text(
                            text = selectedYear.toString(),
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        IconButton(onClick = { selectedYear++ }) {
                            Icon(Icons.Default.ChevronRight, "Sledeća godina", tint = GoldPrimary)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Month grid
                    val months = listOf(
                        "Jan", "Feb", "Mar", "Apr", "Maj", "Jun",
                        "Jul", "Avg", "Sep", "Okt", "Nov", "Dec"
                    )

                    Column {
                        for (row in 0..3) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                for (col in 0..2) {
                                    val monthIndex = row * 3 + col
                                    val isSelected = monthIndex == selectedMonth

                                    Surface(
                                        modifier = Modifier
                                            .weight(1f)
                                            .padding(4.dp),
                                        shape = RoundedCornerShape(8.dp),
                                        color = if (isSelected) GoldPrimary else CardBackground,
                                        onClick = { selectedMonth = monthIndex }
                                    ) {
                                        Text(
                                            text = months[monthIndex],
                                            modifier = Modifier.padding(12.dp),
                                            color = if (isSelected) Background else TextPrimary,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showMonthPicker = false
                        scope.launch {
                            isExporting = true
                            try {
                                val db = AppDatabase.getInstance(context)
                                val exporter = ExcelExporter(context)
                                val file = exporter.exportMonthlyReportInstance(
                                    year = selectedYear,
                                    month = selectedMonth,
                                    database = db
                                )

                                withContext(Dispatchers.Main) {
                                    // Share the file
                                    val uri = FileProvider.getUriForFile(
                                        context,
                                        "${context.packageName}.fileprovider",
                                        file
                                    )
                                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                        type = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
                                        putExtra(Intent.EXTRA_STREAM, uri)
                                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                    }
                                    context.startActivity(Intent.createChooser(shareIntent, "Podeli izveštaj"))
                                }
                            } catch (e: Exception) {
                                withContext(Dispatchers.Main) {
                                    Toast.makeText(context, "Greška: ${e.message}", Toast.LENGTH_LONG).show()
                                }
                            }
                            isExporting = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary)
                ) {
                    Text("Izvezi", color = Background)
                }
            },
            dismissButton = {
                TextButton(onClick = { showMonthPicker = false }) {
                    Text("Otkaži", color = TextSecondary)
                }
            }
        )
    }
}

@Composable
fun SettingsSection(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Column {
        Text(
            text = title,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = GoldPrimary,
            modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
        )
        content()
    }
}

@Composable
fun SettingsCard(
    icon: ImageVector,
    title: String,
    description: String,
    isLoading: Boolean = false,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        shape = RoundedCornerShape(12.dp),
        onClick = { if (!isLoading) onClick() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        color = GoldPrimary.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(12.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = GoldPrimary,
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(
                        icon,
                        contentDescription = null,
                        tint = GoldPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = TextPrimary
                )
                Text(
                    text = description,
                    fontSize = 13.sp,
                    color = TextSecondary
                )
            }

            Icon(
                Icons.Default.ChevronRight,
                contentDescription = null,
                tint = TextSecondary
            )
        }
    }
}

// Backup funkcije
private suspend fun createLocalBackup(context: ComponentActivity): String {
    return withContext(Dispatchers.IO) {
        val dbFile = context.getDatabasePath("project_tracker_db")
        if (!dbFile.exists()) {
            return@withContext "Baza podataka ne postoji"
        }

        // Close database before copying
        AppDatabase.closeDatabase()

        val backupDir = File(context.getExternalFilesDir(null), "backups")
        if (!backupDir.exists()) backupDir.mkdirs()

        val dateFormat = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
        val backupFile = File(backupDir, "backup_${dateFormat.format(Date())}.db")

        FileInputStream(dbFile).use { input ->
            FileOutputStream(backupFile).use { output ->
                input.copyTo(output)
            }
        }

        "Backup sačuvan: ${backupFile.name}"
    }
}

private suspend fun restoreLocalBackup(context: ComponentActivity): String {
    return withContext(Dispatchers.IO) {
        val backupDir = File(context.getExternalFilesDir(null), "backups")
        if (!backupDir.exists() || backupDir.listFiles()?.isEmpty() != false) {
            return@withContext "Nema dostupnih backup fajlova"
        }

        val backupFiles = backupDir.listFiles()?.filter { it.extension == "db" }?.sortedByDescending { it.lastModified() }
        if (backupFiles.isNullOrEmpty()) {
            return@withContext "Nema dostupnih backup fajlova"
        }

        val latestBackup = backupFiles.first()
        val dbFile = context.getDatabasePath("project_tracker_db")

        // Close database before restoring
        AppDatabase.closeDatabase()

        FileInputStream(latestBackup).use { input ->
            FileOutputStream(dbFile).use { output ->
                input.copyTo(output)
            }
        }

        "Backup vraćen: ${latestBackup.name}"
    }
}
