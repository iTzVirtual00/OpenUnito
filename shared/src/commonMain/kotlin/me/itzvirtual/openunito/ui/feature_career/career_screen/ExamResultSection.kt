package me.itzvirtual.openunito.ui.feature_career.career_screen

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.AndroidUiModes.UI_MODE_NIGHT_YES
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import co.touchlab.kermit.Logger
import kotlinx.coroutines.Job
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import me.itzvirtual.openunito.data.localization.withDateFormatter
import me.itzvirtual.openunito.domain.examresult.ExamResult
import me.itzvirtual.openunito.domain.examresult.GradeStatus
import me.itzvirtual.openunito.domain.examresult.examResultsDataSample
import me.itzvirtual.openunito.resources.Res
import me.itzvirtual.openunito.resources.date
import me.itzvirtual.openunito.resources.editable_until
import me.itzvirtual.openunito.ui.components.ListWrapper
import me.itzvirtual.openunito.ui.components.StdDrawerCard
import me.itzvirtual.openunito.ui.components.StdTextButton
import me.itzvirtual.openunito.ui.shared.AnimatedSymmetricalBorder
import me.itzvirtual.openunito.ui.shared.shimmerEffect
import me.itzvirtual.openunito.ui.shared.unShoutCourseName
import me.itzvirtual.openunito.ui.themes.LocalCustomColorsPalette
import org.jetbrains.compose.resources.stringResource


data class ExamResultActions(
    val changeGradeStatus: (String, GradeStatus) -> Job = { gradeId, newStatus ->
        Logger.i(tag = "TranscriptActions") { "Grade with id $gradeId clicked, new status: $newStatus" }
        Job() // placeholder
    },
)

val LocalExamResultActions = compositionLocalOf { ExamResultActions() }

fun isGradeEditable(
    grade: ExamResult
): Boolean {
    return grade.gradeResult!!.passed
            && (
            grade.gradeResult.canAccept
                    || grade.gradeResult.canReject
                    || grade.status == GradeStatus.NOT_TOUCHED
            )
}

@Composable
fun ExamResultsSection(
    // TODO: Test this thoroughly, may contain some bugs
    // TODO: Move editable until, now it is not very readable
    modifier: Modifier = Modifier,
    gradesList: List<ExamResult>,
    stateError: String?,
    isLoading: Boolean,
    debug: Boolean? = null,
) {
    var expandedExamId by remember { mutableStateOf<String?>(null) }

    val gradesList = examResultsDataSample // TODO: TEMP

    ListWrapper(
        modifier = modifier,
        isEmpty = gradesList.isEmpty(),
        debug = debug,
        isLoading = isLoading,
        stateError = stateError
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
        ) {
            gradesList.forEachIndexed { idx, grade ->
                var selectedStatus by remember { mutableStateOf(grade.status) }
                var showDialog by remember { mutableStateOf(false) }

                val isGradeStatusLoadingState = remember { mutableStateOf(false) }

                val drawerEnabled = !isGradeStatusLoadingState.value && isGradeEditable(grade)


                StdDrawerCard(
                    modifier = Modifier
                        .padding(bottom = 10.dp),
                    internalPadding = PaddingValues(horizontal = 0.dp),
                    expanded = grade.id == expandedExamId, // TODO: TEMP
                    headerContent = {
                        GradeEntryHeader(
                            modifier = Modifier
                                .clickable(
                                    enabled = drawerEnabled,
                                    onClick = { // TODO: same as ExamSess, use bringIntoView scroll when opening last element
                                        val id = grade.id
                                        expandedExamId = if (expandedExamId != id) id else null
                                    },
                                )
                                .padding(horizontal = 16.dp)
                                .padding(top = 10.dp, bottom = 8.dp),
//                                    .padding(16.dp),
                            grade = grade,
                        )
                    },
                    drawerContent = {
                        GradeEntryDrawer(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp)
                                .padding(top = 8.dp, bottom = 10.dp),
                            grade = grade,
                            selectedStatus = selectedStatus,
                            onAccept = {
                                showDialog = true
                                selectedStatus = GradeStatus.ACCEPTED
                            },
                            onReject = {
                                showDialog = true
                                selectedStatus = GradeStatus.REJECTED
                            },
                        )
                    }
                )

                if (showDialog) { // TODO OLD: use one dialog reusable for all cards
                    val haptics = LocalHapticFeedback.current
                    val scope = rememberCoroutineScope()
                    val actions = LocalExamResultActions.current

                    SelectionConfirmationDialog(
                        grade = grade,
                        selectedStatus = selectedStatus,
                        onDismiss = { showDialog = false },
                        onClickYes = {
                            if (selectedStatus != GradeStatus.UNKNOWN) {
                                val selectionCopy = selectedStatus
                                selectedStatus = GradeStatus.UNKNOWN // Reset selection
                                haptics.performHapticFeedback(HapticFeedbackType.LongPress)

                                isGradeStatusLoadingState.value = true
                                scope.launch {
                                    joinAll(
                                        actions.changeGradeStatus(
                                            grade.id,
                                            selectionCopy
                                        )
                                    )
                                    isGradeStatusLoadingState.value = false
                                }
                            }
                        },
                        onClickNo = {
                            showDialog = false
                            selectedStatus = grade.status
                        }
                    )
                }
            }
        }
    }


}

