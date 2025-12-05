package com.example.projecttracker.ui

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.projecttracker.data.*
import com.example.projecttracker.ui.theme.*
import com.example.projecttracker.utils.ExcelExporter
//import com.example.projecttracker.utils.GoogleDriveHelper
//import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.example.projecttracker.ui.DodajSateDialog
import com.example.projecttracker.ui.DodajTrosakDialog
import com.example.projecttracker.ui.DodajUplatuDialog
import com.example.projecttracker.ui.EditSateDialog
import com.example.projecttracker.ui.EditTrosakDialog
import com.example.projecttracker.ui.EditUplatuDialog
import com.example.projecttracker.ui.EmptyState
import kotlinx.coroutines.launch
import org.burnoutcrew.reorderable.*
import java.io.File
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

class ProjekatDetaljiActivity : ComponentActivity() {
//    private lateinit var googleDriveHelper: GoogleDriveHelper
    private var projekatId: Long = 0

    private val signInLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
//            val account = GoogleSignIn.getSignedInAccountFromIntent(result.data).result
            // Handle upload after sign-in
        }
    }

    private val pickImageLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            // Handle slika picked
            currentPickedImage = it
        }
    }

    private var currentPickedImage: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        projekatId = intent.getLongExtra("projekat_id", 0)
//        googleDriveHelper = GoogleDriveHelper(this)

        setContent {
            ProjectTrackerTheme {
                ProjekatDetaljiScreen(
                    projekatId = projekatId,
                    onBack = { finish() },
                    onPickImage = { pickImageLauncher.launch("image/*") },
                    onExportExcel = { handleExcelExport() },
                    onUploadToDrive = { handleDriveUpload() }
                )
            }
        }
    }

    private fun handleExcelExport() {
        // Excel export logic
        Toast.makeText(this, "Eksportujem u Excel...", Toast.LENGTH_SHORT).show()
    }

    private fun handleDriveUpload() {
//        val account = GoogleSignIn.getLastSignedInAccount(this)
//        if (account == null) {
////            signInLauncher.launch(googleDriveHelper.getSignInIntent())
//        } else {
//            // Upload logic
//            Toast.makeText(this, "Upload na Drive...", Toast.LENGTH_SHORT).show()
//        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProjekatDetaljiScreen(
    projekatId: Long,
    onBack: () -> Unit,
    onPickImage: () -> Unit,
    onExportExcel: () -> Unit,
    onUploadToDrive: () -> Unit
) {
    val viewModel: ProjekatDetaljiViewModel = viewModel(
        factory = ProjekatDetaljiViewModelFactory(
            LocalContext.current.applicationContext as android.app.Application,
            projekatId
        )
    )
    var editingSat by remember { mutableStateOf<RadniSat?>(null) }
    var editingTrosak by remember { mutableStateOf<Trosak?>(null) }
    var editingUplata by remember { mutableStateOf<Uplata?>(null) }

    editingSat?.let { sat ->
        EditSateDialog(
            sat = sat,
            onDismiss = { editingSat = null },
            onSave = { brojSati, opis, datum ->
                viewModel.azurirajSate(sat, brojSati, opis, datum)
                editingSat = null
            }
        )
    }

    editingTrosak?.let { trosak ->
        EditTrosakDialog(
            trosak = trosak,
            onDismiss = { editingTrosak = null },
            onPickImage = onPickImage,
            onSave = { iznos, opis, kategorija, datum ->
                viewModel.azurirajTrosak(trosak, iznos, opis, kategorija, trosak.putanjaDoSlike, datum)
                editingTrosak = null
            }
        )
    }

    editingUplata?.let { uplata ->
        EditUplatuDialog(
            uplata = uplata,
            onDismiss = { editingUplata = null },
            onSave = { iznos, opis, datum ->
                viewModel.azurirajUplatu(uplata, iznos, opis, datum)
                editingUplata = null
            }
        )
    }

    val projekat by viewModel.projekat.collectAsState()
    val sati by viewModel.sati.collectAsState()
    val troskovi by viewModel.troskovi.collectAsState()
    val uplate by viewModel.uplate.collectAsState()
    val stats by viewModel.stats.collectAsState()

    var selectedTab by remember { mutableStateOf(0) }
    var showDialog by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = projekat?.naziv ?: "...",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = projekat?.klijent ?: "",
                            fontSize = 12.sp,
                            color = GoldPrimary
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, "Nazad")
                    }
                },
                actions = {
                    IconButton(onClick = onExportExcel) {
                        Icon(Icons.Default.TableChart, "Excel", tint = GoldPrimary)
                    }
                    IconButton(onClick = onUploadToDrive) {
                        Icon(Icons.Default.CloudUpload, "Drive", tint = GoldPrimary)
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
        ) {

            // Statistika kartice
            StatistikaCards(stats)

            // Tab bar
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = Surface,
                contentColor = GoldPrimary,
                indicator = { tabPositions ->
                    TabRowDefaults.Indicator(
                        Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                        color = GoldPrimary,
                        height = 4.dp
                    )
                }
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("Sati") }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Troškovi") }
                )
                Tab(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    text = { Text("Uplate") }
                )
            }

            // Content
            when (selectedTab) {
                0 -> SatiTab(
                    sati = sati,
                    onAdd = { showDialog = "sati" },
                    { sat -> editingSat = sat },
                    onDelete = { viewModel.obrisiSat(it) }
                )
                1 -> TroskoviTab(
                    troskovi = troskovi,
                    onAdd = { showDialog = "trosak" },
                    onEdit = { trosak -> editingTrosak = trosak },
                    onDelete = { viewModel.obrisiTrosak(it) },
                    onImageClick = { /* TODO */ }
                )
                2 -> UplateTab(
                    uplate = uplate,
                    onAdd = { showDialog = "uplata" },
                    onEdit = { uplata -> editingUplata = uplata },
                    onDelete = { viewModel.obrisiUplatu(it) }
                )
            }
        }
    }

    // Dialozi za unos
    when (showDialog) {
        "sati" -> DodajSateDialog(
            onDismiss = { showDialog = null },
            onSave = { brojSati, opis, datum ->
                viewModel.dodajSate(brojSati, opis, datum)
                showDialog = null
            }
        )
        "trosak" -> DodajTrosakDialog(
            onDismiss = { showDialog = null },
            onPickImage = onPickImage,
            onSave = { iznos, opis, kategorija, datum ->
                viewModel.dodajTrosak(iznos, opis, kategorija, null, datum)
                showDialog = null
            }
        )
        "uplata" -> DodajUplatuDialog(
            onDismiss = { showDialog = null },
            onSave = { iznos, opis, datum ->
                viewModel.dodajUplatu(iznos, opis, datum)
                showDialog = null
            }
        )
    }
}

