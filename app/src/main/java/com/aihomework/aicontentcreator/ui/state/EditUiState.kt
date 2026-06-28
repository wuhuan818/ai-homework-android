package com.aihomework.aicontentcreator.ui.state

import com.aihomework.aicontentcreator.data.model.CreationScenario

data class EditUiState(
    val itemId: Long? = null,
    val scenario: CreationScenario? = null,
    val text: String = "",
    val message: String? = null
)

