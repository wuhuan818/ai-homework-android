package com.aihomework.aicontentcreator.data.model

enum class TextCreationStyle(
    val displayName: String,
    val scenario: CreationScenario
) {
    WarmDaily("温馨日常", CreationScenario.Moments),
    LightHumor("轻松幽默", CreationScenario.Moments),
    ShortPremium("简短高级", CreationScenario.Moments),
    FreeCasual("洒脱随性", CreationScenario.Moments),
    LiteraryMood("文艺氛围", CreationScenario.Moments),
    EmotionalExpression("情绪表达", CreationScenario.Moments),
    Recommendation("种草推荐", CreationScenario.Product),
    SellingPoints("卖点清单", CreationScenario.Product),
    ProfessionalTrust("专业可信", CreationScenario.Product),
    PromotionConversion("促销转化", CreationScenario.Product),
    RedBook("小红书风", CreationScenario.Product),
    ShortVideoScript("短视频口播", CreationScenario.Product);

    companion object {
        fun defaultFor(scenario: CreationScenario): TextCreationStyle {
            return when (scenario) {
                CreationScenario.Moments -> WarmDaily
                CreationScenario.Product -> Recommendation
                CreationScenario.ImageDescription -> WarmDaily
                CreationScenario.ImageGeneration -> WarmDaily
            }
        }

        fun optionsFor(scenario: CreationScenario): List<TextCreationStyle> {
            return entries.filter { it.scenario == scenario }
        }

        fun fromDisplayName(name: String, scenario: CreationScenario): TextCreationStyle? {
            return optionsFor(scenario).firstOrNull { it.displayName == name.trim() }
        }
    }
}
