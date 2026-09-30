package me.itzvirtual.myunitox.ui.feature_profile

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import me.itzvirtual.myunitox.R
import me.itzvirtual.myunitox.domain.student.sampleStudentData
import me.itzvirtual.myunitox.ui.shared.StudentCareerCard
import me.itzvirtual.myunitox.ui.themes.MyUnitoXTheme


@Composable
@Preview(uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES, showBackground = true)
fun ProfileScreenPreview() {
    MyUnitoXTheme {
        Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
            ProfileScreenContent(
                state = ProfileScreenState(
                    data = sampleStudentData,
                    isLoading = false,
                    error = null
                ),
                modifier = Modifier.padding(innerPadding)
            )
        }
    }

}

@Composable
fun ProfileScreen(viewModel: ProfileViewModel = hiltViewModel(), modifier: Modifier) {
    val state by viewModel.screenState.collectAsStateWithLifecycle()
    var showStatistics by remember { mutableStateOf(false) }

    ProfileScreenContent(state, modifier)
}

@Composable
fun ProfileScreenContent(
    state: ProfileScreenState,
    modifier: Modifier = Modifier,
) {
    // column??
    Column(modifier = modifier) {
        CareersSection(
            state = state,
        )
    }
}


@Composable
fun CareersSection(state: ProfileScreenState, modifier: Modifier = Modifier) {
    Box(modifier = modifier) {
        when {
            state.isLoading -> {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }

            state.error != null -> {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = state.error,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
            }

            else -> {
                val careers = state.data!!.careers
                Column(
                    modifier = Modifier
                        .padding(bottom = 16.dp)
                        .padding(horizontal = 8.dp)
                ) {
                    Text(
                        text = stringResource(R.string.careers_title),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    careers.forEach { career ->
                        StudentCareerCard(
                            career = career,
                            defaultExpanded = career.isActive()
                        )
                    }
                }
            }
        }
    }
}