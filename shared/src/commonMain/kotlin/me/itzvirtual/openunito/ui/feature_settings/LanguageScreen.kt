package me.itzvirtual.openunito.ui.feature_settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.AndroidUiModes.UI_MODE_NIGHT_YES
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.launch
import me.itzvirtual.openunito.data.config.AppConfigRepository
import me.itzvirtual.openunito.data.localization.AppLocaleManager
import me.itzvirtual.openunito.data.localization.ResolvedLocale
import me.itzvirtual.openunito.data.localization.SupportedLanguage
import me.itzvirtual.openunito.data.localization.SupportedLocales
import me.itzvirtual.openunito.data.localization.SupportedRegion
import me.itzvirtual.openunito.data.localization.withDateFormatter
import me.itzvirtual.openunito.domain.getToday
import me.itzvirtual.openunito.resources.Res
import me.itzvirtual.openunito.resources.language_screen_title
import me.itzvirtual.openunito.ui.LocalResolvedLocale
import me.itzvirtual.openunito.ui.themes.OpenUnitoTheme
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
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
            TopAppBar(
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
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                val resolvedLocale = LocalResolvedLocale.current
                Text("Languages")
                LazyColumn {
                    items(
                        items = SupportedLocales.languages,
                        key = { it.tag }
                    ) { language ->
                        val isSelected = language.tag == resolvedLocale.langCode

                        LanguageOptionRow(
                            displayName = language.displayName,
                            isSelected = isSelected,
                            onClick = { onSelectLanguage(language) },
                        )
                    }
                }

                Text("Region")
                LazyColumn {
                    items(
                        items = SupportedLocales.regions,
                        key = { it.regionCode }
                    ) { region ->
                        val isSelected = region.regionCode == resolvedLocale.regionCode

                        LanguageOptionRow(
                            displayName = region.displayName,
                            isSelected = isSelected,
                            onClick = { onSelectRegion(region) },
                        )
                    }
                }
                Spacer(Modifier.height(12.dp))
                Text("Dates Preview")
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