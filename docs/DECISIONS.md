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
