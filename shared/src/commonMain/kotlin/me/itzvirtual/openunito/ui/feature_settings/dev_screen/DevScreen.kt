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
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
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
import me.itzvirtual.openunito.resources.dev_tab_title
import me.itzvirtual.openunito.ui.components.StdSurface
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
        onSetDebug = { value ->
            viewModel.setDebug(value)
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
    onSetDebug: (Boolean) -> Unit = {},
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
                onSetDebug = onSetDebug,

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
    onSetDebug: (Boolean) -> Unit = {},
) {
    StdSurface {
        DebugSwitchEntry(
            modifier = modifier
                .padding(horizontal = 16.dp, 2.dp)
                .fillMaxWidth(),
            debugValue = debugValue,
            onChecked = onSetDebug,
        )
    }
}

@Composable
fun DebugSwitchEntry(
    modifier: Modifier = Modifier,
    debugValue: Boolean,
    onChecked: (Boolean) -> Unit = {},
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            modifier = Modifier
        ) {
            Icon(
                modifier = Modifier,
                imageVector = Icons.Default.BugReport,
                contentDescription = null,
                tint = Color(0.976f, 0.733f, 0.275f, 1.0f),
            )
            Spacer(
                modifier = Modifier
                    .width(12.dp),
            )
            Text(
                text = "Debug",
                fontWeight = FontWeight.Bold
            )
        }
        Switch(
            modifier = Modifier
                .padding(vertical = 2.dp),
            checked = debugValue,
            onCheckedChange = { state -> onChecked(state) }
        )
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
            )
        }
    }
}