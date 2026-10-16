package me.itzvirtual.openunito.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import me.itzvirtual.openunito.ui.themes.MyUnitoXTheme

@Composable
fun StdButtonEmpty(
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
    height: Float = 90f,
    shape: Shape = RoundedCornerShape(25f),
    border: BorderStroke? = null,
    tonalElevation: Dp = 2.dp,
    content: @Composable (() -> Unit)
) {
    Surface(
        shape = shape,
        onClick = onClick,
        modifier = modifier
            .height(Dp(height)),
        tonalElevation = tonalElevation,
        border = border,
    ) {
        content()
    }
}

@Composable
fun StdTextButton(
    text: String,
    modifier: Modifier = Modifier,
    textModifier: Modifier = Modifier,
    onClick: () -> Unit = {},
    height: Float = 90f,
    shape: Shape = RoundedCornerShape(25f),
    border: BorderStroke? = null,
    tonalElevation: Dp = 2.dp,
) {
    StdButtonEmpty(
        modifier = modifier,
        onClick = onClick,
        height = height,
        shape = shape,
        tonalElevation = tonalElevation,
        border = border,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(26f.dp, 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start
        ) {
            Text(
                text = text,
                modifier = textModifier,
                fontSize = 20.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
fun StdIconTextButton(
    text: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    textModifier: Modifier = Modifier,
    onClick: () -> Unit = {},
    height: Float = 90f,
    shape: Shape = RoundedCornerShape(25f),
    tonalElevation: Dp = 2.dp,
    border: BorderStroke? = null,
) {
    StdButtonEmpty(
        modifier = modifier,
        onClick = onClick,
        height = height,
        shape = shape,
        tonalElevation = tonalElevation,
        border = border,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20f.dp, 0.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier
                    .fillMaxHeight(0.43f)
                    .aspectRatio(1f)
            )
            Spacer(
                modifier = Modifier.width((height * 0.25).dp)
            )
            Text(
                text = text,
                modifier = textModifier,
                fontSize = 20.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
fun StdArrowButton(
    text: String,
    modifier: Modifier = Modifier,
    textModifier: Modifier = Modifier,
    onClick: () -> Unit = {},
    height: Float = 90f,
    shape: Shape = RoundedCornerShape(25f),
    tonalElevation: Dp = 2.dp,
    border: BorderStroke? = null,
) {
    StdButtonEmpty(
        modifier = modifier,
        onClick = onClick,
        height = height,
        shape = shape,
        tonalElevation = tonalElevation,
        border = border,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(26f.dp, 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = text,
                modifier = textModifier,
                fontSize = 20.sp,
                fontWeight = FontWeight.Medium
            )
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                modifier = Modifier
                    .fillMaxHeight(0.5f)
                    .aspectRatio(1f)
            )
        }

    }
}


@Preview(showBackground = true)
@Suppress("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun CareerScreenPreview() {
    MyUnitoXTheme {
        Scaffold(modifier = Modifier.fillMaxSize()) { _ ->
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    StdIconTextButton(
                        icon = Icons.Default.Home,
                        modifier = Modifier.weight(1f),
                        text = "SUCA",
                        onClick = {}
                    )
                    Spacer(Modifier.width(4.dp))
                    StdIconTextButton(
                        icon = Icons.Default.Home,
                        modifier = Modifier.weight(1f),
                        text = "SUCA3",
                        onClick = {}
                    )
                }
                StdTextButton(
                    text = "Suca"
                )
                StdArrowButton(
                    "Sucamela"
                )
            }
        }
    }
}
