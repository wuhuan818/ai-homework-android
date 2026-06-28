package com.aihomework.aicontentcreator.data.model

enum class CreationScenario(
    val displayName: String,
    val description: String,
    val inputHint: String
) {
    Moments(
        displayName = "朋友圈文案",
        description = "把生活主题改写成适合社交平台发布的文案。",
        inputHint = "例如：周末咖啡、毕业旅行、夜跑"
    ),
    Product(
        displayName = "商品描述",
        description = "根据商品信息生成简洁、实用的介绍文案。",
        inputHint = "例如：便携保温杯、蓝牙耳机、护眼台灯"
    ),
    ImageDescription(
        displayName = "图片描述",
        description = "模拟图片理解结果，生成可用于发布的图片说明。",
        inputHint = "例如：一张城市夜景照片，或点击模拟图片"
    )
}

