package com.example.chatapp.views

data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val username: String = "",
    val isRegistering: Boolean = false,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)