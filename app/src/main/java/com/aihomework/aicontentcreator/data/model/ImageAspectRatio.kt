package com.aihomework.aicontentcreator.data.model

enum class ImageAspectRatio(
    val displayName: String,
    val size: String,
    val qwenOfficialSize: String,
    val width: Int,
    val height: Int
) {
    Square("1:1", "1024x1024", "2048*2048", 1024, 1024),
    LandscapeClassic("4:3", "1024x768", "2368*1728", 1024, 768),
    PortraitClassic("3:4", "768x1024", "1728*2368", 768, 1024),
    LandscapeWide("16:9", "1280x720", "2688*1536", 1280, 720),
    PortraitWide("9:16", "720x1280", "1536*2688", 720, 1280)
}
