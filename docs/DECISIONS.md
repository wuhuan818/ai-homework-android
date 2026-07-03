# Decisions

## 2026-06-29 - Project Skeleton

- Use Kotlin and Jetpack Compose for the Android UI.
- Use Material 3 for the initial visual system.
- Keep the first milestone small: a buildable app with four placeholder screens.
- Defer model API integration, Room, Retrofit/OkHttp, and Android Keystore until the skeleton is stable.
- Use `D:\Android\Sdk` as the local Android SDK location to reduce C drive pressure.

## 2026-06-29 - Mock Flow Before Real API

- Build the full demo flow with `MockModelClient` before connecting a real model API.
- Reason: the contest timeline is short, and a mock flow proves the product path before network, credentials, billing, and API error handling add risk.
- Keep the model access behind a `ModelClient` interface so a real client can replace the mock implementation later.
- Use temporary in-memory history for this phase, while keeping repository boundaries ready for Room and encrypted storage.

## 2026-06-29 - Deferred Scope

- Do not add an account system in the mock phase because the contest demo only needs local app behavior.
- Do not add multi-model switching yet because it would increase UI and configuration complexity before the core flow is proven.
- Do not add cloud sync because it requires backend, auth, privacy, and network reliability work outside the current milestone.

## 2026-06-29 - Stage 3 Real API Integration

- Keep `MockModelClient` as the stable fallback path for demos and offline/error situations.
- Add a single OpenAI-compatible Real mode rather than provider selection or multi-model comparison.
- Add only OkHttp for HTTP calls. Use Android `org.json` for request and response JSON to avoid extra serialization dependencies.
- Add only `android.permission.INTERNET`; Photo Picker does not require broad media read permissions for this stage.
- Store API Key with Android Keystore backed AES-GCM encryption. SharedPreferences stores configuration values plus encrypted API Key ciphertext and IV, not the plaintext key.
- Settings exposes Base URL, Text Model, and Vision Model so users can configure one compatible service.
- Image description attempts vision format when an image is selected. If reading the image, request size, provider support, network, auth, or response parsing fails, the app falls back to Mock description with a readable explanation.
- Still deferred: account system, provider comparison, cloud sync, Room, SQLCipher, complex image editing, and encrypted history database.

## 2026-07-01 - Stage 4 Encrypted Local History

- Store history and favorite state locally so app restarts preserve generated, edited, and favorited content.
- Use Android Keystore backed AES-GCM because it keeps the AES key non-exportable and stores only ciphertext plus IV in SharedPreferences.
- Store history as a single encrypted JSON blob for this stage because the app only needs a small local history list and simple favorite toggles.
- Do not add SQLCipher now because it would add database setup and dependency complexity that is not needed for the current evidence requirement.
- Do not add Room now because the current repository can preserve its `StateFlow` API with less risk and no schema migration work.
- Current limitation: a single encrypted blob is not ideal for large histories or advanced queries; future stages can migrate to Room or SQLCipher if search, paging, or structured history management becomes necessary.

## 2026-07-03 - Stage 5 Basic Image Processing

- Implement only rotation and text watermark because the contest requirement asks for at least two basic image processing abilities.
- Do not build filters, crop, drawing, multi-image editing, album management, or a complex editor because those would expand scope and UI risk beyond the stage goal.
- Save processed images in the app cache so results can be shared without requiring gallery writes or broad storage permissions.
- Use Android native `Bitmap`, `Canvas`, and `Matrix` APIs to avoid large image editing dependencies and keep the feature small and reviewable.
- Use Android native `ImageDecoder` on Android 9+ for better Photo Picker Uri compatibility, with `BitmapFactory` kept as the older-version fallback.
- Share processed images through a scoped `FileProvider` that exposes only the `cache/shared_images` directory.
- Current limitation: very large images are downsampled for processing, and future work can add stronger memory handling only if real-device evidence shows a need.

## 2026-07-03 - Stage 6 UI And Prompt Polish

- Prioritize UI wording and Prompt quality because the core app flow is already usable, and the next risk for a contest demo is whether judges can understand what each page is doing.
- Keep Mock / Real boundaries clear so users do not confuse local template output with real model output. Mock output now explicitly marks itself as `【演示模式生成】`, while Real mode explains that configured model endpoints will be called.
- Keep API Key visibility limited to configured / not configured status. The UI should never show the full key.
- Improve prompts before adding more features because better structured outputs make the existing three scenarios easier to demonstrate.
- Do not add configuration presets in this stage because provider-specific defaults would expand the settings surface and increase test risk.
- Do not add multi-model comparison in this stage because the app needs a stable single configured endpoint for demonstration, not a broader benchmarking tool.

## 2026-07-03 - Stage 7 Profile Presets And Image Description Styles

- Add 3 lightweight interface configuration presets because demos often need a primary endpoint plus backup endpoints, but the app still only calls the currently enabled profile.
- Keep presets out of model comparison scope: no scoring, no speed testing, no ranking, and no side-by-side results.
- Store each profile's display name, Base URL, text model, vision model, and key status in settings, while keeping the full API Key out of the plaintext `AppSettings` object.
- Reuse Android Keystore backed AES-GCM for profile API Keys, with a separate ciphertext and IV per profile.
- Add image description style selection because the same image can reasonably need an objective description, a social caption, or product-oriented copy in a contest demo.
- Keep generation as a single request and single result display because it protects the existing Mock / Real flow and avoids turning style selection into a benchmarking feature.
- Current limitation: backup profiles ship with empty endpoint/model fields and must be filled by the user before Real mode can use them.
