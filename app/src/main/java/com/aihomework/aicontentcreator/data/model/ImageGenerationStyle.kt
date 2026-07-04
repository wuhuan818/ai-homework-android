package com.aihomework.aicontentcreator.data.model

enum class ImageGenerationStyle(
    val displayName: String,
    val promptHint: String
) {
    RealisticPhoto("写实照片", "写实照片风格，画面自然，细节真实"),
    Illustration("插画风格", "温暖插画风格，色彩柔和，构图清晰"),
    PosterDesign("海报设计", "海报设计风格，主体突出，适合视觉传播"),
    ProductDisplay("产品展示", "产品展示风格，干净背景，突出主体质感"),
    Anime("二次元风格", "二次元风格，角色感明确，色彩鲜明")
}
