package com.smartgallery.ai.engine

import android.graphics.Bitmap
import android.graphics.Matrix
import android.graphics.RectF
import com.smartgallery.ai.model.FaceAnalysis
import com.smartgallery.data.model.FaceBox
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.math.atan2
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

class FaceEmbeddingPipeline(
    private val faceDetector: FaceDetector,
    private val faceRecognizer: FaceRecognizer,
    private val paddingRatio: Float = 0.2f,
    private val targetSize: Int = 160
) {
    suspend fun analyze(bitmap: Bitmap): List<FaceAnalysis> {
        val faces = faceDetector.detectFaces(bitmap)
        return faces.mapNotNull { box ->
            val crop = alignAndCrop(bitmap, box) ?: return@mapNotNull null
            val resized = Bitmap.createScaledBitmap(crop, targetSize, targetSize, true)
            val inputBuffer = bitmapToNormalizedBuffer(resized)
            val embedding = faceRecognizer.extractEmbedding(inputBuffer)
            FaceAnalysis(box = box, embedding = embedding)
        }
    }

    private fun bitmapToNormalizedBuffer(bitmap: Bitmap): ByteBuffer {
        val buffer = ByteBuffer.allocateDirect(1 * targetSize * targetSize * 3 * 4)
        buffer.order(ByteOrder.nativeOrder())
        val intValues = IntArray(targetSize * targetSize)
        bitmap.getPixels(intValues, 0, targetSize, 0, 0, targetSize, targetSize)
        var pixelIndex = 0
        for (y in 0 until targetSize) {
            for (x in 0 until targetSize) {
                val pixel = intValues[pixelIndex++]
                val r = ((pixel shr 16) and 0xFF) - 127.5f
                val g = ((pixel shr 8) and 0xFF) - 127.5f
                val b = (pixel and 0xFF) - 127.5f
                buffer.putFloat(r / 128f)
                buffer.putFloat(g / 128f)
                buffer.putFloat(b / 128f)
            }
        }
        buffer.rewind()
        return buffer
    }

    private fun alignAndCrop(bitmap: Bitmap, box: FaceBox): Bitmap? {
        val leftEyeX = box.leftEyeX
        val leftEyeY = box.leftEyeY
        val rightEyeX = box.rightEyeX
        val rightEyeY = box.rightEyeY

        return if (leftEyeX != null && leftEyeY != null && rightEyeX != null && rightEyeY != null) {
            cropAlignedFace(bitmap, box, leftEyeX, leftEyeY, rightEyeX, rightEyeY)
        } else {
            cropFace(bitmap, box)
        }
    }

    private fun cropAlignedFace(
        bitmap: Bitmap,
        box: FaceBox,
        leftEyeX: Float,
        leftEyeY: Float,
        rightEyeX: Float,
        rightEyeY: Float
    ): Bitmap? {
        val width = bitmap.width
        val height = bitmap.height
        if (width <= 1 || height <= 1) return null

        val leftEyePx = leftEyeX * width
        val leftEyePy = leftEyeY * height
        val rightEyePx = rightEyeX * width
        val rightEyePy = rightEyeY * height

        val dx = rightEyePx - leftEyePx
        val dy = rightEyePy - leftEyePy
        val angle = Math.toDegrees(atan2(dy, dx).toDouble()).toFloat()
        val centerX = (leftEyePx + rightEyePx) / 2f
        val centerY = (leftEyePy + rightEyePy) / 2f

        val matrix = Matrix()
        matrix.setRotate(-angle, centerX, centerY)

        val originalBounds = RectF(0f, 0f, width.toFloat(), height.toFloat())
        val rotatedBounds = RectF()
        matrix.mapRect(rotatedBounds, originalBounds)
        matrix.postTranslate(-rotatedBounds.left, -rotatedBounds.top)

        val rotatedBitmap = Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, true)

        val left = box.left * width
        val top = box.top * height
        val right = box.right * width
        val bottom = box.bottom * height
        val points = floatArrayOf(
            left, top,
            right, top,
            right, bottom,
            left, bottom
        )
        matrix.mapPoints(points)

        var minX = min(min(points[0], points[2]), min(points[4], points[6]))
        var minY = min(min(points[1], points[3]), min(points[5], points[7]))
        var maxX = max(max(points[0], points[2]), max(points[4], points[6]))
        var maxY = max(max(points[1], points[3]), max(points[5], points[7]))

        val padX = (maxX - minX) * paddingRatio
        val padY = (maxY - minY) * paddingRatio
        minX = (minX - padX).coerceIn(0f, rotatedBitmap.width.toFloat() - 1f)
        minY = (minY - padY).coerceIn(0f, rotatedBitmap.height.toFloat() - 1f)
        maxX = (maxX + padX).coerceIn(minX + 1f, rotatedBitmap.width.toFloat())
        maxY = (maxY + padY).coerceIn(minY + 1f, rotatedBitmap.height.toFloat())

        val cropLeft = minX.roundToInt().coerceIn(0, rotatedBitmap.width - 1)
        val cropTop = minY.roundToInt().coerceIn(0, rotatedBitmap.height - 1)
        val cropWidth = (maxX - minX).roundToInt().coerceAtLeast(1)
            .coerceAtMost(rotatedBitmap.width - cropLeft)
        val cropHeight = (maxY - minY).roundToInt().coerceAtLeast(1)
            .coerceAtMost(rotatedBitmap.height - cropTop)

        return Bitmap.createBitmap(rotatedBitmap, cropLeft, cropTop, cropWidth, cropHeight)
    }

    private fun cropFace(bitmap: Bitmap, box: FaceBox): Bitmap? {
        val width = bitmap.width
        val height = bitmap.height
        if (width <= 1 || height <= 1) return null
        val left = ((box.left - paddingRatio) * width).roundToInt().coerceIn(0, width - 1)
        val top = ((box.top - paddingRatio) * height).roundToInt().coerceIn(0, height - 1)
        val right = ((box.right + paddingRatio) * width).roundToInt().coerceIn(left + 1, width)
        val bottom = ((box.bottom + paddingRatio) * height).roundToInt().coerceIn(top + 1, height)
        return Bitmap.createBitmap(bitmap, left, top, right - left, bottom - top)
    }
}
