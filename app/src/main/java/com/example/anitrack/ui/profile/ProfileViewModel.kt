package com.example.anitrack.ui.profile

import android.net.Uri
import android.util.Patterns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.anitrack.AnitrackApplication
import com.example.anitrack.data.AuthRepository
import com.example.anitrack.data.DatabaseCollections
import com.example.anitrack.data.DatabaseRepository
import com.example.anitrack.data.StorageRepository
import com.example.anitrack.model.Content
import com.example.anitrack.model.User
import com.example.anitrack.network.AuthState
import com.example.anitrack.network.DatabaseResult
import com.example.anitrack.network.DatabaseService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ProfileViewModel(
    private val databaseRepository: DatabaseRepository,
    private val authRepository: AuthRepository,
    private val storageRepository: StorageRepository
) : ViewModel() {

    private val _userProfile = MutableStateFlow<DatabaseResult<User?>?>(null)
    val userProfile: StateFlow<DatabaseResult<User?>?> = _userProfile.asStateFlow()

    private val _userContentList =
        MutableStateFlow<DatabaseResult<List<Content>>>(DatabaseResult.Success(emptyList()))
    val userContentList: StateFlow<DatabaseResult<List<Content>>> = _userContentList.asStateFlow()

    private val _profileEditState = MutableStateFlow<AuthState>(AuthState.Idle)
    val profileEditState: StateFlow<AuthState> = _profileEditState.asStateFlow()

    fun loadUserProfileAndFavorites(userId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val userResult = databaseRepository.readDocument(
                collectionPath = DatabaseCollections.Users,
                model = User::class.java,
                documentId = userId
            )

            if (userResult is DatabaseResult.Success) {
                val user = userResult.data
                val favoriteContentIds = user?.favorites.orEmpty().distinct()

                val contentResult = if (favoriteContentIds.isNotEmpty()) {
                    databaseRepository.readDocuments(
                        collectionPath = DatabaseCollections.Contents,
                        model = Content::class.java,
                        documentIds = favoriteContentIds
                    )
                } else {
                    DatabaseResult.Success(emptyList())
                }

                withContext(Dispatchers.Main) {
                    _userProfile.value = userResult
                    _userContentList.value = contentResult
                }
            } else {
                withContext(Dispatchers.Main) {
                    _userProfile.value = userResult
                }
            }
        }
    }

    fun resetProfileEditState() {
        _profileEditState.value = AuthState.Idle
    }

    suspend fun isFieldTaken(
        collection: DatabaseCollections,
        field: String,
        value: String
    ): Boolean {
        val result = databaseRepository.filterCollection(
            collectionPath = collection,
            fieldName = field,
            value = value,
            operation = DatabaseService.ComparisonType.Equals,
            model = User::class.java
        )
        return result is DatabaseResult.Success && result.data.isNotEmpty()
    }

    private suspend fun updateEmail(newEmail: String): AuthState {
        return try {
            val result = authRepository.updateEmail(newEmail)
            if (result is AuthState.Success) {
                AuthState.Success
            } else {
                AuthState.Error((result as AuthState.Error).exception)
            }
        } catch (e: Exception) {
            AuthState.Error(e)
        }
    }

    private suspend fun updatePassword(newPassword: String): AuthState {
        return try {
            val result = authRepository.updatePassword(newPassword)
            if (result is AuthState.Success) {
                AuthState.Success
            } else {
                AuthState.Error((result as AuthState.Error).exception)
            }
        } catch (e: Exception) {
            AuthState.Error(e)
        }
    }


    fun updateUserDetails(
        userId: String,
        currentEmail: String,
        currentUsername: String,
        newUsername: String,
        newEmail: String,
        newDescription: String?
    ) {
        viewModelScope.launch {
            _profileEditState.value = AuthState.Loading()

            val trimmedUsername = newUsername.trim()
            val trimmedEmail = newEmail.trim()

            if (trimmedUsername.length < 2) {
                _profileEditState.value =
                    AuthState.ValidationError("Username must be at least 2 characters.")
                return@launch
            }

            if (!Patterns.EMAIL_ADDRESS.matcher(trimmedEmail).matches()) {
                _profileEditState.value = AuthState.ValidationError("Invalid email format.")
                return@launch
            }

            if (trimmedUsername != currentUsername) {
                val usernameExists =
                    isFieldTaken(DatabaseCollections.Users, "username", trimmedUsername)
                if (usernameExists) {
                    _profileEditState.value = AuthState.ValidationError("Username already taken.")
                    return@launch
                }
            }

            if (trimmedEmail != currentEmail) {
                val emailExists = isFieldTaken(DatabaseCollections.Users, "email", trimmedEmail)
                if (emailExists) {
                    _profileEditState.value = AuthState.ValidationError("Email already registered.")
                    return@launch
                }
            }

            val updates = mutableMapOf<String, Any>(
                "username" to trimmedUsername,
                "email" to trimmedEmail
            )
            newDescription?.let {
                updates["description"] = it.trim()
            }

            val updateResult = databaseRepository.updateDocument(
                collectionPath = DatabaseCollections.Users,
                documentId = userId,
                updates = updates
            )

            if (updateResult is DatabaseResult.Success) {
                if (trimmedEmail != currentEmail) {
                    val emailUpdateResult = updateEmail(trimmedEmail)
                    if (emailUpdateResult !is AuthState.Success) {
                        _profileEditState.value = emailUpdateResult
                        return@launch
                    }
                }
                _profileEditState.value = AuthState.Success
            } else {
                _profileEditState.value =
                    AuthState.Error((updateResult as DatabaseResult.Failure).error)
            }
        }
    }

    fun updateUserPassword(newPassword: String, repeatPassword: String) {
        viewModelScope.launch {
            _profileEditState.value = AuthState.Loading()

            // Password validations
            if (newPassword.length < 8 || !newPassword.any { it.isDigit() } || !newPassword.any { it.isUpperCase() }) {
                _profileEditState.value =
                    AuthState.ValidationError("Password must be at least 8 characters, include a number, and an uppercase letter.")
                return@launch
            }

            if (newPassword != repeatPassword) {
                _profileEditState.value = AuthState.ValidationError("Passwords do not match.")
                return@launch
            }

            val result = updatePassword(newPassword)
            _profileEditState.value = result
        }
    }

    fun updateUserProfilePicture(userId: String, imageUrl: String) {
        viewModelScope.launch {
            _profileEditState.value = AuthState.Loading()
            val updateResult = databaseRepository.updateDocument(
                collectionPath = DatabaseCollections.Users,
                documentId = userId,
                updates = mapOf("profilePicture" to imageUrl)
            )

            if (updateResult is DatabaseResult.Success) {
                _profileEditState.value = AuthState.Success
            } else {
                _profileEditState.value =
                    AuthState.Error((updateResult as DatabaseResult.Failure).error)
            }
        }
    }

    fun updateUserProfilePictureFromUri(userId: String, imageUri: Uri) {
        viewModelScope.launch {
            _profileEditState.value = AuthState.Loading()
            val uploadResult = storageRepository.uploadProfilePicture(userId, imageUri)

            if (uploadResult is DatabaseResult.Success) {
                updateUserProfilePicture(userId, uploadResult.data)
            } else {
                _profileEditState.value =
                    AuthState.Error((uploadResult as DatabaseResult.Failure).error)
            }
        }
    }

    fun deleteAccount(userId: String) {
        viewModelScope.launch {
            _profileEditState.value = AuthState.Loading()
            val result = databaseRepository.deleteDocument(DatabaseCollections.Users, userId)
            if (result is DatabaseResult.Success) {
                val authRes = authRepository.deleteAccount()
                _profileEditState.value = authRes
            } else {
                _profileEditState.value = AuthState.Error((result as DatabaseResult.Failure).error)
            }
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application =
                    this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as AnitrackApplication
                ProfileViewModel(
                    databaseRepository = application.container.databaseRepository,
                    authRepository = application.container.authRepository,
                    storageRepository = application.container.storageRepository
                )
            }
        }
    }
}
