package com.smartgallery.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.smartgallery.data.model.Album
import com.smartgallery.data.model.AlbumType
import com.smartgallery.data.repository.AlbumRepository
import com.smartgallery.ui.viewmodel.AlbumsViewModel

@Composable
fun AlbumsScreen(
    paddingValues: PaddingValues,
    albumRepository: AlbumRepository
) {
    val viewModel = viewModel(factory = AlbumsViewModelFactory(albumRepository))
    val albums by viewModel.albumRepository.albums().collectAsState(initial = emptyList())
    val systemAlbums = albums.filter { it.type == AlbumType.SYSTEM }
    val aiAlbums = albums.filter { it.type == AlbumType.AI }

    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(bottom = 80.dp)
    ) {
        item(span = { GridItemSpan(maxLineSpan) }) {
            Text(text = "System albums", style = MaterialTheme.typography.titleLarge)
        }
        items(systemAlbums) { album ->
            AlbumCard(album)
        }
        item(span = { GridItemSpan(maxLineSpan) }) {
            Text(text = "AI albums", style = MaterialTheme.typography.titleLarge)
        }
        items(aiAlbums) { album ->
            AlbumCard(album)
        }
    }
}

@Composable
private fun AlbumCard(album: Album) {
    androidx.compose.material3.Surface(
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surface
    ) {
        androidx.compose.foundation.layout.Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            AsyncImage(
                model = album.coverUri,
                contentDescription = album.name,
                modifier = Modifier
                    .height(120.dp)
                    .fillMaxSize()
            )
            Text(text = album.name, style = MaterialTheme.typography.bodyLarge)
            Text(text = "${album.photoCount} items", style = MaterialTheme.typography.labelMedium)
        }
    }
}

private class AlbumsViewModelFactory(
    private val albumRepository: AlbumRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return AlbumsViewModel(albumRepository) as T
    }
}
