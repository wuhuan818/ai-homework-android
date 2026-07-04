# Project Decisions

## Git And Collaboration

- Keep the `main` branch stable.
- Use a separate branch for each feature or focused task.
- Do not do unrelated refactors.
- After each modification, output `git diff --stat`.
- Prioritize the minimum usable version over broad architecture work.

## Delivery Principles

- Make the app buildable after each meaningful change.
- Prefer small, reviewable changes.
- Keep business logic easy to replace later.
- Defer complex features until the real-device demo is stable.

## Stage 3 API And Key Handling

- Keep `MockModelClient` available so the original demo flow remains usable if network, credentials, or provider issues occur.
- Add `RealModelClient` for one OpenAI-compatible Chat Completions configuration instead of building a multi-provider or multi-model comparison feature.
- API Key entry happens only inside the Android Settings screen.
- Do not hardcode, log, document, or commit real API keys.
- Store the API Key locally as Android Keystore encrypted AES-GCM ciphertext plus IV in SharedPreferences.

## Stage 4 Encrypted History

- Persist history and favorite state locally after generate, edit, favorite, and clear operations.
- Use Android Keystore backed AES-GCM and SharedPreferences ciphertext/IV fields for the stage 4 encrypted storage requirement.
- Keep the implementation as an encrypted JSON blob instead of adding Room or SQLCipher in this stage.
- Defer database-backed history until the app needs search, paging, migrations, or larger structured history data.

## Stage 6 UI And Prompt Polish

- Improve Chinese UI wording and Prompt quality before adding more features because the app is now functionally usable and needs to be clear for contest demonstration.
- Keep demo mode and real model mode visibly distinct so local template output is not mistaken for a real model response.
- Do not add configuration presets or multi-model comparison in this stage because they would expand scope beyond polishing the existing demo-ready flow.

## Stage 7 Profile Presets And Image Description Styles

- Add 3 interface configuration presets for backup endpoint switching, not for multi-model scoring, ranking, or speed comparison.
- Keep each preset's API Key independently encrypted and keep full keys out of plaintext settings objects and UI.
- Add image description styles because they improve demo flexibility while preserving the existing single-request, single-result generation flow.

## Stage 8 Image Upload And History Polish

- Compress only the image copy sent to the vision model so large device photos are less likely to fail during Real mode demos.
- Do not replace the user's original image or processed image cache output; rotation, watermark, and image sharing should keep their existing behavior.
- Keep history management small: add favorites filtering, single-item delete, and clear confirmations instead of categories, albums, cloud sync, Room, or SQLCipher.
- Warn users that compression can reduce detail recognition accuracy.
