package com.aihomework.aicontentcreator.data.ai

import android.content.Context
import com.aihomework.aicontentcreator.data.image.VisionImagePreparationException
import com.aihomework.aicontentcreator.data.image.VisionImagePreprocessor
import com.aihomework.aicontentcreator.data.model.CreationRequest
import com.aihomework.aicontentcreator.data.model.CreationResult
import com.aihomework.aicontentcreator.data.model.CreationScenario
import com.aihomework.aicontentcreator.data.model.ImageDescriptionStyle
import com.aihomework.aicontentcreator.data.settings.AppSettings
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
            throw ModelClientException("当前配置尚未填写模型密钥，请前往设置页补充。")
        }

        if (request.scenario == CreationScenario.ImageDescription && request.imageUri != null) {
            try {
                return generateVision(request, apiKey)
            } catch (error: VisionImagePreparationException) {
                throw ModelClientException(error.userMessage, error)
            } catch (error: Throwable) {
                val fallback = MockModelClient().generate(
                    request.copy(
                        input = request.input.ifBlank { "Selected image" },
                        imageLabel = request.imageLabel ?: "Selected image"
                    )
                )
                return fallback.copy(
                    content = "真实图片描述暂不可用，已改用演示模式结果。\n\n${fallback.content}\n\n原因：${toUserMessage(error)}"
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
        val imageUri = request.imageUri ?: throw ModelClientException("请先选择图片。")
        val preparedImage = VisionImagePreprocessor(context).prepareForVisionUpload(imageUri)
        val content = JSONArray()
            .put(JSONObject().put("type", "text").put("text", imagePromptFor(request.imageDescriptionStyle)))
            .put(
                JSONObject()
                    .put("type", "image_url")
                    .put("image_url", JSONObject().put("url", preparedImage.dataUrl))
            )
        val messages = JSONArray()
            .put(JSONObject().put("role", "system").put("content", SYSTEM_PROMPT))
            .put(JSONObject().put("role", "user").put("content", content))
        return request.toResult(
            postChatCompletion(
                apiKey = apiKey,
                model = settings.visionModel.trim(),
                messages = messages
            ),
            warningMessage = preparedImage.warningMessage
        )
    }

    private suspend fun postChatCompletion(
        apiKey: String,
        model: String,
        messages: JSONArray
    ): String = withContext(Dispatchers.IO) {
        if (model.isBlank()) {
            throw ModelClientException("当前配置的模型名称为空，请前往设置页补充。")
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
                    throw ModelClientException("接口返回为空，请稍后重试。")
                }
                parseAssistantContent(responseText)
            }
        } catch (error: UnknownHostException) {
            throw ModelClientException("网络不可用，请检查连接。", error)
        } catch (error: SocketTimeoutException) {
            throw ModelClientException("接口请求超时，请稍后重试。", error)
        } catch (error: IOException) {
            throw ModelClientException("网络请求失败，请稍后重试。", error)
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
                throw ModelClientException("接口返回的生成内容为空。")
            }
            content
        } catch (error: JSONException) {
            throw ModelClientException("接口返回格式无法识别。", error)
        }
    }

    private fun validateCommonSettings(input: String) {
        if (settings.baseUrl.isBlank()) {
            throw ModelClientException("当前配置的接口地址为空，请前往设置页补充。")
        }
        if (input.length > MAX_INPUT_CHARS) {
            throw ModelClientException("输入内容过长，请缩短后重试。")
        }
    }

    private fun promptFor(request: CreationRequest): String {
        return when (request.scenario) {
            CreationScenario.Moments ->
                """
                请根据用户输入生成 3 条朋友圈文案。
                三条风格分别为：
                1. 温柔日常
                2. 轻松幽默
                3. 简洁高级
                要求每条不超过 60 字，不要像广告，不要空泛，尽量保留用户输入的具体细节。

                用户输入：${request.input}
                """.trimIndent()

            CreationScenario.Product ->
                """
                请根据用户输入生成商品文案。
                输出格式：
                标题：
                核心卖点：
                适用人群：
                使用场景：
                短文案：
                要求不编造参数，不夸大功效，语言真实克制。

                用户输入：${request.input}
                """.trimIndent()

            CreationScenario.ImageDescription ->
                imagePromptFor(request.imageDescriptionStyle)
        }
    }

    private fun CreationRequest.toResult(content: String): CreationResult {
        return toResult(content, warningMessage = null)
    }

    private fun CreationRequest.toResult(content: String, warningMessage: String?): CreationResult {
        val now = System.currentTimeMillis()
        val finalContent = if (scenario == CreationScenario.ImageDescription) {
            "图片描述风格：${imageDescriptionStyle.displayName}\n\n$content"
        } else {
            content
        }
        return CreationResult(
            id = now,
            scenario = scenario,
            originalInput = input.ifBlank { imageLabel ?: "已选择图片" },
            content = finalContent,
            createdAtMillis = now,
            warningMessage = warningMessage
        )
    }

    private fun imagePromptFor(style: ImageDescriptionStyle): String {
        return when (style) {
            ImageDescriptionStyle.Objective ->
                """
                请根据图片内容进行客观中文描述。
                输出包括：
                1. 画面主体
                2. 背景环境
                3. 颜色与氛围
                4. 可见细节
                要求不要编造图片中不存在的内容。
                """.trimIndent()

            ImageDescriptionStyle.SocialCaption ->
                """
                请根据图片内容生成适合社交平台发布的中文内容。
                输出包括：
                1. 画面简述
                2. 一段自然的社交配文
                3. 3 到 5 个标签
                要求表达自然，不要像广告。
                """.trimIndent()

            ImageDescriptionStyle.ProductCopy ->
                """
                请根据图片内容生成偏商品或宣传用途的中文文案。
                输出包括：
                1. 可能的商品/主体
                2. 卖点表达
                3. 使用场景
                4. 短文案
                要求不要编造具体品牌、价格、参数或功效。
                如果图片中无法判断商品，不要强行编造，应提示“更适合普通图片描述”。
                """.trimIndent()
        }
    }

    private fun messageForStatus(code: Int): String {
        return when (code) {
            401, 403 -> "接口鉴权失败，请检查设置页中的模型密钥。"
            429 -> "接口调用过于频繁，请稍后重试。"
            in 500..599 -> "模型服务暂时不可用，请稍后重试。"
            else -> "接口请求失败，状态码：$code。"
        }
    }

    private fun toUserMessage(error: Throwable): String {
        return if (error is ModelClientException) error.userMessage else "图片请求失败。"
    }

    private companion object {
        val JSON_MEDIA_TYPE = "application/json; charset=utf-8".toMediaType()
        const val MAX_INPUT_CHARS = 4000
        const val SYSTEM_PROMPT =
            "你是中文内容创作助手。输出要自然、具体、克制，尽量可以直接使用。"
    }
}
