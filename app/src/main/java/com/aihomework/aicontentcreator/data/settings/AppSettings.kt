package com.aihomework.aicontentcreator.data.settings

enum class ModelMode {
    Mock,
    Real
}

data class ModelProfile(
    val id: String,
    val name: String,
    val baseUrl: String,
    val textModel: String,
    val visionModel: String,
    val hasApiKey: Boolean = false
)

data class AppSettings(
    val mode: ModelMode = ModelMode.Mock,
    val activeProfileId: String = DEFAULT_PROFILE_ID,
    val profiles: List<ModelProfile> = defaultProfiles(),
    val keyStorageDescription: String = "Android Keystore 加密本地保存"
) {
    val activeProfile: ModelProfile
        get() = profiles.firstOrNull { it.id == activeProfileId } ?: profiles.first()

    val baseUrl: String
        get() = activeProfile.baseUrl

    val textModel: String
        get() = activeProfile.textModel

    val visionModel: String
        get() = activeProfile.visionModel

    val hasApiKey: Boolean
        get() = activeProfile.hasApiKey

    fun updateActiveProfile(transform: (ModelProfile) -> ModelProfile): AppSettings {
        return copy(
            profiles = profiles.map { profile ->
                if (profile.id == activeProfile.id) transform(profile) else profile
            }
        )
    }

    companion object {
        const val DEFAULT_PROFILE_ID = "default"
        const val DEFAULT_BASE_URL = "https://api.openai.com/v1"
        const val DEFAULT_TEXT_MODEL = "gpt-4o-mini"
        const val DEFAULT_VISION_MODEL = "gpt-4o-mini"

        fun defaultProfiles(): List<ModelProfile> {
            return listOf(
                ModelProfile(
                    id = DEFAULT_PROFILE_ID,
                    name = "默认配置",
                    baseUrl = DEFAULT_BASE_URL,
                    textModel = DEFAULT_TEXT_MODEL,
                    visionModel = DEFAULT_VISION_MODEL
                ),
                ModelProfile(
                    id = "backup_one",
                    name = "备用配置一",
                    baseUrl = "",
                    textModel = "",
                    visionModel = ""
                ),
                ModelProfile(
                    id = "backup_two",
                    name = "备用配置二",
                    baseUrl = "",
                    textModel = "",
                    visionModel = ""
                )
            )
        }
    }
}
