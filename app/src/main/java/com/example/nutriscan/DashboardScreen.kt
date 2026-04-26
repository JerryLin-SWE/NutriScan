package com.example.nutriscan

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val StatCardBg = Color(0xFFB2DFDB)

private val CardTeal = Color(0xFF7EC8C8)
private val RingBackground = Color(0xFFD0D0D0)

@Composable
fun DashboardScreen(
    onLogout: () -> Unit,
    onNavigateToScan: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(NutriBackground)
            .padding(16.dp)
    ) {
        DietAtAGlanceCard()
        Spacer(modifier = Modifier.height(16.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            StatCard(label = "Sugar Intake", value = "256g", change = "+26%", up = true, modifier = Modifier.weight(1f))
            StatCard(label = "Fat Intake", value = "174g", change = "-13%", up = false, modifier = Modifier.weight(1f))
        }
        Spacer(modifier = Modifier.height(12.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            StatCard(label = "Water Intake", value = "376ml", change = "-32%", up = false, modifier = Modifier.weight(1f))
            StatCard(label = "Protein Intake", value = "120g", change = "+56%", up = true, modifier = Modifier.weight(1f))
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
            Text(text = "This week", fontSize = 11.sp, color = Color.White.copy(alpha = 0.7f))
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
