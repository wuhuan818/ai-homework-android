package com.aihomework.aicontentcreator.ui

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

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

private fun loadPreviewBitmap(context: Context, uriText: String): Bitmap? {
    val uri = Uri.parse(uriText)
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
        return loadPreviewWithImageDecoder(context, uri)
    }

    return loadPreviewWithBitmapFactory(context, uri)
}

private fun loadPreviewWithImageDecoder(context: Context, uri: Uri): Bitmap? {
    return try {
        val source = ImageDecoder.createSource(context.contentResolver, uri)
        ImageDecoder.decodeBitmap(source) { decoder, info, _ ->
            decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
            val width = info.size.width
            val height = info.size.height
            if (width > MAX_PREVIEW_DIMENSION || height > MAX_PREVIEW_DIMENSION) {
                val scale = minOf(
                    MAX_PREVIEW_DIMENSION.toFloat() / width,
                    MAX_PREVIEW_DIMENSION.toFloat() / height
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

private fun loadPreviewWithBitmapFactory(context: Context, uri: Uri): Bitmap? {
    val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
    context.contentResolver.openInputStream(uri)?.use { stream ->
        BitmapFactory.decodeStream(stream, null, bounds)
    } ?: return null

    if (bounds.outWidth <= 0 || bounds.outHeight <= 0) return null

    val options = BitmapFactory.Options().apply {
        inSampleSize = calculatePreviewSampleSize(bounds.outWidth, bounds.outHeight)
    }
    return context.contentResolver.openInputStream(uri)?.use { stream ->
        BitmapFactory.decodeStream(stream, null, options)
    }
}

private fun calculatePreviewSampleSize(width: Int, height: Int): Int {
    var sampleSize = 1
    while (width / sampleSize > MAX_PREVIEW_DIMENSION || height / sampleSize > MAX_PREVIEW_DIMENSION) {
        sampleSize *= 2
    }
    return sampleSize
}

private const val MAX_PREVIEW_DIMENSION = 900
