package com.example.chatapp.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.chatapp.data.models.Chat
import com.example.chatapp.data.repository.ChatRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class RecentChatsViewModel : ViewModel() {
    // Expose the flow from the Repository as a StateFlow for the UI
    val recentChats: StateFlow<List<Chat>> = ChatRepository.getRecentChats()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Companion.WhileSubscribed(5000),
            initialValue = emptyList()
        )
}