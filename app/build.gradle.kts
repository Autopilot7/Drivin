plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id("kotlin-kapt") // Needed for annotation processing with Hilt
    id("dagger.hilt.android.plugin")
}

android {
    namespace = "com.example.drivin_final"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.drivin_final"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    
    // Add kapt options to ensure compatibility
    kapt {
        correctErrorTypes = true
        useBuildCache = true
        javacOptions {
            option("-source", "11")
            option("-target", "11")
        }
    }
}

dependencies {
    implementation(libs.hilt.android)
    implementation(libs.androidx.camera.view)
    kapt(libs.hilt.android.compiler)

    implementation(libs.androidx.activity.ktx)

    implementation("androidx.camera:camera-core:1.0.2")
    implementation("androidx.camera:camera-camera2:1.0.2")
    implementation("androidx.camera:camera-lifecycle:1.0.2")
    implementation("androidx.camera:camera-view:1.0.0-alpha27")

    implementation("com.google.mlkit:face-detection:16.0.6")

    implementation("androidx.activity:activity-ktx:1.8.2")

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}