package com.aihomework.aicontentcreator.ui

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.aihomework.aicontentcreator.ui.state.EditUiState

@Composable
fun EditScreen(
    state: EditUiState,
    onTextChanged: (String) -> Unit,
    onSave: () -> Unit,
    onConvertMarkdown: () -> Unit,
    onConvertPlainText: () -> Unit,
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
        Text("编辑作品", style = MaterialTheme.typography.headlineSmall)
        val hasEditableContent = state.itemId != null ||
            state.scenario != null ||
            state.text.isNotBlank()
        if (!hasEditableContent) {
            Text("暂无可编辑作品", style = MaterialTheme.typography.titleMedium)
            Text("请先在创作页生成内容，或从历史页打开作品。")
            return@Column
        }

        Text(state.scenario?.displayName ?: "当前作品")

        OutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            value = state.text,
            onValueChange = onTextChanged,
            label = { Text("编辑创作结果") },
            minLines = 8
        )

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = onSave) {
                Text("保存")
            }
            OutlinedButton(onClick = onShare) {
                Text("分享")
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(onClick = onConvertMarkdown) {
                Text("整理为 Markdown")
            }
            OutlinedButton(onClick = onConvertPlainText) {
                Text("去除 Markdown 符号")
            }
        }
    }
}
