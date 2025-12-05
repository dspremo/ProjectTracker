package com.spremodesign.projecttracker.ui
// Compose UI
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.room.util.copy
import com.spremodesign.projecttracker.data.Projekat
import com.spremodesign.projecttracker.data.RadniSat
import com.spremodesign.projecttracker.data.Trosak
import com.spremodesign.projecttracker.data.Uplata
import com.spremodesign.projecttracker.ui.theme.BrownLight

// Tema (tvoje custom boje)
import com.spremodesign.projecttracker.ui.theme.GoldPrimary
import com.spremodesign.projecttracker.ui.theme.Surface
import com.spremodesign.projecttracker.ui.theme.SurfaceLight
import com.spremodesign.projecttracker.ui.theme.SurfaceDark
import com.spremodesign.projecttracker.ui.theme.TextPrimary
import com.spremodesign.projecttracker.ui.theme.TextSecondary

// Java utilities
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZoneOffset
import java.util.*
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatePickerField(
    label: String,
    selectedDate: LocalDate?,
    onDateSelected: (LocalDate) -> Unit
) {
    val showDialog = remember { mutableStateOf(false) }

    OutlinedTextField(
        value = selectedDate?.toString() ?: "",
        onValueChange = {},
        label = { Text(label) },
        modifier = Modifier.fillMaxWidth(),
        readOnly = true,
        trailingIcon = {
            IconButton(onClick = { showDialog.value = true }) {
                Icon(Icons.Default.DateRange, contentDescription = null)
            }
        }
    )

    if (showDialog.value) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = selectedDate
                ?.atStartOfDay(ZoneId.systemDefault())
                ?.toInstant()
                ?.toEpochMilli()
        )

        DatePickerDialog(
            onDismissRequest = { showDialog.value = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val picked = Instant.ofEpochMilli(millis)
                            .atZone(ZoneId.systemDefault())
                            .toLocalDate()
                        onDateSelected(picked)
                    }
                    showDialog.value = false
                }) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDialog.value = false }) {
                    Text("Otkaži")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}


