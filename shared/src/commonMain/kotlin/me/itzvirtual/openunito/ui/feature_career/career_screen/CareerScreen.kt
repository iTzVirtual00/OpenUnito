package me.itzvirtual.openunito.ui.feature_career.career_screen

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Book
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.dropShadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.AndroidUiModes.UI_MODE_NIGHT_YES
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import me.itzvirtual.openunito.domain.dashboard.DashboardData
import me.itzvirtual.openunito.domain.dashboard.dashboardDataSample
import me.itzvirtual.openunito.domain.examresult.GradeStatus
import me.itzvirtual.openunito.domain.examresult.examResultsDataSample
import me.itzvirtual.openunito.resources.Res
import me.itzvirtual.openunito.resources.credits_section_title
import me.itzvirtual.openunito.resources.exam_results_title
import me.itzvirtual.openunito.resources.examsession_title
import me.itzvirtual.openunito.resources.open_credit_statistics
import me.itzvirtual.openunito.resources.remaining_cfu_title
import me.itzvirtual.openunito.resources.transcript_title
import me.itzvirtual.openunito.resources.yearly_progress_title
import me.itzvirtual.openunito.ui.components.GradientBackground
import me.itzvirtual.openunito.ui.components.ShadowDefaults
import me.itzvirtual.openunito.ui.components.StaleCacheBanner
import me.itzvirtual.openunito.ui.components.StdIconTextButton
import me.itzvirtual.openunito.ui.components.StdSurface
import me.itzvirtual.openunito.ui.feature_career.transcript_screen.TranscriptViewModel
import me.itzvirtual.openunito.ui.shared.CreditProgressIndicator
import me.itzvirtual.openunito.ui.shared.ErrorItem
import me.itzvirtual.openunito.ui.themes.LocalCustomColorsPalette
import me.itzvirtual.openunito.ui.themes.OpenUnitoTheme
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import kotlin.time.Duration.Companion.milliseconds


@Composable
fun CareerScreen(
    modifier: Modifier = Modifier,
    viewModel: CareerViewModel = koinViewModel(),
    transcriptViewModel: TranscriptViewModel = koinViewModel(),
    onOpenTranscript: () -> Unit = {},
    onOpenInvokes: () -> Unit = {},
    onOpenStatistics: () -> Unit = {},
) {
    val state by viewModel.screenState.collectAsStateWithLifecycle()
    var showStatistics by remember { mutableStateOf(false) }

    val actions = ExamResultActions(
        changeGradeStatus = { gradeId, newStatus ->
            transcriptViewModel.changeGradeStatus(gradeId, newStatus)
        }
    )

    CompositionLocalProvider(LocalExamResultActions provides actions) {
        CareerScreenContent(
            modifier = modifier,
            dashboardState = state.dashboardState,
            examResultsState = state.examResultsState,
            onChangeGradeStatus = { gradeId, newStatus ->
                transcriptViewModel.changeGradeStatus(gradeId, newStatus)
          },
            onOpenTranscript = onOpenTranscript,
            onOpenInvoke = onOpenInvokes,
            onOpenStatistics = onOpenStatistics,
        )
    }
}
@Composable
fun CareerScreenContent(
    modifier: Modifier = Modifier,
    dashboardState: CareerDashboardUiState,
    examResultsState: ExamResultsUiState,
    debug: Boolean? = null,
    onChangeGradeStatus: (String, GradeStatus) -> Unit = {gradeId, newStatus -> },
    onOpenTranscript: () -> Unit = {},
    onOpenInvoke: () -> Unit = {},
    onOpenStatistics: () -> Unit = {},
) {
    val isCacheStale = dashboardState.isStale || examResultsState.isStale

    Scaffold(
        modifier = modifier,
        topBar = {
            GradientBackground(
                colors = listOf(
                    MaterialTheme.colorScheme.onPrimary,
                    LocalCustomColorsPalette.current.onCareerColor,
                ),
            ) {
                CreditsSection(
                    modifier = Modifier
                        .statusBarsPadding()
                        .padding(16.dp),
                    data = dashboardState.data,
                    isLoading = dashboardState.isLoading,
                    stateError = dashboardState.error,
                    debug = debug,
                    onClick = onOpenStatistics,
                    onOpenStatistics = onOpenStatistics, // TODO: remove this
                )
            }
        }
    ) { paddingValues ->
        // column??
        // Yes, column. Wdym?!
        Column(
            modifier = modifier
                .padding(paddingValues)
                .fillMaxSize()
        ) {
            Column(
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .weight(1f)
                    .verticalScroll(rememberScrollState()),
            ) {
                StaleCacheBanner(
                    modifier = Modifier.padding(top = 8.dp),
                    visible = isCacheStale,
                )

                Spacer(Modifier.height(16.dp))

                Text(
                    text = stringResource(Res.string.exam_results_title),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )

                Spacer(Modifier.height(6.dp))

                ExamResultsSection(
                    gradesList = examResultsState.results,
                    stateError = examResultsState.error,
                    isLoading = examResultsState.isLoading,
                    debug = debug,
                )

                Spacer(Modifier.height(16.dp))
            }

            Row(
                modifier = Modifier
                    .padding(horizontal = 12.dp)
                    .padding(bottom = 8.dp)
            ){
                StdIconTextButton(
                    modifier = Modifier
                        .weight(1f)
                        .height(80.dp),
                    icon = Icons.Default.Book,
                    text = stringResource(Res.string.transcript_title),
                    onClick = onOpenTranscript,
                    border = BorderStroke(
                        2.dp,
                        LocalCustomColorsPalette.current.careerColor,
                    )
                )

                Spacer(Modifier.width(8.dp))

                StdIconTextButton(
                    modifier = Modifier
                        .weight(1f)
                        .height(80.dp),
                    icon = Icons.Default.AccountBalance,
                    text = stringResource(Res.string.examsession_title),
                    onClick = onOpenInvoke,
                    border = BorderStroke(
                        2.dp,
                        LocalCustomColorsPalette.current.careerColor,
                    )
                )
            }
        }
    }
}

