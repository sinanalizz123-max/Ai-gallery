package com.smartgallery.data.demo

import android.net.Uri
import com.smartgallery.data.model.Album
import com.smartgallery.data.model.AlbumType
import com.smartgallery.data.model.MediaItem
import com.smartgallery.data.model.MediaType
import com.smartgallery.data.model.Person

object DemoData {
    val people = listOf(
        Person(id = "p1", name = "Ali", photoCount = 42),
        Person(id = "p2", name = "Maya", photoCount = 31),
        Person(id = "p3", name = "Unknown", photoCount = 18, isUnknown = true),
        Person(id = "p4", name = "Pet", photoCount = 12)
    )

    val media = List(60) { index ->
        MediaItem(
            id = index.toLong(),
            uri = Uri.parse("file:///android_asset/demo_${index % 6}.jpg"),
            displayName = "IMG_$index.jpg",
            dateTakenEpochMillis = System.currentTimeMillis() - index * 86_400_000L,
            mediaType = MediaType.PHOTO,
            label = when (index % 4) {
                0 -> "Ali"
                1 -> "Maya"
                2 -> "Unknown"
                else -> "Pet"
            },
            peopleCount = (1..4).random(),
            keywords = listOf("dog", "night", "car", "city").shuffled().take(2)
        )
    }

    val albums = listOf(
        Album(id = "sys_camera", name = "Camera", photoCount = 152, type = AlbumType.SYSTEM),
        Album(id = "sys_whatsapp", name = "WhatsApp", photoCount = 98, type = AlbumType.SYSTEM),
        Album(id = "sys_screens", name = "Screenshots", photoCount = 44, type = AlbumType.SYSTEM),
        Album(id = "ai_selfies", name = "Selfies", photoCount = 36, type = AlbumType.AI),
        Album(id = "ai_group", name = "Group photos", photoCount = 28, type = AlbumType.AI),
        Album(id = "ai_pets", name = "Pets", photoCount = 19, type = AlbumType.AI)
    )
}
