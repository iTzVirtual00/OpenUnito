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