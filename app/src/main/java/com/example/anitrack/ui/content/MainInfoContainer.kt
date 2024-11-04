package com.example.anitrack.ui.content

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.anitrack.R

/* This section needs the following information to display:
* contentName: String,
* contentDescription: String,
* contentType: String,
* contentEpisodes: Int,
* contentStatus: String,
* contentScore: Float
* contentCoverImageUrl: String
* */

@Composable
fun MainInfoContainer(
    modifier: Modifier = Modifier,
    contentName: String = "Sousou No Frieren",
    contentDescription: String = "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Maecenas molestie nibh id risus blandit, non sodales elit sollicitudin. Orci varius natoque penatibus et magnis dis parturient montes, nascetur ridiculus mus. Sed accumsan odio orci, vitae commodo urna semper et.",
    contentStatus: String = "defaultContentStatus",
    contentType: String = "defaultContentType",
    contentEpisodes: Int = 0,
    contentScore: Float = 0f,
    contentCoverImageUrl: String = "defaultContentImageUrl"
){
    Column(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth()
        ) {
            Image(
                painter = painterResource(R.drawable.coverimage),
                contentDescription = null,
                modifier = Modifier
                    .padding()
                    .align(Alignment.CenterVertically)
            )
            Column(
                modifier = Modifier
                    .padding(start = 15.dp)
                    .fillMaxWidth()
            ) {
                Text(
                    text = contentName,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.titleMedium
                )
                InfoTagsLayout(
                    contentType = contentType,
                    contentEpisodes = contentEpisodes,
                    contentStatus = contentStatus,
                    contentScore = contentScore,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
        Text(
            text = contentDescription,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 25.dp),
            color = MaterialTheme.colorScheme.primary,
            style = MaterialTheme.typography.bodyMedium,
            lineHeight = 25.sp
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun InfoTagsLayout(
    contentType: String,
    contentEpisodes: Int,
    contentStatus: String,
    contentScore: Float,
    modifier: Modifier = Modifier
) {
    FlowRow(
        modifier = modifier.padding(top = 15.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        InfoTag(contentType)
        InfoTag(contentStatus)
        InfoTag("$contentEpisodes episodes")
        InfoTag("Score: $contentScore")
    }
}

@Composable
fun InfoTag(content: String){
    Text(
        text = content,
        modifier = Modifier
            .background(MaterialTheme.colorScheme.primaryContainer)
            .padding(5.dp).horizontalScroll(rememberScrollState()),
        maxLines = 1,
        color = MaterialTheme.colorScheme.onPrimaryContainer,
        style = MaterialTheme.typography.labelSmall
    )
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun MainInfoContainerPreview(){
    MainInfoContainer(
        modifier = Modifier
        .fillMaxWidth()
        .padding(15.dp)
    )
}