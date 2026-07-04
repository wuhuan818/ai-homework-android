package com.aihomework.aicontentcreator.data.ai

import android.content.Context
import android.util.Base64
import com.aihomework.aicontentcreator.data.image.GeneratedImageFileStore
import com.aihomework.aicontentcreator.data.model.ImageAspectRatio
import com.aihomework.aicontentcreator.data.model.ImageGenerationResult
import com.aihomework.aicontentcreator.data.model.ImageGenerationStyle
import com.aihomework.aicontentcreator.data.settings.AppSettings
import com.aihomework.aicontentcreator.data.settings.ImageGenerationApiType
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

class RealImageGenerationClient(
    context: Context,
    private val settings: AppSettings,
    private val apiKeyProvider: () -> String?
) : ImageGenerationClient {
    private val fileStore = GeneratedImageFileStore(context.applicationContext)
    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(20, TimeUnit.SECONDS)
        .readTimeout(90, TimeUnit.SECONDS)
        .writeTimeout(45, TimeUnit.SECONDS)
        .build()

    override suspend fun generateImage(
        prompt: String,
        style: ImageGenerationStyle,
        aspectRatio: ImageAspectRatio
    ): ImageGenerationResult = withContext(Dispatchers.IO) {
        val cleanPrompt = prompt.trim()
        validateSettings(cleanPrompt)
        val apiKey = apiKeyProvider()?.trim()
        if (apiKey.isNullOrBlank()) {
            throw ModelClientException("请先在设置页配置模型密钥。")
        }

        val finalPrompt = buildPrompt(cleanPrompt, style)
        val body = requestBodyFor(finalPrompt, aspectRatio)
            .toString()
            .toRequestBody(JSON_MEDIA_TYPE)
        val request = Request.Builder()
            .url(imageGenerationUrl())
            .header("Authorization", "Bearer $apiKey")
            .header("Content-Type", "application/json")
            .post(body)
            .build()

        try {
            httpClient.newCall(request).execute().use { response ->
                val responseText = response.body?.string().orEmpty()
                if (!response.isSuccessful) {
                    throw exceptionForStatus(response.code)
                }
                if (responseText.isBlank()) {
                    throw ModelClientException("图片生成接口返回格式无法识别，请检查接口类型和接口地址。")
                }
                val stored = parseAndStoreImage(responseText)
                val now = System.currentTimeMillis()
                ImageGenerationResult(
                    id = now,
                    prompt = cleanPrompt,
                    style = style,
                    aspectRatio = aspectRatio,
                    imageFileName = stored.fileName,
                    previewUri = stored.uri.toString(),
                    createdAtMillis = now,
                    isMock = false
                )
            }
        } catch (error: UnknownHostException) {
            throw ModelClientException("网络连接异常。", error)
        } catch (error: SocketTimeoutException) {
            throw ModelClientException("网络连接异常。", error)
        } catch (error: IOException) {
            throw ModelClientException("网络连接异常。", error)
        }
    }

    private fun requestBodyFor(finalPrompt: String, aspectRatio: ImageAspectRatio): JSONObject {
        return when (settings.imageGenerationApiType) {
            ImageGenerationApiType.QWEN_IMAGE_OFFICIAL -> qwenOfficialRequestBody(finalPrompt, aspectRatio)
            ImageGenerationApiType.OPENAI_IMAGES -> openAiImagesRequestBody(finalPrompt, aspectRatio)
        }
    }

    private fun qwenOfficialRequestBody(
        finalPrompt: String,
        aspectRatio: ImageAspectRatio
    ): JSONObject {
        return JSONObject()
            .put("model", settings.imageGenerationModel.trim())
            .put(
                "input",
                JSONObject().put(
                    "messages",
                    JSONArray().put(
                        JSONObject()
                            .put("role", "user")
                            .put(
                                "content",
                                JSONArray().put(JSONObject().put("text", finalPrompt))
                            )
                    )
                )
            )
            .put(
                "parameters",
                JSONObject()
                    .put("size", aspectRatio.qwenOfficialSize)
                    .put("n", 1)
                    .put("prompt_extend", true)
                    .put("watermark", false)
            )
    }

    private fun openAiImagesRequestBody(
        finalPrompt: String,
        aspectRatio: ImageAspectRatio
    ): JSONObject {
        return JSONObject()
            .put("model", settings.imageGenerationModel.trim())
            .put("prompt", finalPrompt)
            .put("size", aspectRatio.size)
            .put("n", 1)
    }

    private fun parseAndStoreImage(responseText: String) = try {
        val root = JSONObject(responseText)
        val b64 = findOpenAiB64(root)
        if (b64 != null) {
            fileStore.saveBytes(Base64.decode(b64, Base64.DEFAULT))
        } else {
            val url = findOpenAiUrl(root)
                ?: findQwenOfficialImageUrl(root)
                ?: findOutputResultUrl(root)
                ?: throw responseFormatException(root)
            downloadAndStore(url)
        }
    } catch (error: JSONException) {
        throw ModelClientException("图片生成接口返回格式无法识别，请检查接口类型和接口地址。", error)
    } catch (error: IllegalArgumentException) {
        throw ModelClientException("图片生成接口返回格式无法识别，请检查接口类型和接口地址。", error)
    }

    private fun findOpenAiB64(root: JSONObject): String? {
        return root.optJSONArray("data")
            ?.optJSONObject(0)
            ?.optString("b64_json")
            ?.takeIf { it.isNotBlank() }
    }

    private fun findOpenAiUrl(root: JSONObject): String? {
        return root.optJSONArray("data")
            ?.optJSONObject(0)
            ?.optString("url")
            ?.takeIf { it.isNotBlank() }
    }

    private fun findQwenOfficialImageUrl(root: JSONObject): String? {
        val content = root.optJSONObject("output")
            ?.optJSONArray("choices")
            ?.optJSONObject(0)
            ?.optJSONObject("message")
            ?.optJSONArray("content")
            ?: return null
        for (index in 0 until content.length()) {
            val imageUrl = content.optJSONObject(index)
                ?.optString("image")
                ?.takeIf { it.isNotBlank() }
            if (imageUrl != null) return imageUrl
        }
        return null
    }

    private fun findOutputResultUrl(root: JSONObject): String? {
        return root.optJSONObject("output")
            ?.optJSONArray("results")
            ?.optJSONObject(0)
            ?.optString("url")
            ?.takeIf { it.isNotBlank() }
    }

    private fun responseFormatException(root: JSONObject): ModelClientException {
        val message = root.optString("message").takeIf { it.isNotBlank() }
            ?: root.optJSONObject("error")?.optString("message")?.takeIf { it.isNotBlank() }
        if (message != null) {
            return ModelClientException("图片生成失败：${message.take(MAX_ERROR_MESSAGE_CHARS)}")
        }
        return ModelClientException("图片生成接口返回格式无法识别，请检查接口类型和接口地址。")
    }

    private fun downloadAndStore(url: String) = try {
        val request = Request.Builder().url(url).get().build()
        httpClient.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                throw exceptionForStatus(response.code)
            }
            val bytes = response.body?.bytes()
                ?: throw ModelClientException("图片生成接口返回格式无法识别，请检查接口类型和接口地址。")
            fileStore.saveBytes(bytes)
        }
    } catch (error: IllegalArgumentException) {
        throw ModelClientException("图片生成接口返回格式无法识别，请检查接口类型和接口地址。", error)
    } catch (error: UnknownHostException) {
        throw ModelClientException("网络连接异常，请检查网络后重试。", error)
    } catch (error: SocketTimeoutException) {
        throw ModelClientException("网络连接异常，请检查网络后重试。", error)
    } catch (error: IOException) {
        throw ModelClientException("网络连接异常，请检查网络后重试。", error)
    }

    private fun validateSettings(prompt: String) {
        if (prompt.isBlank()) {
            throw ModelClientException("请先输入图片描述。")
        }
        if (settings.imageGenerationModel.isBlank()) {
            throw ModelClientException("请先在设置页填写图片生成模型。")
        }
        imageGenerationUrl()
    }

    private fun buildPrompt(prompt: String, style: ImageGenerationStyle): String {
        return "${style.promptHint}\n\n$prompt"
    }

    private fun imageGenerationUrl(): String {
        return when (settings.imageGenerationApiType) {
            ImageGenerationApiType.QWEN_IMAGE_OFFICIAL -> {
                val endpoint = settings.imageGenerationEndpoint.trim()
                if (endpoint.isBlank()) {
                    throw ModelClientException("请先在设置页填写图片生成接口地址。")
                }
                if (endpoint.toHttpUrlOrNull() == null) {
                    throw ModelClientException("图片生成接口地址格式不正确，请检查设置页。")
                }
                endpoint
            }

            ImageGenerationApiType.OPENAI_IMAGES -> {
                if (settings.baseUrl.isBlank()) {
                    throw ModelClientException("请先在设置页配置接口地址。")
                }
                if (settings.baseUrl.trim().toHttpUrlOrNull() == null) {
                    throw ModelClientException("当前配置的接口地址格式不正确，请检查 Base URL。")
                }
                settings.baseUrl.trim().trimEnd('/') + IMAGE_GENERATIONS_PATH
            }
        }
    }

    private fun exceptionForStatus(code: Int): ModelClientException {
        return ModelClientException(
            when (code) {
                401, 403 -> "图片生成鉴权失败，请检查模型密钥、接口区域和接口地址。"
                429 -> "图片生成调用频率过高，请稍后重试。"
                in 500..599 -> "图片生成服务暂时异常，请稍后重试。"
                else -> "图片生成失败，请检查接口配置。"
            }
        )
    }

    private companion object {
        val JSON_MEDIA_TYPE = "application/json; charset=utf-8".toMediaType()
        const val IMAGE_GENERATIONS_PATH = "/images/generations"
        const val MAX_ERROR_MESSAGE_CHARS = 80
    }
}
