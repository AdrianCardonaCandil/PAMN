package com.example.anitrack.ui.lists

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.anitrack.model.Content
import com.example.anitrack.ui.global.ContentCard

@Composable
fun ContentList(
    contentList: List<Content>,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp)
    ) {
        items(contentList) { contentData ->
            ContentCard(
                content = contentData,
                userContentEpisodes = contentData.episodes ?: 0,
                onCardClicked = { /* Implementa la lógica de click */ },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 15.dp, start = 15.dp, end = 15.dp)
            )
        }
    }
}