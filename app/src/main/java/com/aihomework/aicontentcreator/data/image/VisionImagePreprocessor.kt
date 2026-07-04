package com.aihomework.aicontentcreator.data.image

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.OpenableColumns
import android.util.Base64
import java.io.ByteArrayOutputStream
import kotlin.math.max
import kotlin.math.min

data class PreparedVisionImage(
    val dataUrl: String,
    val originalWidth: Int,
    val originalHeight: Int,
    val finalWidth: Int,
    val finalHeight: Int,
    val originalBytes: Long,
    val finalBytes: Long,
    val wasCompressed: Boolean,
    val warningMessage: String?
)

class VisionImagePreparationException(
    val userMessage: String,
    cause: Throwable? = null
) : Exception(userMessage, cause)

class VisionImagePreprocessor(private val context: Context) {
    fun prepareForVisionUpload(uriText: String): PreparedVisionImage {
        if (uriText.isBlank()) {
            throw VisionImagePreparationException("请先选择图片。")
        }

        return try {
            val uri = Uri.parse(uriText)
            val mimeType = context.contentResolver.getType(uri) ?: DEFAULT_MIME_TYPE
            val bounds = readImageBounds(uri)
                ?: throw VisionImagePreparationException("无法读取所选图片，请重新选择图片。")
            val originalBytes = readOriginalByteSize(uri)

            val originalBytesForUpload = when {
                originalBytes in 1..TARGET_IMAGE_BYTES -> readBytesWithLimit(uri, TARGET_IMAGE_BYTES)
                originalBytes <= 0L && bounds.maxDimension <= INITIAL_MAX_DIMENSION ->
                    readBytesWithLimit(uri, TARGET_IMAGE_BYTES)
                else -> null
            }
            if (originalBytesForUpload != null && bounds.maxDimension <= INITIAL_MAX_DIMENSION) {
                return PreparedVisionImage(
                    dataUrl = toDataUrl(mimeType, originalBytesForUpload),
                    originalWidth = bounds.width,
                    originalHeight = bounds.height,
                    finalWidth = bounds.width,
                    finalHeight = bounds.height,
                    originalBytes = if (originalBytes > 0L) originalBytes else originalBytesForUpload.size.toLong(),
                    finalBytes = originalBytesForUpload.size.toLong(),
                    wasCompressed = false,
                    warningMessage = null
                )
            }

            compressForUpload(uri, bounds, originalBytes)
        } catch (error: VisionImagePreparationException) {
            throw error
        } catch (error: OutOfMemoryError) {
            throw VisionImagePreparationException(IMAGE_TOO_LARGE_MESSAGE, error)
        } catch (error: Exception) {
            throw VisionImagePreparationException("图片处理失败，请重新选择图片。", error)
        }
    }

    private fun compressForUpload(
        uri: Uri,
        bounds: ImageBounds,
        originalBytes: Long
    ): PreparedVisionImage {
        val decoded = decodeBitmap(uri, bounds, INITIAL_MAX_DIMENSION)
            ?: throw VisionImagePreparationException("无法读取所选图片，请重新选择图片。")

        var workingBitmap = scaleBitmap(decoded, INITIAL_MAX_DIMENSION)
        var outputBytes = compressBitmap(workingBitmap, INITIAL_JPEG_QUALITY)
        var quality = INITIAL_JPEG_QUALITY

        while (outputBytes.size > TARGET_IMAGE_BYTES && quality > MIN_JPEG_QUALITY) {
            quality = max(MIN_JPEG_QUALITY, quality - JPEG_QUALITY_STEP)
            outputBytes = compressBitmap(workingBitmap, quality)
        }

        var maxDimension = max(workingBitmap.width, workingBitmap.height)
        while (outputBytes.size > TARGET_IMAGE_BYTES && maxDimension > MIN_IMAGE_DIMENSION) {
            maxDimension = max(MIN_IMAGE_DIMENSION, (maxDimension * DIMENSION_STEP).toInt())
            val resized = scaleBitmap(workingBitmap, maxDimension)
            if (resized != workingBitmap) {
                workingBitmap.recycle()
                workingBitmap = resized
            }

            quality = INITIAL_JPEG_QUALITY
            outputBytes = compressBitmap(workingBitmap, quality)
            while (outputBytes.size > TARGET_IMAGE_BYTES && quality > MIN_JPEG_QUALITY) {
                quality = max(MIN_JPEG_QUALITY, quality - JPEG_QUALITY_STEP)
                outputBytes = compressBitmap(workingBitmap, quality)
            }
        }

        if (outputBytes.size > TARGET_IMAGE_BYTES) {
            workingBitmap.recycle()
            throw VisionImagePreparationException("图片仍然过大，无法稳定上传，请选择较小图片或先进行压缩。")
        }

        val result = PreparedVisionImage(
            dataUrl = toDataUrl(DEFAULT_MIME_TYPE, outputBytes),
            originalWidth = bounds.width,
            originalHeight = bounds.height,
            finalWidth = workingBitmap.width,
            finalHeight = workingBitmap.height,
            originalBytes = originalBytes,
            finalBytes = outputBytes.size.toLong(),
            wasCompressed = true,
            warningMessage = COMPRESSION_WARNING
        )
        workingBitmap.recycle()
        return result
    }

