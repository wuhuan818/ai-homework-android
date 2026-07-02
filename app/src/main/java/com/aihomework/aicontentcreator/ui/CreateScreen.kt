package com.aihomework.aicontentcreator.ui

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
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
    onChooseImage: () -> Unit,
    onRotateImage: () -> Unit,
    onWatermarkTextChanged: (String) -> Unit,
    onAddWatermark: () -> Unit,
    onShareProcessedImage: () -> Unit,
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
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = onChooseImage) {
                        Text("Choose image")
                    }
                    OutlinedButton(onClick = onUseMockImage) {
                        Text("Use Mock image")
                    }
                }

                val previewUri = state.processedImageUri ?: state.selectedImageUri
                val imageStatus = when {
                    state.processedImageUri != null -> "处理后的图片已生成，可继续处理或分享。"
                    state.selectedImageUri != null -> "已选择图片，可旋转或添加文字水印。"
                    else -> "No image selected."
                }
                Text(imageStatus)
                ImagePreview(uriText = previewUri)

                state.imageProcessingMessage?.let { Text(it) }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = onRotateImage,
                        enabled = !state.isImageProcessing
                    ) {
                        Text("Rotate 90")
                    }
                    OutlinedButton(
                        onClick = onShareProcessedImage,
                        enabled = !state.isImageProcessing
                    ) {
                        Text("Share image")
                    }
                    if (state.isImageProcessing) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp))
                    }
                }

                OutlinedTextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = state.watermarkText,
                    onValueChange = onWatermarkTextChanged,
                    label = { Text("Watermark text") },
                    singleLine = true
                )
                OutlinedButton(
                    onClick = onAddWatermark,
                    enabled = !state.isImageProcessing
                ) {
                    Text("Add watermark")
                }
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

@Composable
private fun ImagePreview(uriText: String?) {
    if (uriText == null) return

    val context = LocalContext.current
    val bitmap = remember(uriText) {
        loadPreviewBitmap(context, uriText)
    }
    bitmap?.let {
        Image(
            bitmap = it.asImageBitmap(),
            contentDescription = "Image preview",
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp),
            contentScale = ContentScale.Fit
        )
    }
}

private fun loadPreviewBitmap(context: Context, uriText: String): Bitmap? {
    val uri = Uri.parse(uriText)
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
        return loadPreviewWithImageDecoder(context, uri)
    }

    return loadPreviewWithBitmapFactory(context, uri)
}

private fun loadPreviewWithImageDecoder(context: Context, uri: Uri): Bitmap? {
    return try {
        val source = ImageDecoder.createSource(context.contentResolver, uri)
        ImageDecoder.decodeBitmap(source) { decoder, info, _ ->
            decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
            val width = info.size.width
            val height = info.size.height
            if (width > MAX_PREVIEW_DIMENSION || height > MAX_PREVIEW_DIMENSION) {
                val scale = minOf(
                    MAX_PREVIEW_DIMENSION.toFloat() / width,
                    MAX_PREVIEW_DIMENSION.toFloat() / height
                )
                decoder.setTargetSize(
                    maxOf(1, (width * scale).toInt()),
                    maxOf(1, (height * scale).toInt())
                )
            }
        }
    } catch (error: Exception) {
        null
    }
}

private fun loadPreviewWithBitmapFactory(context: Context, uri: Uri): Bitmap? {
    val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
    context.contentResolver.openInputStream(uri)?.use { stream ->
        BitmapFactory.decodeStream(stream, null, bounds)
    } ?: return null

    if (bounds.outWidth <= 0 || bounds.outHeight <= 0) return null

    val options = BitmapFactory.Options().apply {
        inSampleSize = calculatePreviewSampleSize(bounds.outWidth, bounds.outHeight)
    }
    return context.contentResolver.openInputStream(uri)?.use { stream ->
        BitmapFactory.decodeStream(stream, null, options)
    }
}

private fun calculatePreviewSampleSize(width: Int, height: Int): Int {
    var sampleSize = 1
    while (width / sampleSize > MAX_PREVIEW_DIMENSION || height / sampleSize > MAX_PREVIEW_DIMENSION) {
        sampleSize *= 2
    }
    return sampleSize
}

private const val MAX_PREVIEW_DIMENSION = 900
