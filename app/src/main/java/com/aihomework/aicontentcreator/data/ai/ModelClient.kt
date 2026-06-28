package com.aihomework.aicontentcreator.data.ai

import com.aihomework.aicontentcreator.data.model.CreationRequest
import com.aihomework.aicontentcreator.data.model.CreationResult

interface ModelClient {
    suspend fun generate(request: CreationRequest): CreationResult
}

