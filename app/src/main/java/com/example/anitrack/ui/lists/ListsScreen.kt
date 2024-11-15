package com.example.anitrack.ui.lists

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun ListsScreen(
    modifier: Modifier = Modifier,
    viewModel: ListsViewModel = viewModel(),
    onContentClicked: (Int) -> Unit,
    userId : String,
) {
    var selectedTabIndex by rememberSaveable { mutableIntStateOf(0) }
    val contentList by viewModel.userContentList.collectAsState()

    LaunchedEffect(selectedTabIndex) {
        viewModel.loadUserContents(selectedTabIndex, userId)
    }

    Column(modifier = modifier.fillMaxSize()) {

        ListsTabRow(selectedTabIndex) { newIndex ->
            selectedTabIndex = newIndex
            viewModel.loadUserContents(newIndex, userId)
        }

        ListHeader(
            selectedTabIndex = selectedTabIndex,
            itemCount = contentList.size
        )

        ContentList(
            contentList = contentList,
            onContentClicked = onContentClicked,
            modifier = Modifier.fillMaxSize()
        )
    }
}
