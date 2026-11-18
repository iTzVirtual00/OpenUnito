package me.itzvirtual.openunito.ui.feature_settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.LogoDev
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.AndroidUiModes.UI_MODE_NIGHT_YES
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import me.itzvirtual.openunito.resources.Res
import me.itzvirtual.openunito.resources.dev_tab_title
import me.itzvirtual.openunito.resources.settings_customization
import me.itzvirtual.openunito.ui.components.GradientBackground
import me.itzvirtual.openunito.ui.components.StdSurface
import me.itzvirtual.openunito.ui.components.StdTextIconEntry
import me.itzvirtual.openunito.ui.feature_settings.UpdateCard
import me.itzvirtual.openunito.ui.shared.rememberRestartApp
import me.itzvirtual.openunito.ui.themes.LocalCustomColorsPalette
import me.itzvirtual.openunito.ui.themes.OpenUnitoTheme
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun SettingsScreen(
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel = koinViewModel(),
    onClickAppearance: () -> Unit,
    onClickLanguage: () -> Unit,
    onClickDev: () -> Unit,
    onBack: () -> Unit,
) {
    val state by viewModel.screenState.collectAsStateWithLifecycle()
    var showDevCount by remember { mutableStateOf(0) }
    val showDev = state.showDev

    // I can do WHAT??
    val restartApp = rememberRestartApp()
    val doLogout = { viewModel.clearToken { viewModel.purgeAllCaches { restartApp() } } }


    SettingsScreenContent(
        modifier = modifier,
        onClickAppearance = onClickAppearance,
        onClickLanguage = onClickLanguage,
        onClickLogout = doLogout,
        showDev = showDev,
        onCLickUpdateCard = {
            when {
                (showDev ?: false) -> {}
                showDevCount >= 6 -> {// Triggers on 7th click
                    viewModel.setShowDev(true)
                }

                else -> {
                    showDevCount++
                }
            }
        },
        onClickDev = onClickDev,
        onBack = onBack,
    )
}

@Composable
fun SettingsScreenContent(
    modifier: Modifier = Modifier,
    onClickAppearance: () -> Unit = {},
    onClickLanguage: () -> Unit = {},
    onClickLogout: () -> Unit = {},
    showDev: Boolean? = null,
    onCLickUpdateCard: () -> Unit = {},
    onClickDev: () -> Unit = {},
    onBack: () -> Unit = {},
) {
    val spacing = 12.dp
    val insetPadding = 12.dp

    Scaffold(
        modifier = modifier,
        contentWindowInsets = WindowInsets(
            insetPadding,
            insetPadding,
            insetPadding,
            insetPadding,
        ),
        topBar = {
            GradientBackground(
                colors = listOf(
                    MaterialTheme.colorScheme.onPrimary,
                    LocalCustomColorsPalette.current.onSettingsColor
                ),
            ) {
                UpdateCard(
                    modifier = Modifier
                        .padding(horizontal = 18.dp)
                        .padding(top = 18.dp,bottom = 18.dp)
                        .statusBarsPadding(),
                    onClick = onCLickUpdateCard
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
        ) {


            Spacer(
                modifier = Modifier.height(spacing)
            )

            SettingsEntries(
                showDev = showDev,
                onClickAppearance = onClickAppearance,
                onClickLanguage = onClickLanguage,
                onClickLogout = onClickLogout,
                onClickDev = onClickDev,
            )
        }
    }
}

@Composable
fun SettingsEntries(
    modifier: Modifier = Modifier,
    horPadding: Dp = 14.dp,
    spacing: Dp = 12.dp,
    showDev: Boolean?,
    onClickAppearance: () -> Unit = {},
    onClickLanguage: () -> Unit = {},
    onClickLogout: () -> Unit = {},
    onClickDev: () -> Unit = {},
) {
    StdSurface {
        Column {
            StdTextIconEntry(
                modifier = Modifier
                    .padding(horizontal = horPadding, vertical = spacing),
                text = stringResource(Res.string.settings_customization),
                fontWeight = FontWeight.Bold,
                icon = Icons.Default.Palette,
                iconColor = LocalCustomColorsPalette.current.customizationColor,
                onClick = onClickAppearance,
            )

            HorizontalDivider()

            StdTextIconEntry(
                modifier = Modifier
                    .padding(horizontal = horPadding, vertical = spacing),
                text = "Language",
                fontWeight = FontWeight.Bold,
                icon = Icons.Default.Language,
                onClick = onClickLanguage,
                iconColor = LocalCustomColorsPalette.current.languageColor,
            )

            if (showDev ?: false) { // Dev
                HorizontalDivider()

                StdTextIconEntry(
                    modifier = Modifier
                        .padding(horizontal = horPadding, vertical = spacing),
                    text = stringResource(Res.string.dev_tab_title),
                    icon = Icons.Default.LogoDev,
                    onClick = onClickDev,
                    fontWeight = FontWeight.Bold,
                    iconColor = LocalCustomColorsPalette.current.devColor,
                )
            }

            HorizontalDivider()

            Row(
                modifier = Modifier
                    .clickable(
                        onClick = onClickLogout,
                    )
                    .fillMaxWidth()
                    .padding(horizontal = horPadding, vertical = spacing)
            ) {
                val color = MaterialTheme.colorScheme.error
                Icon(
                    modifier = Modifier
                        .offset(2.dp),
                    imageVector = Icons.AutoMirrored.Filled.Logout,
                    contentDescription = null,
                    tint = color,
                )

                Spacer(
                    modifier = Modifier.width(12.dp)
                )


                Text(
                    text = "Log Out",
                    fontWeight = FontWeight.Bold,
                    color = color,
                )
            }
        }
    }
}


@Preview(showBackground = true, uiMode = UI_MODE_NIGHT_YES)
@Composable
fun UpdateCardPreview() {
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