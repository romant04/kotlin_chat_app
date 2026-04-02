package com.example.chatapp.utils

object ChatUtils {
    /**
     * Generates a unique, deterministic ID for a chat between two users.
     * By sorting the UIDs alphabetically, both users will always
     * land in the same Firestore document path.
     */
    fun getChatId(uid1: String, uid2: String): String {
        return if (uid1 < uid2) {
            "${uid1}_${uid2}"
        } else {
            "${uid2}_${uid1}"
        }
    }

    fun formatTimestamp(timestamp: Any?): String {
        return when (timestamp) {
            is com.google.firebase.Timestamp -> {
                val sdf = java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault())
                sdf.format(timestamp.toDate())
            }

            else -> "" // Covers null or the FieldValue while sending
        }
    }
}