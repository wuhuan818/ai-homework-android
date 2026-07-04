package com.aihomework.aicontentcreator.data.ai

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import com.aihomework.aicontentcreator.data.image.GeneratedImageFileStore
import com.aihomework.aicontentcreator.data.model.ImageAspectRatio
import com.aihomework.aicontentcreator.data.model.ImageGenerationResult
import com.aihomework.aicontentcreator.data.model.ImageGenerationStyle
import kotlinx.coroutines.delay

class MockImageGenerationClient(context: Context) : ImageGenerationClient {
    private val fileStore = GeneratedImageFileStore(context.applicationContext)

    override suspend fun generateImage(
        prompt: String,
        style: ImageGenerationStyle,
        aspectRatio: ImageAspectRatio
    ): ImageGenerationResult {
        delay(500)
        val cleanPrompt = prompt.trim()
        if (cleanPrompt.isBlank()) {
            throw ModelClientException("请先输入图片描述。")
        }

        val bitmap = Bitmap.createBitmap(
            aspectRatio.previewWidth(),
            aspectRatio.previewHeight(),
            Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(bitmap)
        drawPlaceholder(canvas, bitmap.width, bitmap.height, cleanPrompt, style, aspectRatio)
        val stored = fileStore.saveBitmap(bitmap)
        bitmap.recycle()

        val now = System.currentTimeMillis()
        return ImageGenerationResult(
            id = now,
            prompt = cleanPrompt,
            style = style,
            aspectRatio = aspectRatio,
            imageFileName = stored.fileName,
            previewUri = stored.uri.toString(),
            createdAtMillis = now,
            isMock = true
        )
    }

    private fun drawPlaceholder(
        canvas: Canvas,
        width: Int,
        height: Int,
        prompt: String,
        style: ImageGenerationStyle,
        aspectRatio: ImageAspectRatio
    ) {
        canvas.drawColor(Color.rgb(235, 239, 244))
        val accent = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(61, 98, 145)
            this.style = Paint.Style.FILL
        }
        canvas.drawRect(0f, 0f, width.toFloat(), (height * 0.24f), accent)

        val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            textSize = maxOf(30f, width / 22f)
            isFakeBoldText = true
        }
        canvas.drawText("演示模式生成", width * 0.08f, height * 0.11f, titlePaint)

        val bodyPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(28, 34, 43)
            textSize = maxOf(24f, width / 30f)
        }
        val smallPaint = Paint(bodyPaint).apply {
            color = Color.rgb(82, 92, 105)
            textSize = maxOf(20f, width / 38f)
        }

        var y = height * 0.34f
        y = drawWrappedText(canvas, "风格：${style.displayName}", width, y, bodyPaint)
        y = drawWrappedText(canvas, "比例：${aspectRatio.displayName}", width, y + 14f, bodyPaint)
        drawWrappedText(canvas, "提示词：${prompt.take(MAX_PROMPT_SUMMARY)}", width, y + 24f, smallPaint)
    }

    private fun drawWrappedText(
        canvas: Canvas,
        text: String,
        width: Int,
        startY: Float,
        paint: Paint
    ): Float {
        val maxWidth = width * 0.84f
        val words = text.chunked(18)
        var y = startY
        words.take(MAX_LINES).forEach { line ->
            val bounds = Rect()
            paint.getTextBounds(line, 0, line.length, bounds)
            canvas.drawText(line, width * 0.08f, y, paint)
            y += bounds.height() + 18f
        }
        if (paint.measureText(text) <= maxWidth && words.size == 1) {
            return y
        }
        return y
    }

    private fun ImageAspectRatio.previewWidth(): Int {
        val scale = minOf(1f, MAX_PREVIEW_EDGE.toFloat() / maxOf(width, height))
        return maxOf(1, (width * scale).toInt())
    }

    private fun ImageAspectRatio.previewHeight(): Int {
        val scale = minOf(1f, MAX_PREVIEW_EDGE.toFloat() / maxOf(width, height))
        return maxOf(1, (height * scale).toInt())
    }

    private companion object {
        const val MAX_PREVIEW_EDGE = 1024
        const val MAX_PROMPT_SUMMARY = 80
        const val MAX_LINES = 5
    }
}