@Composable
fun StatistikaCards(stats: ProjekatStats?) {
    val currencyFormat = remember { NumberFormat.getCurrencyInstance(Locale("sr", "RS")) }

    stats?.let {
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                StatCard(
                    label = "Ukupno sati",
                    value = "${String.format("%.1f", it.ukupnoSati)}h",
                    icon = Icons.Default.Schedule,
                    color = BrownLight
                )
            }
            item {
                StatCard(
                    label = "Zarada",
                    value = currencyFormat.format(it.zarada),
                    icon = Icons.Default.TrendingUp,
                    color = Color(0xFF4CAF50)
                )
            }
            item {
                StatCard(
                    label = "Zarada/h",
                    value = currencyFormat.format(it.zaradaPoSatu),
                    icon = Icons.Default.AttachMoney,
                    color = GoldPrimary
                )
            }
            item {
                StatCard(
                    label = "Troškovi",
                    value = currencyFormat.format(it.ukupniTroskovi),
                    icon = Icons.Default.ShoppingCart,
                    color = Color(0xFFFF5722)
                )
            }
        }
    }
}

@Composable
fun StatCard(
    label: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color
) {
    Card(
        modifier = Modifier
            .width(160.dp)
            .height(100.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceLight)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(24.dp)
            )
            Column {
                Text(
                    text = label,
                    fontSize = 12.sp,
                    color = TextDisabled
                )
                Text(
                    text = value,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = color
                )
            }
        }
    }
}

