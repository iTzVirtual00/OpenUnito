package me.itzvirtual.myunitox.ui.dashboard

import android.content.res.Configuration
import android.util.Log
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
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.runtime.compositionLocalOf
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
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.Job
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import me.itzvirtual.myunitox.R
import me.itzvirtual.myunitox.data.models.ExamGrade
import me.itzvirtual.myunitox.data.models.ExamResult
import me.itzvirtual.myunitox.data.models.GradeResult
import me.itzvirtual.myunitox.data.models.GradeStatus
import me.itzvirtual.myunitox.data.models.Score
import me.itzvirtual.myunitox.data.models.dashboardDataSample
import me.itzvirtual.myunitox.ui.themes.MyUnitoXTheme
import me.itzvirtual.myunitox.ui.themes.main.LocalCustomColorsPalette
import me.itzvirtual.myunitox.ui.util.AnimatedSymmetricalBorder
import me.itzvirtual.myunitox.ui.util.ErrorItem
import me.itzvirtual.myunitox.ui.util.LoadingIconItem
import me.itzvirtual.myunitox.ui.util.LoadingShimmerItem
import me.itzvirtual.myunitox.ui.util.formatDate
import me.itzvirtual.myunitox.ui.util.parseDate
import me.itzvirtual.myunitox.ui.util.shimmerEffect
import me.itzvirtual.myunitox.ui.util.toColor


@Composable
@Preview(locale = "it", uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Preview(showBackground = true)
fun DashboardScreenPreview() {
    MyUnitoXTheme {
        Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
            DashboardScreen(
                state = DashboardUiState(
                    dashboardData = DashboardData(
                        dashboardData = dashboardDataSample,
                        isLoading = false,
                        errorMessage = null
                    ),
                    examResultsData = ExamResultData(
                        examResults = listOf(
                            ExamResult(
                                "placeholder1",
                                "PROGRAMMAZIONE I",
                                "0801L31",
                                "22/01/2026",
                                9,
                                "07/02/2026 23:59:59",
                                "30",
                                GradeStatus.VIEWED,
                                GradeResult(
                                    passed = false,
                                    grade = "0",
                                    isAbsent = false,
                                    isWithdrawn = false,
                                    canAccept = false,
                                    canReject = false,
                                ),
                            ),
                            ExamResult(
                                "placeholder1",
                                "PROGRAMMAZIONE I",
                                "0801L31",
                                "22/01/2026",
                                9,
                                "07/02/2026 23:59:59",
                                "30",
                                GradeStatus.VIEWED,
                                GradeResult(
                                    passed = false,
                                    grade = "10",
                                    isAbsent = false,
                                    isWithdrawn = false,
                                    canAccept = false,
                                    canReject = false,
                                ),
                            ),
                            ExamResult(
                                "placeholder1",
                                "PROGRAMMAZIONE I",
                                "0801L31",
                                "22/01/2026",
                                9,
                                "07/02/2026 23:59:59",
                                "30",
                                GradeStatus.VIEWED,
                                GradeResult(
                                    passed = true,
                                    grade = "30L",
                                    isAbsent = false,
                                    isWithdrawn = false,
                                    canAccept = false,
                                    canReject = false,
                                ),
                            ),
                        ),
                        isLoading = false,
                        errorMessage = null
                    ),
                    examGradesData = ExamGradeData(
                        grades = listOf(
                            ExamGrade(
                                "1",
                                "Programmazione I",
                                Score(parseDate("22/01/2026"), "30", true),
                                "Obbligatorio",
                                "S",
                                2025,
                                "2025",
                                "0801L31-25",
                                "INFORMATICA",
                                0,
                                "12"
                            ),
                            ExamGrade(
                                "1",
                                "Programmazione I",
                                Score(parseDate("22/01/2026"), "30", true),
                                "Obbligatorio",
                                "S",
                                2025,
                                "2025",
                                "0801L31-25",
                                "INFORMATICA",
                                0,
                                "12"
                            ),
                            ExamGrade(
                                "1",
                                "Programmazione I",
                                Score(parseDate("22/01/2026"), "30", true),
                                "Obbligatorio",
                                "S",
                                2025,
                                "2025",
                                "0801L31-25",
                                "INFORMATICA",
                                0,
                                "12"
                            ),
                        ),
                        isLoading = false,
                        errorMessage = null
                    ),
                    isLoading = false,
                ),
                modifier = Modifier.padding(innerPadding)
            )
        }
    }
}

