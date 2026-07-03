package com.aihomework.aicontentcreator.ui.state

import com.aihomework.aicontentcreator.data.model.CreationResult
import com.aihomework.aicontentcreator.data.model.CreationScenario
import com.aihomework.aicontentcreator.data.model.ImageDescriptionStyle

data class CreateUiState(
    val selectedScenario: CreationScenario = CreationScenario.Moments,
    val input: String = "",
    val imageDescriptionStyle: ImageDescriptionStyle = ImageDescriptionStyle.Objective,
    val selectedImageUri: String? = null,
    val processedImageUri: String? = null,
    val watermarkText: String = "",
    val imageProcessingMessage: String? = null,
    val isImageProcessing: Boolean = false,
    val isLoading: Boolean = false,
    val result: CreationResult? = null,
    val message: String? = null
)
