# 验证证据

## 构建证据

- 构建命令：`.\gradlew.bat assembleDebug`
- 构建结果：BUILD SUCCESSFUL
- APK 源路径：`D:\AIHomework\AIContentCreator\app\build\outputs\apk\debug\app-debug.apk`
- APK 提交路径：`天津--灵感工坊--姜良振--APK-debug.apk`
- APK 大小：12,598,930 bytes，约 12.02 MB
- APK SHA-256：`55D42137F049AB320BE1D6BA1C63BBEC5EBFE340094D75006EDD42CD3C884AF7`
- 当前 commit：以最终打包时 `git rev-parse HEAD` 和 `evidence/logs/git_info.txt` 记录为准。
- 当前分支：`stage15-final-submission`

## 真机安装证据

- 安装命令：`adb install -r app\build\outputs\apk\debug\app-debug.apk`
- 设备 ID：`10AC8M0C61001B1`
- Android 版本：`14`
- 安装结果：`Success`

## 功能验证路径

1. 文本生成：进入创作页，选择朋友圈文案或商品描述，输入内容并生成。
2. 朋友圈文案：选择不同创作风格，可使用风格推荐和生成 3 个版本。
3. 商品描述：输入商品信息，生成结构化商品文案。
4. 图片描述：选择图片或内置示例图，选择客观描述、社交配文或商品文案。
5. 图片生成：输入图片描述，选择图片风格和画幅比例，生成并预览图片。
6. 编辑：进入编辑页，使用改写候选，对比原文和改写后内容，再确认应用。
7. 收藏：在结果卡片或历史列表中切换收藏状态。
8. 分享：文本走系统文本分享，图片走系统图片分享。
9. 保存相册：对生成图片执行保存到相册。
10. 历史再次使用 / 再次生成：历史文本可再次使用，历史图片生成记录可恢复设置后手动再生成。
11. 图片处理：验证旋转、水印、黑白滤镜、中心裁剪、框选裁剪、恢复原图和分享处理后图片。

## 本地加密验证

推荐使用 `LOCAL-VERIFY-2026` 作为验证关键词：在演示模式生成包含该关键词的内容，收藏或保存后重启 App，确认历史仍可读。App 内可读内容不等于本地明文存储；本次补充使用 `adb run-as` 在 App 私有 `shared_prefs` / `files` 范围搜索该关键词，日志结果为 `NO_PLAINTEXT_MATCH`，支持私有落盘文件不能直接读出该用户内容。生成图片长期目录 `files/generated_images_encrypted/` 的文件头检查已记录；当前设备该目录不存在或为空，待生成图片后补充文件头证据。保存到相册或系统分享属于用户主动导出的明文副本，不再属于 App 私有加密存储边界。

## 异常场景

- 未配置 API Key：真实模式会提示先在设置页配置密钥。
- 网络 / API 报错：鉴权失败、限流、服务异常、接口格式不匹配会显示可读错误。
- 图片加载失败或旧图片不可用：应用提示重新选择图片或说明图片不可用。
- 生成图片失败：应用提示检查图片生成接口、模型或稍后重试。

## 性能验证

- 历史页图片缩略图使用懒加载和后台解密/解码，避免切换到历史页时阻塞。
- 创作页生成图片预览使用异步加载，结果操作不依赖预览同步完成。

## 证据文件索引

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
- `evidence/screenshots/`：用户已手动补充 13 张截图，需最终审阅。
- `evidence/videos/`：用户已手动补充 1 个录屏，需最终审阅。
