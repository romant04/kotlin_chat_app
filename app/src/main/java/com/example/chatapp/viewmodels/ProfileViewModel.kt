package com.example.chatapp.viewmodels

import androidx.lifecycle.ViewModel
import com.example.chatapp.data.models.User
import com.example.chatapp.data.repository.UserRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.StateFlow

class ProfileViewModel : ViewModel() {
    // Observe the global repository instead of fetching locally
    val userState: StateFlow<User?> = UserRepository.currentUserData

    fun logout(onLogoutComplete: () -> Unit) {
        FirebaseAuth.getInstance().signOut()
        // Clear the global store on logout
        UserRepository.stopListening()
        onLogoutComplete()
    }
}