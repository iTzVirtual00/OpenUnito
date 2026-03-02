package me.itzvirtual.myunitox.ui.util

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.toClipEntry
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import me.itzvirtual.myunitox.data.models.GradeStatus
import me.itzvirtual.myunitox.ui.themes.LocalCustomColorsPalette
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun LoadingIconItem(modifier: Modifier) {
    Box(contentAlignment = Alignment.Center) {
        LoadingIndicator(
            modifier = Modifier
                .fillMaxWidth()
                .then(modifier),
        )
    }
}


@Composable
fun ErrorItem(errorMessage: String, color: Color = MaterialTheme.colorScheme.error) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp)
    ) {
        Text(text = errorMessage, color = color)
    }
}

@Composable
fun LoadingShimmerItem(
    centerColor: Color = MaterialTheme.colorScheme.surface,
    durationMillis: Int = 1500,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp)
            .shimmerEffect(
                centerColor,
                durationMillis = durationMillis
            )
    )
}

@Composable
fun Modifier.shimmerEffect(
    centerColor: Color,
    leftColor: Color = Color.Transparent,
    rightColor: Color = Color.Transparent,
    durationMillis: Int = 1500,
    easing: Easing = LinearEasing
): Modifier = composed {
    var size by remember { mutableStateOf(IntSize.Zero) }
    val transition = rememberInfiniteTransition(label = "shimmer")

    val startOffsetX by transition.animateFloat(
        initialValue = -2 * size.width.toFloat(),
        targetValue = 2 * size.width.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = durationMillis,
                easing = easing
            )
        ),
        label = "shimmerOffset"
    )

    background(
        brush = Brush.linearGradient(
            colors = listOf(leftColor, centerColor, rightColor),
            start = Offset(startOffsetX, 0f),
            end = Offset(startOffsetX + size.width.toFloat(), size.height.toFloat())
        )
    ).onGloballyPositioned { size = it.size }
}

val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.ITALY)

fun formatDate(date: Date): String {
    return dateFormat.format(date)
}

fun parseDate(dateStr: String): Date {
    return try {
        dateFormat.parse(dateStr)
    } catch (_: Exception) {
        Date(0)
    }
}

@Composable
fun GradeStatus.toColor(): Color = when (this) {
    GradeStatus.VIEWED -> MaterialTheme.colorScheme.surfaceContainerHighest
    GradeStatus.NOT_TOUCHED -> Color.Transparent

    GradeStatus.ACCEPTED -> LocalCustomColorsPalette.current.gradePassed
    GradeStatus.REJECTED -> LocalCustomColorsPalette.current.gradeNotPassed

    GradeStatus.UNKNOWN -> Color.DarkGray
}

// TODO: move this somewhere else
// Ai powered animation :sob:
@Composable
fun AnimatedSymmetricalBorder(
    isRevealed: Boolean,
    modifier: Modifier = Modifier,
    lineColor: Color = MaterialTheme.colorScheme.tertiary,
    pathCornerRadius: Dp = 8.dp,
    content: @Composable () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "BorderChase")
    val phase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                2000,
                easing = CubicBezierEasing(0.4f, 0.2f, 0.6f, 0.8f)
            ),
            repeatMode = RepeatMode.Restart
        ),
        label = "Phase"
    )

    val alpha by animateFloatAsState(
        targetValue = if (isRevealed) 0f else 1f,
        label = "BorderAlpha"
    )

    Box(
        modifier = Modifier
            .drawWithContent {
                drawContent()

                if (alpha > 0f) {
                    val strokeWidth = 2.dp.toPx()
                    val cornerRadius = pathCornerRadius.toPx()

                    // We calculate the perimeter once
                    val totalLength =
                        (size.width + size.height) * 2 - (8 * cornerRadius) + (2 * Math.PI.toFloat() * cornerRadius)

                    val segmentLength = totalLength * 0.2f
                    val gapLength = totalLength * 0.3f // 0.2 segment + 0.3 gap = 0.5 (for symmetry)

                    drawRoundRect(
                        color = lineColor.copy(alpha = alpha),
                        style = Stroke(
                            width = strokeWidth,
                            cap = StrokeCap.Round,
                            pathEffect = PathEffect.dashPathEffect(
                                intervals = floatArrayOf(segmentLength, gapLength),
                                // This is the "magic" that moves the lines
                                phase = -phase * totalLength
                            )
                        ),
                        cornerRadius = CornerRadius(cornerRadius),
                        size = size
                    )
                }
            }
            .then(modifier),
        contentAlignment = Alignment.Center
    ) {
        content()
    }
}

fun restartApp(context: Context) {
    val packageManager = context.packageManager
    val intent = packageManager.getLaunchIntentForPackage(context.packageName)
    val componentName = intent?.component
    val mainIntent = Intent.makeRestartActivityTask(componentName)

    context.startActivity(mainIntent)
    Runtime.getRuntime().exit(0)
}


fun String.toClipEntry() = android.content.ClipData.newPlainText(this, this).toClipEntry()

class MemoizedFetcher<K, V>(
    private val ttlMillis: Long = 5 * 60 * 1000,
    private val provider: suspend (K) -> V
) {
    private val cache = mutableMapOf<K, CacheEntry<V>>()
    private val cacheLock = Mutex()

    // Key-specific locks to allow parallel fetching of different data
    private val argumentLocks = mutableMapOf<K, Mutex>()

    private data class CacheEntry<V>(val data: V, val timestamp: Long)

    suspend fun execute(key: K, skipCache: Boolean = false): V {
        // fast path
        val now = System.currentTimeMillis()
        if (!skipCache) {
            cacheLock.withLock { cache[key] }?.let { entry ->
                if (now - entry.timestamp <= ttlMillis) return entry.data
            }
        }

        // fetch path
        val keyMutex = cacheLock.withLock {
            argumentLocks.getOrPut(key) { Mutex() }
        }

        return keyMutex.withLock {
            // Double-check, did someone else fetch it while we waited?
            if (!skipCache) {
                cacheLock.withLock { cache[key] }?.let { entry ->
                    if (System.currentTimeMillis() - entry.timestamp <= ttlMillis) {
                        Log.i("MemoizedFetcher", "Cache hit for key: $key")
                        return entry.data
                    }
                }
                Log.i("MemoizedFetcher", "Cache miss [${provider}] for key: $key")
            }

            // fetch
            val newData = provider(key)

            // save
            val isFailure = (newData as? Result<*>)?.isFailure ?: false
            if (!isFailure) {
                cacheLock.withLock {
                    cache[key] = CacheEntry(newData, System.currentTimeMillis())
                }
            }
            newData
        }
    }

    suspend fun clear() {
        cacheLock.withLock { cache.clear(); argumentLocks.clear() }
    }
}