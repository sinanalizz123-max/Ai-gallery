package com.smartgallery.data.model

import android.net.Uri

enum class MediaType {
    PHOTO,
    VIDEO
}

data class MediaItem(
    val id: Long,
    val uri: Uri,
    val displayName: String,
    val dateTakenEpochMillis: Long,
    val mediaType: MediaType,
    val label: String? = null,
    val peopleCount: Int = 0,
    val keywords: List<String> = emptyList()
)
