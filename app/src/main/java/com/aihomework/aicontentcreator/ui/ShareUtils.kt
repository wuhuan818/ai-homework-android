package com.aihomework.aicontentcreator.ui

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore

fun shareText(context: Context, text: String) {
    val shareIntent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, text)
    }
    context.startActivity(Intent.createChooser(shareIntent, "分享创作内容"))
}

fun shareImage(context: Context, uriText: String) {
    val shareIntent = Intent(Intent.ACTION_SEND).apply {
        type = "image/*"
        putExtra(Intent.EXTRA_STREAM, Uri.parse(uriText))
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    context.startActivity(Intent.createChooser(shareIntent, "分享图片"))
}

fun saveImageToGallery(context: Context, uriText: String): String? {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
        return "当前系统版本请使用分享功能保存图片。"
    }

    return runCatching {
        val sourceUri = Uri.parse(uriText)
        val resolver = context.contentResolver
        val fileName = "AIContentCreator_${System.currentTimeMillis()}.png"
        val values = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, fileName)
            put(MediaStore.Images.Media.MIME_TYPE, "image/png")
            put(MediaStore.Images.Media.RELATIVE_PATH, "${Environment.DIRECTORY_PICTURES}/AIContentCreator")
            put(MediaStore.Images.Media.IS_PENDING, 1)
        }
        val targetUri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
            ?: return "保存到相册失败，请稍后重试。"

        try {
            resolver.openInputStream(sourceUri)?.use { input ->
                resolver.openOutputStream(targetUri)?.use { output ->
                    input.copyTo(output)
                } ?: return "保存到相册失败，请稍后重试。"
            } ?: return "图片文件不可用，无法保存。"

            val publishedValues = ContentValues().apply {
                put(MediaStore.Images.Media.IS_PENDING, 0)
            }
            resolver.update(targetUri, publishedValues, null, null)
            null
        } catch (error: Exception) {
            resolver.delete(targetUri, null, null)
            "保存到相册失败，请稍后重试。"
        }
    }.getOrElse {
        "保存到相册失败，请稍后重试。"
    }
}
