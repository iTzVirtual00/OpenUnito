package me.itzvirtual.openunito.ui.feature_devtools


import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import co.touchlab.kermit.Logger
import kotlinx.coroutines.launch
import me.itzvirtual.openunito.domain.rememberAppClipboard
import me.itzvirtual.openunito.ui.shared.rememberRestartApp
import me.itzvirtual.openunito.ui.themes.MyUnitoXTheme
import org.koin.compose.viewmodel.koinViewModel

//TODO: move it somewhere else
data class DevtoolsActions(
    val setToken: (String) -> Unit = { _ ->
        Logger.d(tag = "DevtoolsActions") { "setToken triggered" }
    },
    val copyToken: () -> Unit = {
        Logger.d(tag = "DevtoolsActions") { "onCopyToken triggered" }
    },
    val clearToken: () -> Unit = {
        Logger.d(tag = "DevtoolsActions") { "clearToken triggered" }
    },
    val purgeAllCaches: () -> Unit = {
        Logger.d(tag = "DevtoolsActions") { "purgeAllCaches triggered" }
    }
)

val LocalDevtoolsActions = compositionLocalOf { DevtoolsActions() }

//TODO: add database dump feature
@Composable
fun DevtoolsScreen(
    modifier: Modifier = Modifier,
    viewModel: DevtoolsViewModel = koinViewModel()
) {
    // I can do WHAT??
    val restartApp = rememberRestartApp()
    val clipboard = rememberAppClipboard()
    val scope = rememberCoroutineScope()

    // Bind ViewModel logic to the Actions data class
    val actions = remember(viewModel, restartApp, clipboard) {
        DevtoolsActions(
            setToken = { token ->
                viewModel.login(token) {
                    viewModel.purgeAllCaches {
                        restartApp()
                    }
                }
            },
            copyToken = {
                viewModel.copyTokenToClipboard { token ->
                    scope.launch { clipboard.copyText(token) }
                }
            },
            clearToken = { viewModel.clearToken { viewModel.purgeAllCaches { restartApp() } } },
            purgeAllCaches = { viewModel.purgeAllCaches { restartApp() } }
        )
    }

    CompositionLocalProvider(LocalDevtoolsActions provides actions) {
        MyUnitoXTheme {
            DevtoolsContent(modifier = modifier)
        }
    }
}

@Composable
@Preview(showBackground = true)
fun DevtoolsPreview() {
    val mockActions = DevtoolsActions()

    MyUnitoXTheme {
        CompositionLocalProvider(LocalDevtoolsActions provides mockActions) {
            DevtoolsContent()
        }
    }
}

@Composable
fun DevtoolsContent(modifier: Modifier = Modifier) {
    Scaffold(modifier = modifier) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(16.dp)
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            UpdateCard()
            PurgeCachesCard()
            AuthDebuggerCard()
        }
    }
}

@Composable
fun PurgeCachesCard() {
    val actions = LocalDevtoolsActions.current
    OutlinedCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Cache Management",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "Purge all cached data (agenda, dashboard, grades, exams, student). The app will restart.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
            ) {
                Button(
                    onClick = { actions.purgeAllCaches() },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor = MaterialTheme.colorScheme.onError
                    )
                ) {
                    Text("Purge All Caches")
                }
            }
        }
    }
}

@Composable
expect fun UpdateCard()

@Composable
fun AuthDebuggerCard() {
    var tokenInput by remember { mutableStateOf("") }
    val actions = LocalDevtoolsActions.current
    OutlinedCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Authentication Debugger",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )

            OutlinedTextField(
                value = tokenInput,
                onValueChange = { input ->
                    tokenInput = input.filter { it != '\n' && it != '\r' } // really Android?
                },
                label = { Text("setToken") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true, // UI only :(
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Secondary button for copying
                TextButton(onClick = { actions.copyToken() }) {
                    Text("Copy Current")
                }

                Spacer(Modifier.width(8.dp))

                Button(onClick = { actions.setToken(tokenInput) }) {
                    Text("Set Token")
                }

                Spacer(Modifier.width(8.dp))

                Button(
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor = MaterialTheme.colorScheme.onError
                    ),
                    onClick = { actions.clearToken() }) {
                    Text("Clear")
                }
            }
        }
    }

}
