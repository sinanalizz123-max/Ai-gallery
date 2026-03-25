package com.smartgallery.ui.screens

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SmallTopAppBar
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import coil.size.Size
import com.smartgallery.data.model.FaceBox
import com.smartgallery.data.repository.FaceRepository
import com.smartgallery.data.repository.MediaRepository
import com.smartgallery.ui.viewmodel.PhotoDetailViewModel
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PhotoDetailScreen(
    mediaId: Long,
    mediaRepository: MediaRepository,
    faceRepository: FaceRepository
) {
    val viewModel: PhotoDetailViewModel = viewModel(
        factory = PhotoDetailViewModelFactory(mediaRepository, faceRepository)
    )
    val media by viewModel.media.collectAsState(initial = null)
    val details by viewModel.details.collectAsState(initial = null)
    val metadata by viewModel.metadata.collectAsState(initial = null)
    val faces by viewModel.faceBoxes.collectAsState(initial = emptyList<FaceBox>())
    var selectedFaceIndex by remember { mutableStateOf<Int?>(null) }
    var menuExpanded by remember { mutableStateOf(false) }
    var showRenameDialog by remember { mutableStateOf(false) }
    var showDetailsDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showMetadataDialog by remember { mutableStateOf(false) }
    var renameText by remember { mutableStateOf("") }
    var tagsText by remember { mutableStateOf("") }
    var notesText by remember { mutableStateOf("") }
    var statusMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(mediaId) {
        viewModel.load(mediaId)
    }

    val context = LocalContext.current
    val imageRequest = remember(media?.uri) {
        ImageRequest.Builder(context)
            .data(media?.uri)
            .size(Size.ORIGINAL)
            .build()
    }

    Scaffold(
        topBar = {
            SmallTopAppBar(
                title = { Text(text = media?.displayName ?: "Photo") },
                actions = {
                    IconButton(onClick = { menuExpanded = true }) {
                        Icon(imageVector = Icons.Filled.MoreVert, contentDescription = "Actions")
                    }
                    DropdownMenu(
                        expanded = menuExpanded,
                        onDismissRequest = { menuExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Rename") },
                            onClick = {
                                menuExpanded = false
                                renameText = media?.displayName.orEmpty()
                                showRenameDialog = true
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Details") },
                            onClick = {
                                menuExpanded = false
                                showDetailsDialog = true
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Edit Tags/Notes") },
                            onClick = {
                                menuExpanded = false
                                tagsText = metadata?.tags.orEmpty()
                                notesText = metadata?.notes.orEmpty()
                                showMetadataDialog = true
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Delete") },
                            onClick = {
                                menuExpanded = false
                                showDeleteDialog = true
                            }
                        )
                    }
                }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            AsyncImage(
                model = imageRequest,
                contentDescription = media?.displayName,
                contentScale = ContentScale.Fit,
                modifier = Modifier.fillMaxSize()
            )

            BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                val width = maxWidth
                val height = maxHeight
                for (index in faces.indices) {
                    val face = faces[index]
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
                        modifier = Modifier.offset(left, top + boxHeight)
                    ) {
                        DropdownMenuItem(text = { Text(face.personId ?: "Unknown") }, onClick = {})
                        DropdownMenuItem(text = { Text("Change name") }, onClick = {})
                        DropdownMenuItem(text = { Text("Mark wrong") }, onClick = {})
                        DropdownMenuItem(text = { Text("Add new person") }, onClick = {})
                    }
                }
            }

            statusMessage?.let { message ->
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(text = message, color = MaterialTheme.colorScheme.error)
                }
            }
        }
    }

    if (showRenameDialog) {
        AlertDialog(
            onDismissRequest = { showRenameDialog = false },
            title = { Text("Rename") },
            text = {
                OutlinedTextField(
                    value = renameText,
                    onValueChange = { renameText = it },
                    label = { Text("New name") }
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showRenameDialog = false
                        viewModel.rename(renameText) { success ->
                            statusMessage = if (success) null else "Rename failed"
                        }
                    }
                ) { Text("Save") }
            },
            dismissButton = {
                TextButton(onClick = { showRenameDialog = false }) { Text("Cancel") }
            }
        )
    }

    if (showDetailsDialog) {
        AlertDialog(
            onDismissRequest = { showDetailsDialog = false },
            title = { Text("Details") },
            text = {
                Column {
                    Text(text = "Name: ${details?.displayName ?: ""}")
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(text = "Date: ${details?.dateTakenEpochMillis ?: 0L}")
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(text = "Type: ${details?.mimeType ?: ""}")
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(text = "Size: ${details?.sizeBytes ?: 0L} bytes")
                }
            },
            confirmButton = {
                TextButton(onClick = { showDetailsDialog = false }) { Text("Close") }
            }
        )
    }

    if (showMetadataDialog) {
        AlertDialog(
            onDismissRequest = { showMetadataDialog = false },
            title = { Text("Edit Metadata") },
            text = {
                Column {
                    OutlinedTextField(
                        value = tagsText,
                        onValueChange = { tagsText = it },
                        label = { Text("Tags (comma separated)") }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = notesText,
                        onValueChange = { notesText = it },
                        label = { Text("Notes") }
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showMetadataDialog = false
                        viewModel.updateMetadata(
                            tagsText.split(",").map { it.trim() }.filter { it.isNotEmpty() },
                            notesText
                        )
                    }
                ) { Text("Save") }
            },
            dismissButton = {
                TextButton(onClick = { showMetadataDialog = false }) { Text("Cancel") }
            }
        )
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete photo") },
            text = { Text("This will remove the photo from your device.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        viewModel.delete { success ->
                            statusMessage = if (success) null else "Delete failed"
                        }
                    }
                ) { Text("Delete") }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) { Text("Cancel") }
            }
        )
    }
}

private operator fun Dp.times(value: Float): Dp = (value * this.value).dp

private class PhotoDetailViewModelFactory(
    private val mediaRepository: MediaRepository,
    private val faceRepository: FaceRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return PhotoDetailViewModel(mediaRepository, faceRepository) as T
    }
}
