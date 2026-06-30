# Evidence Plan

Evidence to collect for the contest submission:

1. Screenshot of the app launching on a real Android device.
2. Screenshot or recording of switching between Create, Edit, History, and Settings.
3. Build output showing `assembleDebug` success.
4. APK installation output from `adb install`.
5. Later: screenshots for AI generation, edit, favorite/history, and share flows.
6. Later: notes explaining local encrypted storage and exception handling.

## Mock Flow Evidence

Capture these screenshots or short recordings:

1. Three creation scenario entries.
2. `朋友圈文案` mock generation result.
3. `商品描述` mock generation result.
4. `图片描述` mock result.
5. Edit screen with editable text.
6. Markdown conversion result.
7. History/favorite screen.
8. Android system share sheet.
9. Successful real-device installation.

## Stage 3 Evidence

Capture these screenshots or short recordings:

1. Settings page with Mock / Real mode and configurable Base URL / model fields.
2. API Key status showing configured but hidden.
3. Mock mode still generating content.
4. Real mode text generation result.
5. Real mode product copy result.
6. Missing API Key error in Real mode.
7. Network or API error message without stack trace or API Key.
8. Successful system image selection.
9. Image description result, either Real vision output or Mock fallback message.
10. Successful `assembleDebug` output.
11. Successful `adb install -r app\build\outputs\apk\debug\app-debug.apk` output on a real device.
