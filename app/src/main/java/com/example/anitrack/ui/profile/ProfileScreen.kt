package com.example.anitrack.ui.profile

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.anitrack.model.Content
import com.example.anitrack.model.User
import com.example.anitrack.network.DatabaseResult
import com.example.anitrack.ui.lists.ContentList

@Composable
fun ProfileScreen(
    modifier: Modifier = Modifier,
    userId: String,
    viewModel: ProfileViewModel,
    onContentClicked: (Int) -> Unit,
    onSignOutClick: () -> Unit,
    onDeleteAccountClick: () -> Unit,
    onEditProfileClick: () -> Unit
) {
    val userProfileState by viewModel.userProfile.collectAsState()
    val userContentListState by viewModel.userContentList.collectAsState()

    var isEditProfileDialogOpen by remember { mutableStateOf(false) }
    var isShareDialogOpen by remember { mutableStateOf(false) }

    // State to hold the selected image URI
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }

    // Launcher to pick an image from the device
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri: Uri? ->
            // If user picked an image, update selectedImageUri
            if (uri != null) {
                selectedImageUri = uri
            }
        }
    )

    // The callback passed to EditProfileDialog to initiate image selection
    val onProfilePictureChangeRequest: () -> Unit = {
        // Launch image picker for images only
        imagePickerLauncher.launch("image/*")
    }

    LaunchedEffect(userId) {
        viewModel.loadUserProfileAndFavorites(userId)
    }

    Column(modifier = modifier.fillMaxSize()) {
        when (val profileResult = userProfileState) {
            is DatabaseResult.Success -> {
                val user = profileResult.data
                user?.let { currentUser ->
                    ProfileHeader(
                        profileImageUrl = currentUser.profilePicture,
                        userName = currentUser.username,
                        joinedDate = currentUser.createdAt ?: "Unknown",
                        description = currentUser.description ?: "",
                        userId = currentUser.id,
                        onDeleteAccountClick = onDeleteAccountClick,
                        // Show the Edit Profile dialog
                        onEditProfileClick = {
                            isEditProfileDialogOpen = true
                        },
                        // Show the Share dialog
                        onShareProfileClick = {
                            isShareDialogOpen = true
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(0.3f)
                    )
                }
            }
            is DatabaseResult.Failure -> {
                Text(
                    text = "Failed to load profile: ${profileResult.error.message}",
                    modifier = Modifier.padding(16.dp)
                )
            }
            else -> Text(text = "Loading profile...", modifier = Modifier.padding(16.dp))
        }

        when (val contentResult = userContentListState) {
            is DatabaseResult.Success -> {
                val contentList = contentResult.data
                ProfileListHeader(
                    contentCount = contentList.size,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(0.05f)
                )

                ContentList(
                    contentList = contentList,
                    onContentClicked = onContentClicked,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(0.6f)
                )
            }
            is DatabaseResult.Failure -> {
                Text(
                    text = "Failed to load contents: ${contentResult.error.message}",
                    modifier = Modifier.padding(16.dp)
                )
            }
            else -> Text(text = "Loading contents...", modifier = Modifier.padding(16.dp))
        }

        Button(
            onClick = { onSignOutClick() },
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(text = "Sign Out")
        }
    }

    val userData = (userProfileState as? DatabaseResult.Success<User?>)?.data
    if (isEditProfileDialogOpen && userData != null) {
        EditProfileDialog(
            onDismissRequest = {
                isEditProfileDialogOpen = false
                selectedImageUri = null // Reset selected image when closing dialog
            },
            onDeleteAccountClick = {
                isEditProfileDialogOpen = false
                onDeleteAccountClick()
            },
            userId = userData.id,
            currentUsername = userData.username,
            currentEmail = userData.email,
            currentDescription = userData.description,
            selectedImageUri = selectedImageUri,
            viewModel = viewModel,
            onProfilePictureChangeRequest = onProfilePictureChangeRequest
        )
    }

    if (isShareDialogOpen && userData != null) {
        ShareProfileDialog(
            userId = userData.id,
            onDismiss = { isShareDialogOpen = false }
        )
    }
}
