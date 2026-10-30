package me.itzvirtual.openunito.ui.feature_settings.customization_screen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.HistoryEdu
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.AndroidUiModes.UI_MODE_NIGHT_YES
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import me.itzvirtual.openunito.resources.Res
import me.itzvirtual.openunito.resources.attendance_tracking_button_name
import me.itzvirtual.openunito.resources.settings_customization
import me.itzvirtual.openunito.ui.components.StdSurface
import me.itzvirtual.openunito.ui.components.StdSwitch
import me.itzvirtual.openunito.ui.themes.OpenUnitoTheme
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun AppearanceScreen(
    modifier: Modifier = Modifier,
    viewModel: CustomizationScreenViewModel = koinViewModel(),
    onBack: () -> Unit,
) {
    val state by viewModel.screenState.collectAsStateWithLifecycle()

    AppearanceScreenContent(
        modifier = modifier,
        onBack = onBack,
        showAttendanceValue = state.showAttendance,
        onChangeShowAttendance = { value ->
            viewModel.setShowAttendance(value)
        },
    )
}

@Composable
fun AppearanceScreenContent(
    modifier: Modifier = Modifier,
    showAttendanceValue: Boolean,
    onChangeShowAttendance: (Boolean) -> Unit,
    onBack: () -> Unit = {},
) {
    val insetPadding = 12.dp
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {
                    Text(
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
            StdSurface {
                Column {
                    StdSwitch(
                        modifier = Modifier
                            .padding(horizontal = 16.dp, 2.dp)
                            .fillMaxWidth(),
                        value = showAttendanceValue,
                        onChanged = onChangeShowAttendance,
                        icon = Icons.Default.HistoryEdu,
                        iconColor = Color(99, 166, 234, 255),
                        text = stringResource(Res.string.attendance_tracking_button_name),
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true, uiMode = UI_MODE_NIGHT_YES)
@Composable
fun AppearanceScreenPreview() {
    OpenUnitoTheme {
        Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
            AppearanceScreenContent(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                showAttendanceValue = false,
                onChangeShowAttendance = {}
            )
        }
    }
}