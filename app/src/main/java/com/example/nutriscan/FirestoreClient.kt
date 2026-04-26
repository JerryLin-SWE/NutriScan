package com.example.nutriscan

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch

class FirestoreClient {
    private val tag = "FirestoreClient: "
    private val db = FirebaseFirestore.getInstance()
    private val userCollection = "users"
    private val dietaryCollection = "dietaryInfo"

    //adds user data to database
    fun insertUser(user: User): Flow<Boolean> {
        return callbackFlow {
            db.collection(userCollection)
                .document(user.userId)
                .set(user.toHashMap())
                .addOnSuccessListener { document ->
                    println(tag + "insert user with id: ${user.userId}")
                    CoroutineScope(Dispatchers.IO).launch {
                        updateUser(user).collect {}
                    }
                    trySend(true)
                }
                .addOnFailureListener { e ->
                    e.printStackTrace()
                    println(tag + "error inserting user: $e")
                    trySend(false)
                }
            awaitClose {  }
        }
    }

    //updates user data in database
    fun updateUser(user: User): Flow<Boolean?> {
        return callbackFlow{
            val userId = user.userId
            if (userId.isEmpty()) {
                println(tag + "user id is empty")
                trySend(false)
            } else {
                // Handle the case where the user isn't logged in
                db.collection(userCollection)
                    .document(userId)
                    .set(user.toHashMap())
                    .addOnSuccessListener { document ->
                        println(tag + "update user with id: ${user.userId}")
                        trySend(true)
                    }
                    .addOnFailureListener { e ->
                        e.printStackTrace()
                        println(tag + "error updating user: ${e.message}")
                        trySend(false)
                    }
                awaitClose {  }
            }
        }
    }

