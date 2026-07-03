# AIContentCreator

面向 ISBG 2026 AI 作业比赛的 Android AI 内容创作助手。

## 项目目标

构建一个可在 Android 真机展示的 AI 内容创作 App，支持朋友圈文案、商品描述、图片描述、文本编辑、收藏历史、本地加密保存、图片基础处理和 Android 系统分享。

## 当前状态

已实现最小可用系统：

1. 演示模式 / 真实模型模式切换。
2. OpenAI-compatible Chat Completions 风格的大模型接口接入。
3. 图片选择与图片描述。
4. 文本编辑、Markdown / 纯文本转换、收藏、历史记录和系统分享。
5. Android Keystore + AES-GCM 加密保存模型密钥和历史内容。
6. 图片旋转 90°、文字水印和处理后图片分享。
7. 创作页、编辑页、历史页、设置页主要用户可见文案已中文化。

## 技术栈

- Kotlin
- Jetpack Compose
- Material 3
- OkHttp
- Android Photo Picker
- Android Keystore + AES-GCM
- FileProvider

## 演示模式与真实模型模式

### 演示模式

- 使用本地 `MockModelClient` 模板生成内容。
- 输出会明确标注 `【演示模式生成】`。
- 不上传用户输入或图片。
- 适合离线演示、比赛讲解和基础流程验证。

### 真实模型模式

- 使用用户在设置页配置的接口地址、文本模型、图像模型和模型密钥。
- 调用 OpenAI-compatible Chat Completions 接口。
- 文本输入和已选择的图片会发送到用户配置的大模型接口。
- 模型密钥不会在 UI 中完整展示，保存时通过 Android Keystore 加密。

## Prompt 设计思路

### 朋友圈文案

要求模型生成 3 条不同风格的朋友圈文案：温柔日常、轻松幽默、简洁高级。每条控制在 60 字以内，避免广告腔和空泛表达，并尽量保留用户输入的具体细节。

### 商品描述

要求模型按固定结构输出：标题、核心卖点、适用人群、使用场景、短文案。Prompt 明确要求不编造参数、不夸大功效，语言真实克制。

### 图片描述

要求模型输出画面主体、背景与氛围、适合社交平台发布的配文、可能的标签。Prompt 明确禁止编造人物身份、品牌或图片中不存在的内容。

## 隐私与合规说明

- 演示模式不上传内容，只使用本地模板。
- 真实模型模式会把输入内容发送到用户配置的大模型接口。
- 模型密钥仅在设置页输入，UI 只显示已配置 / 未配置，不展示完整密钥。
- 模型密钥通过 Android Keystore + AES-GCM 加密保存。
- 历史内容本地加密保存，生成内容不会直接以明文写入本地历史文件。
- 不要把真实 API Key 写入源码、Git 提交、README、文档、日志、截图或问题报告。

## 主要使用流程

1. 打开 App，进入“创作”页。
2. 选择朋友圈文案、商品描述或图片描述。
3. 在演示模式下直接生成演示内容，或在设置页配置模型密钥后切换到真实模型模式。
4. 查看创作结果，并可继续编辑、收藏或分享。
5. 在图片描述场景中，可选择图片、旋转 90°、添加文字水印，并分享处理后的图片。
6. 在“历史”页查看本地保存的作品和收藏状态。
7. 在“设置”页查看模型模式、接口配置、模型密钥状态和安全存储说明。

## 真实模型配置

1. 打开“设置”页。
2. 将模型模式切换为“真实模型模式”。
3. 输入模型密钥（API Key）并点击“保存密钥”。
4. 根据需要配置接口地址（Base URL）、文本模型（Text Model）和图像模型（Vision Model）。
5. 回到“创作”页，确认顶部显示真实模型模式提示。

默认配置：

- 接口地址（Base URL）：`https://api.openai.com/v1`
- 文本模型（Text Model）：`gpt-4o-mini`
- 图像模型（Vision Model）：`gpt-4o-mini`

## 图片基础处理验证

1. 安装 debug APK 并打开 App。
2. 进入“图片描述”场景。
3. 点击“选择图片”，选择一张设备图片。
4. 点击“旋转 90°”，确认预览和状态更新。
5. 输入水印文字，点击“添加水印”，确认处理后的预览和状态更新。
6. 点击“分享图片”，确认 Android 系统分享面板打开。
7. 未选择图片时点击图片处理操作，应看到中文提示。

## 加密历史验证

1. 使用演示模式生成包含可识别短语的内容，例如 `LOCAL-VERIFY-2026`。
2. 收藏结果，或进入编辑页保存修改。
3. 关闭并重新打开 App，确认“历史”页仍显示作品。
4. 检查 debug SharedPreferences，确认可识别短语没有以明文出现。

可用命令：

```powershell
adb shell run-as com.aihomework.aicontentcreator ls shared_prefs
adb shell run-as com.aihomework.aicontentcreator cat shared_prefs/ai_content_creator_history.xml
```

如果设备不支持 `run-as`，可使用 Android Studio Device Explorer 查看 debug 包的 `shared_prefs` 目录。

## 构建

在项目根目录使用 Windows PowerShell：

```powershell
.\gradlew.bat tasks
.\gradlew.bat assembleDebug
```

## 安装 debug APK

连接已开启 USB 调试的 Android 手机：

```powershell
adb devices
adb install -r app\build\outputs\apk\debug\app-debug.apk
```

## 当前限制

- 当前仅支持一个 OpenAI-compatible Chat Completions 配置，不做配置预设和多模型对比。
- 图片描述能力取决于用户配置的服务是否支持 vision 输入。
- 真实图片描述失败时会回退到演示模式输出，并展示中文原因。
- 历史记录以单个本地加密 JSON blob 保存，适合比赛阶段展示，不适合大量数据检索。
- 图片处理仅包含旋转和文字水印，处理后图片保存于 App cache，不作为永久相册导出。
- 当前没有账号系统、云同步、复杂图片编辑器或应用商店发布流程。
