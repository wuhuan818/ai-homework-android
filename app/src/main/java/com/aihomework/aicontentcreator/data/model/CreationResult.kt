package com.aihomework.aicontentcreator.data.model

data class CreationResult(
    val id: Long,
    val scenario: CreationScenario,
    val originalInput: String,
    val content: String,
    val createdAtMillis: Long,
    val warningMessage: String? = null,
    val contentType: HistoryContentType = HistoryContentType.TEXT,
    val imageFileName: String? = null,
    val imagePreviewUri: String? = null,
    val imageGenerationStyle: ImageGenerationStyle? = null,
    val imageAspectRatio: ImageAspectRatio? = null,
    val isMockImage: Boolean = false
)
