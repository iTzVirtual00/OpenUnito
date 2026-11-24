package me.itzvirtual.openunito.ui.feature_settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.LogoDev
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.AndroidUiModes.UI_MODE_NIGHT_YES
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.ktor.client.plugins.convertLongTimeoutToIntWithInfiniteAsZero
import io.ktor.websocket.Frame
import me.itzvirtual.openunito.resources.Res
import me.itzvirtual.openunito.resources.dev_tab_title
import me.itzvirtual.openunito.resources.github_invertocat_black
import me.itzvirtual.openunito.resources.language_screen_title
import me.itzvirtual.openunito.resources.link_to_repo_button
import me.itzvirtual.openunito.resources.log_out_label
import me.itzvirtual.openunito.resources.playing_cards_35dp_E3E3E3_FILL1_wght400_GRAD0_opsz40
import me.itzvirtual.openunito.resources.settings_customization
import me.itzvirtual.openunito.resources.unofficial_disclaimer
import me.itzvirtual.openunito.ui.components.GradientBackground
import me.itzvirtual.openunito.ui.components.StdSurface
import me.itzvirtual.openunito.ui.components.StdTextIconEntry
import me.itzvirtual.openunito.ui.shared.rememberRestartApp
import me.itzvirtual.openunito.ui.themes.LocalCustomColorsPalette
import me.itzvirtual.openunito.ui.themes.OpenUnitoTheme
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.resources.vectorResource
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
    val doLogout = { viewModel.logout { viewModel.purgeAllCaches { restartApp() } } }


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

            Spacer(
                modifier = Modifier.weight(1f)
            )

            GitHubLinkText(
                modifier = Modifier
                    .padding(vertical = 8.dp)
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
                text = stringResource(Res.string.language_screen_title),
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
                    text = stringResource(Res.string.log_out_label),
                    fontWeight = FontWeight.Bold,
                    color = color,
                )
            }
        }
    }
}

@Composable
fun GitHubLinkText(
    modifier: Modifier = Modifier,
) {
    val uriHandler = LocalUriHandler.current
    val linkString = "https://github.com/iTzVirtual00/OpenUnito"

    val textColor = MaterialTheme.colorScheme.outline
    val textStyle = MaterialTheme.typography.labelMedium.copy(
        color = textColor
    )

    Row(
        modifier = modifier,
    ) {
        Text(
            text = stringResource(Res.string.unofficial_disclaimer),
            style = textStyle
        )

        Spacer(
            modifier = Modifier
                .width(textStyle.lineHeight.value.dp.times(0.9f)),
        )

        Surface(
            modifier = Modifier
                .clickable(
                    onClick = {
                        uriHandler.openUri(linkString)
                    }
                ),
            color = Color.Transparent
        ) {
            Row {
                Icon(
                    modifier = Modifier
                        .size(textStyle.lineHeight.value.dp.times(0.9f)),
                    imageVector = vectorResource(Res.drawable.github_invertocat_black),
                    contentDescription = null,
                    tint = textColor,
                )

                Spacer(
                    modifier = Modifier
                        .width(textStyle.lineHeight.value.dp.times(0.3f)),
                )

                Text(
                    text = stringResource(Res.string.link_to_repo_button),
                    style = textStyle
                )
            }
        }
    }
}

@Preview(showBackground = true, uiMode = UI_MODE_NIGHT_YES)
@Composable
fun SettingsPreview() {
    OpenUnitoTheme {
        Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
            GitHubLinkText()
            SettingsScreenContent(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            )
        }
    }
}