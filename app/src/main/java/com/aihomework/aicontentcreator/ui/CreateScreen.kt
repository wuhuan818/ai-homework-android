package com.aihomework.aicontentcreator.ui

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import com.aihomework.aicontentcreator.data.model.CreationScenario
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
    onOptimizeImagePrompt: () -> Unit,
    onApplyOptimizedImagePrompt: () -> Unit,
    onKeepOriginalImagePrompt: () -> Unit,
    onImagePromptExampleSelected: (String) -> Unit,
    onPrepareTextToImagePrompt: () -> Unit,
    onApplyTextToImagePrompt: () -> Unit,
    onUseOriginalTextToImagePrompt: () -> Unit,
    onGenerate: () -> Unit,
    onEdit: () -> Unit,
    onUseResultForImagePrompt: () -> Unit,
    onFavorite: () -> Unit,
    onShare: () -> Unit,
    onSaveImage: () -> Unit,
    onCopyResult: () -> Unit,
    isCurrentResultFavorite: Boolean,
    onMessageShown: () -> Unit
) {
    val context = LocalContext.current
    var isScenarioSelectorExpanded by remember { mutableStateOf(true) }

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

        CreationScenarioSelector(
            selectedScenario = state.selectedScenario,
            expanded = isScenarioSelectorExpanded,
            onExpand = { isScenarioSelectorExpanded = true },
            onScenarioSelected = { scenario ->
                onScenarioSelected(scenario)
                isScenarioSelectorExpanded = false
            }
        )

        CreationSection(title = "当前场景设置") {
            Text(state.selectedScenario.displayName, style = MaterialTheme.typography.titleSmall)
            Text(state.selectedScenario.description)
            if (state.selectedScenario == CreationScenario.ImageGeneration) {
                ImageGenerationPanel(
                    state = state,
                    onInputChanged = onInputChanged,
                    onOptimizeImagePrompt = onOptimizeImagePrompt,
                    onApplyOptimizedImagePrompt = onApplyOptimizedImagePrompt,
                    onKeepOriginalImagePrompt = onKeepOriginalImagePrompt,
                    onImagePromptExampleSelected = onImagePromptExampleSelected,
                    onPrepareTextToImagePrompt = onPrepareTextToImagePrompt,
                    onApplyTextToImagePrompt = onApplyTextToImagePrompt,
                    onUseOriginalTextToImagePrompt = onUseOriginalTextToImagePrompt,
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
            TextCreationPanel(
                state = state,
                onTextStyleSelected = onTextStyleSelected,
                onGenerationCountChanged = onGenerationCountChanged,
                onSuggestStyle = onSuggestStyle
            )
        }

        if (state.selectedScenario == CreationScenario.ImageDescription) {
            ImageDescriptionPanel(
                state = state,
                onImageDescriptionStyleSelected = onImageDescriptionStyleSelected,
                onChooseImage = onChooseImage,
                onUseMockImage = onUseMockImage,
                onRotateImage = onRotateImage,
                onRestoreOriginalImage = onRestoreOriginalImage,
                onShareProcessedImage = onShareProcessedImage,
                onWatermarkTextChanged = onWatermarkTextChanged,
                onApplyGrayscale = onApplyGrayscale,
                onAddWatermark = onAddWatermark,
                onCropImage = onCropImage
            )
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
                CircularProgressIndicator()
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
            CreationResultCard(
                result = result,
                isLoading = state.isLoading,
                isCurrentResultFavorite = isCurrentResultFavorite,
                onSaveImage = onSaveImage,
                onShare = onShare,
                onFavorite = onFavorite,
                onGenerate = onGenerate,
                onEdit = onEdit,
                onUseResultForImagePrompt = onUseResultForImagePrompt,
                onCopyResult = onCopyResult
            )
        }
    }
}
