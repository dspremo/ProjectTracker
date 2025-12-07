package com.example.projecttracker.ui

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.projecttracker.data.AppDatabase
import com.example.projecttracker.ui.theme.*
import com.example.projecttracker.utils.BackupInfo
import com.example.projecttracker.utils.ExcelExporter
import com.example.projecttracker.utils.GoogleDriveHelper
import com.google.android.gms.auth.api.signin.GoogleSignIn
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class SettingsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ProjectTrackerTheme {
                SettingsScreen(onBack = { finish() })
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val authViewModel: AuthViewModel = viewModel()
    val authState by authViewModel.authState.collectAsState()
    val scope = rememberCoroutineScope()

    var isLoading by remember { mutableStateOf(false) }
    var loadingMessage by remember { mutableStateOf("") }
    var showMonthPicker by remember { mutableStateOf(false) }
    var showBackupList by remember { mutableStateOf(false) }
    var backups by remember { mutableStateOf<List<BackupInfo>>(emptyList()) }

    val signInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            authViewModel.handleSignInResult(result.data)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Podešavanja", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Nazad")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Surface,
                    titleContentColor = TextPrimary
                )
            )
        },
        containerColor = SurfaceDark
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Google Account sekcija
            SettingsSection(title = "Google Nalog") {
                if (authState.isSignedIn) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = authState.userName ?: "Korisnik",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Text(
                                text = authState.userEmail ?: "",
                                fontSize = 14.sp,
                                color = TextSecondary
                            )
                        }
                        TextButton(onClick = { authViewModel.signOut() }) {
                            Text("Odjavi se", color = GoldPrimary)
                        }
                    }
                } else {
                    SettingsButton(
                        icon = Icons.Default.Person,
                        title = "Prijavi se sa Google",
                        subtitle = "Potrebno za backup i izvještaje",
                        onClick = { signInLauncher.launch(authViewModel.getSignInIntent()) }
                    )
                }
            }

            // Backup sekcija
            SettingsSection(title = "Backup & Restore") {
                SettingsButton(
                    icon = Icons.Default.CloudUpload,
                    title = "Backup na Google Drive",
                    subtitle = "Sačuvaj bazu podataka u cloud",
                    enabled = authState.isSignedIn && !isLoading,
                    onClick = {
                        scope.launch {
                            isLoading = true
                            loadingMessage = "Pravim backup..."
                            
                            val account = GoogleSignIn.getLastSignedInAccount(context)
                            if (account != null) {
                                val driveHelper = GoogleDriveHelper(context)
                                val dbFile = context.getDatabasePath("project_tracker_db")
                                
                                val result = driveHelper.backupDatabase(dbFile, account)
                                result.fold(
                                    onSuccess = {
                                        Toast.makeText(context, "Backup uspješan!", Toast.LENGTH_SHORT).show()
                                    },
                                    onFailure = {
                                        Toast.makeText(context, "Greška: ${it.message}", Toast.LENGTH_LONG).show()
                                    }
                                )
                            }
                            isLoading = false
                        }
                    }
                )

                SettingsButton(
                    icon = Icons.Default.CloudDownload,
                    title = "Restore iz backup-a",
                    subtitle = "Vrati podatke iz cloud-a",
                    enabled = authState.isSignedIn && !isLoading,
                    onClick = {
                        scope.launch {
                            isLoading = true
                            loadingMessage = "Učitavam backup-e..."
                            
                            val account = GoogleSignIn.getLastSignedInAccount(context)
                            if (account != null) {
                                val driveHelper = GoogleDriveHelper(context)
                                val result = driveHelper.listBackups(account)
                                result.fold(
                                    onSuccess = { list ->
                                        backups = list
                                        showBackupList = true
                                    },
                                    onFailure = {
                                        Toast.makeText(context, "Greška: ${it.message}", Toast.LENGTH_LONG).show()
                                    }
                                )
                            }
                            isLoading = false
                        }
                    }
                )
            }

            // Izvještaji sekcija
            SettingsSection(title = "Izvještaji") {
                SettingsButton(
                    icon = Icons.Default.Description,
                    title = "Mjesečni izvještaj",
                    subtitle = "Generiši Excel i upload na Drive",
                    enabled = authState.isSignedIn && !isLoading,
                    onClick = { showMonthPicker = true }
                )
            }

            // Loading indicator
            if (isLoading) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Surface)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = GoldPrimary
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(loadingMessage, color = TextSecondary)
                    }
                }
            }

            // Info
            if (!authState.isSignedIn) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = BrownLight.copy(alpha = 0.3f))
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Info,
                            contentDescription = null,
                            tint = GoldPrimary
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            "Prijavi se sa Google nalogom za korištenje backup-a i izvještaja.",
                            fontSize = 14.sp,
                            color = TextSecondary
                        )
                    }
                }
            }
        }
    }

    // Month picker dialog
    if (showMonthPicker) {
        MonthPickerDialog(
            onDismiss = { showMonthPicker = false },
            onMonthSelected = { year, month ->
                showMonthPicker = false
                scope.launch {
                    isLoading = true
                    loadingMessage = "Generiram izvještaj..."
                    
                    val account = GoogleSignIn.getLastSignedInAccount(context)
                    if (account != null) {
                        val database = AppDatabase.getInstance(context)
                        val exporter = ExcelExporter(context)
                        val driveHelper = GoogleDriveHelper(context)
                        
                        try {
                            val file = exporter.exportMonthlyReport(year, month, database)
                            loadingMessage = "Upload na Drive..."
                            
                            val monthYear = "${year}_${String.format("%02d", month)}"
                            val result = driveHelper.uploadExcelReport(file, account, monthYear)
                            
                            result.fold(
                                onSuccess = { link ->
                                    Toast.makeText(context, "Izvještaj uploadovan!", Toast.LENGTH_SHORT).show()
                                },
                                onFailure = {
                                    Toast.makeText(context, "Greška: ${it.message}", Toast.LENGTH_LONG).show()
                                }
                            )
                        } catch (e: Exception) {
                            Toast.makeText(context, "Greška: ${e.message}", Toast.LENGTH_LONG).show()
                        }
                    }
                    isLoading = false
                }
            }
        )
    }

    // Backup list dialog
    if (showBackupList) {
        BackupListDialog(
            backups = backups,
            onDismiss = { showBackupList = false },
            onRestore = { backup ->
                showBackupList = false
                scope.launch {
                    isLoading = true
                    loadingMessage = "Restoriram backup..."
                    
                    val account = GoogleSignIn.getLastSignedInAccount(context)
                    if (account != null) {
                        val driveHelper = GoogleDriveHelper(context)
                        val dbFile = context.getDatabasePath("project_tracker_db")
                        
                        // Zatvori bazu prije restore-a
                        AppDatabase.closeDatabase()
                        
                        val result = driveHelper.restoreDatabase(backup.id, dbFile, account)
                        result.fold(
                            onSuccess = {
                                Toast.makeText(context, "Restore uspješan! Restartuj app.", Toast.LENGTH_LONG).show()
                            },
                            onFailure = {
                                Toast.makeText(context, "Greška: ${it.message}", Toast.LENGTH_LONG).show()
                            }
                        )
                    }
                    isLoading = false
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
            fontWeight = FontWeight.Bold,
            color = GoldPrimary,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Surface)
        ) {
            Column(
                modifier = Modifier.padding(8.dp),
                content = content
            )
        }
    }
}

