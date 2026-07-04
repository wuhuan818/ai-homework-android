# AI Collaboration Notes

This project is built with AI assistance. AI is used for environment checks, project scaffolding, documentation drafts, and implementation support.

Rules:

- Do not paste API keys, passwords, or tokens into prompts.
- Review AI-generated code before accepting it.
- Keep generated changes small enough to inspect.
- Record major AI-assisted decisions in `docs/DECISIONS.md`.
- Keep evidence such as screenshots, build logs, and install verification for the final submission.

## 2026-06-29 - Mock Flow Implementation

Codex generated or modified:

- Data models for creation scenarios, requests, results, and history items.
- `ModelClient` and `MockModelClient`.
- Creation and history repositories.
- Compose screens for Create, Edit, History, and Settings.
- System share intent helper.
- README and decision/evidence documentation updates.

Human verification points:

- Confirm the APK installs on a real phone.
- Run the three demo scenarios on device.
- Verify Markdown/plain-text conversion is understandable.
- Capture screenshots or recordings for the final submission.
- Confirm no API key or private token is present in prompts, logs, or project files.

## 2026-06-29 - Stage 3 Real API Support

Codex generated or modified:

- Settings data model and repository for mode, Base URL, model names, and encrypted API Key storage.
- `RealModelClient` for OpenAI-compatible Chat Completions requests.
- Settings UI for Mock / Real mode, API Key save/clear, Base URL, Text Model, and Vision Model.
- Android Photo Picker entry for selecting an image in the image description scenario.
- Error handling for missing API Key, empty config, network failures, timeout, 401/403, 429, 5xx, empty body, JSON parse failure, long input, and oversized image reads.

Human verification points:

- Enter the real API Key only on the device in Settings.
- Verify the API Key is hidden after saving.
- Test Mock mode after the Real mode changes.
- Test Real text generation and product copy with a valid compatible endpoint.
- Test image selection and confirm vision failure falls back to Mock output when needed.
- Review `git diff` before commit to confirm no real API Key or token is present.

AI risk notes:

- API formats may differ across OpenAI-compatible providers.
- Vision support may not be available even when text generation works.
- Large images can exceed request limits and should be handled by the current size guard or a future compression step.

## 2026-07-01 - Stage 4 Encrypted History Storage

Codex generated or modified:

- `CryptoManager` for Android Keystore backed AES-GCM encryption and decryption.
- `EncryptedHistoryStorage` for encrypted JSON history persistence in SharedPreferences.
- `HistoryRepository` so generated results, edits, favorite toggles, and clears are saved locally.
- History and Settings UI status text for encrypted storage visibility.
- README, decision notes, and evidence instructions for checking persistence and encrypted storage.

Human verification points:

- Generate Mock content with a recognizable phrase, then close and reopen the app.
- Confirm History still contains the generated or edited item.
- Export or inspect `shared_prefs/ai_content_creator_history.xml`.
- Search for the recognizable phrase and confirm it is not present as plaintext.
- Test Real mode again with a device-entered API Key to confirm the stage 3 path still works.

AI risk notes:

- Encryption must cover generated content, edited content, original input, and favorite state, not just part of the item.
- Serialization bugs could lose `isFavorite`, scenario, or timestamps when reloading.
- SharedPreferences files should contain only ciphertext and IV for history content; plaintext generated copy would fail the evidence requirement.

## 2026-07-03 - Stage 5 Basic Image Processing

Codex generated or modified:

- `ImageProcessor` for native Bitmap rotation, text watermark drawing, cache output, and FileProvider content Uri creation.
- Create screen state and UI controls for selected image status, preview, rotate, watermark text, add watermark, processed-image share, and processing messages.
- `MainActivity` glue code for image processing actions while preserving the existing Mock / Real generation path.
- FileProvider manifest and cache path XML limited to processed images under `cache/shared_images`.
- README, decision notes, and evidence instructions for Stage 5 verification.

Human verification points:

- Select a real device image and confirm the preview/status updates.
- Tap rotate and confirm the processed image appears changed.
- Enter visible watermark text and confirm it appears on the processed image.
- Share the processed image and confirm the Android system share sheet opens.
- Tap rotate or watermark before choosing an image and confirm the prompt is readable.
- Re-test Mock and Real generation paths, especially image description, after image processing changes.

AI risk notes:

- Large images can still pressure memory even with downsampling.
- Photo Picker Uri read access can vary by device/provider, so real-device testing is required.
- FileProvider authorities and cache paths must remain aligned or image sharing will fail.
- Processed images are temporary cache files and should not be treated as permanent gallery items.

## 2026-07-03 - Stage 6 UI And Prompt Polish

Codex generated or modified:

