package me.itzvirtual.myunitox.ui.feature_career

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
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
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
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import me.itzvirtual.myunitox.R
import me.itzvirtual.myunitox.domain.dashboard.dashboardDataSample
import me.itzvirtual.myunitox.domain.examresult.ExamResult
import me.itzvirtual.myunitox.domain.examresult.GradeStatus
import me.itzvirtual.myunitox.ui.shared.CreditProgressIndicator
import me.itzvirtual.myunitox.ui.shared.ErrorItem
import me.itzvirtual.myunitox.ui.shared.LoadingIconItem
import me.itzvirtual.myunitox.ui.shared.LoadingShimmerItem
import me.itzvirtual.myunitox.ui.shared.toColor
import me.itzvirtual.myunitox.ui.themes.MyUnitoXTheme
import me.itzvirtual.myunitox.ui.transcript.GradeWrapper
import me.itzvirtual.myunitox.ui.transcript.LocalTranscriptActions
import me.itzvirtual.myunitox.ui.transcript.TranscriptSheet

//TODO: add new grades, add transcript and invokes buttons
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
    var showTranscript by remember { mutableStateOf(false) }
    var showInvoke by remember { mutableStateOf(false) }


    CareerScreenContent(
        state,
        modifier,
        onClickStatistics = { showStatistics = true },
        onClickTranscript = { showTranscript = true },
        onClickInvoke = { showInvoke = true },

    )

    //Callback handling
    if (showStatistics) {
        TranscriptSheet(onDismiss = { showStatistics = false }) //TODO: change to Statistic fun
    }
    if (showTranscript) {
        TranscriptSheet(onDismiss = { showTranscript = false })
    }
    if (showInvoke) {
        TranscriptSheet(onDismiss = { showInvoke = false }) //TODO: change to Invoke fun
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
    Column(modifier = modifier) {
        Column() {
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = modifier.clickable {
                    onClickStatistics()
                }
            ) {
                CreditsSection(
                    state = state.dashboardState
                )
            }
            GoToTranscriptSection()
            Spacer(Modifier.weight(0.1f))
            ExamResultsSection(state = state.examResulState)
            Spacer(Modifier.weight(0.1f))
            Row(
                Modifier.weight(1f)
                    .padding(Dp(10f))
            ) {
                Button(
                    onClick = {
                        onClickTranscript()
                    },
                    modifier = modifier.weight(1f)
                        .aspectRatio(1f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Libretto")
                }
                Spacer(Modifier.width(Dp(10f)))
                Button(
                    onClick = {
                        onClickInvoke()
                    },
                    modifier = modifier.weight(1f)
                        .aspectRatio(1f),
                    shape = RoundedCornerShape(12.dp),
                    //border = borderStroke
                ) {
                    Text("Appelli")
                }
            }

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
                    //TODO: add refresh button and show error numbers only in dev mode
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



@Composable
fun ExamResultsSection(state: ExamResultsUiState) {
    val results = state.results
    val cornerRadius = 16.dp

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        shape = RoundedCornerShape(cornerRadius),
        color = MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
        ) {
            if (state.isLoading) {
                LoadingShimmerItem()
            } else if (state.error != null) {
                ErrorItem(state.error!!)
            } else if (results.count() == 0) {
                ErrorItem(stringResource(R.string.no_data), Color.Companion.Gray)
            } else {
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
//                    ExamResultRow(result)
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = shape,
                        color = Color.Companion.Transparent // Uses parent surface color
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
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
fun ExamResultRow(result: ExamResult) {
    result.gradeResult!!

    val mainContent = 0.95f
    val sideContent = 1 - mainContent

    var isGradeStatusLoading by remember { mutableStateOf(false) }
    val actions = LocalTranscriptActions.current
    val isGradeEditable =
        result.gradeResult.canAccept || result.gradeResult.canReject || result.status == GradeStatus.NOT_TOUCHED
    val drawerEnabled = !isGradeStatusLoading && isGradeEditable
    var expanded by remember { mutableStateOf(false) }
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.Companion
                .fillMaxSize()
                .height(IntrinsicSize.Min)
                .clickable(enabled = drawerEnabled) {
                    expanded = !expanded
                },
            verticalAlignment = Alignment.Companion.CenterVertically
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .padding(end = 0.dp)
                    .weight(mainContent)
            ) {
                Row(
                    verticalAlignment = Alignment.Companion.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    // info
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = result.courseName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            maxLines = 2
                        )

                        Text(
                            text = "${
                                stringResource(
                                    R.string.date,
                                    result.date
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


            val showChroma =
                (isGradeStatusLoading || result.status == GradeStatus.VIEWED || result.status == GradeStatus.NOT_TOUCHED) && isGradeEditable
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
                targetValue = result.status.toColor(),
                animationSpec = tween(durationMillis = 500),
                label = "GradeStatusColor"
            )



            Box(
                modifier = Modifier.Companion
                    .fillMaxHeight()
                    .weight(sideContent)
                    .drawBehind {
                        if (showChroma) {
                            // ai-powered, no idea how this works. also, cool palette gemini!
                            val brush = Brush.Companion.linearGradient(
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
                                tileMode = TileMode.Companion.Repeated
                            )
                            drawRect(brush)
                        } else {
                            drawRect(animatedColor)
                        }
                    },
                contentAlignment = Alignment.Companion.Center
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
                        haptics.performHapticFeedback(HapticFeedbackType.Companion.LongPress)
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
                    else -> Color.Companion.Transparent
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
                        drawRect(Color.Companion.LightGray.copy(alpha = 0.3f))

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
                Column(horizontalAlignment = Alignment.Companion.CenterHorizontally) {
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
