# Inspiration Workshop README

## Project Info

- Project name: Inspiration Workshop
- Track: Track 2, Android generative content creation tool
- GitHub repository: https://github.com/wuhuan818/ai-homework-android
- Final branch: stage15-final-submission
- APK: `天津--灵感工坊--姜良振--APK-debug.apk`, a debug build for assignment review installation.

## Goals

Inspiration Workshop is an Android app for a complete generative content workflow on a real device. A user enters a creative request, generates content through Demo mode or a configured model service, reviews the result, edits it, saves it to history, marks favorites, shares text or images, and performs basic image processing.

## Non-Goals

This version does not include accounts, cloud sync, app-store release, self-trained models, multi-model ranking, image-to-image, inpainting, drawing, batch image generation, or a professional image editor. Image processing is intentionally limited to basic operations required by the contest.

## Completion Criteria

1. The debug APK can be built and provided for review installation.
2. The app covers at least Moments copy, product description, image description, and image generation.
3. The main chain works: input -> model/demo generation -> result display -> edit -> favorite/share.
4. The app supports rewrite candidates, Markdown/plain text conversion, history, and favorites.
5. API keys, text history, and newly generated image files are protected by local encryption.
6. The submission includes build logs, APK metadata, validation paths, and a screenshot/recording checklist.

## Core Features

- Text creation: Moments copy, product description, style selection, style recommendation, and optional 3-version generation.
- Image description: local image selection or built-in sample image, with objective, social-caption, and product-copy styles.
- Image generation: text-to-image prompt, style, aspect ratio, prompt optimization, and companion-image handoff from text results.
- Text editing: shorter, gentler, premium, conversational, and title-extraction rewrite candidates, applied only after confirmation.
- Format conversion: Markdown and plain text conversion.
- Basic image processing: rotate 90 degrees, text watermark, black-and-white filter, center crop, gesture box crop, restore original, and share processed image.
- Work management: encrypted local history, favorites, reuse, regenerate, system sharing, and saving generated images to the gallery.
- Modes: Demo mode uses local templates; Real mode calls the configured model APIs.
- Settings: Base URL, API Key, Text Model, Vision Model, Image Generation Endpoint, image generation model, and multiple configuration presets.

## Running On A Device

1. Open the repository in Android Studio, or run `.\gradlew.bat assembleDebug` in the project root.
2. Connect an Android device with USB debugging enabled.
3. Run `adb install -r app\build\outputs\apk\debug\app-debug.apk`, or install the debug APK from the submission package.
4. Open Inspiration Workshop. Demo mode can be used without a model key.

## Model Configuration

In Settings, switch to Real mode and fill the active profile fields: Base URL, API Key, Text Model, Vision Model, image generation API type, image generation endpoint, and image generation model. Real keys are entered only on the device and are not committed in code, docs, or logs.

## Demo Mode And Real Mode

Demo mode does not upload user input or images. It uses local mock templates and labels the output as demo-generated content. Real mode calls the configured compatible text, vision, or image-generation service. Network, auth, rate-limit, service, and response-format failures are shown as readable errors instead of being presented as successful generation.

## Demo Scenarios

1. Moments copy: enter an event or feeling, choose a tone, and generate one or three versions.
2. Product description: enter product facts and generate clear, restrained product copy.
3. Image description: import an image or use the sample image, then generate objective, social, or product-oriented descriptions.
4. Image generation: enter an image prompt, choose style and ratio, then preview, save, share, favorite, or regenerate.

## Local Encrypted Storage

The app uses Android Keystore to create a non-exportable AES 256-bit key. API keys, text history, and history metadata are encrypted with AES-GCM, random IVs, and versioned payloads. Newly generated image files are stored under `files/generated_images_encrypted/`. Preview, sharing, and gallery export decrypt temporary cache copies only. Images saved to the system gallery are user-initiated plaintext exports.

## Error Handling

The app handles missing API key, empty Base URL or model name, network failure, auth failure, rate limit, service errors, image read failures, unavailable legacy images, and image generation failures with user-readable prompts.

## Known Boundaries

Local encryption does not claim protection against rooted devices, malicious operating systems, runtime screenshots, memory inspection, or external distribution after the user shares content. History is stored as a local encrypted JSON blob, which is suitable for the contest demo but not intended for large-scale search or cloud collaboration.
