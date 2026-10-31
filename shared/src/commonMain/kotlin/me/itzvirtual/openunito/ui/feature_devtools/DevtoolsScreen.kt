package me.itzvirtual.openunito.ui.feature_devtools


import androidx.compose.foundation.BorderStroke
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

//TODO: add database dump feature

@Composable
expect fun UpdateCard(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 16.dp,
    border: BorderStroke = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
    onClick: () -> Unit = {},
)