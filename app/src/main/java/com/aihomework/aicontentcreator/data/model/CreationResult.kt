package com.aihomework.aicontentcreator.data.model

data class CreationResult(
    val id: Long,
    val scenario: CreationScenario,
    val originalInput: String,
    val content: String,
    val createdAtMillis: Long
)

