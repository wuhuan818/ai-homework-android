package com.aihomework.aicontentcreator.data.ai

class ModelClientException(
    val userMessage: String,
    cause: Throwable? = null,
    val allowImageMockFallback: Boolean = false
) : Exception(userMessage, cause)
