package com.smartgallery.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
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
import com.smartgallery.data.model.Person
import com.smartgallery.data.repository.PeopleRepository
import com.smartgallery.ui.components.PersonAvatarCard
import com.smartgallery.ui.viewmodel.PeopleViewModel

@Composable
fun PeopleScreen(
    paddingValues: PaddingValues,
    peopleRepository: PeopleRepository,
    onPersonClick: (String) -> Unit
) {
    val viewModel: PeopleViewModel = viewModel(
        factory = PeopleViewModelFactory(peopleRepository)
    )
    val people by viewModel.people.collectAsState(initial = emptyList<Person>())
    val unknown by viewModel.unknownPeople.collectAsState(initial = emptyList<Person>())

    LazyVerticalGrid(
        columns = GridCells.Fixed(3),
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(bottom = 80.dp)
    ) {
        if (unknown.isNotEmpty()) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                Text(
                    text = "Unknown faces",
                    style = MaterialTheme.typography.titleLarge
                )
            }
            items(unknown) { person ->
                PersonAvatarCard(
                    person = person,
                    highlight = true,
                    onClick = { onPersonClick(person.id) }
                )
            }
        }

        item(span = { GridItemSpan(maxLineSpan) }) {
            Text(
                text = "People",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(top = 12.dp)
            )
        }

        items(people) { person ->
            PersonAvatarCard(
                person = person,
                onClick = { onPersonClick(person.id) }
            )
        }
    }
}

private class PeopleViewModelFactory(
    private val peopleRepository: PeopleRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return PeopleViewModel(peopleRepository) as T
    }
}
