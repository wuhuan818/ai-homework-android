package com.aihomework.aicontentcreator.data.ai

import android.content.Context
import android.net.Uri
import android.util.Base64
import com.aihomework.aicontentcreator.data.model.CreationRequest
import com.aihomework.aicontentcreator.data.model.CreationResult
import com.aihomework.aicontentcreator.data.model.CreationScenario
import com.aihomework.aicontentcreator.data.settings.AppSettings
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

class RealModelClient(
    private val context: Context,
    private val settings: AppSettings,
    private val apiKeyProvider: () -> String?
) : ModelClient {
    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(20, TimeUnit.SECONDS)
        .readTimeout(45, TimeUnit.SECONDS)
        .writeTimeout(45, TimeUnit.SECONDS)
        .build()

    override suspend fun generate(request: CreationRequest): CreationResult {
        validateCommonSettings(request.input)
        val apiKey = apiKeyProvider()?.trim()
        if (apiKey.isNullOrBlank()) {
            throw ModelClientException("Real mode needs an API Key. Please add it in Settings.")
        }

        if (request.scenario == CreationScenario.ImageDescription && request.imageUri != null) {
            try {
                return generateVision(request, apiKey)
            } catch (error: Throwable) {
                val fallback = MockModelClient().generate(
                    request.copy(
                        input = request.input.ifBlank { "Selected image" },
                        imageLabel = request.imageLabel ?: "Selected image"
                    )
                )
                return fallback.copy(
                    content = "Real image description is unavailable, so Mock fallback was used.\n\n${fallback.content}\n\nReason: ${toUserMessage(error)}"
                )
            }
        }

        return generateText(request, apiKey)
    }

    private suspend fun generateText(request: CreationRequest, apiKey: String): CreationResult {
        val content = postChatCompletion(
            apiKey = apiKey,
            model = settings.textModel.trim(),
            messages = JSONArray()
                .put(JSONObject().put("role", "system").put("content", SYSTEM_PROMPT))
                .put(JSONObject().put("role", "user").put("content", promptFor(request)))
        )
        return request.toResult(content)
    }

    private suspend fun generateVision(request: CreationRequest, apiKey: String): CreationResult {
        val imageUri = request.imageUri ?: throw ModelClientException("Please choose an image first.")
        val imageDataUrl = readImageAsDataUrl(imageUri)
        val content = JSONArray()
            .put(JSONObject().put("type", "text").put("text", IMAGE_PROMPT))
            .put(
                JSONObject()
                    .put("type", "image_url")
                    .put("image_url", JSONObject().put("url", imageDataUrl))
            )
        val messages = JSONArray()
            .put(JSONObject().put("role", "system").put("content", SYSTEM_PROMPT))
            .put(JSONObject().put("role", "user").put("content", content))
        return request.toResult(
            postChatCompletion(
                apiKey = apiKey,
                model = settings.visionModel.trim(),
                messages = messages
            )
        )
    }

    private suspend fun postChatCompletion(
        apiKey: String,
        model: String,
        messages: JSONArray
    ): String = withContext(Dispatchers.IO) {
        if (model.isBlank()) {
            throw ModelClientException("Model is empty. Please check Settings.")
        }

        val body = JSONObject()
            .put("model", model)
            .put("messages", messages)
            .put("temperature", 0.7)
            .toString()
            .toRequestBody(JSON_MEDIA_TYPE)
        val request = Request.Builder()
            .url(settings.baseUrl.trim().trimEnd('/') + "/chat/completions")
            .header("Authorization", "Bearer $apiKey")
            .header("Content-Type", "application/json")
            .post(body)
            .build()

        try {
            httpClient.newCall(request).execute().use { response ->
                val responseText = response.body?.string().orEmpty()
                if (!response.isSuccessful) {
                    throw ModelClientException(messageForStatus(response.code))
                }
                if (responseText.isBlank()) {
                    throw ModelClientException("The API returned an empty response.")
                }
                parseAssistantContent(responseText)
            }
        } catch (error: UnknownHostException) {
            throw ModelClientException("Network is unavailable. Please check your connection.", error)
        } catch (error: SocketTimeoutException) {
            throw ModelClientException("The API request timed out. Please try again.", error)
        } catch (error: IOException) {
            throw ModelClientException("Network request failed. Please try again.", error)
        }
    }

    private fun parseAssistantContent(responseText: String): String {
        return try {
            val content = JSONObject(responseText)
                .getJSONArray("choices")
                .getJSONObject(0)
                .getJSONObject("message")
                .getString("content")
                .trim()
            if (content.isBlank()) {
                throw ModelClientException("The API returned empty generated content.")
            }
            content
        } catch (error: JSONException) {
            throw ModelClientException("The API response format was not recognized.", error)
        }
    }

    private fun readImageAsDataUrl(uriText: String): String {
        val uri = Uri.parse(uriText)
        val mimeType = context.contentResolver.getType(uri) ?: "image/jpeg"
        val bytes = context.contentResolver.openInputStream(uri)?.use { input ->
            val output = ByteArrayOutputStream()
            val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
            var total = 0
            while (true) {
                val read = input.read(buffer)
                if (read == -1) break
                total += read
                if (total > MAX_IMAGE_BYTES) {
                    throw ModelClientException("The selected image is too large for this demo.")
                }
                output.write(buffer, 0, read)
            }
            output.toByteArray()
        } ?: throw ModelClientException("Could not read the selected image.")

        return "data:$mimeType;base64,${Base64.encodeToString(bytes, Base64.NO_WRAP)}"
    }

    private fun validateCommonSettings(input: String) {
        if (settings.baseUrl.isBlank()) {
            throw ModelClientException("Base URL is empty. Please check Settings.")
        }
        if (input.length > MAX_INPUT_CHARS) {
            throw ModelClientException("Input is too long. Please shorten it and try again.")
        }
    }

    private fun promptFor(request: CreationRequest): String {
        return when (request.scenario) {
            CreationScenario.Moments ->
                "Please generate 3 Chinese social posts for Moments based on this topic, mood, or keywords. Keep them natural, short, and not like hard advertising. User input: ${request.input}"

            CreationScenario.Product ->
                "Please generate Chinese product copy based on this product information. Include title, core selling points, usage scenarios, and a short description. Keep it concise and avoid exaggerated claims. User input: ${request.input}"

            CreationScenario.ImageDescription ->
                IMAGE_PROMPT
        }
    }

    private fun CreationRequest.toResult(content: String): CreationResult {
        val now = System.currentTimeMillis()
        return CreationResult(
            id = now,
            scenario = scenario,
            originalInput = input.ifBlank { imageLabel ?: "Selected image" },
            content = content,
            createdAtMillis = now
        )
    }

    private fun messageForStatus(code: Int): String {
        return when (code) {
            401, 403 -> "API authentication failed. Please check your API Key in Settings."
            429 -> "The API rate limit was reached. Please try again later."
            in 500..599 -> "The API service is temporarily unavailable. Please try again later."
            else -> "The API request failed with status $code."
        }
    }

    private fun toUserMessage(error: Throwable): String {
        return if (error is ModelClientException) error.userMessage else "Image request failed."
    }

    private companion object {
        val JSON_MEDIA_TYPE = "application/json; charset=utf-8".toMediaType()
        const val MAX_INPUT_CHARS = 4000
        const val MAX_IMAGE_BYTES = 4 * 1024 * 1024
        const val SYSTEM_PROMPT =
            "You are a Chinese content creation assistant. Output concise, natural content that can be used directly."
        const val IMAGE_PROMPT =
            "Please describe the main subject, background, mood, and possible use of this image in Chinese, then provide one social media caption."
    }
}
