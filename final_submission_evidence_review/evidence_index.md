# Stage 15 Evidence Review Index

This directory is a temporary GitHub review branch evidence subset. It is not the final submission package.

Source final package folder: C:\Users\86158\Desktop\天津--灵感工坊--姜良振
Formal branch commit used: 0ed5d6bec58d1189c3307ad58ba88568ee47bf5c

## Scope

- Included here: user-supplied screenshots, local-encryption adb logs, and this index.
- Excluded from this branch: recording video, APK, zip package, local.properties, build outputs, .gradle, app/build, SharedPreferences dumps, key material, and real API keys.
- The recording remains only in the desktop final submission package for manual review.

## Screenshots (15)

- screenshots/10-历史记录.jpg
- screenshots/11-作品分享.jpg
- screenshots/12-作品收藏.jpg
- screenshots/13-异常提示.jpg
- screenshots/14-本地加密验证-明文搜索结果.png
- screenshots/15-本地加密验证-图片文件头.png
- screenshots/1-创作界面.jpg
- screenshots/2-设置界面.jpg
- screenshots/3-空白编辑页.jpg
- screenshots/4-空白历史页.jpg
- screenshots/5-朋友圈文案.jpg
- screenshots/6-商品描述.jpg
- screenshots/7-图片描述.jpg
- screenshots/8-图片生成.jpg
- screenshots/9-作品编辑.jpg

Local-encryption screenshot highlights:

- screenshots/14-本地加密验证-明文搜索结果.png
- screenshots/15-本地加密验证-图片文件头.png

## Logs (5)

- logs/encrypted_image_file_check.txt
- logs/encrypted_image_file_check_numbered.txt
- logs/encryption_screenshot_targets.txt
- logs/encryption_verify_log.txt
- logs/encryption_verify_log_numbered.txt

## Local Encryption Result Summary

- Plaintext storage search: `adb shell run-as com.aihomework.aicontentcreator ... grep -R LOCAL-VERIFY-2026 shared_prefs files ...` returned `NO_PLAINTEXT_MATCH`.
- Interpretation: app-private persisted shared_prefs/files did not directly expose the validation keyword as plaintext.
- Encrypted generated image check: selected `.imgenc` file first 16 bytes were `41 49 43 49 00 00 00 01 00 00 00 0c b9 1b 40 e7`.
- Interpretation: the selected long-term generated image file is not directly readable as a common PNG/JPEG/WebP plaintext image file.

## Manual Review Still Needed

- Visually review all screenshots.
- Review the copied logs and compare them with the desktop final package.
- Confirm the regenerated desktop zip content before formal upload.
