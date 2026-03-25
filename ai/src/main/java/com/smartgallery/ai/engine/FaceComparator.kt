package com.smartgallery.ai.engine

class FaceComparator(
    private val threshold: Float = 1.0f
) {
    fun squaredEuclidean(a: FloatArray, b: FloatArray): Float {
        require(a.size == b.size) { "Embedding size mismatch: ${a.size} vs ${b.size}" }
        var sum = 0f
        for (i in a.indices) {
            val diff = a[i] - b[i]
            sum += diff * diff
        }
        return sum
    }

    fun isSamePerson(a: FloatArray, b: FloatArray): Boolean {
        return squaredEuclidean(a, b) <= threshold
    }
}
