package com.smartgallery.data.model

import android.net.Uri

data class Person(
    val id: String,
    val name: String,
    val coverUri: Uri? = null,
    val photoCount: Int = 0,
    val isUnknown: Boolean = false
)
