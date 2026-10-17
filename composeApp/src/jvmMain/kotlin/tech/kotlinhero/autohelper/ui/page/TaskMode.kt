package tech.kotlinhero.autohelper.ui.page

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import io.github.vinceglb.filekit.dialogs.FileKitType
import io.github.vinceglb.filekit.dialogs.compose.rememberFilePickerLauncher
import io.github.vinceglb.filekit.name
import io.github.vinceglb.filekit.path
import kotlinx.coroutines.launch
import tech.kotlinhero.autohelper.core.config.AppPreferences
import tech.kotlinhero.autohelper.ui.viewmodel.TaskExecuteViewModel
import tech.kotlinhero.autohelper.ui.viewmodel.UserExcelTaskStartParams

/**
 * 一个任务入口：任务名、启动前读取哪组默认账号密码、启动后记住哪组账号密码，以及如何启动任务。
 * 8 个任务的弹窗结构完全一致，用这份描述表替代原先复制的 8 段 Dialog 代码。
 */
private class TaskModeEntry(
    val modeDescription: String,
    val credentials: () -> TaskCredentials,
    val saveCredentials: (TaskCredentials) -> Unit,
    val start: (UserExcelTaskStartParams) -> Unit,
)

private class TaskCredentials(val username: String, val password: String)

@Composable
fun TaskMode(
    taskExecuteViewModel: TaskExecuteViewModel
) {
    val scope = rememberCoroutineScope()
    val snackBarHostState = remember { SnackbarHostState() }
    val entries = remember(taskExecuteViewModel) {
        listOf(
            TaskModeEntry(
                modeDescription = "导入高血压随访",
                credentials = { TaskCredentials(AppPreferences.hyperVisitUsername, AppPreferences.hyperVisitPassword) },
                saveCredentials = {
                    AppPreferences.hyperVisitUsername = it.username
                    AppPreferences.hyperVisitPassword = it.password
                },
                start = taskExecuteViewModel::startHypertensionVisitImportTask,
            ),
            TaskModeEntry(
                modeDescription = "导入高血压分组评估",
                credentials = { TaskCredentials(AppPreferences.hyperVisitUsername, AppPreferences.hyperVisitPassword) },
                saveCredentials = {
                    AppPreferences.hyperVisitUsername = it.username
                    AppPreferences.hyperVisitPassword = it.password
                },
                start = taskExecuteViewModel::startHypertensionRiskStratificationTask,
            ),
            TaskModeEntry(
                modeDescription = "导入糖尿病随访",
                credentials = { TaskCredentials(AppPreferences.diabetesUsername, AppPreferences.diabetesPassword) },
                saveCredentials = {
                    AppPreferences.diabetesUsername = it.username
                    AppPreferences.diabetesPassword = it.password
                },
                start = taskExecuteViewModel::startDiabetesVisitImportTask,
            ),
            TaskModeEntry(
                modeDescription = "导入糖尿病分组评估",
                credentials = { TaskCredentials(AppPreferences.diabetesUsername, AppPreferences.diabetesPassword) },
                saveCredentials = {
                    AppPreferences.diabetesUsername = it.username
                    AppPreferences.diabetesPassword = it.password
                },
                start = taskExecuteViewModel::startDiabetesRiskStratificationTask,
            ),
            TaskModeEntry(
                modeDescription = "新建健康体检表",
                // 健康体检表与糖尿病任务共用同一组账号配置（业务侧约定）
                credentials = { TaskCredentials(AppPreferences.diabetesUsername, AppPreferences.diabetesPassword) },
                saveCredentials = {
                    AppPreferences.diabetesUsername = it.username
                    AppPreferences.diabetesPassword = it.password
                },
                start = taskExecuteViewModel::startHealthFormCreateTask,
            ),
            TaskModeEntry(
                modeDescription = "完善健康体检表",
                credentials = { TaskCredentials(AppPreferences.diabetesUsername, AppPreferences.diabetesPassword) },
                saveCredentials = {
                    AppPreferences.diabetesUsername = it.username
                    AppPreferences.diabetesPassword = it.password
                },
                start = taskExecuteViewModel::startHealthFormCompleteTask,
            ),
            TaskModeEntry(
                modeDescription = "导入签约服务",
                credentials = { TaskCredentials(AppPreferences.contractUsername, AppPreferences.contractPassword) },
                saveCredentials = {
                    AppPreferences.contractUsername = it.username
                    AppPreferences.contractPassword = it.password
                },
                start = taskExecuteViewModel::startContractImportTask,
            ),
            TaskModeEntry(
                modeDescription = "档案保存",
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
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackBarHostState) }
    ) {
        Column {
            val entry = activeEntry
            if (entry != null && activeCredentials != null) {
                Dialog(onDismissRequest = { activeEntry = null }) {
                    UserExcelTaskStartCard(
                        defaultUsername = activeCredentials.username,
                        defaultPassword = activeCredentials.password,
                        onStart = { params ->
                            entry.start(params)
                            entry.saveCredentials(TaskCredentials(params.username, params.password))
                            activeEntry = null
                            scope.launch {
                                snackBarHostState.showSnackbar(entry.modeDescription)
                            }
                        }
                    )
                }
            }
            entries.forEach { item ->
                TaskModeItem(
                    modeDescription = item.modeDescription,
                    onCreateClick = { activeEntry = item }
                )
            }
        }
    }
}

@Composable
fun UserExcelTaskStartCard(
    onStart: (params: UserExcelTaskStartParams) -> Unit,
    modifier: Modifier = Modifier,
    defaultUsername: String = "",
    defaultPassword: String = "",
) {
    var username by remember { mutableStateOf(defaultUsername) }
    var password by remember { mutableStateOf(defaultPassword) }
    var excelPath by remember { mutableStateOf("") }
    var excelFilename by remember { mutableStateOf("") }
    val excelPicker = rememberFilePickerLauncher(
        type = FileKitType.File(setOf("xlsx", "xls"))
    ) { file ->
        excelPath = file?.path ?: ""
        excelFilename = file?.name ?: ""
    }
    Card(modifier = modifier) {
        TextField(
            username,
            onValueChange = { username = it },
            label = { Text("账号") },
            modifier = Modifier.fillMaxWidth(),
            isError = username.isEmpty()
        )
        TextField(
            password,
            onValueChange = { password = it },
            label = { Text("密码") },
            modifier = Modifier.fillMaxWidth(),
            isError = password.isEmpty()
        )
        TextField(
            excelFilename,
            onValueChange = { },
            label = { Text("文件路径") },
            modifier = Modifier.fillMaxWidth(),
            readOnly = true,
            isError = excelPath.isEmpty()
        )

        Button(
            modifier = Modifier.padding(5.dp).fillMaxWidth(),
            onClick = { excelPicker.launch() }
        ) {
            Text(text = "选择文件")
        }
        Button(
            modifier = Modifier.padding(5.dp).fillMaxWidth(),
            enabled = listOf(username, password, excelPath).all { it.isNotEmpty() },
            onClick = {
                onStart(
                    UserExcelTaskStartParams(
                        username,
                        password,
                        excelPath
                    )
                )
            }
        ) {
            Text(text = "启动任务")
        }
    }
}

@Composable
fun TaskModeItem(
    onCreateClick: () -> Unit,
    modeDescription: String,
) {
    Card(
        modifier = Modifier
            .padding(5.dp)
            .height(50.dp)
            .fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 5.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(text = modeDescription)
            Button(onClick = onCreateClick) {
                Text(text = "创建任务")
            }
        }
    }
}
