plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
    // alias(libs.plugins.hilt)
    alias(libs.plugins.kotlinx.serialization)
}
android {
    namespace = "com.sailinghawklabs.speakfreely"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = namespace // defined above
        minSdk = 24
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            optimization {
                enable = false
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17

        // Desugaring so we can use LocalDateTime with older versions of Android
        isCoreLibraryDesugaringEnabled = true
    }
    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.junit.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.navigation.compose)
    testImplementation(libs.junit)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)

    // Room - Local Database
    implementation(libs.bundles.room)
    ksp(libs.androidx.room.compiler)

    // Hilt - DI
    //    implementation(libs.bundles.hilt)
    //    ksp(libs.hilt.compiler)

    // Koin - DI
    implementation(platform(libs.koin.bom))
    implementation(libs.bundles.koin)

    // Networking - KTOR
    implementation(platform(libs.ktor.bom))
    implementation(libs.bundles.networking.ktor)

    // Networking - Retrofit
    // implementation(libs.bundles.networking.retrofit)

    // Coil
    implementation(libs.bundles.coil)

    // Icons
    implementation(libs.bundles.icons)

    // Desugaring so we can use LocalDateTime with older versions of Android
    coreLibraryDesugaring(libs.desugar.jdk.libs)

    // Splash
    implementation(libs.androidx.core.splashscreen)

    // Datastore
    implementation(libs.androidx.datastore.preferences)

}
