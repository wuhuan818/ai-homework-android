package com.aihomework.aicontentcreator

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.platform.LocalContext
import com.aihomework.aicontentcreator.data.ai.MockModelClient
import com.aihomework.aicontentcreator.data.model.CreationRequest
import com.aihomework.aicontentcreator.data.model.CreationResult
import com.aihomework.aicontentcreator.data.model.CreationScenario
import com.aihomework.aicontentcreator.data.model.HistoryItem
import com.aihomework.aicontentcreator.data.repository.CreationRepository
import com.aihomework.aicontentcreator.data.repository.HistoryRepository
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
    Create("创作"),
    Edit("编辑"),
    History("历史"),
    Settings("设置")
}

@Composable
private fun AIContentCreatorApp() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val creationRepository = remember { CreationRepository(MockModelClient()) }
    val historyRepository = remember { HistoryRepository() }
    val historyItems by historyRepository.items.collectAsState()

    var selectedTab by remember { mutableStateOf(AppTab.Create) }
    var createState by remember { mutableStateOf(CreateUiState()) }
    var editState by remember { mutableStateOf(EditUiState()) }

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
                                message = null
                            )
                        },
                        onInputChanged = { createState = createState.copy(input = it) },
                        onUseMockImage = {
                            createState = createState.copy(input = "模拟图片：城市街景与明亮招牌")
                        },
                        onGenerate = {
                            val input = createState.input.trim()
                            if (input.isBlank()) {
                                createState = createState.copy(message = "请先输入主题或选择模拟图片")
                                return@CreateScreen
                            }
                            scope.launch {
                                createState = createState.copy(isLoading = true, message = null)
                                val result = creationRepository.generate(
                                    CreationRequest(
                                        scenario = createState.selectedScenario,
                                        input = input
                                    )
                                )
                                historyRepository.addResult(result)
                                createState = createState.copy(isLoading = false, result = result)
                                editState = result.toEditState()
                            }
                        },
                        onEdit = {
                            val result = createState.result
                            if (result == null) {
                                createState = createState.copy(message = "暂无可编辑内容")
                            } else {
                                editState = result.toEditState()
                                selectedTab = AppTab.Edit
                            }
                        },
                        onFavorite = {
                            val result = createState.result
                            if (result == null) {
                                createState = createState.copy(message = "暂无可收藏内容")
                            } else {
                                historyRepository.toggleFavorite(result.id)
                                createState = createState.copy(message = "已更新收藏状态")
                            }
                        },
                        onShare = {
                            val text = createState.result?.content.orEmpty()
                            if (text.isBlank()) {
                                createState = createState.copy(message = "暂无可分享内容")
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
                                editState = editState.copy(message = "暂无可保存内容")
                            } else {
                                historyRepository.updateContent(id, editState.text)
                                createState = createState.updateResultText(id, editState.text)
                                editState = editState.copy(message = "已保存修改")
                                selectedTab = AppTab.Create
                            }
                        },
                        onConvertMarkdown = {
                            if (editState.text.isBlank()) {
                                editState = editState.copy(message = "暂无可转换内容")
                            } else {
                                editState = editState.copy(
                                    text = toMarkdown(editState),
                                    message = "已转换为 Markdown"
                                )
                            }
                        },
                        onConvertPlainText = {
                            if (editState.text.isBlank()) {
                                editState = editState.copy(message = "暂无可转换内容")
                            } else {
                                editState = editState.copy(
                                    text = toPlainText(editState.text),
                                    message = "已转换为纯文本"
                                )
                            }
                        },
                        onShare = {
                            if (editState.text.isBlank()) {
                                Toast.makeText(context, "暂无可分享内容", Toast.LENGTH_SHORT).show()
                            } else {
                                shareText(context, editState.text)
                            }
                        },
                        onMessageShown = {
                            editState = editState.copy(message = null)
                        }
                        )

                        AppTab.History -> HistoryScreen(
                        state = HistoryUiState(historyItems),
                        onOpenForEdit = { item: HistoryItem ->
                            editState = item.toEditState()
                            selectedTab = AppTab.Edit
                        },
                        onToggleFavorite = { id ->
                            historyRepository.toggleFavorite(id)
                        }
                        )

                        AppTab.Settings -> SettingsScreen()
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
    val title = state.scenario?.displayName ?: "AI 创作结果"
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