@Composable
fun SatiTab(
    sati: List<RadniSat>,
    onAdd: () -> Unit,
    onEdit: (RadniSat) -> Unit,
    onDelete: (RadniSat) -> Unit
) {
    val dateFormat = remember { SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()) }

    Box(modifier = Modifier.fillMaxSize()) {
        if (sati.isEmpty()) {
            EmptyState(
                icon = Icons.Default.Schedule,
                message = "Nema unetih sati",
                buttonText = "Dodaj sate"
            ) { onAdd() }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(sati, key = { it.id }) { sat ->
                    SatCard(
                        sat = sat,
                        dateFormat = dateFormat,
                        onEdit = { onEdit(sat) },
                        onDelete = { onDelete(sat) }
                    )
                }
            }
        }

        FloatingActionButton(
            onClick = onAdd,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
            containerColor = GoldPrimary,
            contentColor = SurfaceDark
        ) {
            Icon(Icons.Default.Add, "Dodaj")
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatePickerField(
    selectedDate: Long,
    onDateSelected: (Long) -> Unit
) {
    val dateFormatter = remember { SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()) }

    var openDialog by remember { mutableStateOf(false) }

    OutlinedTextField(
        value = dateFormatter.format(Date(selectedDate)),
        onValueChange = {},
        modifier = Modifier
            .fillMaxWidth()
            .clickable { openDialog = true },
        label = { Text("Datum") },
        readOnly = true,
        trailingIcon = {
            Icon(Icons.Default.CalendarMonth, contentDescription = null)
        }
    )

    if (openDialog) {
        DatePickerDialog(
            onDismissRequest = { openDialog = false },
            confirmButton = {
                TextButton(onClick = { openDialog = false }) {
                    Text("OK")
                }
            }
        ) {
            val state = rememberDatePickerState(initialSelectedDateMillis = selectedDate)

            DatePicker(
                state = state
            )

            LaunchedEffect(state.selectedDateMillis) {
                state.selectedDateMillis?.let {
                    onDateSelected(it)
                }
            }
        }
    }
}

@Composable
fun SatCard(
    sat: RadniSat,
    dateFormat: SimpleDateFormat,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = dateFormat.format(Date(sat.datum)),
                    fontSize = 12.sp,
                    color = TextDisabled
                )
                Text(
                    text = if (sat.opis.isNotBlank()) sat.opis else "Bez opisa",
                    fontSize = 14.sp,
                    color = TextPrimary,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "${sat.brojSati}h",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = GoldPrimary
                )

                Box {
                    IconButton(onClick = { showMenu = true }) {
                        Icon(
                            Icons.Default.MoreVert,
                            contentDescription = "Opcije",
                            tint = TextSecondary
                        )
                    }

                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Izmeni") },
                            onClick = {
                                showMenu = false
                                onEdit()
                            },
                            leadingIcon = {
                                Icon(Icons.Default.Edit, null)
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Obriši", color = Color.Red) },
                            onClick = {
                                showMenu = false
                                onDelete()
                            },
                            leadingIcon = {
                                Icon(Icons.Default.Delete, null, tint = Color.Red)
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TroskoviTab(
    troskovi: List<Trosak>,
    onAdd: () -> Unit,
    onEdit: (Trosak) -> Unit,
    onDelete: (Trosak) -> Unit,
    onImageClick: (String) -> Unit
) {
    val dateFormat = remember { SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()) }
    val currencyFormat = remember { NumberFormat.getCurrencyInstance(Locale("sr", "RS")) }

    Box(modifier = Modifier.fillMaxSize()) {
        if (troskovi.isEmpty()) {
            EmptyState(
                icon = Icons.Default.Receipt,
                message = "Nema unetih troškova",
                buttonText = "Dodaj trošak"
            ) { onAdd() }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(troskovi, key = { it.id }) { trosak ->
                    TrosakCard(
                        trosak = trosak,
                        dateFormat = dateFormat,
                        currencyFormat = currencyFormat,
                        onEdit = { onEdit(trosak) },
                        onDelete = { onDelete(trosak) },
                        onImageClick = { trosak.putanjaDoSlike?.let(onImageClick) }
                    )
                }
            }
        }

        FloatingActionButton(
            onClick = onAdd,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
            containerColor = GoldPrimary,
            contentColor = SurfaceDark
        ) {
            Icon(Icons.Default.Add, "Dodaj")
        }
    }
}

@Composable
fun TrosakCard(
    trosak: Trosak,
    dateFormat: SimpleDateFormat,
    currencyFormat: NumberFormat,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onImageClick: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = dateFormat.format(Date(trosak.datum)),
                        fontSize = 12.sp,
                        color = TextDisabled
                    )
                    Text(
                        text = trosak.opis,
                        fontSize = 14.sp,
                        color = TextPrimary,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                    if (trosak.kategorija.isNotBlank()) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = BrownLight.copy(alpha = 0.3f),
                            modifier = Modifier.padding(top = 8.dp)
                        ) {
                            Text(
                                text = trosak.kategorija,
                                fontSize = 11.sp,
                                color = BrownLighter,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = currencyFormat.format(trosak.iznos),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFFF5722)
                    )

                    Box {
                        IconButton(onClick = { showMenu = true }) {
                            Icon(
                                Icons.Default.MoreVert,
                                contentDescription = "Opcije",
                                tint = TextSecondary
                            )
                        }

                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Izmeni") },
                                onClick = {
                                    showMenu = false
                                    onEdit()
                                },
                                leadingIcon = { Icon(Icons.Default.Edit, null) }
                            )
                            DropdownMenuItem(
                                text = { Text("Obriši", color = Color.Red) },
                                onClick = {
                                    showMenu = false
                                    onDelete()
                                },
                                leadingIcon = { Icon(Icons.Default.Delete, null, tint = Color.Red) }
                            )
                        }
                    }
                }
            }

            if (trosak.putanjaDoSlike != null) {
                Spacer(modifier = Modifier.height(12.dp))
                AsyncImage(
                    model = File(trosak.putanjaDoSlike),
                    contentDescription = "Račun",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .clickable(onClick = onImageClick)
                )
            }
        }
    }
}

