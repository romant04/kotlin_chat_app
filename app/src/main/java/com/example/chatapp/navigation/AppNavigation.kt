package com.example.chatapp.navigation

import android.net.Uri
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
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.navArgument
import com.example.chatapp.views.ChatDetailScreen
import com.example.chatapp.views.FriendsScreen
import com.example.chatapp.views.LoginScreen
import com.example.chatapp.views.ProfileScreen
import com.example.chatapp.views.RecentChatsScreen


@Composable
fun AppNavigation(navController: NavHostController, startDestination: String) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // Only show bottom bar if the current route is one of our 3 main tabs
    val showBottomBar =
        Destinations.bottomNavItems.any { it.route == currentRoute || it.route.contains("chat_detail/") }

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
                RecentChatsScreen(
                    onNavigateToChat = { id, name ->
                        // 1. Encode the name to handle spaces/special characters
                        val encodedName = Uri.encode(name)

                        // 2. Navigate using the pattern: chat_detail/{chatId}/{userName}
                        navController.navigate("chat_detail/$id/$encodedName")
                    }
                )
            }

            composable(Destinations.Friends.route) {
                FriendsScreen(
                    onNavigateToChat = { route ->
                        navController.navigate("chat_detail/$route")
                    }
                )
            }

            composable(Destinations.Profile.route) {
                ProfileScreen(onLogout = {
                    navController.navigate(Destinations.Login.route) {
                        // This clears the entire history so the user is truly "out"
                        popUpTo(0) { inclusive = true }
                    }
                })
            }

            composable(
                route = "chat_detail/{chatId}/{friendName}",
                arguments = listOf(
                    navArgument("chatId") { type = NavType.StringType },
                    navArgument("friendName") { type = NavType.StringType }
                )
            ) { backStackEntry ->
                val chatId = backStackEntry.arguments?.getString("chatId") ?: ""
                val friendName = backStackEntry.arguments?.getString("friendName") ?: "Friend"

                ChatDetailScreen(
                    chatId = chatId,
                    friendName = friendName,
                    onBack = { navController.popBackStack() }
                )
            }
        }
    }
}