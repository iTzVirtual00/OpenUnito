package me.itzvirtual.openunito.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowRight
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.CardTravel
import androidx.compose.material.icons.filled.LogoDev
import androidx.compose.material.icons.filled.Man
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchColors
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.draw.innerShadow
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.shadow.Shadow
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.AndroidUiModes.UI_MODE_NIGHT_YES
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.isSpecified
import me.itzvirtual.openunito.domain.examdate.ExamDate
import me.itzvirtual.openunito.resources.Res
import me.itzvirtual.openunito.resources.all
import me.itzvirtual.openunito.resources.data_is_old
import me.itzvirtual.openunito.resources.debug_entry
import me.itzvirtual.openunito.resources.empty_list
import me.itzvirtual.openunito.resources.error
import me.itzvirtual.openunito.resources.examsession_open_from_to
import me.itzvirtual.openunito.resources.loading
import me.itzvirtual.openunito.resources.show_dev_button
import me.itzvirtual.openunito.ui.feature_examsessions.ExamSessionCardContent
import me.itzvirtual.openunito.ui.feature_examsessions.ExamSessionCardDrawer
import me.itzvirtual.openunito.ui.feature_examsessions.ExamSessionCardHeader
import me.itzvirtual.openunito.ui.shared.ErrorItem
import me.itzvirtual.openunito.ui.shared.annotatedStringResource
import me.itzvirtual.openunito.ui.shared.unShoutCourseName
import me.itzvirtual.openunito.ui.themes.OpenUnitoTheme
import org.jetbrains.compose.resources.stringResource
import kotlin.math.sqrt

object ShadowDefaults {
    val Text = Shadow(
        radius = 12.dp,
        spread = 0.5f.dp,
        color = Color(0f, 0f, 0f, 0.2f),
        offset = DpOffset(x = 0.dp, 0.dp)
    )

    val Surface = Shadow(
        radius = 10.dp,
        spread = 1.dp,
        color = Color(0f, 0f, 0f, 0.5f),
        offset = DpOffset(x = 0.dp, 0.dp)
    )
}

object StdSurfaceDefaults {
    val CornerRadius = 16.dp

    val InternalPadding = PaddingValues(16.dp)

    @Composable fun InternalPadding(
        start: Dp = InternalPadding.calculateStartPadding(LocalLayoutDirection.current),
        top: Dp = InternalPadding.calculateTopPadding(),
        end: Dp = InternalPadding.calculateEndPadding(LocalLayoutDirection.current),
        bottom: Dp = InternalPadding.calculateBottomPadding(),
    ) {
        PaddingValues(
            start = start,
            top = top,
            end = end,
            bottom = bottom,
        )
    }

    val BorderColor : Color
        @Composable get() = MaterialTheme.colorScheme.outlineVariant

    val Border : BorderStroke
        @Composable get() = BorderStroke(1.dp, BorderColor)

    val Color : Color
        @Composable get() = MaterialTheme.colorScheme.surface
}

fun Shadow.copy(
    radius: Dp = this.radius,
    color: Color = this.color,
    spread: Dp = this.spread,
    offset: DpOffset = this.offset,
    alpha: Float = this.alpha,
    blendMode: BlendMode = this.blendMode
    ): Shadow = Shadow(
        radius = radius,
        color = color,
        spread = spread,
        offset = offset,
        alpha = alpha,
        blendMode = blendMode,
    )

@Composable
fun StaleCacheBanner( // TODO: UI may be better
    modifier: Modifier = Modifier,
    visible: Boolean,
    textStyle: TextStyle = MaterialTheme.typography.titleMedium
) {
    val iconSize = textStyle.lineHeight.value.dp

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn() + slideInVertically(),
        exit = fadeOut() + slideOutVertically(),
    ) {
        Row(
            modifier = modifier,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                modifier = Modifier
                    .size(iconSize),
                imageVector = Icons.Rounded.Warning,
                contentDescription = null,
                tint = Color(0.976f, 0.733f, 0.275f, 1.0f),
            )

            Spacer(
                modifier = Modifier.width(iconSize.times(0.2f)),
            )

            Text(
                text = stringResource(Res.string.data_is_old), // TODO: add "3 days old" if we really need it,
                style = textStyle,
            )
        }
    }

}

@Composable
fun StdSurface(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = StdSurfaceDefaults.CornerRadius,
    border: BorderStroke? = StdSurfaceDefaults.Border,
    color: Color = StdSurfaceDefaults.Color,
    content: @Composable (() -> Unit),
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(cornerRadius),
        color = color,
        border = border
    ) {
        content()
    }
}

