package com.example.projecttracker.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.projecttracker.ui.theme.*
import com.kizitonwose.calendar.compose.HorizontalCalendar
import com.kizitonwose.calendar.compose.rememberCalendarState
import com.kizitonwose.calendar.core.*
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.time.*
import java.time.format.TextStyle
import java.util.*

class StatistikaActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ProjectTrackerTheme {
                StatistikaScreen(
                    onBack = { finish() }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatistikaScreen(onBack: () -> Unit) {
    var selectedMode by remember { mutableStateOf(0) } // 0 = Po projektu, 1 = Po mesecu

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Statistika",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, "Nazad")
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
            // Mode selector
            SegmentedButton(
                selectedMode = selectedMode,
                onModeChange = { selectedMode = it }
            )

            when (selectedMode) {
                0 -> StatistikaPoProjetkuScreen()
                1 -> StatistikaPoMesecuScreen()
            }
        }
    }
}

@Composable
fun SegmentedButton(
    selectedMode: Int,
    onModeChange: (Int) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(Surface),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        listOf("Po Projektu", "Po Mesecu").forEachIndexed { index, label ->
            Button(
                onClick = { onModeChange(index) },
                modifier = Modifier
                    .weight(1f)
                    .padding(4.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (selectedMode == index) GoldPrimary else Color.Transparent,
                    contentColor = if (selectedMode == index) SurfaceDark else TextSecondary
                ),
                shape = RoundedCornerShape(12.dp),
                elevation = ButtonDefaults.buttonElevation(
                    defaultElevation = if (selectedMode == index) 4.dp else 0.dp
                )
            ) {
                Text(
                    text = label,
                    fontWeight = if (selectedMode == index) FontWeight.Bold else FontWeight.Normal,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
        }
    }
}

@Composable
fun StatistikaPoProjetkuScreen() {
    // TODO: Implementacija sa calendar view i odabirom projekta
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Projekat selector
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Surface)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Izaberi projekat",
                    fontSize = 16.sp,
                    color = TextPrimary
                )
                Icon(
                    Icons.Default.ArrowDropDown,
                    contentDescription = null,
                    tint = GoldPrimary
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Calendar view
        CalendarView()

        Spacer(modifier = Modifier.height(16.dp))

        // Stats cards
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            MiniStatCard(
                label = "Ukupno sati",
                value = "125.5h",
                color = BrownLight,
                modifier = Modifier.weight(1f)
            )
            MiniStatCard(
                label = "Zarada",
                value = "250,000",
                color = Color(0xFF4CAF50),
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun StatistikaPoMesecuScreen() {
    val currentMonth = remember { YearMonth.now() }
    val currencyFormat = remember { NumberFormat.getCurrencyInstance(Locale("sr", "RS")) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Month summary card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Surface)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp)
                ) {
                    Text(
                        text = currentMonth.month.getDisplayName(TextStyle.FULL, Locale("sr")),
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = GoldPrimary
                    )
                    Text(
                        text = currentMonth.year.toString(),
                        fontSize = 14.sp,
                        color = TextDisabled
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // Metrics
                    MetricRow(
                        label = "Ukupno sati",
                        value = "186.5h",
                        icon = Icons.Default.Schedule,
                        color = BrownLight
                    )
                    MetricRow(
                        label = "Ukupne uplate",
                        value = currencyFormat.format(450000),
                        icon = Icons.Default.TrendingUp,
                        color = Color(0xFF4CAF50)
                    )
                    MetricRow(
                        label = "Ukupni troškovi",
                        value = currencyFormat.format(120000),
                        icon = Icons.Default.TrendingDown,
                        color = Color(0xFFFF5722)
                    )

                    Divider(
                        modifier = Modifier.padding(vertical = 12.dp),
                        color = BrownLight.copy(alpha = 0.3f)
                    )

                    MetricRow(
                        label = "Neto zarada",
                        value = currencyFormat.format(330000),
                        icon = Icons.Default.Paid,
                        color = GoldPrimary,
                        isTotal = true
                    )
                }
            }
        }

        // Chart section
        item {
            Text(
                text = "Dnevni pregled",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }

        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(250.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Surface)
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "📊 Grafikon\n(Sati, Uplate, Troškovi)",
                        textAlign = TextAlign.Center,
                        color = TextDisabled,
                        fontSize = 16.sp
                    )
                }
            }
        }

        // Projects list for month
        item {
            Text(
                text = "Projekti ovog meseca",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }

        items(3) { index ->
            MonthProjectCard(
                projectName = "Projekat ${index + 1}",
                hours = 45.5 + index * 10,
                earnings = 85000.0 + index * 20000
            )
        }
    }
}

@Composable
fun CalendarView() {
    val currentMonth = remember { YearMonth.now() }
    val startMonth = remember { currentMonth.minusMonths(12) }
    val endMonth = remember { currentMonth.plusMonths(12) }

    val state = rememberCalendarState(
        startMonth = startMonth,
        endMonth = endMonth,
        firstVisibleMonth = currentMonth,
        firstDayOfWeek = DayOfWeek.MONDAY
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Surface)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            HorizontalCalendar(
                state = state,
                dayContent = { day ->
                    Day(day)
                }
            )
        }
    }
}

@Composable
fun Day(day: CalendarDay) {
    val hasWork = remember { (1..28).random() > 20 } // Mock data

    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .padding(4.dp)
            .clip(CircleShape)
            .background(
                if (hasWork) GoldPrimary.copy(alpha = 0.3f)
                else Color.Transparent
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = day.date.dayOfMonth.toString(),
            fontSize = 12.sp,
            color = if (hasWork) GoldPrimary else TextSecondary,
            fontWeight = if (hasWork) FontWeight.Bold else FontWeight.Normal
        )
    }
}

@Composable
fun MetricRow(
    label: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    isTotal: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(if (isTotal) 28.dp else 24.dp)
            )
            Text(
                text = label,
                fontSize = if (isTotal) 18.sp else 16.sp,
                color = TextPrimary,
                fontWeight = if (isTotal) FontWeight.Bold else FontWeight.Normal
            )
        }
        Text(
            text = value,
            fontSize = if (isTotal) 20.sp else 16.sp,
            fontWeight = FontWeight.Bold,
            color = color
        )
    }
}

@Composable
fun MiniStatCard(
    label: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceLight)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = label,
                fontSize = 12.sp,
                color = TextDisabled
            )
            Text(
                text = value,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = color,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

@Composable
fun MonthProjectCard(
    projectName: String,
    hours: Double,
    earnings: Double
) {
    val currencyFormat = remember { NumberFormat.getCurrencyInstance(Locale("sr", "RS")) }

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
                    text = projectName,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Text(
                    text = "${String.format("%.1f", hours)}h",
                    fontSize = 14.sp,
                    color = BrownLight,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
            Text(
                text = currencyFormat.format(earnings),
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = GoldPrimary
            )
        }
    }
}