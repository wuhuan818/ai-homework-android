package com.aihomework.aicontentcreator.data.ai

import com.aihomework.aicontentcreator.data.model.CreationRequest
import com.aihomework.aicontentcreator.data.model.CreationResult
import com.aihomework.aicontentcreator.data.model.CreationScenario
import kotlinx.coroutines.delay

class MockModelClient : ModelClient {
    override suspend fun generate(request: CreationRequest): CreationResult {
        delay(700)
        val cleanInput = request.input.ifBlank { request.imageLabel ?: "模拟图片" }
        val content = when (request.scenario) {
            CreationScenario.Moments ->
                "今天的关键词是：$cleanInput。把平凡的小事认真过好，也是一种值得记录的生活。"

            CreationScenario.Product ->
                "商品名称：$cleanInput。这是一款适合日常使用的产品，突出特点包括实用、简洁、易上手。"

            CreationScenario.ImageDescription ->
                "这张图片可能包含一个主要主体、背景环境和可用于社交平台发布的描述。当前为 Mock 图片理解结果。图片线索：$cleanInput。"
        }

        return CreationResult(
            id = System.currentTimeMillis(),
            scenario = request.scenario,
            originalInput = cleanInput,
            content = content,
            createdAtMillis = System.currentTimeMillis()
        )
    }
}

