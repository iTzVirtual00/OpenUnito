package me.itzvirtual.openunito.ui.shared


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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import me.itzvirtual.openunito.ui.themes.MyUnitoXTheme
import kotlin.math.cos
import kotlin.math.sin

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
) {// TODO: make this better
    val creditProgress = if (credits >= 0) credits.toFloat() / totalCredits else 0f

    val plannedCreditProgress = if (credits >= 0) plannedCredits.toFloat() / totalCredits else 1f


    var animationPlayed by rememberSaveable { mutableStateOf(false) }
    val playAnimation = animate && !animationPlayed

    var targetProgress by remember { mutableFloatStateOf(if (playAnimation) 0f else creditProgress) }
    var targetPlanned by remember { mutableFloatStateOf(if (playAnimation) 0f else plannedCreditProgress) }

    LaunchedEffect(Unit) {
        if (playAnimation) {
            targetProgress = creditProgress
            targetPlanned = plannedCreditProgress
            animationPlayed = true
        }
    }

    val currentCreditProgress by animateFloatAsState(
        targetValue = targetProgress,
        animationSpec = tween(durationMillis = 1000, delayMillis = 300),
        label = "cfu"
    )
    val currentPlannedCreditProgress by animateFloatAsState(
        targetValue = targetPlanned,
        animationSpec = tween(durationMillis = 1000),
        label = "plannedCfu"
    )

    Box(modifier = modifier, contentAlignment = Alignment.Center) {
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

            if (currentPlannedCreditProgress > 0f) {
                val plannedAngle =
                    Math.toRadians((-90f + currentPlannedCreditProgress * 360f).toDouble())
                val markerLength = strokeWidth.toPx() * 1.5f
                val innerRadius = radius - markerLength / 2
                val outerRadius = radius + markerLength / 2
                drawLine(
                    color = plannedProgressColor,
                    start = Offset(
                        center.x + innerRadius * cos(plannedAngle).toFloat(),
                        center.y + innerRadius * sin(plannedAngle).toFloat()
                    ),
                    end = Offset(
                        center.x + outerRadius * cos(plannedAngle).toFloat(),
                        center.y + outerRadius * sin(plannedAngle).toFloat()
                    ),
                    strokeWidth = 4.dp.toPx(), cap = StrokeCap.Round
                )
            }
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "${if (credits >= 0) credits else "--"} CFU",
                fontWeight = FontWeight.ExtraBold,
                fontSize = 16.sp,
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "/ ${if (credits >= 0) totalCredits else "--"}",
                fontSize = 16.sp,
                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Light),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Preview
@Composable
fun CreditProgressIndicatorPreview() {
    MyUnitoXTheme {
        ElevatedCard(modifier = Modifier.padding(16.dp)) {
            CreditProgressIndicator(
                credits = 16,
                plannedCredits = 50,
                totalCredits = 180,
                modifier = Modifier.size(120.dp)
            )
        }
    }
}
