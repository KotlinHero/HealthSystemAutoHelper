package tech.kotlinhero.autohelper.ui.page

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import io.github.vinceglb.filekit.dialogs.compose.rememberFilePickerLauncher
import io.github.vinceglb.filekit.path
import tech.kotlinhero.autohelper.core.settings.AppSettings
import tech.kotlinhero.autohelper.core.settings.AppSettingsPreferences

@Composable
fun Settings() {
    val appSettings = remember { AppSettingsState() }

    val chromeBinaryPicker = rememberFilePickerLauncher { file ->
        appSettings.chromeBinaryPath = file?.path ?: ""
    }

    val chromeDriverPicker = rememberFilePickerLauncher { file ->
        appSettings.chromeDriverPath = file?.path ?: ""
    }

    Surface {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            Button(onClick = {
                chromeBinaryPicker.launch()
            }) {
                Text(text = "选择谷歌浏览器执行文件路径")
            }
            Text("选择谷歌浏览器执行文件路径: ${appSettings.chromeBinaryPath}")
            Button(onClick = {
                chromeDriverPicker.launch()
            }) {
                Text("选择谷歌浏览器驱动路径")
            }
            Text("谷歌浏览器驱动路径: ${appSettings.chromeDriverPath}")
        }
    }
}

class AppSettingsState : AppSettings {
    private var chromeBinaryPathState by mutableStateOf(AppSettingsPreferences.chromeBinaryPath)
    private var chromeDriverPathState by mutableStateOf(AppSettingsPreferences.chromeDriverPath)

    override var chromeBinaryPath: String
        get() = chromeBinaryPathState
        set(value) {
            chromeBinaryPathState = value
            AppSettingsPreferences.chromeBinaryPath = value
        }

    override var chromeDriverPath: String
        get() = chromeDriverPathState
        set(value) {
            chromeDriverPathState = value
            AppSettingsPreferences.chromeDriverPath = value
        }
}