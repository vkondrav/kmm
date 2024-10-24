package com.rogers.seekr

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil3.annotation.ExperimentalCoilApi
import coil3.compose.AsyncImage
import coil3.compose.setSingletonImageLoaderFactory
import org.jetbrains.compose.ui.tooling.preview.Preview

@OptIn(ExperimentalCoilApi::class)
@Composable
@Preview
fun App(
    viewModel: AppViewModel = AppViewModel()
) {

    setSingletonImageLoaderFactory { context ->
        getAsyncImageLoader(context)
    }

    val stations = viewModel.stations.collectAsState().value

    MaterialTheme {
        Box(modifier = Modifier.fillMaxSize()) {
            LazyColumn(
                modifier = Modifier
                    .wrapContentWidth()
            ) {
                items(stations) { station ->
                    Item(station)
                }
            }
        }
    }
}

@Composable
@Preview
fun Item(
    station: Station,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .wrapContentSize(Alignment.Center)
    ) {
        AsyncImage(
            model = station.image,
            contentDescription = null,
            modifier = Modifier
                .height(70.dp)
                .width(70.dp)
                .padding(start = 8.dp, end = 8.dp)
        )
        Column(
            modifier = Modifier
                .wrapContentSize(Alignment.Center)
        ) {
            Text(
                text = station.name,
            )
            Text(
                text = station.nowPlaying,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
