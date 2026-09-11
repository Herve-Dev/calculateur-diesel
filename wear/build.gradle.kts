import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.example.dieselcalculateur.wear"
    compileSdk {
        version = release(37) {
            minorApiLevel = 1
        }
    }

    val localProperties = Properties()
    val localPropertiesFile = rootProject.file("local.properties")
    if (localPropertiesFile.exists()) {
        localPropertiesFile.inputStream().use { 
            localProperties.load(it) 
        }
    }

    signingConfigs {
        create("release") {
            val path = System.getenv("KEYSTORE_PATH")
                ?: localProperties.getProperty("signing.keystore.path")
            if (path != null) {
                storeFile = file(path)
            }
            storePassword = System.getenv("KEYSTORE_PASSWORD")
                ?: localProperties.getProperty("signing.keystore.password")
            keyAlias = System.getenv("KEY_ALIAS")
                ?: localProperties.getProperty("signing.key.alias")
            keyPassword = System.getenv("KEY_PASSWORD")
                ?: localProperties.getProperty("signing.key.password")
        }
    }

    defaultConfig {
        applicationId = "com.example.dieselcalculateur.wear"
        minSdk = 30
        targetSdk = 35
        versionCode = 5
        versionName = "1.4.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            
            val releaseSigning = signingConfigs.getByName("release")
            if (releaseSigning.storeFile != null && releaseSigning.storeFile?.exists() == true) {
                signingConfig = releaseSigning
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation(project(":core"))
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.wear.compose.material)
    implementation(libs.androidx.wear.compose.foundation)
    implementation(libs.androidx.wear.compose.navigation)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.google.play.services.location)
    implementation(libs.google.play.services.wearable)
    debugImplementation(libs.androidx.compose.ui.tooling)
}
