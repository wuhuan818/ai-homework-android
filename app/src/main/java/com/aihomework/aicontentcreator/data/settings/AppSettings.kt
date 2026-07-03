package com.aihomework.aicontentcreator.data.settings

enum class ModelMode {
    Mock,
    Real
}

data class AppSettings(
    val mode: ModelMode = ModelMode.Mock,
    val baseUrl: String = DEFAULT_BASE_URL,
    val textModel: String = DEFAULT_TEXT_MODEL,
    val visionModel: String = DEFAULT_VISION_MODEL,
    val hasApiKey: Boolean = false,
    val keyStorageDescription: String = "Android Keystore 加密本地保存"
) {
    companion object {
        const val DEFAULT_BASE_URL = "https://api.openai.com/v1"
        const val DEFAULT_TEXT_MODEL = "gpt-4o-mini"
        const val DEFAULT_VISION_MODEL = "gpt-4o-mini"
    }
}
