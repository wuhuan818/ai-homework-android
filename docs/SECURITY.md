# Security Notes

## Protected Data

- Text history, edited content, original inputs, favorite state, and image history metadata.
- API Keys entered in the app settings screen.
- New generated image binaries created after Stage 14-5.

## Not Protected

- Content the user actively shares through Android share targets.
- Images the user actively saves into the system gallery.
- Runtime screenshots, screen recordings, clipboard contents, memory inspection, root access, malicious OS behavior, or compromised devices.

## Key Management

- The app uses Android Keystore to generate non-exportable AES keys.
- Encryption uses AES-GCM with randomized IVs and 128-bit authentication tags.
- New Keystore AES keys are explicitly requested as 256-bit keys.
- The app does not print plaintext, ciphertext, IVs, key aliases, image paths, prompts, request bodies, response bodies, image Base64, or API Keys.

## History Encryption

- History remains a single encrypted JSON blob in SharedPreferences.
- SharedPreferences stores ciphertext, IV, update metadata, and payload version, not plaintext generated content.
- Legacy history payloads that only contain ciphertext and IV remain readable through the compatibility path.

## API Key Encryption

- API Keys are encrypted with Android Keystore backed AES-GCM under the existing API-key alias.
- The UI shows only configured/unconfigured state and does not display the full saved key.
- Legacy API-key payloads without a payload version remain readable.

## Generated Image Encryption

- New generated images are saved as encrypted `.imgenc` files under `files/generated_images_encrypted/`.
- Image history stores the encrypted file name, not an external path, remote URL, or Base64 body.
- Preview, share, and gallery export decrypt only temporary copies under `cache/decrypted_generated_images/`.
- Legacy plaintext image files under `files/generated_images/` are read when present so old history records remain compatible.

## Backup Policy

- Android automatic backup is disabled with `android:allowBackup="false"`.
- This avoids restoring encrypted SharedPreferences or encrypted image files onto a device that does not have the original Keystore keys.

## Verification

1. Generate text containing `LOCAL-VERIFY-2026`.
2. Favorite it and restart the app.
3. Confirm History can still read the item.
4. Inspect debug SharedPreferences with Android Studio Device Explorer or `adb run-as`.
5. Confirm `LOCAL-VERIFY-2026` does not appear as plaintext in local app files.
6. Generate an image and confirm `files/generated_images_encrypted/` contains an encrypted `.imgenc` file.
7. Confirm the encrypted file cannot be opened directly as an image.
8. Save the image to the gallery and confirm the gallery copy is readable because it was explicitly exported by the user.

## Boundaries

- This design reduces exposure from ordinary local file inspection and accidental app-private file backup.
- It does not claim absolute security or resistance to root, malicious system images, runtime capture, memory inspection, or user-exported copies.
