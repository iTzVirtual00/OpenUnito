package me.itzvirtual.openunito.ui.feature_settings

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File


@Serializable
data class GitHubRelease(
    @SerialName("tag_name")
    val tagName: String,
    @SerialName("assets")
    val assets: List<GitHubAsset> = emptyList()
)

@Serializable
data class GitHubAsset(
    @SerialName("name")
    val name: String,
    @SerialName("browser_download_url")
    val browserDownloadUrl: String
)

data class VersionInfo(
    val versionCode: Int,
    val versionName: String,
    val downloadUrl: String
)

sealed class UpdateCheckResult {
    data class UpdateAvailable(val versionInfo: VersionInfo) : UpdateCheckResult()
    data object NoUpdateAvailable : UpdateCheckResult()
    data class Error(val message: String) : UpdateCheckResult()
}

class HTTPUpdater(private val context: Context) {
    companion object {
        private const val GITHUB_LATEST_RELEASE_URL =
            "https://api.github.com/repos/iTzVirtual00/OpenUnito/releases/latest"
    }

    private val client = OkHttpClient()
    private val json = Json { ignoreUnknownKeys = true }
    private var lastFetchedVersion: VersionInfo? = null

    // e.g. v1.0.0+50
    private fun parseTagName(tagName: String): Pair<String, Int>? {
        val parts = tagName.split("+")
        if (parts.size != 2) return null

        val versionName = parts[0].removePrefix("v")
        val versionCode = parts[1].toIntOrNull() ?: return null
        return versionName to versionCode
    }

    suspend fun checkForUpdates(currentVersionCode: Int): UpdateCheckResult =
        withContext(Dispatchers.IO) {
            try {
                // https://docs.github.com/en/rest/about-the-rest-api/api-versions?apiVersion=2022-11-28#specifying-an-api-version
                val request = Request.Builder()
                    .url(GITHUB_LATEST_RELEASE_URL)
                    .addHeader("Accept", "application/json")
                    .addHeader("X-GitHub-Api-Version", "2022-11-28")
                    .build()
                val response = client.newCall(request).execute()

                if (!response.isSuccessful) {
                    return@withContext UpdateCheckResult.Error(
                        "HTTP ${response.code}: ${response.message}"
                    )
                }

                val bodyString = response.body.string()

                val release = json.decodeFromString<GitHubRelease>(bodyString)

                val (versionName, versionCode) = parseTagName(release.tagName)
                    ?: return@withContext UpdateCheckResult.Error(
                        "Invalid tag format: ${release.tagName}"
                    )

                if (versionCode <= currentVersionCode) {
                    return@withContext UpdateCheckResult.NoUpdateAvailable
                }

                val expectedAssetName = "OpenUnito-${release.tagName}.apk"
                val apkAsset = release.assets.firstOrNull { it.name == expectedAssetName }
                    ?: return@withContext UpdateCheckResult.Error(
                        "Asset '$expectedAssetName' not found in release"
                    )

                val versionInfo = VersionInfo(
                    versionCode = versionCode,
                    versionName = versionName,
                    downloadUrl = apkAsset.browserDownloadUrl
                )
                lastFetchedVersion = versionInfo

                UpdateCheckResult.UpdateAvailable(versionInfo)
            } catch (e: Exception) {
                e.printStackTrace()
                UpdateCheckResult.Error(e.message ?: "Unknown error")
            }
        }

    suspend fun downloadApk(
        onProgress: (Float) -> Unit
    ): File? = withContext(Dispatchers.IO) {
        val downloadUrl = lastFetchedVersion?.downloadUrl ?: return@withContext null
        try {
            val request = Request.Builder()
                .url(downloadUrl)
                .build()

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