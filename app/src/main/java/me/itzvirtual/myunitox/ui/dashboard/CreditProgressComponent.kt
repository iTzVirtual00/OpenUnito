package me.itzvirtual.myunitox.ui.dashboard

import android.content.res.Configuration
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import me.itzvirtual.myunitox.ui.themes.main.MyUnitoXTheme


@Composable
fun CreditProgressIndicator(
    modifier: Modifier = Modifier,
    credits: Int,
    totalCredits: Int,
    plannedCredits: Int,
    strokeWidth: Dp = 10.dp,
    progressColor: Color = MaterialTheme.colorScheme.tertiary,
    plannedProgressColor: Color = MaterialTheme.colorScheme.tertiary,
    backgroundTrackColor: Color = MaterialTheme.colorScheme.secondaryContainer,
    animate: Boolean = !LocalInspectionMode.current,
) {
    val creditProgress = credits.toFloat() / totalCredits
    val plannedCreditProgress = plannedCredits.toFloat() / totalCredits

    var targetProgress by remember { mutableFloatStateOf(if (animate) 0f else creditProgress) }
    var targetPlanned by remember { mutableFloatStateOf(if (animate) 0f else plannedCreditProgress) }

    LaunchedEffect(Unit) {
        if (animate) {
            targetProgress = creditProgress
            targetPlanned = plannedCreditProgress
        }
    }

    val currentCreditProgress = if (animate) {
        animateFloatAsState(
            targetValue = targetProgress,
            animationSpec = tween(durationMillis = 1000, delayMillis = 300),
            label = "cfu"
        ).value
    } else {
        creditProgress
    }

    val currentPlannedCreditProgress = if (animate) {
        animateFloatAsState(
            targetValue = targetPlanned,
            animationSpec = tween(durationMillis = 1000),
            label = "plannedCfu"
        ).value
    } else {
        plannedCreditProgress
    }

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(100.dp)) {
            val stroke = Stroke(width = strokeWidth.toPx(), cap = StrokeCap.Butt)
            val radius = size.minDimension / 2
            val center = this.center

            drawArc(
                color = backgroundTrackColor,
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                style = stroke
            )

            drawArc(
                color = progressColor,
                startAngle = -90f,
                sweepAngle = currentCreditProgress * 360f,
                useCenter = false,
                style = stroke
            )

            // planned credits bar
            if (currentPlannedCreditProgress > 0f) {
                val plannedAngle =
                    Math.toRadians((-90f + currentPlannedCreditProgress * 360f).toDouble())
                val markerLength = strokeWidth.toPx() * 1.5f // Extends beyond the arc
                val innerRadius = radius - markerLength / 2
                val outerRadius = radius + markerLength / 2

                val startX = center.x + innerRadius * kotlin.math.cos(plannedAngle).toFloat()
                val startY = center.y + innerRadius * kotlin.math.sin(plannedAngle).toFloat()
                val endX = center.x + outerRadius * kotlin.math.cos(plannedAngle).toFloat()
                val endY = center.y + outerRadius * kotlin.math.sin(plannedAngle).toFloat()

                drawLine(
                    color = plannedProgressColor,
                    start = androidx.compose.ui.geometry.Offset(startX, startY),
                    end = androidx.compose.ui.geometry.Offset(endX, endY),
                    strokeWidth = 4.dp.toPx(),
                    cap = StrokeCap.Round
                )
            }
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "$credits CFU",
                fontWeight = FontWeight.ExtraBold,
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "/ $totalCredits",
                fontSize = 14.sp,
                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Light),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Preview
@Composable
fun CreditProgressIndicatorPreview() {
    MyUnitoXTheme {
        ElevatedCard(
            modifier = Modifier
                .padding(16.dp),
        ) {
            CreditProgressIndicator(
                credits = 16,
                plannedCredits = 50,
                totalCredits = 180,
                modifier = Modifier.size(120.dp),
            )
        }
    }
}
