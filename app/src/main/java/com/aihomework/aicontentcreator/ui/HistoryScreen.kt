package com.aihomework.aicontentcreator.ui

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.util.LruCache
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.aihomework.aicontentcreator.data.image.GeneratedImageFileStore
import com.aihomework.aicontentcreator.data.model.HistoryContentType
import com.aihomework.aicontentcreator.data.model.HistoryItem
import com.aihomework.aicontentcreator.ui.state.HistoryUiState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HistoryScreen(
    state: HistoryUiState,
    onOpenForEdit: (HistoryItem) -> Unit,
    onReuseText: (HistoryItem) -> Unit,
    onRegenerateImage: (HistoryItem) -> Unit,
    onToggleFavorite: (Long) -> Unit,
    onShareText: (HistoryItem) -> Unit,
    onShareImage: (HistoryItem) -> String?,
    onSaveImage: (HistoryItem) -> String?,
    onDeleteItem: (Long) -> String?,
    onClearHistory: () -> Unit
) {
    val context = LocalContext.current
    var selectedFilter by remember { mutableStateOf(HistoryFilter.All) }
    var pendingDeleteItem by remember { mutableStateOf<HistoryItem?>(null) }
    var showClearConfirmation by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf<String?>(null) }
    val thumbnailCache = remember { HistoryThumbnailCache() }
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
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("历史作品", style = MaterialTheme.typography.headlineSmall)
            OutlinedButton(
                onClick = { showClearConfirmation = true },
                enabled = state.items.isNotEmpty()
            ) {
                Text("清空历史")
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            HistoryFilter.entries.forEach { filter ->
                FilterChip(
                    selected = selectedFilter == filter,
                    onClick = { selectedFilter = filter },
                    label = { Text(filter.label) }
                )
            }
        }
        Text(state.storageStatus, style = MaterialTheme.typography.bodySmall)
        if (state.items.isEmpty()) {
            Text("暂无历史作品，请先在创作页生成内容。")
        } else {
            if (visibleItems.isEmpty()) {
                Text("暂无收藏作品")
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(visibleItems, key = { it.id }) { item ->
                        HistoryCard(
                            item = item,
                            thumbnailCache = thumbnailCache,
                            onOpenForEdit = { onOpenForEdit(item) },
                            onReuseText = { onReuseText(item) },
                            onRegenerateImage = { onRegenerateImage(item) },
                            onToggleFavorite = { onToggleFavorite(item.id) },
                            onShareText = { onShareText(item) },
                            onShareImage = {
                                val errorMessage = onShareImage(item)
                                if (errorMessage != null) {
                                    message = errorMessage
                                }
                            },
                            onSaveImage = {
                                val errorMessage = onSaveImage(item)
                                message = errorMessage ?: "图片已保存到相册。"
                            },
                            onDelete = { pendingDeleteItem = item }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun HistoryCard(
    item: HistoryItem,
    thumbnailCache: HistoryThumbnailCache,
    onOpenForEdit: () -> Unit,
    onReuseText: () -> Unit,
    onRegenerateImage: () -> Unit,
    onToggleFavorite: () -> Unit,
    onShareText: () -> Unit,
    onShareImage: () -> Unit,
    onSaveImage: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                enabled = item.contentType == HistoryContentType.TEXT,
                onClick = onOpenForEdit
            )
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(item.scenario.displayName, style = MaterialTheme.typography.titleMedium)
                if (item.isFavorite) {
                    Text("已收藏")
                }
            }
            Text(formatTime(item.createdAtMillis), style = MaterialTheme.typography.bodySmall)
            if (item.contentType == HistoryContentType.IMAGE) {
                HistoryImagePreview(
                    fileName = item.imageFileName,
                    thumbnailCache = thumbnailCache
                )
                Text("风格：${item.imageGenerationStyle?.displayName ?: "未记录"}")
                Text("比例：${item.imageAspectRatio?.displayName ?: "未记录"}")
                Text(item.summary, maxLines = 3)
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            modifier = Modifier.weight(1f),
                            onClick = onSaveImage
                        ) {
                            Text("保存到相册")
                        }
                        OutlinedButton(
                            modifier = Modifier.weight(1f),
                            onClick = onShareImage
                        ) {
                            Text("分享")
                        }
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            modifier = Modifier.weight(1f),
                            onClick = onRegenerateImage
                        ) {
                            Text("再次生成")
                        }
                        OutlinedButton(
                            modifier = Modifier.weight(1f),
                            onClick = onToggleFavorite
                        ) {
                            Text(if (item.isFavorite) "取消收藏" else "收藏")
                        }
                    }
                    OutlinedButton(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = onDelete
                    ) {
                        Text("删除")
                    }
                }
            } else {
                Text(item.summary)
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            modifier = Modifier.weight(1f),
                            onClick = onOpenForEdit
                        ) {
                            Text("编辑")
                        }
                        OutlinedButton(
                            modifier = Modifier.weight(1f),
                            onClick = onReuseText
                        ) {
                            Text("再次使用")
                        }
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            modifier = Modifier.weight(1f),
                            onClick = onToggleFavorite
                        ) {
                            Text(if (item.isFavorite) "取消收藏" else "收藏")
                        }
                        OutlinedButton(
                            modifier = Modifier.weight(1f),
                            onClick = onShareText
                        ) {
                            Text("分享")
                        }
                    }
                    OutlinedButton(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = onDelete
                    ) {
                            Text("删除")
                    }
                }
            }
        }
    }
}

