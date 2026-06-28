package com.aihomework.aicontentcreator.data.model

data class HistoryItem(
    val id: Long,
    val scenario: CreationScenario,
    val input: String,
    val content: String,
    val createdAtMillis: Long,
    val isFavorite: Boolean = false
) {
    val summary: String
        get() = content.replace('\n', ' ').take(64)
}

