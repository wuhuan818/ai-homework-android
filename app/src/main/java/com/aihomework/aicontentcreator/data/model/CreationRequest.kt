package com.aihomework.aicontentcreator.data.model

data class CreationRequest(
    val scenario: CreationScenario,
    val input: String,
    val imageLabel: String? = null,
    val imageUri: String? = null,
    val imageDescriptionStyle: ImageDescriptionStyle = ImageDescriptionStyle.Objective
)
