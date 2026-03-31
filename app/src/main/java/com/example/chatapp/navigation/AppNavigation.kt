package com.example.chatapp.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.chatapp.views.ChatScreen
import com.example.chatapp.views.FriendsScreen
import com.example.chatapp.views.LoginScreen
import com.example.chatapp.views.ProfileScreen


@Composable
fun AppNavigation(navController: NavHostController, startDestination: String) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // Only show bottom bar if the current route is one of our 3 main tabs
    val showBottomBar = Destinations.bottomNavItems.any { it.route == currentRoute }

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    Destinations.bottomNavItems.forEach { screen ->
                        NavigationBarItem(
                            selected = currentRoute == screen.route,
                            onClick = {
                                navController.navigate(screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = {
                                // Use a fallback or only render if not null
                                screen.icon?.let { Icon(it, contentDescription = screen.label) }
                            },
                            label = {
                                screen.label?.let { Text(it) }
                            }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Destinations.Login.route) {
                LoginScreen(onLoginSuccess = {
                    navController.navigate(Destinations.Chats.route) {
                        popUpTo(Destinations.Login.route) { inclusive = true }
                    }
                })
            }

            composable(Destinations.Chats.route) {
                ChatScreen()
            }

            composable(Destinations.Friends.route) {
                FriendsScreen(onNavigateToChat = { friendId ->
                    navController.navigate("chat_detail/$friendId")
                })
            }

            composable(Destinations.Profile.route) {
                ProfileScreen(onLogout = {
                    navController.navigate(Destinations.Login.route) {
                        // This clears the entire history so the user is truly "out"
                        popUpTo(0) { inclusive = true }
                    }
                })
            }

            // Note: This route is NOT in bottomNavItems, so the bar will disappear here!
            composable("chat_detail/{friendId}") { backStackEntry ->
                val friendId = backStackEntry.arguments?.getString("friendId")
                ChatScreen() // TODO: Swap for active/opened chat
            }
        }
    }
}