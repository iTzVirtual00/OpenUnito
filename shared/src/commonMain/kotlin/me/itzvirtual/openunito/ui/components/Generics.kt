package me.itzvirtual.openunito.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import me.itzvirtual.openunito.resources.Res
import me.itzvirtual.openunito.resources.empty_list
import me.itzvirtual.openunito.resources.no_data
import me.itzvirtual.openunito.ui.shared.ErrorItem
import org.jetbrains.compose.resources.stringResource

@Composable
fun StdSurface(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 16.dp,
    border: BorderStroke = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
    content: @Composable (() -> Unit),
) {
    Surface(
        modifier = modifier
            .fillMaxWidth(), //TODO: remove these
        shape = RoundedCornerShape(cornerRadius),
        color = MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp),
        border = border
    ) {
        content()
    }
}

@Composable
fun ListWrapper(
    modifier: Modifier = Modifier,
    isEmpty: Boolean,
    debug: Boolean,
    isLoading: Boolean,
    stateError: String?,
    content: @Composable () -> Unit,
) {
    if (isEmpty || stateError != null || isLoading) {
        StdSurface(
            modifier = modifier,
        ) {
            if (debug && stateError != null) {
                ErrorItem(stateError)
            } else {
                val msg = when {
                    isEmpty -> stringResource(Res.string.empty_list)
                    isLoading -> {
                        stringResource(Res.string.empty_list) + "..."
                    }
                    else -> stringResource(Res.string.no_data)
                }

                ErrorItem(msg, Color.Gray)
            }
        }
    } else {
        Box(
            modifier = modifier,
        ) {
            content()
        }
    }
}

@Composable
fun StdEntry(
    modifier: Modifier = Modifier,
    icon: ImageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
    clickable: Boolean = true,
    onClick: () -> Unit = {},
    arrowWeight: Float = 0.1f,
    content: @Composable () -> Unit,
) {
    Box (
        modifier = modifier
            .clickable(
                enabled = clickable,
                onClick = onClick,
            ),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .weight(1 - arrowWeight)
            ) {
                content()
            }
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier
                    .weight(arrowWeight)

            )
        }
    }
}

@Composable
fun StdTextIconEntry(
    modifier: Modifier = Modifier,
    text: String,
    fontWeight: FontWeight? = null,
    textColor: Color = Color.Unspecified,
    icon: ImageVector,
    iconColor: Color = LocalContentColor.current,
    clickable: Boolean = true,
    onClick: () -> Unit = {},
    spacing: Dp = 12.dp,
    arrowWeight: Float = 0.1f,
) {
    StdEntry (
        modifier = modifier,
        clickable = clickable,
        onClick = onClick,
        arrowWeight = arrowWeight,
    ) {
        Row {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconColor,
            )

            Spacer(
                modifier = Modifier.width(spacing)
            )

            Text(
                text = text,
                fontWeight = fontWeight,
                color = textColor,
            )
        }
    }
}