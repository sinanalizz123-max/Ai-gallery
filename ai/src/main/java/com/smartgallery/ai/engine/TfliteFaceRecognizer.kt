package com.smartgallery.ai.engine

import android.content.Context
import com.smartgallery.data.model.FaceEmbedding
import java.io.FileInputStream
import java.nio.ByteBuffer
import java.nio.channels.FileChannel
import java.util.UUID
import kotlin.math.sqrt
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.tensorflow.lite.Interpreter

class TfliteFaceRecognizer(
    private val context: Context,
    private val modelAssetPath: String = "models/mobilefacenet.tflite",
    private val outputSizeOverride: Int? = null,
    private val numThreads: Int = 4
) : FaceRecognizer {

    private val interpreter: Interpreter by lazy {
        Interpreter(loadModelFile(), Interpreter.Options().setNumThreads(numThreads))
    }

    private val modelOutputSize: Int by lazy {
        outputSizeOverride ?: interpreter.getOutputTensor(0).shape().last()
    }

    override suspend fun extractEmbedding(inputBuffer: ByteBuffer): FaceEmbedding = withContext(Dispatchers.Default) {
        inputBuffer.rewind()
        val output = Array(1) { FloatArray(modelOutputSize) }
        interpreter.run(inputBuffer, output)
        val normalized = l2Normalize(output[0])
        FaceEmbedding(
            id = UUID.randomUUID().toString(),
            vector = normalized,
            confidence = 0.7f
        )
    }

    private fun l2Normalize(vector: FloatArray): FloatArray {
        var sum = 0f
        for (v in vector) {
            sum += v * v
        }
        val norm = sqrt(sum).coerceAtLeast(1e-6f)
        return FloatArray(vector.size) { index -> vector[index] / norm }
    }

    private fun loadModelFile(): ByteBuffer {
        val assetFile = context.assets.openFd(modelAssetPath)
        val inputStream = FileInputStream(assetFile.fileDescriptor)
        val fileChannel = inputStream.channel
        val startOffset = assetFile.startOffset
        val declaredLength = assetFile.declaredLength
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
    }
}
