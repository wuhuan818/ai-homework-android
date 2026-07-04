package com.aihomework.aicontentcreator.data.ai

import com.aihomework.aicontentcreator.data.model.CreationRequest
import com.aihomework.aicontentcreator.data.model.CreationResult
import com.aihomework.aicontentcreator.data.model.CreationScenario
import com.aihomework.aicontentcreator.data.model.RewriteAction
import com.aihomework.aicontentcreator.data.model.StyleAdvice

interface ModelClient {
    suspend fun generate(request: CreationRequest): CreationResult

    suspend fun suggestStyles(scenario: CreationScenario, input: String): List<StyleAdvice>

    suspend fun rewriteText(text: String, action: RewriteAction): String

    suspend fun optimizeImagePrompt(input: String): String
}
