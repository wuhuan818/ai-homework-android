package com.aihomework.aicontentcreator.data.repository

import com.aihomework.aicontentcreator.data.ai.ModelClient
import com.aihomework.aicontentcreator.data.model.CreationRequest
import com.aihomework.aicontentcreator.data.model.CreationResult

class CreationRepository(
    private val modelClient: ModelClient
) {
    suspend fun generate(request: CreationRequest): CreationResult {
        return modelClient.generate(request)
    }
}

