package com.aihomework.aicontentcreator.ui.state

import com.aihomework.aicontentcreator.data.model.CreationResult
import com.aihomework.aicontentcreator.data.model.CreationScenario
import com.aihomework.aicontentcreator.data.model.ImageDescriptionStyle
import com.aihomework.aicontentcreator.data.model.ImageAspectRatio
import com.aihomework.aicontentcreator.data.model.ImageGenerationStyle
import com.aihomework.aicontentcreator.data.model.StyleAdvice
import com.aihomework.aicontentcreator.data.model.TextCreationStyle

data class CreateUiState(
    val selectedScenario: CreationScenario = CreationScenario.Moments,
    val input: String = "",
    val imageDescriptionStyle: ImageDescriptionStyle = ImageDescriptionStyle.Objective,
    val imageGenerationStyle: ImageGenerationStyle = ImageGenerationStyle.RealisticPhoto,
    val imageAspectRatio: ImageAspectRatio = ImageAspectRatio.Square,
    val textStyle: TextCreationStyle = TextCreationStyle.WarmDaily,
    val generationCount: Int = 1,
    val styleAdvice: List<StyleAdvice> = emptyList(),
    val styleAdviceMessage: String? = null,
    val selectedImageUri: String? = null,
    val processedImageUri: String? = null,
    val watermarkText: String = "",
    val imageProcessingMessage: String? = null,
    val imageUploadNotice: String? = null,
    val isImageProcessing: Boolean = false,
    val isSuggestingStyle: Boolean = false,
    val isOptimizingImagePrompt: Boolean = false,
    val imagePromptOriginal: String? = null,
    val optimizedImagePrompt: String? = null,
    val isLoading: Boolean = false,
    val result: CreationResult? = null,
    val message: String? = null
)
