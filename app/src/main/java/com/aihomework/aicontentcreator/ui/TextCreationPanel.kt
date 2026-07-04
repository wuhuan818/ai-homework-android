package com.aihomework.aicontentcreator.ui

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Card
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.aihomework.aicontentcreator.data.model.TextCreationStyle
import com.aihomework.aicontentcreator.ui.state.CreateUiState

@Composable
internal fun TextCreationPanel(
    state: CreateUiState,
    onTextStyleSelected: (TextCreationStyle) -> Unit,
    onGenerationCountChanged: (Int) -> Unit,
    onSuggestStyle: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text("创作风格", style = MaterialTheme.typography.titleMedium)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TextCreationStyle.optionsFor(state.selectedScenario).forEach { style ->
                    FilterChip(
                        selected = state.textStyle == style,
                        onClick = { onTextStyleSelected(style) },
                        label = { Text(style.displayName) }
                    )
                }
            }

            OutlinedButton(
                modifier = Modifier.fillMaxWidth(),
                onClick = onSuggestStyle,
                enabled = !state.isSuggestingStyle
            ) {
                Text(if (state.isSuggestingStyle) "正在推荐..." else "帮我推荐风格")
            }

            Text("生成设置", style = MaterialTheme.typography.titleMedium)
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("生成 3 个版本", style = MaterialTheme.typography.titleSmall)
                    Text(
                        "开启后会生成三个不同表达方向。",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                Switch(
                    checked = state.generationCount >= 3,
                    onCheckedChange = { checked ->
                        onGenerationCountChanged(if (checked) 3 else 1)
                    }
                )
            }

            state.styleAdviceMessage?.let { message ->
                Text(message, color = MaterialTheme.colorScheme.primary)
            }

            if (state.styleAdvice.isNotEmpty()) {
                Text("推荐方向：", style = MaterialTheme.typography.titleSmall)
                state.styleAdvice.forEachIndexed { index, advice ->
                    Text("${index + 1}. ${advice.style.displayName}：${advice.reason}")
                }
            }
        }
    }
}
