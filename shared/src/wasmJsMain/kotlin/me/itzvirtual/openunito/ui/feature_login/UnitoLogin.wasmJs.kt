package me.itzvirtual.openunito.ui.feature_login

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import me.itzvirtual.openunito.ui.feature_settings.dev_screen.AuthDebuggerCard

import me.itzvirtual.openunito.ui.themes.OpenUnitoTheme

@Composable
actual fun UniToLoginScreen(
    startUrl: String,
    onAuthSuccess: (String) -> Unit,
    modifier: Modifier
) {
    OpenUnitoTheme {
        Scaffold(modifier = modifier) { innerPadding ->
            Column(
                modifier = Modifier
                    .padding(innerPadding)
                    .padding(16.dp)
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                AuthDebuggerCard(
                    onSetToken = onAuthSuccess,
                    onCopyToken = {},
                    showCopyTokenButton = false,
                )
            }
        }
    }
}