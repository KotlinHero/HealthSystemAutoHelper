package tech.kotlinhero.autohelper.ui.page

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.*
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
                value = appSettings.chromeBinaryPath,
                onValueChange = { appSettings.chromeBinaryPath = it },
                label = "谷歌浏览器执行文件"
            )
            FilePickerTextFieldSetting(
                value = appSettings.chromeDriverPath,
                onValueChange = { appSettings.chromeDriverPath = it },
                label = "谷歌浏览器驱动文件"
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