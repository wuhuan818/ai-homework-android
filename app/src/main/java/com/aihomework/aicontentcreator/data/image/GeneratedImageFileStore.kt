package com.aihomework.aicontentcreator.data.image

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import androidx.core.content.FileProvider
import com.aihomework.aicontentcreator.data.security.ByteEncryptedPayload
import com.aihomework.aicontentcreator.data.security.CryptoManager
import java.io.ByteArrayOutputStream
import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

data class StoredGeneratedImage(
    val fileName: String,
    val uri: Uri
)

class GeneratedImageFileStore(private val context: Context) {
    private val cryptoManager = CryptoManager(IMAGE_KEY_ALIAS)

    fun saveBitmap(bitmap: Bitmap): StoredGeneratedImage {
        val bytes = ByteArrayOutputStream().use { output ->
            check(bitmap.compress(Bitmap.CompressFormat.PNG, PNG_QUALITY, output))
            output.toByteArray()
        }
        return saveBytes(bytes)
    }

    fun saveBytes(bytes: ByteArray): StoredGeneratedImage {
        val file = newEncryptedImageFile()
        val payload = cryptoManager.encryptBytes(bytes).getOrThrow()
        writeEncryptedFile(file, payload)
        val cacheFile = writeCacheFile(file.name, bytes)
        return StoredGeneratedImage(file.name, contentUriFor(cacheFile))
    }

    fun uriFor(fileName: String?): Uri? {
        if (fileName.isNullOrBlank() || fileName.contains('/') || fileName.contains('\\')) return null
        val encryptedFile = File(encryptedDirectory(), fileName)
        if (encryptedFile.exists()) {
            return decryptToCache(encryptedFile)?.let { contentUriFor(it) }
        }

        val legacyFile = File(legacyDirectory(), fileName)
        if (!legacyFile.exists()) return null
        return contentUriFor(legacyFile)
    }

    fun delete(fileName: String?): Boolean {
        if (fileName.isNullOrBlank() || fileName.contains('/') || fileName.contains('\\')) return true
        val encryptedFile = File(encryptedDirectory(), fileName)
        val legacyFile = File(legacyDirectory(), fileName)
        val cacheFile = File(cacheDirectory(), cacheFileName(fileName))
        return deleteIfExists(encryptedFile) &&
            deleteIfExists(legacyFile) &&
            deleteIfExists(cacheFile)
    }

    fun clearAll(): Boolean {
        return clearDirectory(encryptedDirectory()) &&
            clearDirectory(legacyDirectory()) &&
            clearDirectory(cacheDirectory())
    }

    private fun newEncryptedImageFile(): File {
        val fileName = "encrypted_${System.currentTimeMillis()}_${UUID.randomUUID()}.imgenc"
        return File(encryptedDirectory(), fileName)
    }

    private fun writeEncryptedFile(file: File, payload: ByteEncryptedPayload) {
        encryptedDirectory()
        DataOutputStream(FileOutputStream(file)).use { output ->
            output.writeInt(FILE_MAGIC)
            output.writeInt(payload.version)
            output.writeInt(payload.iv.size)
            output.write(payload.iv)
            output.writeInt(payload.cipherText.size)
            output.write(payload.cipherText)
        }
    }

    private fun decryptToCache(file: File): File? {
        val payload = readEncryptedFile(file) ?: return null
        val bytes = cryptoManager.decryptBytes(payload).getOrNull() ?: return null
        return writeCacheFile(file.name, bytes)
    }

    private fun readEncryptedFile(file: File): ByteEncryptedPayload? {
        return runCatching {
            DataInputStream(file.inputStream()).use { input ->
                if (input.readInt() != FILE_MAGIC) return@runCatching null
                val version = input.readInt()
                val ivLength = input.readInt()
                if (ivLength <= 0 || ivLength > MAX_IV_BYTES) return@runCatching null
                val iv = ByteArray(ivLength)
                input.readFully(iv)
                val cipherTextLength = input.readInt()
                if (cipherTextLength <= 0) return@runCatching null
                val cipherText = ByteArray(cipherTextLength)
                input.readFully(cipherText)
                ByteEncryptedPayload(cipherText = cipherText, iv = iv, version = version)
            }
        }.getOrNull()
    }

    private fun writeCacheFile(sourceFileName: String, bytes: ByteArray): File {
        val file = File(cacheDirectory(), cacheFileName(sourceFileName))
        FileOutputStream(file).use { output ->
            output.write(bytes)
        }
        return file
    }

    private fun encryptedDirectory(): File {
        return File(context.filesDir, GENERATED_IMAGE_ENCRYPTED_DIRECTORY).apply { mkdirs() }
    }

    private fun legacyDirectory(): File {
        return File(context.filesDir, GENERATED_IMAGE_DIRECTORY).apply { mkdirs() }
    }

    private fun cacheDirectory(): File {
        return File(context.cacheDir, DECRYPTED_IMAGE_CACHE_DIRECTORY).apply { mkdirs() }
    }

    private fun contentUriFor(file: File): Uri {
        return FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
    }

    private fun cacheFileName(sourceFileName: String): String {
        return "decrypted_${sourceFileName.substringBeforeLast('.')}.png"
    }

    private fun clearDirectory(dir: File): Boolean {
        if (!dir.exists()) return true
        return dir.listFiles()?.fold(true) { ok, file -> deleteIfExists(file) && ok } ?: true
    }

    private fun deleteIfExists(file: File): Boolean {
        return !file.exists() || file.delete()
    }

    companion object {
        const val GENERATED_IMAGE_DIRECTORY = "generated_images"
        const val GENERATED_IMAGE_ENCRYPTED_DIRECTORY = "generated_images_encrypted"
        const val DECRYPTED_IMAGE_CACHE_DIRECTORY = "decrypted_generated_images"
        private const val IMAGE_KEY_ALIAS = "ai_content_creator_generated_images"
        private const val FILE_MAGIC = 0x41494349
        private const val MAX_IV_BYTES = 32
        private const val PNG_QUALITY = 100
    }
}
