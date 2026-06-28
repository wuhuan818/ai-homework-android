package com.aihomework.aicontentcreator.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
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
    onToggleFavorite: (Long) -> Unit
) {
    Column(
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("历史与收藏", style = MaterialTheme.typography.headlineSmall)
        if (state.items.isEmpty()) {
            Text("暂无历史记录。请先在创作页生成内容。")
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(state.items, key = { it.id }) { item ->
                    HistoryCard(
                        item = item,
                        onOpenForEdit = { onOpenForEdit(item) },
                        onToggleFavorite = { onToggleFavorite(item.id) }
                    )
                }
            }
        }
    }
}

@Composable
private fun HistoryCard(
    item: HistoryItem,
    onOpenForEdit: () -> Unit,
    onToggleFavorite: () -> Unit
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
            OutlinedButton(onClick = onToggleFavorite) {
                Text(if (item.isFavorite) "取消收藏" else "收藏")
            }
        }
    }
}

private fun formatTime(timeMillis: Long): String {
    return SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date(timeMillis))
}

