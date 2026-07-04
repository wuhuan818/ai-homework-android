package com.aihomework.aicontentcreator

import android.content.ClipData
import android.content.ClipboardManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.aihomework.aicontentcreator.data.ai.MockModelClient
import com.aihomework.aicontentcreator.data.ai.ModelClientException
import com.aihomework.aicontentcreator.data.ai.RealModelClient
import com.aihomework.aicontentcreator.data.history.HistoryStorageStatus
import com.aihomework.aicontentcreator.data.image.ImageProcessor
import com.aihomework.aicontentcreator.data.model.CreationRequest
import com.aihomework.aicontentcreator.data.model.CreationResult
import com.aihomework.aicontentcreator.data.model.HistoryItem
import com.aihomework.aicontentcreator.data.repository.CreationRepository
import com.aihomework.aicontentcreator.data.repository.HistoryRepository
import com.aihomework.aicontentcreator.data.settings.ModelMode
import com.aihomework.aicontentcreator.data.settings.SettingsRepository
import com.aihomework.aicontentcreator.ui.CreateScreen
import com.aihomework.aicontentcreator.ui.EditScreen
import com.aihomework.aicontentcreator.ui.HistoryScreen
import com.aihomework.aicontentcreator.ui.SettingsScreen
import com.aihomework.aicontentcreator.ui.shareImage
import com.aihomework.aicontentcreator.ui.shareText
import com.aihomework.aicontentcreator.ui.state.CreateUiState
import com.aihomework.aicontentcreator.ui.state.EditUiState
import com.aihomework.aicontentcreator.ui.state.HistoryUiState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AIContentCreatorApp()
        }
    }
}

private enum class AppTab(val title: String, val shortTitle: String) {
    Create("创作", "创"),
    Edit("编辑", "编"),
    History("历史", "史"),
    Settings("设置", "设")
}

