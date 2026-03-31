package com.example.chatapp.data.models

data class User(
    val uid: String = "",
    val email: String = "",
    val username: String = "",
    val friends: List<String> = emptyList()
)