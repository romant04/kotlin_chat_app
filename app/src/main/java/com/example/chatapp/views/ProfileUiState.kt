package com.example.chatapp.views

data class ProfileUiState(
    val username: String = "",
    val email: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)