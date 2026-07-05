package com.aihomework.aicontentcreator.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.aihomework.aicontentcreator.data.model.ImageAspectRatio
import com.aihomework.aicontentcreator.data.model.ImageDescriptionStyle
import com.aihomework.aicontentcreator.ui.state.CreateUiState

@Composable
internal fun ImageDescriptionPanel(
    state: CreateUiState,
    onImageDescriptionStyleSelected: (ImageDescriptionStyle) -> Unit,
    onChooseImage: () -> Unit,
    onUseMockImage: () -> Unit,
    onRotateImage: () -> Unit,
    onRestoreOriginalImage: () -> Unit,
    onShareProcessedImage: () -> Unit,
    onWatermarkTextChanged: (String) -> Unit,
    onApplyGrayscale: () -> Unit,
    onAddWatermark: () -> Unit,
    onOpenGestureCrop: () -> Unit,
    onCropImage: (ImageAspectRatio) -> Unit
) {
    CreationSection(title = "描述风格") {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ImageDescriptionStyle.entries.forEach { style ->
                FilterChip(
                    modifier = Modifier.weight(1f),
                    selected = state.imageDescriptionStyle == style,
                    onClick = { onImageDescriptionStyleSelected(style) },
                    label = { Text(style.displayName) }
                )
            }
        }
    }

    CreationSection(title = "图片来源") {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedButton(
                modifier = Modifier.weight(1f),
                onClick = onChooseImage
            ) {
                Text("选择图片")
            }
            OutlinedButton(
                modifier = Modifier.weight(1f),
                onClick = onUseMockImage
            ) {
                Text("示例图片")
            }
        }

        val previewUri = state.processedImageUri ?: state.selectedImageUri
        val currentImageText = when {
            state.processedImageUri != null -> "当前使用：处理后图片"
            state.selectedImageUri != null -> "当前使用：原图"
            else -> "当前使用：未选择图片"
        }
        Text(currentImageText)
        ImagePreview(uriText = previewUri)
        state.imageUploadNotice?.let { notice ->
            Text(notice, color = MaterialTheme.colorScheme.primary)
            if (notice.contains("转换/压缩")) {
                Text(
                    "压缩/转换只用于识别，不覆盖原图。",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }

    ImageProcessingPanel(
        state = state,
        onRotateImage = onRotateImage,
        onRestoreOriginalImage = onRestoreOriginalImage,
        onShareProcessedImage = onShareProcessedImage,
        onWatermarkTextChanged = onWatermarkTextChanged,
        onApplyGrayscale = onApplyGrayscale,
        onAddWatermark = onAddWatermark,
        onOpenGestureCrop = onOpenGestureCrop,
        onCropImage = onCropImage
    )
}