@Composable
fun GradeEntryHeader(
    modifier: Modifier = Modifier,
    grade: ExamResult,
) {
    grade.gradeResult!! // TODO: looks wrong

    val mainContentWeight = 0.95f
    val sideContentWeight = 1 - mainContentWeight



    Row(
        modifier = modifier
            .height(IntrinsicSize.Min),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier
//                .padding(16.dp)
//                .padding(end = 0.dp)
                .weight(mainContentWeight)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                // info
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = unShoutCourseName(grade.courseName),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 2
                    )

                    withDateFormatter {
                        Text(
                            text = "${
                                stringResource(
                                    Res.string.date,
                                    grade.date.formatDate()
                                )
                            } \u2022 ${grade.weight} CFU",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                }

                Spacer(modifier = Modifier.weight(0.1f))

                // grade
                GradeWrapper(grade)
            }
            // editable until
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = stringResource(Res.string.editable_until),
                    style = MaterialTheme.typography.labelMedium,
                    color = when {
//                        isGradeEditable -> LocalCustomColorsPalette.current.careerColor // TODO: fix this
                        else -> Color.Gray
                    },
                )

                Text( // TODO: Change font weights, add careerColor and fix layout
                    text = grade.editableUntil,
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.Gray,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // GradeEntryBar()

    }
}


@Composable
fun GradeEntryDrawer(
    modifier: Modifier = Modifier,
    grade: ExamResult,
    selectedStatus: GradeStatus,
    onAccept: () -> Unit,
    onReject: () -> Unit,
) {
    grade.gradeResult!!

    val innerWeight = 0.13f
    val buttonWeight = 1.5f


    val rejectBorder = when (selectedStatus) {
        GradeStatus.REJECTED -> BorderStroke(
            2.dp,
            LocalCustomColorsPalette.current.gradeNotPassed.copy(alpha = 0.5f)
        )

        else -> BorderStroke(2.dp, LocalCustomColorsPalette.current.gradeNotPassed)
    }
    val acceptBorder = when (selectedStatus) {
        GradeStatus.ACCEPTED -> BorderStroke(
            2.dp,
            LocalCustomColorsPalette.current.careerColor.copy(alpha = 0.5f)
        )

        else -> BorderStroke(2.dp, LocalCustomColorsPalette.current.careerColor)
    }

    val rejectTextStyle = when (selectedStatus) {
        GradeStatus.REJECTED -> LocalTextStyle.current.copy(
            color = MaterialTheme.colorScheme.outline,
        )

        else -> LocalTextStyle.current
    }
    val acceptTextStyle = when (selectedStatus) {
        GradeStatus.ACCEPTED -> LocalTextStyle.current.copy(
            color = MaterialTheme.colorScheme.outline,
        )

        else -> MaterialTheme.typography.labelMedium
    }


    Row(
        modifier = modifier,
    ) {
        val inactiveTextColor = LocalTextStyle.current.copy(
            color = LocalTextStyle.current.color.copy(alpha = 0.5f),
        )

        StdTextButton(
            modifier = Modifier.weight(buttonWeight),
            text = "Reject",
            textStyle = rejectTextStyle,
            border = rejectBorder,
            onClick = onReject,
        )

        Spacer(modifier = Modifier.weight(innerWeight))

        StdTextButton(
            modifier = Modifier.weight(buttonWeight * 1f),
            text = "Accept",
            textStyle = acceptTextStyle,
            border = acceptBorder,
            onClick = onAccept,
        )
    }
}

