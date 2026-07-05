# Decisions

## 2026-07-05 - Stage 14.5 Secure Local Storage

- Keep Android Keystore + AES-GCM because the app already uses that scheme for encrypted history and API keys; reusing and strengthening it is lower risk than migrating to EncryptedSharedPreferences during this stage.
- Explicitly use 256-bit AES keys, randomized IVs, and 128-bit GCM authentication tags for newly generated Keystore keys.
- Add versioned encrypted payloads so later migrations can distinguish storage formats while old history and API key payloads without a stored payload version still read through the legacy-compatible path.
- Encrypt generated image binaries because image metadata alone was not enough for the local encrypted storage requirement; new image files are stored under `files/generated_images_encrypted/`.
- Keep old plaintext generated-image files readable from `files/generated_images/` when present, so older image history records do not crash or disappear solely because the file format changed.
- Keep history as a single encrypted JSON blob because the current app needs a small local work list, not database search, paging, schema migration, or cloud sync.
- Temporarily decrypt generated images into `cache/decrypted_generated_images/` for preview, sharing through FileProvider, and user-initiated gallery export; long-term storage remains encrypted.
- Disable Android automatic backup with `android:allowBackup="false"` to avoid restoring encrypted SharedPreferences or encrypted image files onto a device that does not have the original Keystore keys.
- Security boundary: this protects ordinary local file inspection scenarios, but it does not claim protection from root access, a malicious OS, runtime screenshots, memory inspection, or external copies after the user exports/shares content.

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

## 2026-07-04 - Stage 13 Image Generation And Work Management

- Add image generation now because the product is moving from text and image understanding into a small, demonstrable media creation workflow.
- Keep the first image generation step to minimal text-to-image because it fits the existing Create / History / Share model without becoming a complex image workstation.
- Defer image-to-image, inpaint, multi-image generation, negative prompts, seeds, steps, cfg, samplers, ranking, scoring, speed tests, and model comparison because each would expand UI, storage, provider behavior, and verification scope.
- Keep image generation model configuration separate from the Vision model because image description and image generation are different provider capabilities and may use different model names.
- Store image works in History and Favorites because generated images are user works just like generated text, and users need the same retrieval, share, favorite, and delete paths.
- Move the clear-history action upward because destructive global actions should be visible in the history task area rather than hidden after long lists.
- Add edit restore instead of only a confirmation dialog because it preserves the fast rewrite workflow while giving users a way back if the new text is worse.
- Current limitation: generated image files are app-private files and are not encrypted as binary blobs; history metadata remains encrypted, and future work can encrypt image files if storage risk or product requirements justify it.

## 2026-07-04 - Stage 13.1 Qwen-Image Official API Adaptation

- Do not force all image generation providers through `/images/generations` because Qwen-Image official synchronous HTTP uses a different request shape and may require a full service endpoint.
- Store the image generation endpoint separately from the text Base URL because text/chat, vision understanding, and image generation can live on different service paths or regions.
- Keep the OpenAI-compatible image interface as an option so existing compatible gateways still work without changing their configuration.
- Do not implement asynchronous task polling in this stage because `qwen-image-2.0-pro` is being adapted through the recommended synchronous HTTP path for the current test target.
- Download generated remote URLs into app-private local files before history and sharing so encrypted history stores only metadata and not a remote image URL.

## 2026-07-04 - Stage 14 V1 Polish

- Use “灵感工坊” as the temporary V1 product name because it is more understandable to end users than the engineering project name while still fitting the original AI 内容创作助手 positioning.
- Use a warm creative adaptive icon because the app is a creation tool, and a pen tip, image frame, speech bubble, and star accents communicate writing, images, conversation, and inspiration without external copyrighted assets.
- Change the Edit page rewrite flow to original / rewritten comparison because immediately replacing the editing text makes it hard for users to compare and can feel destructive.
- Add History “再次使用 / 再次生成” because history should be a re-creation entry, not only a storage list; restoring inputs without auto-generating keeps user control and avoids surprise model calls.
- Add only black-and-white filter and center crop because they are useful, native Bitmap operations that do not require new libraries or a complex editor surface.
- Continue to defer freehand drawing and complex filter collections because they would expand the UI and testing surface beyond a V1 polish pass.
- Continue to defer gesture crop because center crop gives a low-risk crop path now while preserving room for a future manual selection interaction.
- Weaken user-visible Mock wording to “演示模式” because it explains the local-generation behavior without exposing implementation language; technical docs can still mention Mock where useful.

## 2026-07-04 - Stage 14.2 Creation UI Polish

- Prioritize Create screen layout and button density because the app already has the main V1 functions, and the next visible risk is crowded controls, awkward Chinese wrapping, and unclear task hierarchy.
- Split “帮我推荐风格” from “生成 3 个版本” because recommendation is an action while multi-version is a setting; separating them keeps the style area readable on narrow screens and preserves both existing logic paths.
- Add image prompt optimization instead of advanced image parameters because users need help turning simple descriptions into usable image prompts, while seed, steps, cfg, and sampler would expand configuration complexity beyond this stage.
- Let text results become image prompts because generated copy often contains enough scene or product context to start a companion image, and the handoff keeps user control by not auto-generating images.
- Keep center crop as the existing automatic center-crop action because it is already buildable and useful, while deeper crop work would compete with the higher-priority creation-page layout fixes.
- Defer gesture crop to a later stage because manual selection needs new interaction design, preview behavior, and real-device validation that are outside this 14-2 pass.

## 2026-07-04 - Stage 14.4 Creation Structure Polish

- Lightly split `CreateScreen` because the page had grown into one large file containing scenario selection, text settings, image description, image generation, image processing, results, and bitmap preview logic.
- Keep the split at UI component boundaries rather than introducing a full MVVM rewrite because the current state source and callbacks are already working and the stage goal is reviewable structure reduction.
- Collapse the scenario entry after selection so long text results and image-description flows spend less vertical space on the top selector while keeping a clear `当前创作` summary and `切换` entry.
- Default long text results to preview with expand/collapse because complete generated copy can be long, but copy, edit, share, favorite, and image-prompt handoff must still use the full stored result.
- Do not force image prompts to become very short; instead make original and optimized prompts expandable so users can inspect complete useful prompt text without silent truncation.
- Add a confirmation step before using text results for companion images because product copy or long social text may need to be整理成画面描述 before it is suitable for text-to-image prompting.
- Add a separate `prepareImagePromptFromText` path instead of changing `optimizeImagePrompt` so the UI can keep `优化提示词` and `整理提示词` as distinct actions.
- Keep gesture crop for a later stage because manual selection needs dedicated touch interaction, preview behavior, and real-device validation beyond this creation-page polish pass.

## 2026-07-05 - Stage 14.6 Gesture Box Crop

- Add gesture box crop as its own stage because manual crop needs touch interaction, fitted-image coordinate mapping, file output, and real-device validation that are riskier than quick center crop.
- Keep center crop as quick crop because fixed 1:1 / 4:3 / 3:4 / 16:9 / 9:16 center crops are still useful one-tap actions and already work in the existing processing chain.
- Implement only lightweight P0 gestures because dragging the crop rectangle and resizing from the lower-right handle covers the immediate need without introducing a large crop library.
- Do not add multi-touch image zoom, rotated crop rectangles, drawing, stickers, inpaint, image-to-image, or a professional editor surface because those expand the product and testing scope beyond the stage goal.
- Output box-crop results as a new `processedImageUri` so the original selected image remains intact, downstream image description and sharing use the processed result, and `恢复原图` can continue clearing only the processed image state.
