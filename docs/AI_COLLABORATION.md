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
