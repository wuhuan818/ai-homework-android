package com.aihomework.aicontentcreator.ui

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.aihomework.aicontentcreator.data.model.ImageAspectRatio
import com.aihomework.aicontentcreator.ui.state.CreateUiState

@Composable
internal fun ImageProcessingPanel(
    state: CreateUiState,
    onRotateImage: () -> Unit,
    onRestoreOriginalImage: () -> Unit,
    onShareProcessedImage: () -> Unit,
    onWatermarkTextChanged: (String) -> Unit,
    onApplyGrayscale: () -> Unit,
    onAddWatermark: () -> Unit,
    onOpenGestureCrop: () -> Unit,
    onCropImage: (ImageAspectRatio) -> Unit
) {
    CreationSection(title = "图片基础处理") {
        val hasSelectedImage = state.selectedImageUri != null || state.processedImageUri != null
        if (!hasSelectedImage) {
            Text("选图后可旋转、水印、滤镜和裁剪。")
        }
        state.imageProcessingMessage?.let { Text(it) }

        if (hasSelectedImage) {
            Text("基础操作", style = MaterialTheme.typography.titleSmall)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    modifier = Modifier.weight(1f),
                    onClick = onRotateImage,
                    enabled = !state.isImageProcessing
                ) {
                    Text("旋转 90°")
                }
                OutlinedButton(
                    modifier = Modifier.weight(1f),
                    onClick = onRestoreOriginalImage,
                    enabled = state.processedImageUri != null && !state.isImageProcessing
                ) {
                    Text("恢复原图")
                }
            }
            OutlinedButton(
                modifier = Modifier.fillMaxWidth(),
                onClick = onShareProcessedImage,
                enabled = state.processedImageUri != null && !state.isImageProcessing
            ) {
                Text("分享处理后图片")
            }
            if (state.isImageProcessing) {
                CircularProgressIndicator()
            }

            Text("效果处理", style = MaterialTheme.typography.titleSmall)
            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = state.watermarkText,
                onValueChange = onWatermarkTextChanged,
                label = { Text("水印文字") },
                singleLine = true
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    modifier = Modifier.weight(1f),
                    onClick = onApplyGrayscale,
                    enabled = !state.isImageProcessing
                ) {
                    Text("黑白滤镜")
                }
                OutlinedButton(
                    modifier = Modifier.weight(1f),
                    onClick = onAddWatermark,
                    enabled = !state.isImageProcessing
                ) {
                    Text("添加水印")
                }
            }

            Text("裁剪", style = MaterialTheme.typography.titleSmall)
            OutlinedButton(
                modifier = Modifier.fillMaxWidth(),
                onClick = onOpenGestureCrop,
                enabled = !state.isImageProcessing
            ) {
                Text("框选裁剪")
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ImageAspectRatio.entries.forEach { ratio ->
                    OutlinedButton(
                        onClick = { onCropImage(ratio) },
                        enabled = !state.isImageProcessing
                    ) {
                        Text(ratio.displayName)
                    }
                }
            }
        }
    }
}
