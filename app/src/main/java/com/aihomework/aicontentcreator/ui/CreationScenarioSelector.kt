package com.aihomework.aicontentcreator.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.aihomework.aicontentcreator.data.model.CreationScenario
import com.aihomework.aicontentcreator.data.settings.ModelMode

@Composable
internal fun CreationScenarioSelector(
    selectedScenario: CreationScenario,
    expanded: Boolean,
    onExpand: () -> Unit,
    onScenarioSelected: (CreationScenario) -> Unit
) {
    if (!expanded) {
        Card(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "当前创作：${selectedScenario.displayName}",
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.titleSmall
                )
                OutlinedButton(onClick = onExpand) {
                    Text("切换")
                }
            }
        }
        return
    }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("选择创作类型", style = MaterialTheme.typography.titleMedium)
        CreationScenario.entries.forEach { scenario ->
            ScenarioCard(
                scenario = scenario,
                selected = selectedScenario == scenario,
                onClick = { onScenarioSelected(scenario) }
            )
        }
    }
}

@Composable
internal fun ModeNotice(
    modelMode: ModelMode,
    hasApiKey: Boolean,
    scenario: CreationScenario
) {
    val notice = when {
        modelMode == ModelMode.Mock && scenario == CreationScenario.ImageGeneration ->
            "当前为演示模式，生成本地占位图，不调用真实模型。"

        modelMode == ModelMode.Mock -> "演示模式：本地生成，不上传内容。"
        !hasApiKey -> "未配置密钥：请先在设置页配置密钥。"
        scenario == CreationScenario.ImageGeneration -> "当前为真实模式，将调用当前图片生成模型。"
        else -> "真实模式：调用当前模型配置。"
    }
    Card(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = notice,
            modifier = Modifier.padding(12.dp),
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
private fun ScenarioCard(
    scenario: CreationScenario,
    selected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = if (selected) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surfaceVariant
            }
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(scenario.displayName, style = MaterialTheme.typography.titleSmall)
            Text(
                text = scenario.description,
                style = MaterialTheme.typography.bodySmall,
                maxLines = 2
            )
        }
    }
}
