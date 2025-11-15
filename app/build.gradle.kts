import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    // Kotlin Serialization
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ksp)
}

android {
    namespace = "com.amos_tech_code.smartattend"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.amos_tech_code.smartattend"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    val secretProperties = Properties()
    val secretPropertiesFile = File(rootDir, "secret.properties")
    if (secretPropertiesFile.exists() && secretPropertiesFile.isFile) {
        secretPropertiesFile.inputStream().use {
            secretProperties.load(it)
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )

            // Store secrets securely in BuildConfig
            buildConfigField("String", "GOOGLE_SERVER_CLIENT_ID", "\"${secretProperties.getProperty("GOOGLE_SERVER_CLIENT_ID")}\"")
        }

        debug {
            // Store secrets securely in BuildConfig
            buildConfigField("String", "GOOGLE_SERVER_CLIENT_ID", "\"${secretProperties.getProperty("GOOGLE_SERVER_CLIENT_ID")}\"")
        }

    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    kotlinOptions {
        jvmTarget = "11"
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    flavorDimensions += "environment"

    productFlavors {
        create("student") {
            dimension = "environment"
            applicationIdSuffix = ".student"
            resValue("string", "app_name", "ClassTrack")
        }
        create("lecturer") {
            dimension = "environment"
            applicationIdSuffix = ".lecturer"
            resValue("string", "app_name", "ClassTrack Pro")
        }
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

    //Koin
    implementation(libs.koin.android)
    implementation(libs.koin.android.compose)
    //SplashScreen
    implementation(libs.androidx.core.splashscreen)
    //Lifecycle
    implementation(libs.androidx.lifecycle.runtime.compose)
    //Navigation
    implementation(libs.androidx.navigation.compose)
    //Coil
    implementation(libs.coil.compose)
    implementation(libs.coil) // For image downloading
    // Worker for background task
    //implementation(libs.androidx.work.runtime.ktx)
    //Material3-Extended icons
    implementation (libs.androidx.material.icons.extended)
    // Kotlinx JSON serialization
    implementation(libs.kotlinx.serialization.json)
    //Google
    implementation(libs.androidx.credentials)
    implementation(libs.androidx.credentials.play.services.auth)
    implementation(libs.googleid)
    // Google Play Services for App Set ID
    implementation(libs.play.services.appset)
    implementation(libs.kotlinx.coroutines.play.services)
    // Accompanist permissions
    implementation(libs.accompanist.permissions)
    // Google Play Services location
    implementation(libs.play.services.location)

    //Kotlin Date Time
    implementation(libs.kotlinx.datetime)
    // Retrofit
    implementation(libs.retrofit)
    // Gson converter for Retrofit
    implementation(libs.converter.gson)
    //Logging
    implementation(libs.logging.interceptor)
    // CameraX for QR scanning
    implementation(libs.androidx.camera.camera2)
    implementation(libs.androidx.camera.lifecycle)
    implementation(libs.androidx.camera.view)

    // ML Kit Barcode Scanning
    implementation(libs.barcode.scanning)

    // ZXing for additional QR processing
    //implementation("com.google.zxing:core:3.5.2")

    // KSP
    ksp(libs.androidx.room.compiler)
    // Room
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)

}