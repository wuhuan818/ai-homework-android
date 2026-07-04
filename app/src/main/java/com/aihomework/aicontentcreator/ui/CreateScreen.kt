package com.aihomework.aicontentcreator.ui

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
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
import androidx.compose.material3.Switch
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
import com.aihomework.aicontentcreator.data.model.HistoryContentType
import com.aihomework.aicontentcreator.data.model.ImageAspectRatio
import com.aihomework.aicontentcreator.data.model.ImageDescriptionStyle
import com.aihomework.aicontentcreator.data.model.ImageGenerationStyle
import com.aihomework.aicontentcreator.data.model.TextCreationStyle
import com.aihomework.aicontentcreator.data.settings.ModelMode
import com.aihomework.aicontentcreator.ui.state.CreateUiState

@Composable
fun CreateScreen(
    state: CreateUiState,
    modelMode: ModelMode,
    hasApiKey: Boolean,
    onScenarioSelected: (CreationScenario) -> Unit,
    onImageDescriptionStyleSelected: (ImageDescriptionStyle) -> Unit,
    onImageGenerationStyleSelected: (ImageGenerationStyle) -> Unit,
    onImageAspectRatioSelected: (ImageAspectRatio) -> Unit,
    onTextStyleSelected: (TextCreationStyle) -> Unit,
    onGenerationCountChanged: (Int) -> Unit,
    onSuggestStyle: () -> Unit,
    onInputChanged: (String) -> Unit,
    onUseMockImage: () -> Unit,
    onChooseImage: () -> Unit,
    onRotateImage: () -> Unit,
    onApplyGrayscale: () -> Unit,
    onCropImage: (ImageAspectRatio) -> Unit,
    onWatermarkTextChanged: (String) -> Unit,
    onAddWatermark: () -> Unit,
    onRestoreOriginalImage: () -> Unit,
    onShareProcessedImage: () -> Unit,
    onGenerate: () -> Unit,
    onEdit: () -> Unit,
    onFavorite: () -> Unit,
    onShare: () -> Unit,
    onSaveImage: () -> Unit,
    onCopyResult: () -> Unit,
    isCurrentResultFavorite: Boolean,
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
        Text("灵感工坊", style = MaterialTheme.typography.headlineSmall)
        Text("选择场景，开始创作。")
        ModeNotice(
            modelMode = modelMode,
            hasApiKey = hasApiKey,
            scenario = state.selectedScenario
        )

        ImageSection(title = "创作入口") {
            CreationScenario.entries.forEach { scenario ->
                ScenarioCard(
                    scenario = scenario,
                    selected = state.selectedScenario == scenario,
                    onClick = { onScenarioSelected(scenario) }
                )
            }
        }

        ImageSection(title = "当前场景设置") {
            Text(state.selectedScenario.displayName, style = MaterialTheme.typography.titleSmall)
            Text(state.selectedScenario.description)
            if (state.selectedScenario == CreationScenario.ImageGeneration) {
                ImageGenerationOptions(
                    state = state,
                    onInputChanged = onInputChanged,
                    onImageGenerationStyleSelected = onImageGenerationStyleSelected,
                    onImageAspectRatioSelected = onImageAspectRatioSelected
                )
            } else {
                OutlinedTextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = state.input,
                    onValueChange = onInputChanged,
                    label = { Text(state.selectedScenario.inputHint) },
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                    minLines = 3
                )
            }
        }

        if (state.selectedScenario == CreationScenario.Moments ||
            state.selectedScenario == CreationScenario.Product
        ) {
            TextCreationOptions(
                state = state,
                onTextStyleSelected = onTextStyleSelected,
                onGenerationCountChanged = onGenerationCountChanged,
                onSuggestStyle = onSuggestStyle
            )
        }

        if (state.selectedScenario == CreationScenario.ImageDescription) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                ImageSection(title = "描述风格") {
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

                ImageSection(title = "图片来源") {
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
                            Text("使用示例图片")
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

                ImageSection(title = "图片基础处理") {
                    val hasSelectedImage = state.selectedImageUri != null || state.processedImageUri != null
                    if (!hasSelectedImage) {
                        Text("选择图片后可进行旋转、水印、滤镜和裁剪。")
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
                            OutlinedButton(
                                modifier = Modifier.weight(1f),
                                onClick = onShareProcessedImage,
                                enabled = state.processedImageUri != null && !state.isImageProcessing
                            ) {
                                Text("分享")
                            }
                        }
                        if (state.isImageProcessing) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp))
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

                        Text("裁剪比例", style = MaterialTheme.typography.titleSmall)
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
        }

        if (state.isLoading && state.selectedScenario == CreationScenario.ImageGeneration) {
            Text("正在生成图片...", color = MaterialTheme.colorScheme.primary)
        }

        Button(
            onClick = onGenerate,
            enabled = !state.isLoading,
            modifier = Modifier.fillMaxWidth()
        ) {
            if (state.isLoading) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp))
            } else {
                val text = when {
                    state.selectedScenario == CreationScenario.ImageGeneration -> "生成图片"
                    modelMode == ModelMode.Mock -> "生成演示内容"
                    else -> "调用模型生成"
                }
                Text(text)
            }
        }

        state.result?.let { result ->
            ImageSection(title = "生成结果") {
                if (result.contentType == HistoryContentType.IMAGE) {
                    Text(result.scenario.displayName, style = MaterialTheme.typography.bodyMedium)
                    ImagePreview(uriText = result.imagePreviewUri)
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
                            enabled = !state.isLoading
                        ) {
                            Text("重新生成")
                        }
                    }
                    Text("已保存到历史", style = MaterialTheme.typography.bodySmall)
                } else {
                    Text(result.scenario.displayName, style = MaterialTheme.typography.bodyMedium)
                    Text(result.content)
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            modifier = Modifier.fillMaxWidth(),
                            onClick = onEdit
                        ) {
                            Text("编辑")
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
            }
        }
    }
}

@Composable
private fun ImageGenerationOptions(
    state: CreateUiState,
    onInputChanged: (String) -> Unit,
    onImageGenerationStyleSelected: (ImageGenerationStyle) -> Unit,
    onImageAspectRatioSelected: (ImageAspectRatio) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("生成设置", style = MaterialTheme.typography.titleSmall)
        OutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            value = state.input,
            onValueChange = onInputChanged,
            label = { Text("描述你想生成的图片") },
            placeholder = { Text("例如：一只坐在窗边看雨的橘猫，温暖插画风") },
            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
            minLines = 3
        )

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
private fun ImageSection(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            content()
        }
    }
}

@Composable
private fun TextCreationOptions(
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

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    modifier = Modifier.weight(1f),
                    onClick = onSuggestStyle,
                    enabled = !state.isSuggestingStyle
                ) {
                    Text(if (state.isSuggestingStyle) "正在推荐..." else "帮我推荐风格")
                }
                Row(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("生成 3 个版本")
                    Switch(
                        checked = state.generationCount >= 3,
                        onCheckedChange = { checked ->
                            onGenerationCountChanged(if (checked) 3 else 1)
                        }
                    )
                }
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

@Composable
private fun ModeNotice(
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
            contentDescription = "图片预览",
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
