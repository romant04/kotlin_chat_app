package com.example.chatapp.viewmodels

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.chatapp.data.repository.AuthRepository
import com.example.chatapp.data.repository.UserRepository
import com.example.chatapp.views.LoginUiState
import kotlinx.coroutines.launch

class LoginViewModel(private val repository: AuthRepository = AuthRepository()) : ViewModel() {
    // Internal state (private)
    private var _uiState = mutableStateOf(LoginUiState())

    // Public read-only state for the Composable
    val uiState: State<LoginUiState> = _uiState

    fun onEmailChange(newValue: String) {
        _uiState.value = _uiState.value.copy(email = newValue)
    }

    fun onPasswordChange(newValue: String) {
        _uiState.value = _uiState.value.copy(password = newValue)
    }

    fun onUsernameChange(newValue: String) {
        _uiState.value = _uiState.value.copy(username = newValue)
    }

    fun toggleMode() {
        _uiState.value = _uiState.value.copy(
            isRegistering = !_uiState.value.isRegistering,
            errorMessage = null
        )
    }

    // --- Actions ---

    fun onAuthAction(onSuccess: () -> Unit) {
        val currentState = _uiState.value

        // Validation
        if (currentState.email.isBlank() || currentState.password.isBlank() ||
            (currentState.isRegistering && currentState.username.isBlank())
        ) {
            _uiState.value = _uiState.value.copy(errorMessage = "Please fill in all fields")
            return
        }

        viewModelScope.launch {
            // Set loading to true and clear old errors
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

            val result = if (currentState.isRegistering) {
                repository.registerUser(
                    username = currentState.username,
                    email = currentState.email,
                    pass = currentState.password
                )
            } else {
                repository.loginUser(
                    email = currentState.email,
                    pass = currentState.password
                )
            }

            result.onSuccess {
                UserRepository.startListening()
                onSuccess()
            }.onFailure { exception ->
                _uiState.value = _uiState.value.copy(
                    errorMessage = exception.localizedMessage ?: "Authentication failed"
                )
            }

            _uiState.value = _uiState.value.copy(isLoading = false)
        }
    }
}