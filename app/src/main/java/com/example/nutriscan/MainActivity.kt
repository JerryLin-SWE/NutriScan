package com.example.nutriscan

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.Composable
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.ui.unit.dp
import androidx.activity.compose.setContent
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.lifecycle.lifecycleScope
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    //temporary auth inject data
    private val email = "test1@gmail.com"
    private val password = "Test123456"

    lateinit var authId: String

    //temporary user database inject data
    private var user = User(
        firstName = "test",
        lastName = "test",
        email = "test1@gmail.com",
        age = 30
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //connects to auth and database
        val authRepository = AuthRepository()
        val firestoreClient = FirestoreClient()

        setContentView(R.layout.activity_main)

        //temporary button layout
        @Composable
        fun StyledButtons() {
            var isLoggedIn by rememberSaveable {
                mutableStateOf(authRepository.isLoggedIn())
            }
            LaunchedEffect(isLoggedIn) {
                val uid = FirebaseAuth.getInstance().currentUser?.uid ?: ""
                if (user.userId != uid) {
                    user = user.copy(userId = uid)
                }
            }
            Column(
                verticalArrangement = Arrangement.spacedBy(30.dp)
            ) {
                //register/login/logout auth buttons

                if(isLoggedIn) {
                    Text("Status: Logged In")
                    Text("Auth ID: ${user.userId}")
                    Button(onClick = {
                        authRepository.logout()
                    })
                    { Text("Log Out") }
                }

                else {
                    Button(onClick = {
                        lifecycleScope.launch {
                            isLoggedIn = authRepository.register(email, password)
                        }
                    })
                    { Text("Register") }

                    Button(onClick = {
                        lifecycleScope.launch {
                            isLoggedIn = authRepository.login(email, password)
                        }
                    })
                    { Text("Log In") }
                }


                //userData database read/update/write buttons
                Button(onClick = {
                    lifecycleScope.launch {
                        firestoreClient.insertUser(user).collect{ id ->
                            println("Firestore: Created/Set document with ID: $id")
                        }
                    }
                }) { Text("Insert") }

                Button(onClick = {lifecycleScope.launch {
                    firestoreClient.updateUser(user).collect{ result->
                        println("FirestoreClient: is updated = $result")
                    }
                }})
                { Text("Update") }

                Button(onClick = {lifecycleScope.launch {
                    firestoreClient.getUser(user.email).collect{ result->
                        if (result != null){
                            user = result
                            println("FirestoreClient: did get user userId = ${user.userId}")
                            println("FirestoreClient: did get user firstName = ${user.firstName}")
                            println("FirestoreClient: did get user lastName = ${user.lastName}")
                            println("FirestoreClient: did get user email = ${user.email}")
                            println("FirestoreClient: did get user age = ${user.age}")
                        }
                        else{
                            println("FirestoreClient: did not get user")
                        }
                    }
                }})
                { Text("Get") }
            }
        }

        //puts buttons on screen (temp)
        setContent {
            StyledButtons()
        }
    }
}