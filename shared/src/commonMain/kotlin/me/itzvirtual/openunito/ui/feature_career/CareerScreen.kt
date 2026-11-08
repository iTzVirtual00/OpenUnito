package me.itzvirtual.openunito.ui.feature_career


import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Book
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.AndroidUiModes.UI_MODE_NIGHT_YES
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.delay
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import me.itzvirtual.openunito.data.localization.withDateFormatter
import me.itzvirtual.openunito.domain.dashboard.DashboardData
import me.itzvirtual.openunito.domain.dashboard.dashboardDataSample
import me.itzvirtual.openunito.domain.examresult.ExamResult
import me.itzvirtual.openunito.domain.examresult.GradeStatus
import me.itzvirtual.openunito.domain.examresult.examResultsDataSample
import me.itzvirtual.openunito.resources.Res
import me.itzvirtual.openunito.resources.date
import me.itzvirtual.openunito.resources.editable_until
import me.itzvirtual.openunito.resources.exam_results_title
import me.itzvirtual.openunito.resources.examsession_title
import me.itzvirtual.openunito.resources.language_screen_title
import me.itzvirtual.openunito.resources.open_credit_statistics
import me.itzvirtual.openunito.resources.remaining_cfu_title
import me.itzvirtual.openunito.resources.transcript_title
import me.itzvirtual.openunito.resources.yearly_progress_title
import me.itzvirtual.openunito.ui.components.GradientBackground
import me.itzvirtual.openunito.ui.components.ListWrapper
import me.itzvirtual.openunito.ui.components.StdIconTextButton
import me.itzvirtual.openunito.ui.components.StdSurface
import me.itzvirtual.openunito.ui.shared.CreditProgressIndicator
import me.itzvirtual.openunito.ui.shared.ErrorItem
import me.itzvirtual.openunito.ui.shared.toColor
import me.itzvirtual.openunito.ui.shared.unShoutCourseName
import me.itzvirtual.openunito.ui.themes.OpenUnitoTheme
import me.itzvirtual.openunito.ui.feature_career.transcript_screen.GradeWrapper
import me.itzvirtual.openunito.ui.feature_career.transcript_screen.LocalTranscriptActions
import me.itzvirtual.openunito.ui.feature_career.transcript_screen.TranscriptActions
import me.itzvirtual.openunito.ui.feature_career.transcript_screen.TranscriptViewModel
import me.itzvirtual.openunito.ui.themes.LocalCustomColorsPalette
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

    val actions = TranscriptActions(
        changeGradeStatus = { gradeId, newStatus ->
            transcriptViewModel.changeGradeStatus(gradeId, newStatus)
        }
    )

    CompositionLocalProvider(LocalTranscriptActions provides actions) {
        CareerScreenContent(
            modifier = modifier.statusBarsPadding(),
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
    dashboardState: CareerDashboardUiState,
    examResultsState: ExamResultsUiState,
    modifier: Modifier = Modifier,
    debug: Boolean? = null,
    onChangeGradeStatus: (String, GradeStatus) -> Unit = {gradeId, newStatus -> },
    onOpenTranscript: () -> Unit = {},
    onOpenInvoke: () -> Unit = {},
    onOpenStatistics: () -> Unit = {},
) {

    Scaffold(
        modifier = modifier,
        topBar = {
//            GradientBackground(
//                colors = listOf(
//                    MaterialTheme.colorScheme.onPrimary,
//                    LocalCustomColorsPalette.current.languageColor.copy(alpha = 0.5f),
//                    // TODO: i don't like that this is half transparent, maybe lerp?
//                ),
//            ) {
                CfooBar(
                    modifier = Modifier
                        .statusBarsPadding()
                        .padding(horizontal = 16.dp)
                        .padding(top = 16.dp, bottom = 2.dp)
                        .fillMaxWidth(),
                    currentCredits = 60,
                    onOpenStatistics = onOpenStatistics,
                )
//            }
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
                    .padding(horizontal = 8.dp)
                    .weight(1f)
                    .verticalScroll(rememberScrollState()),
            ) {
                Spacer(modifier = Modifier.height(12.dp))

//            CreditsSection(
//                data = dashboardState.data,
//                isLoading = dashboardState.isLoading,
//                stateError = dashboardState.error,
//                debug = debug,
//                onOpenStatistics = onOpenStatistics, // TODO: remove this
//            )
//
//            Spacer(Modifier.height(16.dp))


                Spacer(Modifier.height(16.dp))

                Text(
                    text = stringResource(Res.string.exam_results_title),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )

                Spacer(Modifier.height(6.dp))

                ExamResultsSection(
                    results = examResultsState.results,
                    stateError = examResultsState.error,
                    isLoading = examResultsState.isLoading,
                    debug = debug,
                )
            }

            Row(
                modifier = Modifier
                    .padding(horizontal = 8.dp)
                    .padding(bottom = 6.dp)
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
                        Brush.linearGradient(
                            listOf(
                                LocalCustomColorsPalette.current.languageColor,
                                MaterialTheme.colorScheme.onPrimary,
                                lerp(
                                    LocalCustomColorsPalette.current.languageColor,
                                    MaterialTheme.colorScheme.onPrimary,
                                    0.5f,
                                ),
                            ),
                            start = Offset.Zero,
                            end = Offset.Infinite,
                        )
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
                        Brush.linearGradient(
                            listOf(
                                lerp(
                                    LocalCustomColorsPalette.current.languageColor,
                                    MaterialTheme.colorScheme.onPrimary,
                                    0.5f,
                                ),
                                MaterialTheme.colorScheme.onPrimary,
                                LocalCustomColorsPalette.current.languageColor,
                            ),
                            start = Offset.Zero,
                            end = Offset.Infinite,
                        )
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
                        LocalCustomColorsPalette.current.languageColor.copy(alpha = 0.5f),
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
fun GoToStatisticsSection() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 12.dp),
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
    data: DashboardData?,
    isLoading: Boolean,
    stateError: String?,
    debug: Boolean? = null,
    onOpenStatistics: () -> Unit,
) {
    val showDebug = debug ?: false && stateError != null

    val currentCredits: Int
    val totalPlannedCredits: Int
    val totalCredits: Int
    val yearlyProgress: Float
    val remainingCfu: String

    if (data != null) {
        val stats = data.stats

        currentCredits = stats.creditsGained.toInt()
        totalPlannedCredits = stats.totalPlanCredits
        totalCredits = stats.totalCredits
        remainingCfu = (totalCredits - currentCredits).toString()
        yearlyProgress = currentCredits.toFloat() / totalPlannedCredits.toFloat()
    } else {
        currentCredits = -1
        totalPlannedCredits = -1
        totalCredits = -1
        remainingCfu = "--"
        yearlyProgress = 0f
    }

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
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.4f),
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.tertiaryContainer,
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
                            textAlign = TextAlign.Center,

                            )
                    }
                }
            }
        }
    }

    GoToStatisticsSection()
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
            color = MaterialTheme.colorScheme.primary,
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
fun ExamResultsSection(
    modifier: Modifier = Modifier,
    results: List<ExamResult>,
    stateError: String?,
    isLoading: Boolean,
    debug: Boolean? = null,
) {
    val cornerRadius = 16.dp

    ListWrapper(
        modifier = modifier,
        isEmpty = results.isEmpty(),
        debug = debug,
        isLoading = isLoading,
        stateError = stateError
    ) {
        StdSurface(
            modifier = Modifier
                .height(90.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                results.forEachIndexed { idx, result ->
                    val shape = when {
                        results.size == 1 -> androidx.compose.foundation.shape.RoundedCornerShape(
                            cornerRadius
                        )

                        idx == 0 -> RoundedCornerShape(
                            topStart = cornerRadius,
                            topEnd = cornerRadius
                        )

                        idx == results.lastIndex -> RoundedCornerShape(
                            bottomStart = cornerRadius,
                            bottomEnd = cornerRadius
                        )

                        else -> RectangleShape
                    }
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = shape,
                        color = Color.Transparent // Uses parent surface color
                    ) {
                        ExamResultRow(result)
                    }
                    if (idx < results.lastIndex) {
                        HorizontalDivider(
                            modifier = Modifier.padding(end = 60.dp), // cool? not cool? idk
                            thickness = 1.dp,
                            color = MaterialTheme.colorScheme.outlineVariant
                        )
                    }
                }
            }
        }
    }
}


