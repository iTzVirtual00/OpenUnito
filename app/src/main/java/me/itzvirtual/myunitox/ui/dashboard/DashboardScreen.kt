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
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
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
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import me.itzvirtual.myunitox.R
import me.itzvirtual.myunitox.data.models.ActivityType
import me.itzvirtual.myunitox.data.models.AgendaEntry
import me.itzvirtual.myunitox.data.models.Building
import me.itzvirtual.myunitox.data.models.ExamGrade
import me.itzvirtual.myunitox.data.models.ExamResult
import me.itzvirtual.myunitox.data.models.GradeResult
import me.itzvirtual.myunitox.data.models.GradeStatus
import me.itzvirtual.myunitox.data.models.Professor
import me.itzvirtual.myunitox.data.models.Room
import me.itzvirtual.myunitox.data.models.Score
import me.itzvirtual.myunitox.data.models.dashboardDataSample
import me.itzvirtual.myunitox.data.models.StudentCareer
import me.itzvirtual.myunitox.data.models.StudentData
import me.itzvirtual.myunitox.data.models.sampleStudentData
import me.itzvirtual.myunitox.ui.themes.LocalCustomColorsPalette
import me.itzvirtual.myunitox.ui.themes.MyUnitoXTheme
import me.itzvirtual.myunitox.ui.util.AnimatedSymmetricalBorder
import me.itzvirtual.myunitox.ui.util.ErrorItem
import me.itzvirtual.myunitox.ui.util.LoadingIconItem
import me.itzvirtual.myunitox.ui.util.LoadingShimmerItem
import me.itzvirtual.myunitox.ui.util.formatDate
import me.itzvirtual.myunitox.ui.util.parseDate
import me.itzvirtual.myunitox.ui.util.shimmerEffect
import me.itzvirtual.myunitox.ui.util.toClipEntry
import me.itzvirtual.myunitox.ui.util.toColor
import java.time.Instant
import java.util.Date


