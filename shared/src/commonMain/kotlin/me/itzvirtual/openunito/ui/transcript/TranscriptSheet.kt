package me.itzvirtual.openunito.ui.transcript

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import org.jetbrains.compose.resources.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.koin.compose.viewmodel.koinViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import co.touchlab.kermit.Logger
import kotlinx.coroutines.Job
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import me.itzvirtual.openunito.resources.*
import me.itzvirtual.openunito.domain.examresult.ExamResult
import me.itzvirtual.openunito.domain.examresult.GradeStatus
import me.itzvirtual.openunito.domain.grade.ExamGrade
import me.itzvirtual.openunito.ui.components.ListWrapper
import me.itzvirtual.openunito.ui.shared.AnimatedSymmetricalBorder
import me.itzvirtual.openunito.ui.shared.formatDate
import me.itzvirtual.openunito.ui.shared.shimmerEffect
import me.itzvirtual.openunito.ui.shared.unShoutCourseName
import me.itzvirtual.openunito.ui.themes.LocalCustomColorsPalette

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TranscriptSheet(
    viewModel: TranscriptViewModel = koinViewModel(),
    onDismiss: () -> Unit,
) {
    val state by viewModel.screenState.collectAsStateWithLifecycle()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val actions = TranscriptActions(
        changeGradeStatus = { gradeId, newStatus ->
            viewModel.changeGradeStatus(gradeId, newStatus)
        }
    )

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
    ) {
        CompositionLocalProvider(LocalTranscriptActions provides actions) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(bottom = 32.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = stringResource(Res.string.grades_section_title),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                GradesSection(
                    state.gradesState.grades,
                    state.gradesState.isLoading,
                    state.gradesState.error,
                    debug = state.debug,
                )

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

data class TranscriptActions(
    val changeGradeStatus: (String, GradeStatus) -> Job = { gradeId, newStatus ->
        Logger.i(tag = "TranscriptActions") { "Grade with id $gradeId clicked, new status: $newStatus" }
        Job() // placeholder
    },
)

val LocalTranscriptActions = compositionLocalOf { TranscriptActions() }

@Composable
fun GradeWrapper(result: ExamResult) {
    result.gradeResult!!

    val haptics = LocalHapticFeedback.current
    val cornerRadius = 8.dp
    val actions = LocalTranscriptActions.current
    val scope = rememberCoroutineScope()


    val isRevealed = result.status != GradeStatus.NOT_TOUCHED || !result.gradeResult.passed
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
                modifier = Modifier.clip(
                    androidx.compose.foundation.shape.RoundedCornerShape(
                        cornerRadius
                    )
                ).clickable(
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
                            !isRevealed -> "30"
                            !passed && grade == "0" -> "X"
                            laude -> "${grade}L"
                            else -> grade
                        }
                    },
                    style = MaterialTheme.typography.headlineMedium,
                    color = when {
                        !isRevealed -> Color.Transparent
                        result.gradeResult.passed -> LocalCustomColorsPalette.current.gradePassed
                        else -> LocalCustomColorsPalette.current.gradeNotPassed
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
fun GradesSection(
    rawGrades: List<ExamGrade>,
    isLoading: Boolean,
    stateError: String?,
    debug: Boolean,
) {
    val grades = remember(rawGrades) {
        rawGrades.filter { it.score != null }
    }


    ListWrapper(
        isEmpty = grades.isEmpty(),
        debug = debug,
        isLoading = isLoading,
        stateError = stateError
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
            shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
            ) {
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
                    text = unShoutCourseName(courseName),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 2
                )

                Text(
                    text = stringResource(
                        Res.string.date,
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