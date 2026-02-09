package me.itzvirtual.myunitox.ui.dashboard

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import me.itzvirtual.myunitox.R
import me.itzvirtual.myunitox.data.models.DashboardDataResponse
import me.itzvirtual.myunitox.data.models.dashboardDataSample
import me.itzvirtual.myunitox.ui.theme.MyUnitoXTheme


@Composable
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Preview(showBackground = true)
fun DashboardScreenPreview() {
    MyUnitoXTheme {
        Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
            DashboardScreen(
                state = DashboardUiState(
                    dashboardData = dashboardDataSample,
                    isLoading = false,
                    errorMessage = null
                ),
                modifier = Modifier.padding(innerPadding)
            )
        }
    }
}

@Composable
fun DashboardScreen(viewModel: DashboardViewModel = hiltViewModel(), modifier: Modifier) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    DashboardScreen(
        state = state,
        modifier = modifier
    )
}

@Composable
fun DashboardScreen(state: DashboardUiState, modifier: Modifier) {
    Box(modifier = modifier) {
        when {
            state.isLoading -> {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }

            state.errorMessage != null -> {
                Text(text = state.errorMessage, color = Color.Red)
            }

            else -> {
                val data = state.dashboardData!!
                Column {
                    Text(
                        text = stringResource(R.string.credits_section_title),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(16.dp)
                    )
                    CreditsSection(data)
                    Text(
                        text = stringResource(R.string.grades_section_title),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(16.dp)
                    )
                    GradesSection(data)
                }
            }
        }
    }
}

@Composable
fun GradesSection(data: DashboardDataResponse) {
    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceContainerHighest)
    ) {
        items(data.grades) { grade ->
            Grade(
                name = grade.name,
                date = grade.date,
                cfu = "?",
                grade = grade.value.toString()
            )
            HorizontalDivider(thickness = 0.5.dp)
        }
    }
}

@Composable
fun Grade(name: String, grade: String, date: String, cfu: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { /* Handle click */ }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = name,
                style = MaterialTheme.typography.titleMedium,
            )
            Text(
                text = "$date - $cfu CFU",
                style = MaterialTheme.typography.bodySmall,
            )
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = grade,
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                modifier = Modifier.padding(end = 8.dp)
            )
        }
    }
}

@Composable
fun CreditsSection(data: DashboardDataResponse) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .height(IntrinsicSize.Min), // grow based on content
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Card(
            modifier = Modifier
                .weight(1.5f)
                .fillMaxHeight(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                contentColor = MaterialTheme.colorScheme.primary // TODO: fix this
            )
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                CreditProgressIndicator(
                    credits = data.stats.creditsGained.toInt(),
                    totalCredits = data.stats.totalCredits,
                    plannedCredits = data.stats.totalPlanCredits,
                )
            }
        }

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Card(modifier = Modifier.fillMaxWidth()) {
                Text("2", modifier = Modifier.padding(24.dp))
            }
            Card(modifier = Modifier.fillMaxWidth()) {
                Text("3", modifier = Modifier.padding(24.dp))
            }
        }
    }
}