@Composable
private fun AIContentCreatorApp() {
    val context = LocalContext.current
    val appContext = context.applicationContext
    val scope = rememberCoroutineScope()
    val settingsRepository = remember { SettingsRepository(appContext) }
    val historyRepository = remember { HistoryRepository(appContext) }
    val imageProcessor = remember { ImageProcessor(appContext) }
    val historyItems by historyRepository.items.collectAsState()
    val historyStorageStatus by historyRepository.status.collectAsState()

    var selectedTab by remember { mutableStateOf(AppTab.Create) }
    var createState by remember { mutableStateOf(CreateUiState()) }
    var editState by remember { mutableStateOf(EditUiState()) }
    var settings by remember { mutableStateOf(settingsRepository.loadSettings()) }
    var apiKeyInput by remember { mutableStateOf("") }
    var settingsMessage by remember { mutableStateOf<String?>(null) }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri == null) {
            createState = createState.copy(message = "已取消选择图片。")
        } else {
            createState = createState.copy(
                selectedImageUri = uri.toString(),
                processedImageUri = null,
                imageProcessingMessage = "已选择图片，可继续旋转或添加文字水印。",
                imageUploadNotice = null,
                input = createState.input.ifBlank { "已选择图片" },
                message = "图片已选择。"
            )
        }
    }

    MaterialTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            Scaffold(
                bottomBar = {
                    NavigationBar {
                        AppTab.entries.forEach { tab ->
                            NavigationBarItem(
                                selected = selectedTab == tab,
                                onClick = { selectedTab = tab },
                                label = { Text(tab.title) },
                                icon = { Text(tab.shortTitle) }
                            )
                        }
                    }
                }
            ) { innerPadding ->
                Box(modifier = Modifier.padding(innerPadding)) {
                    when (selectedTab) {
                        AppTab.Create -> CreateScreen(
                            state = createState,
                            modelMode = settings.mode,
                            hasApiKey = settings.hasApiKey,
                            onScenarioSelected = { scenario ->
                                createState = createState.copy(
                                    selectedScenario = scenario,
                                    input = "",
                                    selectedImageUri = null,
                                    processedImageUri = null,
                                    imageProcessingMessage = null,
                                    imageUploadNotice = null,
                                    result = null,
                                    message = null
                                )
                            },
                            onImageDescriptionStyleSelected = { style ->
                                createState = createState.copy(imageDescriptionStyle = style)
                            },
                            onInputChanged = { createState = createState.copy(input = it) },
                            onUseMockImage = {
                                scope.launch {
                                    createState = createState.copy(
                                        isImageProcessing = true,
                                        imageProcessingMessage = "正在加载示例图片...",
                                        imageUploadNotice = null
                                    )
                                    val result = withContext(Dispatchers.Default) {
                                        imageProcessor.createSampleImage()
                                    }
                                    createState = if (result.uri != null) {
                                        createState.copy(
                                            input = createState.input.ifBlank {
                                                "示例图片：夜晚城市街道和明亮招牌"
                                            },
                                            selectedImageUri = result.uri.toString(),
                                            processedImageUri = null,
                                            isImageProcessing = false,
                                            imageProcessingMessage = "已使用内置示例图片，可用于演示图片描述。",
                                            imageUploadNotice = null,
                                            message = "示例图片已加载。"
                                        )
                                    } else {
                                        createState.copy(
                                            isImageProcessing = false,
                                            imageProcessingMessage = result.errorMessage,
                                            imageUploadNotice = null,
                                            message = result.errorMessage
                                        )
                                    }
                                }
                            },
                            onChooseImage = {
                                imagePickerLauncher.launch(
                                    PickVisualMediaRequest(
                                        ActivityResultContracts.PickVisualMedia.ImageOnly
                                    )
                                )
                            },
                            onRotateImage = {
                                val sourceUri = createState.processedImageUri ?: createState.selectedImageUri
                                if (sourceUri == null) {
                                    createState = createState.copy(message = "请先选择图片。")
                                    return@CreateScreen
                                }
                                scope.launch {
                                    createState = createState.copy(
                                        isImageProcessing = true,
                                        imageProcessingMessage = "正在旋转图片...",
                                        imageUploadNotice = null
                                    )
                                    val result = withContext(Dispatchers.Default) {
                                        imageProcessor.rotateImage(sourceUri, 90f)
                                    }
                                    createState = if (result.uri != null) {
                                        createState.copy(
                                            processedImageUri = result.uri.toString(),
                                            isImageProcessing = false,
                                            imageProcessingMessage = "图片已旋转 90°。",
                                            imageUploadNotice = null
                                        )
                                    } else {
                                        createState.copy(
                                            isImageProcessing = false,
                                            imageProcessingMessage = result.errorMessage,
                                            imageUploadNotice = null,
                                            message = result.errorMessage
                                        )
                                    }
                                }
                            },
                            onWatermarkTextChanged = {
                                createState = createState.copy(watermarkText = it)
                            },
                            onAddWatermark = {
                                val sourceUri = createState.processedImageUri ?: createState.selectedImageUri
                                if (sourceUri == null) {
                                    createState = createState.copy(message = "请先选择图片。")
                                    return@CreateScreen
                                }
                                scope.launch {
                                    createState = createState.copy(
                                        isImageProcessing = true,
                                        imageProcessingMessage = "正在添加文字水印...",
                                        imageUploadNotice = null
                                    )
                                    val result = withContext(Dispatchers.Default) {
                                        imageProcessor.addTextWatermark(
                                            sourceUri,
                                            createState.watermarkText
                                        )
                                    }
                                    createState = if (result.uri != null) {
                                        createState.copy(
                                            processedImageUri = result.uri.toString(),
                                            isImageProcessing = false,
                                            imageProcessingMessage = "文字水印已添加。",
                                            imageUploadNotice = null
                                        )
                                    } else {
                                        createState.copy(
                                            isImageProcessing = false,
                                            imageProcessingMessage = result.errorMessage,
                                            imageUploadNotice = null,
                                            message = result.errorMessage
                                        )
                                    }
                                }
                            },
                            onRestoreOriginalImage = {
                                createState = createState.copy(
                                    processedImageUri = null,
                                    imageProcessingMessage = "已恢复为原图。",
                                    imageUploadNotice = null,
                                    message = "已恢复为原图。"
                                )
                            },
                            onShareProcessedImage = {
                                val processedImageUri = createState.processedImageUri
                                if (processedImageUri == null) {
                                    createState = createState.copy(message = "请先旋转图片或添加水印。")
                                } else {
                                    shareImage(context, processedImageUri)
                                }
                            },
                            onGenerate = {
                                val input = createState.input.trim()
                                val imageUri = createState.processedImageUri ?: createState.selectedImageUri
                                val hasImage = imageUri != null
                                if (input.isBlank() && !hasImage) {
                                    createState = createState.copy(message = "请先输入内容或选择图片。")
                                    return@CreateScreen
                                }
                                scope.launch {
                                    createState = createState.copy(
                                        isLoading = true,
                                        message = null,
                                        imageUploadNotice = null
                                    )
                                    try {
                                        val client = if (settings.mode == ModelMode.Mock) {
                                            MockModelClient()
                                        } else {
                                            RealModelClient(appContext, settings) {
                                                settingsRepository.getApiKey(settings.activeProfile.id)
                                            }
                                        }
                                        val result = CreationRepository(client).generate(
                                            CreationRequest(
                                                scenario = createState.selectedScenario,
                                                input = input,
                                                imageLabel = if (hasImage) "已选择图片" else null,
                                                imageUri = imageUri,
                                                imageDescriptionStyle = createState.imageDescriptionStyle
                                            )
                                        )
                                        historyRepository.addResult(result)
                                        createState = createState.copy(
                                            isLoading = false,
                                            result = result,
                                            imageUploadNotice = result.warningMessage
                                        )
                                        editState = result.toEditState()
                                    } catch (error: ModelClientException) {
                                        createState = createState.copy(
                                            isLoading = false,
                                            message = error.userMessage,
                                            imageUploadNotice = error.userMessage
                                        )
                                    } catch (error: Exception) {
                                        createState = createState.copy(
                                            isLoading = false,
                                            message = "生成失败，请稍后重试。"
                                        )
                                    }
                                }
                            },
                            onEdit = {
                                val result = createState.result
                                if (result == null) {
                                    createState = createState.copy(message = "暂无可编辑内容。")
                                } else {
                                    editState = result.toEditState()
                                    selectedTab = AppTab.Edit
                                }
                            },
                            onFavorite = {
                                val result = createState.result
                                if (result == null) {
                                    createState = createState.copy(message = "暂无可收藏内容。")
                                } else {
                                    historyRepository.toggleFavorite(result.id)
                                    createState = createState.copy(message = "收藏状态已更新。")
                                }
                            },
                            onShare = {
                                val text = createState.result?.content.orEmpty()
                                if (text.isBlank()) {
                                    createState = createState.copy(message = "暂无可分享内容。")
                                } else {
                                    shareText(context, text)
                                }
                            },
                            onCopyResult = {
                                val text = createState.result?.content.orEmpty()
                                if (text.isBlank()) {
                                    createState = createState.copy(message = "暂无可复制内容。")
                                } else {
                                    val clipboard = context.getSystemService(ClipboardManager::class.java)
                                    clipboard.setPrimaryClip(
                                        ClipData.newPlainText("创作结果", text)
                                    )
                                    createState = createState.copy(message = "已复制到剪贴板。")
                                }
                            },
                            onMessageShown = {
                                createState = createState.copy(message = null)
                            }
                        )

                        AppTab.Edit -> EditScreen(
                            state = editState,
                            onTextChanged = { editState = editState.copy(text = it) },
                            onSave = {
                                val id = editState.itemId
                                if (id == null || editState.text.isBlank()) {
                                    editState = editState.copy(message = "暂无可保存内容。")
                                } else {
                                    historyRepository.updateContent(id, editState.text)
                                    createState = createState.updateResultText(id, editState.text)
                                    editState = editState.copy(message = "修改已保存。")
                                    selectedTab = AppTab.Create
                                }
                            },
                            onConvertMarkdown = {
                                if (editState.text.isBlank()) {
                                    editState = editState.copy(message = "暂无可转换内容。")
                                } else {
                                    editState = editState.copy(
                                        text = toMarkdown(editState),
                                        message = "已转换为 Markdown。"
                                    )
                                }
                            },
                            onConvertPlainText = {
                                if (editState.text.isBlank()) {
                                    editState = editState.copy(message = "暂无可转换内容。")
                                } else {
                                    editState = editState.copy(
                                        text = toPlainText(editState.text),
                                        message = "已转换为纯文本。"
                                    )
                                }
                            },
                            onShare = {
                                if (editState.text.isBlank()) {
                                    Toast.makeText(context, "暂无可分享内容。", Toast.LENGTH_SHORT).show()
                                } else {
                                    shareText(context, editState.text)
                                }
                            },
                            onMessageShown = {
                                editState = editState.copy(message = null)
                            }
                        )

                        AppTab.History -> HistoryScreen(
                            state = HistoryUiState(
                                items = historyItems,
                                storageStatus = historyStorageStatus.toDisplayText()
                            ),
                            onOpenForEdit = { item: HistoryItem ->
                                editState = item.toEditState()
                                selectedTab = AppTab.Edit
                            },
                            onToggleFavorite = { id ->
                                historyRepository.toggleFavorite(id)
                            },
                            onDeleteItem = { id ->
                                historyRepository.deleteItem(id.toString())
                            },
                            onClearHistory = {
                                historyRepository.clearHistory()
                            }
                        )

                        AppTab.Settings -> SettingsScreen(
                            settings = settings,
                            apiKeyInput = apiKeyInput,
                            historyStorageStatus = historyStorageStatus.toDisplayText(),
                            message = settingsMessage,
                            onModeChanged = { mode ->
                                val updated = settings.copy(mode = mode)
                                settings = updated
                                settingsRepository.saveSettings(updated)
                            },
                            onActiveProfileChanged = { profileId ->
                                val updated = settings.copy(activeProfileId = profileId)
                                settings = updated
                                apiKeyInput = ""
                                settingsRepository.saveSettings(updated)
                                val activeProfile = updated.activeProfile
                                val keyStatus = if (activeProfile.hasApiKey) "已配置" else "未配置"
                                settingsMessage = "已切换到配置：${activeProfile.name.ifBlank { "未命名配置" }}。密钥状态：$keyStatus。"
                            },
                            onProfileNameChanged = { name ->
                                settings = settings.updateActiveProfile { it.copy(name = name) }
                            },
                            onBaseUrlChanged = { baseUrl ->
                                settings = settings.updateActiveProfile { it.copy(baseUrl = baseUrl) }
                            },
                            onTextModelChanged = { textModel ->
                                settings = settings.updateActiveProfile { it.copy(textModel = textModel) }
                            },
                            onVisionModelChanged = { visionModel ->
                                settings = settings.updateActiveProfile { it.copy(visionModel = visionModel) }
                            },
                            onApiKeyInputChanged = { apiKeyInput = it },
                            onSaveSettings = {
                                settingsRepository.saveSettings(settings)
                                settings = settingsRepository.loadSettings()
                                settingsMessage = "设置已保存。"
                            },
                            onSaveApiKey = {
                                settingsRepository.saveSettings(settings)
                                if (settingsRepository.saveApiKey(apiKeyInput, settings.activeProfile.id)) {
                                    apiKeyInput = ""
                                    settings = settingsRepository.loadSettings()
                                    settingsMessage = "模型密钥已按当前配置预设加密保存。"
                                } else {
                                    settings = settingsRepository.loadSettings()
                                    settingsMessage = "请输入模型密钥后再保存。"
                                }
                            },
                            onClearApiKey = {
                                settingsRepository.clearApiKey(settings.activeProfile.id)
                                apiKeyInput = ""
                                settings = settingsRepository.loadSettings()
                                settingsMessage = "模型密钥已清除。"
                            },
                            onMessageShown = {
                                settingsMessage = null
                            }
                        )
                    }
                }
            }
        }
    }
}

