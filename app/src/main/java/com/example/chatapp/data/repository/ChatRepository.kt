package com.example.chatapp.data.repository

import com.example.chatapp.data.models.Chat
import com.example.chatapp.data.models.Message
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

object ChatRepository {
    private val db get() = Firebase.firestore
    private val auth get() = FirebaseAuth.getInstance()

    /**
     * Listens to real-time message updates for a specific chat room.
     */
    fun getMessages(chatId: String): Flow<List<Message>> = callbackFlow {
        val subscription = db.collection("chats")
            .document(chatId)
            .collection("messages")
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                val msgList = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(Message::class.java)
                } ?: emptyList()

                trySend(msgList)
            }

        awaitClose { subscription.remove() }
    }

    /**
     * Sends a message AND updates the parent Chat document for the Inbox.
     * We pass names here so the Inbox always has the latest display info.
     */
    fun sendMessage(
        chatId: String,
        text: String,
        friendId: String,
        myName: String,
        friendName: String
    ) {
        val myId = auth.currentUser?.uid ?: return
        val serverTime = com.google.firebase.firestore.FieldValue.serverTimestamp()

        // 1. Create the Message object
        val newMessage = Message(
            senderId = myId,
            text = text,
            timestamp = serverTime
        )

        // 2. Add message to sub-collection
        db.collection("chats")
            .document(chatId)
            .collection("messages")
            .add(newMessage)

        // 3. Update the Parent Chat document (The "Inbox" entry)
        // We use .set with Merge so it creates the doc if it doesn't exist
        val chatUpdate = Chat(
            chatId = chatId,
            participants = listOf(myId, friendId),
            participantNames = mapOf(myId to myName, friendId to friendName),
            lastMessage = text,
            lastSenderId = myId,
            lastTimestamp = serverTime
        )

        db.collection("chats")
            .document(chatId)
            .set(chatUpdate, SetOptions.merge())
    }

    /**
     * Listens to all chats where the current user is a participant.
     * This fuels the "Recent Chats" / Inbox screen.
     */
    fun getRecentChats(): Flow<List<Chat>> = callbackFlow {
        val myId = auth.currentUser?.uid ?: ""
        if (myId.isEmpty()) {
            trySend(emptyList())
            return@callbackFlow
        }

        val subscription = db.collection("chats")
            .whereArrayContains("participants", myId)
            .orderBy("lastTimestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) return@addSnapshotListener

                val chatList = snapshot?.toObjects(Chat::class.java) ?: emptyList()
                trySend(chatList)
            }

        awaitClose { subscription.remove() }
    }
}