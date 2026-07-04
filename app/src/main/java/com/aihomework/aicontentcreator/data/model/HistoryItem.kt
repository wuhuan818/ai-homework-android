package com.aihomework.aicontentcreator.data.model

data class HistoryItem(
    val id: Long,
    val scenario: CreationScenario,
    val input: String,
    val content: String,
    val createdAtMillis: Long,
    val isFavorite: Boolean = false,
    val contentType: HistoryContentType = HistoryContentType.TEXT,
    val imageFileName: String? = null,
    val imageGenerationStyle: ImageGenerationStyle? = null,
    val imageAspectRatio: ImageAspectRatio? = null,
    val isMockImage: Boolean = false
) {
    val summary: String
        get() = if (contentType == HistoryContentType.IMAGE) {
            input.replace('\n', ' ').take(64)
        } else {
            content.replace('\n', ' ').take(64)
        }
}
