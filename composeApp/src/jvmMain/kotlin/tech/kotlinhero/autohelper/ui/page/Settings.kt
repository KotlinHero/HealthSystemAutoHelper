package tech.kotlinhero.autohelper.ui.page

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.vinceglb.filekit.dialogs.compose.rememberFilePickerLauncher
import io.github.vinceglb.filekit.path
import tech.kotlinhero.autohelper.core.config.AppPreferences
import tech.kotlinhero.autohelper.core.config.AppSettings
import tech.kotlinhero.autohelper.core.config.HEALTH_SYSTEM_WEBSITE_URL
import java.io.File

@Composable
fun Settings() {
    val appSettings = remember { AppSettingsState() }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter,
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 720.dp)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "设置",
                    style = MaterialTheme.typography.headlineSmall,
                )
                Text(
                    text = "修改后立即生效并保存。账号密码随任务一起录入，不在此处维护。",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            SettingsSection(
                title = "卫生健康信息系统",
                description = "自动登录与录入数据的目标站点地址",
            ) {
                val url = appSettings.healthSystemWebsiteUrl
                OutlinedTextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = url,
                    onValueChange = { appSettings.healthSystemWebsiteUrl = it },
                    label = { Text("系统网址") },
                    singleLine = true,
                    isError = url.isBlank(),
                    supportingText = {
                        Text(if (url.isBlank()) "网址不能为空" else "示例：$HEALTH_SYSTEM_WEBSITE_URL")
                    },
                    trailingIcon = {
                        if (url != HEALTH_SYSTEM_WEBSITE_URL) {
                            IconButton(
                                onClick = { appSettings.healthSystemWebsiteUrl = HEALTH_SYSTEM_WEBSITE_URL }
                            ) {
                                Icon(Icons.Filled.Restore, contentDescription = "恢复默认网址")
                            }
                        }
                    },
                )
            }

            SettingsSection(
                title = "谷歌浏览器",
                description = "Chrome 主程序与对应版本 chromedriver 的完整路径，任务执行时使用",
            ) {
                FilePathSetting(
                    label = "谷歌浏览器执行文件",
                    description = "例如 chrome.exe 或 chrome-headless-shell.exe",
                    value = appSettings.chromeBinaryPath,
                    onValueChange = { appSettings.chromeBinaryPath = it },
                )
                FilePathSetting(
                    label = "谷歌浏览器驱动文件",
                    description = "与浏览器版本匹配的 chromedriver.exe",
                    value = appSettings.chromeDriverPath,
                    onValueChange = { appSettings.chromeDriverPath = it },
                )
            }
        }
    }
}

@Composable
private fun SettingsSection(
    title: String,
    description: String,
    content: @Composable ColumnScope.() -> Unit,
) {
    OutlinedCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(text = title, style = MaterialTheme.typography.titleMedium)
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            content()
        }
    }
}

@Composable
private fun FilePathSetting(
    label: String,
    description: String,
    value: String,
    onValueChange: (String) -> Unit,
) {
    val filePicker = rememberFilePickerLauncher { file ->
        // 取消选择时返回 null，保留原路径而不是清空
        file?.path?.let(onValueChange)
    }
    val fileExists = value.isNotBlank() && File(value).exists()
    OutlinedTextField(
        modifier = Modifier.fillMaxWidth(),
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        singleLine = true,
        isError = value.isNotBlank() && !fileExists,
        supportingText = {
            Text(
                when {
                    value.isBlank() -> description
                    fileExists -> "文件存在"
                    else -> "文件不存在，请重新选择"
                }
            )
        },
        trailingIcon = {
            IconButton(onClick = { filePicker.launch() }) {
                Icon(Icons.Filled.FolderOpen, contentDescription = "选择文件")
            }
        },
    )
}

class AppSettingsState : AppSettings {
    private var chromeBinaryPathState by mutableStateOf(AppPreferences.chromeBinaryPath)
    private var chromeDriverPathState by mutableStateOf(AppPreferences.chromeDriverPath)
    private var healthSystemWebsiteUrlState by mutableStateOf(AppPreferences.healthSystemWebsiteUrl)

    override var healthSystemWebsiteUrl: String
        get() = healthSystemWebsiteUrlState
        set(value) {
            healthSystemWebsiteUrlState = value
            AppPreferences.healthSystemWebsiteUrl = value
        }

    override var chromeBinaryPath: String
        get() = chromeBinaryPathState
        set(value) {
            chromeBinaryPathState = value
            AppPreferences.chromeBinaryPath = value
        }

    override var chromeDriverPath: String
        get() = chromeDriverPathState
        set(value) {
            chromeDriverPathState = value
            AppPreferences.chromeDriverPath = value
        }
}
