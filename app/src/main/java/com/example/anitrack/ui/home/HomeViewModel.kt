package com.example.anitrack.ui.home

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.anitrack.AnitrackApplication
import com.example.anitrack.data.JikanRepository
import kotlinx.coroutines.launch
import java.io.IOException

class HomeViewModel(private val jikanRepository: JikanRepository) : ViewModel() {

    var totalContentsReceived: String by mutableStateOf("Trying to get contents")
        private set

    init {
        testCurrentSeasonRequest()
    }

    private fun testCurrentSeasonRequest() {
        viewModelScope.launch {
            try {
                var listResult = jikanRepository.getCurrentSeason().data
                totalContentsReceived = "The amount of contents received are: ${listResult?.size}"
            } catch (e: IOException) {
                totalContentsReceived = "There has been an error loading the contents"
            }
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                HomeViewModel(jikanRepository = (this[APPLICATION_KEY] as AnitrackApplication)
                    .container
                    .jikanRepository
                )
            }
        }
    }
}