@Composable
fun UplateTab(
    uplate: List<Uplata>,
    onAdd: () -> Unit,
    onEdit: (Uplata) -> Unit,
    onDelete: (Uplata) -> Unit
) {
    val dateFormat = remember { SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()) }
    val currencyFormat = remember { NumberFormat.getCurrencyInstance(Locale("sr", "RS")) }

    Box(modifier = Modifier.fillMaxSize()) {
        if (uplate.isEmpty()) {
            EmptyState(
                icon = Icons.Default.Payments,
                message = "Nema unetih uplata",
                buttonText = "Dodaj uplatu"
            ) { onAdd() }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(uplate, key = { it.id }) { uplata ->
                    UplataCard(
                        uplata = uplata,
                        dateFormat = dateFormat,
                        currencyFormat = currencyFormat,
                        onEdit = { onEdit(uplata) },
                        onDelete = { onDelete(uplata) }
                    )
                }
            }
        }

        FloatingActionButton(
            onClick = onAdd,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
            containerColor = GoldPrimary,
            contentColor = SurfaceDark
        ) {
            Icon(Icons.Default.Add, "Dodaj")
        }
    }
}

@Composable
fun UplataCard(
    uplata: Uplata,
    dateFormat: SimpleDateFormat,
    currencyFormat: NumberFormat,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = dateFormat.format(Date(uplata.datum)),
                    fontSize = 12.sp,
                    color = TextDisabled
                )
                Text(
                    text = uplata.opis,
                    fontSize = 14.sp,
                    color = TextPrimary,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = currencyFormat.format(uplata.iznos),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF4CAF50)
                )

                Box {
                    IconButton(onClick = { showMenu = true }) {
                        Icon(
                            Icons.Default.MoreVert,
                            contentDescription = "Opcije",
                            tint = TextSecondary
                        )
                    }

                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Izmeni") },
                            onClick = {
                                showMenu = false
                                onEdit()
                            },
                            leadingIcon = { Icon(Icons.Default.Edit, null) }
                        )
                        DropdownMenuItem(
                            text = { Text("Obriši", color = Color.Red) },
                            onClick = {
                                showMenu = false
                                onDelete()
                            },
                            leadingIcon = { Icon(Icons.Default.Delete, null, tint = Color.Red) }
                        )
                    }
                }
            }
        }
    }
}