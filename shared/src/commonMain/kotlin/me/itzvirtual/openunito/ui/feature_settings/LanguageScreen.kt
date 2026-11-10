package me.itzvirtual.openunito.ui.feature_settings

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.dropShadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.AndroidUiModes.UI_MODE_NIGHT_YES
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import me.itzvirtual.openunito.data.localization.SupportedLanguage
import me.itzvirtual.openunito.data.localization.SupportedLocales
import me.itzvirtual.openunito.data.localization.SupportedRegion
import me.itzvirtual.openunito.data.localization.withDateFormatter
import me.itzvirtual.openunito.domain.getToday
import me.itzvirtual.openunito.resources.Res
import me.itzvirtual.openunito.resources.good_morning
import me.itzvirtual.openunito.resources.language_screen_title
import me.itzvirtual.openunito.ui.LocalResolvedLocale
import me.itzvirtual.openunito.ui.components.GradientBackground
import me.itzvirtual.openunito.ui.components.ShadowDefaults
import me.itzvirtual.openunito.ui.components.StdDropdownEntry
import me.itzvirtual.openunito.ui.components.StdSurface
import me.itzvirtual.openunito.ui.themes.LocalCustomColorsPalette
import me.itzvirtual.openunito.ui.themes.OpenUnitoTheme
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import kotlin.time.Clock

@Composable
fun LanguageScreen(
    modifier: Modifier = Modifier,
    onBack: () -> Unit,
    viewModel: SettingsViewModel = koinViewModel(),
) {
    val coroutineScope = rememberCoroutineScope()
    LanguageScreenContent(
        modifier = modifier,
        onBack = onBack,
        onSelectLanguage = {
            coroutineScope.launch {
                viewModel.setLanguage(it)
            }
        },
        onSelectRegion = {
            coroutineScope.launch {
                viewModel.setRegion(it)
            }
        }
    )
}

@Composable
fun LanguageScreenContent(
    modifier: Modifier = Modifier,
    onBack: () -> Unit = {},
    onSelectLanguage: (SupportedLanguage) -> Unit = {},
    onSelectRegion: (SupportedRegion) -> Unit = {}
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            GradientBackground(
                colors = listOf(
                    MaterialTheme.colorScheme.onPrimary,
                    LocalCustomColorsPalette.current.onSettingsColor
                ),
            ) {
                TopAppBar(
                    modifier = Modifier
                        .dropShadow(
                            RoundedCornerShape(16.dp),
                            shadow = ShadowDefaults.Text
                        ),
                    title = {
                        Text(
                            text = stringResource(Res.string.language_screen_title),
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
                    },
                    colors = TopAppBarDefaults.topAppBarColors().copy(
                        containerColor = Color.Transparent,
                    )
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 8.dp)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                Spacer(
                    modifier = Modifier.height(12.dp)
                )

                LanguageUserPreview()

                Spacer(
                    modifier = Modifier.height(12.dp)
                )

                val resolvedLocale = LocalResolvedLocale.current

                val rawLanguages = SupportedLocales.languages
                var selectedLanguage by remember { mutableStateOf<Int?>(null) } // TODO: Maybe get this as param

                val languageList = remember(rawLanguages) {
                    buildList {
                        rawLanguages.forEachIndexed { index, el ->
                            if (el.tag == resolvedLocale.langCode) {
                                selectedLanguage = index
                            }

                            add(el.displayName)
                        }
                    }
                }

                val rawRegions = SupportedLocales.regions
                var selectedRegion by remember { mutableStateOf<Int?>(null) }

                val regionsList = remember(rawRegions) {
                    buildList {
                        rawRegions.forEachIndexed { index, el ->
                            if (el.regionCode == resolvedLocale.regionCode) {
                                selectedRegion = index
                            }

                            add(el.displayName)
                        }
                    }
                }

                StdSurface {
                    Column {
                        StdDropdownEntry(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal= 16.dp, vertical= 8.dp),
                            text = "Language", // TODO: Add translation and change names
                            fontWeight = FontWeight.Bold,
                            elementList = languageList,
                            selectedElement = selectedLanguage,
                            onChangeSelected = { index ->
                                selectedLanguage = index
                                onSelectLanguage(rawLanguages[index])
                            },
                            buttonSize = 180.dp,
                        )

                        HorizontalDivider()

                        StdDropdownEntry(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal= 16.dp, vertical= 8.dp),
                            text = "Date Format", // TODO: Add translation and change names
                            fontWeight = FontWeight.Bold,
                            elementList = regionsList,
                            selectedElement = selectedRegion,
                            onChangeSelected = { index ->
                                selectedRegion = index
                                onSelectRegion(rawRegions[index])
                            },
                            buttonSize = 180.dp,
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun LanguageUserPreview(
    modifier: Modifier = Modifier
) {
    StdSurface(
        modifier = modifier,
        color = MaterialTheme.colorScheme.background,
        border = BorderStroke(2.dp, LocalCustomColorsPalette.current.settingsColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp,),
        ) {
            Text(
                modifier = Modifier.padding(start = 8.dp),
                text = "Preview", // TODO: translation
                fontWeight = FontWeight.Bold,
                fontSize = 22.sp,
            )

            Spacer(
                modifier = Modifier.height(12.dp)
            )

            StdSurface(
                modifier = Modifier
                    .fillMaxWidth(),
                color = MaterialTheme.colorScheme.surfaceColorAtElevation(2.dp),
            ) {
                Column(
                    modifier = Modifier
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                ) {
                    Text(
                        text = stringResource(Res.string.good_morning),
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(
                        modifier = Modifier.height(8.dp)
                    )

                    val today = getToday()
                    withDateFormatter {
                        Text(today.formatFriendlyDayMonth())
                        Text(today.formatFriendlyDayOfWeek())
                        Text(today.formatFriendlyFullDate())
                        Text(Clock.System.now().formatDate())
                        Text(Clock.System.now().formatFriendlyTime())
                        Text(Clock.System.now().formatFullMonth())
                        Text(Clock.System.now().formatMediumDate())
                    }
                }
            }
        }
    }
}

@Composable
fun LanguageOptionRow(
    displayName: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    ListItem(
        headlineContent = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = displayName.ifBlank { "Follow System" },
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        },
        trailingContent = {
            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        },
        modifier = Modifier.clickable(onClick = onClick)
    )
}

@Preview(showBackground = true, uiMode = UI_MODE_NIGHT_YES)
@Composable
fun LanguageScreenPreview() {
    OpenUnitoTheme {
        Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
            LanguageScreenContent(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            )
        }
    }
}