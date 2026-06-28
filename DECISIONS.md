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

