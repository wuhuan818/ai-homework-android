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
