package com.aihomework.aicontentcreator.data.ai

import com.aihomework.aicontentcreator.data.model.CreationRequest
import com.aihomework.aicontentcreator.data.model.CreationResult
import com.aihomework.aicontentcreator.data.model.CreationScenario
import com.aihomework.aicontentcreator.data.model.ImageDescriptionStyle
import kotlinx.coroutines.delay

class MockModelClient : ModelClient {
    override suspend fun generate(request: CreationRequest): CreationResult {
        delay(700)
        val cleanInput = request.input.ifBlank { request.imageLabel ?: "示例图片" }
        val content = when (request.scenario) {
            CreationScenario.Moments ->
                """
                【演示模式生成】
                1. 温柔日常：把「$cleanInput」留在今天，像给生活按下一次轻轻的保存。
                2. 轻松幽默：今日关键词：$cleanInput。普通日子也要认真营业一下。
                3. 简洁高级：$cleanInput，刚刚好，值得记录。
                """.trimIndent()

            CreationScenario.Product ->
                """
                【演示模式生成】
                标题：$cleanInput
                核心卖点：围绕用户提供的信息做简洁表达，突出日常实用感。
                适用人群：需要快速了解这类商品特点的用户。
                使用场景：日常使用、送礼参考或商品页基础介绍。
                短文案：用真实克制的语言介绍「$cleanInput」，不夸大、不硬推。
                """.trimIndent()

            CreationScenario.ImageDescription -> imageDescriptionContent(
                input = cleanInput,
                style = request.imageDescriptionStyle
            )
        }

        val now = System.currentTimeMillis()
        return CreationResult(
            id = now,
            scenario = request.scenario,
            originalInput = cleanInput,
            content = content,
            createdAtMillis = now
        )
    }

    private fun imageDescriptionContent(input: String, style: ImageDescriptionStyle): String {
        return when (style) {
            ImageDescriptionStyle.Objective ->
                """
                【演示模式生成】
                图片描述风格：${style.displayName}
                画面主体：根据线索「$input」生成一段客观示例说明。
                背景环境：以用户提供的图片线索为准，不代表真实识图结论。
                颜色与氛围：保持中性描述，避免夸张联想。
                可见细节：仅整理已提供线索，不编造不存在的内容。
                """.trimIndent()

            ImageDescriptionStyle.SocialCaption ->
                """
                【演示模式生成】
                图片描述风格：${style.displayName}
                画面简述：根据线索「$input」整理一段轻量图片说明。
                社交配文：把眼前这一幕收进今天的记忆里，简单一点，也很好。
                标签：#图片记录 #生活片刻 #演示模式
                """.trimIndent()

            ImageDescriptionStyle.ProductCopy ->
                """
                【演示模式生成】
                图片描述风格：${style.displayName}
                可能的商品/主体：根据线索「$input」判断主体，无法确认时更适合普通图片描述。
                卖点表达：围绕可见主体做克制表达，不编造品牌、价格、参数或功效。
                使用场景：适合基础展示、介绍页或素材整理。
                短文案：用清晰自然的语言呈现画面中的主体。
                """.trimIndent()
        }
    }
}
