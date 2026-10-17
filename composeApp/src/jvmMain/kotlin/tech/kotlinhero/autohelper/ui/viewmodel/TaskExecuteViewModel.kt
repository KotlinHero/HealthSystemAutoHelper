package tech.kotlinhero.autohelper.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import tech.kotlinhero.autohelper.core.*
import tech.kotlinhero.autohelper.core.business.diabetes.risk.DiabetesRiskStratificationTask
import tech.kotlinhero.autohelper.core.business.form.HealthFormCompleteTask
import tech.kotlinhero.autohelper.core.business.form.HealthFormCreateTask
import tech.kotlinhero.autohelper.core.business.task.ContractImportTask
import tech.kotlinhero.autohelper.core.business.task.ContractSaveTask
import tech.kotlinhero.autohelper.core.business.task.DiabetesVisitImportTask
import tech.kotlinhero.autohelper.core.business.task.HypertensionRiskStratificationTask
import tech.kotlinhero.autohelper.core.business.task.HypertensionVisitImportTask
import tech.kotlinhero.autohelper.core.config.AppPreferences

class TaskExecuteViewModel : ViewModel() {

    private val _taskState = MutableStateFlow(TaskState.Idle)

    private val _totalCount = MutableStateFlow(0)

    private val _finishCount = MutableStateFlow(0)

    private val _taskDescription = MutableStateFlow("")

    private val _taskLog = MutableStateFlow(emptyList<String>())

    val taskState = _taskState.asStateFlow()

    val totalCount = _totalCount.asStateFlow()

    val finishCount = _finishCount.asStateFlow()

    val taskDescription = _taskDescription.asStateFlow()

    private var currentJob: Job? = null

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
            HypertensionVisitImportTask(
                importTaskParam(
                    AppPreferences.chromeBinaryPath,
                    AppPreferences.chromeDriverPath,
                    params.username,
                    params.password,
                    params.excelPath
                )
            ),
        )
    }

    fun startDiabetesVisitImportTask(
        params: UserExcelTaskStartParams,
    ) {
        startIndexLogTask(
            DiabetesVisitImportTask(
                importTaskParam(
                    AppPreferences.chromeBinaryPath,
                    AppPreferences.chromeDriverPath,
                    params.username,
                    params.password,
                    params.excelPath
                )
            ),
        )
    }

    fun startHypertensionRiskStratificationTask(
        params: UserExcelTaskStartParams,
    ) {
        startIndexLogTask(
            HypertensionRiskStratificationTask(
                browserDriverConfig(AppPreferences.chromeBinaryPath, AppPreferences.chromeDriverPath),
                healthSystemAuthentication(params.username, params.password),
                params.excelPath
            )
        )
    }

    fun startDiabetesRiskStratificationTask(
        params: UserExcelTaskStartParams,
    ) {
        startIndexLogTask(
            DiabetesRiskStratificationTask(
                browserDriverConfig(AppPreferences.chromeBinaryPath, AppPreferences.chromeDriverPath),
                healthSystemAuthentication(params.username, params.password),
                params.excelPath
            )
        )
    }

    fun startHealthFormCompleteTask(params: UserExcelTaskStartParams) {
        startIndexLogTask(
            HealthFormCompleteTask(
                browserDriverConfig(AppPreferences.chromeBinaryPath, AppPreferences.chromeDriverPath),
                healthSystemAuthentication(params.username, params.password),
                params.excelPath
            )
        )
    }

    fun startHealthFormCreateTask(params: UserExcelTaskStartParams) {
        startIndexLogTask(
            HealthFormCreateTask(
                browserDriverConfig(AppPreferences.chromeBinaryPath, AppPreferences.chromeDriverPath),
                healthSystemAuthentication(params.username, params.password),
                params.excelPath
            )
        )
    }

    fun startContractImportTask(
        params: UserExcelTaskStartParams,
    ) {
        startIndexLogTask(
            ContractImportTask(
                importTaskParam(
                    AppPreferences.chromeBinaryPath,
                    AppPreferences.chromeDriverPath,
                    params.username,
                    params.password,
                    params.excelPath
                )
            ),
        )
    }

    fun startContractSaveTask(
        params: UserExcelTaskStartParams,
    ) {
        startIndexLogTask(
            ContractSaveTask(
                importTaskParam(
                    AppPreferences.chromeBinaryPath,
                    AppPreferences.chromeDriverPath,
                    params.username,
                    params.password,
                    params.excelPath
                )
            ),
        )
    }

    private fun startIndexLogTask(
        task: ProgressTask,
    ) {
        // currentJob 只在任务真正结束后才置空，避免旧任务仍在收尾时被新任务并发启动
        if (currentJob != null) {
            log("已有任务正在运行，请等待完成或先取消")
            return
        }
        _taskDescription.value = task.taskDescription
        _taskState.value = TaskState.Running
        _taskLog.value = emptyList()
        _totalCount.value = 0
        _finishCount.value = 0
        currentJob = viewModelScope.launch {
            try {
                task.execute().collect {
                    when (it) {
                        is TaskProgress.TotalCount -> _totalCount.value = it.value
                        is TaskProgress.ProgressUpdate -> _finishCount.value = it.value
                        is TaskProgress.Log -> log(it.value)
                    }
                }
                _taskState.value = TaskState.Finished
            } catch (_: CancellationException) {
                log("任务已取消")
                _taskState.value = TaskState.Cancelled
            } catch (e: Exception) {
                e.printStackTrace()
                log(e.message?.takeIf { it.isNotBlank() } ?: "任务执行异常：${e.javaClass.simpleName}")
                _taskState.value = TaskState.Failed
            } finally {
                currentJob = null
            }
        }
    }
}

data class UserExcelTaskStartParams(
    val username: String,
    val password: String,
    val excelPath: String
)