    private fun readImageBounds(uri: Uri): ImageBounds? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            readBoundsWithImageDecoder(uri)
        } else {
            readBoundsWithBitmapFactory(uri)
        }
    }

    private fun readBoundsWithImageDecoder(uri: Uri): ImageBounds? {
        return try {
            val source = ImageDecoder.createSource(context.contentResolver, uri)
            var width = 0
            var height = 0
            val bitmap = ImageDecoder.decodeBitmap(source) { decoder, info, _ ->
                width = info.size.width
                height = info.size.height
                decoder.setTargetSize(1, 1)
            }
            bitmap.recycle()
            if (width > 0 && height > 0) ImageBounds(width, height) else null
        } catch (error: Exception) {
            null
        }
    }

    private fun readBoundsWithBitmapFactory(uri: Uri): ImageBounds? {
        val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        context.contentResolver.openInputStream(uri)?.use { stream ->
            BitmapFactory.decodeStream(stream, null, options)
        } ?: return null

        return if (options.outWidth > 0 && options.outHeight > 0) {
            ImageBounds(options.outWidth, options.outHeight)
        } else {
            null
        }
    }

    private fun decodeBitmap(uri: Uri, bounds: ImageBounds, maxDimension: Int): Bitmap? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            decodeWithImageDecoder(uri, bounds, maxDimension)
        } else {
            decodeWithBitmapFactory(uri, bounds, maxDimension)
        }
    }

    private fun decodeWithImageDecoder(uri: Uri, bounds: ImageBounds, maxDimension: Int): Bitmap? {
        return try {
            val source = ImageDecoder.createSource(context.contentResolver, uri)
            ImageDecoder.decodeBitmap(source) { decoder, _, _ ->
                decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
                val target = bounds.scaledTo(maxDimension)
                decoder.setTargetSize(target.width, target.height)
            }
        } catch (error: Exception) {
            null
        }
    }

    private fun decodeWithBitmapFactory(uri: Uri, bounds: ImageBounds, maxDimension: Int): Bitmap? {
        val options = BitmapFactory.Options().apply {
            inSampleSize = calculateInSampleSize(bounds.width, bounds.height, maxDimension)
            inPreferredConfig = Bitmap.Config.ARGB_8888
        }
        return context.contentResolver.openInputStream(uri)?.use { stream ->
            BitmapFactory.decodeStream(stream, null, options)
        }
    }

    private fun calculateInSampleSize(width: Int, height: Int, maxDimension: Int): Int {
        var sampleSize = 1
        while (width / sampleSize > maxDimension || height / sampleSize > maxDimension) {
            sampleSize *= 2
        }
        return sampleSize
    }

    private fun scaleBitmap(bitmap: Bitmap, maxDimension: Int): Bitmap {
        val largestSide = max(bitmap.width, bitmap.height)
        if (largestSide <= maxDimension) return bitmap

        val scale = maxDimension.toFloat() / largestSide
        val targetWidth = max(1, (bitmap.width * scale).toInt())
        val targetHeight = max(1, (bitmap.height * scale).toInt())
        return Bitmap.createScaledBitmap(bitmap, targetWidth, targetHeight, true)
    }

    private fun compressBitmap(bitmap: Bitmap, quality: Int): ByteArray {
        val output = ByteArrayOutputStream()
        if (!bitmap.compress(Bitmap.CompressFormat.JPEG, quality, output)) {
            throw VisionImagePreparationException("图片压缩失败，请重新选择图片。")
        }
        return output.toByteArray()
    }

    private fun readOriginalByteSize(uri: Uri): Long {
        val queriedSize = runCatching {
            context.contentResolver.query(uri, arrayOf(OpenableColumns.SIZE), null, null, null)?.use { cursor ->
                val index = cursor.getColumnIndex(OpenableColumns.SIZE)
                if (index >= 0 && cursor.moveToFirst()) cursor.getLong(index) else -1L
            } ?: -1L
        }.getOrDefault(-1L)
        if (queriedSize > 0L) return queriedSize

        return runCatching {
            context.contentResolver.openAssetFileDescriptor(uri, "r")?.use { descriptor ->
                descriptor.length
            } ?: -1L
        }.getOrDefault(-1L)
    }

    private fun readBytesWithLimit(uri: Uri, maxBytes: Long): ByteArray? {
        return try {
            context.contentResolver.openInputStream(uri)?.use { input ->
                val output = ByteArrayOutputStream()
                val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
                var total = 0L
                while (true) {
                    val read = input.read(buffer)
                    if (read == -1) break
                    total += read
                    if (total > maxBytes) return null
                    output.write(buffer, 0, read)
                }
                output.toByteArray()
            }
        } catch (error: Exception) {
            null
        }
    }

    private fun toDataUrl(mimeType: String, bytes: ByteArray): String {
        return "data:$mimeType;base64,${Base64.encodeToString(bytes, Base64.NO_WRAP)}"
    }

    private data class ImageBounds(
        val width: Int,
        val height: Int
    ) {
        val maxDimension: Int
            get() = max(width, height)

        fun scaledTo(maxDimension: Int): ImageBounds {
            val largestSide = max(width, height)
            if (largestSide <= maxDimension) return this

            val scale = maxDimension.toFloat() / largestSide
            return ImageBounds(
                width = max(1, (width * scale).toInt()),
                height = max(1, (height * scale).toInt())
            )
        }
    }

    private companion object {
        const val INITIAL_MAX_DIMENSION = 1600
        const val MIN_IMAGE_DIMENSION = 640
        const val TARGET_IMAGE_BYTES = 1800 * 1024L
        const val INITIAL_JPEG_QUALITY = 85
        const val MIN_JPEG_QUALITY = 60
        const val JPEG_QUALITY_STEP = 5
        const val DEFAULT_MIME_TYPE = "image/jpeg"
        const val DIMENSION_STEP = 0.85f
        const val COMPRESSION_WARNING = "图片较大，已压缩用于识别，可能对细节识别有一定影响。"
        const val IMAGE_TOO_LARGE_MESSAGE = "图片过大，处理失败，请选择较小图片或先进行压缩。"
    }
}
