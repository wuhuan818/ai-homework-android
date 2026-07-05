# 安全与隐私说明

## 保护对象

应用重点保护 API Key、文本历史、图片历史元数据和新生成图片文件。这些内容属于用户在本地创作和配置过程中产生的敏感数据。

## 密钥管理

应用使用 Android Keystore 生成不可导出的 AES 256 bit 密钥，并使用 AES-GCM 加密。每次加密使用随机 IV，payload 带版本字段，便于后续识别和兼容迁移。GCM 认证标签用于检测密文被篡改的情况。

## 存储策略

API Key 和历史数据保存在 SharedPreferences 中，但只保存密文、IV 和版本，不保存明文内容。新生成图片文件保存到 App 私有目录 `generated_images_encrypted`，文件内容为加密后的 `.imgenc`。`decrypted_generated_images` 只作为临时 cache，用于预览、分享或保存相册时生成短期明文副本。

## 导出边界

分享图片或保存到相册时，应用会临时解密图片。系统相册中的图片是用户主动导出后的明文副本，不再属于 App 私有加密存储范围。文本分享同样属于用户主动传播行为。

## 备份策略

Manifest 中设置 `allowBackup=false`，避免系统把加密 SharedPreferences 或加密图片文件备份并恢复到没有原 Keystore 密钥的设备上。

## 不保护对象

本方案不声称保护 root 设备、恶意系统、运行时截屏、内存读取、调试器读取，或用户主动分享后的外部传播。

## 验证方法

可使用 `LOCAL-VERIFY-2026` 作为关键词生成历史，再通过 Android Studio Device Explorer 或 `adb run-as` 检查 `shared_prefs` 中无该明文。生成图片后检查 `generated_images_encrypted` 下文件不可直接作为图片打开。

## 安全声明边界

本方案用于降低普通本地文件查看风险，不声明绝对安全。
