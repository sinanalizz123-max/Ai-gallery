package com.smartgallery.ai.engine

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import com.google.mediapipe.framework.image.BitmapImageBuilder
import com.google.mediapipe.tasks.components.containers.NormalizedKeypoint
import com.google.mediapipe.tasks.core.BaseOptions
import com.google.mediapipe.tasks.vision.core.RunningMode
import com.google.mediapipe.tasks.vision.facedetector.FaceDetector.FaceDetectorOptions
import com.google.mediapipe.tasks.vision.facedetector.FaceDetector as MpFaceDetector
import com.smartgallery.data.model.FaceBox
import java.util.Locale

class MediaPipeFaceDetector(
    private val context: Context,
    private val modelAssetPath: String = "models/face_detection_short_range.tflite",
    private val minConfidence: Float = 0.5f
) : FaceDetector {

    private var detector: MpFaceDetector? = null
    private var initFailed: Boolean = false

    override suspend fun detectFaces(bitmap: Bitmap): List<FaceBox> {
        val activeDetector = ensureDetector() ?: return emptyList()
        val image = BitmapImageBuilder(bitmap).build()
        val result = activeDetector.detect(image)
        val width = bitmap.width.toFloat().coerceAtLeast(1f)
        val height = bitmap.height.toFloat().coerceAtLeast(1f)
        return result.detections().map { detection ->
            val box = detection.boundingBox()
            val left = (box.left / width).coerceIn(0f, 1f)
            val top = (box.top / height).coerceIn(0f, 1f)
            val right = (box.right / width).coerceIn(0f, 1f)
            val bottom = (box.bottom / height).coerceIn(0f, 1f)
            val score = detection.categories().firstOrNull()?.score() ?: 0f

            val keypoints = detection.keypoints().orElse(emptyList())
            val leftEye = pickKeypoint(
                keypoints,
                fallbackIndex = 0,
                labels = listOf("left eye", "left_eye", "lefteye")
            )
            val rightEye = pickKeypoint(
                keypoints,
                fallbackIndex = 1,
                labels = listOf("right eye", "right_eye", "righteye")
            )

            FaceBox(
                left = left,
                top = top,
                right = right,
                bottom = bottom,
                confidence = score,
                leftEyeX = leftEye?.x(),
                leftEyeY = leftEye?.y(),
                rightEyeX = rightEye?.x(),
                rightEyeY = rightEye?.y()
            )
        }
    }

    private fun ensureDetector(): MpFaceDetector? {
        if (detector != null) return detector
        if (initFailed) return null
        if (!assetExists(modelAssetPath)) {
            Log.w(TAG, "Missing MediaPipe model asset: $modelAssetPath")
            initFailed = true
            return null
        }
        return try {
            detector = MpFaceDetector.createFromOptions(
                context,
                FaceDetectorOptions.builder()
                    .setBaseOptions(BaseOptions.builder().setModelAssetPath(modelAssetPath).build())
                    .setRunningMode(RunningMode.IMAGE)
                    .setMinDetectionConfidence(minConfidence)
                    .build()
            )
            detector
        } catch (t: Throwable) {
            Log.e(TAG, "Failed to initialize MediaPipe FaceDetector", t)
            initFailed = true
            null
        }
    }

    private fun assetExists(assetPath: String): Boolean {
        return try {
            context.assets.open(assetPath).close()
            true
        } catch (t: Throwable) {
            false
        }
    }

    private fun pickKeypoint(
        keypoints: List<NormalizedKeypoint>,
        fallbackIndex: Int,
        labels: List<String>
    ): NormalizedKeypoint? {
        if (keypoints.isEmpty()) return null
        val normalizedLabels = labels.map { normalizeLabel(it) }.toSet()
        val labeled = keypoints.firstOrNull { kp ->
            kp.label().isPresent && normalizeLabel(kp.label().get()) in normalizedLabels
        }
        if (labeled != null) return labeled
        return keypoints.getOrNull(fallbackIndex)
    }

    private fun normalizeLabel(label: String): String {
        return label
            .lowercase(Locale.US)
            .replace(" ", "")
            .replace("_", "")
    }

    private companion object {
        private const val TAG = "MediaPipeFaceDetector"
    }
}