    //gets user data from database
    fun getUser(uid: String): Flow<User?> {
        return callbackFlow{
            db.collection(userCollection).document(uid)
                .get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        trySend(document.data?.toUser())
                    } else {
                        println(tag + "user not found: $uid")
                        trySend(null)
                    }
                }
                .addOnFailureListener { e ->
                    e.printStackTrace()
                    println(tag + "error getting user: ${e.message}")
                    trySend(null)
                }
            awaitClose {  }
        }
    }

    //structure for saving data to database
    private fun User.toHashMap(): HashMap<String, Any?> {
        return hashMapOf(
            "userId" to userId,//primary key
            "firstName" to firstName,
            "lastName" to lastName,
            "email" to email
        )
    }

    //structure for reading from the database
    private fun Map<String, Any>.toUser(): User {
        return User(
            userId = this["userId"] as String,
            firstName = this["firstName"] as String,
            lastName = this["lastName"] as String,
            email = this["email"] as String
        )
    }



    //dietary Functions

    //adds dietary data to database
    fun insertDietary(dietary: Dietary): Flow<Boolean> {
        return callbackFlow {
            val docRef = db.collection(dietaryCollection).document()
            val newDietary = dietary.copy(dietaryId = docRef.id)

            docRef.set(newDietary.toHashMap())
                .addOnSuccessListener {
                    println(tag + "insert dietary info with id: ${dietary.dietaryId}")
                    trySend(true)
                }
                .addOnFailureListener { e ->
                    e.printStackTrace()
                    println(tag + "error inserting dietary info: ${e.message}")
                    trySend(false)
                }
            awaitClose {  }
        }
    }

    //updates dietary data in database
    fun updateDietary(dietary: Dietary): Flow<Boolean?> {
        return callbackFlow{
            val userId = dietary.dietaryId
            if (userId.isEmpty()) {
                println(tag + "dietary id is empty")
                trySend(false)
            } else {
                // Handle the case where the user isn't logged in
                db.collection(dietaryCollection)
                    .document(userId)
                    .set(dietary.toHashMap())
                    .addOnSuccessListener { document ->
                        println(tag + "update dietary info with id: ${dietary.dietaryId}")
                        trySend(true)
                    }
                    .addOnFailureListener { e ->
                        e.printStackTrace()
                        println(tag + "error updating dietary info: ${e.message}")
                        trySend(false)
                    }
                awaitClose {  }
            }
        }
    }

    //gets user data from database
    fun getDietary(dietaryId: String): Flow<Dietary?> {
        return callbackFlow{
            db.collection(dietaryCollection).document(dietaryId)
                .get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        trySend(document.data?.toDietary())
                    } else {
                        println(tag + "dietary info not found: $dietaryId")
                        trySend(null)
                    }
                }
                .addOnFailureListener { e ->
                    e.printStackTrace()
                    println(tag + "error getting dietary info: ${e.message}")
                    trySend(null)
                }
            awaitClose {  }
        }
    }

    //structure for saving data to database
    private fun Dietary.toHashMap(): HashMap<String, Any?> {
        return hashMapOf(
            "dietaryId" to dietaryId, //primary key
            "age" to age,
            "weight" to weight,
            "metricUnit" to metricUnit,
            "activityLevel" to activityLevel,
            "goals" to goals,
            "allergens" to allergens,
            "diets" to diets,
            "userId" to userId //foreign key
        )
    }

    //structure for reading from the database
    private fun Map<String, Any>.toDietary(): Dietary {
        return Dietary(
            dietaryId = this["dietaryId"] as String,
            age = this["age"] as Int,
            weight = this["weight"] as Int,
            metricUnit = this["metricUnit"] as String,
            activityLevel = this["activityLevel"] as Int,
            goals = this["goals"] as List<String>,
            allergens = this["allergens"] as List<String>,
            diets = this["diets"] as List<String>,
            userId = this["userId"] as String
        )
    }

    // nutrition log functions

    fun insertNutritionLog(log: NutritionLog): Flow<Boolean> {
        return callbackFlow {
            val docRef = db.collection("nutritionLogs").document()
            val newLog = log.copy(logId = docRef.id)
            docRef.set(newLog.toHashMap())
                .addOnSuccessListener {
                    println(tag + "inserted nutrition log: ${newLog.logId}")
                    trySend(true)
                }
                .addOnFailureListener { e ->
                    e.printStackTrace()
                    trySend(false)
                }
            awaitClose { }
        }
    }

    fun getWeeklyLogs(userId: String): Flow<List<NutritionLog>> {
        return callbackFlow {
            val oneWeekAgo = System.currentTimeMillis() - 7L * 24 * 60 * 60 * 1000
            db.collection("nutritionLogs")
                .whereEqualTo("userId", userId)
                .whereGreaterThanOrEqualTo("timestamp", oneWeekAgo)
                .get()
                .addOnSuccessListener { result ->
                    val logs = result.documents.mapNotNull { it.data?.toNutritionLog() }
                    trySend(logs)
                }
                .addOnFailureListener { e ->
                    e.printStackTrace()
                    trySend(emptyList())
                }
            awaitClose { }
        }
    }

    private fun NutritionLog.toHashMap(): HashMap<String, Any?> {
        return hashMapOf(
            "logId" to logId,
            "userId" to userId,
            "calories" to calories,
            "proteinG" to proteinG,
            "carbsG" to carbsG,
            "fatG" to fatG,
            "timestamp" to timestamp
        )
    }

    private fun Map<String, Any>.toNutritionLog(): NutritionLog {
        return NutritionLog(
            logId = this["logId"] as? String ?: "",
            userId = this["userId"] as? String ?: "",
            calories = (this["calories"] as? Long)?.toInt() ?: 0,
            proteinG = (this["proteinG"] as? Long)?.toInt() ?: 0,
            carbsG = (this["carbsG"] as? Long)?.toInt() ?: 0,
            fatG = (this["fatG"] as? Long)?.toInt() ?: 0,
            timestamp = this["timestamp"] as? Long ?: 0L
        )
    }
}