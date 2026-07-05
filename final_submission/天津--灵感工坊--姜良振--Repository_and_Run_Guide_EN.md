# Repository And Run Guide

## Repository

- GitHub repository: https://github.com/wuhuan818/ai-homework-android
- Final branch: stage15-final-submission
- Current commit: `744fdf0e9440395833efe53e5bb617f534e53c44`

## Build Environment

- Android Studio / Gradle
- Kotlin
- Jetpack Compose
- minSdk: 26
- targetSdk: 36
- versionCode: 1
- versionName: 0.1.0

## Build Command

```powershell
.\gradlew.bat assembleDebug
```

Build artifact:

```text
app\build\outputs\apk\debug\app-debug.apk
```

## Install Command

```powershell
adb install -r app\build\outputs\apk\debug\app-debug.apk
```

During final packaging, device `10AC8M0C61001B1` was visible and reported Android `14`. The install attempt returned `INSTALL_FAILED_ABORTED: User rejected permissions`, so a successful install log should be added after the user approves installation on the device.

## API Configuration

Use the in-app Settings page to configure Base URL, API Key, Text Model, Vision Model, Image Generation Endpoint, image generation API type, and image generation model. Real API keys are not written into the repository, documentation, logs, or submission package.

## Demo Mode

Without a key or network access, Demo mode can complete the local workflow. Demo output is labeled and user input/images are not uploaded.

## Real Mode

After real API configuration, text generation, style recommendation, rewrite, image description, and image generation call the configured model service. Network, auth, rate-limit, service, or response-format errors are shown as readable messages.

## Common Issues

- Missing key: save the active profile API key in Settings.
- Network failure: check device network and service address.
- Wrong image generation endpoint: check the API type and full image-generation endpoint.
- Legacy data read failure: the app reports the issue; the user can clear history or generate new content.
