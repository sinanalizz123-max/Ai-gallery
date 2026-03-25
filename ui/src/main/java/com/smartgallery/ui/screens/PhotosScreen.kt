package com.smartgallery.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemKey
import com.smartgallery.data.repository.MediaRepository
import com.smartgallery.data.repository.SearchRepository
import com.smartgallery.ui.components.PhotoGridItem
import com.smartgallery.ui.viewmodel.PhotosViewModel

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PhotosScreen(
    paddingValues: PaddingValues,
    mediaRepository: MediaRepository,
    searchRepository: SearchRepository,
    onOpenPhoto: (Long) -> Unit
) {
    val viewModel = viewModel(factory = PhotosViewModelFactory(mediaRepository, searchRepository))
    val pagingItems = viewModel.pagedMedia.collectAsLazyPagingItems()
    var query by remember { mutableStateOf("") }
    var selectedIds by remember { mutableStateOf(setOf<Long>()) }

    val context = LocalContext.current
    val permissions = remember {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arrayOf(Manifest.permission.READ_MEDIA_IMAGES, Manifest.permission.READ_MEDIA_VIDEO)
        } else {
            arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
        }
    }
    var hasPermission by remember {
        mutableStateOf(
            permissions.all { perm ->
                ContextCompat.checkSelfPermission(context, perm) == PackageManager.PERMISSION_GRANTED
            }
        )
    }
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { result ->
        hasPermission = result.values.all { it }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (!hasPermission) {
            Text(text = "Allow gallery access", style = MaterialTheme.typography.titleLarge)
            Text(
                text = "SmartGallery AI needs access to your photos and videos to build albums and face groups.",
                style = MaterialTheme.typography.bodyMedium
            )
            Button(onClick = { launcher.launch(permissions) }) {
                Text("Grant access")
            }
            return@Column
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(
                value = query,
                onValueChange = {
                    query = it
                    viewModel.updateQuery(it)
                },
                label = { Text("Search people or keywords") },
                modifier = Modifier.weight(1f)
            )
            IconButton(onClick = { viewModel.updatePeopleFilter(min = 2, max = null) }) {
                Icon(
                    imageVector = Icons.Filled.FilterList,
                    contentDescription = "Filter"
                )
            }
        }

        if (selectedIds.isNotEmpty()) {
            Text(
                text = "${selectedIds.size} selected",
                style = MaterialTheme.typography.labelMedium
            )
        }

        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 80.dp)
        ) {
            items(
                count = pagingItems.itemCount,
                key = pagingItems.itemKey { it.id }
            ) { index ->
                val item = pagingItems[index] ?: return@items
                PhotoGridItem(
                    item = item,
                    isSelected = selectedIds.contains(item.id),
                    onClick = {
                        if (selectedIds.isNotEmpty()) {
                            selectedIds = selectedIds.toggle(item.id)
                        } else {
                            onOpenPhoto(item.id)
                        }
                    },
                    onLongPress = {
                        selectedIds = selectedIds.toggle(item.id)
                    }
                )
            }
        }

        if (pagingItems.itemCount == 0) {
            Text(
                text = "No media found yet.",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

private fun Set<Long>.toggle(id: Long): Set<Long> {
    return if (contains(id)) minus(id) else plus(id)
}

private class PhotosViewModelFactory(
    private val mediaRepository: MediaRepository,
    private val searchRepository: SearchRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return PhotosViewModel(mediaRepository, searchRepository) as T
    }
}
