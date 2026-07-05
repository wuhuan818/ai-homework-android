package com.aihomework.aicontentcreator.data.security

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

data class EncryptedPayload(
    val cipherText: String,
    val iv: String,
    val version: Int = CryptoManager.PAYLOAD_VERSION
)

data class ByteEncryptedPayload(
    val cipherText: ByteArray,
    val iv: ByteArray,
    val version: Int = CryptoManager.PAYLOAD_VERSION
)

class CryptoManager(
    private val keyAlias: String = KEY_ALIAS
) {
    fun encrypt(plainText: String): Result<EncryptedPayload> {
        return runCatching {
            val cipher = Cipher.getInstance(TRANSFORMATION)
            cipher.init(Cipher.ENCRYPT_MODE, getOrCreateSecretKey())
            val encrypted = cipher.doFinal(plainText.toByteArray(Charsets.UTF_8))
            EncryptedPayload(
                cipherText = Base64.encodeToString(encrypted, Base64.NO_WRAP),
                iv = Base64.encodeToString(cipher.iv, Base64.NO_WRAP),
                version = PAYLOAD_VERSION
            )
        }
    }

    fun decrypt(payload: EncryptedPayload): Result<String> {
        return runCatching {
            val encrypted = Base64.decode(payload.cipherText, Base64.NO_WRAP)
            val iv = Base64.decode(payload.iv, Base64.NO_WRAP)
            val cipher = Cipher.getInstance(TRANSFORMATION)
            cipher.init(Cipher.DECRYPT_MODE, getOrCreateSecretKey(), GCMParameterSpec(GCM_TAG_BITS, iv))
            String(cipher.doFinal(encrypted), Charsets.UTF_8)
        }
    }

    fun encryptBytes(bytes: ByteArray): Result<ByteEncryptedPayload> {
        return runCatching {
            val cipher = Cipher.getInstance(TRANSFORMATION)
            cipher.init(Cipher.ENCRYPT_MODE, getOrCreateSecretKey())
            ByteEncryptedPayload(
                cipherText = cipher.doFinal(bytes),
                iv = cipher.iv,
                version = PAYLOAD_VERSION
            )
        }
    }

    fun decryptBytes(payload: ByteEncryptedPayload): Result<ByteArray> {
        return runCatching {
            val cipher = Cipher.getInstance(TRANSFORMATION)
            cipher.init(
                Cipher.DECRYPT_MODE,
                getOrCreateSecretKey(),
                GCMParameterSpec(GCM_TAG_BITS, payload.iv)
            )
            cipher.doFinal(payload.cipherText)
        }
    }

    private fun getOrCreateSecretKey(): SecretKey {
        val keyStore = KeyStore.getInstance(KEYSTORE_PROVIDER).apply { load(null) }
        val existing = keyStore.getEntry(keyAlias, null) as? KeyStore.SecretKeyEntry
        if (existing != null) return existing.secretKey

        val keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, KEYSTORE_PROVIDER)
        val keySpec = KeyGenParameterSpec.Builder(
            keyAlias,
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
        )
            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            .setKeySize(AES_KEY_SIZE_BITS)
            .setRandomizedEncryptionRequired(true)
            .build()
        keyGenerator.init(keySpec)
        return keyGenerator.generateKey()
    }

    companion object {
        const val PAYLOAD_VERSION = 1

        private const val AES_KEY_SIZE_BITS = 256
        const val KEYSTORE_PROVIDER = "AndroidKeyStore"
        private const val KEY_ALIAS = "ai_content_creator_history"
        private const val TRANSFORMATION = "AES/GCM/NoPadding"
        private const val GCM_TAG_BITS = 128
    }
}
