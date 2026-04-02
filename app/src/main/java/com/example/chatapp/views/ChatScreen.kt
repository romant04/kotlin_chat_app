package com.example.chatapp.views

import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.chatapp.data.models.Chat
import com.example.chatapp.utils.ChatUtils.formatTimestamp
import com.example.chatapp.viewmodels.RecentChatsViewModel
import com.google.firebase.auth.FirebaseAuth


@Composable
fun RecentChatsScreen(
    onNavigateToChat: (String, String) -> Unit
) {
    val viewModel: RecentChatsViewModel = viewModel()
    val chats by viewModel.recentChats.collectAsStateWithLifecycle()
    val myUid = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    // Wrap everything in a Column!
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 16.dp) // Give the header some breathing room from the status bar
    ) {
        // 1. Header
        Text(
            text = "Recent chats",
            style = MaterialTheme.typography.headlineLarge,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )

        Spacer(modifier = Modifier.height(8.dp))

        // 2. Content Area
        if (chats.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No conversations yet", color = Color.Gray)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                items(chats, key = { it.chatId }) { chat ->
                    val friendName = chat.participantNames.entries
                        .find { it.key != myUid }?.value ?: "Unknown User"

                    val isLastMessageMine = chat.lastSenderId == myUid
                    val displayMessage =
                        if (isLastMessageMine) "You: ${chat.lastMessage}" else chat.lastMessage

                    RecentChatRow(
                        chat = chat,
                        friendName = friendName,
                        displayMessage = displayMessage,
                        onClick = {
                            val encodedName = Uri.encode(friendName)
                            onNavigateToChat(chat.chatId, encodedName)
                        }
                    )

                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        thickness = 0.5.dp,
                        color = MaterialTheme.colorScheme.outlineVariant
                    )
                }
            }
        }
    }
}

@Composable
fun RecentChatRow(
    chat: Chat,
    friendName: String,
    displayMessage: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Avatar
        Surface(
            modifier = Modifier.size(56.dp),
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primaryContainer
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = friendName.take(1).uppercase(),
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = friendName, // Always shows the person you're talking to
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = displayMessage, // Shows "You: hello" or just "hello"
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        Column(
            horizontalAlignment = Alignment.End,
            modifier = Modifier.padding(start = 8.dp)
        ) {
            Text(
                text = formatTimestamp(chat.lastTimestamp),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.outline
            )

            Spacer(modifier = Modifier.size(16.dp))
        }
    }
}