@Composable
fun GradeWrapper(examGraderade: ExamResult) {
    examGraderade.gradeResult!!

    val haptics = LocalHapticFeedback.current
    val actions = LocalExamResultActions.current
    val scope = rememberCoroutineScope()

    val isRevealed =
        examGraderade.status != GradeStatus.NOT_TOUCHED
                || !examGraderade.gradeResult.passed

    val cornerRadius = 8.dp

    var isLoading by remember { mutableStateOf(false) }

    AnimatedSymmetricalBorder(
        isRevealed = isRevealed,
        modifier = Modifier.padding(0.dp),
        pathCornerRadius = cornerRadius,
        lineColor = MaterialTheme.colorScheme.tertiary,
    ) {
        Box (
            modifier = Modifier
                .size(MaterialTheme.typography.headlineMedium.lineHeight.value.dp),
            contentAlignment = Alignment.Center,
        ) {
            Column(
                horizontalAlignment = Alignment.End,
                modifier = Modifier.clip(
                    RoundedCornerShape(
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
                        joinAll(actions.changeGradeStatus(examGraderade.id, GradeStatus.VIEWED))
                        isLoading = false
                    }
                }
                    .let {
                        when (examGraderade.status) {
                            GradeStatus.NOT_TOUCHED -> if (!isRevealed) it.shimmerEffect(
                                Color.Gray
                            ) else it

                            else -> it
                        }
                    }

            ) {
                Text(
                    text = examGraderade.gradeResult.run {
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
                        examGraderade.gradeResult.passed -> LocalCustomColorsPalette.current.gradePassed
                        else -> LocalCustomColorsPalette.current.gradeNotPassed
                    },
                    fontWeight = FontWeight.ExtraBold
                )
                // TODO: this is not an issue now, find an elegant solution when it arises
//                if (examGraderade.maxGrade.isNotBlank() && examGraderade.maxGrade != "30") {
//                    Text(
//                        text = "/ ${examGraderade.maxGrade}",
//                        style = MaterialTheme.typography.labelMedium
//                    )
//                }
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

// TODO: Decide if we want to put it in again
//
//@Composable
//fun GradeEntryBar(
//    showChroma: Boolean,
//) {
////    val showChroma = (
////                isGradeStatusLoading
////                || (grade.status == GradeStatus.VIEWEDU
////                || grade.status == GradeStatus.NOT_TOUCHED)
////            ) && isGradeEditable
//
//    // when showChroma
//    val infiniteTransition = rememberInfiniteTransition(label = "ChromaMovement")
//    val chromaOffset by infiniteTransition.animateFloat(
//        initialValue = 0f,
//        targetValue = 2000f, // This distance determines the speed of the flow
//        animationSpec = infiniteRepeatable(
//            animation = tween(3000, easing = LinearEasing),
//            repeatMode = RepeatMode.Restart
//        ),
//        label = "ChromaOffset"
//    )
//    val colors = listOf(
//        MaterialTheme.colorScheme.primary,
//        MaterialTheme.colorScheme.onPrimary,
//        LocalCustomColorsPalette.current.careerColor,
//        MaterialTheme.colorScheme.primary,
////                Color(0xFF64B5F6), // Start: Blue
////                Color(0xFFBA68C8), // Purple
////                Color(0xFFF06292), // Pink
////                Color(0xFF64B5F6)  // End: Blue (Same as start)
//    )
//
//    // when !showChroma
//    val animatedColor by animateColorAsState(
//        targetValue = if (grade.gradeResult.passed) grade.status.toColor() else GradeStatus.UNKNOWN.toColor(),
//        animationSpec = tween(durationMillis = 500),
//        label = "GradeStatusColor"
//    )
//
//
//    Box(
//        modifier = Modifier
//            .fillMaxHeight()
//            .weight(sideContentWeight)
//            .drawBehind {
//                if (showChroma) {
//                    // ai-powered, no idea how this works. also, cool palette gemini!
//                    val brush = Brush.linearGradient(
//                        colors = colors,
//                        // Start offset moves from -size to +size
//                        start = Offset(0f, chromaOffset - 1000f),
//                        // End offset stays a fixed distance away from start
//                        end = Offset(0f, chromaOffset),
//                        tileMode = TileMode.Repeated
//                    )
//                    drawRect(brush)
//                } else {
//                    drawRect(animatedColor)
//                }
//            },
//        contentAlignment = Alignment.Center
//    ) {}
//}


@Composable
fun SelectionConfirmationDialog(
    modifier: Modifier = Modifier,
    grade: ExamResult,
    selectedStatus: GradeStatus,
    onDismiss: () -> Unit,
    onClickYes: () -> Unit,
    onClickNo: () -> Unit,
) {
    val selectionText = when (selectedStatus) { // TODO: translations
        GradeStatus.ACCEPTED -> "You are choosing to ACCEPT this grade."
        GradeStatus.REJECTED -> "You are choosing to REJECT this grade."
        GradeStatus.VIEWED -> "You are choosing remove grade choice." // Shouldn't be possible
        GradeStatus.NOT_TOUCHED -> "You are choosing to remove grade choice (not touched)." // Shouldn't be possible
        else -> throw Exception("Invalid grade status selection")
    }

    AlertDialog(
        modifier = modifier,
        onDismissRequest = onDismiss,
        title = { Text(grade.courseName) },
        text = { Text(selectionText) },
        confirmButton = {
            TextButton(
                onClick = onClickYes
            ) {
                Text("Yes")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onClickNo
            ) {
                Text("No")
            }
        }
    )
}

@Preview(showBackground = true, uiMode = UI_MODE_NIGHT_YES)
@Composable
fun ExamResultPreview() {
    CareerScreenPreview()
}