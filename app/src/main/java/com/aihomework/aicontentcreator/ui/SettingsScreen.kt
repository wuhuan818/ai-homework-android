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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.aihomework.aicontentcreator.data.settings.AppSettings
import com.aihomework.aicontentcreator.data.settings.ModelMode

@Composable
fun SettingsScreen(
    settings: AppSettings,
    apiKeyInput: String,
    historyStorageStatus: String,
    message: String?,
    onModeChanged: (ModelMode) -> Unit,
    onActiveProfileChanged: (String) -> Unit,
    onProfileNameChanged: (String) -> Unit,
    onBaseUrlChanged: (String) -> Unit,
    onTextModelChanged: (String) -> Unit,
    onVisionModelChanged: (String) -> Unit,
    onApiKeyInputChanged: (String) -> Unit,
    onSaveSettings: () -> Unit,
    onSaveApiKey: () -> Unit,
    onClearApiKey: () -> Unit,
    onMessageShown: () -> Unit
) {
    val context = LocalContext.current

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

        SettingsSection(title = "模型模式") {
            Text("当前模式：${settings.mode.displayText()}")
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = settings.mode == ModelMode.Mock,
                    onClick = { onModeChanged(ModelMode.Mock) },
                    label = { Text("演示模式") }
                )
                FilterChip(
                    selected = settings.mode == ModelMode.Real,
                    onClick = { onModeChanged(ModelMode.Real) },
                    label = { Text("真实模型模式") }
                )
            }
        }

        SettingsSection(title = "接口配置预设") {
            Text("该功能用于保存备用接口配置，不进行模型评分或横向对比。")
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
                label = { Text("图像模型（Vision Model）") },
                singleLine = true
            )
            Button(
                onClick = onSaveSettings,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("保存当前配置")
            }
        }

        SettingsSection(title = "模型密钥") {
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
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = onSaveApiKey) {
                    Text("保存密钥")
                }
                OutlinedButton(onClick = onClearApiKey) {
                    Text("清除密钥")
                }
            }
        }

        SettingsSection(title = "安全与存储") {
            Text("存储方式：Android Keystore 加密保存")
            Text("历史记录：AES-GCM 本地加密")
            Text("密钥显示：仅显示已配置 / 未配置，不展示完整密钥")
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
        ModelMode.Real -> "真实模型模式"
    }
}
