package com.aihomework.aicontentcreator.data.model

data class CreationRequest(
    val scenario: CreationScenario,
    val input: String,
    val imageLabel: String? = null,
    val imageUri: String? = null,
    val imageDescriptionStyle: ImageDescriptionStyle = ImageDescriptionStyle.Objective,
    val textStyle: TextCreationStyle = TextCreationStyle.WarmDaily,
    val generationCount: Int = 1,
    val imageGenerationStyle: ImageGenerationStyle = ImageGenerationStyle.RealisticPhoto,
    val imageAspectRatio: ImageAspectRatio = ImageAspectRatio.Square
)
