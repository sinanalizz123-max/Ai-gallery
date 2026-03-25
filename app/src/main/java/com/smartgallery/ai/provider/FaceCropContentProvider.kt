package com.smartgallery.ai.provider

import android.content.ContentProvider
import android.content.ContentValues
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.ParcelFileDescriptor
import androidx.room.Room
import com.smartgallery.data.db.AppDatabase
import com.smartgallery.data.datasource.MediaStoreDataSource
import com.smartgallery.data.model.FaceBox
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class FaceCropContentProvider : ContentProvider() {

    override fun onCreate(): Boolean = true

    override fun query(
        uri: Uri, projection: Array<String>?, selection: String?,
        selectionArgs: Array<String>?, sortOrder: String?
    ): Cursor? = null

    override fun getType(uri: Uri): String? = "image/jpeg"

    override fun insert(uri: Uri, values: ContentValues?): Uri? = null

    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<String>?): Int = 0

    override fun update(
        uri: Uri, values: ContentValues?, selection: String?,
        selectionArgs: Array<String>?
    ): Int = 0

    override fun openFile(uri: Uri, mode: String): ParcelFileDescriptor? {
        val embeddingId = uri.lastPathSegment ?: return null
        
        val context = context ?: return null
        val db = Room.databaseBuilder(context, AppDatabase::class.java, "smartgallery.db")
            .fallbackToDestructiveMigration()
            .build()
        
        val embedding = try {
            // Need to run on a background thread if using Room, but openFile is on a binder thread.
            // We'll use a blocking call here for simplicity as it's a content provider.
            kotlinx.coroutines.runBlocking {
                db.embeddingDao().getAll().firstOrNull { it.id == embeddingId }
            }
        } catch (e: Exception) {
            null
        } ?: return null
        
        val dataSource = MediaStoreDataSource(context)
        val mediaUri = dataSource.uriFromStableId(embedding.mediaId)
        
        val bitmap = context.contentResolver.openInputStream(mediaUri)?.use { input ->
            BitmapFactory.decodeStream(input)
        } ?: return null
        
        val box = FaceBox(
            left = embedding.left,
            top = embedding.top,
            right = embedding.right,
            bottom = embedding.bottom,
            confidence = embedding.confidence,
            personId = embedding.personId
        )
        
        val crop = cropFace(bitmap, box) ?: return null
        
        val cacheFile = File(context.cacheDir, "face_$embeddingId.jpg")
        try {
            FileOutputStream(cacheFile).use { out ->
                crop.compress(Bitmap.CompressFormat.JPEG, 90, out)
            }
        } catch (e: IOException) {
            return null
        }
        
        return ParcelFileDescriptor.open(cacheFile, ParcelFileDescriptor.MODE_READ_ONLY)
    }

    private fun cropFace(bitmap: Bitmap, box: FaceBox): Bitmap? {
        val width = bitmap.width
        val height = bitmap.height
        val padding = 0.2f
        val left = ((box.left - padding) * width).toInt().coerceIn(0, width - 1)
        val top = ((box.top - padding) * height).toInt().coerceIn(0, height - 1)
        val right = ((box.right + padding) * width).toInt().coerceIn(left + 1, width)
        val bottom = ((box.bottom + padding) * height).toInt().coerceIn(top + 1, height)
        return Bitmap.createBitmap(bitmap, left, top, right - left, bottom - top)
    }

    companion object {
        const val AUTHORITY = "com.smartgallery.ai.provider"
        fun getUriForEmbedding(embeddingId: String): Uri {
            return Uri.parse("content://$AUTHORITY/face/$embeddingId")
        }
    }
}
