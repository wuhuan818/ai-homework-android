# Validation Evidence

## Build Evidence

- Build command: `.\gradlew.bat assembleDebug`
- Build result: BUILD SUCCESSFUL
- APK source path: `D:\AIHomework\AIContentCreator\app\build\outputs\apk\debug\app-debug.apk`
- APK submission path: `天津--灵感工坊--姜良振--APK-debug.apk`
- APK size: 16,056,638 bytes, about 15.31 MB
- APK SHA-256: `B38E4FE99B30913C2D942BB7D3AD7B7D132C5DFE7B29A8BE37DFC456C372CE0A`
- Current commit: see `git rev-parse HEAD` and `evidence/logs/git_info.txt` from the final packaging run.
- Current branch: `stage15-final-submission`

## Device Install Evidence

- Install command: `adb install -r app\build\outputs\apk\debug\app-debug.apk`
- Device ID: `10AC8M0C61001B1`
- Android version: `14`
- Install result: this attempt returned `INSTALL_FAILED_ABORTED: User rejected permissions`, meaning the device-side install confirmation was rejected. The package does not fake a successful install; a successful install log should be added after the user approves installation on the device.

## Functional Validation Path

1. Text generation: open Create, choose Moments copy or Product description, enter content, and generate.
2. Moments copy: choose tones, use style recommendation, and enable 3-version generation.
3. Product description: enter product facts and generate structured product copy.
4. Image description: select a local image or sample image and choose objective, social, or product-copy style.
5. Image generation: enter a prompt, choose style and aspect ratio, generate and preview an image.
6. Editing: open Edit, generate a rewrite candidate, compare original and rewritten text, then apply if desired.
7. Favorites: toggle favorite state from the result card or history list.
8. Sharing: share text through system text share and images through system image share.
9. Gallery save: save generated images to the system gallery.
10. Reuse/regenerate: reuse text history and restore image-generation settings from image history.
11. Image processing: verify rotate, watermark, black-and-white filter, center crop, gesture box crop, restore original, and share processed image.

## Local Encryption Validation

Use `LOCAL-VERIFY-2026` as a validation keyword. Generate demo content containing the keyword, favorite or save it, restart the app, and confirm History still loads. Then use Android Studio Device Explorer or `adb run-as` to inspect `shared_prefs` and confirm the keyword is not present as plaintext. After generating an image, verify that new files under `files/generated_images_encrypted/` are `.imgenc` files and cannot be opened directly as images. Gallery-saved images are user-initiated plaintext exports and are outside app-private encrypted storage.

## Error Scenarios

- Missing API key: Real mode prompts the user to configure a key in Settings.
- Network or API error: auth, rate limit, service, and response-format failures are shown as readable errors.
- Image load failure or unavailable legacy image: the app prompts the user to choose the image again or explains that the image is unavailable.
- Image generation failure: the app asks the user to check the image-generation endpoint/model or retry later.

## Performance Checks

- History image thumbnails are lazy loaded and decrypted/decoded off the main UI path.
- Create page generated-image previews load asynchronously, so actions remain available while preview loading completes.

## Evidence Index

- `evidence/logs/build_log.txt`
- `evidence/logs/install_log.txt`
- `evidence/logs/apk_info.txt`
- `evidence/logs/git_info.txt`
- `evidence/logs/version_info.txt`
- `evidence/screenshots/`: no user-provided screenshots found; pending user supplement.
- `evidence/videos/`: no user-provided recording found; pending user supplement.
