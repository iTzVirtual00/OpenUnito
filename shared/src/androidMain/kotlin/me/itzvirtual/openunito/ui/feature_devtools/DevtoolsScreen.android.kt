package me.itzvirtual.openunito.ui.feature_devtools

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import me.itzvirtual.openunito.BuildKonfig
import java.io.File

@Composable
actual fun UpdateCard() {
    val context = LocalContext.current
    val updater = remember { HTTPUpdater(context) }
    
    val currentVersionCode = BuildKonfig.VERSION_CODE
    val currentVersionName = BuildKonfig.VERSION_NAME

    var updateStatus by remember { mutableStateOf("No updates checked") }
    var availableVersion by remember { mutableStateOf<VersionInfo?>(null) }
    var downloadedFile by remember { mutableStateOf<File?>(null) }
    var isChecking by remember { mutableStateOf(false) }
    var isDownloading by remember { mutableStateOf(false) }
    var downloadProgress by remember { mutableFloatStateOf(0f) }

    OutlinedCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Updater",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )

            Text(
                text = "Current: $currentVersionName (Code: $currentVersionCode)",
                style = MaterialTheme.typography.bodyMedium
            )

            Text(
                text = "Status: $updateStatus",
                style = MaterialTheme.typography.bodySmall, // i feel smart
                color = MaterialTheme.colorScheme.onSurfaceVariant // i fill smart pt2
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = {
                        isChecking = true
                        updateStatus = "Checking for updates..."
                        CoroutineScope(Dispatchers.Main).launch {
                            when (val result = updater.checkForUpdates(currentVersionCode)) {
                                is UpdateCheckResult.UpdateAvailable -> {
                                    availableVersion = result.versionInfo
                                    updateStatus =
                                        "Update available: ${result.versionInfo.versionName} (Code: ${result.versionInfo.versionCode})"
                                }

                                is UpdateCheckResult.NoUpdateAvailable -> {
                                    availableVersion = null
                                    updateStatus = "You're up to date!"
                                }

                                is UpdateCheckResult.Error -> {
                                    availableVersion = null
                                    updateStatus = "Error: ${result.message}"
                                }
                            }
                            isChecking = false
                        }
                    },
                    enabled = !isChecking && !isDownloading
                ) {
                    Text(if (isChecking) "Checking..." else "Check")
                }

                // Download Button
                if (availableVersion != null && downloadedFile == null) {
                    Spacer(Modifier.width(8.dp))
                    Button(
                        onClick = {
                            isDownloading = true
                            downloadProgress = 0f
                            updateStatus = "Downloading..."
                            CoroutineScope(Dispatchers.Main).launch {
                                downloadedFile = updater.downloadApk { progress ->
                                    downloadProgress = progress
                                }
                                isDownloading = false
                                updateStatus = if (downloadedFile != null) {
                                    "Download complete! Ready to install"
                                } else {
                                    "Download failed"
                                }
                                updater.triggerInstallation(downloadedFile!!)
                            }
                        },
                        enabled = !isDownloading
                    ) {
                        Text(
                            if (isDownloading) "Downloading ${(downloadProgress * 100).toInt()}%"
                            else "Download"
                        )
                    }
                }

                // Install Button
                if (downloadedFile != null) {
                    Spacer(Modifier.width(8.dp))
                    Button(
                        onClick = { updater.triggerInstallation(downloadedFile!!) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        )
                    ) {
                        Text("Install")
                    }
                }
            }
        }
    }
}