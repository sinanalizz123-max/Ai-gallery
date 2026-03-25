package com.smartgallery.ai.workers

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.room.Room
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.smartgallery.ai.engine.FaceEmbeddingPipeline
import com.smartgallery.ai.engine.GroupingLogic
import com.smartgallery.ai.engine.MediaPipeFaceDetector
import com.smartgallery.ai.engine.TfliteFaceRecognizer
import com.smartgallery.data.datasource.MediaStoreDataSource
import com.smartgallery.data.db.AppDatabase
import com.smartgallery.data.db.entity.EmbeddingEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.UUID

class FaceRecognitionWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        if (!hasMediaPermission(applicationContext)) {
            return@withContext Result.retry()
        }

        createNotificationChannel()
        try {
            setForeground(createForegroundInfo(0))
        } catch (e: Exception) {
            // Might fail on some Android versions if not started from foreground service properly
        }

        val prefs = applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val lastScan = prefs.getLong(KEY_LAST_SCAN, 0L)

        val dataSource = MediaStoreDataSource(applicationContext)
        val allMedia = dataSource.loadAllMedia()
        val media = allMedia.filter { it.dateTakenEpochMillis > lastScan }

        if (media.isEmpty()) {
            return@withContext Result.success()
        }

        val pipeline = FaceEmbeddingPipeline(
            faceDetector = MediaPipeFaceDetector(applicationContext),
            faceRecognizer = TfliteFaceRecognizer(applicationContext)
        )

        val db = Room.databaseBuilder(applicationContext, AppDatabase::class.java, DB_NAME)
            .fallbackToDestructiveMigration()
            .build()
        val embeddingDao = db.embeddingDao()
        val personDao = db.personDao()
        val trainingPairDao = db.trainingPairDao()
        val groupingLogic = GroupingLogic(embeddingDao, personDao, trainingPairDao)

        var newestTimestamp = lastScan
        var inserted = false

        media.forEachIndexed { index, item ->
            val progress = ((index + 1).toFloat() / media.size * 100).toInt()
            setProgress(workDataOf("progress" to progress))
            if (index % 5 == 0) {
                try {
                    setForeground(createForegroundInfo(progress))
                } catch (e: Exception) { }
            }

            newestTimestamp = maxOf(newestTimestamp, item.dateTakenEpochMillis)
            val bitmap = loadBitmap(item.uri) ?: return@forEachIndexed
            val analyses = pipeline.analyze(bitmap)
            analyses.forEach { analysis ->
                val embedding = analysis.embedding
                val box = analysis.box
                val entity = EmbeddingEntity(
                    id = UUID.randomUUID().toString(),
                    mediaId = item.id,
                    personId = null,
                    vector = floatArrayToByteArray(embedding.vector),
                    confidence = box.confidence,
                    left = box.left,
                    top = box.top,
                    right = box.right,
                    bottom = box.bottom,
                    createdAt = System.currentTimeMillis()
                )
                embeddingDao.insert(entity)
                inserted = true
            }
        }

        if (inserted) {
            groupingLogic.buildAndPersistClusters()
            groupingLogic.generateUncertainPairs()
        }

        prefs.edit().putLong(KEY_LAST_SCAN, newestTimestamp).apply()
        Result.success()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Face Recognition",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    private fun createForegroundInfo(progress: Int): ForegroundInfo {
        val notification = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setContentTitle("SmartGallery AI")
            .setContentText("Scanning for faces... $progress%")
            .setSmallIcon(android.R.drawable.ic_menu_search)
            .setProgress(100, progress, false)
            .setOngoing(true)
            .build()
        return ForegroundInfo(NOTIFICATION_ID, notification)
    }

    private fun loadBitmap(uri: Uri): Bitmap? {
        return try {
            applicationContext.contentResolver.openInputStream(uri)?.use { input ->
                val options = BitmapFactory.Options().apply {
                    inPreferredConfig = Bitmap.Config.ARGB_8888
                }
                BitmapFactory.decodeStream(input, null, options)
            }
        } catch (_: Exception) {
            null
        }
    }

    private fun floatArrayToByteArray(values: FloatArray): ByteArray {
        val buffer = ByteBuffer.allocate(values.size * 4).order(ByteOrder.nativeOrder())
        values.forEach { buffer.putFloat(it) }
        return buffer.array()
    }

    private fun hasMediaPermission(context: Context): Boolean {
        val permissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arrayOf(
                android.Manifest.permission.READ_MEDIA_IMAGES,
                android.Manifest.permission.READ_MEDIA_VIDEO
            )
        } else {
            arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE)
        }
        return permissions.all {
            ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
        }
    }

    private companion object {
        private const val PREFS_NAME = "face_recognition_worker"
        private const val KEY_LAST_SCAN = "last_scan_epoch"
        private const val DB_NAME = "smartgallery.db"
        private const val CHANNEL_ID = "face_worker_channel"
        private const val NOTIFICATION_ID = 1001
    }
}
