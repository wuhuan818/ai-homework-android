package com.aihomework.aicontentcreator.data.history

import android.content.Context
import android.content.SharedPreferences
import com.aihomework.aicontentcreator.data.model.CreationScenario
import com.aihomework.aicontentcreator.data.model.HistoryItem
import com.aihomework.aicontentcreator.data.model.HistoryContentType
import com.aihomework.aicontentcreator.data.model.ImageAspectRatio
import com.aihomework.aicontentcreator.data.model.ImageGenerationStyle
import com.aihomework.aicontentcreator.data.security.CryptoManager
import com.aihomework.aicontentcreator.data.security.EncryptedPayload
import org.json.JSONArray
import org.json.JSONObject

enum class HistoryStorageStatus {
    NotInitialized,
    Encrypted,
    LoadFailed,
    SaveFailed
}

data class HistoryLoadResult(
    val items: List<HistoryItem>,
    val status: HistoryStorageStatus
)

class EncryptedHistoryStorage(
    context: Context,
    private val cryptoManager: CryptoManager = CryptoManager()
) {
    private val prefs: SharedPreferences =
        context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun loadHistory(): HistoryLoadResult {
        val cipherText = prefs.getString(KEY_CIPHER_TEXT, null)
        val iv = prefs.getString(KEY_IV, null)
        if (cipherText.isNullOrBlank() || iv.isNullOrBlank()) {
            return HistoryLoadResult(emptyList(), HistoryStorageStatus.NotInitialized)
        }

        val json = cryptoManager.decrypt(EncryptedPayload(cipherText, iv)).getOrElse {
            return HistoryLoadResult(emptyList(), HistoryStorageStatus.LoadFailed)
        }
        val items = runCatching { deserializeHistory(json) }.getOrElse {
            return HistoryLoadResult(emptyList(), HistoryStorageStatus.LoadFailed)
        }
        return HistoryLoadResult(items, HistoryStorageStatus.Encrypted)
    }

    fun saveHistory(items: List<HistoryItem>): HistoryStorageStatus {
        val json = serializeHistory(items)
        val payload = cryptoManager.encrypt(json).getOrElse {
            return HistoryStorageStatus.SaveFailed
        }
        prefs.edit()
            .putInt(KEY_VERSION, STORAGE_VERSION)
            .putLong(KEY_UPDATED_AT, System.currentTimeMillis())
            .putString(KEY_CIPHER_TEXT, payload.cipherText)
            .putString(KEY_IV, payload.iv)
            .apply()
        return HistoryStorageStatus.Encrypted
    }

    fun clearHistory() {
        prefs.edit()
            .remove(KEY_VERSION)
            .remove(KEY_UPDATED_AT)
            .remove(KEY_CIPHER_TEXT)
            .remove(KEY_IV)
            .apply()
    }

    private fun serializeHistory(items: List<HistoryItem>): String {
        val array = JSONArray()
        items.forEach { item ->
            array.put(
                JSONObject()
                    .put("id", item.id)
                    .put("scenario", item.scenario.name)
                    .put("input", item.input)
                    .put("content", item.content)
                    .put("createdAtMillis", item.createdAtMillis)
                    .put("isFavorite", item.isFavorite)
                    .put("contentType", item.contentType.name)
                    .put("imageFileName", item.imageFileName)
                    .put("imageGenerationStyle", item.imageGenerationStyle?.name)
                    .put("imageAspectRatio", item.imageAspectRatio?.name)
                    .put("isMockImage", item.isMockImage)
            )
        }
        return JSONObject()
            .put("version", STORAGE_VERSION)
            .put("items", array)
            .toString()
    }

    private fun deserializeHistory(json: String): List<HistoryItem> {
        val root = JSONObject(json)
        val array = root.optJSONArray("items") ?: JSONArray()
        return List(array.length()) { index ->
            val item = array.getJSONObject(index)
            HistoryItem(
                id = item.getLong("id"),
                scenario = CreationScenario.valueOf(item.getString("scenario")),
                input = item.optString("input"),
                content = item.optString("content"),
                createdAtMillis = item.getLong("createdAtMillis"),
                isFavorite = item.optBoolean("isFavorite", false),
                contentType = item.optEnum("contentType", HistoryContentType.TEXT),
                imageFileName = item.optNullableString("imageFileName"),
                imageGenerationStyle = item.optNullableEnum<ImageGenerationStyle>("imageGenerationStyle"),
                imageAspectRatio = item.optNullableEnum<ImageAspectRatio>("imageAspectRatio"),
                isMockImage = item.optBoolean("isMockImage", false)
            )
        }
    }

    private inline fun <reified T : Enum<T>> JSONObject.optEnum(key: String, fallback: T): T {
        return optNullableEnum<T>(key) ?: fallback
    }

    private inline fun <reified T : Enum<T>> JSONObject.optNullableEnum(key: String): T? {
        val value = optNullableString(key) ?: return null
        return enumValues<T>().firstOrNull { it.name == value }
    }

    private fun JSONObject.optNullableString(key: String): String? {
        if (!has(key) || isNull(key)) return null
        return optString(key).takeIf { it.isNotBlank() }
    }

    private companion object {
        const val PREFS_NAME = "ai_content_creator_history"
        const val KEY_VERSION = "version"
        const val KEY_UPDATED_AT = "updated_at"
        const val KEY_CIPHER_TEXT = "history_cipher_text"
        const val KEY_IV = "history_iv"
        const val STORAGE_VERSION = 1
    }
}