@Composable
fun ListWrapper(
    modifier: Modifier = Modifier,
    isEmpty: Boolean,
    debug: Boolean?,
    isLoading: Boolean,
    stateError: String?,
    errorMessage: String = stringResource(Res.string.error),
    emptyMessage: String = stringResource(Res.string.empty_list),
    content: @Composable () -> Unit,
) {
    if (isEmpty || stateError != null || isLoading) {
        StdSurface(
            modifier = modifier
                .padding(vertical = 8.dp), // To add padding only in case of noData in ExamSessions
        ) {
            if (debug ?: false && stateError != null) {
                ErrorItem(stateError)
            } else {
                val msg = when {
                    stateError != null -> errorMessage
                    isLoading -> {
                        stringResource(Res.string.loading)
                    }
                    else -> emptyMessage // state error is not null
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
    Box(
        modifier = Modifier
            .clickable(
                enabled = clickable,
                onClick = onClick,
            )
            .then(modifier),
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
    StdEntry(
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

@Composable
fun StdSwitch(
    modifier: Modifier = Modifier,
    value: Boolean,
    onChanged: (Boolean) -> Unit = {},
    icon: ImageVector,
    iconColor: Color = Color.Unspecified,
    text: String,
    textColor: Color = Color.Unspecified,
    fontWeight: FontWeight? = null,
    fontSize: TextUnit = TextUnit.Unspecified,
    switchColors: SwitchColors = SwitchDefaults.colors(),
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            modifier = Modifier
        ) {
            Icon(
                modifier = Modifier,
                imageVector = icon,
                contentDescription = null,
                tint = iconColor,
            )
            Spacer(
                modifier = Modifier
                    .width(12.dp),
            )
            Text(
                text = text,
                color = textColor,
                fontWeight = fontWeight,
                fontSize = fontSize,
                lineHeight = when {
                    fontSize.isSpecified -> fontSize.times(1.4)
                    else -> TextUnit.Unspecified
                },
            )
        }
        Switch(
            modifier = Modifier
                .padding(vertical = 2.dp),
            checked = value,
            onCheckedChange = onChanged,
            colors = switchColors,
        )
    }
}

@Composable
fun StdDropdownEntry(
    modifier: Modifier = Modifier,
    elementList: List<String>,
    selectedElement: Int? = null,
    onChangeSelected: (Int) -> Unit,
    buttonSize: Dp = 120.dp,
    text: String,
    textColor: Color = Color.Unspecified,
    fontWeight: FontWeight? = null,
    fontSize: TextUnit = TextUnit.Unspecified,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            modifier = Modifier
        ) {
            Text(
                text = text,
                color = textColor,
                fontWeight = fontWeight,
                fontSize = fontSize,
                lineHeight = when {
                    fontSize.isSpecified -> fontSize.times(1.4)
                    else -> TextUnit.Unspecified
                },
            )
        }
        StdDropdownButton(
            modifier = Modifier.width(buttonSize),
            elementList = elementList,
            selectedElement = selectedElement,
            onChangeSelected = onChangeSelected,
        )
    }

}

@Composable
fun StdIconDropdownEntry(
    modifier: Modifier = Modifier,
    elementList: List<String>,
    selectedElement: Int? = null,
    onChangeSelected: (Int) -> Unit,
    buttonSize: Dp = 120.dp,
    icon: ImageVector = Icons.Default.Man,
    iconColor: Color = LocalContentColor.current,
    text: String,
    textColor: Color = Color.Unspecified,
    fontWeight: FontWeight? = null,
    fontSize: TextUnit = TextUnit.Unspecified,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            modifier = Modifier
        ) {
            Icon(
                modifier = Modifier,
                imageVector = icon,
                contentDescription = null,
                tint = iconColor,
            )

            Spacer(
                modifier = Modifier
                    .width(12.dp),
            )

            Text(
                text = text,
                color = textColor,
                fontWeight = fontWeight,
                fontSize = fontSize,
                lineHeight = when {
                    fontSize.isSpecified -> fontSize.times(1.4)
                    else -> TextUnit.Unspecified
                },
            )
        }
        StdDropdownButton(
            modifier = Modifier.width(buttonSize),
            elementList = elementList,
            selectedElement = selectedElement,
            onChangeSelected = onChangeSelected,
        )
    }

}

@Composable
fun StdDropdownButton(
    modifier: Modifier = Modifier,
    elementList: List<String>,
    selectedElement: Int?,
    onChangeSelected: (Int) -> Unit,
    defaultText: String = stringResource(Res.string.all),
    fontSize: TextUnit = TextUnit.Unspecified,
    color: Color = Color.Unspecified,
) {
    var isDropDownExpanded by remember { mutableStateOf(false) }


    Box(
        modifier = modifier
    ) {
        StdButtonEmpty(
            modifier = Modifier,
            onClick = {
                isDropDownExpanded = true
            }
        ) {
            Row(
                modifier = Modifier
                    .padding(horizontal = 12.dp, vertical = 8.dp)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(

                    Modifier.weight(1f)
                ) {
                    Spacer(
                        modifier = Modifier.width(8.dp)
                    )
                    Text(
                        text = when (selectedElement) {
                            null -> defaultText
                            else -> unShoutCourseName(elementList[selectedElement])
                        },
                        fontSize = fontSize,
                        lineHeight = when {
                            fontSize.isSpecified -> fontSize.times(1.4)
                            else -> TextUnit.Unspecified
                        },
                        color = color
                    )
                }

                //Arrow
                AnimatedContent(
                    targetState = isDropDownExpanded
                ) { scope ->
                    val rot = if (!scope) 90f else 270f
                    Icon(
                        modifier = Modifier
                            .rotate(rot),
                        imageVector = Icons.AutoMirrored.Filled.ArrowRight,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.outline,
                    )
                }
            }
        }

        DropdownMenu(
            expanded = isDropDownExpanded,
            onDismissRequest = { isDropDownExpanded = false },
        ) {
            elementList.forEachIndexed { index, string ->
                DropdownMenuItem(
                    text = {
                        Text(
                            text = string,
                        )
                    },
                    onClick = {
                        isDropDownExpanded = false
                        onChangeSelected(index)
                    }
                )
            }
        }
    }
}

class Vector2d(
    var x: Float,
    var y: Float,
) {
    fun dotProduct(a: Vector2d): Float {
        return (a.x + this.x) + (a.y * this.y)
    }

    fun length(): Float {
        return sqrt((this.x * this.x) + (this.y * this.y))
    }

    fun times(a: Float): Vector2d {
        x *= a
        y *= a
        return this
    }

    fun copy(): Vector2d {
        return Vector2d(x, y)
    }
}

@Composable
fun GradientBackground(
    modifier: Modifier = Modifier,
    colors: List<Color>,
    content: @Composable () -> Unit,
) {
    Box(
        modifier = modifier
            .drawWithCache {
                val directionVec = Vector2d(1f, 1f)
                val centerVec = Vector2d(size.width * 0.5f, size.height * 0.5f)

                // Projection of centerVec on directionVec
                // ((a * b) / sqrt(b.len)) * b
                val projMod = (centerVec.dotProduct(directionVec) / sqrt(directionVec.length()))
                val projVec = directionVec.times(projMod).times(1f)

                // Map gradient beginning and end to the vector
                val startVec = projVec.copy().times(0.1f)
                val endVec = projVec.copy().times(1f)

                val brush = Brush.linearGradient(
                    colors = colors,
                    start = Offset(startVec.x, startVec.y),
                    end = Offset(endVec.x, endVec.y)
                )
                onDrawBehind {
                    drawRect(brush)
                }
            }
            .innerShadow(
                shape = RoundedCornerShape(0.dp),
                shadow = Shadow(
                    radius = 18.dp,
                    spread = (-18).dp,
                    color = Color(0f, 0f, 0f, 0.7f),
                    offset = DpOffset(x = 0.dp, (-15).dp)
                )
            ),


        ) {
        content()
    }
}

@Composable
fun StdDrawerCard(
    modifier: Modifier = Modifier,
    expanded: Boolean,
    border: BorderStroke? = StdSurfaceDefaults.Border,
    internalPadding: PaddingValues = StdSurfaceDefaults.InternalPadding,
    headerContent: @Composable () -> Unit,
    drawerContent: @Composable () -> Unit,
) {
    StdSurface(
        modifier = modifier,
        border = border,
    ) {
        Column(
            modifier = Modifier
                .padding(internalPadding),
        ) {
            headerContent()

            AnimatedVisibility(
                visible = expanded,
            ) {
                Column {
                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )

                    drawerContent()
                }
            }
        }
    }
}


