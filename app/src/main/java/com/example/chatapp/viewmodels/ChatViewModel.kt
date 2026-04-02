package com.example.chatapp.viewmodels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.chatapp.data.models.Message
import com.example.chatapp.data.repository.ChatRepository
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ChatViewModel(
    private val chatId: String,
    private val friendName: String,
) : ViewModel() {

    private val db = Firebase.firestore
    private val _messages = MutableStateFlow<List<Message>>(emptyList())
    val messages = _messages.asStateFlow()

    private var myName by mutableStateOf("")
    private val myUid = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    init {
        fetchMyName()
    }

    private fun fetchMyName() {
        db.collection("users").document(myUid).get()
            .addOnSuccessListener { doc ->
                myName = doc.getString("username") ?: "Me"
            }
    }

    // To track what the user is currently typing
    var messageText by mutableStateOf("")
        private set

    init {
        observeMessages()
    }

    private fun observeMessages() {
        viewModelScope.launch {
            ChatRepository.getMessages(chatId).collect { list ->
                _messages.value = list
            }
        }
    }

    fun updateMessageText(newValue: String) {
        messageText = newValue
    }

    fun sendMessage() {
        if (messageText.isNotBlank()) {
            // We need to figure out the friend's UID from the chatId
            // Since chatId is "uid1_uid2", we just remove our own ID
            val friendId = chatId.replace(myUid, "").replace("_", "")

            ChatRepository.sendMessage(
                chatId = chatId,
                text = messageText,
                friendId = friendId,
                myName = myName,
                friendName = friendName
            )
            messageText = ""
        }
    }

    // A Factory to allow passing the chatId
    companion object {
        fun provideFactory(
            chatId: String,
            friendName: String,
        ): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    @Suppress("UNCHECKED_CAST")
                    return ChatViewModel(chatId, friendName) as T
                }
            }
    }
}