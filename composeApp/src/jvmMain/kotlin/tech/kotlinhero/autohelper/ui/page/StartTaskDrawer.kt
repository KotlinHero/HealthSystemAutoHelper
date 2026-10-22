package tech.kotlinhero.autohelper.ui.page

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.WarningAmber
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import io.github.vinceglb.filekit.dialogs.FileKitType
import io.github.vinceglb.filekit.dialogs.compose.rememberFilePickerLauncher
import io.github.vinceglb.filekit.name
import io.github.vinceglb.filekit.path
import tech.kotlinhero.autohelper.ui.component.AppButton
import tech.kotlinhero.autohelper.ui.component.AppTextField
import tech.kotlinhero.autohelper.ui.component.BtnSize
import tech.kotlinhero.autohelper.ui.component.BtnVariant
import tech.kotlinhero.autohelper.ui.component.DomainIcon
import tech.kotlinhero.autohelper.ui.component.FileDropField
import tech.kotlinhero.autohelper.ui.component.HairLine
import tech.kotlinhero.autohelper.ui.component.HintLine
import tech.kotlinhero.autohelper.ui.component.IconTap
import tech.kotlinhero.autohelper.ui.component.rememberHoverState
import tech.kotlinhero.autohelper.ui.task.TaskCredentials
import tech.kotlinhero.autohelper.ui.task.TaskCredentialsStore
import tech.kotlinhero.autohelper.ui.task.TaskDefinition
import tech.kotlinhero.autohelper.ui.theme.AppColors
import tech.kotlinhero.autohelper.ui.theme.AppShapes
import tech.kotlinhero.autohelper.ui.theme.AppType
import tech.kotlinhero.autohelper.ui.viewmodel.UserExcelTaskStartParams

/**
 * 右侧抽屉：唯一的启动路径。设计稿把原来的居中模态换成贴边抽屉，
 * 是因为居中弹窗会盖住任务卡，而这里用户恰恰需要一边核对任务名一边填参数。
 */
@Composable
fun StartTaskDrawer(
    definition: TaskDefinition,
    initialParams: UserExcelTaskStartParams?,
    onDismiss: () -> Unit,
    onStart: (UserExcelTaskStartParams) -> Boolean,
    modifier: Modifier = Modifier,
) {
    val saved = remember(definition) { TaskCredentialsStore.load(definition.accountGroup) }
    var username by remember(definition) { mutableStateOf(initialParams?.username ?: saved.username) }
    var password by remember(definition) { mutableStateOf(initialParams?.password ?: saved.password) }
    var excelPath by remember(definition) { mutableStateOf(initialParams?.excelPath.orEmpty()) }
    // 补录时 Excel 路径来自上次运行记录，可能带 / 或 \，两种分隔符都取最后一段
    var excelName by remember(definition) {
        mutableStateOf(excelPath.substringAfterLast('\\').substringAfterLast('/'))
    }
    var startFailed by remember(definition) { mutableStateOf(false) }
    /** 补录模式：抽屉带着「只跑这几行」进来，启动时原样带给任务 */
    val retryRows = remember(definition, initialParams) { initialParams?.onlyRows.orEmpty() }
    val picker = rememberFilePickerLauncher(type = FileKitType.File(setOf("xlsx", "xls"))) { file ->
        file?.let {
            excelPath = it.path
            excelName = it.name
        }
    }
    val canStart = username.isNotBlank() && password.isNotBlank() && excelPath.isNotBlank()

    Row(modifier.fillMaxHeight().background(AppColors.Surface).clickable(
        interactionSource = rememberHoverState().interactionSource,
        indication = null,
        onClick = {},
    )) {
        Box(Modifier.width(1.dp).fillMaxHeight().background(AppColors.Line))
        Column(Modifier.width(440.dp).fillMaxHeight()) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(start = 20.dp, end = 12.dp, top = 18.dp, bottom = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                DomainIcon(definition, 34.dp)
                Spacer(Modifier.width(11.dp))
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(definition.title, style = AppType.CardTitle, color = AppColors.Ink, maxLines = 1)
                    Text(
                        if (retryRows.isEmpty()) "读取 Excel 后逐条录入系统" else "只补录上次未录入的记录",
                        style = AppType.Meta,
                        color = AppColors.Ink3,
                    )
                }
                IconTap(Icons.Outlined.Close, "关闭", onDismiss, tint = AppColors.Ink3)
            }
            HairLine()
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 20.dp),
            ) {
                if (retryRows.isNotEmpty()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(AppShapes.Md)
                            .background(AppColors.BrandTint)
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                    ) {
                        Text("只补录未录入的 ${retryRows.size} 条", style = AppType.MetaStrong, color = AppColors.BrandDeep)
                        Spacer(Modifier.height(3.dp))
                        Text(
                            "其余记录不会重跑；用的仍是上次那份 Excel，换文件也可以。",
                            style = AppType.Small,
                            color = AppColors.Ink2,
                        )
                    }
                    Spacer(Modifier.height(18.dp))
                }
                Field("登录账号") {
                    AppTextField(
                        value = username,
                        onValueChange = { username = it; startFailed = false },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = definition.accountGroup.label,
                    )
                }
                Field("登录密码") {
                    AppTextField(
                        value = password,
                        onValueChange = { password = it; startFailed = false },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = "系统登录密码",
                        password = true,
                    )
                    Spacer(Modifier.height(8.dp))
                    HintLine("账号与密码已保存在本机，下次启动自动带出")
                }
                Field("Excel 文件") {
                    FileDropField(
                        fileName = excelName.takeIf { it.isNotBlank() },
                        path = excelPath.takeIf { it.isNotBlank() },
                        onPick = { picker.launch() },
                    )
                }
                Column(Modifier.padding(top = 4.dp), verticalArrangement = Arrangement.spacedBy(7.dp)) {
                    HairLine()
                    Spacer(Modifier.height(6.dp))
                    HintLine("Excel 只在本机读取，不会上传到其他位置")
                    HintLine("同一时间只跑一个任务，运行期间其他任务暂时不可启动")
                }
                if (startFailed) {
                    Spacer(Modifier.height(12.dp))
                    HintLine("有任务正在运行，等这一轮结束后再启动", tint = AppColors.Err, icon = Icons.Outlined.WarningAmber)
                }
            }
            HairLine()
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("任务开始后可随时取消", style = AppType.Small, color = AppColors.Ink3, modifier = Modifier.weight(1f))
                AppButton("取消", onDismiss, variant = BtnVariant.Ghost, size = BtnSize.Lg)
                Spacer(Modifier.width(8.dp))
                AppButton(
                    text = if (retryRows.isEmpty()) "启动任务" else "补录这 ${retryRows.size} 条",
                    onClick = {
                        val params = UserExcelTaskStartParams(
                            username = username,
                            password = password,
                            excelPath = excelPath,
                            onlyRows = retryRows.ifEmpty { null },
                        )
                        TaskCredentialsStore.save(definition.accountGroup, username, password)
                        val started = onStart(params)
                        startFailed = !started
                        if (started) onDismiss()
                    },
                    variant = BtnVariant.Primary,
                    size = BtnSize.Lg,
                    enabled = canStart,
                    minWidth = 132.dp,
                )
            }
        }
    }
}

@Composable
private fun Field(label: String, content: @Composable () -> Unit) {
    Column(Modifier.fillMaxWidth().padding(bottom = 20.dp)) {
        Text(label, style = AppType.Label, color = AppColors.Ink2)
        Spacer(Modifier.height(8.dp))
        content()
    }
}