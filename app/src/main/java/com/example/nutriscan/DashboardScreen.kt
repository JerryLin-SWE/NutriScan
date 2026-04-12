package com.example.nutriscan

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

@Composable
fun DashboardScreen() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(NutriBackground),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Dashboard",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = NutriTeal
        )
    }
}
