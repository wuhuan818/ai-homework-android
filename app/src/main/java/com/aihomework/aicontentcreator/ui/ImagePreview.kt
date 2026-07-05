package com.aihomework.aicontentcreator.ui

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.util.LruCache
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
internal fun ImagePreview(uriText: String?) {
    if (uriText == null) return

    val context = LocalContext.current
    val bitmap = remember(uriText) {
        loadPreviewBitmap(context, uriText)
    }
    bitmap?.let {
        Image(
            bitmap = it.asImageBitmap(),
            contentDescription = "图片预览",
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp),
            contentScale = ContentScale.Fit
        )
    }
}

@Composable
internal fun AsyncGeneratedImagePreview(
    imageFileName: String?,
    fallbackUriText: String?,
    cacheKey: String
) {
    val context = LocalContext.current
    var bitmap by remember(cacheKey) {
        mutableStateOf(generatedPreviewCache.get(cacheKey))
    }
    var isLoading by remember(cacheKey) {
        mutableStateOf(bitmap == null && (!imageFileName.isNullOrBlank() || !fallbackUriText.isNullOrBlank()))
    }
    var loadFailed by remember(cacheKey) {
        mutableStateOf(imageFileName.isNullOrBlank() && fallbackUriText.isNullOrBlank())
    }

    LaunchedEffect(cacheKey, imageFileName, fallbackUriText) {
        generatedPreviewCache.get(cacheKey)?.let {
            bitmap = it
            isLoading = false
            loadFailed = false
            return@LaunchedEffect
        }

        if (imageFileName.isNullOrBlank() && fallbackUriText.isNullOrBlank()) {
            bitmap = null
            isLoading = false
            loadFailed = true
            return@LaunchedEffect
        }

        isLoading = true
        loadFailed = false
        val loadedBitmap = withContext(Dispatchers.IO) {
            val appContext = context.applicationContext
            val uri = if (!imageFileName.isNullOrBlank()) {
                GeneratedImageFileStore(appContext).uriFor(imageFileName)
            } else {
                fallbackUriText?.let { Uri.parse(it) }
            }
            uri?.let { loadPreviewBitmap(appContext, it, GENERATED_PREVIEW_MAX_DIMENSION) }
        }
        if (loadedBitmap == null) {
            bitmap = null
            loadFailed = true
        } else {
            generatedPreviewCache.put(cacheKey, loadedBitmap)
            bitmap = loadedBitmap
            loadFailed = false
        }
        isLoading = false
    }

    when {
        bitmap != null -> {
            Image(
                bitmap = bitmap!!.asImageBitmap(),
                contentDescription = "图片预览",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp),
                contentScale = ContentScale.Fit
            )
        }
        isLoading -> Text("图片预览加载中...")
        loadFailed -> Text("图片预览加载失败。")
    }
}

private fun loadPreviewBitmap(context: Context, uriText: String): Bitmap? {
    val uri = Uri.parse(uriText)
    return loadPreviewBitmap(context, uri, MAX_PREVIEW_DIMENSION)
}

private fun loadPreviewBitmap(context: Context, uri: Uri, maxDimension: Int): Bitmap? {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
        return loadPreviewWithImageDecoder(context, uri, maxDimension)
    }

    return loadPreviewWithBitmapFactory(context, uri, maxDimension)
}

private fun loadPreviewWithImageDecoder(context: Context, uri: Uri, maxDimension: Int): Bitmap? {
    return try {
        val source = ImageDecoder.createSource(context.contentResolver, uri)
        ImageDecoder.decodeBitmap(source) { decoder, info, _ ->
            decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
            val width = info.size.width
            val height = info.size.height
            if (width > maxDimension || height > maxDimension) {
                val scale = minOf(
                    maxDimension.toFloat() / width,
                    maxDimension.toFloat() / height
                )
                decoder.setTargetSize(
                    maxOf(1, (width * scale).toInt()),
                    maxOf(1, (height * scale).toInt())
                )
            }
        }
    } catch (error: Exception) {
        null
    }
}

private fun loadPreviewWithBitmapFactory(context: Context, uri: Uri, maxDimension: Int): Bitmap? {
    val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
    context.contentResolver.openInputStream(uri)?.use { stream ->
        BitmapFactory.decodeStream(stream, null, bounds)
    } ?: return null

    if (bounds.outWidth <= 0 || bounds.outHeight <= 0) return null

    val options = BitmapFactory.Options().apply {
        inSampleSize = calculatePreviewSampleSize(bounds.outWidth, bounds.outHeight, maxDimension)
    }
    return context.contentResolver.openInputStream(uri)?.use { stream ->
        BitmapFactory.decodeStream(stream, null, options)
    }
}

private fun calculatePreviewSampleSize(width: Int, height: Int, maxDimension: Int): Int {
    var sampleSize = 1
    while (width / sampleSize > maxDimension || height / sampleSize > maxDimension) {
        sampleSize *= 2
    }
    return sampleSize
}

private val generatedPreviewCache = object : LruCache<String, Bitmap>(GENERATED_PREVIEW_CACHE_KB) {
    override fun sizeOf(key: String, value: Bitmap): Int {
        return maxOf(1, value.byteCount / 1024)
    }
}

private const val MAX_PREVIEW_DIMENSION = 900
private const val GENERATED_PREVIEW_MAX_DIMENSION = 640
private const val GENERATED_PREVIEW_CACHE_KB = 12 * 1024
