package com.example.anitrack.ui.search

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.anitrack.ui.global.ContentCard

@Composable
fun SearchScreen(modifier: Modifier = Modifier){
    LazyColumn(
        modifier = modifier
    ) {
        item {
            SearchBar(
                modifier = Modifier.padding(15.dp)
            )
        }
        items(listOf(1, 2, 3, 4, 5, 6, 7, 8, 9, 10)){
            ContentCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        top = 15.dp,
                        start = 15.dp,
                        end = 15.dp
                    )
            )
        }
    }
}