@Composable
fun EmptyState(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    message: String,
    buttonText: String,
    onButtonClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            icon,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = BrownLight.copy(alpha = 0.3f)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = message,
            fontSize = 16.sp,
            color = TextSecondary
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = onButtonClick,
            colors = ButtonDefaults.buttonColors(
                containerColor = GoldPrimary,
                contentColor = SurfaceDark
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(buttonText)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DodajSateDialog(
    onDismiss: () -> Unit,
    onSave: (Double, String, Long) -> Unit
) {
    var brojSati by remember { mutableStateOf("") }
    var opis by remember { mutableStateOf("") }
    var datum by remember { mutableStateOf(LocalDate.now()) }
    var showDatePicker by remember { mutableStateOf(false) }

    val dateFormat = remember { SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Dodaj radne sate", color = GoldPrimary, fontWeight = FontWeight.Bold)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = brojSati,
                    onValueChange = { brojSati = it },
                    label = { Text("Broj sati *") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = SurfaceLight,
                        unfocusedContainerColor = SurfaceLight,
                        focusedIndicatorColor = GoldPrimary
                    )
                )

                OutlinedTextField(
                    value = opis,
                    onValueChange = { opis = it },
                    label = { Text("Opis (opciono)") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2,
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = SurfaceLight,
                        unfocusedContainerColor = SurfaceLight,
                        focusedIndicatorColor = GoldPrimary
                    )
                )

                DatePickerField (
                    label = "Datum početka",
                    selectedDate = datum,
                    onDateSelected = { datum = it }
                )

            }
        },
        confirmButton = {
            Button(
                onClick = {
                    brojSati.toDoubleOrNull()?.let {
                        onSave(it, opis, datum.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli())
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = GoldPrimary,
                    contentColor = SurfaceDark
                )
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
        shape = RoundedCornerShape(20.dp)
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DodajTrosakDialog(
    onDismiss: () -> Unit,
    onPickImage: () -> Unit,
    onSave: (Double, String, String, Long) -> Unit
) {
    var iznos by remember { mutableStateOf("") }
    var opis by remember { mutableStateOf("") }
    var kategorija by remember { mutableStateOf("") }
    var datum by remember { mutableStateOf(LocalDate.now()) }

    val dateFormat = remember { SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Dodaj trošak", color = GoldPrimary, fontWeight = FontWeight.Bold)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = iznos,
                    onValueChange = { iznos = it },
                    label = { Text("Iznos (RSD) *") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = SurfaceLight,
                        unfocusedContainerColor = SurfaceLight,
                        focusedIndicatorColor = GoldPrimary
                    )
                )

                OutlinedTextField(
                    value = opis,
                    onValueChange = { opis = it },
                    label = { Text("Opis *") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = SurfaceLight,
                        unfocusedContainerColor = SurfaceLight,
                        focusedIndicatorColor = GoldPrimary
                    )
                )

                OutlinedTextField(
                    value = kategorija,
                    onValueChange = { kategorija = it },
                    label = { Text("Kategorija (npr. Alat, Materijal)") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = SurfaceLight,
                        unfocusedContainerColor = SurfaceLight,
                        focusedIndicatorColor = GoldPrimary
                    )
                )

                DatePickerField (
                    label = "Datum početka",
                    selectedDate = datum,
                    onDateSelected = { datum = it }
                )

                OutlinedButton(
                    onClick = onPickImage,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = GoldPrimary
                    )
                ) {
                    Icon(Icons.Default.Image, null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Dodaj sliku računa")
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (iznos.isNotBlank() && opis.isNotBlank()) {
                        onSave(iznos.toDoubleOrNull() ?: 0.0, opis, kategorija, datum.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli())
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = GoldPrimary,
                    contentColor = SurfaceDark
                )
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
        shape = RoundedCornerShape(20.dp)
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DodajUplatuDialog(
    onDismiss: () -> Unit,
    onSave: (Double, String, Long) -> Unit
) {
    var iznos by remember { mutableStateOf("") }
    var opis by remember { mutableStateOf("") }
    var datum by remember { mutableStateOf(LocalDate.now()) }

    val dateFormat = remember { SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Dodaj uplatu", color = GoldPrimary, fontWeight = FontWeight.Bold)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = iznos,
                    onValueChange = { iznos = it },
                    label = { Text("Iznos (RSD) *") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = SurfaceLight,
                        unfocusedContainerColor = SurfaceLight,
                        focusedIndicatorColor = GoldPrimary
                    )
                )

                OutlinedTextField(
                    value = opis,
                    onValueChange = { opis = it },
                    label = { Text("Opis *") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2,
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = SurfaceLight,
                        unfocusedContainerColor = SurfaceLight,
                        focusedIndicatorColor = GoldPrimary
                    )
                )

                DatePickerField (
                    label = "Datum početka",
                    selectedDate = datum,
                    onDateSelected = { datum = it }
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (iznos.isNotBlank() && opis.isNotBlank()) {
                        onSave(iznos.toDoubleOrNull() ?: 0.0, opis, datum.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli())
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = GoldPrimary,
                    contentColor = SurfaceDark
                )
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
        shape = RoundedCornerShape(20.dp)
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProjekatDialog(
    projekat: Projekat,
    onDismiss: () -> Unit,
    onSave: (Projekat) -> Unit
) {
    var naziv by remember { mutableStateOf(projekat.naziv) }
    var suma by remember { mutableStateOf(projekat.dogovorenaSuma.toString()) }
    var aktivan by remember { mutableStateOf(projekat.aktivan) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                "Izmeni projekat",
                fontSize = 22.sp,
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
                    label = { Text("Naziv projekta") },
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
                    label = { Text("Dogovorena suma (RSD)") },
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = SurfaceLight,
                        unfocusedContainerColor = SurfaceLight,
                        focusedIndicatorColor = GoldPrimary,
                        focusedLabelColor = GoldPrimary
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Status",
                        color = TextPrimary,
                        fontSize = 14.sp
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = if (aktivan) "Aktivan" else "Završen",
                            color = if (aktivan) Color(0xFF4CAF50) else Color(0xFF9E9E9E),
                            modifier = Modifier.padding(end = 8.dp)
                        )
                        Switch(
                            checked = aktivan,
                            onCheckedChange = { aktivan = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = GoldPrimary,
                                checkedTrackColor = GoldPrimary.copy(alpha = 0.4f)
                            )
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val novaSuma = suma.toDoubleOrNull() ?: 0.0
                    onSave(
                        projekat.copy(
                            naziv = naziv,
                            dogovorenaSuma = novaSuma,
                            aktivan = aktivan
                        )
                    )
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


// EDIT SAT DIALOG
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditSateDialog(
    sat: RadniSat,
    onDismiss: () -> Unit,
    onSave: (Double, String, Long) -> Unit
) {
    var brojSati by remember { mutableStateOf(sat.brojSati.toString()) }
    var opis by remember { mutableStateOf(sat.opis) }
    var datum by remember { mutableStateOf(longToLocalDate(sat.datum)) }

    val dateFormat = remember { SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Izmeni radne sate", color = GoldPrimary, fontWeight = FontWeight.Bold)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = brojSati,
                    onValueChange = { brojSati = it },
                    label = { Text("Broj sati *") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = SurfaceLight,
                        unfocusedContainerColor = SurfaceLight,
                        focusedIndicatorColor = GoldPrimary
                    )
                )

                OutlinedTextField(
                    value = opis,
                    onValueChange = { opis = it },
                    label = { Text("Opis") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2,
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = SurfaceLight,
                        unfocusedContainerColor = SurfaceLight,
                        focusedIndicatorColor = GoldPrimary
                    )
                )

                DatePickerField (
                    label = "Datum početka",
                    selectedDate = datum,
                    onDateSelected = {
                        datum = it
                        sat.datum
                    }
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    brojSati.toDoubleOrNull()?.let {
                        onSave(it, opis, datum.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli())
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = GoldPrimary,
                    contentColor = SurfaceDark
                )
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
        shape = RoundedCornerShape(20.dp)
    )
}

// EDIT TROSAK DIALOG
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditTrosakDialog(
    trosak: Trosak,
    onDismiss: () -> Unit,
    onPickImage: () -> Unit,
    onSave: (Double, String, String, Long) -> Unit
) {
    var iznos by remember { mutableStateOf(trosak.iznos.toString()) }
    var opis by remember { mutableStateOf(trosak.opis) }
    var kategorija by remember { mutableStateOf(trosak.kategorija) }
    var datum by remember { mutableStateOf(longToLocalDate(trosak.datum)) }

    val dateFormat = remember { SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Izmeni trošak", color = GoldPrimary, fontWeight = FontWeight.Bold)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = iznos,
                    onValueChange = { iznos = it },
                    label = { Text("Iznos (RSD) *") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = SurfaceLight,
                        unfocusedContainerColor = SurfaceLight,
                        focusedIndicatorColor = GoldPrimary
                    )
                )

                OutlinedTextField(
                    value = opis,
                    onValueChange = { opis = it },
                    label = { Text("Opis *") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = SurfaceLight,
                        unfocusedContainerColor = SurfaceLight,
                        focusedIndicatorColor = GoldPrimary
                    )
                )

                OutlinedTextField(
                    value = kategorija,
                    onValueChange = { kategorija = it },
                    label = { Text("Kategorija") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = SurfaceLight,
                        unfocusedContainerColor = SurfaceLight,
                        focusedIndicatorColor = GoldPrimary
                    )
                )

                DatePickerField (
                    label = "Datum početka",
                    selectedDate = datum,
                    onDateSelected = { datum = it }
                )

                OutlinedButton(
                    onClick = onPickImage,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = GoldPrimary
                    )
                ) {
                    Icon(Icons.Default.Image, null)
                    Spacer(Modifier.width(8.dp))
                    Text("Promeni sliku")
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (iznos.isNotBlank() && opis.isNotBlank()) {
                        onSave(iznos.toDoubleOrNull() ?: 0.0, opis, kategorija, datum.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli())
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = GoldPrimary,
                    contentColor = SurfaceDark
                )
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
        shape = RoundedCornerShape(20.dp)
    )
}

// EDIT UPLATA DIALOG
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditUplatuDialog(
    uplata: Uplata,
    onDismiss: () -> Unit,
    onSave: (Double, String, Long) -> Unit
) {
    var iznos by remember { mutableStateOf(uplata.iznos.toString()) }
    var opis by remember { mutableStateOf(uplata.opis) }
    var datum by remember { mutableStateOf(LocalDate.now()) }
    val dateFormat = remember { SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Izmeni uplatu", color = GoldPrimary, fontWeight = FontWeight.Bold)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = iznos,
                    onValueChange = { iznos = it },
                    label = { Text("Iznos (RSD) *") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = SurfaceLight,
                        unfocusedContainerColor = SurfaceLight,
                        focusedIndicatorColor = GoldPrimary
                    )
                )

                OutlinedTextField(
                    value = opis,
                    onValueChange = { opis = it },
                    label = { Text("Opis *") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2,
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = SurfaceLight,
                        unfocusedContainerColor = SurfaceLight,
                        focusedIndicatorColor = GoldPrimary
                    )
                )

                DatePickerField (
                    label = "Datum početka",
                    selectedDate = datum,
                    onDateSelected = { datum = it }
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (iznos.isNotBlank() && opis.isNotBlank()) {
                        onSave(iznos.toDoubleOrNull() ?: 0.0, opis, datum.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli())
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = GoldPrimary,
                    contentColor = SurfaceDark
                )
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
        shape = RoundedCornerShape(20.dp)
    )
}

fun longToLocalDate(timestamp: Long): LocalDate {
    return Instant.ofEpochMilli(timestamp)
        .atZone(ZoneOffset.systemDefault())
        .toLocalDate()
}

