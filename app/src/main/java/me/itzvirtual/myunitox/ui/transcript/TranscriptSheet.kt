package me.itzvirtual.myunitox.ui.transcript

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import me.itzvirtual.myunitox.R
import me.itzvirtual.myunitox.ui.feature_dashboard.DashboardActions
import me.itzvirtual.myunitox.ui.feature_dashboard.ExamResultsSection
import me.itzvirtual.myunitox.ui.feature_dashboard.GradesSection
import me.itzvirtual.myunitox.ui.feature_dashboard.LocalDashboardActions

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TranscriptSheet(
    onDismiss: () -> Unit,
    viewModel: TranscriptViewModel = hiltViewModel(),
) {
    val state by viewModel.screenState.collectAsStateWithLifecycle()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val actions = DashboardActions(
        changeGradeStatus = { gradeId, newStatus ->
            viewModel.changeGradeStatus(gradeId, newStatus)
        }
    )

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
    ) {
        CompositionLocalProvider(LocalDashboardActions provides actions) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(bottom = 32.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = stringResource(R.string.exam_results_title),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                ExamResultsSection(state.examResultsState)

                Text(
                    text = stringResource(R.string.grades_section_title),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                GradesSection(state.gradesState)

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}
