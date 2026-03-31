package com.example.chatapp.views

import com.example.chatapp.data.models.User

data class FriendsUiState(
    val users: List<User> = emptyList(),
    val friends: List<User> = emptyList()
)