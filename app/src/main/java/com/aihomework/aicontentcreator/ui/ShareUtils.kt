package com.aihomework.aicontentcreator.ui

import android.content.Context
import android.content.Intent

fun shareText(context: Context, text: String) {
    val shareIntent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, text)
    }
    context.startActivity(Intent.createChooser(shareIntent, "分享创作内容"))
}

