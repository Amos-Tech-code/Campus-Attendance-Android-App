import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    // Kotlin Serialization
    alias(libs.plugins.kotlin.serialization)
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
    //Material3-Extended icons
    implementation (libs.androidx.material.icons.extended)
    // Kotlinx JSON serialization
    implementation(libs.kotlinx.serialization.json)
    //Google
    implementation("androidx.credentials:credentials:1.5.0")
    implementation("androidx.credentials:credentials-play-services-auth:1.5.0")
    implementation("com.google.android.libraries.identity.googleid:googleid:1.1.1")
    //Kotlin Date Time
    implementation(libs.kotlinx.datetime)
    // Retrofit
    implementation(libs.retrofit)
    // Gson converter for Retrofit
    implementation(libs.converter.gson)
    //Logging
    implementation(libs.logging.interceptor)

}