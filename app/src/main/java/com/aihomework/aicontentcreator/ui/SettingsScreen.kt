package com.aihomework.aicontentcreator.ui

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.aihomework.aicontentcreator.data.settings.AppSettings
import com.aihomework.aicontentcreator.data.settings.ModelMode

@Composable
fun SettingsScreen(
    settings: AppSettings,
    apiKeyInput: String,
    message: String?,
    onModeChanged: (ModelMode) -> Unit,
    onBaseUrlChanged: (String) -> Unit,
    onTextModelChanged: (String) -> Unit,
    onVisionModelChanged: (String) -> Unit,
    onApiKeyInputChanged: (String) -> Unit,
    onSaveSettings: () -> Unit,
    onSaveApiKey: () -> Unit,
    onClearApiKey: () -> Unit,
    onMessageShown: () -> Unit
) {
    val context = LocalContext.current

    LaunchedEffect(message) {
        message?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            onMessageShown()
        }
    }

    Column(
        modifier = Modifier
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text("Settings", style = MaterialTheme.typography.headlineSmall)

        Text("Model mode", style = MaterialTheme.typography.titleMedium)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(
                selected = settings.mode == ModelMode.Mock,
                onClick = { onModeChanged(ModelMode.Mock) },
                label = { Text("Mock") }
            )
            FilterChip(
                selected = settings.mode == ModelMode.Real,
                onClick = { onModeChanged(ModelMode.Real) },
                label = { Text("Real") }
            )
        }

        OutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            value = apiKeyInput,
            onValueChange = onApiKeyInputChanged,
            label = { Text("API Key") },
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.None,
                keyboardType = KeyboardType.Password
            ),
            singleLine = true
        )
        Text(
            text = if (settings.hasApiKey) "API Key status: configured (hidden)" else "API Key status: not configured",
            style = MaterialTheme.typography.bodyMedium
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = onSaveApiKey) {
                Text("Save API Key")
            }
            OutlinedButton(onClick = onClearApiKey) {
                Text("Clear API Key")
            }
        }

        OutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            value = settings.baseUrl,
            onValueChange = onBaseUrlChanged,
            label = { Text("Base URL") },
            singleLine = true
        )
        OutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            value = settings.textModel,
            onValueChange = onTextModelChanged,
            label = { Text("Text Model") },
            singleLine = true
        )
        OutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            value = settings.visionModel,
            onValueChange = onVisionModelChanged,
            label = { Text("Vision Model") },
            singleLine = true
        )

        Button(
            onClick = onSaveSettings,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Save Settings")
        }

        Text("Storage: ${settings.keyStorageDescription}")
        Text("Real mode uses one OpenAI-compatible Chat Completions endpoint.")
    }
}
