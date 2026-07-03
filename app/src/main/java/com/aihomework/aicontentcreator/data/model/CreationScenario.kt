package com.aihomework.aicontentcreator.data.model

enum class CreationScenario(
    val displayName: String,
    val description: String,
    val inputHint: String
) {
    Moments(
        displayName = "朋友圈文案",
        description = "把生活主题改写成适合社交平台发布的自然文案。",
        inputHint = "例如：周末咖啡、毕业旅行、夜跑后的风"
    ),
    Product(
        displayName = "商品描述",
        description = "根据商品信息生成真实克制、结构清楚的介绍文案。",
        inputHint = "例如：便携保温杯、蓝牙耳机、护眼台灯"
    ),
    ImageDescription(
        displayName = "图片描述",
        description = "根据图片或图片线索生成适合发布的中文描述。",
        inputHint = "例如：一张城市夜景照片，或点击使用示例图片"
    )
}
