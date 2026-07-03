package com.aihomework.aicontentcreator.data.settings

import android.content.Context
import android.content.SharedPreferences
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

class SettingsRepository(context: Context) {
    private val appContext = context.applicationContext
    private val prefs: SharedPreferences =
        appContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun loadSettings(): AppSettings {
        val profiles = AppSettings.defaultProfiles().map { defaultProfile ->
            defaultProfile.copy(
                name = prefs.getString(profileKey(defaultProfile.id, PROFILE_NAME), defaultProfile.name)
                    ?: defaultProfile.name,
                baseUrl = loadProfileValue(defaultProfile.id, PROFILE_BASE_URL, defaultProfile.baseUrl, KEY_BASE_URL),
                textModel = loadProfileValue(defaultProfile.id, PROFILE_TEXT_MODEL, defaultProfile.textModel, KEY_TEXT_MODEL),
                visionModel = loadProfileValue(defaultProfile.id, PROFILE_VISION_MODEL, defaultProfile.visionModel, KEY_VISION_MODEL),
                hasApiKey = hasEncryptedApiKey(defaultProfile.id)
            )
        }
        val activeProfileId = prefs.getString(KEY_ACTIVE_PROFILE_ID, AppSettings.DEFAULT_PROFILE_ID)
            ?: AppSettings.DEFAULT_PROFILE_ID
        return AppSettings(
            mode = runCatching {
                ModelMode.valueOf(prefs.getString(KEY_MODE, ModelMode.Mock.name) ?: ModelMode.Mock.name)
            }.getOrDefault(ModelMode.Mock),
            activeProfileId = if (profiles.any { it.id == activeProfileId }) {
                activeProfileId
            } else {
                AppSettings.DEFAULT_PROFILE_ID
            },
            profiles = profiles
        )
    }

    fun saveSettings(settings: AppSettings) {
        val editor = prefs.edit()
            .putString(KEY_MODE, settings.mode.name)
            .putString(KEY_ACTIVE_PROFILE_ID, settings.activeProfile.id)
        settings.profiles.forEach { profile ->
            editor
                .putString(profileKey(profile.id, PROFILE_NAME), profile.name.trim())
                .putString(profileKey(profile.id, PROFILE_BASE_URL), profile.baseUrl.trim())
                .putString(profileKey(profile.id, PROFILE_TEXT_MODEL), profile.textModel.trim())
                .putString(profileKey(profile.id, PROFILE_VISION_MODEL), profile.visionModel.trim())
        }
        editor.apply()
    }

    fun saveApiKey(apiKey: String, profileId: String): Boolean {
        val cleanKey = apiKey.trim()
        if (cleanKey.isBlank()) return false

        return runCatching {
            val cipher = Cipher.getInstance(TRANSFORMATION)
            cipher.init(Cipher.ENCRYPT_MODE, getOrCreateSecretKey())
            val encrypted = cipher.doFinal(cleanKey.toByteArray(Charsets.UTF_8))
            prefs.edit()
                .putString(apiKeyCipherTextKey(profileId), Base64.encodeToString(encrypted, Base64.NO_WRAP))
                .putString(apiKeyIvKey(profileId), Base64.encodeToString(cipher.iv, Base64.NO_WRAP))
                .apply()
        }.isSuccess
    }

    fun clearApiKey(profileId: String) {
        val editor = prefs.edit()
            .remove(apiKeyCipherTextKey(profileId))
            .remove(apiKeyIvKey(profileId))
        if (profileId == AppSettings.DEFAULT_PROFILE_ID) {
            editor
                .remove(KEY_API_KEY_CIPHER_TEXT)
                .remove(KEY_API_KEY_IV)
        }
        editor
            .apply()
    }

    fun getApiKey(profileId: String): String? {
        val encryptedText = prefs.getString(apiKeyCipherTextKey(profileId), null)
            ?: if (profileId == AppSettings.DEFAULT_PROFILE_ID) {
                prefs.getString(KEY_API_KEY_CIPHER_TEXT, null)
            } else {
                null
            }
            ?: return null
        val ivText = prefs.getString(apiKeyIvKey(profileId), null)
            ?: if (profileId == AppSettings.DEFAULT_PROFILE_ID) {
                prefs.getString(KEY_API_KEY_IV, null)
            } else {
                null
            }
            ?: return null

        return runCatching {
            val encrypted = Base64.decode(encryptedText, Base64.NO_WRAP)
            val iv = Base64.decode(ivText, Base64.NO_WRAP)
            val cipher = Cipher.getInstance(TRANSFORMATION)
            cipher.init(Cipher.DECRYPT_MODE, getOrCreateSecretKey(), GCMParameterSpec(GCM_TAG_BITS, iv))
            String(cipher.doFinal(encrypted), Charsets.UTF_8)
        }.getOrNull()
    }

    private fun loadProfileValue(
        profileId: String,
        field: String,
        defaultValue: String,
        legacyKey: String
    ): String {
        return prefs.getString(profileKey(profileId, field), null)
            ?: if (profileId == AppSettings.DEFAULT_PROFILE_ID) {
                prefs.getString(legacyKey, defaultValue)
            } else {
                defaultValue
            }
            ?: defaultValue
    }

    private fun hasEncryptedApiKey(profileId: String): Boolean {
        val hasProfileKey = prefs.contains(apiKeyCipherTextKey(profileId)) && prefs.contains(apiKeyIvKey(profileId))
        val hasLegacyKey = profileId == AppSettings.DEFAULT_PROFILE_ID &&
            prefs.contains(KEY_API_KEY_CIPHER_TEXT) &&
            prefs.contains(KEY_API_KEY_IV)
        return hasProfileKey || hasLegacyKey
    }

    private fun getOrCreateSecretKey(): SecretKey {
        val keyStore = KeyStore.getInstance(KEYSTORE_PROVIDER).apply { load(null) }
        val existing = keyStore.getEntry(KEY_ALIAS, null) as? KeyStore.SecretKeyEntry
        if (existing != null) return existing.secretKey

        val keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, KEYSTORE_PROVIDER)
        val keySpec = KeyGenParameterSpec.Builder(
            KEY_ALIAS,
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
        )
            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            .setRandomizedEncryptionRequired(true)
            .build()
        keyGenerator.init(keySpec)
        return keyGenerator.generateKey()
    }

    private companion object {
        const val PREFS_NAME = "ai_content_creator_settings"
        const val KEYSTORE_PROVIDER = "AndroidKeyStore"
        const val KEY_ALIAS = "ai_content_creator_api_key"
        const val TRANSFORMATION = "AES/GCM/NoPadding"
        const val GCM_TAG_BITS = 128
        const val KEY_MODE = "mode"
        const val KEY_ACTIVE_PROFILE_ID = "active_profile_id"
        const val KEY_BASE_URL = "base_url"
        const val KEY_TEXT_MODEL = "text_model"
        const val KEY_VISION_MODEL = "vision_model"
        const val KEY_API_KEY_CIPHER_TEXT = "api_key_cipher_text"
        const val KEY_API_KEY_IV = "api_key_iv"
        const val PROFILE_NAME = "name"
        const val PROFILE_BASE_URL = "base_url"
        const val PROFILE_TEXT_MODEL = "text_model"
        const val PROFILE_VISION_MODEL = "vision_model"

        fun profileKey(profileId: String, field: String): String {
            return "profile_${profileId}_$field"
        }

        fun apiKeyCipherTextKey(profileId: String): String {
            return "profile_${profileId}_api_key_cipher_text"
        }

        fun apiKeyIvKey(profileId: String): String {
            return "profile_${profileId}_api_key_iv"
        }
    }
}
