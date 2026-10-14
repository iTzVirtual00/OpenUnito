package me.itzvirtual.myunitox.ui.shared

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import me.itzvirtual.myunitox.R
import me.itzvirtual.myunitox.domain.student.StudentCareer

@Composable
fun StudentCareerCard(career: StudentCareer) {
    Surface(
        modifier = Modifier
            .fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceColorAtElevation(4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = career.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = stringResource(R.string.student_id_label) + ": ",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        CopyableText(
                            text = career.studentId,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (career.isActive()) MaterialTheme.colorScheme.primaryContainer
                    else MaterialTheme.colorScheme.errorContainer
                ) {
                    Text(
                        text = career.staStuDes,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = if (career.isActive()) MaterialTheme.colorScheme.onPrimaryContainer
                        else MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }

            Column {
                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(
                    thickness = 1.dp,
                    color = MaterialTheme.colorScheme.outlineVariant
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = career.description,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (career.notes.isNotBlank()) {
                    Text(
                        text = career.notes,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))

                // Career Details Grid
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Left Column
                    Column(modifier = Modifier.weight(1f)) {
                        CareerDetailItem(
                            stringResource(R.string.student_id_label),
                            career.studentId
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        CareerDetailItem(
                            stringResource(R.string.course_year_label),
                            career.courseYear.toString()
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        CareerDetailItem(stringResource(R.string.cohort_label), career.cohort)
                        Spacer(modifier = Modifier.height(8.dp))
                        CareerDetailItem(
                            stringResource(R.string.normativa_label),
                            career.normativa
                        )
                    }

                    // Right Column
                    Column(modifier = Modifier.weight(1f)) {
                        CareerDetailItem(
                            stringResource(R.string.regulation_year_label),
                            career.regulationYear
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        CareerDetailItem(stringResource(R.string.class_label), career.className)
                        Spacer(modifier = Modifier.height(8.dp))
                        CareerDetailItem(
                            stringResource(R.string.location_label),
                            career.sedeDes
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        CareerDetailItem(
                            stringResource(R.string.percorso_label),
                            career.percorsoDes
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CareerDetailItem(label: String, value: String) {
    Column {
        Text(
            text = label.uppercase(),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun CopyableText(
    text: String,
    modifier: Modifier = Modifier,
    style: TextStyle = MaterialTheme.typography.bodyMedium,
    color: Color = MaterialTheme.colorScheme.onSurface,
) {
    val clipboard = LocalClipboard.current
    val haptics = LocalHapticFeedback.current
    var showCopied by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(4.dp))
            .clickable {
                scope.launch { clipboard.setClipEntry(text.toClipEntry()) }
                haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                showCopied = true
            }
            .padding(vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = text, style = style, color = color,
            modifier = Modifier.drawBehind {
                val strokeWidth = 2f // Thickness of the dots
                val dashWidth = 4f   // Length of each dot/dash
                val gapWidth = 4f    // Space between dots

                val y = size.height // Draw at the very bottom of the text bounds

                drawLine(
                    color = Color.Gray, // Or MaterialTheme.colorScheme.primary
                    start = Offset(0f, y),
                    end = Offset(size.width, y),
                    strokeWidth = strokeWidth,
                    pathEffect = PathEffect.dashPathEffect(
                        intervals = floatArrayOf(dashWidth, gapWidth),
                        phase = 0f
                    )
                )
            }
        )
        Icon(
            imageVector = Icons.Default.ContentCopy,
            contentDescription = "Copy",
            modifier = Modifier.size(12.dp),
            tint = color.copy(alpha = 0.6f)
        )
        AnimatedVisibility(
            visible = showCopied,
            enter = fadeIn() + expandHorizontally(),
            exit = fadeOut() + shrinkHorizontally()
        ) {
            Text(
                text = stringResource(R.string.copied_label),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
        }
    }

    if (showCopied) {
        LaunchedEffect(Unit) { delay(1500); showCopied = false }
    }
}
