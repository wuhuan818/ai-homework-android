package com.aihomework.aicontentcreator.ui

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.aihomework.aicontentcreator.data.model.HistoryItem
import com.aihomework.aicontentcreator.ui.state.HistoryUiState
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HistoryScreen(
    state: HistoryUiState,
    onOpenForEdit: (HistoryItem) -> Unit,
    onToggleFavorite: (Long) -> Unit,
    onDeleteItem: (Long) -> String?,
    onClearHistory: () -> Unit
) {
    val context = LocalContext.current
    var selectedFilter by remember { mutableStateOf(HistoryFilter.All) }
    var pendingDeleteItem by remember { mutableStateOf<HistoryItem?>(null) }
    var showClearConfirmation by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf<String?>(null) }
    val visibleItems = remember(state.items, selectedFilter) {
        when (selectedFilter) {
            HistoryFilter.All -> state.items
            HistoryFilter.Favorites -> state.items.filter { it.isFavorite }
        }
    }

    LaunchedEffect(message) {
        message?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            message = null
        }
    }

    pendingDeleteItem?.let { item ->
        AlertDialog(
            onDismissRequest = { pendingDeleteItem = null },
            title = { Text("确认删除这条历史？") },
            text = { Text("删除后，本地加密保存的该条作品也会被移除。") },
            confirmButton = {
                TextButton(
                    onClick = {
                        val errorMessage = onDeleteItem(item.id)
                        if (errorMessage != null) {
                            message = errorMessage
                        }
                        pendingDeleteItem = null
                    }
                ) {
                    Text("删除")
                }
            },
            dismissButton = {
                TextButton(onClick = { pendingDeleteItem = null }) {
                    Text("取消")
                }
            }
        )
    }

    if (showClearConfirmation) {
        AlertDialog(
            onDismissRequest = { showClearConfirmation = false },
            title = { Text("确认清空历史？") },
            text = { Text("清空后，本地加密保存的全部历史作品都会被删除。") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onClearHistory()
                        showClearConfirmation = false
                    }
                ) {
                    Text("清空")
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearConfirmation = false }) {
                    Text("取消")
                }
            }
        )
    }

    Column(
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("历史作品", style = MaterialTheme.typography.headlineSmall)
        StorageStatusBlock(state.storageStatus)
        if (state.items.isEmpty()) {
            Text("暂无历史作品，请先在创作页生成内容。")
        } else {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                HistoryFilter.entries.forEach { filter ->
                    FilterChip(
                        selected = selectedFilter == filter,
                        onClick = { selectedFilter = filter },
                        label = { Text(filter.label) }
                    )
                }
            }
            Button(onClick = { showClearConfirmation = true }) {
                Text("清空历史")
            }
            if (visibleItems.isEmpty()) {
                Text("暂无收藏作品")
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(visibleItems, key = { it.id }) { item ->
                        HistoryCard(
                            item = item,
                            onOpenForEdit = { onOpenForEdit(item) },
                            onToggleFavorite = { onToggleFavorite(item.id) },
                            onDelete = { pendingDeleteItem = item }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StorageStatusBlock(storageStatus: String) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text("本地存储状态", style = MaterialTheme.typography.titleMedium)
            Text("历史记录：已本地保存")
            Text("加密方式：Android Keystore + AES-GCM")
            Text("明文保护：生成内容不会直接写入本地文件")
            Text(storageStatus)
        }
    }
}

@Composable
private fun HistoryCard(
    item: HistoryItem,
    onOpenForEdit: () -> Unit,
    onToggleFavorite: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onOpenForEdit)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(item.scenario.displayName, style = MaterialTheme.typography.titleMedium)
                Text(if (item.isFavorite) "已收藏" else "未收藏")
            }
            Text(formatTime(item.createdAtMillis), style = MaterialTheme.typography.bodySmall)
            Text(item.summary)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = onToggleFavorite) {
                    Text(if (item.isFavorite) "取消收藏" else "收藏")
                }
                OutlinedButton(onClick = onDelete) {
                    Text("删除")
                }
            }
        }
    }
}

private enum class HistoryFilter(val label: String) {
    All("全部"),
    Favorites("收藏夹")
}

private fun formatTime(timeMillis: Long): String {
    return SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date(timeMillis))
}
