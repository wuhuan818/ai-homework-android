package com.aihomework.aicontentcreator.data.model

data class ImageGenerationResult(
    val id: Long,
    val prompt: String,
    val style: ImageGenerationStyle,
    val aspectRatio: ImageAspectRatio,
    val imageFileName: String,
    val previewUri: String,
    val createdAtMillis: Long,
    val isMock: Boolean
)