@Composable
private fun HistoryImagePreview(
    fileName: String?,
    thumbnailCache: HistoryThumbnailCache
) {
    val context = LocalContext.current
    var bitmap by remember(fileName) {
        mutableStateOf(fileName?.let { thumbnailCache.get(it) })
    }
    var isLoading by remember(fileName) {
        mutableStateOf(fileName != null && bitmap == null)
    }
    var loadFailed by remember(fileName) {
        mutableStateOf(fileName == null)
    }

    LaunchedEffect(fileName) {
        if (fileName.isNullOrBlank()) {
            bitmap = null
            isLoading = false
            loadFailed = true
            return@LaunchedEffect
        }

        thumbnailCache.get(fileName)?.let {
            bitmap = it
            isLoading = false
            loadFailed = false
            return@LaunchedEffect
        }

        isLoading = true
        loadFailed = false
        val loadedBitmap = withContext(Dispatchers.IO) {
            val appContext = context.applicationContext
            val uri = GeneratedImageFileStore(appContext).uriFor(fileName)
            uri?.let { loadHistoryThumbnail(appContext, it) }
        }
        if (loadedBitmap == null) {
            bitmap = null
            loadFailed = true
        } else {
            thumbnailCache.put(fileName, loadedBitmap)
            bitmap = loadedBitmap
            loadFailed = false
        }
        isLoading = false
    }

    when {
        bitmap != null -> {
            Image(
                bitmap = bitmap!!.asImageBitmap(),
                contentDescription = "图片作品缩略图",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp),
                contentScale = ContentScale.Fit
            )
        }
        isLoading -> Text("图片缩略图加载中...")
        loadFailed -> Text("图片文件不可用")
    }
}

private enum class HistoryFilter(val label: String) {
    All("全部"),
    Favorites("收藏夹")
}

private fun formatTime(timeMillis: Long): String {
    return SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date(timeMillis))
}

private class HistoryThumbnailCache {
    private val cache = object : LruCache<String, Bitmap>(HISTORY_THUMBNAIL_CACHE_KB) {
        override fun sizeOf(key: String, value: Bitmap): Int {
            return maxOf(1, value.byteCount / 1024)
        }
    }

    fun get(fileName: String): Bitmap? = cache.get(fileName)

    fun put(fileName: String, bitmap: Bitmap) {
        cache.put(fileName, bitmap)
    }
}

private fun loadHistoryThumbnail(context: Context, uri: Uri): Bitmap? {
    return try {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            val source = ImageDecoder.createSource(context.contentResolver, uri)
            ImageDecoder.decodeBitmap(source) { decoder, info, _ ->
                decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
                val width = info.size.width
                val height = info.size.height
                if (width > HISTORY_THUMBNAIL_MAX_DIMENSION || height > HISTORY_THUMBNAIL_MAX_DIMENSION) {
                    val scale = minOf(
                        HISTORY_THUMBNAIL_MAX_DIMENSION.toFloat() / width,
                        HISTORY_THUMBNAIL_MAX_DIMENSION.toFloat() / height
                    )
                    decoder.setTargetSize(
                        maxOf(1, (width * scale).toInt()),
                        maxOf(1, (height * scale).toInt())
                    )
                }
            }
        } else {
            val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            context.contentResolver.openInputStream(uri)?.use { stream ->
                BitmapFactory.decodeStream(stream, null, bounds)
            } ?: return null

            if (bounds.outWidth <= 0 || bounds.outHeight <= 0) return null

            val options = BitmapFactory.Options().apply {
                inSampleSize = calculateHistoryThumbnailSampleSize(bounds.outWidth, bounds.outHeight)
            }
            context.contentResolver.openInputStream(uri)?.use { stream ->
                BitmapFactory.decodeStream(stream, null, options)
            }
        }
    } catch (error: Exception) {
        null
    }
}

private fun calculateHistoryThumbnailSampleSize(width: Int, height: Int): Int {
    var sampleSize = 1
    while (width / sampleSize > HISTORY_THUMBNAIL_MAX_DIMENSION ||
        height / sampleSize > HISTORY_THUMBNAIL_MAX_DIMENSION
    ) {
        sampleSize *= 2
    }
    return sampleSize
}

private const val HISTORY_THUMBNAIL_MAX_DIMENSION = 320
private const val HISTORY_THUMBNAIL_CACHE_KB = 8 * 1024
