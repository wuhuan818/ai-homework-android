# AIContentCreator

Android AI content creation tool for the ISBG 2026 AI homework contest.

## Project Goal

Build an Android AI content creation tool that supports text generation, image description, copywriting scenarios, text editing, favorite/history records, local encrypted storage, and Android system sharing.

## Non-goals

- Do not build a self-developed model.
- Do not build multi-model switching.
- Do not build an account system.
- Do not build cloud sync.
- Do not build complex image editing.
- Do not publish to an app store.

## Completion Criteria

1. The app can be installed and opened on a real Android device.
2. The app provides at least 3 creation scenario entry points.
3. The app can later complete the flow: input -> call model -> display -> edit -> favorite/share.
4. The app has a local encrypted storage plan.
5. The app has exception handling.
6. The project has screenshot or screen recording evidence.
7. The project has an AI collaboration explanation.

## Tech Stack

- Kotlin
- Jetpack Compose
- Material 3
- MVVM-ready package structure
- Planned: Room, Retrofit/OkHttp, Android Keystore

## Current Progress

- Mock version of the complete creation flow is implemented.
- The app supports scene selection, input, mock AI generation, result display, editing, format conversion, history/favorite records, and Android system sharing.
- Current model access keeps `MockModelClient` and adds `RealModelClient` behind the same `ModelClient` interface.
- Settings supports Mock / Real mode, Base URL, text model, vision model, and API Key entry.
- API Key is entered only in the app Settings screen and stored locally with Android Keystore backed AES-GCM encryption.
- Real mode uses an OpenAI-compatible Chat Completions endpoint.
- Image description supports Android Photo Picker selection. Real vision calls are attempted when a selected image and vision model are configured; failures fall back to Mock description with a user-readable message.

## Mock Demo Paths

1. Select `朋友圈文案`, enter a topic, and generate mock social copy.
2. Select `商品描述`, enter product information, and generate mock product copy.
3. Select `图片描述`, use the mock image button or enter image notes, and generate a mock image description.
4. Open the generated result in the edit screen.
5. Convert the result to Markdown or plain text.
6. Save the edit and return to the result display.
7. Favorite generated content and view it in History.
8. Share generated or edited text with the Android system share sheet.

## Next Stage Plan

- Keep `MockModelClient` as a stable demo fallback while improving `RealModelClient`.
- Test Real mode on a real device with a user-entered API Key.
- Replace temporary in-memory history with Room plus Android Keystore based encryption.
- Add exception handling for network, API, and storage failures.
- Collect screenshots or screen recordings for contest evidence.

## Real API Configuration

1. Open Settings in the app.
2. Switch mode from `Mock` to `Real`.
3. Enter the API Key on the device and tap `Save API Key`.
4. Confirm the status changes to `configured (hidden)`.
5. Configure Base URL, Text Model, and Vision Model if needed.

Do not put real API keys in source code, Git commits, README files, docs, logs, prompts, screenshots, or bug reports.

Default OpenAI-compatible values:

- Base URL: `https://api.openai.com/v1`
- Text Model: `gpt-4o-mini`
- Vision Model: `gpt-4o-mini`

Current limitations:

- The app targets OpenAI-compatible Chat Completions style APIs.
- History remains in-memory for this stage.
- Vision support depends on the configured service. If the call fails, image description falls back to Mock output.

## Build

Use Windows PowerShell from the project root:

```powershell
.\gradlew.bat tasks
.\gradlew.bat assembleDebug
```

## Install Debug APK

After a successful build, connect an Android phone with USB debugging enabled:

```powershell
adb devices
adb install -r app\build\outputs\apk\debug\app-debug.apk
```
