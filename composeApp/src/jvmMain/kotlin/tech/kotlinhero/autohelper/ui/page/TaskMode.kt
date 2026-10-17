package tech.kotlinhero.autohelper.ui.page

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Handshake
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import io.github.vinceglb.filekit.dialogs.FileKitType
import io.github.vinceglb.filekit.dialogs.compose.rememberFilePickerLauncher
import io.github.vinceglb.filekit.name
import io.github.vinceglb.filekit.path
import tech.kotlinhero.autohelper.core.config.AppPreferences
import tech.kotlinhero.autohelper.ui.viewmodel.TaskExecuteViewModel
import tech.kotlinhero.autohelper.ui.viewmodel.UserExcelTaskStartParams

/**
 * 一个任务入口：展示文案与图标、启动前读取哪组默认账号密码、启动后记住哪组账号密码，以及如何启动任务。
 * 8 个任务的弹窗结构完全一致，用这份描述表替代原先复制的 8 段 Dialog 代码。
 */
private class TaskModeEntry(
    val title: String,
    val description: String,
    val icon: ImageVector,
    val credentials: () -> TaskCredentials,
    val saveCredentials: (TaskCredentials) -> Unit,
    val start: (UserExcelTaskStartParams) -> Unit,
)

private class TaskCredentials(val username: String, val password: String)

@Composable
fun TaskMode(
    taskExecuteViewModel: TaskExecuteViewModel,
    onTaskStarted: () -> Unit = {},
) {
    val entries = remember(taskExecuteViewModel) {
        listOf(
            TaskModeEntry(
                title = "导入高血压随访",
                description = "读取 Excel 批量录入高血压随访记录",
                icon = Icons.Filled.Favorite,
                credentials = { TaskCredentials(AppPreferences.hyperVisitUsername, AppPreferences.hyperVisitPassword) },
                saveCredentials = {
                    AppPreferences.hyperVisitUsername = it.username
                    AppPreferences.hyperVisitPassword = it.password
                },
                start = taskExecuteViewModel::startHypertensionVisitImportTask,
            ),
            TaskModeEntry(
                title = "导入高血压分组评估",
                description = "按风险分层批量填写高血压分组评估",
                icon = Icons.Filled.Assessment,
                credentials = { TaskCredentials(AppPreferences.hyperVisitUsername, AppPreferences.hyperVisitPassword) },
                saveCredentials = {
                    AppPreferences.hyperVisitUsername = it.username
                    AppPreferences.hyperVisitPassword = it.password
                },
                start = taskExecuteViewModel::startHypertensionRiskStratificationTask,
            ),
            TaskModeEntry(
                title = "导入糖尿病随访",
                description = "读取 Excel 批量录入糖尿病随访记录",
                icon = Icons.Filled.WaterDrop,
                credentials = { TaskCredentials(AppPreferences.diabetesUsername, AppPreferences.diabetesPassword) },
                saveCredentials = {
                    AppPreferences.diabetesUsername = it.username
                    AppPreferences.diabetesPassword = it.password
                },
                start = taskExecuteViewModel::startDiabetesVisitImportTask,
            ),
            TaskModeEntry(
                title = "导入糖尿病分组评估",
                description = "按风险分层批量填写糖尿病分组评估",
                icon = Icons.Filled.Assessment,
                credentials = { TaskCredentials(AppPreferences.diabetesUsername, AppPreferences.diabetesPassword) },
                saveCredentials = {
                    AppPreferences.diabetesUsername = it.username
                    AppPreferences.diabetesPassword = it.password
                },
                start = taskExecuteViewModel::startDiabetesRiskStratificationTask,
            ),
            TaskModeEntry(
                title = "新建健康体检表",
                // 健康体检表与糖尿病任务共用同一组账号配置（业务侧约定）
                description = "为尚未建档的居民批量创建健康体检表",
                icon = Icons.AutoMirrored.Filled.Assignment,
                credentials = { TaskCredentials(AppPreferences.diabetesUsername, AppPreferences.diabetesPassword) },
                saveCredentials = {
                    AppPreferences.diabetesUsername = it.username
                    AppPreferences.diabetesPassword = it.password
                },
                start = taskExecuteViewModel::startHealthFormCreateTask,
            ),
            TaskModeEntry(
                title = "完善健康体检表",
                description = "批量补全已有健康体检表的缺失内容",
                icon = Icons.AutoMirrored.Filled.Assignment,
                credentials = { TaskCredentials(AppPreferences.diabetesUsername, AppPreferences.diabetesPassword) },
                saveCredentials = {
                    AppPreferences.diabetesUsername = it.username
                    AppPreferences.diabetesPassword = it.password
                },
                start = taskExecuteViewModel::startHealthFormCompleteTask,
            ),
            TaskModeEntry(
                title = "导入签约服务",
                description = "读取 Excel 批量录入家庭医生签约服务",
                icon = Icons.Filled.Handshake,
                credentials = { TaskCredentials(AppPreferences.contractUsername, AppPreferences.contractPassword) },
                saveCredentials = {
                    AppPreferences.contractUsername = it.username
                    AppPreferences.contractPassword = it.password
                },
                start = taskExecuteViewModel::startContractImportTask,
            ),
            TaskModeEntry(
                title = "档案保存",
                description = "批量保存已录入的居民健康档案",
                icon = Icons.Filled.Save,
                credentials = { TaskCredentials(AppPreferences.contractUsername, AppPreferences.contractPassword) },
                saveCredentials = {
                    AppPreferences.contractUsername = it.username
                    AppPreferences.contractPassword = it.password
                },
                start = taskExecuteViewModel::startContractSaveTask,
            ),
        )
    }
    var activeEntry by remember { mutableStateOf<TaskModeEntry?>(null) }
    // 以 activeEntry 为 key：每次重新打开弹窗都会重新读取该任务对应的账号配置
    val activeCredentials = remember(activeEntry) { activeEntry?.credentials() }

    Column(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier.padding(start = 24.dp, top = 20.dp, end = 24.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = "任务模式",
                style = MaterialTheme.typography.headlineSmall,
            )
            Text(
                text = "选择业务任务，填写系统账号与 Excel 文件后开始执行",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        LazyVerticalGrid(
            columns = GridCells.Adaptive(320.dp),
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(24.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            items(entries, key = { it.title }) { item ->
                TaskModeItem(
                    title = item.title,
                    description = item.description,
                    icon = item.icon,
                    onCreateClick = { activeEntry = item },
                )
            }
        }
    }

    val entry = activeEntry
    if (entry != null && activeCredentials != null) {
        Dialog(onDismissRequest = { activeEntry = null }) {
            UserExcelTaskStartCard(
                title = entry.title,
                defaultUsername = activeCredentials.username,
                defaultPassword = activeCredentials.password,
                onDismiss = { activeEntry = null },
                onStart = { params ->
                    entry.start(params)
                    entry.saveCredentials(TaskCredentials(params.username, params.password))
                    activeEntry = null
                    onTaskStarted()
                },
            )
        }
    }
}

@Composable
private fun TaskModeItem(
    title: String,
    description: String,
    icon: ImageVector,
    onCreateClick: () -> Unit,
) {
    OutlinedCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onCreateClick),
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Surface(
                    shape = MaterialTheme.shapes.small,
                    color = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        modifier = Modifier.padding(8.dp).size(24.dp),
                    )
                }
                Text(text = title, style = MaterialTheme.typography.titleMedium)
            }
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
            ) {
                FilledTonalButton(
                    onClick = onCreateClick,
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    ),
                ) {
                    Text("开始任务")
                }
            }
        }
    }
}

