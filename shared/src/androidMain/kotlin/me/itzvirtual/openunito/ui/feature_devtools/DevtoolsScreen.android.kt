package me.itzvirtual.openunito.ui.feature_devtools

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DownloadDone
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.AndroidUiModes.UI_MODE_NIGHT_YES
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import me.itzvirtual.openunito.BuildKonfig
import me.itzvirtual.openunito.ui.themes.OpenUnitoTheme
import java.io.File

enum class buttonType {
    DISABLED,
    DOWNLOAD,
    INSTALL,
}

@Composable
actual fun UpdateCard(
    modifier: Modifier,
    cornerRadius: Dp,
    border: BorderStroke,
    onClick: () -> Unit,
) {
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

    Column(
        modifier = modifier
            .clickable(
                onClick = onClick
            ),
        ) {
        val titleSize = 20.sp
        Text(
            text = "Updater",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            fontSize = titleSize,
            lineHeight = titleSize.times(1.4f)
        )

        Spacer(
            modifier = Modifier.height(10.dp)
        )

        Text(
            text = "Current: $currentVersionName (Code: $currentVersionCode)",
            style = MaterialTheme.typography.bodyMedium
        )

        Spacer(
            modifier = Modifier.height(4.dp)
        )

        Text(
            text = "Status: $updateStatus",
            style = MaterialTheme.typography.bodySmall, // i feel smart
            color = MaterialTheme.colorScheme.outline // i fill smart pt2
        )

        Spacer(
            modifier = Modifier.height(12.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val checkButtonWeight = 0.6f
            val textPadding = 12.dp

            Surface(
                modifier = Modifier.weight(checkButtonWeight),
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
                enabled = !isChecking && !isDownloading,
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.6f),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                shape = RoundedCornerShape(25f),

                ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(textPadding),
                    horizontalArrangement = Arrangement.Start,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = null,
                    )

                    Spacer(
                        modifier = Modifier.weight(1f)
                    )

                    Text(
                        modifier = Modifier,
                        text = if (isChecking) "Checking..." else "Check",
                        fontWeight = FontWeight.Bold,
                    )
                    Spacer(
                        modifier = Modifier.weight(1.4f)
                    )
                }
            }

            Spacer(
                modifier = Modifier.width(8.dp)
            )

            val selectedButton = when {
                downloadedFile != null -> buttonType.INSTALL // Install Button
                availableVersion != null && downloadedFile == null -> buttonType.DOWNLOAD // Download Button
                else -> buttonType.DISABLED // Download Button (Disabled)
            }

            // Download / Install Button
            Surface(
                modifier = Modifier.weight(1 - checkButtonWeight),
                onClick = {
                    if (selectedButton == buttonType.DOWNLOAD) {
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
                    } else if (selectedButton == buttonType.INSTALL) {
                        updater.triggerInstallation(downloadedFile!!)
                    }
                },
                enabled = when (selectedButton) {
                    buttonType.INSTALL -> true
                    buttonType.DOWNLOAD -> !isDownloading
                    else -> false
                },
                color = when (selectedButton) {
                    buttonType.DISABLED -> MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)
                    else -> MaterialTheme.colorScheme.surface
                },
                border = BorderStroke(
                    2.dp,
                    color = when (selectedButton) {
                        buttonType.DISABLED -> MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                        else -> MaterialTheme.colorScheme.primary
                    }
                ),
                shape = RoundedCornerShape(25f),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(textPadding),
                    horizontalArrangement = Arrangement.Start,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    val color = when (selectedButton) {
                        buttonType.DISABLED -> Color.White.copy(alpha = 0.5f)
                        else -> Color.White
                    }

                    Icon(
                        imageVector = when (selectedButton) {
                            buttonType.DISABLED -> Icons.Default.DownloadDone
                            else -> Icons.Default.FileDownload
                        },
                        contentDescription = null,
                        tint = color,
                    )

                    Spacer(
                        modifier = Modifier.weight(1f)
                    )

                    Text(
                        text = when (selectedButton) {
                            buttonType.INSTALL -> "Install" // downloadedFile != null
                            buttonType.DOWNLOAD if isDownloading -> "Downloading ${(downloadProgress * 100).toInt()}%"
                            else -> "Download"
                        },
                        color = color,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(
                        modifier = Modifier.weight(1.4f)
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true, uiMode = UI_MODE_NIGHT_YES)
@Composable
fun SettingsScreenPreview() {
    OpenUnitoTheme {
        Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
            UpdateCard(
                modifier = Modifier.padding(innerPadding)
            )
        }
    }
}