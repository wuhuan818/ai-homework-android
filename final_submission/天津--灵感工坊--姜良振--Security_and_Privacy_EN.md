# Security And Privacy

## Protected Data

The app protects API keys, text history, image-history metadata, and newly generated image files. These are the main sensitive artifacts created during local usage and configuration.

## Key Management

The app uses Android Keystore to generate a non-exportable AES 256-bit key. Encryption uses AES-GCM with a random IV for each payload. Payloads include a version field for future compatibility and migration. The GCM authentication tag helps detect tampering.

## Storage Strategy

API keys and history are stored in SharedPreferences only as ciphertext, IV, and payload version. Plain generated content and real keys are not stored there. Newly generated image files are written to the app-private `generated_images_encrypted` directory as encrypted `.imgenc` files. `decrypted_generated_images` is a temporary cache used only for preview, sharing, or gallery export.

## Export Boundary

When the user shares an image or saves it to the gallery, the app temporarily decrypts the file. The image in the system gallery is a user-initiated plaintext export and is outside the app-private encrypted storage boundary. Text sharing is also a user-initiated external transfer.

## Backup Policy

The manifest sets `allowBackup=false` to avoid backing up encrypted SharedPreferences or encrypted image files and restoring them onto a device without the matching Keystore key.

## Not Protected

The design does not claim protection against rooted devices, malicious operating systems, runtime screenshots, memory inspection, debugger access, or external distribution after the user shares content.

## Verification

Use `LOCAL-VERIFY-2026` as a test keyword, generate history, then inspect `shared_prefs` with Android Studio Device Explorer or `adb run-as` and confirm the keyword is not visible as plaintext. After generating an image, check that files under `generated_images_encrypted` cannot be opened directly as images.

## Boundary Statement

This scheme reduces ordinary local file-inspection risk. It does not claim absolute security.
