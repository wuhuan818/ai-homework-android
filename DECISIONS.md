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
