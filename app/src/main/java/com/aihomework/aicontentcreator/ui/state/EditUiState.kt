package com.aihomework.aicontentcreator.ui.state

import com.aihomework.aicontentcreator.data.model.CreationScenario

data class EditUiState(
    val itemId: Long? = null,
    val scenario: CreationScenario? = null,
    val text: String = "",
    val previousEditText: String? = null,
    val rewriteOriginalText: String? = null,
    val rewriteCandidateText: String? = null,
    val isRewriting: Boolean = false,
    val rewriteMessage: String? = null,
    val message: String? = null
)
