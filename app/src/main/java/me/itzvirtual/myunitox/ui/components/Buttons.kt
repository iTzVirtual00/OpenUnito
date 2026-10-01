package me.itzvirtual.myunitox.ui.components

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import me.itzvirtual.myunitox.ui.themes.MyUnitoXTheme

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
@Preview(uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES, showBackground = true)
fun CareerScreenPreview() {
    MyUnitoXTheme {
        Scaffold(modifier = Modifier.fillMaxSize()) { _ ->
            Row (
                modifier = Modifier.fillMaxWidth()
            ) {
                StdButton(
                    icon = Icons.Default.Home,
                    modifier = Modifier.weight(1f),
                    text = "SUCA",
                    onClick = {}
                )
                Spacer(Modifier.width(4.dp))
                StdButton(
                    icon = Icons.Default.Home,
                    modifier = Modifier.weight(1f),
                    text = "SUCA3",
                    onClick = {}
                )
           }
        }

    }
}



@Composable
fun StdButton(
    icon: ImageVector,
    modifier: Modifier = Modifier,
    text: String,
    textModifier: Modifier = Modifier,
    onClick: () -> Unit = {},
    height: Float = 90f
) {
    Surface(
        shape = RoundedCornerShape(25f),
        onClick = onClick,
        modifier = modifier
            .height(Dp(height)),
        tonalElevation = 2.dp,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth()
                .padding(16f.dp, 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.fillMaxHeight(0.5f)
                    .aspectRatio(1f)
            )
            Spacer(modifier = Modifier.width(15.dp))
            Text(
                text = text,
//                modifier = Modifier.fillMaxHeight(),
                fontSize = 20.sp,
                fontWeight = FontWeight.Medium
//                textAlign = TextAlign.Center
            )

        }
    }
}