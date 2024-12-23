package com.example.anitrack.ui.lists

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.anitrack.AnitrackApplication
import com.example.anitrack.data.DatabaseCollections
import com.example.anitrack.data.DatabaseRepository
import com.example.anitrack.model.Content
import com.example.anitrack.model.User
import com.example.anitrack.network.DatabaseResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ListsViewModel(
    private val databaseRepository: DatabaseRepository
) : ViewModel() {

    private val _userContentList = MutableStateFlow<List<Content>>(emptyList())
    val userContentList: StateFlow<List<Content>> = _userContentList
    private val listHandler = ListHandler(databaseRepository)
    private val _contentProgress = MutableStateFlow<Map<String, Int>>(emptyMap())
    val contentProgress: StateFlow<Map<String, Int>> = _contentProgress

    fun loadUserContents(tabIndex: Int, userId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            _userContentList.emit(emptyList())

            val userResult = databaseRepository.readDocument(
                DatabaseCollections.Users,
                User::class.java,
                userId
            )

            if (userResult is DatabaseResult.Success) {
                val user = userResult.data
                _contentProgress.emit(user?.contentProgress ?: emptyMap()) // Actualizar el progreso

                val contentIds = when (tabIndex) {
                    0 -> user?.watching ?: emptyList()
                    1 -> user?.completed ?: emptyList()
                    2 -> user?.planToWatch ?: emptyList()
                    3 -> user?.favorites ?: emptyList()
                    else -> emptyList()
                }

                if (contentIds.isNotEmpty()) {
                    val contentsResult = databaseRepository.readDocuments(
                        DatabaseCollections.Contents,
                        Content::class.java,
                        contentIds
                    )
                    if (contentsResult is DatabaseResult.Success) {
                        _userContentList.emit(contentsResult.data)
                    } else {
                        _userContentList.emit(emptyList())
                    }
                } else {
                    _userContentList.emit(emptyList())
                }
            } else {
                _userContentList.emit(emptyList())
            }
        }
    }

    fun incrementEpisode(userId: String, contentId: String, currentEpisodes: Int) {
        updateEpisodeProgress(userId, contentId, currentEpisodes + 1)
    }

    fun decrementEpisode(userId: String, contentId: String, currentEpisodes: Int) {
        if (currentEpisodes > 0) {
            updateEpisodeProgress(userId, contentId, currentEpisodes - 1)
        }
    }

    private fun updateEpisodeProgress(userId: String, contentId: String, newEpisodes: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            val updates = mapOf("contentProgress.$contentId" to newEpisodes)
            val result = databaseRepository.updateDocument(
                DatabaseCollections.Users,
                userId,
                updates
            )

            if (result is DatabaseResult.Success) {
                withContext(Dispatchers.Main) {
                    _contentProgress.value = _contentProgress.value.toMutableMap().apply {
                        this[contentId] = newEpisodes
                    }
                }
            }
        }
    }


    fun moveToCompleted(userId: String, contentId: String) {
        viewModelScope.launch(Dispatchers.IO) {

            listHandler.removeFromList(userId, contentId, ListHandler.ListType.WATCHING)
            listHandler.addToList(userId, contentId, ListHandler.ListType.COMPLETED)
            loadUserContents(0, userId)

        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application =
                    this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as AnitrackApplication
                ListsViewModel(
                    databaseRepository = application.container.databaseRepository
                )
            }
        }
    }
}


