package me.itzvirtual.myunitox.ui.dashboard

import androidx.compose.foundation.layout.Box
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun DashboardScreen(viewModel: DashboardViewModel = hiltViewModel(), modifier: Modifier) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    Box(modifier = modifier) {
        Text("Test")
    }
}