package com.example.nutriscan

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import java.util.Calendar
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val StatCardBg = Color(0xFFB2DFDB)

private fun calcChange(today: Int, yesterday: Int): String {
    if (yesterday == 0) return if (today > 0) "+100%" else "0%"
    val pct = ((today - yesterday) * 100) / yesterday
    return if (pct >= 0) "+$pct%" else "$pct%"
}
private val CardTeal = Color(0xFF7EC8C8)
private val RingBackground = Color(0xFFD0D0D0)

@Composable
fun DashboardScreen(
    onLogout: () -> Unit,
    onNavigateToScan: () -> Unit,
    firestoreClient: FirestoreClient,
    userId: String,
) {
    var pantryItems by remember { mutableStateOf<List<NutritionLog>>(emptyList()) }
    var todayLogs by remember { mutableStateOf<List<NutritionLog>>(emptyList()) }
    var yesterdayLogs by remember { mutableStateOf<List<NutritionLog>>(emptyList()) }

    LaunchedEffect(userId) {
        // fetch pantry (weekly scan history)
        firestoreClient.getWeeklyLogs(userId).collect { logs ->
            pantryItems = logs
        }
    }

    LaunchedEffect(userId) {
        val cal = Calendar.getInstance()
        // start and end of today
        cal.set(Calendar.HOUR_OF_DAY, 0); cal.set(Calendar.MINUTE, 0); cal.set(Calendar.SECOND, 0)
        val startToday = cal.timeInMillis
        cal.set(Calendar.HOUR_OF_DAY, 23); cal.set(Calendar.MINUTE, 59); cal.set(Calendar.SECOND, 59)
        val endToday = cal.timeInMillis
        // start and end of yesterday
        val startYesterday = startToday - 86400000L
        val endYesterday = startToday - 1L

        firestoreClient.getDailyLogs(userId, startToday, endToday).collect { todayLogs = it }
        firestoreClient.getDailyLogs(userId, startYesterday, endYesterday).collect { yesterdayLogs = it }
    }

    Scaffold(
        containerColor = NutriBackground,
        floatingActionButton = {
            Box(
                modifier = Modifier.fillMaxSize()
            ) {
                // Logout button - pinned bottom right
                Button(
                    onClick = { onLogout() },
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(bottom = 16.dp, end = 16.dp)
                ) {
                    Text("Log Out", color = Color.White)
                }

                // Camera FAB - centered at bottom
                FloatingActionButton(
                    onClick = onNavigateToScan,
                    shape = CircleShape,
                    containerColor = CardTeal,
                    modifier = Modifier.align(Alignment.BottomCenter)
                ) {
                    Image(
                        painter = painterResource(R.drawable.camera),
                        contentDescription = "Camera Icon",
                        modifier = Modifier.size(40.dp)
                    )
                }
            }
        },
        floatingActionButtonPosition = androidx.compose.material3.FabPosition.Center
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(top = 48.dp, start = 16.dp, end = 16.dp, bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item { DietAtAGlanceCard() }
            item {
                val todaySugar = todayLogs.sumOf { it.sugarG }
                val yesterdaySugar = yesterdayLogs.sumOf { it.sugarG }
                val todayFat = todayLogs.sumOf { it.fatG }
                val yesterdayFat = yesterdayLogs.sumOf { it.fatG }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    StatCard(label = "Sugar Intake", value = "${todaySugar}g", change = calcChange(todaySugar, yesterdaySugar), up = todaySugar >= yesterdaySugar, modifier = Modifier.weight(1f))
                    StatCard(label = "Fat Intake", value = "${todayFat}g", change = calcChange(todayFat, yesterdayFat), up = todayFat >= yesterdayFat, modifier = Modifier.weight(1f))
                }
            }
            item {
                val todayWater = todayLogs.sumOf { it.waterMl }
                val yesterdayWater = yesterdayLogs.sumOf { it.waterMl }
                val todayProtein = todayLogs.sumOf { it.proteinG }
                val yesterdayProtein = yesterdayLogs.sumOf { it.proteinG }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    StatCard(label = "Water Intake", value = "${todayWater}ml", change = calcChange(todayWater, yesterdayWater), up = todayWater >= yesterdayWater, modifier = Modifier.weight(1f))
                    StatCard(label = "Protein Intake", value = "${todayProtein}g", change = calcChange(todayProtein, yesterdayProtein), up = todayProtein >= yesterdayProtein, modifier = Modifier.weight(1f))
                }
            }
            item { PantryHeader() }
            if (pantryItems.isEmpty()) {
                item {
                    Text(
                        text = "No items yet. Scan a label to get started!",
                        fontSize = 13.sp,
                        color = Color.Gray,
                        modifier = Modifier.padding(start = 4.dp)
                    )
                }
            } else {
                items(pantryItems) { log ->
                    PantryItem(log = log)
                }
            }
        }
    }
}

@Composable
private fun PantryHeader() {
    Text(
        text = "Your Pantry",
        fontSize = 16.sp,
        fontWeight = FontWeight.ExtraBold,
        color = Color(0xFF00695C)
    )
}

@Composable
private fun PantryItem(log: NutritionLog) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF4DB6AC))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = log.productName.ifBlank { "Unknown Product" },
                fontSize = 14.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(text = "${log.calories} cal", fontSize = 12.sp, color = Color.White)
                Text(text = "${log.proteinG}g pro", fontSize = 12.sp, color = Color.White)
                Text(text = "${log.fatG}g fat", fontSize = 12.sp, color = Color.White)
                Text(text = "${log.sugarG}g sug", fontSize = 12.sp, color = Color.White)
            }
        }
    }
}

@Composable
private fun DietAtAGlanceCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardTeal)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Your diet at a glance...",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
            DonutChart(progress = 0.65f)
        }
    }
}

@Composable
private fun StatCard(label: String, value: String, change: String, up: Boolean, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = StatCardBg)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(text = label, fontSize = 13.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
            Text(text = "Today", fontSize = 11.sp, color = Color.White.copy(alpha = 0.7f))
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = value, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White)
            Text(
                text = if (up) "↑ $change" else "↓ $change",
                fontSize = 14.sp,
                fontWeight = FontWeight.ExtraBold,
                color = if (up) Color(0xFFE53935) else Color(0xFF43A047)
            )
        }
    }
}

@Composable
private fun DonutChart(progress: Float) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.size(80.dp)
    ) {
        Canvas(modifier = Modifier.size(80.dp)) {
            val stroke = Stroke(width = 14.dp.toPx(), cap = StrokeCap.Round)
            drawArc(
                color = RingBackground,
                startAngle = 0f,
                sweepAngle = 360f,
                useCenter = false,
                style = stroke
            )
            drawArc(
                color = Color(0xFF00897B),
                startAngle = -90f,
                sweepAngle = 360f * progress,
                useCenter = false,
                style = stroke
            )
        }
        Text(text = "😐", fontSize = 24.sp)
    }
}
