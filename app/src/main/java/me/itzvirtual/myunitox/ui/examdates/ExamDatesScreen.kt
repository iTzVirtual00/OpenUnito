package me.itzvirtual.myunitox.ui.examdates

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
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
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
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import me.itzvirtual.myunitox.R
import me.itzvirtual.myunitox.data.models.ExamDate
import me.itzvirtual.myunitox.ui.util.formatDate

@Composable
fun ExamDatesScreen(viewModel: ExamDatesViewModel = hiltViewModel(), modifier: Modifier) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Box(modifier = modifier) {
        when {
            state.isLoading -> {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }

            state.errorMessage != null -> {
                Text(text = state.errorMessage!!, color = MaterialTheme.colorScheme.error)
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
    ElevatedCard(
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
                text = stringResource(R.string.date, formatDate(date.date)),
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
                    text = "${formatDate(date.bookingStartDate)} - ${formatDate(date.bookingEndDate)}",
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.Gray,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
