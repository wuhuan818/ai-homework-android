package com.aihomework.aicontentcreator.data.repository

import android.content.Context
import com.aihomework.aicontentcreator.data.history.EncryptedHistoryStorage
import com.aihomework.aicontentcreator.data.history.HistoryStorageStatus
import com.aihomework.aicontentcreator.data.image.GeneratedImageFileStore
import com.aihomework.aicontentcreator.data.model.CreationResult
import com.aihomework.aicontentcreator.data.model.HistoryContentType
import com.aihomework.aicontentcreator.data.model.HistoryItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class HistoryRepository(context: Context) {
    private val storage = EncryptedHistoryStorage(context)
    private val imageFileStore = GeneratedImageFileStore(context.applicationContext)
    private val initialLoad = storage.loadHistory()
    private val history = MutableStateFlow(initialLoad.items)
    private val storageStatus = MutableStateFlow(initialLoad.status)

    val items: StateFlow<List<HistoryItem>> = history
    val status: StateFlow<HistoryStorageStatus> = storageStatus

    fun addResult(result: CreationResult) {
        val item = HistoryItem(
            id = result.id,
            scenario = result.scenario,
            input = result.originalInput,
            content = result.content,
            createdAtMillis = result.createdAtMillis,
            contentType = result.contentType,
            imageFileName = result.imageFileName,
            imageGenerationStyle = result.imageGenerationStyle,
            imageAspectRatio = result.imageAspectRatio,
            isMockImage = result.isMockImage
        )
        updateHistory { current -> listOf(item) + current }
    }

    fun updateContent(id: Long, content: String) {
        updateHistory { current ->
            current.map { item ->
                if (item.id == id) item.copy(content = content) else item
            }
        }
    }

    fun toggleFavorite(id: Long) {
        updateHistory { current ->
            current.map { item ->
                if (item.id == id) item.copy(isFavorite = !item.isFavorite) else item
            }
        }
    }

    fun deleteItem(id: String): String? {
        val itemId = id.toLongOrNull() ?: return "删除失败，请稍后重试。"
        val current = history.value
        if (current.none { it.id == itemId }) {
            return "未找到要删除的历史。"
        }

        val deletedItem = current.first { it.id == itemId }
        val imageDeleted = if (deletedItem.contentType == HistoryContentType.IMAGE) {
            imageFileStore.delete(deletedItem.imageFileName)
        } else {
            true
        }
        val updated = current.filterNot { it.id == itemId }
        history.value = updated
        storageStatus.value = storage.saveHistory(updated)
        return if (storageStatus.value == HistoryStorageStatus.SaveFailed) {
            "删除后保存失败，请稍后重试。"
        } else if (!imageDeleted) {
            "历史已删除，但图片文件清理失败。"
        } else {
            null
        }
    }

    fun clearHistory() {
        storage.clearHistory()
        imageFileStore.clearAll()
        history.value = emptyList()
        storageStatus.value = HistoryStorageStatus.NotInitialized
    }

    private fun updateHistory(transform: (List<HistoryItem>) -> List<HistoryItem>) {
        val updated = transform(history.value)
        history.value = updated
        storageStatus.value = storage.saveHistory(updated)
    }
}
