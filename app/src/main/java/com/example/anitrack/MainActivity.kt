package com.example.anitrack

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.anitrack.ui.AnitrackApp
import com.example.anitrack.ui.global.BottomNavigationBar
import com.example.anitrack.ui.theme.AnitrackTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AnitrackTheme {
                val navController: NavHostController = rememberNavController()
                Scaffold(
                    bottomBar = { BottomNavigationBar(
                        navController = navController,
                        modifier = Modifier
                    ) },
                    modifier = Modifier.fillMaxSize()
                ) { innerPadding ->
                    AnitrackApp(
                        modifier = Modifier.padding(innerPadding),
                        navController = navController
                    )
                }
            }
        }
    }
}