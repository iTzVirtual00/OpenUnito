package me.itzvirtual.myunitox.ui.feature_profile

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import me.itzvirtual.myunitox.R
import me.itzvirtual.myunitox.domain.student.StudentCareer
import me.itzvirtual.myunitox.domain.student.sampleStudentData
import me.itzvirtual.myunitox.ui.components.StdSurface
import me.itzvirtual.myunitox.ui.shared.ErrorItem
import me.itzvirtual.myunitox.ui.shared.StudentCareerCard
import me.itzvirtual.myunitox.ui.themes.MyUnitoXTheme


@Composable
fun ProfileScreen(
    modifier: Modifier = Modifier,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val state by viewModel.screenState.collectAsStateWithLifecycle()

    ProfileScreenContent(state, modifier)
}

@Composable
fun ProfileScreenContent(
    state: ProfileScreenState,
    modifier: Modifier = Modifier,
) {
    val careers = state.data?.careers ?: emptyList()
    val stateError = state.error
    val isLoading = state.isLoading
    // column??
    // Again, yes
    Column(modifier = modifier) {
        CareersSection(
            careers = careers,
            stateError = stateError,
            isLoading = isLoading
        )
    }
}


@Composable
fun CareersSection(
    modifier: Modifier = Modifier,
    careers: List<StudentCareer>,
    stateError: String?,
    isLoading: Boolean,
) {
    val debug = false

    Box(modifier = modifier) {
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
            when {
                isLoading -> {
                    CircularProgressIndicator()
                }

                stateError != null || careers.isEmpty() -> {
                    StdSurface() {
                        if (debug) {
                            val msg = stateError ?: "List is empty" //If state error is null, no error has occurred and list is empty
                            ErrorItem(
                                errorMessage = msg,
                                color = MaterialTheme.colorScheme.error,
                            )
                        }
                        else {
                            ErrorItem(stringResource(R.string.no_data), Color.Gray)
                        }
                    }
                }

                else -> {
                    LazyColumn() {
                        items(careers) { career ->
                            Spacer(modifier = Modifier.height(8.dp))
                            StudentCareerCard(
                                career = career,
                            )
                        }
                    }
                }
            }
        }
    }
}

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