package com.smartgallery.ui.screens

import androidx.compose.foundation.ExperimentalFoundationApi
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
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
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
import com.smartgallery.data.model.Person
import com.smartgallery.data.repository.MediaRepository
import com.smartgallery.data.repository.PeopleRepository
import com.smartgallery.ui.components.PhotoGridItem
import com.smartgallery.ui.viewmodel.PersonDetailViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PersonDetailScreen(
    personId: String,
    mediaRepository: MediaRepository,
    peopleRepository: PeopleRepository
) {
    val viewModel: PersonDetailViewModel = viewModel(
        factory = PersonDetailViewModelFactory(mediaRepository, peopleRepository)
    )
    val pagingItems = viewModel.mediaForPerson(personId).collectAsLazyPagingItems()
    val people by peopleRepository.people().collectAsState(initial = emptyList<Person>())
    val scope = rememberCoroutineScope()
    var renameText by remember { mutableStateOf("") }
    var selectedIds by remember { mutableStateOf(setOf<Long>()) }
    var mergeMenuOpen by remember { mutableStateOf(false) }

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

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            Button(onClick = { mergeMenuOpen = true }) { Text("Merge faces") }
            DropdownMenu(
                expanded = mergeMenuOpen,
                onDismissRequest = { mergeMenuOpen = false }
            ) {
                people.filter { it.id != personId }.forEach { person ->
                    DropdownMenuItem(
                        text = { Text("Merge with ${person.name}") },
                        onClick = {
                            mergeMenuOpen = false
                            scope.launch { viewModel.merge(personId, person.id) }
                        }
                    )
                }
                if (people.size <= 1) {
                    DropdownMenuItem(
                        text = { Text("No other people yet") },
                        onClick = { mergeMenuOpen = false }
                    )
                }
            }

            if (selectedIds.isNotEmpty()) {
                Button(onClick = {
                    scope.launch {
                        viewModel.removeWrong(personId, selectedIds.toList())
                        selectedIds = emptySet()
                    }
                }) { Text("Remove selected") }
            }
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
                        }
                    },
                    onLongPress = {
                        selectedIds = selectedIds.toggle(item.id)
                    }
                )
            }
        }
    }
}

private fun Set<Long>.toggle(id: Long): Set<Long> {
    return if (contains(id)) minus(id) else plus(id)
}

private class PersonDetailViewModelFactory(
    private val mediaRepository: MediaRepository,
    private val peopleRepository: PeopleRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return PersonDetailViewModel(mediaRepository, peopleRepository) as T
    }
}
