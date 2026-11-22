package me.itzvirtual.openunito.ui.feature_career.transcript_screen

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.dropShadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.AndroidUiModes.UI_MODE_NIGHT_YES
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.Job
import me.itzvirtual.openunito.data.localization.withDateFormatter
import me.itzvirtual.openunito.domain.examresult.GradeStatus
import me.itzvirtual.openunito.domain.getNowInstant
import me.itzvirtual.openunito.domain.grade.ExamGrade
import me.itzvirtual.openunito.domain.grade.Score
import me.itzvirtual.openunito.resources.Res
import me.itzvirtual.openunito.resources.date
import me.itzvirtual.openunito.resources.grades_section_title
import me.itzvirtual.openunito.ui.components.GradientBackground
import me.itzvirtual.openunito.ui.components.ListWrapper
import me.itzvirtual.openunito.ui.components.ShadowDefaults
import me.itzvirtual.openunito.ui.components.StaleCacheBanner
import me.itzvirtual.openunito.ui.shared.unShoutCourseName
import me.itzvirtual.openunito.ui.themes.LocalCustomColorsPalette
import me.itzvirtual.openunito.ui.themes.OpenUnitoTheme
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun TranscriptScreen(
    modifier: Modifier = Modifier,
    viewModel: TranscriptViewModel = koinViewModel(),
    onBack: () -> Unit,
) {
    val state by viewModel.screenState.collectAsStateWithLifecycle()
    val uiState = state.gradesState

    TranscriptScreenContent(
        rawGrades = uiState.grades,
        isLoading = uiState.isLoading,
        stateError = uiState.error,
        isCacheStale = uiState.isStale,
        debug = state.debug,
        changeGradeStatus = { gradeId, newStatus ->
            viewModel.changeGradeStatus(gradeId, newStatus)
        },
        onBack = onBack,
    )
}