// TODO: hi Gabriele, cna u pwease UI -> <- ?
@Composable
fun ExamResultRow(result: ExamResult) {
    result.gradeResult!!

    val mainContentWeight = 0.95f
    val sideContentWeight = 1 - mainContentWeight

    var isGradeStatusLoading by remember { mutableStateOf(false) }
    val actions = LocalTranscriptActions.current
    val isGradeEditable =
        result.gradeResult.passed && (result.gradeResult.canAccept || result.gradeResult.canReject || result.status == GradeStatus.NOT_TOUCHED)
    val drawerEnabled = !isGradeStatusLoading && isGradeEditable
    var expanded by remember { mutableStateOf(false) }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min)
                .clickable(enabled = drawerEnabled) {
                    expanded = !expanded
                },
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .padding(end = 0.dp)
                    .weight(mainContentWeight)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    // info
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = unShoutCourseName(result.courseName),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            maxLines = 2
                        )

                        withDateFormatter {
                            Text(
                                text = "${
                                    stringResource(
                                        Res.string.date,
                                        result.date.formatDate()
                                    )
                                } \u2022 ${result.weight} CFU",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                    }

                    Spacer(modifier = Modifier.weight(0.1f))

                    // grade
                    GradeWrapper(result)
                }
                // editable until
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        text = stringResource(Res.string.editable_until),
                        style = MaterialTheme.typography.labelMedium,
                        color = if (isGradeEditable) MaterialTheme.colorScheme.primary else Color.Gray,
                    )
                    //                Spacer(modifier = Modifier.weight(1f))
                    Text(
                        text = result.editableUntil,
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.Gray,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            val showChroma = (
                    isGradeStatusLoading
                            || (result.status == GradeStatus.VIEWED
                            || result.status == GradeStatus.NOT_TOUCHED)
                    ) && isGradeEditable

            // when showChroma
            val infiniteTransition = rememberInfiniteTransition(label = "ChromaMovement")
            val chromaOffset by infiniteTransition.animateFloat(
                initialValue = 0f,
                targetValue = 2000f, // This distance determines the speed of the flow
                animationSpec = infiniteRepeatable(
                    animation = tween(3000, easing = LinearEasing),
                    repeatMode = RepeatMode.Restart
                ),
                label = "ChromaOffset"
            )
            // 2. when !showChroma
            val animatedColor by animateColorAsState(
                targetValue = if (result.gradeResult.passed) result.status.toColor() else GradeStatus.UNKNOWN.toColor(),
                animationSpec = tween(durationMillis = 500),
                label = "GradeStatusColor"
            )
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .weight(sideContentWeight)
                    .drawBehind {
                        if (showChroma) {
                            // ai-powered, no idea how this works. also, cool palette gemini!
                            val brush = Brush.linearGradient(
                                colors = listOf(
                                    Color(0xFF64B5F6), // Start: Blue
                                    Color(0xFFBA68C8), // Purple
                                    Color(0xFFF06292), // Pink
                                    Color(0xFF64B5F6)  // End: Blue (Same as start)
                                ),
                                // Start offset moves from -size to +size
                                start = Offset(0f, chromaOffset - 1000f),
                                // End offset stays a fixed distance away from start
                                end = Offset(0f, chromaOffset),
                                tileMode = TileMode.Repeated
                            )
                            drawRect(brush)
                        } else {
                            drawRect(animatedColor)
                        }
                    },
                contentAlignment = Alignment.Center
            ) {}
        }

        var selection by remember(result.status) { mutableStateOf(result.status) }
        if (selection == GradeStatus.UNKNOWN) selection = result.status
        var showDialog by remember { mutableStateOf(false) }
        val haptics = LocalHapticFeedback.current
        val scope = rememberCoroutineScope()

        // TODO: move this alert dialog to a higher level in the hierarchy,
        //  so that it can be reused for all the exam results,
        //  currently it's duplicated for each exam result, it doesn't sound good
        if (showDialog) {
            val selectionText = when (selection) {
                GradeStatus.ACCEPTED -> "You are choosing to ACCEPT this grade."
                GradeStatus.REJECTED -> "You are choosing to REJECT this grade."
                GradeStatus.VIEWED -> "You are choosing remove grade choice."
                GradeStatus.NOT_TOUCHED -> "You are choosing to remove grade choice (not touched)."
                else -> throw Exception("Invalid grade status selection")
            }
            AlertDialog(
                onDismissRequest = { showDialog = false },
                title = { Text(result.courseName) },
                text = { Text(selectionText) },
                confirmButton = {
                    TextButton(onClick = {
                        if (selection == GradeStatus.UNKNOWN) return@TextButton
                        showDialog = false
                        expanded = false // Close drawer
                        val selectionCopy = selection
                        selection = GradeStatus.UNKNOWN // Reset selection
                        isGradeStatusLoading = true
                        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                        scope.launch {
                            joinAll(
                                actions.changeGradeStatus(
                                    result.id,
                                    selectionCopy
                                )
                            )
                            isGradeStatusLoading = false
                            // TODO: I wanted to do the drilling-thing but don't know what to safely remove
                        }


                    }) { Text("Yes") }
                },
                dismissButton = {
                    TextButton(onClick = {
                        showDialog = false
                        expanded = false
                        selection = result.status
                    }) { Text("No") }
                }
            )
        }

        // Grade Status Drawer
        AnimatedVisibility(
            visible = expanded,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut(),
            modifier = Modifier.fillMaxWidth()
        ) {
            // overkill??
            val animatedColor by animateColorAsState(
                targetValue = when (selection) {
                    GradeStatus.ACCEPTED -> MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
                    GradeStatus.REJECTED -> MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.7f)
                    else -> Color.Transparent
                },
                animationSpec = tween(durationMillis = 500),
                label = "CircleColor"
            )

            val radiusProgress by animateFloatAsState(
                targetValue = when (selection) {
                    GradeStatus.ACCEPTED, GradeStatus.REJECTED -> 1f
                    else -> 0f
                },
                animationSpec = tween(durationMillis = 500),
                label = "RadiusProgress"
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
                    .clip(RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp))
                    .drawBehind {
                        drawRect(Color.LightGray.copy(alpha = 0.3f))

                        if (radiusProgress > 0f) {
                            drawCircle(
                                color = animatedColor,
                                radius = size.maxDimension * 1.5f * radiusProgress,
                                center = Offset(size.width / 2, size.height / 2)
                            )
                        }
                    }
                    .padding(16.dp)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                        val canAccept = result.gradeResult.canAccept
                        val canReject = result.gradeResult.canReject
                        val totalCount = (if (canAccept) 1 else 0) + (if (canReject) 1 else 0)
                        var currentIndex = 0
                        if (result.gradeResult.canAccept) {
                            val itemIndex = currentIndex++
                            SegmentedButton(
                                selected = selection == GradeStatus.ACCEPTED,
                                onClick = {
                                    selection =
                                        if (selection == GradeStatus.ACCEPTED) GradeStatus.NOT_TOUCHED else GradeStatus.ACCEPTED
                                },
                                shape = SegmentedButtonDefaults.itemShape(
                                    index = itemIndex,
                                    count = totalCount
                                )
                            ) { Text("ACCEPT") }
                        }

                        if (result.gradeResult.canReject) {
                            val itemIndex = currentIndex++
                            SegmentedButton(
                                selected = selection == GradeStatus.REJECTED,
                                onClick = {
                                    selection =
                                        if (selection == GradeStatus.REJECTED) GradeStatus.NOT_TOUCHED else GradeStatus.REJECTED
                                },
                                shape = SegmentedButtonDefaults.itemShape(
                                    index = itemIndex,
                                    count = totalCount
                                )
                            ) { Text("REJECT") }
                        }
                    }

                    // Confirmation Trigger (Only shows if selection is different from current status)
                    if (selection != result.status) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = { showDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Text("Confirm Selection")
                        }
                    }
                }
            }
        }
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
        TranscriptActions(
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
            CompositionLocalProvider(LocalTranscriptActions provides mockActions) {
                CareerScreenContent(
                    dashboardState = previewState.dashboardState,
                    examResultsState = previewState.examResultsState,
                    modifier = Modifier.padding(innerPadding)
                )
            }
        }
    }
}