- Chinese user-facing copy across Create, Edit, History, Settings, Share, error prompts, and status prompts.
- Create screen mode notice for demo mode, real model mode, and missing model key state.
- Generation button text that changes between demo generation and real model generation.
- Real model prompts for Moments, Product, and Image Description scenarios.
- Mock output templates that clearly mark `【演示模式生成】`.
- README, decision notes, and evidence plan for the UI / Prompt polish stage.

Human verification points:

- Check the app on a real device for any remaining mixed Chinese / English UI wording.
- Confirm Real mode output is more specific and follows the requested structure.
- Confirm Mock mode still works offline and cannot be mistaken for a real model response.
- Confirm processed images are used for image description after rotation or watermark.
- Re-test sharing, encrypted history, API Key storage, and image processing.

AI risk notes:

- Prompt improvements can guide output structure but cannot guarantee every provider will follow format perfectly.
- UI wording should be reviewed on device because button width and line wrapping may differ from desktop code review.
- Real model behavior depends on the configured endpoint and model capability.

## 2026-07-03 - Stage 7 Profile Presets And Image Description Styles

Codex generated or modified:

- Settings storage for 3 lightweight API configuration presets.
- Settings UI for selecting the active preset, editing the current preset, and saving or clearing its encrypted API Key.
- Real model generation path so API calls read Base URL, text model, vision model, and API Key from the active preset.
- Image description state, UI selector, Mock templates, and Real prompts for `客观描述`、`社交配文`、`商品文案`.
- README, decision notes, and evidence plan for the stage 7 verification path.

Human verification points:

- Confirm preset switching updates the current enabled configuration.
- Confirm each profile's API Key status is displayed only as `已配置` or `未配置`.
- Confirm Real mode calls use the active profile and show a readable prompt when the active profile has no key, Base URL, or model name.
- Confirm the three image description styles produce visibly different Mock and Real outputs.
- Re-test Mock / Real generation, image rotation, watermark, sharing, and encrypted history.

AI risk notes:

- Existing single-profile settings and API Key data are preserved for the default profile, but should be checked on an upgraded install.
- Some compatible endpoints may use different model names or vision support behavior.
- Style prompts guide output but cannot guarantee every provider follows the requested structure exactly.

## 2026-07-04 - Stage 8 Image Upload And History Polish

Human raised:

- Large image uploads can fail in Real mode image description demos.
- History could only be cleared all at once.
- Favorites had no dedicated filtering entry.

Codex generated or modified:

- `VisionImagePreprocessor` for upload-only image preparation with dimension and JPEG size controls.
- Real image description path so large images are compressed before vision upload and compression failures become readable Chinese prompts.
- Create screen state and UI to show the compression notice only when an upload copy was compressed.
- History repository and screen support for single-item delete, delete confirmation, `全部 / 收藏夹` filtering, empty favorites state, and clear-history confirmation.
- README, decision notes, and evidence plan for Stage 8 verification.

Human verification points:

- Select a large real device image in Real mode and confirm the compression notice appears after generation.
- Confirm the image description result still succeeds with a compatible vision endpoint.
- Confirm rotation, watermark, processed-image share, Mock mode, and Real text generation still work.
- Confirm History can switch between `全部` and `收藏夹`, and favorite toggles update the filtered list immediately.
- Confirm single delete and clear history both show confirmation dialogs before removing encrypted local data.

AI risk notes:

- Compression can reduce fine-detail recognition even when it improves upload stability.
- Some providers may still reject image data URLs or have stricter request limits.
- Real-device Photo Picker Uri behavior should be verified because content providers can report size and decode metadata differently.

## 2026-07-04 - Stage 9 Vision Trust Polish

Human raised:

- Image description Prompt did not fully use the user's input clues.
- The fallback strategy could hide real API errors behind Mock output.
- HEIC or unknown image formats could affect Vision interface compatibility.
- Saving an API Key might not save edited profile fields from the same screen state.
- GitHub code readability needed formatting so key files were not hard to review.

Codex generated or modified:

- Real image description Prompt now includes user supplement text or image/example clues, and explicitly distinguishes text-only example clues from real image recognition.
- Real API errors now surface as readable errors instead of Mock fallback for auth, network, timeout, rate limit, service, config, and response-format failures.
- Mock fallback is limited to image preparation and image-input capability problems, with an explicit demo fallback notice.
- Vision upload preparation now directly uploads only JPEG, PNG, or WebP files that are already small enough; HEIC, HEIF, unknown, or oversized images are converted/compressed into an upload-only JPEG copy.
- Saving an API Key now saves the current profile fields first, reloads settings afterward, and the Settings UI explains profile-specific encrypted keys.

Human verification points:

- Test Real mode with a deliberately invalid key and confirm it shows an auth error rather than Mock content.
- Test Real mode image description with a valid vision-capable endpoint and confirm user supplement text affects the output.
- Test a large or HEIC image if available and confirm the conversion/compression notice appears without replacing the original image.
- Switch profiles, save profile fields plus key, and confirm the active profile and key status remain correct after reload.
- Re-test Mock mode, Real text generation, history delete/filtering, encrypted history persistence, rotation, watermark, and sharing.

AI risk notes:

- Provider-specific error bodies vary, so Vision capability fallback uses conservative image-related hints instead of falling back on every HTTP 400.
- JPEG conversion can reduce fine visual detail, but it improves compatibility and keeps the original image untouched.

## 2026-07-04 - Stage 10 UI Image Experience

Human raised:

- The Create screen had redundant UI copy near the top.
- The image description area felt visually unbalanced, with vertical style choices leaving awkward empty space.
- The “使用示例图片” action did not actually show a picture.
- Image generation was discussed, but the current stage should not make it part of the main product line.

Codex generated or modified:

- Create screen copy was shortened to a direct action prompt while keeping Mock / Real mode notices.
- Image description controls were grouped into `描述风格`、`图片来源`、`图片基础处理` modules.
- The three image description styles were changed to a horizontal selector.
- A small local sample image drawable is rendered into cache so “使用示例图片” shows a real preview and can flow through image description and processing.
- `恢复原图` clears only the processed image Uri and keeps the selected original image available.
- The result card gained a low-risk `复制文本` action using the Android clipboard.

Human verification points:

- Confirm the Create screen feels lighter and still shows privacy/mode notices.
- Confirm “使用示例图片” displays the built-in image preview.
- Confirm rotate/watermark switch the status to processed image, and `恢复原图` switches it back.
- Re-test Mock mode, Real mode, image compression notice, history delete, favorites, encrypted history, and sharing.

AI risk notes:

- The built-in sample image is intentionally simple and local; it is for workflow demonstration, not model-quality evaluation.
- Real-device testing should confirm button wrapping and preview sizing on the target phone.

## 2026-07-04 - Stage 11 UX Information Architecture

Human raised:

- Previous optimization was too point-by-point.
- Redundant copy on the Create page was only one example of a broader information hierarchy issue.
- History and Settings also had engineering explanation placed before the user's main task.
- This stage should first inspect the App as a user, then optimize consistently as a builder.

Codex generated or modified:

- Create page hierarchy so scenario choice, mode state, image description modules, and result actions are clearer.
- Edit page empty state with a next-step prompt before showing editing controls.
- History page hierarchy so `全部 / 收藏夹` and 作品列表 are primary, while encrypted storage is a lightweight safety note.
- Settings page hierarchy with current status, mode, presets, advanced configuration, text model self-test, and concise safety storage copy.
- A lightweight Real mode text connection self-test that reuses existing `RealModelClient` error handling and does not save history.
- README, project decisions, and implementation decisions to keep engineering proof in docs rather than the main App UI.

Human verification points:

- Confirm Create, Edit, History, and Settings now read as one consistent App rather than separate feature proofs.
- Confirm the text model self-test works with a valid compatible endpoint and returns readable errors with invalid key, endpoint, or network conditions.
- Confirm self-test does not create a history item and does not expose API Key, request body, or response body in UI or logs.
- Re-test Mock / Real generation, presets, image description styles, example image, restore original, history delete, favorites, encrypted history, rotation, watermark, and sharing.

AI risk notes:

- UI hierarchy still needs real-device review for button wrapping and scroll balance.
- Compatible providers may return different text-model errors, so self-test messaging depends on the existing conservative error mapping.

## 2026-07-04 - Stage 12 Content Richness

Human raised:

- Users may not only be dissatisfied with existing UI; they may need missing creative features.
- 朋友圈文案 lacked a clear style direction selector.
- Users may know what happened but not know which expression direction fits.
- Current text creation choices felt thin and needed moderate enrichment.
- GitHub Raw single-line presentation is not a core blocker for this stage unless touched files can naturally keep normal line breaks.

Codex generated or modified:

- Scene-specific text style options for 朋友圈文案 and 商品文案.
- Mock and Real generation support for selected text style and optional 3-version output.
- “帮我推荐风格” with Mock keyword rules and Real text-model prompting.
- Edit page rewrite actions for 更简短、更温柔、更高级、更口语、提炼标题.
- README and decision docs explaining the content-richness scope and continued exclusion of image generation.

Human verification points:

- Confirm each text scene only shows its own styles and defaults to 温馨日常 or 种草推荐.
- Test style recommendation with empty input, Mock mode keywords, and a Real text model configuration.
- Confirm 3-version output is clearly separated and can still be edited, copied, shared, favorited, and saved to encrypted history.
- Confirm Edit page rewrite updates only the edit textbox until the user taps save.
- Re-test image description, sample image, restore original, rotation, watermark, sharing, history delete, favorites, encrypted history, and Mock / Real mode.

