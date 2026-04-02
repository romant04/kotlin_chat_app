package com.example.chatapp.data.models

data class Chat(
    val chatId: String = "",
    val participants: List<String> = emptyList(),
    val participantNames: Map<String, String> = emptyMap(),
    val lastMessage: String = "",
    val lastTimestamp: Any? = null,
    val lastSenderId: String = ""
)