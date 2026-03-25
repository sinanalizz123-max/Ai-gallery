package com.smartgallery.data.datasource

import android.content.ContentResolver
import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import com.smartgallery.data.model.MediaDetails
import com.smartgallery.data.model.MediaItem
import com.smartgallery.data.model.MediaType

class MediaStoreDataSource(private val context: Context) {
    fun loadAllMedia(): List<MediaItem> {
        val resolver = context.contentResolver
        val images = queryImages(resolver)
        val videos = queryVideos(resolver)
        return (images + videos).sortedByDescending { it.dateTakenEpochMillis }
    }

    fun uriFromStableId(stableId: Long): Uri {
        val (id, type) = resolveStableId(stableId)
        return if (type == MediaType.VIDEO) {
            MediaStore.Video.Media.EXTERNAL_CONTENT_URI.buildUpon()
                .appendPath(id.toString())
                .build()
        } else {
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI.buildUpon()
                .appendPath(id.toString())
                .build()
        }
    }

    fun getMediaDetails(stableId: Long): MediaDetails? {
        val (id, type) = resolveStableId(stableId)
        val resolver = context.contentResolver
        val uri = if (type == MediaType.VIDEO) {
            MediaStore.Video.Media.EXTERNAL_CONTENT_URI
        } else {
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        }
        val projection = arrayOf(
            MediaStore.MediaColumns.DISPLAY_NAME,
            MediaStore.MediaColumns.DATE_TAKEN,
            MediaStore.MediaColumns.MIME_TYPE,
            MediaStore.MediaColumns.SIZE
        )
        resolver.query(
            uri,
            projection,
            "${MediaStore.MediaColumns._ID} = ?",
            arrayOf(id.toString()),
            null
        )?.use { cursor ->
            if (cursor.moveToFirst()) {
                val name = cursor.getString(0)
                val dateTaken = cursor.getLong(1)
                val mimeType = cursor.getString(2)
                val size = cursor.getLong(3)
                return MediaDetails(
                    id = stableId,
                    uri = uriFromStableId(stableId),
                    displayName = name,
                    dateTakenEpochMillis = dateTaken,
                    mimeType = mimeType,
                    sizeBytes = size
                )
            }
        }
        return null
    }

    fun renameMedia(stableId: Long, newName: String): Boolean {
        val (id, type) = resolveStableId(stableId)
        val resolver = context.contentResolver
        val uri = if (type == MediaType.VIDEO) {
            MediaStore.Video.Media.EXTERNAL_CONTENT_URI
        } else {
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        }
        val values = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, newName)
        }
        return resolver.update(
            uri,
            values,
            "${MediaStore.MediaColumns._ID} = ?",
            arrayOf(id.toString())
        ) > 0
    }

    fun deleteMedia(stableId: Long): Boolean {
        val (id, type) = resolveStableId(stableId)
        val resolver = context.contentResolver
        val uri = if (type == MediaType.VIDEO) {
            MediaStore.Video.Media.EXTERNAL_CONTENT_URI
        } else {
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        }
        return resolver.delete(
            uri,
            "${MediaStore.MediaColumns._ID} = ?",
            arrayOf(id.toString())
        ) > 0
    }

    fun loadAllBuckets(): List<Pair<String, String>> {
        val resolver = context.contentResolver
        val projection = arrayOf(
            MediaStore.MediaColumns.BUCKET_ID,
            MediaStore.MediaColumns.BUCKET_DISPLAY_NAME
        )
        val buckets = mutableMapOf<String, String>()

        // Images
        resolver.query(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            projection,
            null, null, null
        )?.use { cursor ->
            val idCol = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.BUCKET_ID)
            val nameCol = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.BUCKET_DISPLAY_NAME)
            while (cursor.moveToNext()) {
                val id = cursor.getString(idCol)
                val name = cursor.getString(nameCol) ?: "Unknown"
                buckets[id] = name
            }
        }

        // Videos
        resolver.query(
            MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
            projection,
            null, null, null
        )?.use { cursor ->
            val idCol = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.BUCKET_ID)
            val nameCol = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.BUCKET_DISPLAY_NAME)
            while (cursor.moveToNext()) {
                val id = cursor.getString(idCol)
                val name = cursor.getString(nameCol) ?: "Unknown"
                buckets[id] = name
            }
        }

        return buckets.toList()
    }

    fun loadMediaInBucket(bucketId: String): List<MediaItem> {
        val resolver = context.contentResolver
        val projection = arrayOf(
            MediaStore.MediaColumns._ID,
            MediaStore.MediaColumns.DISPLAY_NAME,
            MediaStore.MediaColumns.DATE_TAKEN
        )
        val selection = "${MediaStore.MediaColumns.BUCKET_ID} = ?"
        val selectionArgs = arrayOf(bucketId)
        val items = mutableListOf<MediaItem>()

        resolver.query(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            projection,
            selection,
            selectionArgs,
            "${MediaStore.MediaColumns.DATE_TAKEN} DESC"
        )?.use { cursor ->
            val idCol = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns._ID)
            val nameCol = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DISPLAY_NAME)
            val dateCol = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATE_TAKEN)
            while (cursor.moveToNext()) {
                val id = cursor.getLong(idCol)
                val name = cursor.getString(nameCol)
                val date = cursor.getLong(dateCol)
                items.add(MediaItem(
                    id = makeStableId(id, MediaType.PHOTO),
                    uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI.buildUpon().appendPath(id.toString()).build(),
                    displayName = name,
                    dateTakenEpochMillis = date,
                    mediaType = MediaType.PHOTO
                ))
            }
        }

        resolver.query(
            MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
            projection,
            selection,
            selectionArgs,
            "${MediaStore.MediaColumns.DATE_TAKEN} DESC"
        )?.use { cursor ->
            val idCol = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns._ID)
            val nameCol = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DISPLAY_NAME)
            val dateCol = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATE_TAKEN)
            while (cursor.moveToNext()) {
                val id = cursor.getLong(idCol)
                val name = cursor.getString(nameCol)
                val date = cursor.getLong(dateCol)
                items.add(MediaItem(
                    id = makeStableId(id, MediaType.VIDEO),
                    uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI.buildUpon().appendPath(id.toString()).build(),
                    displayName = name,
                    dateTakenEpochMillis = date,
                    mediaType = MediaType.VIDEO
                ))
            }
        }

        return items.sortedByDescending { it.dateTakenEpochMillis }
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

    private fun resolveStableId(stableId: Long): Pair<Long, MediaType> {
        val isVideo = (stableId and VIDEO_ID_MASK) != 0L
        val realId = if (isVideo) stableId and VIDEO_ID_MASK.inv() else stableId
        return realId to if (isVideo) MediaType.VIDEO else MediaType.PHOTO
    }

    private companion object {
        private const val VIDEO_ID_MASK = 1L shl 62
    }
}
