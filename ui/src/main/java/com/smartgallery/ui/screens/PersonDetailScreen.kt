package com.smartgallery.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemKey
import com.smartgallery.data.repository.MediaRepository
import com.smartgallery.data.repository.PeopleRepository
import com.smartgallery.ui.components.PhotoGridItem
import com.smartgallery.ui.viewmodel.PersonDetailViewModel
import kotlinx.coroutines.launch

@Composable
fun PersonDetailScreen(
    personId: String,
    mediaRepository: MediaRepository,
    peopleRepository: PeopleRepository
) {
    val viewModel = viewModel(factory = PersonDetailViewModelFactory(mediaRepository, peopleRepository))
    val pagingItems = viewModel.mediaForPerson(personId).collectAsLazyPagingItems()
    val scope = rememberCoroutineScope()
    var renameText by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(text = "Person details", style = MaterialTheme.typography.titleLarge)

        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = renameText,
                onValueChange = { renameText = it },
                label = { Text("Rename") },
                modifier = Modifier.weight(1f)
            )
            Button(onClick = {
                scope.launch { viewModel.rename(personId, renameText) }
            }) { Text("Save") }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = {
                scope.launch { viewModel.merge(personId, "p2") }
            }) { Text("Merge faces") }
            Button(onClick = {
                scope.launch { viewModel.removeWrong(personId, listOf(1L, 2L)) }
            }) { Text("Remove wrong") }
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
                    isSelected = false,
                    onClick = { },
                    onLongPress = { }
                )
            }
        }
    }
}

private class PersonDetailViewModelFactory(
    private val mediaRepository: MediaRepository,
    private val peopleRepository: PeopleRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return PersonDetailViewModel(mediaRepository, peopleRepository) as T
    }
}
