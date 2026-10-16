package me.itzvirtual.openunito.ui.feature_login

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier


@Composable
expect fun UniToLoginScreen(
    startUrl: String,
    onAuthSuccess: (String) -> Unit,
    modifier: Modifier = Modifier,
)