private fun CreationResult.toEditState(): EditUiState {
    return EditUiState(
        itemId = id,
        scenario = scenario,
        text = content
    )
}

private fun HistoryItem.toEditState(): EditUiState {
    return EditUiState(
        itemId = id,
        scenario = scenario,
        text = content
    )
}

private fun CreateUiState.updateResultText(id: Long, text: String): CreateUiState {
    val current = result ?: return this
    if (current.id != id) return this
    return copy(result = current.copy(content = text))
}

private fun toMarkdown(state: EditUiState): String {
    val title = state.scenario?.displayName ?: "创作结果"
    val body = state.text
        .lines()
        .map { it.trim() }
        .filter { it.isNotEmpty() }
        .joinToString(separator = "\n\n") { "- $it" }
    return "# $title\n\n$body"
}

private fun toPlainText(text: String): String {
    return text
        .replace(Regex("^#+\\s*", RegexOption.MULTILINE), "")
        .replace(Regex("^[-*]\\s*", RegexOption.MULTILINE), "")
        .replace("**", "")
        .replace("__", "")
        .trim()
}

private fun HistoryStorageStatus.toDisplayText(): String {
    return when (this) {
        HistoryStorageStatus.NotInitialized -> "历史记录：尚未初始化"
        HistoryStorageStatus.Encrypted -> "历史记录：已本地加密保存"
        HistoryStorageStatus.LoadFailed -> "历史记录：读取失败"
        HistoryStorageStatus.SaveFailed -> "历史记录：保存失败"
    }
}
