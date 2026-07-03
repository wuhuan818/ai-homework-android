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
