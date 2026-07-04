package com.aihomework.aicontentcreator.data.ai

import com.aihomework.aicontentcreator.data.model.CreationRequest
import com.aihomework.aicontentcreator.data.model.CreationResult
import com.aihomework.aicontentcreator.data.model.CreationScenario
import com.aihomework.aicontentcreator.data.model.ImageDescriptionStyle
import com.aihomework.aicontentcreator.data.model.RewriteAction
import com.aihomework.aicontentcreator.data.model.StyleAdvice
import com.aihomework.aicontentcreator.data.model.TextCreationStyle
import kotlinx.coroutines.delay

class MockModelClient : ModelClient {
    override suspend fun generate(request: CreationRequest): CreationResult {
        delay(700)
        val cleanInput = request.input.ifBlank { request.imageLabel ?: "示例图片" }
        val content = when (request.scenario) {
            CreationScenario.Moments -> momentsContent(cleanInput, request.textStyle, request.generationCount)

            CreationScenario.Product -> productContent(cleanInput, request.textStyle, request.generationCount)

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

    override suspend fun suggestStyles(
        scenario: CreationScenario,
        input: String
    ): List<StyleAdvice> {
        delay(400)
        val cleanInput = input.lowercase()
        return when (scenario) {
            CreationScenario.Moments -> suggestMomentStyles(cleanInput)
            CreationScenario.Product -> suggestProductStyles(cleanInput)
            CreationScenario.ImageDescription -> emptyList()
        }
    }

    override suspend fun rewriteText(text: String, action: RewriteAction): String {
        delay(450)
        val cleanText = text.trim()
        if (cleanText.isBlank()) return cleanText
        return when (action) {
            RewriteAction.Shorter ->
                "【演示模式改写】\n${cleanText.take(80)}${if (cleanText.length > 80) "..." else ""}"

            RewriteAction.Gentler ->
                "【演示模式改写】\n把话说得柔和一点：$cleanText"

            RewriteAction.Premium ->
                "【演示模式改写】\n更克制的表达：$cleanText"

            RewriteAction.Conversational ->
                "【演示模式改写】\n换成更自然的说法：$cleanText"

            RewriteAction.Title ->
                "【演示模式改写】\n标题：${cleanText.lines().firstOrNull { it.isNotBlank() }?.take(24) ?: "创作标题"}"
        }
    }

    private fun momentsContent(input: String, style: TextCreationStyle, generationCount: Int): String {
        val versions = if (generationCount >= 3) {
            listOf(
                momentLine(input, style, 1),
                momentLine(input, style, 2),
                momentLine(input, style, 3)
            ).joinToString(separator = "\n\n")
        } else {
            momentLine(input, style, 1)
        }
        return "【演示模式生成】\n朋友圈风格：${style.displayName}\n\n$versions"
    }

    private fun productContent(input: String, style: TextCreationStyle, generationCount: Int): String {
        val versions = if (generationCount >= 3) {
            listOf(
                productBlock(input, style, 1),
                productBlock(input, style, 2),
                productBlock(input, style, 3)
            ).joinToString(separator = "\n\n")
        } else {
            productBlock(input, style, 1)
        }
        return "【演示模式生成】\n商品文案风格：${style.displayName}\n\n$versions"
    }

    private fun momentLine(input: String, style: TextCreationStyle, index: Int): String {
        val prefix = if (index > 1) "版本 $index：" else ""
        val body = when (style) {
            TextCreationStyle.WarmDaily -> when (index) {
                1 -> "把「$input」放进今天的小确幸里，平凡也有被认真记住的意义。"
                2 -> "今天因为「$input」变得柔软了一点，日子慢下来也挺好。"
                else -> "和「$input」有关的这个瞬间，很普通，也很值得。"
            }

            TextCreationStyle.LightHumor -> when (index) {
                1 -> "今日份快乐库存：$input。普通生活，也要偶尔给自己加个鸡腿。"
                2 -> "关于「$input」：本人宣布，这一刻值得发个朋友圈备案。"
                else -> "生活小剧场更新：$input。剧情不复杂，但心情很到位。"
            }

            TextCreationStyle.ShortPremium -> when (index) {
                1 -> "$input。刚刚好。"
                2 -> "关于「$input」，无需多言。"
                else -> "留白一点，记住「$input」。"
            }

            TextCreationStyle.FreeCasual -> when (index) {
                1 -> "走到哪算哪，遇见「$input」就好好享受一下。"
                2 -> "不赶时间，也不解释，今天就和「$input」待在一起。"
                else -> "风来就走，心动就记，$input 也算今天的答案。"
            }

            TextCreationStyle.LiteraryMood -> when (index) {
                1 -> "风把「$input」吹得很轻，像今天悄悄递来的一封信。"
                2 -> "在「$input」里停了一会儿，忽然觉得时间也有温度。"
                else -> "这一刻有「$input」，也有一点不必说破的温柔。"
            }

            TextCreationStyle.EmotionalExpression -> when (index) {
                1 -> "关于「$input」，心里其实有很多话，最后只想说：真好。"
                2 -> "被「$input」击中的瞬间，才发现自己真的需要这样的片刻。"
                else -> "今天的情绪出口，是「$input」。"
            }

            else -> "$input，值得记录。"
        }
        return "$prefix$body"
    }

    private fun productBlock(input: String, style: TextCreationStyle, index: Int): String {
        val title = if (index > 1) "版本 $index" else "版本 1"
        val angle = when (index) {
            1 -> "表达角度：用户真实体验"
            2 -> "表达角度：核心信息速览"
            else -> "表达角度：购买前判断"
        }
        return when (style) {
            TextCreationStyle.Recommendation ->
                """
                $title
                $angle
                标题：最近会反复用到的「$input」
                推荐理由：围绕真实使用场景介绍，语气像朋友分享，不硬推。
                短文案：如果你正在找一件实用又不复杂的选择，可以看看「$input」。
                """.trimIndent()

            TextCreationStyle.SellingPoints ->
                """
                $title
                $angle
                标题：$input 重点速览
                核心卖点：
                1. 信息清晰，方便快速了解。
                2. 适合日常使用场景。
                3. 表达真实克制，不夸大参数。
                """.trimIndent()

            TextCreationStyle.ProfessionalTrust ->
                """
                $title
                $angle
                标题：$input｜理性选择参考
                说明：基于用户提供信息整理用途、适配场景和购买前关注点。
                短文案：用更清楚的结构介绍「$input」，帮助用户判断是否适合自己。
                """.trimIndent()

            TextCreationStyle.PromotionConversion ->
                """
                $title
                $angle
                标题：想入手「$input」可以重点看这几点
                转化文案：适合正在比较同类商品的人，先看核心需求，再决定是否下单。
                行动引导：感兴趣可以收藏对比，按自己的使用场景选择。
                """.trimIndent()

            TextCreationStyle.RedBook ->
                """
                $title
                $angle
                标题：这个「$input」我会怎么介绍
                正文：不是夸张种草，而是把实际体验、适合人群和注意点讲清楚。
                标签：#好物分享 #实用清单 #演示模式
                """.trimIndent()

            TextCreationStyle.ShortVideoScript ->
                """
                $title
                $angle
                开场：如果你最近在看「$input」，先别急着下单。
                中段：重点看它适合什么场景、解决什么问题、有没有必要买。
                收尾：按自己的需求判断，比被夸张话术带着走更重要。
                """.trimIndent()

            else ->
                """
                $title
                $angle
                标题：$input
                短文案：真实介绍商品特点，不编造、不夸大。
                """.trimIndent()
        }
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

    private fun suggestMomentStyles(input: String): List<StyleAdvice> {
        val suggestions = mutableListOf<StyleAdvice>()
        if (containsAny(input, "夜", "海", "风", "雨", "夕阳", "城市", "月")) {
            suggestions += StyleAdvice(
                TextCreationStyle.LiteraryMood,
                "内容包含画面或氛围元素，适合增强文艺感和场景感。"
            )
        }
        if (containsAny(input, "朋友", "家人", "散步", "咖啡", "周末", "早餐")) {
            suggestions += StyleAdvice(
                TextCreationStyle.WarmDaily,
                "有日常陪伴或生活片刻，适合温柔自然地记录。"
            )
        }
        if (containsAny(input, "累", "难过", "开心", "毕业", "告别", "想念")) {
            suggestions += StyleAdvice(
                TextCreationStyle.EmotionalExpression,
                "内容带有明显情绪，适合把感受表达得更清楚。"
            )
        }
        if (suggestions.size < 2) {
            suggestions += StyleAdvice(
                TextCreationStyle.ShortPremium,
                "如果不想显得太用力，可以选择更克制的表达。"
            )
        }
        if (suggestions.size < 3) {
            suggestions += StyleAdvice(
                TextCreationStyle.LightHumor,
                "适合让内容更轻松，减少正式感。"
            )
        }
        return suggestions.distinctBy { it.style }.take(3)
    }

    private fun suggestProductStyles(input: String): List<StyleAdvice> {
        val suggestions = mutableListOf<StyleAdvice>()
        if (containsAny(input, "优惠", "折扣", "活动", "限时", "下单")) {
            suggestions += StyleAdvice(
                TextCreationStyle.PromotionConversion,
                "内容包含促销或行动信息，适合强化转化路径。"
            )
        }
        if (containsAny(input, "参数", "材质", "容量", "尺寸", "功能")) {
            suggestions += StyleAdvice(
                TextCreationStyle.SellingPoints,
                "信息偏具体，适合整理成清晰卖点清单。"
            )
        }
        if (containsAny(input, "测评", "对比", "专业", "品质", "安全")) {
            suggestions += StyleAdvice(
                TextCreationStyle.ProfessionalTrust,
                "内容需要建立信任，适合更理性和克制的介绍。"
            )
        }
        if (suggestions.size < 2) {
            suggestions += StyleAdvice(
                TextCreationStyle.Recommendation,
                "适合作为自然种草文案，降低广告感。"
            )
        }
        if (suggestions.size < 3) {
            suggestions += StyleAdvice(
                TextCreationStyle.RedBook,
                "适合社交平台分享，用体验感带出商品信息。"
            )
        }
        return suggestions.distinctBy { it.style }.take(3)
    }

    private fun containsAny(text: String, vararg keywords: String): Boolean {
        return keywords.any { it in text }
    }
}
