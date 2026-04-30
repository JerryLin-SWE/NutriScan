package com.example.nutriscan

import com.example.nutriscan.domain.NutritionLabel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

class GeminiClient(private val apiKey: String) {

    suspend fun getNutritionOverview(
        label: NutritionLabel,
        goals: List<String>,
        allergens: List<String>,
        diets: List<String>
    ): String = withContext(Dispatchers.IO) {
        val url = URL("https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent?key=$apiKey")
        val connection = url.openConnection() as HttpURLConnection
        connection.requestMethod = "POST"
        connection.setRequestProperty("Content-Type", "application/json")
        connection.doOutput = true
        connection.connectTimeout = 10000
        connection.readTimeout = 15000

        val prompt = buildPrompt(label, goals, allergens, diets)
        val body = JSONObject().apply {
            put("contents", JSONArray().apply {
                put(JSONObject().apply {
                    put("parts", JSONArray().apply {
                        put(JSONObject().apply { put("text", prompt) })
                    })
                })
            })
        }.toString()

        OutputStreamWriter(connection.outputStream).use { it.write(body) }

        val code = connection.responseCode
        val response = if (code == 200)
            connection.inputStream.bufferedReader().readText()
        else
            connection.errorStream?.bufferedReader()?.readText() ?: ""
        println("GEMINI_RAW($code): $response")
        if (code == 200) {
            JSONObject(response)
                .getJSONArray("candidates")
                .getJSONObject(0)
                .getJSONObject("content")
                .getJSONArray("parts")
                .getJSONObject(0)
                .getString("text")
                .trim()
        } else {
            ""
        }
    }

    private fun buildPrompt(
        label: NutritionLabel,
        goals: List<String>,
        allergens: List<String>,
        diets: List<String>
    ): String {
        return """
            You are a nutrition assistant. Based on the food's nutrition info and the user's profile, give a 2-3 sentence overview of whether this food is a good choice. Be direct and specific. No markdown, no bullet points, plain sentences only.

            Nutrition facts:
            - Calories: ${label.calories} kcal
            - Protein: ${label.proteinG}g
            - Carbs: ${label.carbsG}g
            - Fat: ${label.fatG}g
            - Sugar: ${label.sugarG}g

            User profile:
            - Goals: ${goals.ifEmpty { listOf("none specified") }.joinToString(", ")}
            - Allergens to avoid: ${allergens.ifEmpty { listOf("none") }.joinToString(", ")}
            - Diet type: ${diets.ifEmpty { listOf("no specific diet") }.joinToString(", ")}
        """.trimIndent()
    }
}
