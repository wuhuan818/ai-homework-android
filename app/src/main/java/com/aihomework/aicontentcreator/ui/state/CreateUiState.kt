package com.aihomework.aicontentcreator.ui.state

import com.aihomework.aicontentcreator.data.model.CreationResult
import com.aihomework.aicontentcreator.data.model.CreationScenario

data class CreateUiState(
    val selectedScenario: CreationScenario = CreationScenario.Moments,
    val input: String = "",
    val selectedImageUri: String? = null,
    val isLoading: Boolean = false,
    val result: CreationResult? = null,
    val message: String? = null
)
