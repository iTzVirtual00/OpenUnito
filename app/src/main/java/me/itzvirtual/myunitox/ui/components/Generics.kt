package me.itzvirtual.myunitox.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import me.itzvirtual.myunitox.R
import me.itzvirtual.myunitox.ui.shared.ErrorItem
import me.itzvirtual.myunitox.ui.shared.formatDate
import me.itzvirtual.myunitox.ui.shared.unShoutCourseName

@Composable
fun StdSurface(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 16.dp,
    border: BorderStroke = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
    content: @Composable (() -> Unit),
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(8.dp),//TODO: remove these
        shape = RoundedCornerShape(cornerRadius),
        color = MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp),
        border = border
    ) {
        content()
    }
}

@Composable
fun ListWrapper(
    isEmpty: Boolean,
    debug: Boolean,
    isLoading: Boolean,
    stateError: String?,
    content: @Composable () -> Unit,
) {
    if (isEmpty || stateError != null || isLoading) {
        StdSurface {
            if (debug) {
                val msg =
                    if (isLoading) "Loading"
                    else stateError ?: "Empty List"
                ErrorItem(msg)
            } else {
                ErrorItem(stringResource(R.string.no_data), Color.Gray)
            }
        }
    } else {
        content()
    }
}

@Composable
fun StdEntry(
    modifier: Modifier = Modifier,
    icon: ImageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
    clickable: Boolean = false,
    onClick: () -> Unit = {},
    content: @Composable () -> Unit,
) {
    val arrowWeight = 0.1f
    Row(
        modifier = modifier
            .clickable(
                onClick = onClick
            ),
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
