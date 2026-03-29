package com.example.chatapp.data.repository

import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.tasks.await

class AuthRepository {
    private val auth = Firebase.auth
    private val db = Firebase.firestore

    suspend fun loginUser(email: String, pass: String): Result<Unit> {
        return try {
            // we use await() to turn the Firebase callback into a straight line of code
            auth.signInWithEmailAndPassword(email, pass).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun registerUser(username: String, email: String, pass: String): Result<Unit> {
        return try {
            val db = Firebase.firestore

            // 1. Check Username Unique
            val snapshot = db.collection("users")
                .whereEqualTo("username", username)
                .get()
                .await() // No more addOnSuccessListener!

            if (!snapshot.isEmpty) {
                return Result.failure(Exception("Username is already taken"))
            }

            // 2. Create Auth User
            val authResult = Firebase.auth.createUserWithEmailAndPassword(email, pass).await()
            val uid = authResult.user?.uid ?: throw Exception("User ID null")

            // 3. Save to Firestore
            val userMap = mapOf(
                "uid" to uid,
                "username" to username,
                "email" to email,
                "friends" to emptyList<String>()
            )

            db.collection("users").document(uid).set(userMap).await()

            Result.success(Unit)
        } catch (e: Exception) {
            // This catches Auth errors, Network errors, and our custom "Username taken" error
            Result.failure(e)
        }
    }
}