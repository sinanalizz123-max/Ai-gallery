package com.smartgallery.data.datasource

import android.content.ContentResolver
import android.content.Context
import android.provider.MediaStore
import com.smartgallery.data.model.MediaItem
import com.smartgallery.data.model.MediaType

class MediaStoreDataSource(private val context: Context) {
    fun loadAllMedia(): List<MediaItem> {
        val resolver = context.contentResolver
        val images = queryImages(resolver)
        val videos = queryVideos(resolver)
        return (images + videos).sortedByDescending { it.dateTakenEpochMillis }
    }

    private fun queryImages(contentResolver: ContentResolver): List<MediaItem> {
        val projection = arrayOf(
            MediaStore.Images.Media._ID,
            MediaStore.Images.Media.DISPLAY_NAME,
            MediaStore.Images.Media.DATE_TAKEN
        )
        contentResolver.query(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            projection,
            null,
            null,
            "${MediaStore.Images.Media.DATE_TAKEN} DESC"
        )?.use { cursor ->
            val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
            val nameColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME)
            val dateColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_TAKEN)
            val items = mutableListOf<MediaItem>()
            while (cursor.moveToNext()) {
                val id = cursor.getLong(idColumn)
                val name = cursor.getString(nameColumn)
                val dateTaken = cursor.getLong(dateColumn)
                val uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI.buildUpon()
                    .appendPath(id.toString())
                    .build()
                items.add(
                    MediaItem(
                        id = makeStableId(id, MediaType.PHOTO),
                        uri = uri,
                        displayName = name,
                        dateTakenEpochMillis = dateTaken,
                        mediaType = MediaType.PHOTO
                    )
                )
            }
            return items
        }
        return emptyList()
    }

    private fun queryVideos(contentResolver: ContentResolver): List<MediaItem> {
        val projection = arrayOf(
            MediaStore.Video.Media._ID,
            MediaStore.Video.Media.DISPLAY_NAME,
            MediaStore.Video.Media.DATE_TAKEN
        )
        contentResolver.query(
            MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
            projection,
            null,
            null,
            "${MediaStore.Video.Media.DATE_TAKEN} DESC"
        )?.use { cursor ->
            val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media._ID)
            val nameColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DISPLAY_NAME)
            val dateColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATE_TAKEN)
            val items = mutableListOf<MediaItem>()
            while (cursor.moveToNext()) {
                val id = cursor.getLong(idColumn)
                val name = cursor.getString(nameColumn)
                val dateTaken = cursor.getLong(dateColumn)
                val uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI.buildUpon()
                    .appendPath(id.toString())
                    .build()
                items.add(
                    MediaItem(
                        id = makeStableId(id, MediaType.VIDEO),
                        uri = uri,
                        displayName = name,
                        dateTakenEpochMillis = dateTaken,
                        mediaType = MediaType.VIDEO
                    )
                )
            }
            return items
        }
        return emptyList()
    }

    private fun makeStableId(id: Long, type: MediaType): Long {
        return if (type == MediaType.VIDEO) id or VIDEO_ID_MASK else id
    }

    private companion object {
        private const val VIDEO_ID_MASK = 1L shl 62
    }
}
