package com.aihomework.aicontentcreator.ui

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.aihomework.aicontentcreator.data.image.NormalizedCropRect
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

@Composable
internal fun ImageCropEditorDialog(
    uriText: String,
    isProcessing: Boolean,
    onDismiss: () -> Unit,
    onApply: (NormalizedCropRect) -> Unit
) {
    Dialog(
        onDismissRequest = {
            if (!isProcessing) onDismiss()
        },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = MaterialTheme.shapes.medium,
            tonalElevation = 6.dp
        ) {
            CropEditorContent(
                uriText = uriText,
                isProcessing = isProcessing,
                onDismiss = onDismiss,
                onApply = onApply
            )
        }
    }
}

@Composable
private fun CropEditorContent(
    uriText: String,
    isProcessing: Boolean,
    onDismiss: () -> Unit,
    onApply: (NormalizedCropRect) -> Unit
) {
    val context = LocalContext.current
    val density = LocalDensity.current
    val bitmap = remember(uriText) {
        loadCropPreviewBitmap(context, uriText)
    }
    var containerSize by remember { mutableStateOf(IntSize.Zero) }
    var cropRect by remember(uriText) { mutableStateOf<Rect?>(null) }
    var resetVersion by remember(uriText) { mutableIntStateOf(0) }
    val imageBounds = remember(containerSize, bitmap?.width, bitmap?.height) {
        if (bitmap == null) {
            emptyRect()
        } else {
            fittedImageRect(containerSize, bitmap.width, bitmap.height)
        }
    }

    LaunchedEffect(imageBounds, resetVersion, uriText) {
        if (imageBounds.width > 0f && imageBounds.height > 0f) {
            cropRect = defaultCropRect(imageBounds)
        }
    }

    Column(
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("框选裁剪", style = MaterialTheme.typography.titleMedium)
        if (bitmap == null) {
            Text("裁剪失败，请重新选择图片后再试。")
        } else {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(360.dp)
                    .background(Color.Black)
                    .onSizeChanged { containerSize = it }
            ) {
                Image(
                    bitmap = bitmap.asImageBitmap(),
                    contentDescription = "待裁剪图片",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit
                )
                cropRect?.let { rect ->
                    CropOverlay(cropRect = rect)
                    val cropWidth = with(density) { rect.width.toDp() }
                    val cropHeight = with(density) { rect.height.toDp() }
                    Box(
                        modifier = Modifier
                            .offset {
                                IntOffset(rect.left.roundToInt(), rect.top.roundToInt())
                            }
                            .size(cropWidth, cropHeight)
                            .pointerInput(imageBounds) {
                                detectDragGestures { change, dragAmount ->
                                    cropRect = cropRect?.let {
                                        moveCropRect(it, dragAmount, imageBounds)
                                    }
                                }
                            }
                    ) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .size(48.dp)
                                .pointerInput(imageBounds) {
                                    detectDragGestures { change, dragAmount ->
                                        cropRect = cropRect?.let {
                                            resizeCropRect(
                                                rect = it,
                                                dragAmount = dragAmount,
                                                bounds = imageBounds,
                                                minSizePx = with(density) { 72.dp.toPx() }
                                            )
                                        }
                                    }
                                }
                        )
                    }
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(
                onClick = onDismiss,
                enabled = !isProcessing
            ) {
                Text("取消")
            }
            OutlinedButton(
                onClick = { resetVersion += 1 },
                enabled = bitmap != null && !isProcessing
            ) {
                Text("重置")
            }
            Button(
                onClick = {
                    val rect = cropRect ?: return@Button
                    onApply(normalizedCropRect(rect, imageBounds))
                },
                enabled = bitmap != null && cropRect != null && !isProcessing
            ) {
                Text("应用")
            }
        }
    }
}

@Composable
private fun CropOverlay(cropRect: Rect) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val mask = Color.Black.copy(alpha = 0.45f)
        drawRect(mask, topLeft = Offset.Zero, size = Size(size.width, cropRect.top))
        drawRect(
            color = mask,
            topLeft = Offset(0f, cropRect.bottom),
            size = Size(size.width, max(0f, size.height - cropRect.bottom))
        )
        drawRect(
            color = mask,
            topLeft = Offset(0f, cropRect.top),
            size = Size(cropRect.left, cropRect.height)
        )
        drawRect(
            color = mask,
            topLeft = Offset(cropRect.right, cropRect.top),
            size = Size(max(0f, size.width - cropRect.right), cropRect.height)
        )
        drawRect(
            color = Color.White,
            topLeft = Offset(cropRect.left, cropRect.top),
            size = Size(cropRect.width, cropRect.height),
            style = Stroke(width = 3f)
        )
        val handleRadius = 9.dp.toPx()
        drawCircle(
            color = Color.White,
            radius = handleRadius,
            center = Offset(cropRect.right, cropRect.bottom)
        )
        drawLine(
            color = Color.Black,
            start = Offset(cropRect.right - 18.dp.toPx(), cropRect.bottom),
            end = Offset(cropRect.right, cropRect.bottom - 18.dp.toPx()),
            strokeWidth = 3f,
            cap = StrokeCap.Round
        )
    }
}

