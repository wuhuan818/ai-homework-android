package com.aihomework.aicontentcreator.data.ai

class ModelClientException(
    val userMessage: String,
    cause: Throwable? = null
) : Exception(userMessage, cause)
