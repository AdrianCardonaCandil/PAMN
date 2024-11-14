package com.example.anitrack.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.anitrack.navigation.AnitrackRoutes
import com.example.anitrack.ui.auth.AuthScreen
import com.example.anitrack.ui.auth.AuthViewModel
import com.example.anitrack.ui.content.ContentScreen
import com.example.anitrack.ui.content.ContentViewModel
import com.example.anitrack.ui.home.HomeScreen
import com.example.anitrack.ui.home.HomeViewModel
import com.example.anitrack.ui.lists.ListsScreen
import com.example.anitrack.ui.profile.ProfileScreen
import com.example.anitrack.ui.search.SearchScreen
import com.example.anitrack.ui.search.SearchViewModel
import com.example.anitrack.ui.theme.AnitrackTheme

@Composable
fun AnitrackApp(
    modifier: Modifier = Modifier,
    contentViewModel: ContentViewModel = viewModel(factory = ContentViewModel.Factory),
    homeViewModel: HomeViewModel = viewModel(factory = HomeViewModel.Factory),
    searchViewModel: SearchViewModel = viewModel(factory = SearchViewModel.Factory),
    authViewModel: AuthViewModel = viewModel(factory = AuthViewModel.Factory),
    navController: NavHostController = rememberNavController(),
) {
    val isLoggedIn by authViewModel.isLoggedIn.collectAsState()

    NavHost(
        startDestination = AnitrackRoutes.Home.name,
        navController = navController,
        modifier = modifier
    ) {
        composable(route = AnitrackRoutes.Home.name){
            HomeScreen(modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
                onGridContentClicked = {
                    contentViewModel.updateContentId(it)
                    navController.navigate(AnitrackRoutes.Content.name)
                },
                homeViewModel = homeViewModel
            )
        }
        composable(route = AnitrackRoutes.Search.name){
            SearchScreen(
                modifier = Modifier
                    .fillMaxSize(),
                searchViewModel = searchViewModel,
                onSearchCardClicked = {
                    contentViewModel.updateContentId(it)
                    navController.navigate((AnitrackRoutes.Content.name))
                }
            )
        }
        composable(route = AnitrackRoutes.Content.name){
            ContentScreen(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState()),
                contentViewModel = contentViewModel
            )
        }
        composable(route = AnitrackRoutes.Lists.name){
            ListsScreen(modifier = Modifier.fillMaxSize())
        }
        composable(route = AnitrackRoutes.Profile.name) {
            if (isLoggedIn) {
                ProfileScreen(
                    modifier = Modifier.fillMaxSize(),
                    onSignOutClick = {
                        authViewModel.signOut()
                        navController.navigate(AnitrackRoutes.Auth.name)
                    }
                )
            } else {
                AuthScreen(
                    modifier = Modifier.fillMaxSize(),
                    authViewModel = authViewModel,
                    onSignSuccess = {
                        navController.navigate(AnitrackRoutes.Home.name)
                    }
                )
            }
        }
        composable(route = AnitrackRoutes.Auth.name){
            AuthScreen(
                modifier = Modifier.fillMaxSize(),
                authViewModel = authViewModel,
                onSignSuccess = {
                    navController.navigate(AnitrackRoutes.Profile.name)
                }
            )
        }
    }
}

@Preview(showSystemUi = true)
@Composable
fun AnitrackAppPreview() {
    AnitrackTheme {
        AnitrackApp()
    }
}