package me.itzvirtual.openunito.ui.feature_settings.dev_screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.LogoDev
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.AndroidUiModes.UI_MODE_NIGHT_YES
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.launch
import me.itzvirtual.openunito.domain.PlatformClipboardManager
import me.itzvirtual.openunito.resources.Res
import me.itzvirtual.openunito.resources.debug_entry
import me.itzvirtual.openunito.resources.dev_tab_title
import me.itzvirtual.openunito.resources.show_dev_button
import me.itzvirtual.openunito.ui.components.StdSurface
import me.itzvirtual.openunito.ui.components.StdSwitch
import me.itzvirtual.openunito.ui.shared.rememberRestartApp
import me.itzvirtual.openunito.ui.themes.OpenUnitoTheme
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun DevScreen(
    modifier: Modifier = Modifier,
    viewModel: DevScreenViewModel = koinViewModel(),
    onBack: () -> Unit,
) {
    val state by viewModel.screenState.collectAsStateWithLifecycle()

    val restartApp = rememberRestartApp()
    val clipboard = koinInject<PlatformClipboardManager>()
    val scope = rememberCoroutineScope()

    DevScreenContent(
        modifier = modifier,
        setToken = { token ->
            viewModel.login(token) {
                viewModel.purgeAllCaches {
                    restartApp()
                }
            }
        },
        copyToken = {
            viewModel.copyTokenToClipboard { token ->
                scope.launch { clipboard.setText(token) }
            }
        },
        onPurgeCaches = {
            viewModel.purgeAllCaches { restartApp() }
        },
        debugValue = state.debug,
        onChangeDebug = { value ->
            viewModel.setDebug(value)
        },
        showDevValue = state.showDev,
        onChangeShowDev = { value ->
            viewModel.setShowDev(value)
        },
        onBack = onBack,
    )
}

@Composable
fun DevScreenContent(
    modifier: Modifier = Modifier,
    setToken: (String) -> Unit = {},
    copyToken: () -> Unit = {},
    onPurgeCaches: () -> Unit = {},
    debugValue: Boolean,
    onChangeDebug: (Boolean) -> Unit = {},
    showDevValue: Boolean,
    onChangeShowDev: (Boolean) -> Unit = {},
    onBack: () -> Unit = {},
) {
    val insetPadding = 12.dp

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(Res.string.dev_tab_title),
                        fontWeight = FontWeight.Bold,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        },
        contentWindowInsets = WindowInsets(
            insetPadding,
            0.dp,
            insetPadding,
            insetPadding
        )
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            AuthDebuggerCard(
                onSetToken = setToken,
                onCopyToken = copyToken,
            )

            Spacer(
                modifier = Modifier.height(10.dp)
            )

            PurgeCachesCard(
                onPurgeCaches = onPurgeCaches
            )

            Spacer(
                modifier = Modifier.height(10.dp)
            )

            DevContentCard(
                debugValue = debugValue,
                onChangeDebug = onChangeDebug,
                showDevValue = showDevValue,
                onChangeShowDev = onChangeShowDev,
            )
        }
    }
}

@Composable
fun AuthDebuggerCard(
    onSetToken: (String) -> Unit,
    onCopyToken: () -> Unit,
) {
    var tokenInput by remember { mutableStateOf("") }

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
                Row(
                    modifier = Modifier.weight(0.5f),
                    horizontalArrangement = Arrangement.Center,
                ) {
                    TextButton(
                        onClick = { onCopyToken() }
                    ) {
                        Text("Copy Current")
                    }
                }

                Row(
                    modifier = Modifier.weight(0.5f),
                    horizontalArrangement = Arrangement.Center,
                ) {
                    Button(
                        onClick = { onSetToken(tokenInput) }
                    ) {
                        Text("Set Token")
                    }
                }
            }
        }
    }
}

@Composable
fun PurgeCachesCard(
    onPurgeCaches: () -> Unit = {},
) {
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
                    onClick = onPurgeCaches,
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
fun DevContentCard(
    modifier: Modifier = Modifier,
    debugValue: Boolean,
    onChangeDebug: (Boolean) -> Unit = {},
    showDevValue: Boolean,
    onChangeShowDev: (Boolean) -> Unit = {},
) {
    StdSurface(
        modifier = modifier,
    ) {
        Column() {
            StdSwitch(
                modifier = Modifier
                    .padding(horizontal = 16.dp, 2.dp)
                    .fillMaxWidth(),
                value = debugValue,
                onChanged = onChangeDebug,
                icon = Icons.Default.BugReport,
                iconColor = Color(0.976f, 0.733f, 0.275f, 1.0f),
                text = stringResource(Res.string.debug_entry),
                fontWeight = FontWeight.Bold
            )

            HorizontalDivider()

            StdSwitch(
                modifier = Modifier
                    .padding(horizontal = 16.dp, 2.dp)
                    .fillMaxWidth(),
                value = showDevValue,
                onChanged = onChangeShowDev,
                icon = Icons.Default.LogoDev,
                iconColor = MaterialTheme.colorScheme.error,
                text = stringResource(Res.string.show_dev_button),
                textColor = MaterialTheme.colorScheme.error,
                fontWeight = FontWeight.Bold,
                switchColors = SwitchDefaults.colors().copy(
                    checkedTrackColor = MaterialTheme.colorScheme.error,
                    checkedThumbColor = MaterialTheme.colorScheme.onError,
                )
            )
        }
    }
}

@Preview(showBackground = true, uiMode = UI_MODE_NIGHT_YES)
@Composable
fun DevScreenPreview() {
    OpenUnitoTheme {
        Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
            DevScreenContent(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                debugValue = false,
                showDevValue = false,
            )
        }
    }
}