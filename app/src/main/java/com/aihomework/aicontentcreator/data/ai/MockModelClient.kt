package com.aihomework.aicontentcreator.data.ai

import com.aihomework.aicontentcreator.data.model.CreationRequest
import com.aihomework.aicontentcreator.data.model.CreationResult
import com.aihomework.aicontentcreator.data.model.CreationScenario
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

            CreationScenario.ImageDescription ->
                """
                【演示模式生成】
                画面主体：根据线索「$cleanInput」生成一段示例图片说明。
                背景与氛围：偏生活化、可发布，不代表真实模型识图结果。
                适合社交平台发布的配文：把眼前这一幕收进今天的记忆里。
                可能的标签：#图片记录 #生活片刻 #演示模式
                """.trimIndent()
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
}
