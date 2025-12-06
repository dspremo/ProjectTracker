package com.example.projecttracker.ui

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.projecttracker.data.AppDatabase
import com.example.projecttracker.data.Projekat
import com.example.projecttracker.ui.theme.*
import com.example.projecttracker.ui.EditProjekatDialog
import com.example.projecttracker.ui.StatistikaActivity
import com.example.projecttracker.ui.ProjekatDetaljiActivity
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ProjectTrackerTheme {
                MainScreen(
                    onProjekatClick = { projekat ->
                        val intent = Intent(this, ProjekatDetaljiActivity::class.java)
                        intent.putExtra("projekat_id", projekat.id)
                        startActivity(intent)
                    },
                    onStatistikaClick = {
                        startActivity(Intent(this, StatistikaActivity::class.java))
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    onProjekatClick: (Projekat) -> Unit,
    onStatistikaClick: () -> Unit
) {
    val viewModel: MainViewModel = viewModel()
    val authViewModel: AuthViewModel = viewModel()
    val projekti by viewModel.projekti.collectAsState(initial = emptyList())
    val authState by authViewModel.authState.collectAsState()
    
    var showDialog by remember { mutableStateOf(false) }
    var showProfileDialog by remember { mutableStateOf(false) }

    // Google Sign-In launcher
    val signInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            authViewModel.handleSignInResult(result.data)
        }
    }

    Scaffold(
        topBar = {
            // Futuristički TopBar sa glassmorphism efektom
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
                    Column {
                        Text(
                            text = "Spremo Design",
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = "Pracenje Projekata",
                            fontSize = 14.sp,
                            color = GoldPrimary
                        )
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Sign-In / Profile dugme
                        if (authState.isSignedIn) {
                            // Prikaži avatar korisnika
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(GoldPrimary)
                                    .clickable { showProfileDialog = true },
                                contentAlignment = Alignment.Center
                            ) {
                                if (authState.userPhotoUrl != null) {
                                    AsyncImage(
                                        model = authState.userPhotoUrl,
                                        contentDescription = "Profile",
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                } else {
                                    Text(
                                        text = authState.userName?.firstOrNull()?.uppercase() ?: "U",
                                        color = SurfaceDark,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 18.sp
                                    )
                                }
                            }
                        } else {
                            // Sign-In dugme
                            IconButton(
                                onClick = { signInLauncher.launch(authViewModel.getSignInIntent()) },
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(
                                        Brush.linearGradient(
                                            colors = listOf(Color(0xFF4285F4), Color(0xFF34A853))
                                        )
                                    )
                            ) {
                                Icon(
                                    Icons.Default.Person,
                                    contentDescription = "Prijava",
                                    tint = Color.White
                                )
                            }
                        }
                    }
                }
            }
        },
        bottomBar = {
            NavigationBar(
                containerColor = Surface,
                contentColor = TextPrimary,
                tonalElevation = 8.dp
            ) {
                NavigationBarItem(
                    selected = true,
                    onClick = { /* Already on home */ },
                    icon = {
                        Icon(
                            Icons.Default.Home,
                            contentDescription = "Projekti"
                        )
                    },
                    label = { Text("Projekti") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = GoldPrimary,
                        selectedTextColor = GoldPrimary,
                        indicatorColor = GoldPrimary.copy(alpha = 0.2f),
                        unselectedIconColor = TextSecondary,
                        unselectedTextColor = TextSecondary
                    )
                )
                NavigationBarItem(
                    selected = false,
                    onClick = onStatistikaClick,
                    icon = {
                        Icon(
                            Icons.Default.BarChart,
                            contentDescription = "Statistika"
                        )
                    },
                    label = { Text("Statistika") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = GoldPrimary,
                        selectedTextColor = GoldPrimary,
                        indicatorColor = GoldPrimary.copy(alpha = 0.2f),
                        unselectedIconColor = TextSecondary,
                        unselectedTextColor = TextSecondary
                    )
                )
            }
        },
        floatingActionButton = {
            // Futuristički FAB
            FloatingActionButton(
                onClick = { showDialog = true },
                containerColor = GoldPrimary,
                contentColor = SurfaceDark,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.size(64.dp)
            ) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = "Novi projekat",
                    modifier = Modifier.size(32.dp)
                )
            }
        },
        containerColor = SurfaceDark
    ) { padding ->
        if (projekti.isEmpty()) {
            // Empty state sa futurističkim dizajnom
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(32.dp)
                ) {
                    Icon(
                        Icons.Default.Handyman,
                        contentDescription = null,
                        modifier = Modifier.size(120.dp),
                        tint = BrownLight.copy(alpha = 0.3f)
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Text(
                        text = "Nemaš projekata",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextSecondary
                    )
                    Text(
                        text = "Dodaj prvi projekat da počneš",
                        fontSize = 16.sp,
                        color = TextDisabled,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(projekti, key = { it.id }) { projekat ->
                    FuturisticProjekatCard(
                        projekat = projekat,
                        onClick = { onProjekatClick(projekat) },
                        onEdit = { updated ->
                            viewModel.azurirajProjekat(updated)                        }
                    )
                }
            }
        }
    }

    if (showDialog) {
        NoviProjekatDialog(
            onDismiss = { showDialog = false },
            onSave = { projekat ->
                viewModel.dodajProjekat(projekat)
                showDialog = false
            }
        )
    }

    // Profile Dialog
    if (showProfileDialog) {
        ProfileDialog(
            authState = authState,
            onDismiss = { showProfileDialog = false },
            onSignOut = {
                authViewModel.signOut()
                showProfileDialog = false
            }
        )
    }
}
@Composable
fun FuturisticProjekatCard(
    projekat: Projekat,
    onClick: () -> Unit,
    onEdit: (Projekat) -> Unit
) {
    var showEditDialog by remember { mutableStateOf(false) }

    if (showEditDialog) {
        EditProjekatDialog(
            projekat = projekat,
            onDismiss = { showEditDialog = false },
            onSave = { updated ->
                onEdit(updated)
                showEditDialog = false
            }
        )
    }

    val dateFormat = remember { SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()) }
    val currencyFormat = remember { NumberFormat.getCurrencyInstance(Locale("sr", "RS")) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = Surface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 4.dp,
            pressedElevation = 8.dp
        )
    ) {
        Box {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(GoldDark, GoldPrimary, GoldLight)
                        )
                    )
            )

            Column(
                modifier = Modifier.padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = projekat.naziv,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = projekat.klijent,
                            fontSize = 14.sp,
                            color = GoldPrimary,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (!projekat.aktivan)
                                Color(0xFF4CAF50).copy(alpha = 0.2f)
                            else
                                Color(0xFF9E9E9E).copy(alpha = 0.2f)
                        ) {
                            Text(
                                text = if (projekat.aktivan) "Aktivan" else "Završen",
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                fontSize = 12.sp,
                                color = if (!projekat.aktivan) Color(0xFF4CAF50) else Color(0xFF9E9E9E),
                                fontWeight = FontWeight.Medium
                            )
                        }

                        IconButton(onClick = { showEditDialog = true }) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Izmeni projekat",
                                tint = GoldPrimary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    InfoChip(
                        label = "Dogovoreno",
                        value = currencyFormat.format(projekat.dogovorenaSuma),
                        icon = Icons.Default.AttachMoney,
                        modifier = Modifier.weight(1f)
                    )
                    InfoChip(
                        label = "Početak",
                        value = dateFormat.format(Date(projekat.datumPocetka)),
                        icon = Icons.Default.CalendarToday,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}


