package com.example.nutriscan

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel

class OnboardingViewModel: ViewModel() {
    val availableGoals = listOf(
        "Losing Weight",
        "Gaining Muscle",
        "Gaining Weight",
        "Track Health",
        "Monitor Macros",
        "Track Pantry",
        "Meal Planning",
        "Identify Allergens",
        "Sugar Reduction",
        "Better Digestion"
    )

    val availableAllergens = listOf(
        "Eggs",
        "Corn",
        "Wheat",
        "Tree Nuts",
        "Peanuts",
        "Soy",
        "Shellfish"
    )

    val availableDiets = listOf(
        "Vegan",
        "Pescatarian",
        "Vegetarian",
        "Gluten-free",
        "Shellfish",
        "Keto",
        "Halal"
    )

    val selectedAllergens = mutableStateListOf<String>()

    val selectedDiets = mutableStateListOf<String>()
    val selectedGoals = mutableStateListOf<String>()

    var age by mutableStateOf("")

    var isMetric by mutableStateOf(true)

    var weight by mutableStateOf("")

    var activityLevel by mutableStateOf(1)

    fun toggleGoal(goal: String) {
        if (selectedGoals.contains(goal)) {
            selectedGoals.remove(goal)
        } else {
            selectedGoals.add(goal)
        }
    }

    fun toggleAllergen(allergen: String) {
        if (selectedAllergens.contains(allergen)) {
            selectedAllergens.remove(allergen)
        } else {
            selectedAllergens.add(allergen)
        }
    }
    fun toggleDiet(diet: String) {
        if (selectedDiets.contains(diet)) {
            selectedDiets.remove(diet)
        } else {
            selectedDiets.add(diet)
        }
    }


    fun updateActivityLevel(level: Int) {
        if (level in 1..5) {
            activityLevel = level
        }
    }
}