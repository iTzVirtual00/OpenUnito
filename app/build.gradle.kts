plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)

    // hilt stuff
    id("com.google.devtools.ksp")
    id("com.google.dagger.hilt.android")

    // Json
    kotlin("plugin.serialization") version "2.3.10"
}


android {
    namespace = "me.itzvirtual.myunitox"
    compileSdk {
        version = release(36)
    }

    defaultConfig {
        applicationId = "me.itzvirtual.myunitox"
        minSdk = 28
        targetSdk = 36
        versionCode = 10
        versionName = "v0.2.6"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        // switched from 11 to 1_8 for hilt
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material3.adaptive.navigation.suite)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}

dependencies {
    implementation(libs.androidx.compose.animation.core)
    implementation(libs.androidx.compose.animation)
    implementation(libs.androidx.compose.material.icons.core)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.core.ktx)
    implementation(libs.retrofit)
    implementation(libs.kotlinx.coroutines.android)

    implementation(libs.androidx.lifecycle.viewmodel.compose)

    // hilt stuff
    implementation(libs.hilt.android)
    ksp(libs.hilt.android.compiler)
    implementation(libs.androidx.hilt.navigation.compose)

    implementation(libs.logging.interceptor)

    // Json
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.retrofit2.kotlinx.serialization.converter)

    implementation(libs.androidx.security.crypto)
    implementation(libs.androidx.compose.material.icons.extended)

    implementation(libs.androidx.compose.ui.text.google.fonts)
}

// Generate version.json alongside the release APK
tasks.register("generateVersionJson") {
    doLast {
        val versionCode = android.defaultConfig.versionCode
        val versionName = android.defaultConfig.versionName
        val outputDir = layout.buildDirectory.dir("../release").get().asFile

        outputDir.mkdirs()

        val versionJson = File(outputDir, "version.json")
        versionJson.writeText("""
            {
                "versionCode": $versionCode,
                "versionName": "$versionName"
            }
        """.trimIndent())

        println("Generated version.json: versionCode=$versionCode, versionName=$versionName")
    }
}

// Hook the task to run after assembleRelease
tasks.whenTaskAdded {
    if (name == "assembleRelease") {
        finalizedBy("generateVersionJson")
    }
}
