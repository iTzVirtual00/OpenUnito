package me.itzvirtual.openunito.ui.feature_settings

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
import androidx.compose.ui.draw.dropShadow
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
import me.itzvirtual.openunito.resources.Res
import me.itzvirtual.openunito.resources.updater_check_updates_button
import me.itzvirtual.openunito.resources.updater_checking_for_updates
import me.itzvirtual.openunito.resources.updater_checking_updates
import me.itzvirtual.openunito.resources.updater_current_version
import me.itzvirtual.openunito.resources.updater_download_button
import me.itzvirtual.openunito.resources.updater_download_complete
import me.itzvirtual.openunito.resources.updater_download_failed
import me.itzvirtual.openunito.resources.updater_downloading
import me.itzvirtual.openunito.resources.updater_downloading_progress
import me.itzvirtual.openunito.resources.updater_error
import me.itzvirtual.openunito.resources.updater_install_button
import me.itzvirtual.openunito.resources.updater_no_updates_checked
import me.itzvirtual.openunito.resources.updater_status_label
import me.itzvirtual.openunito.resources.updater_title
import me.itzvirtual.openunito.resources.updater_up_to_date
import me.itzvirtual.openunito.resources.updater_update_available
import me.itzvirtual.openunito.ui.components.ShadowDefaults
import me.itzvirtual.openunito.ui.themes.OpenUnitoTheme
import org.jetbrains.compose.resources.stringResource
import java.io.File

enum class buttonType {
    DISABLED,
    DOWNLOAD,
    INSTALL,
}


private sealed interface UpdateStatusState {
    data object Idle : UpdateStatusState
    data object Checking : UpdateStatusState
    data class Available(val versionName: String, val versionCode: Int) : UpdateStatusState
    data object UpToDate : UpdateStatusState
    data class Error(val message: String) : UpdateStatusState
    data object Downloading : UpdateStatusState
    data object DownloadComplete : UpdateStatusState
    data object DownloadFailed : UpdateStatusState
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

    var statusState by remember { mutableStateOf<UpdateStatusState>(UpdateStatusState.Idle) }
    var availableVersion by remember { mutableStateOf<VersionInfo?>(null) }
    var downloadedFile by remember { mutableStateOf<File?>(null) }
    var isChecking by remember { mutableStateOf(false) }
    var isDownloading by remember { mutableStateOf(false) }
    var downloadProgress by remember { mutableFloatStateOf(0f) }

    val updateStatusText = when (val status = statusState) {
        UpdateStatusState.Idle -> stringResource(Res.string.updater_no_updates_checked)
        UpdateStatusState.Checking -> stringResource(Res.string.updater_checking_for_updates)
        is UpdateStatusState.Available -> stringResource(
            Res.string.updater_update_available,
            status.versionName,
            status.versionCode
        )
        UpdateStatusState.UpToDate -> stringResource(Res.string.updater_up_to_date)
        is UpdateStatusState.Error -> stringResource(Res.string.updater_error, status.message)
        UpdateStatusState.Downloading -> stringResource(Res.string.updater_downloading)
        UpdateStatusState.DownloadComplete -> stringResource(Res.string.updater_download_complete)
        UpdateStatusState.DownloadFailed -> stringResource(Res.string.updater_download_failed)
    }

    Column(
        modifier = Modifier
            .clickable(onClick = onClick)
            .then(modifier),
    ) {
        val titleSize = 20.sp
        Text(
            modifier = Modifier
                .dropShadow(
                    RoundedCornerShape(16.dp),
                    shadow = ShadowDefaults.Text
                ),
            text = stringResource(Res.string.updater_title),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            fontSize = titleSize,
            lineHeight = titleSize.times(1.4f)
        )

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = stringResource(
                Res.string.updater_current_version,
                currentVersionName,
                currentVersionCode
            ),
            style = MaterialTheme.typography.bodyMedium
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = stringResource(Res.string.updater_status_label, updateStatusText),
            style = MaterialTheme.typography.bodySmall, // i feel smart
            color = MaterialTheme.colorScheme.outline // i fill smart pt2
        )

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val checkButtonWeight = 0.6f
            val textPadding = 12.dp

            Surface(
                modifier = Modifier
                    .weight(checkButtonWeight)
                    .dropShadow(
                        RoundedCornerShape(16.dp),
                        shadow = ShadowDefaults.Surface
                    ),
                onClick = {
                    isChecking = true
                    statusState = UpdateStatusState.Checking
                    CoroutineScope(Dispatchers.Main).launch {
                        when (val result = updater.checkForUpdates(currentVersionCode)) {
                            is UpdateCheckResult.UpdateAvailable -> {
                                availableVersion = result.versionInfo
                                statusState = UpdateStatusState.Available(
                                    versionName = result.versionInfo.versionName,
                                    versionCode = result.versionInfo.versionCode
                                )
                            }

                            is UpdateCheckResult.NoUpdateAvailable -> {
                                availableVersion = null
                                statusState = UpdateStatusState.UpToDate
                            }

                            is UpdateCheckResult.Error -> {
                                availableVersion = null
                                statusState = UpdateStatusState.Error(result.message)
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

                    Spacer(modifier = Modifier.weight(1f))

                    Text(
                        text = if (isChecking) {
                            stringResource(Res.string.updater_checking_updates)
                        } else {
                            stringResource(Res.string.updater_check_updates_button)
                        },
                        fontWeight = FontWeight.Bold,
                    )

                    Spacer(modifier = Modifier.weight(1.4f))
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            val selectedButton = when {
                downloadedFile != null -> buttonType.INSTALL // Install Button
                availableVersion != null && downloadedFile == null -> buttonType.DOWNLOAD // Download Button
                else -> buttonType.DISABLED // Download Button (Disabled)
            }

            // Download / Install Button
            Surface(
                modifier = Modifier
                    .weight(1 - checkButtonWeight)
                    .dropShadow(
                        RoundedCornerShape(16.dp),
                        shadow = ShadowDefaults.Surface
                    ),
                onClick = {
                    if (selectedButton == buttonType.DOWNLOAD) {
                        isDownloading = true
                        downloadProgress = 0f
                        statusState = UpdateStatusState.Downloading

                        CoroutineScope(Dispatchers.Main).launch {
                            val file = updater.downloadApk { progress ->
                                downloadProgress = progress
                            }
                            downloadedFile = file
                            isDownloading = false
                            if (file != null) {
                                statusState = UpdateStatusState.DownloadComplete
                                updater.triggerInstallation(file)
                            } else {
                                statusState = UpdateStatusState.DownloadFailed
                            }
                        }
                    } else if (selectedButton == buttonType.INSTALL) {
                        downloadedFile?.let { updater.triggerInstallation(it) }
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

                    Spacer(modifier = Modifier.weight(1f))

                    Text(
                        text = when (selectedButton) {
                            buttonType.INSTALL -> stringResource(Res.string.updater_install_button) // downloadedFile != null
                            buttonType.DOWNLOAD if isDownloading -> stringResource(
                                Res.string.updater_downloading_progress,
                                (downloadProgress * 100).toInt()
                            )
                            else -> stringResource(Res.string.updater_download_button)
                        },
                        color = color,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.weight(1.4f))
                }
            }
        }
    }
}


@Preview(showBackground = true, uiMode = UI_MODE_NIGHT_YES)
@Composable
fun UpdateCardPreview() {
    OpenUnitoTheme {
        Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
            UpdateCard(
                modifier = Modifier.padding(innerPadding)
            )
        }
    }
}