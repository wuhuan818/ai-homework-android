package com.aihomework.aicontentcreator.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.aihomework.aicontentcreator.data.model.CreationResult
import com.aihomework.aicontentcreator.data.model.HistoryContentType

@Composable
internal fun CreationResultCard(
    result: CreationResult,
    isLoading: Boolean,
    isCurrentResultFavorite: Boolean,
    onSaveImage: () -> Unit,
    onShare: () -> Unit,
    onFavorite: () -> Unit,
    onGenerate: () -> Unit,
    onEdit: () -> Unit,
    onUseResultForImagePrompt: () -> Unit,
    onCopyResult: () -> Unit
) {
    CreationSection(title = "生成结果") {
        if (result.contentType == HistoryContentType.IMAGE) {
            ImageResultContent(
                result = result,
                isLoading = isLoading,
                isCurrentResultFavorite = isCurrentResultFavorite,
                onSaveImage = onSaveImage,
                onShare = onShare,
                onFavorite = onFavorite,
                onGenerate = onGenerate
            )
        } else {
            TextResultContent(
                result = result,
                isCurrentResultFavorite = isCurrentResultFavorite,
                onEdit = onEdit,
                onUseResultForImagePrompt = onUseResultForImagePrompt,
                onFavorite = onFavorite,
                onShare = onShare,
                onCopyResult = onCopyResult
            )
        }
    }
}

@Composable
private fun ImageResultContent(
    result: CreationResult,
    isLoading: Boolean,
    isCurrentResultFavorite: Boolean,
    onSaveImage: () -> Unit,
    onShare: () -> Unit,
    onFavorite: () -> Unit,
    onGenerate: () -> Unit
) {
    Text(result.scenario.displayName, style = MaterialTheme.typography.bodyMedium)
    AsyncGeneratedImagePreview(
        imageFileName = result.imageFileName,
        fallbackUriText = result.imagePreviewUri,
        cacheKey = result.imageFileName ?: "result_${result.id}"
    )
    Text(result.content)
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Button(
            modifier = Modifier.weight(1f),
            onClick = onSaveImage
        ) {
            Text("保存到相册")
        }
        OutlinedButton(
            modifier = Modifier.weight(1f),
            onClick = onShare
        ) {
            Text("分享")
        }
    }
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        OutlinedButton(
            modifier = Modifier.weight(1f),
            onClick = onFavorite
        ) {
            Text(if (isCurrentResultFavorite) "取消收藏" else "收藏")
        }
        OutlinedButton(
            modifier = Modifier.weight(1f),
            onClick = onGenerate,
            enabled = !isLoading
        ) {
            Text("重新生成")
        }
    }
    Text("已保存到历史", style = MaterialTheme.typography.bodySmall)
}

@Composable
private fun TextResultContent(
    result: CreationResult,
    isCurrentResultFavorite: Boolean,
    onEdit: () -> Unit,
    onUseResultForImagePrompt: () -> Unit,
    onFavorite: () -> Unit,
    onShare: () -> Unit,
    onCopyResult: () -> Unit
) {
    var expanded by remember(result.id) { mutableStateOf(false) }
    val shouldPreview = result.content.lines().size > 10 || result.content.length > 320

    Text(result.scenario.displayName, style = MaterialTheme.typography.bodyMedium)
    Text(
        text = result.content,
        maxLines = if (expanded || !shouldPreview) Int.MAX_VALUE else 10,
        overflow = TextOverflow.Ellipsis
    )
    if (shouldPreview) {
        TextButton(onClick = { expanded = !expanded }) {
            Text(if (expanded) "收起" else "展开")
        }
    }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                modifier = Modifier.weight(1f),
                onClick = onEdit
            ) {
                Text("编辑")
            }
            OutlinedButton(
                modifier = Modifier.weight(1f),
                onClick = onUseResultForImagePrompt
            ) {
                Text("生成配图")
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedButton(
                modifier = Modifier.weight(1f),
                onClick = onFavorite
            ) {
                Text(if (isCurrentResultFavorite) "取消收藏" else "收藏")
            }
            OutlinedButton(
                modifier = Modifier.weight(1f),
                onClick = onShare
            ) {
                Text("分享")
            }
            OutlinedButton(
                modifier = Modifier.weight(1f),
                onClick = onCopyResult
            ) {
                Text("复制")
            }
        }
    }
}
