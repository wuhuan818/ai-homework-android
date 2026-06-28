package com.aihomework.aicontentcreator.data.repository

import com.aihomework.aicontentcreator.data.model.CreationResult
import com.aihomework.aicontentcreator.data.model.HistoryItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

class HistoryRepository {
    private val history = MutableStateFlow<List<HistoryItem>>(emptyList())

    val items: StateFlow<List<HistoryItem>> = history

    fun addResult(result: CreationResult) {
        val item = HistoryItem(
            id = result.id,
            scenario = result.scenario,
            input = result.originalInput,
            content = result.content,
            createdAtMillis = result.createdAtMillis
        )
        history.update { current -> listOf(item) + current }
    }

    fun updateContent(id: Long, content: String) {
        history.update { current ->
            current.map { item ->
                if (item.id == id) item.copy(content = content) else item
            }
        }
    }

    fun toggleFavorite(id: Long) {
        history.update { current ->
            current.map { item ->
                if (item.id == id) item.copy(isFavorite = !item.isFavorite) else item
            }
        }
    }
}

