package com.example.chatapp.viewmodels

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.chatapp.data.repository.AuthRepository
import com.example.chatapp.views.ProfileUiState
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

class ProfileViewModel(private val repository: AuthRepository = AuthRepository()) : ViewModel() {
    private val auth = FirebaseAuth.getInstance()

    // One source of truth
    private val _uiState = mutableStateOf(ProfileUiState())
    val uiState: State<ProfileUiState> = _uiState

    init {
        loadUserData()
    }

    private fun loadUserData() {
        val user = auth.currentUser ?: return

        // Set initial email from Auth immediately, set loading to true
        _uiState.value = _uiState.value.copy(email = user.email ?: "", isLoading = true)

        viewModelScope.launch {
            val userData = repository.getUserData(user.uid)
            _uiState.value = _uiState.value.copy(
                username = userData?.username ?: "Unknown",
                isLoading = false
            )
        }
    }

    fun logout(onLogoutComplete: () -> Unit) {
        auth.signOut()
        onLogoutComplete()
    }
}