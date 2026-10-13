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

    id("com.google.devtools.ksp")

    // Json
    kotlin("plugin.serialization") version "2.3.10"

    id("androidx.room")
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
        versionCode = appVersionCode
        versionName = appVersionName

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

kotlin {
    room {
        schemaDirectory("$projectDir/schemas")
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
    implementation(libs.androidx.navigation.compose)

    // koin stuff
    implementation(libs.koin.android)
    implementation(libs.koin.androidx.compose)
    implementation(libs.koin.annotations)
    ksp(libs.koin.ksp.compiler)

    implementation(libs.logging.interceptor)

    // Json
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.retrofit2.kotlinx.serialization.converter)

    implementation(libs.androidx.security.crypto)
    implementation(libs.androidx.compose.material.icons.extended)

    implementation(libs.androidx.compose.ui.text.google.fonts)

    // Room
    implementation(libs.androidx.room.runtime)
    ksp(libs.androidx.room.compiler)
    annotationProcessor(libs.androidx.room.compiler)
    implementation(libs.androidx.room.ktx)

    implementation(libs.androidx.core.splashscreen)
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


tasks.register("deployAPK") {
    group = "deployment"
    description = "Deploy APK and version.json to the server."

    val apkPath: RegularFile = layout.projectDirectory.file("/release/app-release.apk")
    val jsonPath: RegularFile = layout.projectDirectory.file("/release/version.json")
    val serverUrl = "REDACTED_WEBHOOK_URL"

    inputs.file(apkPath)
    doLast {
        val apk = apkPath.asFile
        val json = jsonPath.asFile
        if (!apk.exists()) throw GradleException("File not found: ${apk.path}")
        if (!json.exists()) throw GradleException("File not found: ${json.path}")

        val auth = env.fetchOrNull("DEPLOY_AUTH") ?: throw GradleException("Environment variable DEPLOY_AUTH not set")
        val encodedAuth = Base64.getEncoder().encodeToString(auth.toByteArray())
        val trustAllCerts: Array<TrustManager?> =
            listOf(object : X509TrustManager {
                override fun getAcceptedIssuers(): Array<X509Certificate?>? {
                    return null
                }

                override fun checkClientTrusted(
                    certs: Array<X509Certificate?>?,
                    authType: String?
                ) {
                }

                override fun checkServerTrusted(
                    certs: Array<X509Certificate?>?,
                    authType: String?
                ) {
                }
            }).toTypedArray()

        try {
            val sc = SSLContext.getInstance("SSL")
            sc.init(null, trustAllCerts, SecureRandom())
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory())
            HttpsURLConnection.setDefaultHostnameVerifier(HostnameVerifier { hostname: String?, session: SSLSession? -> true })
        } catch (e: Exception) {
            e.printStackTrace()
        }

        var connection = URL(serverUrl).openConnection() as HttpURLConnection
        connection.doOutput = true
        connection.requestMethod = "POST"
        connection.setRequestProperty("Authorization", "Basic $encodedAuth")
        connection.setRequestProperty("Content-Type", "application/octet-stream")
        connection.setRequestProperty("Content-Disposition", "attachment; filename=\"${apk.name}\"")
        println("Uploading ${apk.name} to $serverUrl...")
        apk.inputStream().use { input ->
            connection.outputStream.use { output ->
                input.copyTo(output)
            }
        }
        val responseCode = connection.responseCode
        val responseMessage = connection.responseMessage
        if (responseCode in 200..299) {
            println("Upload successful! (Status: ${responseCode})")
        } else {
            throw GradleException("Upload failed with HTTP ${responseCode}: ${responseMessage}")
        }

        connection = URL(serverUrl).openConnection() as HttpURLConnection
        connection.doOutput = true
        connection.requestMethod = "POST"
        connection.setRequestProperty("Authorization", "Basic $encodedAuth")
        connection.setRequestProperty("Content-Type", "application/octet-stream")
        connection.setRequestProperty("Content-Disposition", "attachment; filename=\"${json.name}\"")

        println("Uploading ${json.name} to $serverUrl...")
        json.inputStream().use { input ->
            connection.outputStream.use { output ->
                input.copyTo(output)
            }
        }
        if (connection.responseCode in 200..299) {
            println("Upload successful! (Status: ${connection.responseCode})")
        } else {
            throw GradleException("Upload failed with HTTP ${connection.responseCode}: ${connection.responseMessage}")
        }
    }
}