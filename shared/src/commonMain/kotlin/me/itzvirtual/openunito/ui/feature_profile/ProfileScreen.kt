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
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.AndroidUiModes.UI_MODE_NIGHT_YES
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.launch
import me.itzvirtual.openunito.domain.PlatformClipboardManager
import me.itzvirtual.openunito.domain.student.StudentCareer
import me.itzvirtual.openunito.domain.student.studentDataSample
import me.itzvirtual.openunito.resources.Res
import me.itzvirtual.openunito.resources.careers_title
import me.itzvirtual.openunito.ui.components.ListWrapper
import me.itzvirtual.openunito.ui.shared.StudentCareerCard
import me.itzvirtual.openunito.ui.themes.OpenUnitoTheme
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun ProfileScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ProfileViewModel = koinViewModel()
) {
    val state by viewModel.screenState.collectAsStateWithLifecycle()
    val clipboard = koinInject<PlatformClipboardManager>()
    val scope = rememberCoroutineScope()

    ProfileScreenContent(
        modifier = modifier,
        state = state,
        onBack = onBack,
        onCopy = { text ->
            scope.launch { clipboard.setText(text) }
        }
    )
}

@Composable
fun ProfileScreenContent(
    modifier: Modifier = Modifier,
    state: ProfileScreenState,
    onBack: () -> Unit = {},
    onCopy: (text: String) -> Unit = {},
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
                    isLoading = isLoading,
                    onCopy = onCopy,
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
    onCopy: (text: String) -> Unit = {},
) {
    val debug = false // TODO pass with viewModel

    Box(modifier = modifier) {
        Column(
            modifier = Modifier
                .padding(bottom = 16.dp)
                .padding(horizontal = 8.dp)
        ) {
            ListWrapper(
                isEmpty = careers.isEmpty(),
                debug = debug,
                isLoading = isLoading,
                stateError = stateError
            ) {
                LazyColumn {
                    items(careers) { career ->
                        Spacer(modifier = Modifier.height(8.dp))
                        StudentCareerCard(
                            career = career,
                            onCopy = onCopy,
                        )
                    }
                }
            }
        }
    }
}

@Composable
@Preview(showBackground = true, uiMode = UI_MODE_NIGHT_YES)
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