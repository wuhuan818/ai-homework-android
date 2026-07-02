package com.aihomework.aicontentcreator.ui.state

import com.aihomework.aicontentcreator.data.model.HistoryItem

data class HistoryUiState(
    val items: List<HistoryItem> = emptyList(),
    val storageStatus: String = "Storage: not initialized"
)