@Composable
fun UserExcelTaskStartCard(
    onStart: (params: UserExcelTaskStartParams) -> Unit,
    modifier: Modifier = Modifier,
    title: String = "启动任务",
    defaultUsername: String = "",
    defaultPassword: String = "",
    onDismiss: () -> Unit = {},
) {
    var username by remember { mutableStateOf(defaultUsername) }
    var password by remember { mutableStateOf(defaultPassword) }
    var passwordVisible by remember { mutableStateOf(false) }
    var excelPath by remember { mutableStateOf("") }
    var excelFilename by remember { mutableStateOf("") }
    val excelPicker = rememberFilePickerLauncher(
        type = FileKitType.File(setOf("xlsx", "xls"))
    ) { file ->
        file?.let {
            excelPath = it.path
            excelFilename = it.name
        }
    }
    val canStart = username.isNotBlank() && password.isNotBlank() && excelPath.isNotEmpty()

    Surface(
        modifier = modifier.widthIn(min = 440.dp, max = 520.dp),
        shape = MaterialTheme.shapes.large,
        tonalElevation = 6.dp,
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(text = title, style = MaterialTheme.typography.titleLarge)
                Text(
                    text = "账号密码按任务类型保存，Excel 文件仅本次任务使用",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = username,
                onValueChange = { username = it },
                label = { Text("账号") },
                singleLine = true,
                isError = username.isBlank(),
            )
            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = password,
                onValueChange = { password = it },
                label = { Text("密码") },
                singleLine = true,
                isError = password.isBlank(),
                visualTransformation = if (passwordVisible) {
                    VisualTransformation.None
                } else {
                    PasswordVisualTransformation()
                },
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            imageVector = if (passwordVisible) {
                                Icons.Filled.VisibilityOff
                            } else {
                                Icons.Filled.Visibility
                            },
                            contentDescription = if (passwordVisible) "隐藏密码" else "显示密码",
                        )
                    }
                },
            )
            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = excelFilename,
                onValueChange = { },
                readOnly = true,
                placeholder = { Text("尚未选择 Excel 文件（.xlsx / .xls）") },
                singleLine = true,
                isError = excelPath.isEmpty(),
                supportingText = {
                    Text(if (excelPath.isEmpty()) "点击右侧文件夹图标选择文件" else excelPath)
                },
                trailingIcon = {
                    IconButton(onClick = { excelPicker.launch() }) {
                        Icon(Icons.Filled.FolderOpen, contentDescription = "选择 Excel 文件")
                    }
                },
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                TextButton(onClick = onDismiss) {
                    Text("取消")
                }
                Button(
                    modifier = Modifier.weight(1f),
                    enabled = canStart,
                    onClick = {
                        onStart(
                            UserExcelTaskStartParams(
                                username = username,
                                password = password,
                                excelPath = excelPath,
                            )
                        )
                    },
                ) {
                    Text("启动任务")
                }
            }
        }
    }
}
