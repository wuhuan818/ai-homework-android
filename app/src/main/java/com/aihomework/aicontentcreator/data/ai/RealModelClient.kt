package com.aihomework.aicontentcreator.data.ai

import android.content.Context
import com.aihomework.aicontentcreator.data.image.VisionImagePreparationException
import com.aihomework.aicontentcreator.data.image.VisionImagePreprocessor
import com.aihomework.aicontentcreator.data.model.CreationRequest
import com.aihomework.aicontentcreator.data.model.CreationResult
import com.aihomework.aicontentcreator.data.model.CreationScenario
import com.aihomework.aicontentcreator.data.model.ImageDescriptionStyle
import com.aihomework.aicontentcreator.data.model.RewriteAction
import com.aihomework.aicontentcreator.data.model.StyleAdvice
import com.aihomework.aicontentcreator.data.model.TextCreationStyle
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
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
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
                return generateImageMockFallback(request, error.userMessage)
            } catch (error: ModelClientException) {
                if (error.allowImageMockFallback) {
                    return generateImageMockFallback(request, error.userMessage)
                }
                throw error
            }
        }

        return generateText(request, apiKey)
    }

    override suspend fun suggestStyles(
        scenario: CreationScenario,
        input: String
    ): List<StyleAdvice> {
        validateCommonSettings(input)
        val apiKey = apiKeyProvider()?.trim()
        if (apiKey.isNullOrBlank()) {
            throw ModelClientException("当前配置尚未填写模型密钥，请前往设置页补充。")
        }
        val content = postChatCompletion(
            apiKey = apiKey,
            model = settings.textModel.trim(),
            messages = JSONArray()
                .put(JSONObject().put("role", "system").put("content", SYSTEM_PROMPT))
                .put(JSONObject().put("role", "user").put("content", styleAdvicePrompt(scenario, input)))
        )
        return parseStyleAdvice(content, scenario)
    }

    override suspend fun rewriteText(text: String, action: RewriteAction): String {
        validateCommonSettings(text)
        val apiKey = apiKeyProvider()?.trim()
        if (apiKey.isNullOrBlank()) {
            throw ModelClientException("当前配置尚未填写模型密钥，请前往设置页补充。")
        }
        return postChatCompletion(
            apiKey = apiKey,
            model = settings.textModel.trim(),
            messages = JSONArray()
                .put(JSONObject().put("role", "system").put("content", SYSTEM_PROMPT))
                .put(JSONObject().put("role", "user").put("content", rewritePrompt(text, action)))
        )
    }

    override suspend fun optimizeImagePrompt(input: String): String {
        validateCommonSettings(input)
        val apiKey = apiKeyProvider()?.trim()
        if (apiKey.isNullOrBlank()) {
            throw ModelClientException("当前配置尚未填写模型密钥，请前往设置页补充。")
        }
        return postChatCompletion(
            apiKey = apiKey,
            model = settings.textModel.trim(),
            messages = JSONArray()
                .put(JSONObject().put("role", "system").put("content", SYSTEM_PROMPT))
                .put(JSONObject().put("role", "user").put("content", imagePromptOptimizationPrompt(input)))
        )
    }

    override suspend fun prepareImagePromptFromText(text: String): String {
        validateCommonSettings(text)
        val apiKey = apiKeyProvider()?.trim()
        if (apiKey.isNullOrBlank()) {
            throw ModelClientException("当前配置尚未填写模型密钥，请前往设置页补充。")
        }
        return postChatCompletion(
            apiKey = apiKey,
            model = settings.textModel.trim(),
            messages = JSONArray()
                .put(JSONObject().put("role", "system").put("content", SYSTEM_PROMPT))
                .put(JSONObject().put("role", "user").put("content", textToImagePrompt(text)))
        )
    }

    suspend fun testTextConnection() {
        validateCommonSettings("连接测试")
        val apiKey = apiKeyProvider()?.trim()
        if (apiKey.isNullOrBlank()) {
            throw ModelClientException("当前配置尚未填写模型密钥，请先保存密钥。")
        }
        postChatCompletion(
            apiKey = apiKey,
            model = settings.textModel.trim(),
            messages = JSONArray()
                .put(JSONObject().put("role", "system").put("content", SYSTEM_PROMPT))
                .put(JSONObject().put("role", "user").put("content", "请回复：连接成功"))
        )
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
            .put(JSONObject().put("type", "text").put("text", imagePromptFor(request, hasRealImage = true)))
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
                messages = messages,
                allowVisionFallback = true
            ),
            warningMessage = preparedImage.warningMessage
        )
    }

    private suspend fun postChatCompletion(
        apiKey: String,
        model: String,
        messages: JSONArray,
        allowVisionFallback: Boolean = false
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
                    throw exceptionForStatus(response.code, responseText, allowVisionFallback)
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
        if (settings.baseUrl.trim().toHttpUrlOrNull() == null) {
            throw ModelClientException("当前配置的接口地址格式不正确，请检查 Base URL。")
        }
        if (input.length > MAX_INPUT_CHARS) {
            throw ModelClientException("输入内容过长，请缩短后重试。")
        }
    }

    private fun promptFor(request: CreationRequest): String {
        return when (request.scenario) {
            CreationScenario.Moments -> momentsPromptFor(request)

            CreationScenario.Product -> productPromptFor(request)

            CreationScenario.ImageDescription ->
                imagePromptFor(request, hasRealImage = request.imageUri != null)

            CreationScenario.ImageGeneration ->
                throw ModelClientException("图片生成请使用独立图片生成接口。")
        }
    }

    private fun momentsPromptFor(request: CreationRequest): String {
        val versionInstruction = if (request.generationCount >= 3) {
            "请生成 3 个明显不同的版本，用“版本 1 / 版本 2 / 版本 3”清晰分隔，每个版本表达角度要不同。"
        } else {
            "请生成 1 个可直接发布的版本。"
        }
        return """
            请根据用户输入生成朋友圈文案。
            用户选择的风格：${request.textStyle.displayName}
            $versionInstruction
            要求：
            1. 保持适合中文社交平台的自然表达。
            2. 不编造用户没有提供的事实。
            3. 不要像广告，不要空泛堆词。
            4. 尽量保留用户输入中的具体细节。
            5. 单个版本尽量控制在 80 字以内。

            用户输入：${request.input}
        """.trimIndent()
    }

    private fun productPromptFor(request: CreationRequest): String {
        val versionInstruction = if (request.generationCount >= 3) {
            "请生成 3 个明显不同的版本，用“版本 1 / 版本 2 / 版本 3”清晰分隔，每个版本从不同表达角度切入。"
        } else {
            "请生成 1 个可直接使用的版本。"
        }
        return """
            请根据用户输入生成商品文案。
            用户选择的风格：${request.textStyle.displayName}
            $versionInstruction
            建议输出包含标题、核心卖点、适用人群、使用场景、短文案。
            要求：
            1. 不编造事实。
            2. 不夸大商品参数、功效、销量、价格或资质。
            3. 保持适合中文电商平台或社交电商平台的表达。
            4. 语言真实克制，但要有清晰购买理由。

            用户输入：${request.input}
        """.trimIndent()
    }

    private fun styleAdvicePrompt(scenario: CreationScenario, input: String): String {
        val styleNames = TextCreationStyle.optionsFor(scenario)
            .joinToString(separator = "、") { it.displayName }
        val sceneName = when (scenario) {
            CreationScenario.Moments -> "朋友圈文案"
            CreationScenario.Product -> "商品文案"
            CreationScenario.ImageDescription -> "文字创作"
            CreationScenario.ImageGeneration -> "图片生成"
        }
        return """
            请为一段${sceneName}输入推荐 2 到 3 个合适的创作风格。
            可选风格只能从以下列表中选择：$styleNames。
            输出格式必须为每行一条：风格名称：简短理由。
            理由要说明输入内容为什么适合该方向。
            不要编造用户没有提供的事实，不要输出可选列表以外的风格。

            用户输入：$input
        """.trimIndent()
    }

    private fun rewritePrompt(text: String, action: RewriteAction): String {
        val instruction = when (action) {
            RewriteAction.Shorter -> "改写得更简短，保留核心信息。"
            RewriteAction.Gentler -> "改写得更温柔自然。"
            RewriteAction.Premium -> "改写得更克制、更高级，减少口水话。"
            RewriteAction.Conversational -> "改写得更口语，像自然表达。"
            RewriteAction.Title -> "从内容中提炼 1 到 3 个中文标题，不要超过 18 字。"
        }
        return """
            请对下面的中文文本进行二次改写。
            改写方向：${action.displayName}
            具体要求：$instruction
            不要编造事实，不要加入原文没有的信息。
            如果是商品相关内容，不要夸大商品参数、功效、价格或资质。
            只输出改写结果，不要解释过程。

            原文：
            $text
        """.trimIndent()
    }

    private fun imagePromptOptimizationPrompt(input: String): String {
        return """
            请把下面的中文图片描述优化成更适合文生图模型的提示词。
            要求：
            1. 只输出优化后的提示词，不要解释过程。
            2. 保留用户原意，不编造具体品牌、人物身份、地点或事实。
            3. 可以补充画面主体、环境、光线、风格、构图和细节。
            4. 不要加入 seed、steps、cfg、sampler 等高级参数。
            5. 控制在 120 字以内。

            原始描述：
            $input
        """.trimIndent()
    }

    private fun textToImagePrompt(text: String): String {
        return """
            请将以下文本整理成适合文生图模型使用的中文提示词。
            只输出一段提示词，不要解释。
            提示词应突出主体、场景、氛围、风格、光线和构图。
            避免保留促销话术、价格、购买引导等不适合画面生成的内容。
            不要加入 seed、steps、cfg、sampler 等高级参数。

            原文：
            $text
        """.trimIndent()
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

    private fun imagePromptFor(request: CreationRequest, hasRealImage: Boolean): String {
        val userClue = request.input.trim()
            .ifBlank { request.imageLabel.orEmpty().trim() }
            .ifBlank { "无" }
        val sourceInstruction = if (hasRealImage) {
            "用户补充要求：$userClue"
        } else {
            "用户未上传真实图片，仅提供图片线索，请基于文字线索生成，不要伪装成真实识图。\n图片线索/用户补充要求：$userClue"
        }
        val stylePrompt = when (request.imageDescriptionStyle) {
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
        return "$stylePrompt\n\n$sourceInstruction"
    }

    private fun parseStyleAdvice(content: String, scenario: CreationScenario): List<StyleAdvice> {
        val options = TextCreationStyle.optionsFor(scenario)
        val parsed = content.lines()
            .map { line ->
                line.trim()
                    .replace(Regex("^[-*]\\s*"), "")
                    .replace(Regex("^\\d+[.、)]\\s*"), "")
            }
            .filter { it.isNotBlank() }
            .mapNotNull { line ->
                val style = options.firstOrNull { line.startsWith(it.displayName) || it.displayName in line }
                if (style == null) {
                    null
                } else {
                    val reason = line
                        .substringAfter("：", missingDelimiterValue = line)
                        .substringAfter(":", missingDelimiterValue = line)
                        .removePrefix(style.displayName)
                        .trim()
                        .ifBlank { "适合当前输入的表达方向。" }
                    StyleAdvice(style, reason)
                }
            }
            .distinctBy { it.style }
            .take(3)
        if (parsed.isNotEmpty()) return parsed

        val fallback = TextCreationStyle.defaultFor(scenario)
        return listOf(StyleAdvice(fallback, "模型返回的推荐格式不够清晰，已保留一个稳妥默认方向。"))
    }

    private suspend fun generateImageMockFallback(
        request: CreationRequest,
        reason: String
    ): CreationResult {
        val fallback = MockModelClient().generate(
            request.copy(
                input = request.input.ifBlank { request.imageLabel ?: "Selected image" },
                imageLabel = request.imageLabel ?: "Selected image"
            )
        )
        val fallbackNotice = "真实图片描述不可用，已使用演示模式兜底。"
        return fallback.copy(
            content = "$fallbackNotice\n\n${fallback.content}\n\n原因：$reason",
            warningMessage = fallbackNotice
        )
    }

    private fun exceptionForStatus(
        code: Int,
        responseText: String,
        allowVisionFallback: Boolean
    ): ModelClientException {
        val allowsImageMockFallback = allowVisionFallback &&
            code in VISION_FALLBACK_STATUS_CODES &&
            responseLooksLikeVisionUnsupported(responseText)
        if (allowsImageMockFallback) {
            return ModelClientException(
                userMessage = "当前模型或接口不支持图片输入，或图片输入格式不兼容。",
                allowImageMockFallback = true
            )
        }
        return ModelClientException(messageForStatus(code))
    }

    private fun messageForStatus(code: Int): String {
        return when (code) {
            401, 403 -> "接口鉴权失败，请检查设置页中的模型密钥。"
            429 -> "接口调用过于频繁，请稍后重试。"
            in 500..599 -> "模型服务暂时不可用，请稍后重试。"
            else -> "接口请求失败，状态码：$code。"
        }
    }

    private fun responseLooksLikeVisionUnsupported(responseText: String): Boolean {
        val normalized = responseText.lowercase()
        return VISION_FALLBACK_ERROR_HINTS.any { hint -> hint in normalized }
    }

    private companion object {
        val JSON_MEDIA_TYPE = "application/json; charset=utf-8".toMediaType()
        const val MAX_INPUT_CHARS = 4000
        const val SYSTEM_PROMPT =
            "你是中文内容创作助手。输出要自然、具体、克制，尽量可以直接使用。"
        val VISION_FALLBACK_STATUS_CODES = setOf(400, 415, 422)
        val VISION_FALLBACK_ERROR_HINTS = listOf(
            "image",
            "vision",
            "unsupported",
            "image_url",
            "modalit",
            "media type",
            "invalid image"
        )
    }
}