private fun fittedImageRect(containerSize: IntSize, imageWidth: Int, imageHeight: Int): Rect {
    if (containerSize.width <= 0 || containerSize.height <= 0 || imageWidth <= 0 || imageHeight <= 0) {
        return emptyRect()
    }
    val containerWidth = containerSize.width.toFloat()
    val containerHeight = containerSize.height.toFloat()
    val imageRatio = imageWidth.toFloat() / imageHeight.toFloat()
    val containerRatio = containerWidth / containerHeight
    val displayWidth: Float
    val displayHeight: Float
    if (containerRatio > imageRatio) {
        displayHeight = containerHeight
        displayWidth = displayHeight * imageRatio
    } else {
        displayWidth = containerWidth
        displayHeight = displayWidth / imageRatio
    }
    val left = (containerWidth - displayWidth) / 2f
    val top = (containerHeight - displayHeight) / 2f
    return Rect(left, top, left + displayWidth, top + displayHeight)
}

private fun emptyRect(): Rect = Rect(0f, 0f, 0f, 0f)

private fun defaultCropRect(bounds: Rect): Rect {
    val width = bounds.width * DEFAULT_CROP_RATIO
    val height = bounds.height * DEFAULT_CROP_RATIO
    val left = bounds.left + (bounds.width - width) / 2f
    val top = bounds.top + (bounds.height - height) / 2f
    return Rect(left, top, left + width, top + height)
}

private fun moveCropRect(rect: Rect, dragAmount: Offset, bounds: Rect): Rect {
    val left = (rect.left + dragAmount.x).coerceIn(bounds.left, bounds.right - rect.width)
    val top = (rect.top + dragAmount.y).coerceIn(bounds.top, bounds.bottom - rect.height)
    return Rect(left, top, left + rect.width, top + rect.height)
}

private fun resizeCropRect(rect: Rect, dragAmount: Offset, bounds: Rect, minSizePx: Float): Rect {
    val minSize = min(minSizePx, min(bounds.width, bounds.height))
    val right = (rect.right + dragAmount.x).coerceIn(rect.left + minSize, bounds.right)
    val bottom = (rect.bottom + dragAmount.y).coerceIn(rect.top + minSize, bounds.bottom)
    return Rect(rect.left, rect.top, right, bottom)
}

private fun normalizedCropRect(rect: Rect, bounds: Rect): NormalizedCropRect {
    return NormalizedCropRect(
        leftRatio = ((rect.left - bounds.left) / bounds.width).coerceIn(0f, 1f),
        topRatio = ((rect.top - bounds.top) / bounds.height).coerceIn(0f, 1f),
        widthRatio = (rect.width / bounds.width).coerceIn(0.01f, 1f),
        heightRatio = (rect.height / bounds.height).coerceIn(0.01f, 1f)
    )
}

private fun loadCropPreviewBitmap(context: Context, uriText: String): Bitmap? {
    val uri = Uri.parse(uriText)
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
        return loadCropPreviewWithImageDecoder(context, uri)
    }

    return loadCropPreviewWithBitmapFactory(context, uri)
}

private fun loadCropPreviewWithImageDecoder(context: Context, uri: Uri): Bitmap? {
    return try {
        val source = ImageDecoder.createSource(context.contentResolver, uri)
        ImageDecoder.decodeBitmap(source) { decoder, info, _ ->
            decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
            val width = info.size.width
            val height = info.size.height
            if (width > MAX_CROP_PREVIEW_DIMENSION || height > MAX_CROP_PREVIEW_DIMENSION) {
                val scale = min(
                    MAX_CROP_PREVIEW_DIMENSION.toFloat() / width,
                    MAX_CROP_PREVIEW_DIMENSION.toFloat() / height
                )
                decoder.setTargetSize(
                    max(1, (width * scale).roundToInt()),
                    max(1, (height * scale).roundToInt())
                )
            }
        }
    } catch (error: Exception) {
        null
    }
}

private fun loadCropPreviewWithBitmapFactory(context: Context, uri: Uri): Bitmap? {
    val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
    context.contentResolver.openInputStream(uri)?.use { stream ->
        BitmapFactory.decodeStream(stream, null, bounds)
    } ?: return null

    if (bounds.outWidth <= 0 || bounds.outHeight <= 0) return null

    val options = BitmapFactory.Options().apply {
        inSampleSize = calculateCropPreviewSampleSize(bounds.outWidth, bounds.outHeight)
        inPreferredConfig = Bitmap.Config.ARGB_8888
    }
    return context.contentResolver.openInputStream(uri)?.use { stream ->
        BitmapFactory.decodeStream(stream, null, options)
    }
}

private fun calculateCropPreviewSampleSize(width: Int, height: Int): Int {
    var sampleSize = 1
    while (width / sampleSize > MAX_CROP_PREVIEW_DIMENSION ||
        height / sampleSize > MAX_CROP_PREVIEW_DIMENSION
    ) {
        sampleSize *= 2
    }
    return sampleSize
}

private const val DEFAULT_CROP_RATIO = 0.7f
private const val MAX_CROP_PREVIEW_DIMENSION = 1200