@Composable
fun SettingsButton(
    icon: ImageVector,
    title: String,
    subtitle: String,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier.fillMaxWidth(),
        color = Surface,
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = if (enabled) GoldPrimary else TextDisabled,
                modifier = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = if (enabled) TextPrimary else TextDisabled
                )
                Text(
                    text = subtitle,
                    fontSize = 12.sp,
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

@Composable
fun MonthPickerDialog(
    onDismiss: () -> Unit,
    onMonthSelected: (Int, Int) -> Unit
) {
    val calendar = Calendar.getInstance()
    var selectedYear by remember { mutableStateOf(calendar.get(Calendar.YEAR)) }
    var selectedMonth by remember { mutableStateOf(calendar.get(Calendar.MONTH) + 1) }

    val months = listOf(
        "Januar", "Februar", "Mart", "April", "Maj", "Jun",
        "Jul", "Avgust", "Septembar", "Oktobar", "Novembar", "Decembar"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Odaberi mjesec", fontWeight = FontWeight.Bold) },
        text = {
            Column {
                // Godina
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { selectedYear-- }) {
                        Icon(Icons.Default.ChevronLeft, contentDescription = "Prethodna")
                    }
                    Text(
                        text = selectedYear.toString(),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = { selectedYear++ }) {
                        Icon(Icons.Default.ChevronRight, contentDescription = "Sljedeća")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Mjeseci u gridu
                Column {
                    for (row in 0..3) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            for (col in 0..2) {
                                val monthIndex = row * 3 + col + 1
                                FilterChip(
                                    selected = selectedMonth == monthIndex,
                                    onClick = { selectedMonth = monthIndex },
                                    label = { Text(months[monthIndex - 1].take(3)) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = GoldPrimary,
                                        selectedLabelColor = SurfaceDark
                                    )
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onMonthSelected(selectedYear, selectedMonth) },
                colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary)
            ) {
                Text("Generiši", color = SurfaceDark, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Otkaži", color = TextSecondary)
            }
        },
        containerColor = Surface
    )
}

@Composable
fun BackupListDialog(
    backups: List<BackupInfo>,
    onDismiss: () -> Unit,
    onRestore: (BackupInfo) -> Unit
) {
    val dateFormat = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Odaberi backup", fontWeight = FontWeight.Bold) },
        text = {
            if (backups.isEmpty()) {
                Text("Nema dostupnih backup-a.", color = TextSecondary)
            } else {
                Column {
                    backups.forEach { backup ->
                        Surface(
                            onClick = { onRestore(backup) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            color = BrownLight.copy(alpha = 0.3f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = backup.name,
                                    fontWeight = FontWeight.Medium,
                                    color = TextPrimary
                                )
                                Text(
                                    text = dateFormat.format(Date(backup.createdTime)),
                                    fontSize = 12.sp,
                                    color = TextSecondary
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Zatvori", color = TextSecondary)
            }
        },
        containerColor = Surface
    )
}
