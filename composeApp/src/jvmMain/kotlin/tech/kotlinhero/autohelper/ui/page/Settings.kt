package tech.kotlinhero.autohelper.ui.page

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
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

@Composable
fun Settings() {
    val appSettings = remember { AppSettingsState() }

    Scaffold {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            TextField(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(4.dp),
                value = appSettings.healthSystemWebsiteUrl,
                onValueChange = { appSettings.healthSystemWebsiteUrl = it },
                label = { Text("卫生健康信息系统网址") }
            )
            FilePickerTextFieldSetting(
                value = appSettings.browserExecutablePath,
                onValueChange = { appSettings.browserExecutablePath = it },
                label = "浏览器可执行文件（可选，留空使用内置 Chromium）"
            )
        }
    }
}

@Composable
fun FilePickerTextFieldSetting(
    value: String,
    onValueChange: (String) -> Unit,
    label: String
) {
    var textFieldValue by remember { mutableStateOf(value) }
    val filePicker = rememberFilePickerLauncher { file ->
        textFieldValue = file?.path ?: ""
        onValueChange(textFieldValue)
    }
    Row(
        modifier = Modifier.padding(4.dp)
    ) {
        TextField(
            modifier = Modifier.weight(0.8f),
            value = textFieldValue,
            onValueChange = onValueChange,
            label = { Text(label) },
            readOnly = true
        )
        Button(
            modifier = Modifier.weight(0.2f)
                .padding(4.dp)
                .align(Alignment.CenterVertically),
            onClick = {
                filePicker.launch()
            },
            shape = RoundedCornerShape(4.dp)
        ) {
            Text(text = "选择")
        }
    }
}

class AppSettingsState : AppSettings {
    private var browserExecutablePathState by mutableStateOf(AppPreferences.browserExecutablePath)
    private var healthSystemWebsiteUrlState by mutableStateOf(AppPreferences.healthSystemWebsiteUrl)

    override var healthSystemWebsiteUrl: String
        get() = healthSystemWebsiteUrlState
        set(value) {
            healthSystemWebsiteUrlState = value
            AppPreferences.healthSystemWebsiteUrl = value
        }

    override var browserExecutablePath: String
        get() = browserExecutablePathState
        set(value) {
            browserExecutablePathState = value
            AppPreferences.browserExecutablePath = value
        }
}