@Composable
fun TranscriptScreenContent(
    modifier: Modifier = Modifier,
    rawGrades: List<ExamGrade>,
    isLoading: Boolean,
    stateError: String?,
    isCacheStale: Boolean,
    debug: Boolean?,
    changeGradeStatus: (String, GradeStatus) -> Job,
    onBack: () -> Unit = {},
) {
    Scaffold(
        topBar = {
            GradientBackground(
                colors = listOf(
                    MaterialTheme.colorScheme.onPrimary,
                    LocalCustomColorsPalette.current.onCareerColor
                ),
            ) {
                TopAppBar(
                    title = {
                        Text(
                            modifier = Modifier
                                .dropShadow(
                                    shape = RoundedCornerShape(16.dp),
                                    shadow = ShadowDefaults.Text,
                                ),
                            text = stringResource(Res.string.grades_section_title),
                            fontWeight = FontWeight.Bold,
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                                contentDescription = "Back"
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors().copy(
                        containerColor = Color.Transparent,
                    )
                )
            }
        },
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(paddingValues)
                .verticalScroll(rememberScrollState()),
        ) {
            StaleCacheBanner(
                modifier = Modifier.padding(top = 8.dp),
                visible = isCacheStale,
            )

            Spacer(
                modifier = Modifier.height(16.dp)
            )
            GradesSection(
                rawGrades = rawGrades,
                isLoading = isLoading,
                stateError = stateError,
                debug = debug,
            )
            Spacer(
                modifier = Modifier.height(16.dp)
            )
        }
    }
}



@Composable
fun GradesSection(
    modifier: Modifier = Modifier,
    rawGrades: List<ExamGrade>,
    isLoading: Boolean,
    stateError: String?,
    debug: Boolean?,
) {
    val grades = remember(rawGrades) {
        rawGrades.filter { it.score != null }
    }


    ListWrapper(
        modifier = modifier,
        isEmpty = grades.isEmpty(),
        debug = debug ?: false,
        isLoading = isLoading,
        stateError = stateError
    ) {
        Surface(
            modifier = Modifier,
            shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                withDateFormatter {
                    grades.forEachIndexed { idx, grade ->
                        GradeRow(
                            courseName = grade.courseName,
                            date = grade.score!!.date.formatDate(),
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
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline
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
@Preview(showBackground = true, uiMode = UI_MODE_NIGHT_YES)
fun TranscriptScreenPreview() {
    OpenUnitoTheme {
        Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
            TranscriptScreenContent(
                modifier = Modifier.padding(innerPadding),
                isLoading = false,
                stateError = null,
                debug = false,
                rawGrades = listOf(
                    ExamGrade(
                        id = "suca",
                        courseName = "Matematica",
                        score = Score(getNowInstant(), "30", true),
                        teaching = "Sucatica",
                        status = "CazzSacc",
                        passYear = 2,
                        yearFreq = "Boh",
                        ordAdCod = "aaa",
                        ordAdDes = "fff",
                        yearIdx = 27,
                        weight = "9"
                    ),
                    ExamGrade(
                        id = "suca",
                        courseName = "Matematica",
                        score = Score(getNowInstant(), "30", true),
                        teaching = "Sucatica",
                        status = "CazzSacc",
                        passYear = 2,
                        yearFreq = "Boh",
                        ordAdCod = "aaa",
                        ordAdDes = "fff",
                        yearIdx = 27,
                        weight = "9"
                    ),
                    ExamGrade(
                        id = "suca",
                        courseName = "Matematica",
                        score = Score(getNowInstant(), "30", true),
                        teaching = "Sucatica",
                        status = "CazzSacc",
                        passYear = 2,
                        yearFreq = "Boh",
                        ordAdCod = "aaa",
                        ordAdDes = "fff",
                        yearIdx = 27,
                        weight = "9"
                    ),
                    ExamGrade(
                        id = "suca",
                        courseName = "Matematica",
                        score = Score(getNowInstant(), "30", true),
                        teaching = "Sucatica",
                        status = "CazzSacc",
                        passYear = 2,
                        yearFreq = "Boh",
                        ordAdCod = "aaa",
                        ordAdDes = "fff",
                        yearIdx = 27,
                        weight = "9"
                    ),
                    ExamGrade(
                        id = "suca",
                        courseName = "Matematica",
                        score = Score(getNowInstant(), "30", true),
                        teaching = "Sucatica",
                        status = "CazzSacc",
                        passYear = 2,
                        yearFreq = "Boh",
                        ordAdCod = "aaa",
                        ordAdDes = "fff",
                        yearIdx = 27,
                        weight = "9"
                    ),
                    ExamGrade(
                        id = "suca",
                        courseName = "Matematica",
                        score = Score(getNowInstant(), "30", true),
                        teaching = "Sucatica",
                        status = "CazzSacc",
                        passYear = 2,
                        yearFreq = "Boh",
                        ordAdCod = "aaa",
                        ordAdDes = "fff",
                        yearIdx = 27,
                        weight = "9"
                    ),
                    ExamGrade(
                        id = "suca",
                        courseName = "Matematica",
                        score = Score(getNowInstant(), "30", true),
                        teaching = "Sucatica",
                        status = "CazzSacc",
                        passYear = 2,
                        yearFreq = "Boh",
                        ordAdCod = "aaa",
                        ordAdDes = "fff",
                        yearIdx = 27,
                        weight = "9"
                    ),
                    ExamGrade(
                        id = "suca",
                        courseName = "Matematica",
                        score = Score(getNowInstant(), "30", true),
                        teaching = "Sucatica",
                        status = "CazzSacc",
                        passYear = 2,
                        yearFreq = "Boh",
                        ordAdCod = "aaa",
                        ordAdDes = "fff",
                        yearIdx = 27,
                        weight = "9"
                    ),
                    ExamGrade(
                        id = "suca",
                        courseName = "Matematica",
                        score = Score(getNowInstant(), "30", true),
                        teaching = "Sucatica",
                        status = "CazzSacc",
                        passYear = 2,
                        yearFreq = "Boh",
                        ordAdCod = "aaa",
                        ordAdDes = "fff",
                        yearIdx = 27,
                        weight = "9"
                    ),
                ),
                isCacheStale = false,
                changeGradeStatus = { s, f -> Job() },
            )
        }
    }
}