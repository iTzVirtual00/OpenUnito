package me.itzvirtual.myunitox.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import me.itzvirtual.myunitox.R
import me.itzvirtual.myunitox.ui.shared.ErrorItem

@Composable
fun StdSurface(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 16.dp,
    content: @Composable (() -> Unit)
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .height(90.dp)
            .padding(8.dp),
        shape = RoundedCornerShape(cornerRadius),
        color = MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        content()
    }
}

@Composable
fun ListWrapper(
    isEmpty: Boolean,
    debug: Boolean,
    isLoading:Boolean,
    stateError: String?,
    content: @Composable () -> Unit,
) {
    if (isEmpty || stateError != null || isLoading) {
        StdSurface() {
            if (debug) {
                val msg =
                    if (isLoading) "Loading"
                    else stateError ?: "Empty List"
                ErrorItem(msg)
            } else {
                ErrorItem(stringResource(R.string.no_data), Color.Gray)
            }
        }
    }
    else {
        content()
    }
}