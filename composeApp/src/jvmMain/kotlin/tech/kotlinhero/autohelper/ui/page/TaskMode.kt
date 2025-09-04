package tech.kotlinhero.autohelper.ui.page

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
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

@Composable
fun TaskMode(
    taskExecuteViewModel: TaskExecuteViewModel
) {
    val scope = rememberCoroutineScope()
    val snackBarHostState = remember { SnackbarHostState() }
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackBarHostState) }
    ) {
        Column {
            var showHyperTaskDialog by remember { mutableStateOf(false) }
            if (showHyperTaskDialog) {
                Dialog(
                    onDismissRequest = { showHyperTaskDialog = false }
                ) {
                    UserExcelTaskStartCard(
                        defaultUsername = AppPreferences.hyperVisitUsername,
                        defaultPassword = AppPreferences.hyperVisitPassword,
                        onStart = { params ->
                            taskExecuteViewModel.startHypertensionVisitImportTask(params)
                            AppPreferences.hyperVisitUsername = params.username
                            AppPreferences.hyperVisitPassword = params.password
                            showHyperTaskDialog = false
                            scope.launch {
                                snackBarHostState.showSnackbar("导入高血压随访")
                            }
                        }
                    )
                }
            }
            var showDiabetesTaskDialog by remember { mutableStateOf(false) }
            if (showDiabetesTaskDialog) {
                Dialog(
                    onDismissRequest = { showDiabetesTaskDialog = false }
                ) {
                    UserExcelTaskStartCard(
                        defaultUsername = AppPreferences.diabetesUsername,
                        defaultPassword = AppPreferences.diabetesPassword,
                        onStart = { params ->
                            taskExecuteViewModel.startDiabetesVisitImportTask(params)
                            AppPreferences.diabetesUsername = params.username
                            AppPreferences.diabetesPassword = params.password
                            showDiabetesTaskDialog = false
                            scope.launch {
                                snackBarHostState.showSnackbar("导入糖尿病随访")
                            }
                        }
                    )
                }
            }
            TaskModeItem(
                modeDescription = "导入高血压随访",
                onCreateClick = {
                    showHyperTaskDialog = true
                }
            )
            TaskModeItem(
                modeDescription = "导入糖尿病随访",
                onCreateClick = {
                    showDiabetesTaskDialog = true
                }
            )
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
        type = FileKitType.File(setOf("xlsx"))
    ) { file ->
        excelPath = file?.path ?: ""
        excelFilename = file?.name ?: ""
    }
    Card(
        modifier = modifier
    ) {
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
            onClick = {
                if (listOf(username, password, excelPath).all { it.isNotEmpty() }) {
                    onStart(
                        UserExcelTaskStartParams(
                            username,
                            password,
                            excelPath
                        )
                    )
                }
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
            modifier = Modifier.fillMaxSize()
        ) {
            Text(
                modifier = Modifier
                    .align(Alignment.CenterVertically)
                    .padding(5.dp),
                text = modeDescription
            )
            Box(
                modifier = Modifier.fillMaxSize()
            ) {
                Button(
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .padding(5.dp),
                    onClick = onCreateClick
                ) {
                    Text(text = "创建任务")
                }
            }
        }
    }
}