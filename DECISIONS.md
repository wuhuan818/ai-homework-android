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

## Stage 9 Vision Trust Polish

- Real API failures should be shown as real errors instead of silently becoming Mock content.
- Mock fallback is acceptable only for image preparation or image-input capability problems, and must be clearly labeled as demo fallback.
- Vision upload can use a compressed or JPEG-converted copy to improve provider compatibility without overwriting the original image or processed share image.
- Saving an API Key should first save the current active profile fields so endpoint/model edits and key storage stay in sync.

## Stage 10 UI Image Experience

- Reduce project-introduction copy on the Create screen so users can start from the scenario choice instead of rereading app capabilities.
- Keep image work focused on preview, local example image, rotation, watermark, restore original, and sharing; do not add image generation in this stage.
- Add restore original by clearing only the processed image Uri so the selected image path remains stable and cache cleanup does not become part of the current scope.
- Use a small local drawable example image rendered to cache instead of network images, copyrighted photos, user photos, or a larger asset pipeline.

## Stage 11 UX Information Architecture

- Reorder Create, Edit, History, and Settings from a normal user's next action instead of from project evidence needs.
- Keep App screens focused on operation, status, and recovery prompts; move engineering proof and competition explanation to README / docs.
- Keep encrypted-history technology details out of the History page's top visual area so the work list and favorites remain primary.
- Add a lightweight text model connection test for the current active configuration, but do not add model comparison, scoring, speed tests, or rankings.
- Continue to exclude image generation because this stage is about information hierarchy and configuration debugging, not expanding media creation scope.
