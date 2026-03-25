plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "com.smartgallery.ai"
    compileSdk = 34

    defaultConfig {
        minSdk = 26
    }

    aaptOptions {
        noCompress += "tflite"
    }
}

dependencies {
    implementation(project(":data"))

    implementation(libs.androidx.core.ktx)
    implementation(libs.lifecycle.runtime.ktx)
    implementation(libs.work.runtime.ktx)
    implementation(libs.mediapipe.tasks.vision)
    implementation(libs.tflite)
}
