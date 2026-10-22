package tech.kotlinhero.autohelper.ui.page

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.WarningAmber
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
import tech.kotlinhero.autohelper.AppInfo
import tech.kotlinhero.autohelper.core.config.AppPreferences
import tech.kotlinhero.autohelper.core.config.AppSettings
import tech.kotlinhero.autohelper.core.config.HEALTH_SYSTEM_WEBSITE_URL
import tech.kotlinhero.autohelper.ui.component.AppButton
import tech.kotlinhero.autohelper.ui.component.AppTextField
import tech.kotlinhero.autohelper.ui.component.BrandMark
import tech.kotlinhero.autohelper.ui.component.BtnSize
import tech.kotlinhero.autohelper.ui.component.BtnVariant
import tech.kotlinhero.autohelper.ui.component.HairLine
import tech.kotlinhero.autohelper.ui.component.HintLine
import tech.kotlinhero.autohelper.ui.component.StatusChip
import tech.kotlinhero.autohelper.ui.theme.AppColors
import tech.kotlinhero.autohelper.ui.theme.AppType
import java.awt.Desktop
import java.io.File

/**
 * 设置：改完立即保存，不再需要「保存」按钮（原实现也是即改即存，这里把这件事写进说明里）。
 * 用 hairline 分区代替一叠卡片，是因为设置项只有 4 组、层级本来就浅，卡片边框反而切碎了阅读。
 */
@Composable
fun SettingsPage() {
    val settings = remember { AppSettingsState() }
    Box(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        Column(Modifier.padding(horizontal = 24.dp, vertical = 20.dp).widthIn(max = 760.dp)) {
            Text("设置", style = AppType.PageTitle, color = AppColors.Ink)
            Spacer(Modifier.height(4.dp))
            Text(
                "改完立即保存，下一次启动任务时生效。任务运行中修改不影响正在跑的这一轮。",
                style = AppType.CardDesc,
                color = AppColors.Ink2,
            )
            Spacer(Modifier.height(4.dp))

            Section("目标系统", "任务执行时打开这个地址。默认值是内置的基层卫生信息系统地址。") {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    AppTextField(
                        value = settings.healthSystemWebsiteUrl,
                        onValueChange = { settings.healthSystemWebsiteUrl = it },
                        modifier = Modifier.weight(1f),
                        placeholder = HEALTH_SYSTEM_WEBSITE_URL,
                        mono = true,
                    )
                    Spacer(Modifier.width(8.dp))
                    AppButton(
                        text = "恢复默认",
                        onClick = { settings.healthSystemWebsiteUrl = HEALTH_SYSTEM_WEBSITE_URL },
                        variant = BtnVariant.Ghost,
                        size = BtnSize.Sm,
                        enabled = settings.healthSystemWebsiteUrl != HEALTH_SYSTEM_WEBSITE_URL,
                    )
                }
            }

            Section("浏览器与驱动", "任务通过本机 Chrome 执行。Chrome 自动升级后驱动会失效，需要在此指定同版本驱动文件。") {
                PathField(
                    label = "Chrome 执行文件",
                    value = settings.chromeBinaryPath,
                    onValueChange = { settings.chromeBinaryPath = it },
                    missingHint = "没找到这个文件，请重新指定 chrome.exe 的完整路径。",
                )
                Spacer(Modifier.height(16.dp))
                PathField(
                    label = "ChromeDriver 执行文件",
                    value = settings.chromeDriverPath,
                    onValueChange = { settings.chromeDriverPath = it },
                    missingHint = "Chrome 升级后需要换同版本驱动，点右侧按钮指定新文件。",
                )
            }

            Section("运行日志", "未捕获的异常会写进这个文件，超过 1 MB 自动轮转。反馈问题时请附上最后一段。") {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    AppTextField(
                        value = logFilePath(),
                        onValueChange = {},
                        modifier = Modifier.weight(1f),
                        readOnly = true,
                        mono = true,
                    )
                    Spacer(Modifier.width(8.dp))
                    AppButton("打开所在文件夹", ::openLogFolder, variant = BtnVariant.Ghost, size = BtnSize.Sm)
                }
            }

            Section("关于", null) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    BrandMark(24.dp)
                    Spacer(Modifier.width(10.dp))
                    Text(
                        "卫生信息系统自动助手 · 版本 ${AppInfo.version} · Windows 桌面版",
                        style = AppType.Meta,
                        color = AppColors.Ink3,
                    )
                }
            }
        }
    }
}

@Composable
private fun Section(title: String, description: String?, content: @Composable () -> Unit) {
    Column(Modifier.fillMaxWidth().padding(top = 14.dp)) {
        HairLine()
        Spacer(Modifier.height(16.dp))
        Text(title, style = AppType.Label.copy(fontSize = AppType.CardTitle.fontSize), color = AppColors.Ink)
        if (description != null) {
            Spacer(Modifier.height(3.dp))
            Text(description, style = AppType.Meta, color = AppColors.Ink3)
        }
        Spacer(Modifier.height(14.dp))
        content()
        Spacer(Modifier.height(18.dp))
    }
}

@Composable
private fun PathField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    missingHint: String,
) {
    val picker = rememberFilePickerLauncher { file ->
        // 取消选择时返回 null，保留原路径
        file?.path?.let(onValueChange)
    }
    val exists = value.isNotBlank() && File(value).exists()
    val blank = value.isBlank()
    Column(Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(label, style = AppType.Label, color = AppColors.Ink2, modifier = Modifier.weight(1f))
            if (!blank) StatusChip(if (exists) "文件存在" else "文件不存在", exists)
        }
        Spacer(Modifier.height(8.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            AppTextField(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier.weight(1f),
                placeholder = "尚未指定",
                mono = true,
            )
            Spacer(Modifier.width(8.dp))
            AppButton("选择…", { picker.launch() }, variant = BtnVariant.Ghost, size = BtnSize.Sm)
        }
        if (!blank && !exists) {
            Spacer(Modifier.height(8.dp))
            HintLine(missingHint, tint = AppColors.Err, icon = Icons.Outlined.WarningAmber)
        }
    }
}

private fun logFilePath(): String = File(System.getProperty("user.home"), "AuthHelper.log").absolutePath

private fun openLogFolder() {
    runCatching {
        Desktop.getDesktop().open(File(System.getProperty("user.home")))
    }
}

/** 设置项直接写 AppPreferences，没有「保存」动作 */
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