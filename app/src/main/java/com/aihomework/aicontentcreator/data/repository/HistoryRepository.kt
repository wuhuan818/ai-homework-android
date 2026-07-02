package com.aihomework.aicontentcreator.data.repository

import android.content.Context
import com.aihomework.aicontentcreator.data.history.EncryptedHistoryStorage
import com.aihomework.aicontentcreator.data.history.HistoryStorageStatus
import com.aihomework.aicontentcreator.data.model.CreationResult
import com.aihomework.aicontentcreator.data.model.HistoryItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class HistoryRepository(context: Context) {
    private val storage = EncryptedHistoryStorage(context)
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
            createdAtMillis = result.createdAtMillis
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

    fun clearHistory() {
        storage.clearHistory()
        history.value = emptyList()
        storageStatus.value = HistoryStorageStatus.NotInitialized
    }

    private fun updateHistory(transform: (List<HistoryItem>) -> List<HistoryItem>) {
        val updated = transform(history.value)
        history.value = updated
        storageStatus.value = storage.saveHistory(updated)
    }
}
