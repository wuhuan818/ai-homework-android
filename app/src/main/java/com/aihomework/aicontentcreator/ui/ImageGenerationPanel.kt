package com.aihomework.aicontentcreator.ui

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import com.aihomework.aicontentcreator.data.model.ImageAspectRatio
import com.aihomework.aicontentcreator.data.model.ImageGenerationStyle
import com.aihomework.aicontentcreator.ui.state.CreateUiState

@Composable
internal fun ImageGenerationPanel(
    state: CreateUiState,
    onInputChanged: (String) -> Unit,
    onOptimizeImagePrompt: () -> Unit,
    onApplyOptimizedImagePrompt: () -> Unit,
    onKeepOriginalImagePrompt: () -> Unit,
    onImagePromptExampleSelected: (String) -> Unit,
    onPrepareTextToImagePrompt: () -> Unit,
    onApplyTextToImagePrompt: () -> Unit,
    onUseOriginalTextToImagePrompt: () -> Unit,
    onImageGenerationStyleSelected: (ImageGenerationStyle) -> Unit,
    onImageAspectRatioSelected: (ImageAspectRatio) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("图片描述", style = MaterialTheme.typography.titleSmall)
        OutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            value = state.input,
            onValueChange = onInputChanged,
            label = { Text("描述你想生成的图片") },
            placeholder = { Text("例如：一只坐在窗边看雨的橘猫，温暖插画风") },
            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
            minLines = 3
        )

        if (state.showTextToImagePromptCard) {
            TextToImagePromptCard(
                state = state,
                onPrepareTextToImagePrompt = onPrepareTextToImagePrompt,
                onApplyTextToImagePrompt = onApplyTextToImagePrompt,
                onUseOriginalTextToImagePrompt = onUseOriginalTextToImagePrompt
            )
        }

        OutlinedButton(
            modifier = Modifier.fillMaxWidth(),
            onClick = onOptimizeImagePrompt,
            enabled = !state.isOptimizingImagePrompt
        ) {
            Text(if (state.isOptimizingImagePrompt) "正在优化提示词..." else "优化提示词")
        }

        val originalPrompt = state.imagePromptOriginal
        val optimizedPrompt = state.optimizedImagePrompt
        if (originalPrompt != null && optimizedPrompt != null) {
            OptimizedPromptCard(
                originalPrompt = originalPrompt,
                optimizedPrompt = optimizedPrompt,
                onApplyOptimizedImagePrompt = onApplyOptimizedImagePrompt,
                onKeepOriginalImagePrompt = onKeepOriginalImagePrompt
            )
        }

        val examples = listOf(
            "橘猫坐在窗边看雨，温暖插画风。",
            "极简风产品海报，一只蓝色保温杯，白色背景。",
            "傍晚海边散步的人，电影感，柔和光线。"
        )
        Text(
            text = if (state.input.isBlank()) "示例提示词" else "示例提示词（可替换当前输入）",
            style = MaterialTheme.typography.titleSmall
        )
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            examples.forEach { example ->
                OutlinedButton(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = { onImagePromptExampleSelected(example) }
                ) {
                    Text(example, maxLines = 2)
                }
            }
        }

        Text("图片风格", style = MaterialTheme.typography.titleSmall)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ImageGenerationStyle.entries.forEach { style ->
                FilterChip(
                    selected = state.imageGenerationStyle == style,
                    onClick = { onImageGenerationStyleSelected(style) },
                    label = { Text(style.displayName) }
                )
            }
        }

        Text("画幅比例", style = MaterialTheme.typography.titleSmall)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ImageAspectRatio.entries.forEach { ratio ->
                FilterChip(
                    selected = state.imageAspectRatio == ratio,
                    onClick = { onImageAspectRatioSelected(ratio) },
                    label = { Text(ratio.displayName) }
                )
            }
        }

        state.imageUploadNotice?.let { notice ->
            Text(notice, color = MaterialTheme.colorScheme.primary)
        }
    }
}

@Composable
private fun TextToImagePromptCard(
    state: CreateUiState,
    onPrepareTextToImagePrompt: () -> Unit,
    onApplyTextToImagePrompt: () -> Unit,
    onUseOriginalTextToImagePrompt: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("配图提示词", style = MaterialTheme.typography.titleSmall)
        Text("这段内容来自文本结果，可以先整理成更适合图片生成的提示词。")
        val candidate = state.textToImagePromptCandidate
        if (!candidate.isNullOrBlank()) {
            Text("整理结果", style = MaterialTheme.typography.titleSmall)
            ExpandablePromptText(text = candidate, collapsedLines = 8)
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (candidate.isNullOrBlank()) {
                Button(
                    modifier = Modifier.weight(1f),
                    onClick = onPrepareTextToImagePrompt,
                    enabled = !state.isPreparingTextToImagePrompt
                ) {
                    if (state.isPreparingTextToImagePrompt) {
                        CircularProgressIndicator()
                    } else {
                        Text("整理提示词")
                    }
                }
                OutlinedButton(
                    modifier = Modifier.weight(1f),
                    onClick = onUseOriginalTextToImagePrompt
                ) {
                    Text("直接使用")
                }
            } else {
                Button(
                    modifier = Modifier.weight(1f),
                    onClick = onApplyTextToImagePrompt
                ) {
                    Text("应用")
                }
                OutlinedButton(
                    modifier = Modifier.weight(1f),
                    onClick = onUseOriginalTextToImagePrompt
                ) {
                    Text("保留原文")
                }
            }
        }
    }
}

@Composable
private fun OptimizedPromptCard(
    originalPrompt: String,
    optimizedPrompt: String,
    onApplyOptimizedImagePrompt: () -> Unit,
    onKeepOriginalImagePrompt: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text("原提示词", style = MaterialTheme.typography.titleSmall)
        ExpandablePromptText(text = originalPrompt, collapsedLines = 3)
        Text("优化后", style = MaterialTheme.typography.titleSmall)
        ExpandablePromptText(text = optimizedPrompt, collapsedLines = 8)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                modifier = Modifier.weight(1f),
                onClick = onApplyOptimizedImagePrompt
            ) {
                Text("应用")
            }
            TextButton(
                modifier = Modifier.weight(1f),
                onClick = onKeepOriginalImagePrompt
            ) {
                Text("保留原文")
            }
        }
    }
}

@Composable
private fun ExpandablePromptText(
    text: String,
    collapsedLines: Int
) {
    var expanded by remember(text) { mutableStateOf(false) }
    val canFold = shouldFoldText(text, collapsedLines)
    SelectionContainer {
        Text(
            text = text,
            maxLines = if (expanded || !canFold) Int.MAX_VALUE else collapsedLines
        )
    }
    if (canFold) {
        TextButton(onClick = { expanded = !expanded }) {
            Text(if (expanded) "收起" else "展开")
        }
    }
}

private fun shouldFoldText(text: String, collapsedLines: Int): Boolean {
    return text.lines().size > collapsedLines || text.length > collapsedLines * 36
}
