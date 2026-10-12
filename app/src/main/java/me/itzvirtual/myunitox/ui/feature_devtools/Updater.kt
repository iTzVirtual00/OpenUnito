package me.itzvirtual.myunitox.ui.feature_devtools

import android.content.Context
import android.content.Intent
import android.util.Base64
import androidx.core.content.FileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.ByteArrayInputStream
import java.io.File
import java.security.KeyStore
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManagerFactory
import javax.net.ssl.X509TrustManager

@kotlinx.serialization.Serializable
data class VersionInfo(
    @kotlinx.serialization.SerialName("versionCode")
    val versionCode: Int,
    @kotlinx.serialization.SerialName("versionName")
    val versionName: String
)

sealed class UpdateCheckResult {
    data class UpdateAvailable(val versionInfo: VersionInfo) : UpdateCheckResult()
    data object NoUpdateAvailable : UpdateCheckResult()
    data class Error(val message: String) : UpdateCheckResult()
}

class HTTPUpdater(private val context: Context) {
    companion object {
        private const val WEBHOOK_BASE_URL =
            "REDACTED_WEBHOOK_URL"
        private const val USERNAME = "REDACTED_USERNAME"
        private const val PASSWORD = "REDACTED_PASSWORD"

        private const val CUSTOM_CA_CERT = """
REDACTED_CA_CERT
"""
    }

    private val client: OkHttpClient by lazy {
        createClientWithCustomCA()
    }

    // system cas OR custom ca
    private fun createClientWithCustomCA(): OkHttpClient {
        return try {
            // Load the custom CA certificate
            val certificateFactory = CertificateFactory.getInstance("X.509")
            val caInputStream = ByteArrayInputStream(CUSTOM_CA_CERT.trimIndent().toByteArray())
            val customCa = certificateFactory.generateCertificate(caInputStream) as X509Certificate
            caInputStream.close()

            val defaultTrustManagerFactory =
                TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm())
            defaultTrustManagerFactory.init(null as KeyStore?) // null = use system default
            val defaultTrustManager = defaultTrustManagerFactory.trustManagers
                .filterIsInstance<X509TrustManager>()
                .first()

            // Create a KeyStore containing the custom CA
            val customKeyStore = KeyStore.getInstance(KeyStore.getDefaultType())
            customKeyStore.load(null, null)
            customKeyStore.setCertificateEntry("custom-ca", customCa)

            // Create a TrustManager for the custom CA
            val customTrustManagerFactory =
                TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm())
            customTrustManagerFactory.init(customKeyStore)
            val customTrustManager = customTrustManagerFactory.trustManagers
                .filterIsInstance<X509TrustManager>()
                .first()

            // Create a composite TrustManager that trusts both system and custom CA
            val compositeTrustManager = object : X509TrustManager {
                override fun checkClientTrusted(
                    chain: Array<out X509Certificate>?,
                    authType: String?
                ) {
                    try {
                        defaultTrustManager.checkClientTrusted(chain, authType)
                    } catch (e: Exception) {
                        customTrustManager.checkClientTrusted(chain, authType)
                    }
                }

                override fun checkServerTrusted(
                    chain: Array<out X509Certificate>?,
                    authType: String?
                ) {
                    try {
                        defaultTrustManager.checkServerTrusted(chain, authType)
                    } catch (e: Exception) {
                        customTrustManager.checkServerTrusted(chain, authType)
                    }
                }

                override fun getAcceptedIssuers(): Array<X509Certificate> {
                    return defaultTrustManager.acceptedIssuers + customTrustManager.acceptedIssuers
                }
            }

            // Create an SSLContext with the composite TrustManager
            val sslContext = SSLContext.getInstance("TLS")
            sslContext.init(null, arrayOf(compositeTrustManager), null)

            OkHttpClient.Builder()
                .sslSocketFactory(sslContext.socketFactory, compositeTrustManager)
                .build()
        } catch (e: Exception) {
            e.printStackTrace()
            OkHttpClient()
        }
    }

    private fun createBasicAuthHeader(): String {
        val credentials = "$USERNAME:$PASSWORD"
        val encodedCredentials = Base64.encodeToString(credentials.toByteArray(), Base64.NO_WRAP)
        return "Basic $encodedCredentials"
    }

    private fun authenticatedRequest(fileName: String): Request {
        val url = "$WEBHOOK_BASE_URL?file=$fileName"
        return Request.Builder()
            .url(url)
            .addHeader("Authorization", createBasicAuthHeader())
            .build()
    }

    suspend fun checkForUpdates(currentVersionCode: Int): UpdateCheckResult =
        withContext(Dispatchers.IO) {
            try {
                val request = authenticatedRequest("version.json")
                val response = client.newCall(request).execute()

                if (!response.isSuccessful) {
                    return@withContext UpdateCheckResult.Error("HTTP ${response.code}: ${response.message}")
                }

                val body = response.body.string()
                val json = kotlinx.serialization.json.Json { ignoreUnknownKeys = true }
                val versionInfo = json.decodeFromString<VersionInfo>(body)

                if (versionInfo.versionCode > currentVersionCode) {
                    UpdateCheckResult.UpdateAvailable(versionInfo)
                } else {
                    UpdateCheckResult.NoUpdateAvailable
                }
            } catch (e: Exception) {
                e.printStackTrace()
                UpdateCheckResult.Error(e.message ?: "Unknown error")
            }
        }

    suspend fun downloadApk(onProgress: (Float) -> Unit): File? = withContext(Dispatchers.IO) {
        try {
            val request = authenticatedRequest("app-release.apk")
            val response = client.newCall(request).execute()

            if (!response.isSuccessful) return@withContext null

            val body = response.body
            val contentLength = body.contentLength()
            val apkFile = File(context.cacheDir, "app-release.apk")

            body.byteStream().use { input ->
                apkFile.outputStream().use { output ->
                    val buffer = ByteArray(8192)
                    var bytesRead: Int
                    var totalRead = 0L

                    while (input.read(buffer).also { bytesRead = it } != -1) {
                        output.write(buffer, 0, bytesRead)
                        totalRead += bytesRead
                        if (contentLength > 0) {
                            onProgress(totalRead.toFloat() / contentLength)
                        }
                    }
                }
            }

            onProgress(1f)
            apkFile
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun triggerInstallation(file: File) {
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "application/vnd.android.package-archive")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    }
}
