package com.aihomework.aicontentcreator

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
import com.aihomework.aicontentcreator.data.model.CreationRequest
import com.aihomework.aicontentcreator.data.model.CreationResult
import com.aihomework.aicontentcreator.data.model.CreationScenario
import com.aihomework.aicontentcreator.data.model.HistoryItem
import com.aihomework.aicontentcreator.data.repository.CreationRepository
import com.aihomework.aicontentcreator.data.repository.HistoryRepository
import com.aihomework.aicontentcreator.data.settings.AppSettings
import com.aihomework.aicontentcreator.data.settings.ModelMode
import com.aihomework.aicontentcreator.data.settings.SettingsRepository
import com.aihomework.aicontentcreator.ui.CreateScreen
import com.aihomework.aicontentcreator.ui.EditScreen
import com.aihomework.aicontentcreator.ui.HistoryScreen
import com.aihomework.aicontentcreator.ui.SettingsScreen
import com.aihomework.aicontentcreator.ui.shareText
import com.aihomework.aicontentcreator.ui.state.CreateUiState
import com.aihomework.aicontentcreator.ui.state.EditUiState
import com.aihomework.aicontentcreator.ui.state.HistoryUiState
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AIContentCreatorApp()
        }
    }
}

private enum class AppTab(val title: String) {
    Create("Create"),
    Edit("Edit"),
    History("History"),
    Settings("Settings")
}

@Composable
private fun AIContentCreatorApp() {
    val context = LocalContext.current
    val appContext = context.applicationContext
    val scope = rememberCoroutineScope()
    val settingsRepository = remember { SettingsRepository(appContext) }
    val historyRepository = remember { HistoryRepository(appContext) }
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
            createState = createState.copy(message = "Image selection was cancelled.")
        } else {
            createState = createState.copy(
                selectedImageUri = uri.toString(),
                input = createState.input.ifBlank { "Selected image" },
                message = "Image selected."
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
                                icon = { Text(tab.title.first().toString()) }
                            )
                        }
                    }
                }
            ) { innerPadding ->
                Box(modifier = Modifier.padding(innerPadding)) {
                    when (selectedTab) {
                        AppTab.Create -> CreateScreen(
                            state = createState,
                            onScenarioSelected = { scenario ->
                                createState = createState.copy(
                                    selectedScenario = scenario,
                                    input = "",
                                    selectedImageUri = null,
                                    result = null,
                                    message = null
                                )
                            },
                            onInputChanged = { createState = createState.copy(input = it) },
                            onUseMockImage = {
                                createState = createState.copy(
                                    input = "Mock image: city street at night with bright signs",
                                    selectedImageUri = null
                                )
                            },
                            onChooseImage = {
                                imagePickerLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            },
                            onGenerate = {
                                val input = createState.input.trim()
                                val hasImage = createState.selectedImageUri != null
                                if (input.isBlank() && !hasImage) {
                                    createState = createState.copy(message = "Please enter content or choose an image first.")
                                    return@CreateScreen
                                }
                                scope.launch {
                                    createState = createState.copy(isLoading = true, message = null)
                                    try {
                                        val client = if (settings.mode == ModelMode.Mock) {
                                            MockModelClient()
                                        } else {
                                            RealModelClient(appContext, settings) {
                                                settingsRepository.getApiKey()
                                            }
                                        }
                                        val result = CreationRepository(client).generate(
                                            CreationRequest(
                                                scenario = createState.selectedScenario,
                                                input = input,
                                                imageLabel = if (hasImage) "Selected image" else null,
                                                imageUri = createState.selectedImageUri
                                            )
                                        )
                                        historyRepository.addResult(result)
                                        createState = createState.copy(isLoading = false, result = result)
                                        editState = result.toEditState()
                                    } catch (error: ModelClientException) {
                                        createState = createState.copy(
                                            isLoading = false,
                                            message = error.userMessage
                                        )
                                    } catch (error: Exception) {
                                        createState = createState.copy(
                                            isLoading = false,
                                            message = "Generation failed. Please try again."
                                        )
                                    }
                                }
                            },
                            onEdit = {
                                val result = createState.result
                                if (result == null) {
                                    createState = createState.copy(message = "No editable content yet.")
                                } else {
                                    editState = result.toEditState()
                                    selectedTab = AppTab.Edit
                                }
                            },
                            onFavorite = {
                                val result = createState.result
                                if (result == null) {
                                    createState = createState.copy(message = "No content to favorite yet.")
                                } else {
                                    historyRepository.toggleFavorite(result.id)
                                    createState = createState.copy(message = "Favorite status updated.")
                                }
                            },
                            onShare = {
                                val text = createState.result?.content.orEmpty()
                                if (text.isBlank()) {
                                    createState = createState.copy(message = "No content to share yet.")
                                } else {
                                    shareText(context, text)
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
                                    editState = editState.copy(message = "No content to save.")
                                } else {
                                    historyRepository.updateContent(id, editState.text)
                                    createState = createState.updateResultText(id, editState.text)
                                    editState = editState.copy(message = "Changes saved.")
                                    selectedTab = AppTab.Create
                                }
                            },
                            onConvertMarkdown = {
                                if (editState.text.isBlank()) {
                                    editState = editState.copy(message = "No content to convert.")
                                } else {
                                    editState = editState.copy(
                                        text = toMarkdown(editState),
                                        message = "Converted to Markdown."
                                    )
                                }
                            },
                            onConvertPlainText = {
                                if (editState.text.isBlank()) {
                                    editState = editState.copy(message = "No content to convert.")
                                } else {
                                    editState = editState.copy(
                                        text = toPlainText(editState.text),
                                        message = "Converted to plain text."
                                    )
                                }
                            },
                            onShare = {
                                if (editState.text.isBlank()) {
                                    Toast.makeText(context, "No content to share yet.", Toast.LENGTH_SHORT).show()
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
                            onBaseUrlChanged = { settings = settings.copy(baseUrl = it) },
                            onTextModelChanged = { settings = settings.copy(textModel = it) },
                            onVisionModelChanged = { settings = settings.copy(visionModel = it) },
                            onApiKeyInputChanged = { apiKeyInput = it },
                            onSaveSettings = {
                                settingsRepository.saveSettings(settings)
                                settings = settingsRepository.loadSettings()
                                settingsMessage = "Settings saved."
                            },
                            onSaveApiKey = {
                                if (settingsRepository.saveApiKey(apiKeyInput)) {
                                    apiKeyInput = ""
                                    settings = settingsRepository.loadSettings()
                                    settingsMessage = "API Key saved securely."
                                } else {
                                    settingsMessage = "Enter an API Key before saving."
                                }
                            },
                            onClearApiKey = {
                                settingsRepository.clearApiKey()
                                apiKeyInput = ""
                                settings = settingsRepository.loadSettings()
                                settingsMessage = "API Key cleared."
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
    val title = state.scenario?.displayName ?: "AI creation result"
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
        HistoryStorageStatus.NotInitialized -> "not initialized"
        HistoryStorageStatus.Encrypted -> "encrypted"
        HistoryStorageStatus.LoadFailed -> "load failed"
        HistoryStorageStatus.SaveFailed -> "save failed"
    }
}
