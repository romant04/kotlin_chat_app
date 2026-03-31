package com.example.chatapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.navigation.compose.rememberNavController
import com.example.chatapp.navigation.AppNavigation
import com.example.chatapp.navigation.Destinations
import com.example.chatapp.ui.theme.AppTheme
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 1. Initialize Firebase first
        FirebaseApp.initializeApp(this)

        enableEdgeToEdge()

        // 2. Logic to decide where to start
        val currentUser = FirebaseAuth.getInstance().currentUser
        val startRoute = if (currentUser != null) {
            Destinations.Chats.route
        } else {
            Destinations.Login.route
        }

        setContent {
            AppTheme {
                val navController = rememberNavController()
                AppNavigation(
                    navController = navController,
                    startDestination = startRoute
                )
            }
        }
    }
}