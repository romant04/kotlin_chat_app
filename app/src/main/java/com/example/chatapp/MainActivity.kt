package com.example.chatapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.chatapp.ui.theme.AppTheme
import com.example.chatapp.views.ChatScreen
import com.example.chatapp.views.LoginScreen
import com.google.firebase.FirebaseApp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        FirebaseApp.initializeApp(this)
        setContent {
            AppTheme {
                val navController = rememberNavController()
                val startRoute = "login"
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Box(modifier = Modifier.padding(innerPadding)) {
                        AppNavigation(
                            navController = navController,
                            startDestination = startRoute
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AppNavigation(navController: NavHostController, startDestination: String) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        // --- Login Screen ---
        composable("login") {
            LoginScreen(onLoginSuccess = {
                // Navigate to home and REMOVE login from the backstack
                navController.navigate("home") {
                    popUpTo("login") { inclusive = true }
                }
            })
        }

        // --- Home ---
        composable("home") {
            ChatScreen()
        }
    }
}