AI risk notes:

- Real model style recommendation and rewrite quality depends on the configured endpoint and model behavior.
- Multi-version output is prompt-guided, so some providers may vary the exact formatting.
- The stage intentionally avoids image generation, image editing expansion, account features, cloud sync, and model benchmarking.

## 2026-07-04 - Stage 13 Image Generation And Work Management

Human raised:

- Frontend experience checks are completed by the user, not delegated to Codex.
- `清空历史` at the bottom is unreasonable when there are many history works.
- Edit-page rewrite can overwrite the original text and needs a prompt or restore ability.
- The current phase should moderately add functionality.
- The user has prepared an image generation interface and plans to connect `qwen-image-2.0-pro`.
- This phase should only build minimal text-to-image, not a complex image workstation.
- Project direction and next-stage planning are handled by the user and ChatGPT; Codex should not output project suggestions.

Codex generated or modified:

- A separate `图片生成` creation scene with prompt input, image style selection, aspect ratio selection, generate status, preview, share, favorite, history save, and regenerate.
- Mock and Real image generation clients, keeping image-generation API requests out of UI code.
- History metadata and UI support for both text works and image works.
- Generated-image file storage in app-private `files/generated_images/` with FileProvider sharing.
- Top-position clear-history action and lightweight storage status text.
- Edit-page rewrite restore using `previousEditText` kept only in the current edit session.
- README and decision notes documenting that history metadata is encrypted while generated image files are app-private but not claimed as encrypted binary data.

Human verification points:

- Test Mock image generation with prompt, style, ratio, share, favorite, history, favorites filter, and delete.
- Test Real image generation with `qwen-image-2.0-pro`, including share, favorite, history, and delete.
- Confirm text generation, style recommendation, multi-version generation, edit rewrite, image description, sample image, rotation, watermark, restore original, and sharing still work.
- Confirm clear history is easy to find near the top and still requires confirmation.
- Confirm rewrite failure does not overwrite the current text and successful rewrite can restore the previous text before saving.

AI risk notes:

- Compatible image generation providers may vary response formats, so the current client supports common `data[0].b64_json` and `data[0].url` shapes.
- Generated image binaries are app-private files, not encrypted blobs; documentation must remain honest about that boundary.
- Real-device testing is still required for visual layout, image preview, share sheet behavior, and provider-specific image generation behavior.

## 2026-07-04 - Stage 13.1 Qwen-Image Official API Adaptation

Human raised:

- Mock image generation and the image history chain are basically complete.
- Real image generation failed in testing.
- The failure looked like the service provider interface did not match `/images/generations`.
- This stage should focus only on Qwen-Image official image generation API adaptation.
- Frontend experience judgment remains with the user; Codex handles implementation and objective reporting.

Codex generated or modified:

- Profile-level image generation API type with `Qwen-Image 官方接口` and `OpenAI-compatible 图片接口`.
- Profile-level image generation endpoint field for full Qwen-Image text-to-image service addresses.
- Real image generation request bodies for Qwen-Image official synchronous HTTP and OpenAI-compatible images.
- Response parsing for `data[0].b64_json`, `data[0].url`, `output.choices[0].message.content[].image`, and `output.results[0].url`.

Human verification points:

- Select Qwen-Image official interface, fill the full image generation endpoint, save settings and key, then test Real image generation.
- Confirm generated remote URLs are downloaded into local app-private files before preview, history, sharing, favorite, and delete.
- Re-test Mock image generation, text generation, style recommendation, multi-version generation, edit rewrite and restore, image description, and history.

## 2026-07-04 - Stage 14 V1 Polish

Human raised:

- The project is entering the last V1 polish pass before final acceptance materials.
- App naming should become more product-like, temporarily “灵感工坊”.
- The icon should feel warm and creative.
- The main Create screen may receive one last structural adjustment.
- History needs “再次使用 / 再次生成” entry points.
- The Edit page needs original / rewritten comparison before applying rewrite output.
- Image handling should add center crop and black-and-white filter.
- Freehand drawing is deferred.
- User-visible Mock wording should be weakened to “演示模式”.

Codex generated or modified:

- App label and adaptive icon resources.
- Create screen grouping, image result actions, image processing groups, black-and-white filter, and center crop controls.
- Edit screen rewrite candidate comparison and apply / keep-original actions.
- History screen text reuse and image regeneration entries without automatic model calls.
- Settings screen wording and advanced-configuration folding.
- README and decision notes for the V1 polish scope.

Human verification points:

- Confirm launcher name and icon on a real device.
- Manually test Create, Edit, History, image processing, image generation, and Settings paths listed in the Stage 14 checklist.
- Review the final report and `git diff --stat` before deciding whether to commit.
