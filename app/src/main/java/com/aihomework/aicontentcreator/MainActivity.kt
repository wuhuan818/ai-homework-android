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
import com.aihomework.aicontentcreator.data.ai.MockImageGenerationClient
import com.aihomework.aicontentcreator.data.ai.ModelClientException
import com.aihomework.aicontentcreator.data.ai.RealImageGenerationClient
import com.aihomework.aicontentcreator.data.ai.RealModelClient
import com.aihomework.aicontentcreator.data.history.HistoryStorageStatus
import com.aihomework.aicontentcreator.data.image.GeneratedImageFileStore
import com.aihomework.aicontentcreator.data.image.ImageProcessor
import com.aihomework.aicontentcreator.data.model.CreationRequest
import com.aihomework.aicontentcreator.data.model.CreationResult
import com.aihomework.aicontentcreator.data.model.CreationScenario
import com.aihomework.aicontentcreator.data.model.HistoryContentType
import com.aihomework.aicontentcreator.data.model.HistoryItem
import com.aihomework.aicontentcreator.data.model.ImageAspectRatio
import com.aihomework.aicontentcreator.data.model.ImageGenerationResult
import com.aihomework.aicontentcreator.data.model.ImageGenerationStyle
import com.aihomework.aicontentcreator.data.model.TextCreationStyle
import com.aihomework.aicontentcreator.data.repository.CreationRepository
import com.aihomework.aicontentcreator.data.repository.HistoryRepository
import com.aihomework.aicontentcreator.data.settings.ImageGenerationApiType
import com.aihomework.aicontentcreator.data.settings.ModelMode
import com.aihomework.aicontentcreator.data.settings.SettingsRepository
import com.aihomework.aicontentcreator.ui.CreateScreen
import com.aihomework.aicontentcreator.ui.EditScreen
import com.aihomework.aicontentcreator.ui.HistoryScreen
import com.aihomework.aicontentcreator.ui.SettingsScreen
import com.aihomework.aicontentcreator.ui.saveImageToGallery
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
    val generatedImageFileStore = remember { GeneratedImageFileStore(appContext) }
    val historyItems by historyRepository.items.collectAsState()
    val historyStorageStatus by historyRepository.status.collectAsState()

    var selectedTab by remember { mutableStateOf(AppTab.Create) }
    var createState by remember { mutableStateOf(CreateUiState()) }
    var editState by remember { mutableStateOf(EditUiState()) }
    var settings by remember { mutableStateOf(settingsRepository.loadSettings()) }
    var apiKeyInput by remember { mutableStateOf("") }
    var settingsMessage by remember { mutableStateOf<String?>(null) }
    var textModelTestResult by remember { mutableStateOf<String?>(null) }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri == null) {
            createState = createState.copy(message = "已取消选择图片。")
        } else {
            createState = createState.copy(
                selectedImageUri = uri.toString(),
                processedImageUri = null,
                gestureCropSourceUri = null,
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
                                    gestureCropSourceUri = null,
                                    imageProcessingMessage = null,
                                    imageUploadNotice = null,
                                    textStyle = TextCreationStyle.defaultFor(scenario),
                                    generationCount = 1,
                                    styleAdvice = emptyList(),
                                    styleAdviceMessage = null,
                                    isSuggestingStyle = false,
                                    isOptimizingImagePrompt = false,
                                    imagePromptOriginal = null,
                                    optimizedImagePrompt = null,
                                    showTextToImagePromptCard = false,
                                    isPreparingTextToImagePrompt = false,
                                    textToImagePromptSource = null,
                                    textToImagePromptCandidate = null,
                                    result = null,
                                    message = null
                                )
                            },
                            onImageDescriptionStyleSelected = { style ->
                                createState = createState.copy(imageDescriptionStyle = style)
                            },
                            onImageGenerationStyleSelected = { style ->
                                createState = createState.copy(imageGenerationStyle = style)
                            },
                            onImageAspectRatioSelected = { ratio ->
                                createState = createState.copy(imageAspectRatio = ratio)
                            },
                            onTextStyleSelected = { style ->
                                createState = createState.copy(textStyle = style)
                            },
                            onGenerationCountChanged = { count ->
                                createState = createState.copy(generationCount = count)
                            },
                            onSuggestStyle = {
                                val input = createState.input.trim()
                                if (input.isBlank()) {
                                    createState = createState.copy(
                                        styleAdvice = emptyList(),
                                        styleAdviceMessage = "请先输入内容，再推荐风格。"
                                    )
                                    return@CreateScreen
                                }
                                scope.launch {
                                    createState = createState.copy(
                                        isSuggestingStyle = true,
                                        styleAdviceMessage = "正在推荐...",
                                        styleAdvice = emptyList(),
                                        message = null
                                    )
                                    try {
                                        val client = if (settings.mode == ModelMode.Mock) {
                                            MockModelClient()
                                        } else {
                                            RealModelClient(appContext, settings) {
                                                settingsRepository.getApiKey(settings.activeProfile.id)
                                            }
                                        }
                                        val advice = CreationRepository(client).suggestStyles(
                                            createState.selectedScenario,
                                            input
                                        )
                                        createState = createState.copy(
                                            isSuggestingStyle = false,
                                            styleAdvice = advice,
                                            styleAdviceMessage = if (advice.isEmpty()) {
                                                "暂未找到合适推荐，请手动选择风格。"
                                            } else {
                                                null
                                            }
                                        )
                                    } catch (error: ModelClientException) {
                                        createState = createState.copy(
                                            isSuggestingStyle = false,
                                            styleAdviceMessage = error.userMessage,
                                            message = error.userMessage
                                        )
                                    } catch (error: Exception) {
                                        createState = createState.copy(
                                            isSuggestingStyle = false,
                                            styleAdviceMessage = "推荐失败，请稍后重试。",
                                            message = "推荐失败，请稍后重试。"
                                        )
                                    }
                                }
                            },
                            onInputChanged = {
                                createState = createState.copy(
                                    input = it,
                                    styleAdviceMessage = null,
                                    imagePromptOriginal = null,
                                    optimizedImagePrompt = null,
                                    showTextToImagePromptCard = false,
                                    isPreparingTextToImagePrompt = false,
                                    textToImagePromptSource = null,
                                    textToImagePromptCandidate = null
                                )
                            },
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
                                            gestureCropSourceUri = null,
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
                            onApplyGrayscale = {
                                val sourceUri = createState.processedImageUri ?: createState.selectedImageUri
                                if (sourceUri == null) {
                                    createState = createState.copy(message = "请先选择图片。")
                                    return@CreateScreen
                                }
                                scope.launch {
                                    createState = createState.copy(
                                        isImageProcessing = true,
                                        imageProcessingMessage = "正在应用黑白滤镜...",
                                        imageUploadNotice = null
                                    )
                                    val result = withContext(Dispatchers.Default) {
                                        imageProcessor.applyGrayscale(sourceUri)
                                    }
                                    createState = if (result.uri != null) {
                                        createState.copy(
                                            processedImageUri = result.uri.toString(),
                                            isImageProcessing = false,
                                            imageProcessingMessage = "黑白滤镜已应用。",
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
                            onOpenGestureCrop = {
                                val sourceUri = createState.processedImageUri ?: createState.selectedImageUri
                                if (sourceUri == null) {
                                    createState = createState.copy(message = "请先选择图片。")
                                } else {
                                    createState = createState.copy(
                                        gestureCropSourceUri = sourceUri,
                                        imageProcessingMessage = null
                                    )
                                }
                            },
                            onDismissGestureCrop = {
                                createState = createState.copy(gestureCropSourceUri = null)
                            },
                            onApplyGestureCrop = { cropRect ->
                                val sourceUri = createState.gestureCropSourceUri
                                    ?: createState.processedImageUri
                                    ?: createState.selectedImageUri
                                if (sourceUri == null) {
                                    createState = createState.copy(
                                        gestureCropSourceUri = null,
                                        message = "请先选择图片。"
                                    )
                                    return@CreateScreen
                                }
                                scope.launch {
                                    createState = createState.copy(
                                        gestureCropSourceUri = null,
                                        isImageProcessing = true,
                                        imageProcessingMessage = "正在框选裁剪图片...",
                                        imageUploadNotice = null
                                    )
                                    val result = withContext(Dispatchers.Default) {
                                        imageProcessor.cropImage(sourceUri, cropRect)
                                    }
                                    createState = if (result.uri != null) {
                                        createState.copy(
                                            processedImageUri = result.uri.toString(),
                                            isImageProcessing = false,
                                            imageProcessingMessage = "已完成框选裁剪。",
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
                            onCropImage = { ratio ->
                                val sourceUri = createState.processedImageUri ?: createState.selectedImageUri
                                if (sourceUri == null) {
                                    createState = createState.copy(message = "请先选择图片。")
                                    return@CreateScreen
                                }
                                scope.launch {
                                    createState = createState.copy(
                                        isImageProcessing = true,
                                        imageProcessingMessage = "正在中心裁剪图片...",
                                        imageUploadNotice = null
                                    )
                                    val result = withContext(Dispatchers.Default) {
                                        imageProcessor.centerCrop(sourceUri, ratio)
                                    }
                                    createState = if (result.uri != null) {
                                        createState.copy(
                                            processedImageUri = result.uri.toString(),
                                            isImageProcessing = false,
                                            imageProcessingMessage = "已裁剪为 ${ratio.displayName}。",
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
                                    createState = createState.copy(message = "请先处理图片。")
                                } else {
                                    shareImage(context, processedImageUri)
                                }
                            },
                            onOptimizeImagePrompt = {
                                val input = createState.input.trim()
                                if (input.isBlank()) {
                                    createState = createState.copy(
                                        imageUploadNotice = "请先输入图片描述，再优化提示词。",
                                        message = "请先输入图片描述，再优化提示词。"
                                    )
                                    return@CreateScreen
                                }
                                scope.launch {
                                    createState = createState.copy(
                                        isOptimizingImagePrompt = true,
                                        imagePromptOriginal = input,
                                        optimizedImagePrompt = null,
                                        imageUploadNotice = "正在优化提示词...",
                                        message = null
                                    )
                                    try {
                                        val client = if (settings.mode == ModelMode.Mock) {
                                            MockModelClient()
                                        } else {
                                            RealModelClient(appContext, settings) {
                                                settingsRepository.getApiKey(settings.activeProfile.id)
                                            }
                                        }
                                        val optimized = CreationRepository(client).optimizeImagePrompt(input)
                                        createState = createState.copy(
                                            isOptimizingImagePrompt = false,
                                            imagePromptOriginal = input,
                                            optimizedImagePrompt = optimized,
                                            imageUploadNotice = "已生成优化提示词，请选择是否应用。"
                                        )
                                    } catch (error: ModelClientException) {
                                        createState = createState.copy(
                                            isOptimizingImagePrompt = false,
                                            imagePromptOriginal = null,
                                            optimizedImagePrompt = null,
                                            imageUploadNotice = error.userMessage,
                                            message = error.userMessage
                                        )
                                    } catch (error: Exception) {
                                        createState = createState.copy(
                                            isOptimizingImagePrompt = false,
                                            imagePromptOriginal = null,
                                            optimizedImagePrompt = null,
                                            imageUploadNotice = "优化提示词失败，请稍后重试。",
                                            message = "优化提示词失败，请稍后重试。"
                                        )
                                    }
                                }
                            },
                            onApplyOptimizedImagePrompt = {
                                val optimized = createState.optimizedImagePrompt
                                if (optimized.isNullOrBlank()) {
                                    createState = createState.copy(message = "暂无可应用的优化提示词。")
                                } else {
                                    createState = createState.copy(
                                        input = optimized,
                                        imagePromptOriginal = null,
                                        optimizedImagePrompt = null,
                                        imageUploadNotice = "已应用优化提示词，请手动生成图片。"
                                    )
                                }
                            },
                            onKeepOriginalImagePrompt = {
                                createState = createState.copy(
                                    imagePromptOriginal = null,
                                    optimizedImagePrompt = null,
                                    imageUploadNotice = "已保留原提示词。"
                                )
                            },
                            onImagePromptExampleSelected = { example ->
                                createState = createState.copy(
                                    input = example,
                                    imagePromptOriginal = null,
                                    optimizedImagePrompt = null,
                                    showTextToImagePromptCard = false,
                                    isPreparingTextToImagePrompt = false,
                                    textToImagePromptSource = null,
                                    textToImagePromptCandidate = null,
                                    imageUploadNotice = "已填入示例提示词，请手动生成图片。"
                                )
                            },
                            onGenerate = {
                                if (createState.selectedScenario == CreationScenario.ImageGeneration) {
                                    val prompt = createState.input.trim()
                                    if (prompt.isBlank()) {
                                        createState = createState.copy(
                                            imageUploadNotice = "请先输入图片描述。",
                                            message = "请先输入图片描述。"
                                        )
                                        return@CreateScreen
                                    }
                                    scope.launch {
                                        createState = createState.copy(
                                            isLoading = true,
                                            result = null,
                                            imageUploadNotice = "正在生成图片...",
                                            message = null
                                        )
                                        try {
                                            val client = if (settings.mode == ModelMode.Mock) {
                                                MockImageGenerationClient(appContext)
                                            } else {
                                                RealImageGenerationClient(appContext, settings) {
                                                    settingsRepository.getApiKey(settings.activeProfile.id)
                                                }
                                            }
                                            val imageResult = client.generateImage(
                                                prompt = prompt,
                                                style = createState.imageGenerationStyle,
                                                aspectRatio = createState.imageAspectRatio
                                            )
                                            val result = imageResult.toCreationResult()
                                            historyRepository.addResult(result)
                                            createState = createState.copy(
                                                isLoading = false,
                                                result = result,
                                                imageUploadNotice = if (imageResult.isMock) {
                                                    "当前为演示模式，生成本地占位图，不调用真实模型。"
                                                } else {
                                                    "图片生成完成。"
                                                },
                                                message = "图片生成完成。"
                                            )
                                        } catch (error: ModelClientException) {
                                            createState = createState.copy(
                                                isLoading = false,
                                                imageUploadNotice = error.userMessage,
                                                message = error.userMessage
                                            )
                                        } catch (error: Exception) {
                                            createState = createState.copy(
                                                isLoading = false,
                                                imageUploadNotice = "图片生成失败，请稍后重试。",
                                                message = "图片生成失败，请稍后重试。"
                                            )
                                        }
                                    }
                                    return@CreateScreen
                                }

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
                                                imageDescriptionStyle = createState.imageDescriptionStyle,
                                                textStyle = createState.textStyle,
                                                generationCount = createState.generationCount
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
                                } else if (result.contentType == HistoryContentType.IMAGE) {
                                    createState = createState.copy(message = "图片作品暂不进入文本编辑页。")
                                } else {
                                    editState = result.toEditState()
                                    selectedTab = AppTab.Edit
                                }
                            },
                            onUseResultForImagePrompt = {
                                val result = createState.result
                                if (result == null || result.contentType == HistoryContentType.IMAGE) {
                                    createState = createState.copy(message = "暂无可用于生成配图的文本结果。")
                                } else {
                                    val imageStyle = when (result.scenario) {
                                        CreationScenario.Product -> ImageGenerationStyle.ProductDisplay
                                        CreationScenario.Moments -> ImageGenerationStyle.Illustration
                                        else -> createState.imageGenerationStyle
                                    }
                                    createState = createState.copy(
                                        selectedScenario = CreationScenario.ImageGeneration,
                                        input = result.content,
                                        selectedImageUri = null,
                                        processedImageUri = null,
                                        imageProcessingMessage = null,
                                        imageUploadNotice = "已填入配图提示词，请手动生成图片。",
                                        imageGenerationStyle = imageStyle,
                                        imageAspectRatio = ImageAspectRatio.Square,
                                        styleAdvice = emptyList(),
                                        styleAdviceMessage = null,
                                        isSuggestingStyle = false,
                                        isOptimizingImagePrompt = false,
                                        imagePromptOriginal = null,
                                        optimizedImagePrompt = null,
                                        showTextToImagePromptCard = true,
                                        isPreparingTextToImagePrompt = false,
                                        textToImagePromptSource = result.content,
                                        textToImagePromptCandidate = null,
                                        message = "已切换到图片生成。"
                                    )
                                }
                            },
                            onPrepareTextToImagePrompt = {
                                val sourceText = createState.textToImagePromptSource
                                    ?: createState.input
                                if (sourceText.isBlank()) {
                                    createState = createState.copy(message = "暂无可整理的文本。")
                                    return@CreateScreen
                                }
                                scope.launch {
                                    createState = createState.copy(
                                        isPreparingTextToImagePrompt = true,
                                        textToImagePromptCandidate = null,
                                        imageUploadNotice = "正在整理提示词...",
                                        message = null
                                    )
                                    try {
                                        val client = if (settings.mode == ModelMode.Mock) {
                                            MockModelClient()
                                        } else {
                                            RealModelClient(appContext, settings) {
                                                settingsRepository.getApiKey(settings.activeProfile.id)
                                            }
                                        }
                                        val prepared = CreationRepository(client).prepareImagePromptFromText(sourceText)
                                        createState = createState.copy(
                                            isPreparingTextToImagePrompt = false,
                                            textToImagePromptCandidate = prepared,
                                            imageUploadNotice = "已整理提示词，请确认是否应用。"
                                        )
                                    } catch (error: ModelClientException) {
                                        createState = createState.copy(
                                            isPreparingTextToImagePrompt = false,
                                            textToImagePromptCandidate = null,
                                            imageUploadNotice = error.userMessage,
                                            message = error.userMessage
                                        )
                                    } catch (error: Exception) {
                                        createState = createState.copy(
                                            isPreparingTextToImagePrompt = false,
                                            textToImagePromptCandidate = null,
                                            imageUploadNotice = "整理提示词失败，请稍后重试。",
                                            message = "整理提示词失败，请稍后重试。"
                                        )
                                    }
                                }
                            },
                            onApplyTextToImagePrompt = {
                                val candidate = createState.textToImagePromptCandidate
                                if (candidate.isNullOrBlank()) {
                                    createState = createState.copy(message = "暂无可应用的整理结果。")
                                } else {
                                    createState = createState.copy(
                                        input = candidate,
                                        showTextToImagePromptCard = false,
                                        isPreparingTextToImagePrompt = false,
                                        textToImagePromptSource = null,
                                        textToImagePromptCandidate = null,
                                        imageUploadNotice = "已应用整理提示词，请手动生成图片。"
                                    )
                                }
                            },
                            onUseOriginalTextToImagePrompt = {
                                createState = createState.copy(
                                    showTextToImagePromptCard = false,
                                    isPreparingTextToImagePrompt = false,
                                    textToImagePromptSource = null,
                                    textToImagePromptCandidate = null,
                                    imageUploadNotice = "已直接使用原文，请手动生成图片。"
                                )
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
                                val result = createState.result
                                if (result == null) {
                                    createState = createState.copy(message = "暂无可分享内容。")
                                } else if (result.contentType == HistoryContentType.IMAGE) {
                                    val uri = result.imagePreviewUri
                                        ?: generatedImageFileStore.uriFor(result.imageFileName)?.toString()
                                    if (uri.isNullOrBlank()) {
                                        createState = createState.copy(message = "暂无可分享图片。")
                                    } else {
                                        runCatching { shareImage(context, uri) }
                                            .onFailure {
                                                createState = createState.copy(message = "图片分享失败，请稍后重试。")
                                            }
                                    }
                                } else {
                                    val text = result.content
                                    if (text.isBlank()) {
                                        createState = createState.copy(message = "暂无可分享内容。")
                                    } else {
                                        shareText(context, text)
                                    }
                                }
                            },
                            onSaveImage = {
                                val result = createState.result
                                val uri = result?.imagePreviewUri
                                    ?: generatedImageFileStore.uriFor(result?.imageFileName)?.toString()
                                if (uri.isNullOrBlank()) {
                                    createState = createState.copy(message = "暂无可保存图片。")
                                } else {
                                    val errorMessage = saveImageToGallery(context, uri)
                                    createState = createState.copy(
                                        message = errorMessage ?: "图片已保存到相册。"
                                    )
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
                            },
                            isCurrentResultFavorite = createState.result?.let { result ->
                                historyItems.firstOrNull { it.id == result.id }?.isFavorite
                            } ?: false
                        )

                        AppTab.Edit -> EditScreen(
                            state = editState,
                            onTextChanged = {
                                editState = editState.copy(
                                    text = it,
                                    previousEditText = null,
                                    rewriteOriginalText = null,
                                    rewriteCandidateText = null,
                                    rewriteMessage = null
                                )
                            },
                            onSave = {
                                val id = editState.itemId
                                if (id == null || editState.text.isBlank()) {
                                    editState = editState.copy(message = "暂无可保存内容。")
                                } else {
                                    historyRepository.updateContent(id, editState.text)
                                    createState = createState.updateResultText(id, editState.text)
                                    editState = editState.copy(
                                        previousEditText = null,
                                        rewriteOriginalText = null,
                                        rewriteCandidateText = null,
                                        rewriteMessage = null,
                                        message = "修改已保存。"
                                    )
                                    selectedTab = AppTab.Create
                                }
                            },
                            onRewrite = { action ->
                                if (editState.text.isBlank()) {
                                    editState = editState.copy(
                                        rewriteMessage = "暂无可改写内容。",
                                        message = "暂无可改写内容。"
                                    )
                                    return@EditScreen
                                }
                                scope.launch {
                                    val previousText = editState.text
                                    editState = editState.copy(
                                        previousEditText = previousText,
                                        rewriteOriginalText = null,
                                        rewriteCandidateText = null,
                                        isRewriting = true,
                                        rewriteMessage = "正在改写...",
                                        message = null
                                    )
                                    try {
                                        val client = if (settings.mode == ModelMode.Mock) {
                                            MockModelClient()
                                        } else {
                                            RealModelClient(appContext, settings) {
                                                settingsRepository.getApiKey(settings.activeProfile.id)
                                            }
                                        }
                                        val rewritten = CreationRepository(client).rewriteText(
                                            editState.text,
                                            action
                                        )
                                        editState = editState.copy(
                                            isRewriting = false,
                                            previousEditText = null,
                                            rewriteOriginalText = previousText,
                                            rewriteCandidateText = rewritten,
                                            rewriteMessage = "已生成改写候选，请对比后选择。"
                                        )
                                    } catch (error: ModelClientException) {
                                        editState = editState.copy(
                                            isRewriting = false,
                                            previousEditText = null,
                                            rewriteOriginalText = null,
                                            rewriteCandidateText = null,
                                            rewriteMessage = error.userMessage,
                                            message = error.userMessage
                                        )
                                    } catch (error: Exception) {
                                        editState = editState.copy(
                                            isRewriting = false,
                                            previousEditText = null,
                                            rewriteOriginalText = null,
                                            rewriteCandidateText = null,
                                            rewriteMessage = "改写失败，请稍后重试。",
                                            message = "改写失败，请稍后重试。"
                                        )
                                    }
                                }
                            },
                            onApplyRewrite = {
                                val candidate = editState.rewriteCandidateText
                                if (candidate != null) {
                                    editState = editState.copy(
                                        text = candidate,
                                        previousEditText = null,
                                        rewriteOriginalText = null,
                                        rewriteCandidateText = null,
                                        rewriteMessage = "已应用改写结果，保存后更新历史。"
                                    )
                                }
                            },
                            onKeepOriginal = {
                                val original = editState.rewriteOriginalText
                                editState = if (original != null) {
                                    editState.copy(
                                        text = original,
                                        previousEditText = null,
                                        rewriteOriginalText = null,
                                        rewriteCandidateText = null,
                                        rewriteMessage = "已保留原文。"
                                    )
                                } else {
                                    editState.copy(
                                        previousEditText = null,
                                        rewriteOriginalText = null,
                                        rewriteCandidateText = null,
                                        rewriteMessage = null
                                    )
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
                            onReuseText = { item: HistoryItem ->
                                createState = createState.copy(
                                    selectedScenario = item.scenario,
                                    input = item.input.ifBlank { item.content },
                                    selectedImageUri = null,
                                    processedImageUri = null,
                                    imageProcessingMessage = null,
                                    imageUploadNotice = null,
                                    textStyle = TextCreationStyle.defaultFor(item.scenario),
                                    generationCount = 1,
                                    styleAdvice = emptyList(),
                                    styleAdviceMessage = null,
                                    isSuggestingStyle = false,
                                    isOptimizingImagePrompt = false,
                                    imagePromptOriginal = null,
                                    optimizedImagePrompt = null,
                                    showTextToImagePromptCard = false,
                                    isPreparingTextToImagePrompt = false,
                                    textToImagePromptSource = null,
                                    textToImagePromptCandidate = null,
                                    result = null,
                                    message = "已从历史填入内容，请手动生成或编辑。"
                                )
                                selectedTab = AppTab.Create
                            },
                            onRegenerateImage = { item: HistoryItem ->
                                createState = createState.copy(
                                    selectedScenario = CreationScenario.ImageGeneration,
                                    input = item.input.ifBlank { item.summary },
                                    selectedImageUri = null,
                                    processedImageUri = null,
                                    imageProcessingMessage = null,
                                    imageUploadNotice = "已从历史恢复图片生成设置，请手动生成图片。",
                                    imageGenerationStyle = item.imageGenerationStyle
                                        ?: createState.imageGenerationStyle,
                                    imageAspectRatio = item.imageAspectRatio ?: createState.imageAspectRatio,
                                    isOptimizingImagePrompt = false,
                                    imagePromptOriginal = null,
                                    optimizedImagePrompt = null,
                                    showTextToImagePromptCard = false,
                                    isPreparingTextToImagePrompt = false,
                                    textToImagePromptSource = null,
                                    textToImagePromptCandidate = null,
                                    result = null,
                                    message = "已从历史恢复图片生成设置。"
                                )
                                selectedTab = AppTab.Create
                            },
                            onToggleFavorite = { id ->
                                historyRepository.toggleFavorite(id)
                            },
                            onShareText = { item ->
                                if (item.content.isBlank()) {
                                    Toast.makeText(context, "暂无可分享内容。", Toast.LENGTH_SHORT).show()
                                } else {
                                    shareText(context, item.content)
                                }
                            },
                            onShareImage = { item ->
                                val uri = generatedImageFileStore.uriFor(item.imageFileName)
                                if (uri == null) {
                                    "图片文件不可用，无法分享。"
                                } else {
                                    runCatching { shareImage(context, uri.toString()) }
                                        .fold(
                                            onSuccess = { null },
                                            onFailure = { "图片分享失败，请稍后重试。" }
                                        )
                                }
                            },
                            onSaveImage = { item ->
                                val uri = generatedImageFileStore.uriFor(item.imageFileName)
                                if (uri == null) {
                                    "图片文件不可用，无法保存。"
                                } else {
                                    saveImageToGallery(context, uri.toString())
                                }
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
                            textModelTestResult = textModelTestResult,
                            onModeChanged = { mode ->
                                val updated = settings.copy(mode = mode)
                                settings = updated
                                settingsRepository.saveSettings(updated)
                                textModelTestResult = null
                            },
                            onActiveProfileChanged = { profileId ->
                                val updated = settings.copy(activeProfileId = profileId)
                                settings = updated
                                apiKeyInput = ""
                                textModelTestResult = null
                                settingsRepository.saveSettings(updated)
                                val activeProfile = updated.activeProfile
                                val keyStatus = if (activeProfile.hasApiKey) "已配置" else "未配置"
                                settingsMessage = "已切换到配置：${activeProfile.name.ifBlank { "未命名配置" }}。密钥状态：$keyStatus。"
                            },
                            onProfileNameChanged = { name ->
                                settings = settings.updateActiveProfile { it.copy(name = name) }
                                textModelTestResult = null
                            },
                            onBaseUrlChanged = { baseUrl ->
                                settings = settings.updateActiveProfile { it.copy(baseUrl = baseUrl) }
                                textModelTestResult = null
                            },
                            onTextModelChanged = { textModel ->
                                settings = settings.updateActiveProfile { it.copy(textModel = textModel) }
                                textModelTestResult = null
                            },
                            onVisionModelChanged = { visionModel ->
                                settings = settings.updateActiveProfile { it.copy(visionModel = visionModel) }
                                textModelTestResult = null
                            },
                            onImageGenerationModelChanged = { imageGenerationModel ->
                                settings = settings.updateActiveProfile {
                                    it.copy(imageGenerationModel = imageGenerationModel)
                                }
                                textModelTestResult = null
                            },
                            onImageGenerationEndpointChanged = { endpoint ->
                                settings = settings.updateActiveProfile {
                                    it.copy(imageGenerationEndpoint = endpoint)
                                }
                                textModelTestResult = null
                            },
                            onImageGenerationApiTypeChanged = { apiType: ImageGenerationApiType ->
                                settings = settings.updateActiveProfile {
                                    it.copy(imageGenerationApiType = apiType)
                                }
                                textModelTestResult = null
                            },
                            onApiKeyInputChanged = { apiKeyInput = it },
                            onSaveSettings = {
                                settingsRepository.saveSettings(settings)
                                settings = settingsRepository.loadSettings()
                                textModelTestResult = null
                                settingsMessage = "设置已保存。"
                            },
                            onSaveApiKey = {
                                settingsRepository.saveSettings(settings)
                                if (settingsRepository.saveApiKey(apiKeyInput, settings.activeProfile.id)) {
                                    apiKeyInput = ""
                                    settings = settingsRepository.loadSettings()
                                    textModelTestResult = null
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
                                textModelTestResult = null
                                settingsMessage = "模型密钥已清除。"
                            },
                            onTestTextConnection = {
                                textModelTestResult = "正在测试文本模型连接..."
                                scope.launch {
                                    try {
                                        RealModelClient(appContext, settings) {
                                            settingsRepository.getApiKey(settings.activeProfile.id)
                                        }.testTextConnection()
                                        textModelTestResult = "文本模型连接成功。"
                                    } catch (error: ModelClientException) {
                                        textModelTestResult = error.userMessage
                                    } catch (error: Exception) {
                                        textModelTestResult = "文本模型连接失败，请稍后重试。"
                                    }
                                }
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

private fun ImageGenerationResult.toCreationResult(): CreationResult {
    val content = buildString {
        if (isMock) {
            appendLine("【演示模式生成】")
        }
        appendLine("风格：${style.displayName}")
        appendLine("比例：${aspectRatio.displayName}")
        append("提示词摘要：${prompt.take(120)}")
    }
    return CreationResult(
        id = id,
        scenario = CreationScenario.ImageGeneration,
        originalInput = prompt,
        content = content,
        createdAtMillis = createdAtMillis,
        contentType = HistoryContentType.IMAGE,
        imageFileName = imageFileName,
        imagePreviewUri = previewUri,
        imageGenerationStyle = style,
        imageAspectRatio = aspectRatio,
        isMockImage = isMock
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
