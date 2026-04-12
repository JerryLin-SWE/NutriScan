package com.example.nutriscan

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OnboardingScreen(onContinue: (age: Int, metric: String, weight: Int, activityLevel: Int) -> Unit) {

    // State
    var selectedAge by remember { mutableStateOf(24) }
    var selectedMetric by remember { mutableStateOf("Imperial (in, lbs, miles)") }
    var selectedWeight by remember { mutableStateOf(150) }
    var selectedActivity by remember { mutableStateOf(1) }

    var ageExpanded by remember { mutableStateOf(false) }
    var metricExpanded by remember { mutableStateOf(false) }
    var weightExpanded by remember { mutableStateOf(false) }

    val metricOptions = listOf("Imperial (in, lbs, miles)", "Metric (cm, kg, km)")
    val ages = (13..100).toList()
    val weights = (50..400).toList()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(NutriBackground)
            .padding(24.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {

            // Progress indicator
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                Text("Getting to know you...", fontSize = 12.sp, color = Color.Gray)
                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Box(modifier = Modifier.size(width = 28.dp, height = 6.dp).background(NutriTeal, RoundedCornerShape(3.dp)))
                    Box(modifier = Modifier.size(width = 28.dp, height = 6.dp).background(Color.LightGray, RoundedCornerShape(3.dp)))
                    Box(modifier = Modifier.size(width = 28.dp, height = 6.dp).background(Color.LightGray, RoundedCornerShape(3.dp)))
                    Box(modifier = Modifier.size(width = 28.dp, height = 6.dp).background(Color.LightGray, RoundedCornerShape(3.dp)))
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Title
            Text(
                text = "Who are you?",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = NutriTeal
            )

            // Age dropdown
            Column {
                Text("My Age", fontSize = 14.sp, color = Color.Black)
                Spacer(modifier = Modifier.height(4.dp))
                ExposedDropdownMenuBox(expanded = ageExpanded, onExpandedChange = { ageExpanded = it }) {
                    OutlinedTextField(
                        value = "$selectedAge",
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = ageExpanded) },
                        modifier = Modifier.fillMaxWidth().menuAnchor(),
                        shape = RoundedCornerShape(8.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NutriTeal,
                            unfocusedBorderColor = Color.LightGray
                        )
                    )
                    ExposedDropdownMenu(expanded = ageExpanded, onDismissRequest = { ageExpanded = false }) {
                        ages.forEach { age ->
                            DropdownMenuItem(text = { Text("$age") }, onClick = {
                                selectedAge = age
                                ageExpanded = false
                            })
                        }
                    }
                }
            }

            // Metric System dropdown
            Column {
                Text("My Metric System", fontSize = 14.sp, color = Color.Black)
                Spacer(modifier = Modifier.height(4.dp))
                ExposedDropdownMenuBox(expanded = metricExpanded, onExpandedChange = { metricExpanded = it }) {
                    OutlinedTextField(
                        value = selectedMetric,
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = metricExpanded) },
                        modifier = Modifier.fillMaxWidth().menuAnchor(),
                        shape = RoundedCornerShape(8.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NutriTeal,
                            unfocusedBorderColor = Color.LightGray
                        )
                    )
                    ExposedDropdownMenu(expanded = metricExpanded, onDismissRequest = { metricExpanded = false }) {
                        metricOptions.forEach { option ->
                            DropdownMenuItem(text = { Text(option) }, onClick = {
                                selectedMetric = option
                                metricExpanded = false
                            })
                        }
                    }
                }
            }

            // Weight dropdown
            Column {
                Text("My Weight", fontSize = 14.sp, color = Color.Black)
                Spacer(modifier = Modifier.height(4.dp))
                ExposedDropdownMenuBox(expanded = weightExpanded, onExpandedChange = { weightExpanded = it }) {
                    OutlinedTextField(
                        value = "$selectedWeight lbs",
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = weightExpanded) },
                        modifier = Modifier.fillMaxWidth().menuAnchor(),
                        shape = RoundedCornerShape(8.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NutriTeal,
                            unfocusedBorderColor = Color.LightGray
                        )
                    )
                    ExposedDropdownMenu(expanded = weightExpanded, onDismissRequest = { weightExpanded = false }) {
                        weights.forEach { w ->
                            DropdownMenuItem(text = { Text("$w lbs") }, onClick = {
                                selectedWeight = w
                                weightExpanded = false
                            })
                        }
                    }
                }
            }

            // Activity Level
            Column {
                Text(
                    "Activity Level (1-5, 1 being Sedentary, 5 being Professionally Active)",
                    fontSize = 14.sp,
                    color = Color.Black
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, NutriTeal, RoundedCornerShape(8.dp))
                ) {
                    (1..5).forEach { level ->
                        val isSelected = selectedActivity == level
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .background(
                                    if (isSelected) NutriTeal else Color.Transparent,
                                    if (level == 1) RoundedCornerShape(topStart = 8.dp, bottomStart = 8.dp)
                                    else if (level == 5) RoundedCornerShape(topEnd = 8.dp, bottomEnd = 8.dp)
                                    else RoundedCornerShape(0.dp)
                                )
                                .clickable { selectedActivity = level }
                                .padding(vertical = 12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "$level",
                                color = if (isSelected) Color.White else Color.Gray,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Continue button
            Button(
                onClick = { onContinue(selectedAge, selectedMetric, selectedWeight, selectedActivity) },
                modifier = Modifier.fillMaxWidth().height(48.dp),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = NutriTeal)
            ) {
                Text("Continue", color = Color.White, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}
