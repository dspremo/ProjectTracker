package com.example.projecttracker.ui

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.projecttracker.data.AppDatabase
import com.example.projecttracker.data.Projekat
import com.example.projecttracker.ui.theme.*
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
    val projekti by viewModel.projekti.collectAsState(initial = emptyList())
    var showDialog by remember { mutableStateOf(false) }

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
                            text = "Projekti",
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = "Drvo & Zanat",
                            fontSize = 14.sp,
                            color = GoldPrimary
                        )
                    }

                    IconButton(
                        onClick = onStatistikaClick,
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(GoldDark, GoldPrimary)
                                )
                            )
                    ) {
                        Icon(
                            Icons.Default.Analytics,
                            contentDescription = "Statistika",
                            tint = SurfaceDark
                        )
                    }
                }
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
                        onClick = { onProjekatClick(projekat) }
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
}

@Composable
fun FuturisticProjekatCard(
    projekat: Projekat,
    onClick: () -> Unit
) {
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
            // Zlatni akcent na vrhu
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

                    // Status badge
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (projekat.aktivan)
                            Color(0xFF4CAF50).copy(alpha = 0.2f)
                        else
                            Color(0xFF9E9E9E).copy(alpha = 0.2f)
                    ) {
                        Text(
                            text = if (projekat.aktivan) "Aktivan" else "Završen",
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            fontSize = 12.sp,
                            color = if (projekat.aktivan) Color(0xFF4CAF50) else Color(0xFF9E9E9E),
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Info cards sa glassmorphism
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