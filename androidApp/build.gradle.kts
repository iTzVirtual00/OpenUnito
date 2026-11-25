import java.io.FileInputStream
import java.net.HttpURLConnection
import java.net.URL
import java.security.SecureRandom
import java.security.cert.X509Certificate
import java.util.Base64
import java.util.Properties
import javax.net.ssl.HostnameVerifier
import javax.net.ssl.HttpsURLConnection
import javax.net.ssl.SSLContext
import javax.net.ssl.SSLSession
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager


val versionProps = Properties().apply {
    val versionPropsFile = file(rootProject.file("version.properties"))
    if (versionPropsFile.exists()) {
        load(FileInputStream(versionPropsFile))
    }
}
val appVersionCode: Int = versionProps.getProperty("versionCode")?.toIntOrNull() ?: 1
val appVersionName: String = versionProps.getProperty("versionName") ?: "v0.0.1"

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
}


android {
    namespace = "me.itzvirtual.openunito.android"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "me.itzvirtual.openunito"
        minSdk = 28
        targetSdk = 37
        versionCode = appVersionCode
        versionName = appVersionName
        manifestPlaceholders["appName"] = "OpenUnito"
        manifestPlaceholders["appIcon"] = "@mipmap/ic_launcher"
        manifestPlaceholders["appIconRound"] = "@mipmap/ic_launcher"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        debug {
            applicationIdSuffix = ".dev"
            versionNameSuffix = "-dev"
            manifestPlaceholders["appName"] = "OpenUnitoDev"
            manifestPlaceholders["appIcon"] = "@mipmap/ic_launcher_dev"
            manifestPlaceholders["appIconRound"] = "@mipmap/ic_launcher_dev"
        }
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
}

dependencies {
    implementation(project(":shared"))

    // NOTE: stripped down after KMP migration, leaving commented
//    implementation(libs.androidx.core.ktx)
//    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.core.splashscreen)
    implementation(libs.koin.android)

//    testImplementation(libs.junit)
//    androidTestImplementation(libs.androidx.junit)
}

// Generate version.json alongside the release APK
tasks.register("generateVersionJson") {
    val vCode = android.defaultConfig.versionCode ?: 1
    val vName = android.defaultConfig.versionName ?: "1.0"
    val targetDir = layout.projectDirectory.dir("release")

    inputs.property("versionCode", vCode)
    inputs.property("versionName", vName)
    outputs.dir(targetDir)

    doLast {
        val outputDir = targetDir.asFile
        outputDir.mkdirs()

        val versionJson = File(outputDir, "version.json")
        versionJson.writeText(
            """
            {
                "versionCode": $vCode,
                "versionName": "$vName"
            }
            """.trimIndent()
        )

        println("Generated version.json: versionCode=$vCode, versionName=$vName")
    }
}

// Hook the task to run after assembleRelease
tasks.whenTaskAdded {
    if (name == "assembleRelease") {
        finalizedBy("generateVersionJson")
    }
}
