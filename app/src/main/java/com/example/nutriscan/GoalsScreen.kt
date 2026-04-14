package com.example.nutriscan

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel


@Composable
fun GoalsScreen(viewModel: OnboardingViewModel = viewModel(), onContinue: () -> Unit) {

    val selectedGoals = remember { mutableStateListOf<String>() }
    Box(
    modifier = Modifier
    .fillMaxSize()
    .background(NutriBackground)
    .padding(24.dp)
    )
    {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {

            // Progress indicator
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Getting to know you...", fontSize = 12.sp, color = Color.Gray)
                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Box(
                        modifier = Modifier.size(width = 28.dp, height = 6.dp)
                            .background(NutriTeal, RoundedCornerShape(3.dp))
                    )
                    Box(
                        modifier = Modifier.size(width = 28.dp, height = 6.dp)
                            .background(NutriTeal, RoundedCornerShape(3.dp))
                    )
                    Box(
                        modifier = Modifier.size(width = 28.dp, height = 6.dp)
                            .background(Color.LightGray, RoundedCornerShape(3.dp))
                    )
                    Box(
                        modifier = Modifier.size(width = 28.dp, height = 6.dp)
                            .background(Color.LightGray, RoundedCornerShape(3.dp))
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Title
            Text(
                text = "What are you goals?",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = NutriTeal
            )

            Text(
                text = "Select all that apply",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.Black
            )



            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Start
            ) {
                viewModel.availableGoals.forEach { goal ->
                    val isSelected = viewModel.selectedGoals.contains(goal)

                    GoalChip(
                        text = goal,
                        isSelected = isSelected,
                        onClick = {
                            viewModel.toggleGoal(goal)
                        }
                    )
                }
        }

            // Continue button
            Button(
                onClick = { onContinue() },
                modifier = Modifier.fillMaxWidth().height(48.dp),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = NutriTeal)
            ) {
                Text("Continue", color = Color.White, fontWeight = FontWeight.SemiBold)
            }
    }
}
}
