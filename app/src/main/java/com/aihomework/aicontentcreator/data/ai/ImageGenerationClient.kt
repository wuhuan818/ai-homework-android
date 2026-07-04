package com.aihomework.aicontentcreator.data.ai

import com.aihomework.aicontentcreator.data.model.ImageAspectRatio
import com.aihomework.aicontentcreator.data.model.ImageGenerationResult
import com.aihomework.aicontentcreator.data.model.ImageGenerationStyle

interface ImageGenerationClient {
    suspend fun generateImage(
        prompt: String,
        style: ImageGenerationStyle,
        aspectRatio: ImageAspectRatio
    ): ImageGenerationResult
}
