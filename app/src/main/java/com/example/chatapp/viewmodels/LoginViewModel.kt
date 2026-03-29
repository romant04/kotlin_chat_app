package com.example.chatapp.viewmodels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.chatapp.data.repository.AuthRepository
import kotlinx.coroutines.launch

class LoginViewModel(private val repository: AuthRepository = AuthRepository()) : ViewModel() {
    var email by mutableStateOf("")
    var password by mutableStateOf("")
    var username by mutableStateOf("")

    var isRegistering by mutableStateOf(false)
    var isLoading by mutableStateOf(false)
    var errorMessage by mutableStateOf<String?>(null)

    // --- Actions ---

    fun onAuthAction(onSuccess: () -> Unit) {
        // 1. Basic Validation
        if (email.isBlank() || password.isBlank() || (isRegistering && username.isBlank())) {
            errorMessage = "Please fill in all fields"
            return
        }

        // 2. Start the Coroutine
        viewModelScope.launch {
            isLoading = true
            errorMessage = null

            val result = if (isRegistering) {
                repository.registerUser(username, email, password)
            } else {
                repository.loginUser(email, password)
            }

            // 3. Handle Result
            result.onSuccess {
                onSuccess()
            }.onFailure { exception ->
                // This is where your guaranteed String handling lives
                errorMessage = exception.localizedMessage ?: "An unexpected error occurred"
            }

            isLoading = false
        }
    }

    fun toggleMode() {
        isRegistering = !isRegistering
        errorMessage = null // Clear errors when switching
    }
}