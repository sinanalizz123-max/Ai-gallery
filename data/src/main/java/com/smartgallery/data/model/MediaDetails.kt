package com.smartgallery.data.model

import android.net.Uri

data class MediaDetails(
    val id: Long,
    val uri: Uri,
    val displayName: String,
    val dateTakenEpochMillis: Long,
    val mimeType: String?,
    val sizeBytes: Long?
)
