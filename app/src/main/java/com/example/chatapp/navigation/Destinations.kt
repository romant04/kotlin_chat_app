package com.example.chatapp.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Person
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Destinations(
    val route: String,
    val label: String? = null,
    val icon: ImageVector? = null
) {
    object Login : Destinations("login")
    object Chats : Destinations("chats", "Chats", Icons.AutoMirrored.Filled.Chat)
    object Friends : Destinations("friends", "Friends", Icons.Default.People)
    object Profile : Destinations("profile", "Profile", Icons.Default.Person)

    fun createChatRoute(friendId: String) = "chat_room/$friendId"

    companion object {
        val bottomNavItems: List<Destinations>
            get() = listOf(Chats, Friends, Profile)
    }
}
