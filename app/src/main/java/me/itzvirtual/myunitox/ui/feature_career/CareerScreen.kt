package me.itzvirtual.myunitox.ui.feature_career

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import me.itzvirtual.myunitox.R
import me.itzvirtual.myunitox.domain.dashboard.dashboardDataSample
import me.itzvirtual.myunitox.ui.shared.CreditProgressIndicator
import me.itzvirtual.myunitox.ui.shared.ErrorItem
import me.itzvirtual.myunitox.ui.shared.LoadingIconItem
import me.itzvirtual.myunitox.ui.shared.LoadingShimmerItem
import me.itzvirtual.myunitox.ui.themes.MyUnitoXTheme

@Composable
@Preview(uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES, showBackground = true)
fun CareerScreenPreview() {
    MyUnitoXTheme {
        Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
            CareerScreenContent(
                state = CareerScreenState(
                    dashboardState = CareerDashboardUiState(
                        data = dashboardDataSample,
                        isLoading = false,
                        error = null
                    )
                ),
                modifier = Modifier.padding(innerPadding)
            )
        }
    }

}

@Composable
fun CareerScreen(viewModel: CareerViewModel = hiltViewModel(), modifier: Modifier) {
    val state by viewModel.screenState.collectAsStateWithLifecycle()
    var showStatistics by remember { mutableStateOf(false) }

    CareerScreenContent(state, modifier, onClick = { showStatistics = true })

    if (showStatistics) {
        //TODO: StatisticsSheet;
        //TranscriptSheet(onDismiss = { showTranscript = false })
    }
}

@Composable
fun CareerScreenContent(
    state: CareerScreenState,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
) {
    // column??
    Column(modifier = modifier) {
        Column(
            modifier = Modifier.clickable {
                onClick()
            }) {
            Spacer(modifier = Modifier.height(4.dp))
            CreditsSection(
                state = state.dashboardState
            )
            GoToTranscriptSection()
        }
    }
}

@Composable
fun GoToTranscriptSection() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Spacer(modifier = Modifier.weight(1f))
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.open_transcript),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Medium
            )
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
fun UserInitialsIcon(
    initials: String,
    modifier: Modifier = Modifier,
    size: Dp = 40.dp
) {
    Surface(
        modifier = modifier.size(size),
        shape = CircleShape,
        color = MaterialTheme.colorScheme.primary,
        contentColor = MaterialTheme.colorScheme.onPrimary
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = initials,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun CreditsSection(state: CareerDashboardUiState) {
    val data = state.data
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp)
            .height(IntrinsicSize.Min), // grow based on content
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Surface(
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp)
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                if (state.isLoading) {
                    LoadingIconItem(Modifier.height(72.dp))
                } else if (state.error != null) {
                    ErrorItem(state.error)
                } else {
                    data!!
                    val stats = state.data.stats
                    Box(
                        modifier = Modifier.padding(20.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CreditProgressIndicator(
                            modifier = Modifier.size(100.dp),
                            credits = stats.creditsGained.toInt(),
                            totalCredits = stats.totalCredits,
                            plannedCredits = stats.totalPlanCredits,
                            progressColor = MaterialTheme.colorScheme.primary,
                            plannedProgressColor = MaterialTheme.colorScheme.primary,
                            backgroundTrackColor = MaterialTheme.colorScheme.secondaryContainer,
                        )
                    }
                }
            }
        }

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp)
            ) {
                if (state.isLoading) {
                    LoadingShimmerItem()
                } else if (state.error != null) {
                    ErrorItem(state.error!!)
                } else {
                    val stats = data!!.stats
                    val yearlyProgress = stats.creditsGained.toFloat() / stats.totalPlanCredits
                    YearlyProgressCard(
                        title = stringResource(R.string.yearly_progress_title),
                        current = stats.creditsGained.toInt(),
                        total = stats.totalPlanCredits,
                        progress = yearlyProgress
                    )
                }
            }
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.tertiaryContainer,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (state.isLoading) {
                    LoadingShimmerItem()
                } else if (state.error != null) {
                    ErrorItem(state.error)
                } else {
                    val stats = data!!.stats
                    Text(
                        text = stringResource(
                            R.string.remaining_cfu_title,
                            stats.totalCredits - stats.creditsGained.toInt()
                        ),
                        modifier = Modifier.padding(12.dp),
                        style = MaterialTheme.typography.bodySmall,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}


@Composable
fun YearlyProgressCard(
    title: String,
    current: Int,
    total: Int,
    progress: Float
) {
    Column(modifier = Modifier.padding(12.dp)) {
        Text(
            text = title.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(8.dp))
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp),
            color = MaterialTheme.colorScheme.primary,
            trackColor = MaterialTheme.colorScheme.surfaceVariant,
            strokeCap = StrokeCap.Round
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "$current / $total CFU",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold
        )
    }
}