@Composable
@Preview(locale = "it", uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Preview(showBackground = true)
fun DashboardScreenPreview() {
    MyUnitoXTheme {
        Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
            DashboardScreen(
                state = DashboardUiState(
                    studentInfo = StudentInfo(
                        studentData = sampleStudentData,
                        isLoading = false,
                        errorMessage = null
                    ),
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
                    nearestAgenda = NearestAgendaData(
                        entry = AgendaEntry(
                            id = "1",
                            title = "Programmazione II",
                            startDateTime = java.time.Instant.now().minusSeconds(1800), // Started 30 min ago
                            endDateTime = java.time.Instant.now().plusSeconds(3600), // Ends in 1 hour
                            activityCode = "INF0123",
                            courseYear = 1,
                            status = "P",
                            rooms = listOf(
                                Room(
                                    id = "r1",
                                    code = "AULA_A",
                                    name = "Aula A",
                                    building = Building(
                                        id = "b1",
                                        name = "Dip. Informatica"
                                    )
                                )
                            ),
                            professors = listOf(
                                Professor("p1", "Mario", "Rossi")
                            ),
                            activityType = ActivityType("LEZ", "Lezione")
                        ),
                        isInProgress = true,
                        isLoading = false
                    ),
                    nextDayPlan = NextDayPlanData(
                        entries = listOf(
                            AgendaEntry(
                                id = "1",
                                title = "Programmazione II",
                                startDateTime = java.time.Instant.parse("2026-02-22T09:00:00Z"),
                                endDateTime = java.time.Instant.parse("2026-02-22T11:00:00Z"),
                                activityCode = "INF0123",
                                courseYear = 1,
                                status = "P",
                                rooms = listOf(
                                    Room(
                                        id = "r1",
                                        code = "AULA_A",
                                        name = "Aula A",
                                        building = Building(
                                            id = "b1",
                                            name = "Dip. Informatica"
                                        )
                                    )
                                ),
                                professors = listOf(
                                    Professor("p1", "Mario", "Rossi")
                                ),
                                activityType = ActivityType("LEZ", "Lezione")
                            ),
                            AgendaEntry(
                                id = "2",
                                title = "Analisi Matematica",
                                startDateTime = Instant.parse("2026-02-22T14:00:00Z"),
                                endDateTime = Instant.parse("2026-02-22T16:00:00Z"),
                                activityCode = "MAT0456",
                                courseYear = 1,
                                status = "P",
                                rooms = listOf(
                                    Room(
                                        id = "r2",
                                        code = "AULA_B",
                                        name = "Aula B",
                                        building = Building(
                                            id = "b1",
                                            name = "Dip. Matematica"
                                        )
                                    )
                                ),
                                professors = listOf(
                                    Professor("p2", "Luigi", "Verdi")
                                ),
                                activityType = ActivityType("LEZ", "Lezione"),
                                isRemote = true
                            )
                        ),
                        isLoading = false
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
        item(key = "student_info") {
            StudentInfoSection(state.studentInfo)
        }
//        item(key = "nearest_agenda") {
//            Text(
//                text = stringResource(R.string.nearest_agenda_title),
//                modifier = Modifier.padding(16.dp),
//                style = MaterialTheme.typography.titleLarge,
//                fontWeight = FontWeight.Bold,
//                color = MaterialTheme.colorScheme.onBackground
//            )
//            NearestAgendaSection(state.nearestAgenda)
//        }
//        item(key = "next_day_plan") {
//            Text(
//                text = stringResource(R.string.next_day_plan_title),
//                modifier = Modifier.padding(16.dp),
//                style = MaterialTheme.typography.titleLarge,
//                fontWeight = FontWeight.Bold,
//                color = MaterialTheme.colorScheme.onBackground
//            )
//            NextDayPlanSection(state.nextDayPlan)
//        }
        item {
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
            color = MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp)
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
    val isGradeEditable =
        result.gradeResult.canAccept || result.gradeResult.canReject || result.status == GradeStatus.NOT_TOUCHED
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
                modifier = Modifier
                    .fillMaxHeight()
                    .weight(sideContent)
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
                            !passed && grade == "0" -> "X"
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
fun StudentInfoSection(state: StudentInfo) {
    val data = state.studentData
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
                .padding(16.dp)
        ) {
            if (state.isLoading) {
                LoadingShimmerItem()
            } else if (state.errorMessage != null) {
                ErrorItem(state.errorMessage)
            } else if (data == null) {
                ErrorItem(stringResource(R.string.no_data), Color.Gray)
            } else {
                // Student Name Header
                Text(
                    text = "${data.firstname} ${data.lastname}",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
//
//                Text(
//                    text = "@${data.username}",
//                    style = MaterialTheme.typography.bodyMedium,
//                    color = MaterialTheme.colorScheme.onSurfaceVariant
//                )

                Spacer(modifier = Modifier.height(16.dp))

                // Career Cards
                data.careers.forEachIndexed { index, career ->
                    if (index > 0) {
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                    StudentCareerCard(career)
                }
            }
        }
    }
}

@Composable
fun StudentCareerCard(career: StudentCareer) {
    var expanded by remember { mutableStateOf(false) }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { expanded = !expanded },
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceColorAtElevation(4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            // Compact Header - Always Visible
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
                    CopyableText(
                        text = career.studentId,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Text(
                        text = career.staStuDes,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            // Expanded Details, im starting to love AnimatedVisibility so much
            AnimatedVisibility(
                visible = expanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column {
                    Spacer(modifier = Modifier.height(12.dp))

                    HorizontalDivider(
                        thickness = 1.dp,
                        color = MaterialTheme.colorScheme.outlineVariant
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Description
                    Text(
                        text = career.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    if (career.notes.isNotBlank()) {
                        Text(
                            text = career.notes,
                            style = MaterialTheme.typography.bodySmall,
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
                                label = stringResource(R.string.student_id_label),
                                value = career.studentId
                            )
                            Spacer(modifier = Modifier.height(8.dp))


                            CareerDetailItem(
                                label = stringResource(R.string.course_year_label),
                                value = career.courseYear.toString()
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            CareerDetailItem(
                                label = stringResource(R.string.cohort_label),
                                value = career.cohort
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            CareerDetailItem(
                                label = stringResource(R.string.normativa_label),
                                value = career.normativa
                            )
                        }

                        // Right Column
                        Column(modifier = Modifier.weight(1f)) {
                            CareerDetailItem(
                                label = stringResource(R.string.regulation_year_label),
                                value = career.regulationYear
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            CareerDetailItem(
                                label = stringResource(R.string.class_label),
                                value = career.className
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            CareerDetailItem(
                                label = stringResource(R.string.location_label),
                                value = career.sedeDes
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            CareerDetailItem(
                                label = stringResource(R.string.percorso_label),
                                value = career.percorsoDes
                            )
                        }
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
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
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
    color: Color = MaterialTheme.colorScheme.onSurface
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
            text = text,
            style = style,
            color = color
        )
        // i love myself
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
        LaunchedEffect(Unit) {
            delay(1500)
            showCopied = false
        }
    }
}



@Composable
fun NearestAgendaSection(state: NearestAgendaData) {
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
                .padding(16.dp)
        ) {
            if (state.isLoading) {
                LoadingShimmerItem()
            } else if (state.errorMessage != null) {
                ErrorItem(state.errorMessage)
            } else if (state.entry == null) {
                Text(
                    text = stringResource(R.string.no_upcoming_events),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                // Status indicator
                if (state.isInProgress) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.errorContainer
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            // Pulsing dot
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .background(
                                        MaterialTheme.colorScheme.error,
                                        shape = RoundedCornerShape(50)
                                    )
                            )
                            Text(
                                text = stringResource(R.string.in_progress),
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                }

                AgendaEntryCard(
                    entry = state.entry,
                    isHighlighted = true,
                    isInProgress = state.isInProgress
                )
            }
        }
    }
}

@Composable
fun NextDayPlanSection(state: NextDayPlanData) {
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
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (state.isLoading) {
                LoadingShimmerItem()
            } else if (state.errorMessage != null) {
                ErrorItem(state.errorMessage)
            } else if (state.entries.isEmpty()) {
                Text(
                    text = stringResource(R.string.no_agenda_tomorrow),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                state.entries.forEach { entry ->
                    AgendaEntryCard(entry = entry)
                }
            }
        }
    }
}

@Composable
private fun AgendaEntryCard(
    entry: AgendaEntry,
    isHighlighted: Boolean = false,
    isInProgress: Boolean = false
) {
    val backgroundColor = when {
        isInProgress -> MaterialTheme.colorScheme.errorContainer
        isHighlighted -> MaterialTheme.colorScheme.primaryContainer
        else -> MaterialTheme.colorScheme.surfaceColorAtElevation(4.dp)
    }

    val contentColor = when {
        isInProgress -> MaterialTheme.colorScheme.onErrorContainer
        isHighlighted -> MaterialTheme.colorScheme.onPrimaryContainer
        else -> MaterialTheme.colorScheme.onSurface
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = backgroundColor
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            // Title and Activity Type
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = entry.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = contentColor
                    )
                    Text(
                        text = entry.activityType.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = contentColor.copy(alpha = 0.7f)
                    )
                }

                // Time badge
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = when {
                        isInProgress -> MaterialTheme.colorScheme.error
                        isHighlighted -> MaterialTheme.colorScheme.primary
                        else -> MaterialTheme.colorScheme.secondaryContainer
                    }
                ) {
                    Text(
                        text = formatTime(entry.startDateTime) + " - " + formatTime(entry.endDateTime),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = when {
                            isInProgress -> MaterialTheme.colorScheme.onError
                            isHighlighted -> MaterialTheme.colorScheme.onPrimary
                            else -> MaterialTheme.colorScheme.onSecondaryContainer
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Room info
            if (entry.rooms.isNotEmpty()) {
                val room = entry.rooms.first()
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "📍",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text(
                        text = room.name + (room.building?.let { " • ${it.name}" } ?: ""),
                        style = MaterialTheme.typography.bodySmall,
                        color = contentColor.copy(alpha = 0.8f)
                    )
                }
            }

            // Professors
            if (entry.professors.isNotEmpty()) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "👤",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text(
                        text = entry.professors.joinToString(", ") { it.fullName },
                        style = MaterialTheme.typography.bodySmall,
                        color = contentColor.copy(alpha = 0.8f)
                    )
                }
            }

            // Remote indicator
            if (entry.isRemote) {
                Spacer(modifier = Modifier.height(4.dp))
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = MaterialTheme.colorScheme.tertiaryContainer
                ) {
                    Text(
                        text = stringResource(R.string.remote_class),
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onTertiaryContainer
                    )
                }
            }
        }
    }
}

@Composable
private fun formatTime(instant: java.time.Instant): String {
    val formatter = java.time.format.DateTimeFormatter.ofPattern("HH:mm")
        .withZone(java.time.ZoneId.systemDefault())
    return formatter.format(instant)
}
