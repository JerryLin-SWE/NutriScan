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
    private val collection = "users"

    //adds user data to database
    fun insertUser(user: User): Flow<Boolean> {
        return callbackFlow {
            db.collection("users")
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
                db.collection(collection)
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
            db.collection("users").document(uid)
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
    private fun User.toHashMap(): HashMap<String, Any> {
        return hashMapOf(
            "userId" to userId,
            "firstName" to firstName,
            "lastName" to lastName,
            "email" to email,
            "age" to age
        )
    }

    //structure for reading from the database
    private fun Map<String, Any>.toUser(): User {
        return User(
            userId = this["userId"] as String,
            firstName = this["firstName"] as String,
            lastName = this["lastName"] as String,
            email = this["email"] as String,
            age = (this["age"] as Long).toInt(),
        )
    }
}