@Composable
fun CfooBar(
    modifier: Modifier = Modifier,
    currentCredits: Int = 0,
    totalCredits: Int = 180,
    onOpenStatistics: () -> Unit,
) {
    Column(
        modifier = Modifier
            .clickable(
                onClick = onOpenStatistics,
            )
            .then(modifier)
    ) {
        Text(
            text = "CFU TOTALI (Moh! Totaleee!!)",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
        )

        Spacer(Modifier.height(6.dp))

        StdSurface (
            modifier = Modifier
                .height(34.dp)
                .fillMaxWidth(),
            cornerRadius = 12.dp
        ) {
            Row {
                GradientBackground(
                    modifier = Modifier
                        .weight(currentCredits.toFloat())
                        .fillMaxHeight()
                        .background(Color.White),
                    colors = listOf(
                        MaterialTheme.colorScheme.onPrimary,
                        LocalCustomColorsPalette.current.careerColor.copy(alpha = 0.5f),
                    ),
                ) {}
                Spacer(
                    modifier = Modifier
                        .weight(totalCredits.toFloat() - currentCredits.toFloat())
                )
            }
        }

        Text(
            text = "60 / 180 CFU" // TODO: Trans
        )

        GoToStatisticsSection()
    }
}

@Composable
fun GoToStatisticsSection(
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .dropShadow(
                RoundedCornerShape(16.dp),
                shadow = ShadowDefaults.Text
            ),
        horizontalArrangement = Arrangement.End,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = stringResource(Res.string.open_credit_statistics),
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


@Composable
fun CreditsSection(
    modifier: Modifier = Modifier,
    data: DashboardData?,
    isLoading: Boolean,
    stateError: String?,
    debug: Boolean? = null,
    onClick:  () -> Unit = {},
    onOpenStatistics: () -> Unit,
) {
    val showDebug = debug ?: false && stateError != null

    val currentCredits: Int
    val totalPlannedCredits: Int
    val totalCredits: Int
    val yearlyProgress: Float
    val remainingCfu: String

//    if (data != null) {
        val stats = data?.stats

        currentCredits = stats?.creditsGained?.toInt() ?: -1
        totalPlannedCredits = stats?.totalPlanCredits ?: -1
        totalCredits = stats?.totalCredits ?: -1
        remainingCfu = when (stats) {
            null -> "--"
            else -> (totalCredits - currentCredits).toString()
        }
        yearlyProgress = when (stats) {
            null -> 0f
            else -> currentCredits.toFloat() / totalPlannedCredits.toFloat()
        }


    Column(
        modifier = Modifier
            .clickable(
                onClick = onClick,
            )
            .then(modifier),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                modifier = Modifier
                    .padding(horizontal = 8.dp)
                    .dropShadow(
                        RoundedCornerShape(16.dp),
                        shadow = ShadowDefaults.Text
                    ),
                text = stringResource(Res.string.credits_section_title),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
            )

            GoToStatisticsSection()
        }

        Spacer(Modifier.height(6.dp))

        Row(
            modifier = Modifier
                .height(IntrinsicSize.Min), // grow based on content
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            StdSurface(
                modifier = Modifier
                    .aspectRatio(1f)
                    .dropShadow(
                        RoundedCornerShape(16.dp),
                        shadow = ShadowDefaults.Surface
                    ),
                border = BorderStroke(
                    0.dp,
                    Color.Transparent
                ),
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.6f),
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    if (showDebug) {
                        ErrorItem(stateError)
                    } else { // !debug or !error
                        Box(
                            modifier = Modifier.padding(20.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CreditProgressIndicator(
                                modifier = Modifier.size(100.dp),
                                credits = currentCredits,
                                totalCredits = totalCredits,
                                plannedCredits = totalPlannedCredits,
                                progressColor = LocalCustomColorsPalette.current.careerColor,
                                plannedProgressColor = LocalCustomColorsPalette.current.careerColor,
                                backgroundTrackColor = MaterialTheme.colorScheme.secondaryContainer,
                            )
                        }
                    }
                }
            }

            Column(
                modifier = Modifier.weight(0.6f),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StdSurface(
                    modifier = Modifier
                        .dropShadow(
                            RoundedCornerShape(16.dp),
                            shadow = ShadowDefaults.Surface
                        ),
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.75f),
                    border = BorderStroke(0.dp, Color.Transparent),
                ) {
                    //TODO: add refresh button
                    if (debug ?: false && isLoading) {
                        ErrorItem("Loading")
                    } else if (debug ?: false && stateError != null) {
                        ErrorItem(stateError)
                    } else { // !debug or !error
                        YearlyProgressCard(
                            title = stringResource(Res.string.yearly_progress_title),
                            current = currentCredits,
                            total = totalPlannedCredits,
                            progress = yearlyProgress
                        )
                    }
                }
                StdSurface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(0.4f)
                        .dropShadow(
                            RoundedCornerShape(16.dp),
                            shadow = ShadowDefaults.Surface
                        ),
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.75f),
                    border = BorderStroke(1.dp, LocalCustomColorsPalette.current.careerColor)
                ) {
                    if (debug ?: false && (stateError != null || isLoading)) {
                        ErrorItem(stateError ?: "Loading")
                    } else {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = stringResource(
                                    Res.string.remaining_cfu_title,
                                    remainingCfu
                                ),
                                modifier = Modifier.padding(12.dp),
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Medium,
                                textAlign = TextAlign.Center,
                            )
                        }
                    }
                }
            }
        }
    }
}


