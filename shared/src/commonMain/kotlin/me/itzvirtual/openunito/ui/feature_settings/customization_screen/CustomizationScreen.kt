package me.itzvirtual.openunito.ui.feature_settings.customization_screen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.HistoryEdu
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.dropShadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.AndroidUiModes.UI_MODE_NIGHT_YES
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import me.itzvirtual.openunito.resources.Res
import me.itzvirtual.openunito.resources.attendance_tracking_button_name
import me.itzvirtual.openunito.resources.settings_customization
import me.itzvirtual.openunito.ui.components.GradientBackground
import me.itzvirtual.openunito.ui.components.ShadowDefaults
import me.itzvirtual.openunito.ui.components.StdSurface
import me.itzvirtual.openunito.ui.components.StdSwitch
import me.itzvirtual.openunito.ui.shared.Toaster
import me.itzvirtual.openunito.ui.themes.LocalCustomColorsPalette
import me.itzvirtual.openunito.ui.themes.OpenUnitoTheme
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun CustomizationScreen(
    modifier: Modifier = Modifier,
    viewModel: CustomizationScreenViewModel = koinViewModel(),
    onBack: () -> Unit,
) {
    val state by viewModel.screenState.collectAsStateWithLifecycle()
    val toaster: Toaster = koinInject()

    CustomizationScreenContent(
        modifier = modifier,
        onBack = onBack,
        showAttendanceValue = state.showAttendance,
        onChangeShowAttendance = { value ->
//            viewModel.setShowAttendance(value)
            toaster.show("Coming soon!")
        },
    )
}

@Composable
fun CustomizationScreenContent(
    modifier: Modifier = Modifier,
    showAttendanceValue: Boolean?,
    onChangeShowAttendance: (Boolean) -> Unit,
    onBack: () -> Unit = {},
) {
    val insetPadding = 12.dp
    Scaffold(
        modifier = modifier,
        topBar = {
            GradientBackground(
                colors = listOf(
                    MaterialTheme.colorScheme.onPrimary,
                    LocalCustomColorsPalette.current.onSettingsColor,
                ),
            ) {
                TopAppBar(
                    title = {
                        Text(
                            modifier = Modifier
                                .dropShadow(
                                    RoundedCornerShape(16.dp),
                                    shadow = ShadowDefaults.Text
                            ),
                            text = stringResource(Res.string.settings_customization),
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
            Spacer(
                modifier = Modifier.height(16.dp)
            )

            StdSurface {
                Column {
                    StdSwitch(
                        modifier = Modifier
                            .padding(horizontal = 16.dp, 2.dp)
                            .fillMaxWidth(),
                        value = showAttendanceValue ?: false,
                        onChanged = onChangeShowAttendance,
                        icon = Icons.Default.HistoryEdu,
                        iconColor = Color(99, 166, 234, 255),
                        text = stringResource(Res.string.attendance_tracking_button_name),
                        fontWeight = FontWeight.Bold,
                        switchColors = SwitchDefaults.colors().copy(
                            checkedTrackColor = LocalCustomColorsPalette.current.settingsColor,
                            checkedThumbColor = lerp(
                                LocalCustomColorsPalette.current.settingsColor,
                                Color.Black,
                                0.6f
                            )
                        )
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true, uiMode = UI_MODE_NIGHT_YES)
@Composable
fun CustomizationScreenPreview() {
    OpenUnitoTheme {
        Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
            CustomizationScreenContent(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                showAttendanceValue = false,
                onChangeShowAttendance = {}
            )
        }
    }
}