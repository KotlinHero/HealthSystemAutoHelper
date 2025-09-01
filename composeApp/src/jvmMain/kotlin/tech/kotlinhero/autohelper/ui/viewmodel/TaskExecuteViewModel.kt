package tech.kotlinhero.autohelper.ui.viewmodel

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import tech.kotlinhero.autohelper.core.IndexLogExecuteTask
import tech.kotlinhero.autohelper.core.settings.AppSettingsPreferences
import tech.kotlinhero.autohelper.core.task.HypertensionVisitImportTask
import tech.kotlinhero.autohelper.core.task.HypertensionVisitImportTaskParams
import tech.kotlinhero.autohelper.core.task.MockTask

class TaskExecuteViewModel : ViewModel() {
    private val _hasTaskExecuting = mutableStateOf(false)

    private val _totalCount = mutableStateOf(0)

    private val _finishCount = mutableStateOf(0)

    private val _taskLog = mutableStateListOf("")

    private val _taskDescription = mutableStateOf("")

    val hasTaskExecuting: State<Boolean> = _hasTaskExecuting

    val totalCount: State<Int> = _totalCount

    val finishCount: State<Int> = _finishCount

    val taskLog: List<String> = _taskLog

    val taskDescription: State<String> = _taskDescription

    fun startHypertensionVisitImportTask(params: UserExcelTaskStartParams) {
        startIndexLogTask(
            HypertensionVisitImportTask(params.toHypertensionVisitImportTaskParams())
        )
    }

    fun startMockTask() {
        startIndexLogTask(MockTask())
    }

    private fun startIndexLogTask(task: IndexLogExecuteTask) {
        viewModelScope.launch {
            _taskLog.clear()
            _taskDescription.value = task.taskDescription
            _hasTaskExecuting.value = true
            task.execute(
                useTotalCount = {
                    _totalCount.value = it
                },
                useFinishCount = {
                    _finishCount.value = it
                },
                useLog = {
                    _taskLog.add(it)
                }
            )
            _hasTaskExecuting.value = false
        }
    }
}

data class UserExcelTaskStartParams(
    val username: String,
    val password: String,
    val excelPath: String
)

fun UserExcelTaskStartParams.toHypertensionVisitImportTaskParams(): HypertensionVisitImportTaskParams {
    return HypertensionVisitImportTaskParams(
        username = username,
        password = password,
        excelPath = excelPath,
        browserBinaryPath = AppSettingsPreferences.chromeBinaryPath,
        driverPath = AppSettingsPreferences.chromeDriverPath
    )
}