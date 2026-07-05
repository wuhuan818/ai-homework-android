package com.aihomework.aicontentcreator.data.image

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.ImageDecoder
import android.graphics.Matrix
import android.graphics.Paint
import android.net.Uri
import android.os.Build
import androidx.core.content.res.ResourcesCompat
import androidx.core.content.FileProvider
import com.aihomework.aicontentcreator.R
import com.aihomework.aicontentcreator.data.model.ImageAspectRatio
import java.io.File
import java.io.FileOutputStream
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

data class ImageProcessResult(
    val uri: Uri? = null,
    val errorMessage: String? = null
)

data class NormalizedCropRect(
    val leftRatio: Float,
    val topRatio: Float,
    val widthRatio: Float,
    val heightRatio: Float
)

class ImageProcessor(private val context: Context) {
    fun createSampleImage(): ImageProcessResult {
        return try {
            val drawable = ResourcesCompat.getDrawable(
                context.resources,
                R.drawable.sample_city_image,
                null
            ) ?: return ImageProcessResult(errorMessage = "无法加载示例图片。")
            val bitmap = Bitmap.createBitmap(
                SAMPLE_IMAGE_WIDTH,
                SAMPLE_IMAGE_HEIGHT,
                Bitmap.Config.ARGB_8888
            )
            val canvas = Canvas(bitmap)
            drawable.setBounds(0, 0, SAMPLE_IMAGE_WIDTH, SAMPLE_IMAGE_HEIGHT)
            drawable.draw(canvas)
            val uri = saveBitmap(bitmap)
            bitmap.recycle()
            ImageProcessResult(uri = uri)
        } catch (error: Exception) {
            ImageProcessResult(errorMessage = "示例图片加载失败，请重试。")
        }
    }

    fun rotateImage(uriText: String, degrees: Float): ImageProcessResult {
        return process(uriText) { source ->
            val matrix = Matrix().apply { postRotate(degrees) }
            Bitmap.createBitmap(source, 0, 0, source.width, source.height, matrix, true)
        }
    }

    fun applyGrayscale(uriText: String): ImageProcessResult {
        return process(uriText) { source ->
            val output = Bitmap.createBitmap(source.width, source.height, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(output)
            val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                colorFilter = ColorMatrixColorFilter(ColorMatrix().apply { setSaturation(0f) })
            }
            canvas.drawBitmap(source, 0f, 0f, paint)
            output
        }
    }

    fun centerCrop(uriText: String, aspectRatio: ImageAspectRatio): ImageProcessResult {
        return process(uriText) { source ->
            val targetRatio = aspectRatio.width.toFloat() / aspectRatio.height.toFloat()
            val sourceRatio = source.width.toFloat() / source.height.toFloat()
            val cropWidth: Int
            val cropHeight: Int
            if (sourceRatio > targetRatio) {
                cropHeight = source.height
                cropWidth = max(1, (cropHeight * targetRatio).toInt())
            } else {
                cropWidth = source.width
                cropHeight = max(1, (cropWidth / targetRatio).toInt())
            }
            val left = max(0, (source.width - cropWidth) / 2)
            val top = max(0, (source.height - cropHeight) / 2)
            Bitmap.createBitmap(source, left, top, cropWidth, cropHeight)
        }
    }

    fun cropImage(uriText: String, cropRect: NormalizedCropRect): ImageProcessResult {
        return process(
            uriText = uriText,
            readFailureMessage = "裁剪失败，请重新选择图片后再试。",
            processFailureMessage = "裁剪失败，请重新选择图片后再试。"
        ) { source ->
            val left = (cropRect.leftRatio.coerceIn(0f, 1f) * source.width)
                .roundToInt()
                .coerceIn(0, source.width - 1)
            val top = (cropRect.topRatio.coerceIn(0f, 1f) * source.height)
                .roundToInt()
                .coerceIn(0, source.height - 1)
            val maxWidth = source.width - left
            val maxHeight = source.height - top
            val cropWidth = (cropRect.widthRatio.coerceIn(0.01f, 1f) * source.width)
                .roundToInt()
                .coerceIn(1, maxWidth)
            val cropHeight = (cropRect.heightRatio.coerceIn(0.01f, 1f) * source.height)
                .roundToInt()
                .coerceIn(1, maxHeight)
            Bitmap.createBitmap(source, left, top, cropWidth, cropHeight)
        }
    }