@Composable
fun YearlyProgressCard( //TODO: move to components package
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
            color = LocalCustomColorsPalette.current.careerColor,
            trackColor = MaterialTheme.colorScheme.surfaceVariant,
            strokeCap = StrokeCap.Round
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "${if (current >= 0) current else "--"} / ${if (current >= 0) total else "--"} CFU", // TODO: is this any good?
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold
        )
    }
}





@Composable
@Preview(showBackground = true, uiMode = UI_MODE_NIGHT_YES)
fun CareerScreenPreview() {
    var previewState by remember {
        mutableStateOf(
            CareerScreenState(
                dashboardState = CareerDashboardUiState(
                    data = dashboardDataSample,
                    isLoading = false,
                    error = null
                ),
                examResultsState = ExamResultsUiState(
                    results = examResultsDataSample,
                    isLoading = false,
                    error = null
                )
            )
        )
    }

    val previewScope = rememberCoroutineScope()

    val mockActions = remember {
        ExamResultActions(
            changeGradeStatus = { id, newStatus ->
                previewScope.launch {
                    delay(600.milliseconds)
                    previewState = previewState.copy(
                        examResultsState = previewState.examResultsState.copy(
                            results = previewState.examResultsState.results.map { result ->
                                if (result.id == id) {
                                    result.copy(status = newStatus)
                                } else {
                                    result
                                }
                            }
                        )
                    )
                }
            }
        )
    }

    OpenUnitoTheme {
        Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
            CompositionLocalProvider(LocalExamResultActions provides mockActions) {
                CareerScreenContent(
                    dashboardState = previewState.dashboardState,
                    examResultsState = previewState.examResultsState,
                    modifier = Modifier.padding(innerPadding)
                )
            }
        }
    }
}