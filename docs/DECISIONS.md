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

## 2026-07-04 - Stage 8 Image Upload And History Polish

- Compress large images before sending them to the vision model because real-device photos can exceed provider request limits or make uploads unstable during a live demo.
- Generate a temporary upload-only image copy so the user's original selected image is not overwritten, and the existing rotation, watermark, preview, and share flow can keep using their own Uri state.
- Keep the first upload target around 1.5 MB to 2 MB with a 1600 px maximum side, starting JPEG quality at 85 and only reducing quality or dimensions when needed.
- Keep history management intentionally simple: add `全部 / 收藏夹` filtering, single-item deletion, and confirmation dialogs, but do not add categories, albums, search, Room, SQLCipher, accounts, or cloud sync.
- Current limitation: compression can make small visual details harder for the model to recognize, so the UI warns users when compression is used.

## 2026-07-04 - Stage 9 Vision Trust Polish

- Do not silently fall back to Mock for real API errors such as auth failure, rate limits, network failure, timeout, empty Base URL, invalid Base URL, empty model name, service errors, or unrecognized response format because that would make a failed real call look successful.
- Use Mock fallback only for image capability issues such as image read failure, unsupported or incompatible image format, image too large for stable upload, or a configured model/interface that cannot accept image input.
- Clearly prefix fallback output with `真实图片描述不可用，已使用演示模式兜底。` so users can distinguish a demo safety net from a real vision result.
- Prepare a vision upload copy that may be compressed or converted to JPEG because HEIC, HEIF, unknown MIME types, and large device photos can be rejected by compatible providers even when the original image is valid on the device.
- Keep upload copy preparation separate from the selected or processed image Uri so rotation, watermark, preview, and image sharing continue to use the existing image flow.
- Save the current profile fields before saving that profile's API Key because users often edit Base URL/model fields and key together, and a key save should not leave the profile pointing at stale configuration values.

## 2026-07-04 - Stage 10 UI Image Experience

- Remove the Create screen's broad capability sentence because the page already has scenario cards and mode notices; repeating the app feature list makes the top area feel heavier without helping the next action.
- Keep the short prompt `选择场景，开始创作。` so the user sees an immediate action cue while the existing Mock / Real mode and privacy notices remain visible.
- Do not add image generation because the current stage is about image description, preview, and basic processing experience; generation would require new product scope, stronger safety wording, and likely new provider behavior.
- Add only `恢复原图` instead of a complex editor because clearing `processedImageUri` restores the selected image with minimal state risk and preserves the existing rotate, watermark, share, Mock, and Real paths.
- Use a small local drawable example image rendered into cache rather than a network image so demos work offline, avoid copyright-sensitive user photos, and provide a normal content Uri for preview, processing, and Real-mode vision preparation.

## 2026-07-04 - Stage 11 UX Information Architecture

- Rework the four main pages from the user's next action because earlier UI polish solved isolated copy problems but did not fully align page hierarchy across the App.
- Weaken engineering and competition explanation inside the App because judges and developers can read README / docs, while normal users need task-oriented screens.
- Keep History focused on 作品列表、收藏夹和删除操作; encrypted storage remains visible only as a lightweight safety note instead of a top-of-page technical proof.
- Move Settings toward current status, mode choice, presets, advanced fields, self-test, and safety note so configuration debugging is easier without turning the page into a technical appendix.
- Add current-configuration text model self-test because Real mode setup often fails at endpoint, model, network, or key entry; keep it as a single short request with readable errors.
- Do not add model comparison, scoring, speed tests, rankings, cloud sync, account features, or image generation because those would expand scope beyond information architecture and setup confidence.
- Continue not to implement image generation because the current product line is content writing, image description, and basic local image handling; image generation would require new UX, safety copy, provider behavior, and evidence scope.

## 2026-07-04 - Stage 12 Content Richness

- Add style choices for 朋友圈文案 and 商品文案 because real users often start from an event, mood, or product facts but still need an expression direction.
- Keep the style selectors scene-specific so 朋友圈文案 shows 温馨日常、轻松幽默、简短高级、洒脱随性、文艺氛围、情绪表达, while 商品文案 shows 种草推荐、卖点清单、专业可信、促销转化、小红书风、短视频口播.
- Add style recommendation because asking users to guess the best tone creates unnecessary friction; the app can infer 2 to 3 reasonable directions from keywords in Mock mode or from the configured text model in Real mode.
- Add optional 3-version generation because comparison helps users choose and edit, while still keeping one active model path and one saved result instead of ranking models or benchmarking output.
- Keep history unchanged because saving the final generated text preserves old encrypted history compatibility and avoids a data migration for this stage.
- Add Edit page rewrite actions so users can continue refining generated content in place; rewrite does not update history until the user taps save.
- Continue not to implement image generation because this stage is about writing richness, not a new media-generation product surface.
