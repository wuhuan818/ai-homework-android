# Validation Evidence

## Build Evidence

- Build command: `.\gradlew.bat assembleDebug`
- Build result: BUILD SUCCESSFUL
- APK source path: `D:\AIHomework\AIContentCreator\app\build\outputs\apk\debug\app-debug.apk`
- APK submission path: `天津--灵感工坊--姜良振--APK-debug.apk`
- APK size: 12,598,930 bytes, about 12.02 MB
- APK SHA-256: `55D42137F049AB320BE1D6BA1C63BBEC5EBFE340094D75006EDD42CD3C884AF7`
- Current commit: see `git rev-parse HEAD` and `evidence/logs/git_info.txt` from the final packaging run.
- Current branch: `stage15-final-submission`

## Device Install Evidence

- Install command: `adb install -r app\build\outputs\apk\debug\app-debug.apk`
- Device ID: `10AC8M0C61001B1`
- Android version: `14`
- Install result: `Success`

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

Use `LOCAL-VERIFY-2026` as a validation keyword. Generate demo content containing the keyword, favorite or save it, restart the app, and confirm History still loads. App-readable content is not the same as local plaintext storage; this evidence set uses `adb run-as` to search the app-private `shared_prefs` / `files` scope for the keyword. The log result is `NO_PLAINTEXT_MATCH`, supporting that the private persisted files do not directly expose this user content as plaintext. The generated-image long-term directory `files/generated_images_encrypted/` file-header check is complete; the selected `.imgenc` file's first 16 bytes are not a common PNG/JPEG/WebP plaintext image header. Gallery save or system share is a user-initiated plaintext export and is outside app-private encrypted storage.

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
- `evidence/logs/encryption_verify_log.txt`
- `evidence/logs/encryption_verify_log_numbered.txt`
- `evidence/logs/encrypted_image_file_check.txt`
- `evidence/logs/encrypted_image_file_check_numbered.txt`
- `evidence/logs/encryption_screenshot_targets.txt`
- `evidence/screenshots/`: 13 user-supplied screenshots are present and require final review.
- `evidence/videos/`: 1 user-supplied recording is present and requires final review.
