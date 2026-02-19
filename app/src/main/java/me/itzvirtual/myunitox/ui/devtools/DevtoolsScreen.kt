package me.itzvirtual.myunitox.ui.devtools

import android.content.res.Configuration
import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import me.itzvirtual.myunitox.ui.themes.MyUnitoXTheme
import me.itzvirtual.myunitox.ui.util.restartApp


data class DevtoolsActions(
    val setToken: (String) -> Unit = { _ ->
        Log.d("DevtoolsActions", "setToken triggered")
    },
    val copyToken: () -> Unit = {
        Log.d("DevtoolsActions", "onCopyToken triggered")
    },
    val clearToken: () -> Unit = {
        Log.d("DevtoolsActions", "clearToken triggered")
    }
)

val LocalDevtoolsActions = compositionLocalOf { DevtoolsActions() }

@Composable
fun DevtoolsScreen(
    modifier: Modifier = Modifier,
    viewModel: DevtoolsViewModel = hiltViewModel()
) {
    val context = LocalContext.current

    // Bind ViewModel logic to the Actions data class
    val actions = remember(viewModel, context) {
        DevtoolsActions(
            setToken = { token -> viewModel.login(token); restartApp(context) },
            copyToken = { viewModel.copyTokenToClipboard(context) },
            clearToken = { viewModel.clearToken(); restartApp(context) }
        )
    }

    CompositionLocalProvider(LocalDevtoolsActions provides actions) {
        MyUnitoXTheme {
            DevtoolsContent(modifier = modifier)
        }
    }
}

@Composable
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
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
    var tokenInput by remember { mutableStateOf("") }
    val actions = LocalDevtoolsActions.current

    Scaffold(modifier = modifier) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
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
    }
}