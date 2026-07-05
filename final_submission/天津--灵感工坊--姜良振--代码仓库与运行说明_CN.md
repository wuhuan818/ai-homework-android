# 代码仓库与运行说明

## 代码仓库

- GitHub 仓库：https://github.com/wuhuan818/ai-homework-android
- 最终分支：stage15-final-submission
- 当前 commit：以最终打包时 `git rev-parse HEAD` 和 `evidence/logs/git_info.txt` 记录为准。

## 构建环境

- Android Studio / Gradle
- Kotlin
- Jetpack Compose
- minSdk：26
- targetSdk：36
- versionCode：1
- versionName：1.0.0

## 构建命令

```powershell
.\gradlew.bat assembleDebug
```

构建产物：

```text
app\build\outputs\apk\debug\app-debug.apk
```

## 安装命令

```powershell
adb install -r app\build\outputs\apk\debug\app-debug.apk
```

本次整理阶段连接到设备 `10AC8M0C61001B1`，Android 版本为 `14`。安装尝试因设备端拒绝确认返回 `INSTALL_FAILED_ABORTED: User rejected permissions`，需用户在设备上允许安装后补充成功日志。

## API 配置

在 App 设置页配置 Base URL、API Key、Text Model、Vision Model、Image Generation Endpoint、图片生成接口类型和图片生成模型。真实 API Key 不写入代码仓库、文档、日志或提交包。

## 演示模式

无 Key 或离线时，可使用演示模式完成本地流程。演示结果会标识为演示生成，不会上传用户输入或图片。

## 真实模式

配置真实 API 后，文本生成、风格推荐、文本改写、图片描述和图片生成会调用用户配置的大模型接口。网络、鉴权、限流、服务异常或接口返回格式不匹配时，应用显示可读错误提示。

## 常见问题

- 未配置 Key：请在设置页保存当前配置的 API Key。
- 网络失败：检查设备网络和接口地址。
- 图片生成接口地址错误：检查图片生成接口类型和完整接口地址。
- 旧数据读取失败：应用会提示历史读取异常，可清空历史或重新生成。
