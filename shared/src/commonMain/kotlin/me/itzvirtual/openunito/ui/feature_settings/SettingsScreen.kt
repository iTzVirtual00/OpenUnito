package me.itzvirtual.openunito.ui.feature_settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.LogoDev
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.AndroidUiModes.UI_MODE_NIGHT_YES
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import me.itzvirtual.openunito.resources.Res
import me.itzvirtual.openunito.resources.dev_tab_title
import me.itzvirtual.openunito.resources.settings_tab_title
import me.itzvirtual.openunito.ui.components.StdSurface
import me.itzvirtual.openunito.ui.components.StdTextIconEntry
import me.itzvirtual.openunito.ui.feature_devtools.UpdateCard
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
) {
    SettingsScreenContent(
        modifier = modifier,
        onClickAppearance = onClickAppearance,
        onClickLanguage = onClickLanguage,
        onClickDev = onClickDev,
    )
}

@Composable
fun SettingsScreenContent(
    modifier: Modifier = Modifier,
    onClickAppearance: () -> Unit = {},
    onClickLanguage: () -> Unit = {},
    onClickDev: () -> Unit = {},
) {
    val spacing = 12.dp

    var showDev = false



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
            UpdateCard().toString()

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
                        text = "Aspetto",
                        fontWeight = FontWeight.Bold,
                        icon = Icons.Default.Palette,
                        iconColor = Color(246f / 256f,85f / 256f,85f / 256f),
                        onClick = onClickAppearance,
                    )

                    HorizontalDivider()

                    StdTextIconEntry(
                        modifier = Modifier
                            .padding(vertical = spacing),
                        text = "Language",
                        fontWeight = FontWeight.Bold,
                        icon = Icons.Default.Language,
                        onClick = onClickAppearance,
                        iconColor = Color(0.624f, 0.42f, 0.953f, 1.0f),
                    )

                    if (true) { // Dev
                        HorizontalDivider()

                        StdTextIconEntry(
                            modifier = Modifier
                                .padding(vertical = spacing),
                            text = stringResource(Res.string.dev_tab_title),
                            icon = Icons.Default.LogoDev,
                            onClick = onClickDev,
                            fontWeight = FontWeight.Bold,
                            iconColor = Color(0.373f, 0.812f, 0.914f, 1.0f)
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