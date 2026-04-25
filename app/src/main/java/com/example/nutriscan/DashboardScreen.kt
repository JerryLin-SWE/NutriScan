package com.example.nutriscan

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

@Composable
fun DashboardScreen(
    onLogout: () -> Unit,
    onNavigateToScan: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(NutriBackground),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "Dashboard",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = NutriTeal
            )
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = onNavigateToScan,
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = NutriTeal)
            ) {
                Text("Scan Label", color = Color.White)
            }
            Spacer(modifier = Modifier.height(12.dp))
            Button(
                onClick = {onLogout()},
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
            ) {
                Text("Log Out", color = Color.White)
            }
        }
    }
}
