package com.aihomework.aicontentcreator.ui

import android.content.Context
import android.content.Intent
import android.net.Uri

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
    context.startActivity(Intent.createChooser(shareIntent, "分享处理后的图片"))
}
