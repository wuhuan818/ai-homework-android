package com.aihomework.aicontentcreator.data.repository

import com.aihomework.aicontentcreator.data.ai.ModelClient
import com.aihomework.aicontentcreator.data.model.CreationRequest
import com.aihomework.aicontentcreator.data.model.CreationResult
import com.aihomework.aicontentcreator.data.model.CreationScenario
import com.aihomework.aicontentcreator.data.model.RewriteAction
import com.aihomework.aicontentcreator.data.model.StyleAdvice

class CreationRepository(
    private val modelClient: ModelClient
) {
    suspend fun generate(request: CreationRequest): CreationResult {
        return modelClient.generate(request)
    }

    suspend fun suggestStyles(scenario: CreationScenario, input: String): List<StyleAdvice> {
        return modelClient.suggestStyles(scenario, input)
    }

    suspend fun rewriteText(text: String, action: RewriteAction): String {
        return modelClient.rewriteText(text, action)
    }

    suspend fun optimizeImagePrompt(input: String): String {
        return modelClient.optimizeImagePrompt(input)
    }
}
