package tech.kotlinhero.autohelper.ui.viewmodel

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
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

    private var currentJob: Job? = null

    val hasTaskExecuting: State<Boolean> = _hasTaskExecuting

    val totalCount: State<Int> = _totalCount

    val finishCount: State<Int> = _finishCount

    val taskLog: List<String> = _taskLog

    val taskDescription: State<String> = _taskDescription

    fun cancelCurrentTask() {
        currentJob?.cancel()
    }

    fun startHypertensionVisitImportTask(
        params: UserExcelTaskStartParams,
        onStartFailure: suspend (String) -> Unit = {},
    ) {
        startIndexLogTask(
            HypertensionVisitImportTask(params.toHypertensionVisitImportTaskParams()),
            onStartFailure
        )
    }

    fun startMockTask() {
        startIndexLogTask(MockTask())
    }

    private fun startIndexLogTask(
        task: IndexLogExecuteTask,
        onStartFailure: suspend (String) -> Unit = {},
    ) {
        viewModelScope.launch {
            _taskLog.clear()
            _taskDescription.value = task.taskDescription
            _hasTaskExecuting.value = true
            try {
                currentJob = launch {
                    runCatching {
                        task.execute {
                            onTotalCountAccessible = {
                                _totalCount.value = it
                            }
                            onProgressUpdate = {
                                _finishCount.value = it
                            }
                            onLogAppend = {
                                _taskLog.add(it)
                            }
                        }
                    }.onFailure {
                        onStartFailure(it.message ?: "请检查设置中谷歌浏览器参数")
                    }
                }
                currentJob?.join()
            } finally {
                _hasTaskExecuting.value = false
            }
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