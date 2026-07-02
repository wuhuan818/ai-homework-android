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

## Stage 4 Encrypted History Evidence

Capture these screenshots, recordings, or terminal snippets:

1. Generate one Mock mode result with a recognizable phrase such as `LOCAL-VERIFY-2026`.
2. Favorite the result or edit and save it.
3. Close and reopen the app, then confirm the History page still shows the item.
4. Capture the History page showing the storage status block.
5. Capture the Settings page showing API Key status and history encryption status.
6. Inspect the app's debug SharedPreferences file and confirm the recognizable phrase is not readable as plaintext.
7. Save the successful `assembleDebug` output and, if a device is connected, the successful `adb install` output.

Suggested commands:

```powershell
adb devices
adb shell run-as com.aihomework.aicontentcreator ls shared_prefs
adb shell run-as com.aihomework.aicontentcreator cat shared_prefs/ai_content_creator_history.xml
adb shell run-as com.aihomework.aicontentcreator cat shared_prefs/ai_content_creator_history.xml | findstr LOCAL-VERIFY-2026
```

Expected result:

- The history preference file contains fields such as `history_cipher_text`, `history_iv`, `version`, and `updated_at`.
- The generated content phrase does not appear directly in the XML output.
- If `findstr LOCAL-VERIFY-2026` returns no matching line, record that as evidence that the exported preference file does not expose the plaintext test phrase.

If `run-as` is not available for the installed build, use Android Studio Device Explorer:

1. Select the connected device.
2. Open `data/data/com.aihomework.aicontentcreator/shared_prefs`.
3. Export or open `ai_content_creator_history.xml`.
4. Search for the recognizable test phrase and record that it is not visible as plaintext.

## Stage 5 Basic Image Processing Evidence

Capture these screenshots or short recordings:

1. Image description scenario after a device image is selected successfully.
2. Image preview/status before rotation and after tapping `Rotate 90`.
3. Text watermark input plus the processed image/status after tapping `Add watermark`.
4. Android system share sheet opened from `Share image` for the processed image.
5. User-readable failure prompt when tapping rotate or watermark before selecting an image, expected text: `请先选择图片`.
6. Successful `assembleDebug` output.
7. Successful `adb install -r app\build\outputs\apk\debug\app-debug.apk` output on a real device.

Notes:

- The processed image is saved under the app cache for sharing evidence and is not expected to appear in the gallery.
- No broad storage permission should appear in the app permission list.