@Preview(showBackground = true, uiMode = UI_MODE_NIGHT_YES)
@Composable
fun GenericsPreview() {
    OpenUnitoTheme {
        Scaffold(modifier = Modifier.fillMaxSize()) { _ ->
            Column {
                StaleCacheBanner(
                    modifier = Modifier.padding(16.dp),
                    visible = true,
                )

                StdSurface {
                    Column {
                        StdSwitch(
                            modifier = Modifier
                                .padding(horizontal = 16.dp, 2.dp)
                                .fillMaxWidth(),
                            value = false,
                            icon = Icons.Default.BugReport,
                            iconColor = Color(0.976f, 0.733f, 0.275f, 1.0f),
                            text = stringResource(Res.string.debug_entry),
                            fontWeight = FontWeight.Bold
                        )

                        HorizontalDivider()

                        StdSwitch(
                            modifier = Modifier
                                .padding(horizontal = 16.dp, 2.dp)
                                .fillMaxWidth(),
                            value = true,
                            icon = Icons.Default.LogoDev,
                            iconColor = MaterialTheme.colorScheme.error,
                            text = stringResource(Res.string.show_dev_button),
                            textColor = MaterialTheme.colorScheme.error,
                            fontWeight = FontWeight.Bold,
                            switchColors = SwitchDefaults.colors().copy(
                                checkedTrackColor = MaterialTheme.colorScheme.error,
                                checkedThumbColor = MaterialTheme.colorScheme.onError,
                            )
                        )

                        HorizontalDivider()

                        StdIconDropdownEntry(
                            modifier = Modifier
                                .padding(horizontal = 16.dp, 16.dp)
                                .fillMaxWidth(),
                            text = "Asd",
                            icon = Icons.Default.CardTravel,
                            elementList = emptyList(),
                            onChangeSelected = {},
                        )
                    }
                }
            }
        }
    }
}