package com.aihomework.aicontentcreator.data.image

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

data class StoredGeneratedImage(
    val fileName: String,
    val uri: Uri
)

class GeneratedImageFileStore(private val context: Context) {
    fun saveBitmap(bitmap: Bitmap): StoredGeneratedImage {
        val file = newImageFile()
        FileOutputStream(file).use { output ->
            bitmap.compress(Bitmap.CompressFormat.PNG, PNG_QUALITY, output)
        }
        return StoredGeneratedImage(file.name, contentUriFor(file))
    }

    fun saveBytes(bytes: ByteArray): StoredGeneratedImage {
        val file = newImageFile()
        FileOutputStream(file).use { output ->
            output.write(bytes)
        }
        return StoredGeneratedImage(file.name, contentUriFor(file))
    }

    fun uriFor(fileName: String?): Uri? {
        if (fileName.isNullOrBlank() || fileName.contains('/') || fileName.contains('\\')) return null
        val file = File(directory(), fileName)
        if (!file.exists()) return null
        return contentUriFor(file)
    }

    fun delete(fileName: String?): Boolean {
        if (fileName.isNullOrBlank() || fileName.contains('/') || fileName.contains('\\')) return true
        val file = File(directory(), fileName)
        return !file.exists() || file.delete()
    }

    fun clearAll(): Boolean {
        val dir = directory()
        if (!dir.exists()) return true
        return dir.listFiles()?.fold(true) { ok, file -> file.delete() && ok } ?: true
    }

    private fun newImageFile(): File {
        val fileName = "generated_${System.currentTimeMillis()}_${UUID.randomUUID()}.png"
        return File(directory(), fileName)
    }

    private fun directory(): File {
        return File(context.filesDir, GENERATED_IMAGE_DIRECTORY).apply { mkdirs() }
    }

    private fun contentUriFor(file: File): Uri {
        return FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
    }

    companion object {
        const val GENERATED_IMAGE_DIRECTORY = "generated_images"
        private const val PNG_QUALITY = 100
    }
}
