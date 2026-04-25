package com.example.nutriscan

import android.annotation.SuppressLint
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DietaryScreen(viewModel: OnboardingViewModel, firestoreClient: FirestoreClient,
                  authRepository: AuthRepository,
                  onContinue: () -> Unit) {
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

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                // Title
                Text(
                    text = "What are your dietary restrictions?",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = NutriTeal
                )

                Text(
                    text = "Common Allergens",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.LightGray
                )


                // allergens flow row type shi
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Start
                ) {
                    viewModel.availableAllergens.forEach { allergen ->
                        val isSelected = viewModel.selectedAllergens.contains(allergen)

                        GoalChip(
                            text = allergen,
                            isSelected = isSelected,
                            onClick = {
                                viewModel.toggleAllergen(allergen)
                            }
                        )
                    }
                }

                Text(
                    text = "Diet",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.LightGray
                )

                // diet flow row type shi
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Start
                ) {
                    viewModel.availableDiets.forEach { diet ->
                        val isSelected = viewModel.selectedDiets.contains(diet)

                        GoalChip(
                            text = diet,
                            isSelected = isSelected,
                            onClick = {
                                viewModel.toggleDiet(diet)
                            }
                        )
                    }
                }
            }



            // Continue button
            val scope = rememberCoroutineScope()
            val context = LocalContext.current
            Button(
                onClick = {val userId = authRepository.uid ?: return@Button
                    val dietary = viewModel.buildDietary(userId)
                    scope.launch {
                        firestoreClient.insertDietary(dietary).collect { success ->
                            if (success) {
                                onContinue()
                            } else {
                                Toast.makeText(
                                    context,
                                    "Failed to save dietary data.",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    }
                          },
                modifier = Modifier.fillMaxWidth().height(48.dp),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = NutriTeal)
            ) {
                Text("Continue", color = Color.White, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}


@SuppressLint("ViewModelConstructorInComposable")
@Preview
@Composable
fun Preview() {
    val fakeViewModel = OnboardingViewModel()
    val fakeFirestore = FirestoreClient()
    val fakeAuth = AuthRepository() // might need constructor args

    DietaryScreen(
        viewModel = fakeViewModel,
        firestoreClient = fakeFirestore,
        authRepository = fakeAuth,
        onContinue = {}
    )
}