    fun addTextWatermark(uriText: String, text: String): ImageProcessResult {
        val watermark = text.trim()
        if (watermark.isBlank()) {
            return ImageProcessResult(errorMessage = "请输入水印文字")
        }

        return process(uriText) { source ->
            val output = source.copy(Bitmap.Config.ARGB_8888, true)
            val canvas = Canvas(output)
            val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.WHITE
                textSize = max(32f, min(output.width, output.height) / 16f)
                setShadowLayer(6f, 2f, 2f, Color.BLACK)
            }
            val safeText = watermark.take(MAX_WATERMARK_LENGTH)
            val padding = max(24f, min(output.width, output.height) / 32f)
            val textWidth = paint.measureText(safeText)
            val x = max(padding, output.width - textWidth - padding)
            val y = output.height - padding
            canvas.drawText(safeText, x, y, paint)
            output
        }
    }

    private fun process(
        uriText: String,
        readFailureMessage: String = "无法读取所选图片，请重新选择图片",
        processFailureMessage: String = "图片处理失败，请重试",
        transform: (Bitmap) -> Bitmap
    ): ImageProcessResult {
        val source = decodeBitmap(uriText)
            ?: return ImageProcessResult(errorMessage = readFailureMessage)

        return try {
            val output = transform(source)
            val uri = saveBitmap(output)
            if (output != source) {
                output.recycle()
            }
            source.recycle()
            ImageProcessResult(uri = uri)
        } catch (error: OutOfMemoryError) {
            source.recycle()
            ImageProcessResult(errorMessage = "图片过大，处理失败，请选择较小的图片")
        } catch (error: Exception) {
            source.recycle()
            ImageProcessResult(errorMessage = processFailureMessage)
        }
    }

    private fun decodeBitmap(uriText: String): Bitmap? {
        val uri = Uri.parse(uriText)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            return decodeWithImageDecoder(uri)
        }

        return decodeWithBitmapFactory(uri)
    }

    private fun decodeWithImageDecoder(uri: Uri): Bitmap? {
        return try {
            val source = ImageDecoder.createSource(context.contentResolver, uri)
            ImageDecoder.decodeBitmap(source) { decoder, info, _ ->
                decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
                val width = info.size.width
                val height = info.size.height
                if (width > MAX_IMAGE_DIMENSION || height > MAX_IMAGE_DIMENSION) {
                    val scale = min(
                        MAX_IMAGE_DIMENSION.toFloat() / width,
                        MAX_IMAGE_DIMENSION.toFloat() / height
                    )
                    decoder.setTargetSize(
                        max(1, (width * scale).toInt()),
                        max(1, (height * scale).toInt())
                    )
                }
            }
        } catch (error: Exception) {
            null
        }
    }

    private fun decodeWithBitmapFactory(uri: Uri): Bitmap? {
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        context.contentResolver.openInputStream(uri)?.use { stream ->
            BitmapFactory.decodeStream(stream, null, bounds)
        } ?: return null

        if (bounds.outWidth <= 0 || bounds.outHeight <= 0) return null

        val options = BitmapFactory.Options().apply {
            inSampleSize = calculateInSampleSize(bounds.outWidth, bounds.outHeight)
            inPreferredConfig = Bitmap.Config.ARGB_8888
        }

        return context.contentResolver.openInputStream(uri)?.use { stream ->
            BitmapFactory.decodeStream(stream, null, options)
        }
    }

    private fun calculateInSampleSize(width: Int, height: Int): Int {
        var sampleSize = 1
        while (width / sampleSize > MAX_IMAGE_DIMENSION || height / sampleSize > MAX_IMAGE_DIMENSION) {
            sampleSize *= 2
        }
        return sampleSize
    }

    private fun saveBitmap(bitmap: Bitmap): Uri {
        val directory = File(context.cacheDir, SHARED_IMAGE_DIRECTORY).apply {
            mkdirs()
        }
        val file = File(directory, "processed_${System.currentTimeMillis()}.jpg")
        FileOutputStream(file).use { output ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, JPEG_QUALITY, output)
        }
        return FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
    }

    companion object {
        private const val SHARED_IMAGE_DIRECTORY = "shared_images"
        private const val MAX_IMAGE_DIMENSION = 2048
        private const val MAX_WATERMARK_LENGTH = 80
        private const val JPEG_QUALITY = 92
        private const val SAMPLE_IMAGE_WIDTH = 640
        private const val SAMPLE_IMAGE_HEIGHT = 400
    }
}
