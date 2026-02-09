package me.itzvirtual.myunitox.ui.dashboard

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import me.itzvirtual.myunitox.ui.theme.MyUnitoXTheme

@Composable
fun CreditProgressIndicator(
    modifier: Modifier = Modifier,
    credits: Int,
    totalCredits: Int,
    plannedCredits: Int,
    strokeWidth: Dp = 10.dp,
    trackColor: Color = MaterialTheme.colorScheme.primaryContainer,
    progressColor: Color = Color(0xFFF4AD43),
    secondaryProgressColor: Color = Color(0xFF354CEA)
) {
    val creditProgress = credits.toFloat() / totalCredits
    val plannedCreditProgress = plannedCredits.toFloat() / totalCredits

    var animationPlayed by remember { mutableStateOf(false) }

    val currentCreditProgress by animateFloatAsState(
        targetValue = if (animationPlayed) creditProgress else 0f,
        animationSpec = tween(durationMillis = 1000, delayMillis = 300), label = "cfu"
    )

    val currentPlannedCreditProgress by animateFloatAsState(
        targetValue = if (animationPlayed) plannedCreditProgress else 0f,
        animationSpec = tween(durationMillis = 1000), label = "plannedCfu"
    )

    LaunchedEffect(Unit) {
        animationPlayed = true
    }

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(100.dp)) {
            val stroke = Stroke(width = strokeWidth.toPx(), cap = StrokeCap.Butt)
            drawArc(
                color = trackColor,
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                style = stroke
            )
            drawArc(
                color = progressColor,
                startAngle = -90f,
                sweepAngle = currentPlannedCreditProgress * 360f,
                useCenter = false,
                style = stroke
            )
            drawArc(
                color = secondaryProgressColor,
                startAngle = -90f,
                sweepAngle = currentCreditProgress * 360f,
                useCenter = false,
                style = stroke
            )
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "$credits/$totalCredits",
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                fontSize = 16.sp
            )
            Text(
                text = "CFU",
                fontSize = 14.sp
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun CreditProgressIndicatorPreview() {
    MyUnitoXTheme {
        Box(
            modifier = Modifier
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            CreditProgressIndicator(
                credits = 16,
                plannedCredits = 50,
                totalCredits = 180,
                modifier = Modifier.size(120.dp)
            )
        }
    }
}
