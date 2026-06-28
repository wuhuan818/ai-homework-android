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
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.unit.dp
import com.aihomework.aicontentcreator.data.model.CreationScenario
import com.aihomework.aicontentcreator.ui.state.CreateUiState

@Composable
fun CreateScreen(
    state: CreateUiState,
    onScenarioSelected: (CreationScenario) -> Unit,
    onInputChanged: (String) -> Unit,
    onUseMockImage: () -> Unit,
    onGenerate: () -> Unit,
    onEdit: () -> Unit,
    onFavorite: () -> Unit,
    onShare: () -> Unit,
    onMessageShown: () -> Unit
) {
    val context = LocalContext.current

    LaunchedEffect(state.message) {
        state.message?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            onMessageShown()
        }
    }

    Column(
        modifier = Modifier
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("AIContentCreator", style = MaterialTheme.typography.headlineSmall)
        Text("选择场景，输入主题，用 MockModelClient 生成可演示内容。")

        CreationScenario.entries.forEach { scenario ->
            ScenarioCard(
                scenario = scenario,
                selected = state.selectedScenario == scenario,
                onClick = { onScenarioSelected(scenario) }
            )
        }

        OutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            value = state.input,
            onValueChange = onInputChanged,
            label = { Text(state.selectedScenario.inputHint) },
            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
            minLines = 3
        )

        if (state.selectedScenario == CreationScenario.ImageDescription) {
            OutlinedButton(onClick = onUseMockImage) {
                Text("选择模拟图片")
            }
        }

        Button(
            onClick = onGenerate,
            enabled = !state.isLoading,
            modifier = Modifier.fillMaxWidth()
        ) {
            if (state.isLoading) {
                CircularProgressIndicator()
            } else {
                Text("生成 Mock 内容")
            }
        }

        state.result?.let { result ->
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(result.scenario.displayName, style = MaterialTheme.typography.titleMedium)
                    Text(result.content)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(onClick = onEdit) {
                            Text("编辑")
                        }
                        OutlinedButton(onClick = onFavorite) {
                            Text("收藏")
                        }
                        OutlinedButton(onClick = onShare) {
                            Text("分享")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ScenarioCard(
    scenario: CreationScenario,
    selected: Boolean,
    onClick: () -> Unit
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = {
            Column {
                Text(scenario.displayName, style = MaterialTheme.typography.titleSmall)
                Text(scenario.description, style = MaterialTheme.typography.bodySmall)
            }
        }
    )
}

