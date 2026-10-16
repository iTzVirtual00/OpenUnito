package me.itzvirtual.openunito.ui.feature_profile

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import me.itzvirtual.openunito.domain.student.StudentCareer
import me.itzvirtual.openunito.domain.student.studentDataSample
import me.itzvirtual.openunito.resources.Res
import me.itzvirtual.openunito.resources.careers_title
import me.itzvirtual.openunito.resources.no_data
import me.itzvirtual.openunito.ui.components.StdSurface
import me.itzvirtual.openunito.ui.shared.ErrorItem
import me.itzvirtual.openunito.ui.shared.StudentCareerCard
import me.itzvirtual.openunito.ui.themes.OpenUnitoTheme
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun ProfileScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ProfileViewModel = koinViewModel()
) {
    val state by viewModel.screenState.collectAsStateWithLifecycle()

    ProfileScreenContent(
        modifier = modifier,
        state = state,
        onBack = onBack,
    )
}

@Composable
fun ProfileScreenContent(
    modifier: Modifier = Modifier,
    state: ProfileScreenState,
    onBack: () -> Unit = {},
) {
    val careers = state.data?.careers ?: emptyList()
    val stateError = state.error
    val isLoading = state.isLoading

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(Res.string.careers_title),
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
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
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
//            Text(
//                text = stringResource(Res.string.careers_title),
//                style = MaterialTheme.typography.headlineSmall,
//                fontWeight = FontWeight.Bold,
//                color = MaterialTheme.colorScheme.onSurface,
//                modifier = Modifier.padding(horizontal = 8.dp)
//            )
            when {
                isLoading -> {
                    CircularProgressIndicator()
                }

                stateError != null || careers.isEmpty() -> {
                    StdSurface {
                        if (debug) {
                            val msg = stateError
                                ?: "List is empty" //If state error is null, no error has occurred and list is empty
                            ErrorItem(
                                errorMessage = msg,
                                color = MaterialTheme.colorScheme.error,
                            )
                        } else {
                            ErrorItem(stringResource(Res.string.no_data), Color.Gray)
                        }
                    }
                }

                else -> {
                    LazyColumn {
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
@Preview(showBackground = true)
fun ProfileScreenPreview() {
    OpenUnitoTheme {
        Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
            ProfileScreenContent(
                state = ProfileScreenState(
                    data = studentDataSample,
                    isLoading = false,
                    error = null
                ),
                modifier = Modifier.padding(innerPadding)
            )
        }
    }
}