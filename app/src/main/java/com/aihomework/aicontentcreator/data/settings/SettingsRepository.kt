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
        return AppSettings(
            mode = runCatching {
                ModelMode.valueOf(prefs.getString(KEY_MODE, ModelMode.Mock.name) ?: ModelMode.Mock.name)
            }.getOrDefault(ModelMode.Mock),
            baseUrl = prefs.getString(KEY_BASE_URL, AppSettings.DEFAULT_BASE_URL)
                ?: AppSettings.DEFAULT_BASE_URL,
            textModel = prefs.getString(KEY_TEXT_MODEL, AppSettings.DEFAULT_TEXT_MODEL)
                ?: AppSettings.DEFAULT_TEXT_MODEL,
            visionModel = prefs.getString(KEY_VISION_MODEL, AppSettings.DEFAULT_VISION_MODEL)
                ?: AppSettings.DEFAULT_VISION_MODEL,
            hasApiKey = prefs.contains(KEY_API_KEY_CIPHER_TEXT) && prefs.contains(KEY_API_KEY_IV)
        )
    }

    fun saveSettings(settings: AppSettings) {
        prefs.edit()
            .putString(KEY_MODE, settings.mode.name)
            .putString(KEY_BASE_URL, settings.baseUrl.trim())
            .putString(KEY_TEXT_MODEL, settings.textModel.trim())
            .putString(KEY_VISION_MODEL, settings.visionModel.trim())
            .apply()
    }

    fun saveApiKey(apiKey: String): Boolean {
        val cleanKey = apiKey.trim()
        if (cleanKey.isBlank()) return false

        return runCatching {
            val cipher = Cipher.getInstance(TRANSFORMATION)
            cipher.init(Cipher.ENCRYPT_MODE, getOrCreateSecretKey())
            val encrypted = cipher.doFinal(cleanKey.toByteArray(Charsets.UTF_8))
            prefs.edit()
                .putString(KEY_API_KEY_CIPHER_TEXT, Base64.encodeToString(encrypted, Base64.NO_WRAP))
                .putString(KEY_API_KEY_IV, Base64.encodeToString(cipher.iv, Base64.NO_WRAP))
                .apply()
        }.isSuccess
    }

    fun clearApiKey() {
        prefs.edit()
            .remove(KEY_API_KEY_CIPHER_TEXT)
            .remove(KEY_API_KEY_IV)
            .apply()
    }

    fun getApiKey(): String? {
        val encryptedText = prefs.getString(KEY_API_KEY_CIPHER_TEXT, null) ?: return null
        val ivText = prefs.getString(KEY_API_KEY_IV, null) ?: return null

        return runCatching {
            val encrypted = Base64.decode(encryptedText, Base64.NO_WRAP)
            val iv = Base64.decode(ivText, Base64.NO_WRAP)
            val cipher = Cipher.getInstance(TRANSFORMATION)
            cipher.init(Cipher.DECRYPT_MODE, getOrCreateSecretKey(), GCMParameterSpec(GCM_TAG_BITS, iv))
            String(cipher.doFinal(encrypted), Charsets.UTF_8)
        }.getOrNull()
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
        const val KEY_BASE_URL = "base_url"
        const val KEY_TEXT_MODEL = "text_model"
        const val KEY_VISION_MODEL = "vision_model"
        const val KEY_API_KEY_CIPHER_TEXT = "api_key_cipher_text"
        const val KEY_API_KEY_IV = "api_key_iv"
    }
}
