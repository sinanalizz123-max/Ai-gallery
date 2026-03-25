package com.smartgallery.ui.screens

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.smartgallery.data.repository.MediaRepository
import com.smartgallery.ui.viewmodel.PhotoDetailViewModel

@Composable
fun PhotoDetailScreen(
    mediaId: Long,
    mediaRepository: MediaRepository
) {
    val viewModel = viewModel(factory = PhotoDetailViewModelFactory(mediaRepository))
    val media by viewModel.media.collectAsState()
    val faces by viewModel.faceBoxes.collectAsState()
    var selectedFaceIndex by remember { mutableStateOf<Int?>(null) }

    LaunchedEffect(mediaId) {
        viewModel.load(mediaId)
    }

    Box(modifier = Modifier.fillMaxSize()) {
        AsyncImage(
            model = media?.uri,
            contentDescription = media?.displayName,
            modifier = Modifier.fillMaxSize()
        )

        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val width = maxWidth
            val height = maxHeight
            faces.forEachIndexed { index, face ->
                val left = width * face.left
                val top = height * face.top
                val boxWidth = width * (face.right - face.left)
                val boxHeight = height * (face.bottom - face.top)

                Box(
                    modifier = Modifier
                        .offset(left, top)
                        .size(boxWidth, boxHeight)
                        .border(2.dp, MaterialTheme.colorScheme.secondary, RectangleShape)
                        .clickable { selectedFaceIndex = index }
                )

                DropdownMenu(
                    expanded = selectedFaceIndex == index,
                    onDismissRequest = { selectedFaceIndex = null },
                    modifier = Modifier
                        .offset(left, top + boxHeight)
                ) {
                    DropdownMenuItem(text = { Text(face.personId ?: "Unknown") }, onClick = {})
                    DropdownMenuItem(text = { Text("Change name") }, onClick = {})
                    DropdownMenuItem(text = { Text("Mark wrong") }, onClick = {})
                    DropdownMenuItem(text = { Text("Add new person") }, onClick = {})
                }
            }
        }
    }
}

private operator fun Dp.times(value: Float): Dp = (value * this.value).dp

private class PhotoDetailViewModelFactory(
    private val mediaRepository: MediaRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return PhotoDetailViewModel(mediaRepository) as T
    }
}
