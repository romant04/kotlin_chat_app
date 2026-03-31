package com.example.chatapp.data.repository

import android.util.Log
import com.example.chatapp.data.models.User
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await

object UserRepository {
    private val _currentUserData = MutableStateFlow<User?>(null)
    val currentUserData = _currentUserData.asStateFlow()

    private var userListener: ListenerRegistration? = null

    fun startListening() {
        val auth = FirebaseAuth.getInstance()
        val uid = auth.currentUser?.uid

        // If no one is logged in, clear state and stop
        if (uid == null) {
            stopListening()
            return
        }

        // 🛡️ Safety: If already listening to THE SAME user, do nothing.
        // If it's a different user or no listener, we reset it.
        userListener?.remove()

        userListener = Firebase.firestore.collection("users").document(uid)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("UserRepository", "Listen failed.", error)
                    return@addSnapshotListener
                }

                if (snapshot != null && snapshot.exists()) {
                    _currentUserData.value = snapshot.toObject(User::class.java)
                } else {
                    _currentUserData.value = null
                }
            }
    }

    fun stopListening() {
        userListener?.remove()
        userListener = null
        _currentUserData.value = null
    }

    fun getUsersByIds(ids: List<String>): Flow<List<User>> = callbackFlow {
        if (ids.isEmpty()) {
            trySend(emptyList())
            return@callbackFlow
        }

        // Firestore 'in' query allows fetching up to 10-30 documents at once
        val query = Firebase.firestore.collection("users")
            .whereIn("uid", ids)

        val listener = query.addSnapshotListener { snapshot, _ ->
            val users =
                snapshot?.documents?.mapNotNull { it.toObject(User::class.java) } ?: emptyList()
            trySend(users)
        }
        awaitClose { listener.remove() }
    }

    fun addFriend(targetUid: String): Flow<Result<Unit>> = flow {
        val currentUid = FirebaseAuth.getInstance().currentUser?.uid ?: return@flow
        val db = Firebase.firestore

        try {
            db.collection("users").document(currentUid)
                .update("friends", FieldValue.arrayUnion(targetUid))
                .await()

            db.collection("users").document(targetUid)
                .update("friends", FieldValue.arrayUnion(currentUid))
                .await()

            emit(Result.success(Unit))
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }
}