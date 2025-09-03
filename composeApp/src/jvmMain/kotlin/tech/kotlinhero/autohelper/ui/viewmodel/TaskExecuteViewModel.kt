package tech.kotlinhero.autohelper.ui.viewmodel

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import tech.kotlinhero.autohelper.core.IndexLogExecuteTask
import tech.kotlinhero.autohelper.core.TaskState
import tech.kotlinhero.autohelper.core.config.AppPreferences
import tech.kotlinhero.autohelper.core.task.HypertensionVisitImportTask
import tech.kotlinhero.autohelper.core.task.HypertensionVisitImportTaskParams
import tech.kotlinhero.autohelper.core.task.MockTask

class TaskExecuteViewModel : ViewModel() {

    private val _taskState = MutableStateFlow(TaskState.FINISHED)

    private val _totalCount = MutableStateFlow(0)

    private val _finishCount = MutableStateFlow(0)

    private val _taskDescription = MutableStateFlow("")

    private val _taskLog = MutableStateFlow(emptyList<String>())

    val taskState = _taskState.asStateFlow()

    val totalCount = _totalCount.asStateFlow()

    val finishCount = _finishCount.asStateFlow()

    val taskDescription = _taskDescription.asStateFlow()

    private val _hasTaskExecuting = mutableStateOf(false)

    private var currentJob: Job? = null

    val hasTaskExecuting: State<Boolean> = _hasTaskExecuting

    val taskLog = _taskLog.asStateFlow()

    private fun log(message: String) {
        _taskLog.update { list -> list + message }
    }

    fun cancelCurrentTask() {
        currentJob?.let {
            if (it.isActive) {
                it.cancel()
                _taskState.value = TaskState.Cancelling
                log("任务取消中")
            }
        }
    }

    fun startHypertensionVisitImportTask(
        params: UserExcelTaskStartParams,
    ) {
        startIndexLogTask(
            HypertensionVisitImportTask(params.toHypertensionVisitImportTaskParams()),
        )
    }

    fun startMockTask() {
        startIndexLogTask(MockTask())
    }

    private fun startIndexLogTask(
        task: IndexLogExecuteTask,
    ) {
        viewModelScope.launch {
            _taskDescription.value = task.taskDescription
            _taskState.value = TaskState.Running
            _taskLog.update { emptyList() }
            currentJob = launch {
                try {
                    task.execute {
                        onTotalCountAccessible = {
                            _totalCount.value = it
                        }
                        onProgressUpdate = {
                            _finishCount.value = it
                        }
                        onLogAppend = { message ->
                            log(message)
                        }
                    }
                } catch (_: CancellationException) {
                    log("任务已取消")
                } catch (_: Exception) {
                    log("任务启动失败")
                } finally {
                    _taskState.value = TaskState.FINISHED
                }
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
        browserBinaryPath = AppPreferences.chromeBinaryPath,
        driverPath = AppPreferences.chromeDriverPath
    )
}