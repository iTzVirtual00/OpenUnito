package me.itzvirtual.openunito.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchColors
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.draw.innerShadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.shadow.Shadow
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.isSpecified
import me.itzvirtual.openunito.resources.ND
import me.itzvirtual.openunito.resources.Res
import me.itzvirtual.openunito.resources.empty_list
import me.itzvirtual.openunito.resources.error
import me.itzvirtual.openunito.ui.shared.ErrorItem
import org.jetbrains.compose.resources.stringResource
import kotlin.math.sqrt as sqrt

@Composable
fun StdSurface(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 16.dp,
    border: BorderStroke = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
    color: Color = MaterialTheme.colorScheme.surface,
    content: @Composable (() -> Unit),
) {
    Surface(
        modifier = modifier,
//            .fillMaxWidth(), //TODO: remove these
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
                    isEmpty -> emptyMessage
                    isLoading -> {
                        stringResource(Res.string.ND)
                    }
                    // state error is not null
                    else -> stringResource(Res.string.error)
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

@Composable
fun StdSwitch(
    modifier: Modifier = Modifier,
    value: Boolean,
    onChanged: (Boolean) -> Unit,
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

class Vector2d(
    public var x: Float,
    public var y: Float,
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
        return Vector2d(x,y)
    }
}

@Composable
fun TopBar(
    modifier: Modifier = Modifier,
    colors: List<Color>,
    content: @Composable () ->Unit,
) {
    Box( // TODO: when is large the gradient is to the right, make direction indipendent from proportions
        modifier = modifier
            .drawWithCache {
                val directionVec = Vector2d(1f,1f)
                val centerVec = Vector2d(size.width * 0.5f, size.height * 0.5f)

                // Projection of centerVec on directionVec
                // ((a * b) / sqrt(b.len)) * b
                val projMod = (centerVec.dotProduct(directionVec) / sqrt(directionVec.length()))
                val projVec = directionVec.times(projMod).times(1f)

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
                    color = Color(0f,0f,0f,0.5f),
                    offset = DpOffset(x = 0.dp, (-15).dp)
                )
            ),


    ) {
        content()
    }
}