data class DashboardActions(
    val changeGradeStatus: (String, GradeStatus) -> Job = { gradeId, newStatus ->
        Log.i("DashboardActions", "Grade with id $gradeId clicked, new status: $newStatus")
        Job() // placeholder
    },
)

val LocalDashboardActions = compositionLocalOf { DashboardActions() }


@Composable
fun DashboardScreen(viewModel: DashboardViewModel = hiltViewModel(), modifier: Modifier) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
//    val scope = rememberCoroutineScope()
    val actions = DashboardActions(
        changeGradeStatus = { gradeId, newStatus ->
            Log.i("DashboardActions", "Grade with id $gradeId clicked, new status: $newStatus")
            return@DashboardActions viewModel.changeGradeStatus(gradeId, newStatus, false)
//            return@DashboardActions scope.launch { delay(3000) }
        }
    )

    CompositionLocalProvider(LocalDashboardActions provides actions) {
        DashboardScreen(
            state = state,
            modifier = modifier
        )
    }

}

@Composable
fun DashboardScreen(state: DashboardUiState, modifier: Modifier) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item(key="test") {
            Text(
                text = stringResource(R.string.credits_section_title),
                modifier = Modifier.padding(16.dp),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            CreditsSection(state.dashboardData)
        }
        item {
            Text(
                text = stringResource(R.string.exam_results_title),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(16.dp)
            )
            ExamResultsSection(state.examResultsData)
        }
        item {
            Text(
                text = stringResource(R.string.grades_section_title),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(16.dp)
            )
            GradesSection(state.examGradesData)
        }
        item {
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun ExamResultsSection(state: ExamResultData) {
    val results = state.examResults
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
            } else if (state.errorMessage != null) {
                ErrorItem(state.errorMessage)
            } else if (results.count() == 0) {
                ErrorItem(stringResource(R.string.no_data), Color.Gray)
            } else {
                results.forEachIndexed { idx, result ->
                    val shape = when {
                        results.size == 1 -> RoundedCornerShape(cornerRadius)
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
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
fun ExamResultRow(result: ExamResult) {
    result.gradeResult!!

    val mainContent = 0.95f
    val sideContent = 1 - mainContent

    var isGradeStatusLoading by remember { mutableStateOf(false) }
    val actions = LocalDashboardActions.current
    val isGradeEditable = result.gradeResult.canAccept || result.gradeResult.canReject || result.status == GradeStatus.NOT_TOUCHED
    val drawerEnabled = !isGradeStatusLoading && isGradeEditable
    var expanded by remember { mutableStateOf(false) }
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxSize()
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
                    .weight(mainContent)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
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
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        text = stringResource(R.string.editable_until),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary
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


            val showChroma = (isGradeStatusLoading || result.status == GradeStatus.VIEWED || result.status == GradeStatus.NOT_TOUCHED) && isGradeEditable
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
                modifier = Modifier
                    .fillMaxHeight()
                    .weight(sideContent)
                    .drawBehind {
                        if (showChroma) {3
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
        if(selection == GradeStatus.UNKNOWN) selection = result.status
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
                        if(selection == GradeStatus.UNKNOWN) return@TextButton
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
                targetValue = when(selection) {
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
//                    // Track the confirmed state (to handle the Apply button's enabled state)
//                    var lastAppliedSelection by remember { mutableStateOf("ACCEPT") }
//                    val options = listOf("Accept", "Wait", "Reject")
//                    val unCheckedIcons = listOf(Icons.Outlined.Check, null, Icons.Filled.Clear)
//                    val checkedIcons = listOf(Icons.Filled.Check, null, Icons.Filled.Clear)
//                    var selectedIndex by remember { mutableIntStateOf(0) }
//
//
//                    Row(
//                        modifier = Modifier
//                            .fillMaxWidth()
//                            .safeDrawingPadding()
//                            .padding(horizontal = 8.dp),
//                        verticalAlignment = Alignment.CenterVertically // Ensures they are perfectly in line
//                    ) {
//                        // 1. Your existing Segmented Group logic
//                        Row(
//                            modifier = Modifier.weight(1f), // Group takes available space but allows Spacer to push
//                            horizontalArrangement = Arrangement.spacedBy(ButtonGroupDefaults.ConnectedSpaceBetween),
//                        ) {
//                            options.forEachIndexed { index, label ->
//                                ToggleButton(
//                                    modifier = Modifier
//                                        .weight(1f)
//                                        .semantics { role = Role.RadioButton },
//                                    checked = selectedIndex == index,
//                                    onCheckedChange = {
//                                        selectedIndex = index
//                                        selection = when (index) {
//                                            0 -> "ACCEPT"
//                                            1 -> "WAIT"
//                                            2 -> "REJECT"
//                                            else -> "ACCEPT"
//                                        }
//                                    },
//                                    shapes = when (index) {
//                                        0 -> ButtonGroupDefaults.connectedLeadingButtonShapes()
//                                        options.lastIndex -> ButtonGroupDefaults.connectedTrailingButtonShapes()
//                                        else -> ButtonGroupDefaults.connectedMiddleButtonShapes()
//                                    },
//                                ) {
//                                    if (checkedIcons[index] != null || unCheckedIcons[index] != null) {
//                                        Icon(
//                                            imageVector = (if (selectedIndex == index) checkedIcons[index] else unCheckedIcons[index])!!,
//                                            contentDescription = null
//                                        )
//                                        Spacer(Modifier.size(ToggleButtonDefaults.IconSpacing))
//                                    }
//                                    Text(label)
//                                }
//                            }
//                        }
//
//                        // 2. The Spacer that pushes the next item to the right
//                        // Note: We use a small static width if we want a gap, or weight if we want the right edge
//                        Spacer(Modifier.width(16.dp))
//
//                        // 3. The Circular Apply Button
//                        Button(
//                            onClick = {
//                                if (selection == null) return@Button
//                                lastAppliedSelection = selection!!
//                            },
//                            // Disables if visual selection matches the last confirmed state
//                            enabled = selection != lastAppliedSelection,
//                            modifier = Modifier.size(56.dp), // Standard FAB-like size for a circle
//                            shape = CircleShape,
//                            contentPadding = PaddingValues(0.dp)
//                        ) {
//                            Icon(Icons.Default.Check, contentDescription = "Apply")
//                        }
//                    }
                    SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                        val canAccept = result.gradeResult.canAccept
                        val canReject = result.gradeResult.canReject
                        val totalCount = (if (canAccept) 1 else 0) + (if (canReject) 1 else 0)
                        var currentIndex = 0
                        if (result.gradeResult.canAccept) {
                            val itemIndex = currentIndex++
                            SegmentedButton(
                                selected = selection == GradeStatus.ACCEPTED,
                                onClick = { selection = if(selection==GradeStatus.ACCEPTED) GradeStatus.NOT_TOUCHED else GradeStatus.ACCEPTED },
                                shape = SegmentedButtonDefaults.itemShape(index = itemIndex, count = totalCount)
                            ) { Text("ACCEPT") }
                        }

                        if (result.gradeResult.canReject) {
                            val itemIndex = currentIndex++
                            SegmentedButton(
                                selected = selection == GradeStatus.REJECTED,
                                onClick = { selection = if(selection==GradeStatus.REJECTED) GradeStatus.NOT_TOUCHED else GradeStatus.REJECTED },
                                shape = SegmentedButtonDefaults.itemShape(index = itemIndex, count = totalCount)
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
fun GradeWrapper(result: ExamResult) {
    result.gradeResult!!

    val haptics = LocalHapticFeedback.current
    val cornerRadius = 8.dp
    val actions = LocalDashboardActions.current
    val scope = rememberCoroutineScope()


    val isRevealed = result.status != GradeStatus.NOT_TOUCHED
    var isLoading by remember { mutableStateOf(false) }
    AnimatedSymmetricalBorder(
        isRevealed = isRevealed,
        modifier = Modifier.padding(0.dp),
        pathCornerRadius = cornerRadius,
        lineColor = MaterialTheme.colorScheme.tertiary,
    ) {
        Box {
            Column(
                horizontalAlignment = Alignment.End,
                modifier = Modifier.clip(RoundedCornerShape(cornerRadius)).clickable(
                    enabled = !isRevealed && !isLoading,
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) {
                    isLoading = true
                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                    scope.launch {
                        joinAll(actions.changeGradeStatus(result.id, GradeStatus.VIEWED))
                        isLoading = false
                    }
                }
                    .let {
                        when (result.status) {
                            GradeStatus.NOT_TOUCHED -> if (!isRevealed) it.shimmerEffect(
                                Color.Gray
                            ) else it

                            else -> it
                        }
                    }

            ) {
                Text(
                    text = result.gradeResult.run {
                        when {
                            !passed && grade=="0" -> "X"
                            laude -> "${grade}L"
                            else -> grade
                        }
                    },
                    style = MaterialTheme.typography.headlineMedium,
                    color = when {
                        result.status == GradeStatus.NOT_TOUCHED -> Color.Transparent
                        !result.gradeResult.passed -> LocalCustomColorsPalette.current.gradeNotPassed
                        else -> LocalCustomColorsPalette.current.gradePassed
                    },
                    fontWeight = FontWeight.ExtraBold
                )
                if (result.maxGrade.isNotBlank() && result.maxGrade != "30") {
                    Text(
                        text = "/ ${result.maxGrade}",
                        style = MaterialTheme.typography.labelMedium
                    )
                }
            }
            // loading animation
            if (isLoading && !isRevealed) {
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.tertiary
                    )
                }
            }
        }
    }
}

@Composable
fun GradesSection(state: ExamGradeData) {
    val grades = state.grades.filter { it.score != null }
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
        ) {
            if (state.isLoading) {
                LoadingShimmerItem()
            } else if (state.errorMessage != null) {
                ErrorItem(state.errorMessage)
            } else if (grades.count() == 0) {
                ErrorItem(stringResource(R.string.no_data), Color.Gray)
            } else {
                grades.forEachIndexed { idx, grade ->
                    GradeRow(
                        courseName = grade.courseName,
                        date = formatDate(grade.score!!.date),
                        cfu = grade.weight,
                        grade = grade.score.value.let {
                            return@let when {
                                grade.score.value == "0" -> grade.status
                                grade.score.laude -> "${it}L"
                                else -> it
                            }
                        }
                    )

                    if (idx < grades.lastIndex) {
                        HorizontalDivider(
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
fun GradeRow(courseName: String, grade: String, date: String, cfu: String) {
    Column(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxWidth(),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // info
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = courseName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 2
                )

                Text(
                    text = stringResource(
                        R.string.date,
                        date
                    ) + if (cfu.isNotBlank() && cfu != "0") " \u2022 $cfu CFU" else "",
                    style = MaterialTheme.typography.bodySmall
                )
                Spacer(modifier = Modifier.height(4.dp))

            }

            Spacer(modifier = Modifier.weight(0.1f))

            // grade
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = grade,
                    style = MaterialTheme.typography.headlineMedium,
                    color = LocalCustomColorsPalette.current.gradePassed,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}


@Composable
fun CreditsSection(state: DashboardData) {
    val data = state.dashboardData
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
            color = MaterialTheme.colorScheme.surfaceColorAtElevation(2.dp)
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                if (state.isLoading) {
                    LoadingIconItem(Modifier.height(72.dp))
                } else if (state.errorMessage != null) {
                    ErrorItem(state.errorMessage)
                } else {
                    data!!
                    val stats = state.dashboardData.stats
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
                } else if (state.errorMessage != null) {
                    ErrorItem(state.errorMessage)
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
                } else if (state.errorMessage != null) {
                    ErrorItem(state.errorMessage)
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


