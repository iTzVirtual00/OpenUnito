package me.itzvirtual.openunito.ui.feature_settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.LogoDev
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.AndroidUiModes.UI_MODE_NIGHT_YES
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import me.itzvirtual.openunito.domain.PlatformClipboardManager
import me.itzvirtual.openunito.resources.Res
import me.itzvirtual.openunito.resources.dev_tab_title
import me.itzvirtual.openunito.resources.settings_customization
import me.itzvirtual.openunito.resources.settings_tab_title
import me.itzvirtual.openunito.ui.components.StdSurface
import me.itzvirtual.openunito.ui.components.StdTextIconEntry
import me.itzvirtual.openunito.ui.feature_devtools.DevtoolsActions
import me.itzvirtual.openunito.ui.feature_devtools.DevtoolsViewModel
import me.itzvirtual.openunito.ui.feature_devtools.UpdateCard
import me.itzvirtual.openunito.ui.shared.rememberRestartApp
import me.itzvirtual.openunito.ui.themes.OpenUnitoTheme
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun SettingsScreen(
    modifier: Modifier = Modifier,
    viewModel: DevtoolsViewModel = koinViewModel(), // TODO: Mario, move this to SettingsViewModel
    onClickAppearance: () -> Unit,
    onClickLanguage: () -> Unit,
    onClickDev: () -> Unit,
) {
    // I can do WHAT??
    val restartApp = rememberRestartApp()
    val clipboard = koinInject<PlatformClipboardManager>()
    val scope = rememberCoroutineScope()

    // Bind ViewModel logic to the Actions data class
    val actions = remember(viewModel, restartApp, clipboard) {
        DevtoolsActions(
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
            clearToken = { viewModel.clearToken { viewModel.purgeAllCaches { restartApp() } } },
            purgeAllCaches = { viewModel.purgeAllCaches { restartApp() } }
        )
    }


    SettingsScreenContent(
        modifier = modifier,
        onClickAppearance = onClickAppearance,
        onClickLanguage = onClickLanguage,
        onClickLogout = actions.clearToken,
        onClickDev = onClickDev,
    )
}

@Composable
fun SettingsScreenContent(
    modifier: Modifier = Modifier,
    onClickAppearance: () -> Unit = {},
    onClickLanguage: () -> Unit = {},
    onClickLogout: () -> Unit = {},
    onClickDev: () -> Unit = {},
) {
    var showDevCount by remember { mutableStateOf(0) }
    val spacing = 12.dp

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(Res.string.settings_tab_title),
                        fontWeight = FontWeight.Bold,
                    )
                },
                navigationIcon = {
                    Icon(
                        modifier = Modifier
                            .padding(6.dp),
                        imageVector = Icons.Default.Settings,
                        contentDescription = null,
                    )
                }
            )
        }
    ) { paddingValues ->
        Column (
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 8.dp)
        ) {
            UpdateCard(
                onClick = {
                    if (showDevCount < 7) {
                        showDevCount++
                    }
                }
            )

            Spacer(
                modifier = Modifier.height(spacing)
            )

            StdSurface {
                Column (
                    modifier = Modifier
                        .padding(horizontal = 14.dp)
                ) {
                    StdTextIconEntry(
                        modifier = Modifier
                            .padding(vertical = spacing),
                        text = stringResource(Res.string.settings_customization),
                        fontWeight = FontWeight.Bold,
                        icon = Icons.Default.Palette,
                        iconColor = Color(1.0f, 0.388f, 0.239f, 1.0f),
                        onClick = onClickAppearance,
                    )

                    HorizontalDivider()

                    StdTextIconEntry(
                        modifier = Modifier
                            .padding(vertical = spacing),
                        text = "Language",
                        fontWeight = FontWeight.Bold,
                        icon = Icons.Default.Language,
                        onClick = onClickLanguage,
                        iconColor = Color(0.624f, 0.42f, 0.953f, 1.0f),
                    )

                    HorizontalDivider()

                    StdTextIconEntry(
                        modifier = Modifier
                            .padding(vertical = spacing),
                        text = "LogOut",
                        fontWeight = FontWeight.Bold,
                        textColor = MaterialTheme.colorScheme.error,
                        icon = Icons.AutoMirrored.Filled.Logout,
                        onClick = onClickLogout,
                        iconColor = MaterialTheme.colorScheme.error,
                    )

                    if (showDevCount >= 7) { // Dev
                        HorizontalDivider()

                        StdTextIconEntry(
                            modifier = Modifier
                                .padding(vertical = spacing),
                            text = stringResource(Res.string.dev_tab_title),
                            icon = Icons.Default.LogoDev,
                            onClick = onClickDev,
                            fontWeight = FontWeight.Bold,
                            iconColor = Color(0.235f, 0.957f, 0.576f, 1.0f)
                        )
                    }
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
            SettingsScreenContent(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            )
        }
    }
}