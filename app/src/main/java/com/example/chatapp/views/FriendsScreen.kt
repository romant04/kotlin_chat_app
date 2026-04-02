package com.example.chatapp.views

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.chatapp.data.models.User
import com.example.chatapp.utils.ChatUtils
import com.example.chatapp.viewmodels.FriendScreenMode
import com.example.chatapp.viewmodels.FriendsViewModel
import com.google.firebase.auth.FirebaseAuth

@Composable
fun FriendsScreen(
    viewModel: FriendsViewModel = viewModel(),
    onNavigateToChat: (String) -> Unit
) {
    val searchText by viewModel.searchText.collectAsStateWithLifecycle()
    val mode by viewModel.screenMode.collectAsStateWithLifecycle()
    val users by viewModel.usersList.collectAsStateWithLifecycle()

    // We use Box to stack the FAB on top of the Column
    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // 1. Search Bar Area
            OutlinedTextField(
                value = searchText,
                onValueChange = { viewModel.onSearchChange(it) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                placeholder = {
                    Text(if (mode == FriendScreenMode.MY_FRIENDS) "Search friends..." else "Find new people...")
                },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    if (searchText.isNotEmpty()) {
                        IconButton(onClick = { viewModel.onSearchChange("") }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear")
                        }
                    }
                },
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )

            // 2. List Header
            Text(
                text = if (mode == FriendScreenMode.MY_FRIENDS) "My Friends" else "Global Search",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )

            // 3. The Results List
            if (users.isEmpty()) {
                EmptySearchState(searchText)
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 80.dp) // Space for the FAB
                ) {
                    items(users, key = { it.uid }) { user ->
                        UserRow(
                            user = user,
                            mode = mode,
                            onActionClick = {
                                if (mode == FriendScreenMode.MY_FRIENDS) {
                                    val myUid = FirebaseAuth.getInstance().currentUser?.uid ?: ""
                                    val chatId = ChatUtils.getChatId(myUid, user.uid)
                                    // Pass both ID and Name
                                    onNavigateToChat("$chatId/${user.username}")
                                } else {
                                    viewModel.addFriend(user)
                                }
                            }
                        )
                    }
                }
            }
        }

        // 4. The "Floating" Toggle Button
        // We align it to BottomEnd (Bottom Right)
        FloatingActionButton(
            onClick = { viewModel.toggleMode() },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp), // Standard Material padding
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
        ) {
            Icon(
                imageVector = if (mode == FriendScreenMode.MY_FRIENDS) Icons.Default.Add else Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Toggle Add Mode"
            )
        }
    }
}

@Composable
fun UserRow(user: User, mode: FriendScreenMode, onActionClick: (User) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Avatar placeholder
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.secondaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = user.username.take(1).uppercase(),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(start = 16.dp)
        ) {
            Text(text = user.username, style = MaterialTheme.typography.bodyLarge)
            Text(
                text = user.email,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Button(
            onClick = { onActionClick(user) },
            shape = RoundedCornerShape(8.dp)
        ) {
            Text(if (mode == FriendScreenMode.MY_FRIENDS) "Chat" else "Add")
        }
    }
}

@Composable
fun EmptySearchState(searchText: String) {
    val text =
        if (searchText.isEmpty()) "Start typing into searchbar to find people" else "No users found"
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(text, style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
    }
}