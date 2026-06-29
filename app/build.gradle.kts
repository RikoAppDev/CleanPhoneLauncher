import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
    alias(libs.plugins.google.gms.google.services)
    alias(libs.plugins.google.firebase.crashlytics)
}

// Auto-versioning: CI sets VERSION_NAME/VERSION_CODE; local builds derive the name from git
// (e.g. "1.2.0-3-g1a2b3c-dirty") so a dev build is clearly distinguishable from a store release.
fun localGitVersionName(): String = try {
    ProcessBuilder("git", "describe", "--tags", "--always", "--dirty")
        .redirectErrorStream(true)
        .start()
        .inputStream.bufferedReader().readText().trim()
        .removePrefix("v")
        .ifEmpty { "1.0.0-dev" }
} catch (e: Exception) {
    "1.0.0-dev"
}

val ciVersionCode: Int = System.getenv("VERSION_CODE")?.toIntOrNull() ?: 1
val ciVersionName: String = System.getenv("VERSION_NAME") ?: localGitVersionName()

// Release signing: enabled only when a keystore + password are provided via env (CI secrets).
// Locally (no secrets) we fall back to debug signing so the project still builds.
val releaseKeystoreFile = file(System.getenv("KEYSTORE_FILE") ?: "release-keystore.jks")
val hasReleaseSigning =
    releaseKeystoreFile.exists() && !System.getenv("KEYSTORE_PASSWORD").isNullOrBlank()

android {
    namespace = "dev.rikoapp.cleanphonelauncher"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "dev.rikoapp.cleanphonelauncher"
        minSdk = 26
        targetSdk = 36
        versionCode = ciVersionCode
        versionName = ciVersionName

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // Make version info accessible in app at runtime
        buildConfigField("String", "VERSION_NAME_FULL", "\"$ciVersionName\"")
        buildConfigField("int", "VERSION_CODE_INT", "$ciVersionCode")
    }

    signingConfigs {
        if (hasReleaseSigning) {
            create("release") {
                storeFile = releaseKeystoreFile
                storePassword = System.getenv("KEYSTORE_PASSWORD")
                keyAlias = System.getenv("KEY_ALIAS")
                keyPassword = System.getenv("KEY_PASSWORD")
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            ndk {
                debugSymbolLevel = "FULL"
            }
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            // Use the real upload key in CI; fall back to debug for local release builds.
            signingConfig = if (hasReleaseSigning) {
                signingConfigs.getByName("release")
            } else {
                signingConfigs.getByName("debug")
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_11)
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.process)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.lifecycle.viewmodel.compose)

    // Room
    implementation(libs.androidx.room.runtime)
    implementation(libs.firebase.crashlytics)
    ksp(libs.androidx.room.compiler)
    implementation(libs.androidx.room.ktx)

    // Koin
    implementation(libs.koin.android)
    implementation(libs.koin.androidx.compose)

    // Drag & drop reordering
    implementation(libs.reorderable)

    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}