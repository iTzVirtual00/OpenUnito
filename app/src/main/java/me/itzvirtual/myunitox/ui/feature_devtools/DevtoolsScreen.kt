package me.itzvirtual.myunitox.ui.feature_devtools

import android.content.res.Configuration
import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import me.itzvirtual.myunitox.BuildConfig
import me.itzvirtual.myunitox.ui.shared.restartApp
import me.itzvirtual.myunitox.ui.themes.MyUnitoXTheme
import java.io.File

//TODO: move it somewhere else
data class DevtoolsActions(
    val setToken: (String) -> Unit = { _ ->
        Log.d("DevtoolsActions", "setToken triggered")
    },
    val copyToken: () -> Unit = {
        Log.d("DevtoolsActions", "onCopyToken triggered")
    },
    val clearToken: () -> Unit = {
        Log.d("DevtoolsActions", "clearToken triggered")
    },
    val purgeAllCaches: () -> Unit = {
        Log.d("DevtoolsActions", "purgeAllCaches triggered")
    }
)

val LocalDevtoolsActions = compositionLocalOf { DevtoolsActions() }

//TODO: add database dump feature
@Composable
fun DevtoolsScreen(
    modifier: Modifier = Modifier,
    viewModel: DevtoolsViewModel = hiltViewModel()
) {
    val context = LocalContext.current

    // Bind ViewModel logic to the Actions data class
    val actions = remember(viewModel, context) {
        DevtoolsActions(
            setToken = { token ->
                viewModel.login(token) {
                    viewModel.purgeAllCaches {
                        restartApp(
                            context
                        )
                    }
                }
            },
            copyToken = { viewModel.copyTokenToClipboard(context) },
            clearToken = { viewModel.clearToken { viewModel.purgeAllCaches { restartApp(context) } } },
            purgeAllCaches = { viewModel.purgeAllCaches { restartApp(context) } }
        )
    }

    CompositionLocalProvider(LocalDevtoolsActions provides actions) {
        MyUnitoXTheme {
            DevtoolsContent(modifier = modifier)
        }
    }
}

@Composable
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
fun DevtoolsPreview() {
    val mockActions = DevtoolsActions()

    MyUnitoXTheme {
        CompositionLocalProvider(LocalDevtoolsActions provides mockActions) {
            DevtoolsContent()
        }
    }
}

@Composable
fun DevtoolsContent(modifier: Modifier = Modifier) {
    Scaffold(modifier = modifier) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(16.dp)
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            UpdateCard()
            PurgeCachesCard()
            AuthDebuggerCard()
        }
    }
}

@Composable
fun PurgeCachesCard() {
    val actions = LocalDevtoolsActions.current
    OutlinedCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Cache Management",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "Purge all cached data (agenda, dashboard, grades, exams, student). The app will restart.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
            ) {
                Button(
                    onClick = { actions.purgeAllCaches() },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor = MaterialTheme.colorScheme.onError
                    )
                ) {
                    Text("Purge All Caches")
                }
            }
        }
    }
}

@Composable
fun UpdateCard() {
    val context = LocalContext.current
    val updater = remember { HTTPUpdater(context) }
    val currentVersionCode = BuildConfig.VERSION_CODE
    val currentVersionName = BuildConfig.VERSION_NAME

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

@Composable
fun AuthDebuggerCard() {
    var tokenInput by remember { mutableStateOf("") }
    val actions = LocalDevtoolsActions.current
    OutlinedCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Authentication Debugger",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )

            OutlinedTextField(
                value = tokenInput,
                onValueChange = { input ->
                    tokenInput = input.filter { it != '\n' && it != '\r' } // really Android?
                },
                label = { Text("setToken") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true, // UI only :(
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Secondary button for copying
                TextButton(onClick = { actions.copyToken() }) {
                    Text("Copy Current")
                }

                Spacer(Modifier.width(8.dp))

                Button(onClick = { actions.setToken(tokenInput) }) {
                    Text("Set Token")
                }

                Spacer(Modifier.width(8.dp))

                Button(
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor = MaterialTheme.colorScheme.onError
                    ),
                    onClick = { actions.clearToken() }) {
                    Text("Clear")
                }
            }
        }
    }

}
