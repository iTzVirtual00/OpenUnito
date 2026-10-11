package me.itzvirtual.myunitox.ui.feature_career

import android.content.res.Configuration
import android.widget.Toast
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
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Book
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.delay
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import me.itzvirtual.myunitox.R
import me.itzvirtual.myunitox.domain.dashboard.DashboardData
import me.itzvirtual.myunitox.domain.dashboard.dashboardDataSample
import me.itzvirtual.myunitox.domain.examresult.ExamResult
import me.itzvirtual.myunitox.domain.examresult.GradeStatus
import me.itzvirtual.myunitox.domain.examresult.examResultsDataSample
import me.itzvirtual.myunitox.ui.components.ListWrapper
import me.itzvirtual.myunitox.ui.components.StdIconTextButton
import me.itzvirtual.myunitox.ui.components.StdSurface
import me.itzvirtual.myunitox.ui.shared.CreditProgressIndicator
import me.itzvirtual.myunitox.ui.shared.ErrorItem
import me.itzvirtual.myunitox.ui.shared.formatDate
import me.itzvirtual.myunitox.ui.shared.toColor
import me.itzvirtual.myunitox.ui.shared.unShoutCourseName
import me.itzvirtual.myunitox.ui.themes.MyUnitoXTheme
import me.itzvirtual.myunitox.ui.transcript.GradeWrapper
import me.itzvirtual.myunitox.ui.transcript.LocalTranscriptActions
import me.itzvirtual.myunitox.ui.transcript.TranscriptActions
import me.itzvirtual.myunitox.ui.transcript.TranscriptSheet
import me.itzvirtual.myunitox.ui.transcript.TranscriptViewModel
import kotlin.time.Duration.Companion.milliseconds

@Composable
fun CareerScreen(
    viewModel: CareerViewModel = hiltViewModel(),
    transcriptViewModel: TranscriptViewModel = hiltViewModel(), // this prefetches the data
    modifier: Modifier
) {
    val state by viewModel.screenState.collectAsStateWithLifecycle()
    var showStatistics by remember { mutableStateOf(false) }
    var showTranscript by remember { mutableStateOf(false) }
    var showInvoke by remember { mutableStateOf(false) }
    val context = LocalContext.current

    val actions = TranscriptActions(
        changeGradeStatus = { gradeId, newStatus ->
            transcriptViewModel.changeGradeStatus(gradeId, newStatus)
        }
    )

    CompositionLocalProvider(LocalTranscriptActions provides actions) {
        CareerScreenContent(
            state,
            modifier,
            onClickStatistics = { showStatistics = true },
            onClickTranscript = { showTranscript = true },
            onClickInvoke = { showInvoke = true },
        )
    }

    // Callback handling
    if (showStatistics) {
        //TODO: change to Statistic fun
        Toast.makeText(
            context,
            "WIP",
            Toast.LENGTH_SHORT
        ).show()
        showStatistics = false
    }
    if (showTranscript) {
        TranscriptSheet(transcriptViewModel, onDismiss = { showTranscript = false })
    }
    if (showInvoke) {
        //TODO: change to Invokes fun
        Toast.makeText(
            context,
            "WIP",
            Toast.LENGTH_SHORT
        ).show()
        showInvoke = false
    }
}

@Composable
fun CareerScreenContent(
    state: CareerScreenState,
    modifier: Modifier = Modifier,
    onClickStatistics: () -> Unit = {},
    onClickTranscript: () -> Unit = {},
    onClickInvoke: () -> Unit = {},
) {
    // column??
    // Yes, column. Wdym?!
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
    ) {
        Column(
            modifier = modifier
                .clickable {
                    onClickStatistics()
                }
                .fillMaxWidth()
        ) {
            Spacer(modifier = Modifier.height(8.dp))
            CreditsSection(
                data = state.dashboardState.data,
                isLoading = state.dashboardState.isLoading,
                stateError = state.dashboardState.error,
                debug = state.debug
            )
        }
        Spacer(Modifier.height(16.dp))
        StdIconTextButton(
            icon = Icons.Default.Book,
            text = "Libretto",
            onClick = onClickTranscript,
            modifier = Modifier.padding(8.dp, 0.dp),
            height = 80f,
            border = BorderStroke(2.dp, MaterialTheme.colorScheme.outlineVariant)
        )
        Spacer(Modifier.height(8.dp))
        StdIconTextButton(
            icon = Icons.Default.AccountBalance,
            text = "Appelli",
            onClick = onClickInvoke,
            modifier = Modifier.padding(8.dp, 0.dp),
            height = 80f,
            border = BorderStroke(2.dp, MaterialTheme.colorScheme.outlineVariant)
        )
        Spacer(Modifier.height(16.dp))
        Text(
            text = stringResource(R.string.exam_results_title),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        ExamResultsSection(
            results = state.examResultsState.results,
            stateError = state.examResultsState.error,
            isLoading = state.examResultsState.isLoading,
            debug = state.debug,
        )
    }
}

@Composable
fun GoToCreditStatisticsSection() {
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
                text = stringResource(R.string.open_credit_statistics),
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
fun CreditsSection(
    data: DashboardData?,
    isLoading: Boolean,
    stateError: String?,
    debug: Boolean = false,
) {
    val showDebug = debug && stateError != null

    val currentCredits: Int
    val totalPlannedCredits: Int
    val totalCredits: Int
    val yearlyProgress: Float
    val remainingCfu: String

    if (!isLoading && stateError == null) {
        val stats = data!!.stats

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
                if (debug && isLoading) {
                    ErrorItem("Loading")
                } else if (debug && stateError != null) {
                    ErrorItem(stateError)
                } else { // !debug or !error
                    YearlyProgressCard(
                        title = stringResource(R.string.yearly_progress_title),
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
                if (debug && (stateError != null || isLoading)) {
                    ErrorItem(stateError ?: "Loading")
                } else {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = stringResource(
                                R.string.remaining_cfu_title,
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
    GoToCreditStatisticsSection() //TODO: replace with statistics
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
    results: List<ExamResult>,
    stateError: String?,
    isLoading: Boolean,
    debug: Boolean = false,
) {
    val cornerRadius = 16.dp

    ListWrapper(
        results,
        debug = debug,
        isLoading = isLoading,
        stateError = stateError
    ) {
        StdSurface {
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

                        Text(
                            text = "${
                                stringResource(
                                    R.string.date,
                                    formatDate(result.date)
                                )
                            } \u2022 ${result.weight} CFU",
                            style = MaterialTheme.typography.bodySmall
                        )
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
                        text = stringResource(R.string.editable_until),
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
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
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

    MyUnitoXTheme {
        Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
            CompositionLocalProvider(LocalTranscriptActions provides mockActions) {
                CareerScreenContent(
                    state = previewState,
                    modifier = Modifier.padding(innerPadding)
                )
            }
        }
    }
}