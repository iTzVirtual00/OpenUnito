package me.itzvirtual.openunito.ui.shared

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

// TODO: Change colors
private val OfflineGray = Color(0xFF616161)
private val OnlineGreen = Color(0xFF4CAF50)

@Composable
fun OfflineBanner(
    isOnline: Boolean,
    modifier: Modifier = Modifier,
) {
    var showBanner by remember { mutableStateOf(false) }
    var wasOffline by remember { mutableStateOf(false) }
    var showReconnected by remember { mutableStateOf(false) }

    LaunchedEffect(isOnline) {
        if (!isOnline) {
            wasOffline = true
            showReconnected = false
            showBanner = true
        } else if (wasOffline) {
            // Came back online after being offline
            showReconnected = true
            delay(2000)
            showBanner = false
            delay(300) // wait for exit animation
            showReconnected = false
            wasOffline = false
        }
    }

    val backgroundColor by animateColorAsState(
        targetValue = if (showReconnected) OnlineGreen else OfflineGray,
        animationSpec = tween(durationMillis = 500),
        label = "bannerColor",
    )

    AnimatedVisibility(
        visible = showBanner,
        enter = expandVertically(expandFrom = Alignment.Bottom),
        exit = shrinkVertically(shrinkTowards = Alignment.Bottom),
        modifier = modifier,
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(backgroundColor)
                .padding(vertical = 4.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = if (showReconnected) "Back online" else "You are offline",
                color = Color.White,
                fontSize = 12.sp,
                textAlign = TextAlign.Center,
            )
        }
    }
}
