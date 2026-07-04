package com.aihomework.aicontentcreator.ui

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.aihomework.aicontentcreator.data.settings.AppSettings
import com.aihomework.aicontentcreator.data.settings.ImageGenerationApiType
import com.aihomework.aicontentcreator.data.settings.ModelMode

@Composable
fun SettingsScreen(
    settings: AppSettings,
    apiKeyInput: String,
    historyStorageStatus: String,
    message: String?,
    textModelTestResult: String?,
    onModeChanged: (ModelMode) -> Unit,
    onActiveProfileChanged: (String) -> Unit,
    onProfileNameChanged: (String) -> Unit,
    onBaseUrlChanged: (String) -> Unit,
    onTextModelChanged: (String) -> Unit,
    onVisionModelChanged: (String) -> Unit,
    onImageGenerationModelChanged: (String) -> Unit,
    onImageGenerationEndpointChanged: (String) -> Unit,
    onImageGenerationApiTypeChanged: (ImageGenerationApiType) -> Unit,
    onApiKeyInputChanged: (String) -> Unit,
    onSaveSettings: () -> Unit,
    onSaveApiKey: () -> Unit,
    onClearApiKey: () -> Unit,
    onTestTextConnection: () -> Unit,
    onMessageShown: () -> Unit
) {
    val context = LocalContext.current
    var showAdvancedConfig by remember { mutableStateOf(false) }

    LaunchedEffect(message) {
        message?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            onMessageShown()
        }
    }

    Column(
        modifier = Modifier
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text("设置", style = MaterialTheme.typography.headlineSmall)

        SettingsSection(title = "当前状态") {
            Text("当前模式：${settings.mode.displayText()}")
            Text("当前配置：${settings.activeProfile.name.ifBlank { "未命名配置" }}")
            Text(if (settings.hasApiKey) "密钥状态：已配置" else "密钥状态：未配置")
        }

        SettingsSection(title = "模型模式") {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = settings.mode == ModelMode.Mock,
                    onClick = { onModeChanged(ModelMode.Mock) },
                    label = { Text("演示模式") }
                )
                FilterChip(
                    selected = settings.mode == ModelMode.Real,
                    onClick = { onModeChanged(ModelMode.Real) },
                    label = { Text("真实模式") }
                )
            }
        }

        SettingsSection(title = "接口配置预设") {
            settings.profiles.forEach { profile ->
                FilterChip(
                    selected = profile.id == settings.activeProfile.id,
                    onClick = { onActiveProfileChanged(profile.id) },
                    label = {
                        Column {
                            Text(profile.name.ifBlank { "未命名配置" })
                            Text(if (profile.hasApiKey) "模型密钥：已配置" else "模型密钥：未配置")
                        }
                    }
                )
            }
            Text("当前启用配置：${settings.activeProfile.name.ifBlank { "未命名配置" }}")
        }

        SettingsSection(title = "高级配置") {
            TextButton(onClick = { showAdvancedConfig = !showAdvancedConfig }) {
                Text(if (showAdvancedConfig) "收起高级配置" else "展开高级配置")
            }
            if (!showAdvancedConfig) {
                Text("高级配置已折叠，当前配置可继续用于生成和自测。")
                return@SettingsSection
            }
            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = settings.activeProfile.name,
                onValueChange = onProfileNameChanged,
                label = { Text("配置名称") },
                singleLine = true
            )
            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = settings.baseUrl,
                onValueChange = onBaseUrlChanged,
                label = { Text("接口地址（Base URL）") },
                singleLine = true
            )
            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = settings.textModel,
                onValueChange = onTextModelChanged,
                label = { Text("文本模型（Text Model）") },
                singleLine = true
            )
            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = settings.visionModel,
                onValueChange = onVisionModelChanged,
                label = { Text("图像模型（用于图片描述 / 看图理解）") },
                singleLine = true
            )
            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = settings.imageGenerationModel,
                onValueChange = onImageGenerationModelChanged,
                label = { Text("图片生成模型（用于根据文字生成图片）") },
                singleLine = true
            )
            Text("图片生成接口类型", style = MaterialTheme.typography.titleSmall)
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                ImageGenerationApiType.entries.forEach { apiType ->
                    FilterChip(
                        modifier = Modifier.fillMaxWidth(),
                        selected = settings.imageGenerationApiType == apiType,
                        onClick = { onImageGenerationApiTypeChanged(apiType) },
                        label = { Text(apiType.displayName) }
                    )
                }
            }
            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = settings.imageGenerationEndpoint,
                onValueChange = onImageGenerationEndpointChanged,
                label = { Text("图片生成接口地址") },
                placeholder = {
                    Text("例如：https://{WorkspaceId}.ap-southeast-1.maas.aliyuncs.com/api/v1/services/aigc/multimodal-generation/generation")
                },
                minLines = 2
            )
            Text(
                text = "图片生成接口可与文本接口不同；Qwen-Image 官方接口请填写完整文生图接口地址。",
                style = MaterialTheme.typography.bodySmall
            )

            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = apiKeyInput,
                onValueChange = onApiKeyInputChanged,
                label = { Text("模型密钥（API Key）") },
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.None,
                    keyboardType = KeyboardType.Password
                ),
                singleLine = true
            )
            Text(
                text = if (settings.hasApiKey) "模型密钥状态：已配置" else "模型密钥状态：未配置",
                style = MaterialTheme.typography.bodyMedium
            )
            Button(
                onClick = onSaveSettings,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("保存配置")
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = onSaveApiKey) {
                    Text("保存密钥")
                }
                OutlinedButton(onClick = onClearApiKey) {
                    Text("清除密钥")
                }
            }
        }

        SettingsSection(title = "配置自测") {
            Button(
                onClick = onTestTextConnection,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("测试文本模型连接")
            }
            textModelTestResult?.let {
                Text(it)
            }
        }

        SettingsSection(title = "安全与存储") {
            Text("模型密钥和历史元数据本地加密保存。")
            Text(historyStorageStatus)
        }
    }
}

@Composable
private fun SettingsSection(
    title: String,
    content: @Composable () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            content()
        }
    }
}

private fun ModelMode.displayText(): String {
    return when (this) {
        ModelMode.Mock -> "演示模式"
        ModelMode.Real -> "真实模式"
    }
}
