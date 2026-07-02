package com.aihomework.aicontentcreator.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
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
    onToggleFavorite: (Long) -> Unit,
    onClearHistory: () -> Unit
) {
    Column(
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("History and Favorites", style = MaterialTheme.typography.headlineSmall)
        StorageStatusBlock(state.storageStatus)
        if (state.items.isEmpty()) {
            Text("No history yet. Generate content from the Create page first.")
        } else {
            Button(onClick = onClearHistory) {
                Text("Clear history")
            }
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
private fun StorageStatusBlock(storageStatus: String) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text("Storage status", style = MaterialTheme.typography.titleMedium)
            Text("History records: locally saved when generated or updated")
            Text("Method: Android Keystore + AES-GCM")
            Text("Plaintext: generated content is not written directly to local files")
            Text(storageStatus)
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
                Text(if (item.isFavorite) "Favorited" else "Not favorited")
            }
            Text(formatTime(item.createdAtMillis), style = MaterialTheme.typography.bodySmall)
            Text(item.summary)
            OutlinedButton(onClick = onToggleFavorite) {
                Text(if (item.isFavorite) "Unfavorite" else "Favorite")
            }
        }
    }
}

private fun formatTime(timeMillis: Long): String {
    return SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date(timeMillis))
}
