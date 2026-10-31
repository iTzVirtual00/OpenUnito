package me.itzvirtual.openunito.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp

// Copy-pasted from stackOverflow
@Composable
fun MeasureWidthLayout(
    viewToMeasure: @Composable () -> Unit,
    content: @Composable (measuredWidth: Dp) -> Unit,
) {
    SubcomposeLayout { constraints ->
        val measuredWidth = subcompose("viewToMeasure", viewToMeasure)[0]
            .measure(Constraints()).width.toDp()

        val contentPlaceable = subcompose("content") {
            content(measuredWidth)
        }[0].measure(constraints)
        layout(contentPlaceable.width, contentPlaceable.height) {
            contentPlaceable.place(0, 0)
        }
    }
}

@Composable
fun MeasureHeightLayout(
    viewToMeasure: @Composable () -> Unit,
    content: @Composable (measuredHeight: Dp) -> Unit,
) {
    SubcomposeLayout { constraints ->
        val measuredHeight = subcompose("viewToMeasure", viewToMeasure)[0]
            .measure(Constraints()).height.toDp()

        val contentPlaceable = subcompose("content") {
            content(measuredHeight)
        }[0].measure(constraints)
        layout(contentPlaceable.width, contentPlaceable.height) {
            contentPlaceable.place(0, 0)
        }
    }
}

@Composable
fun MeasureWidthHeightLayout(
    viewToMeasure: @Composable () -> Unit,
    content: @Composable (measuredWidth: Dp, measuredHeight: Dp) -> Unit,
) {
    SubcomposeLayout { constraints ->
        val measuredWidth = subcompose("viewToMeasure", viewToMeasure)[0]
            .measure(Constraints()).width.toDp()

        val measuredHeight = subcompose("viewToMeasure", viewToMeasure)[0]
            .measure(Constraints()).height.toDp()

        val contentPlaceable = subcompose("content") {
            content(measuredWidth, measuredHeight)
        }[0].measure(constraints)
        layout(contentPlaceable.width, contentPlaceable.height) {
            contentPlaceable.place(0, 0)
        }
    }
}