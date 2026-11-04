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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
import me.itzvirtual.openunito.ui.components.TopBar
import me.itzvirtual.openunito.ui.shared.StudentCareerCard
import me.itzvirtual.openunito.ui.themes.OpenUnitoTheme
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun ProfileScreen(
    modifier: Modifier = Modifier,
    viewModel: ProfileViewModel = koinViewModel(),
    onBack: () -> Unit
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
        },
        onSelectCareer = { careerId ->
            viewModel.setCareer(careerId)
        }
    )
}

@Composable
fun ProfileScreenContent(
    modifier: Modifier = Modifier,
    state: ProfileScreenState,
    onBack: () -> Unit = {},
    onCopy: (text: String) -> Unit = {},
    onSelectCareer: (careerId: Int) -> Unit = {},
) {
    val careers = state.data?.careers ?: emptyList()
    val selectedCareerId = state.selectedCareerId
    val stateError = state.error
    val isLoading = state.isLoading

    Scaffold(
        modifier = modifier,
        topBar = {
            TopBar( // TODO: do this for every screen
                colors = listOf(
                    MaterialTheme.colorScheme.onPrimary,
                    MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.35f), // TODO: find a fucking cool color
                ),
            ) {
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
                    },
                    colors = TopAppBarDefaults.topAppBarColors().copy(
                        containerColor = Color.Transparent,
                    )
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
                .padding(paddingValues)
        ) {
            // column??
            // Again, yes
            Column(modifier = Modifier.fillMaxSize()) {
                CareersList(
                    careers = careers,
                    selectedCareerId = selectedCareerId,
                    stateError = stateError,
                    isLoading = isLoading,
                    onCopy = onCopy,
                    onSelectCareer = onSelectCareer,
                )
            }
        }
    }
}


@Composable
fun CareersList(
    modifier: Modifier = Modifier,
    careers: List<StudentCareer>,
    selectedCareerId: Int? = null,
    stateError: String?,
    isLoading: Boolean,
    onCopy: (text: String) -> Unit = {},
    onSelectCareer: (careerId: Int) -> Unit = {},
) {
    val debug = false // TODO pass with viewModel

    Box(modifier = modifier) {
        Column(
            modifier = Modifier
        ) {
            ListWrapper( // TODO Gabriele: no calendar shows No element present, prepone state error
                isEmpty = careers.isEmpty(),
                debug = debug,
                isLoading = isLoading,
                stateError = stateError
            ) {
                LazyColumn {
                    items(careers) { career ->
                        Spacer(modifier = Modifier.height(16.dp))

                        StudentCareerCard(
                            career = career,
                            isSelected = career.id == selectedCareerId,
                            onCopy = onCopy,
                            onSelectCareer = onSelectCareer,
                        )
                    }

                    item {
                        Spacer(modifier = Modifier.height(16.dp))
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
                    selectedCareerId = 1,
                    isLoading = false,
                    error = null
                ),
                modifier = Modifier.padding(innerPadding)
            )
        }
    }
}