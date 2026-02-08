package me.itzvirtual.myunitox.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import me.itzvirtual.myunitox.R
import me.itzvirtual.myunitox.data.models.ExamDate
import me.itzvirtual.myunitox.data.models.ExamResult

@Composable
fun HomeScreen(viewModel: HomeViewModel = hiltViewModel(), modifier: Modifier) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Box(modifier = modifier) {
        when {
            state.isLoading -> {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }

            state.errorMessage != null -> {
                Text(text = state.errorMessage!!, color = Color.Red)
            }

            else -> {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    item {
                        Text(
                            stringResource(R.string.exam_dates_title),
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                    if (state.examDates.isEmpty()) {
                        item { EmptyStateMessage(stringResource(R.string.no_data)) }
                    } else {
                        items(state.examDates) { item ->
                            ExamDateRow(item)
                        }
                    }

                    item {
                        Text(
                            stringResource(R.string.exam_results_title),
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                    if (state.examResults.isEmpty()) {
                        item { EmptyStateMessage(stringResource(R.string.no_data)) }
                    } else {
                        items(state.examResults) { exam ->
                            ExamResultRow(exam)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun EmptyStateMessage(message: String) {
    Text(
        text = message,
        modifier = Modifier
            .padding(16.dp)
            .fillMaxWidth(),
        style = MaterialTheme.typography.bodySmall,
        color = Color.Gray,
        textAlign = TextAlign.Center
    )
}

@Composable
fun ExamDateRow(date: ExamDate) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            // Exam Description as card title
            // Use Description : Title, or Title : ""
            // I suppose that title can't be blank
            val cardTitle: String = date.description.ifBlank { date.title }
            val cardDescription: String = date.title

            Text(
                text = cardTitle,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                maxLines = 2
            )
            if (cardTitle != cardDescription) {
                Text(
                    text = cardDescription,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            // Date
            Text(
                text = stringResource(R.string.date, date.date),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Status row (students and booking status)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Booking status
                if (date.bookingEnabled) {
                    Text(
                        text = stringResource(R.string.booking_open),
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.Green,
                        fontWeight = FontWeight.Bold
                    )
                } else {
                    Text(
                        text = stringResource(R.string.booking_closed),
                        style = MaterialTheme.typography.labelMedium,
                        color = Color(255, 85, 85)
                    )
                }
                Text(
                    text = "${date.bookingStartDate} - ${date.bookingEndDate}",
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.Gray,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun ExamResultRow(result: ExamResult) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
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
                    Row() {
                        Text(
                            text = result.courseName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            maxLines = 2
                        )
                        Spacer(modifier = Modifier.padding(horizontal = 4.dp))
                        Text(
                            text = "\u2022 ${result.weight} CFU",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Normal,
                            maxLines = 2
                        )
                    }

                    Text(
                        text = stringResource(R.string.date, result.date),
                        style = MaterialTheme.typography.bodySmall
                    )
                    Spacer(modifier = Modifier.height(4.dp))

                }

                // grade
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = result.grade,
                        style = MaterialTheme.typography.headlineMedium,
                        color = Color(0xFF388E3C),
                        fontWeight = FontWeight.ExtraBold
                    )
                    if (result.maxGrade != 30) {
                        Text(
                            text = "/ ${result.maxGrade}",
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = stringResource(R.string.editable_until),
                    style = MaterialTheme.typography.labelMedium,
                    color = Color(255, 170, 0)
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
    }
}

