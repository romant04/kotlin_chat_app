package com.example.chatapp.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.chatapp.data.models.User
import com.example.chatapp.data.repository.UserRepository
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class FriendScreenMode { MY_FRIENDS, ADD_PEOPLE }

class FriendsViewModel : ViewModel() {
    private val _screenMode = MutableStateFlow(FriendScreenMode.MY_FRIENDS)
    val screenMode = _screenMode.asStateFlow()

    private val _searchText = MutableStateFlow("")
    val searchText = _searchText.asStateFlow()

    // Observe the global user state as a Flow
    val userState: StateFlow<User?> = UserRepository.currentUserData


    @OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
    val usersList: StateFlow<List<User>> =
        combine(_screenMode, _searchText, userState) { mode, query, currentUser ->
            Triple(mode, query, currentUser)
        }.debounce { (mode, _, _) ->
            if (mode == FriendScreenMode.ADD_PEOPLE) 500L else 0L
        }.flatMapLatest { (mode, query, currentUser) ->
            when (mode) {
                FriendScreenMode.MY_FRIENDS -> {
                    val friendIds = currentUser?.friends ?: emptyList()
                    if (friendIds.isEmpty()) return@flatMapLatest flowOf(emptyList())

                    // 1. Get the full User objects for the IDs
                    // 2. Then filter them based on the search query
                    UserRepository.getUsersByIds(friendIds).map { fullUsers ->
                        fullUsers.filter { it.username.contains(query, ignoreCase = true) }
                    }
                }

                FriendScreenMode.ADD_PEOPLE -> {
                    if (query.isBlank()) flowOf(emptyList())
                    else searchGlobalUsersFromFirebase(query, currentUser)
                }
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private fun searchGlobalUsersFromFirebase(
        query: String,
        currentUser: User?
    ): Flow<List<User>> = callbackFlow {
        val db = Firebase.firestore
        val currentUserId = currentUser?.uid ?: ""
        val myFriendIds = currentUser?.friends ?: emptyList()

        val queryRequest = db.collection("users")
            .whereGreaterThanOrEqualTo("username", query)
            .whereLessThanOrEqualTo("username", query + "\uf8ff")
            .limit(40)

        val listener = queryRequest.addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }

            val users = snapshot?.documents?.mapNotNull { doc ->
                doc.toObject(User::class.java)
            }?.filter { potentialFriend ->
                // 1. Don't show myself
                // 2. Don't show people already in my friends list
                potentialFriend.uid != currentUserId && !myFriendIds.contains(potentialFriend.uid)
            } ?: emptyList()

            trySend(users)
        }

        awaitClose { listener.remove() }
    }

    fun onSearchChange(newText: String) {
        _searchText.value = newText
    }

    fun toggleMode() {
        _searchText.value = ""
        _screenMode.value = if (_screenMode.value == FriendScreenMode.MY_FRIENDS)
            FriendScreenMode.ADD_PEOPLE else FriendScreenMode.MY_FRIENDS
    }

    fun addFriend(targetUser: User) {
        viewModelScope.launch {
            UserRepository.addFriend(targetUser.uid).collect { result ->
                result.onSuccess {
                    _screenMode.value = FriendScreenMode.MY_FRIENDS
                    _searchText.value = ""
                }.onFailure {
                }
            }
        }
    }
}