@Composable
fun InfoChip(
    label: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = SurfaceLight.copy(alpha = 0.6f)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = GoldLight,
                modifier = Modifier.size(16.dp)
            )
            Column {
                Text(
                    text = label,
                    fontSize = 10.sp,
                    color = TextDisabled
                )
                Text(
                    text = value,
                    fontSize = 12.sp,
                    color = TextPrimary,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
fun NoviProjekatDialog(
    onDismiss: () -> Unit,
    onSave: (Projekat) -> Unit
) {
    var naziv by remember { mutableStateOf("") }
    var klijent by remember { mutableStateOf("") }
    var suma by remember { mutableStateOf("") }
    var opis by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                "Novi Projekat",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = GoldPrimary
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedTextField(
                    value = naziv,
                    onValueChange = { naziv = it },
                    label = { Text("Naziv projekta *") },
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = SurfaceLight,
                        unfocusedContainerColor = SurfaceLight,
                        focusedIndicatorColor = GoldPrimary,
                        focusedLabelColor = GoldPrimary
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = klijent,
                    onValueChange = { klijent = it },
                    label = { Text("Klijent *") },
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = SurfaceLight,
                        unfocusedContainerColor = SurfaceLight,
                        focusedIndicatorColor = GoldPrimary,
                        focusedLabelColor = GoldPrimary
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = suma,
                    onValueChange = { suma = it },
                    label = { Text("Dogovorena suma (RSD) *") },
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = SurfaceLight,
                        unfocusedContainerColor = SurfaceLight,
                        focusedIndicatorColor = GoldPrimary,
                        focusedLabelColor = GoldPrimary
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = opis,
                    onValueChange = { opis = it },
                    label = { Text("Opis (opciono)") },
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = SurfaceLight,
                        unfocusedContainerColor = SurfaceLight,
                        focusedIndicatorColor = GoldPrimary,
                        focusedLabelColor = GoldPrimary
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (naziv.isNotBlank() && klijent.isNotBlank() && suma.isNotBlank()) {
                        onSave(
                            Projekat(
                                naziv = naziv,
                                klijent = klijent,
                                datumPocetka = System.currentTimeMillis(),
                                dogovorenaSuma = suma.toDoubleOrNull() ?: 0.0,
                                opis = opis
                            )
                        )
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = GoldPrimary,
                    contentColor = SurfaceDark
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Sačuvaj", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Otkaži", color = TextSecondary)
            }
        },
        containerColor = Surface,
        shape = RoundedCornerShape(24.dp)
    )
}

@Composable
fun ProfileDialog(
    authState: AuthState,
    onDismiss: () -> Unit,
    onSignOut: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Surface)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Avatar
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(GoldPrimary),
                    contentAlignment = Alignment.Center
                ) {
                    if (authState.userPhotoUrl != null) {
                        AsyncImage(
                            model = authState.userPhotoUrl,
                            contentDescription = "Profile",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Text(
                            text = authState.userName?.firstOrNull()?.uppercase() ?: "U",
                            color = SurfaceDark,
                            fontWeight = FontWeight.Bold,
                            fontSize = 32.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Ime korisnika
                Text(
                    text = authState.userName ?: "Korisnik",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )

                // Email
                Text(
                    text = authState.userEmail ?: "",
                    fontSize = 14.sp,
                    color = TextSecondary
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Sign Out dugme
                Button(
                    onClick = onSignOut,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFE53935),
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        Icons.Default.Logout,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Odjavi se", fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Zatvori dugme
                TextButton(onClick = onDismiss) {
                    Text("Zatvori", color = TextSecondary)
                }
            }
        }
    }
}