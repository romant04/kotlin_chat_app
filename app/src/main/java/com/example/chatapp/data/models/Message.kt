package com.example.chatapp.data.models

data class Message(
    val senderId: String = "",
    val text: String = "",
    val timestamp: Any? = null
)