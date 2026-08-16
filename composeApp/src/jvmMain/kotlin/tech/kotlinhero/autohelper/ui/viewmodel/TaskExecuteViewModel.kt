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

    private val _taskState = MutableStateFlow(TaskState.Finished)

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
                    browserExecutablePath = AppPreferences.browserExecutablePath,
                    username = params.username,
                    password = params.password,
                    excelPath = params.excelPath
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
                    browserExecutablePath = AppPreferences.browserExecutablePath,
                    username = params.username,
                    password = params.password,
                    excelPath = params.excelPath
                )
            ),
        )
    }

    fun startHypertensionRiskStratificationTask(
        params: UserExcelTaskStartParams,
    ) {
        startIndexLogTask(
            HypertensionRiskStratificationTask(
                browserExecutablePath = AppPreferences.browserExecutablePath,
                healthSystemAuthentication = healthSystemAuthentication(params.username, params.password),
                excelFilePath = params.excelPath
            )
        )
    }

    fun startDiabetesRiskStratificationTask(
        params: UserExcelTaskStartParams,
    ) {
        startIndexLogTask(
            DiabetesRiskStratificationTask(
                browserExecutablePath = AppPreferences.browserExecutablePath,
                healthSystemAuthentication = healthSystemAuthentication(params.username, params.password),
                excelFilePath = params.excelPath
            )
        )
    }

    fun startHealthFormCompleteTask(params: UserExcelTaskStartParams) {
        startIndexLogTask(
            HealthFormCompleteTask(
                browserExecutablePath = AppPreferences.browserExecutablePath,
                healthSystemAuthentication = healthSystemAuthentication(params.username, params.password),
                excelFilePath = params.excelPath
            )
        )
    }

    fun startHealthFormCreateTask(params: UserExcelTaskStartParams) {
        startIndexLogTask(
            HealthFormCreateTask(
                browserExecutablePath = AppPreferences.browserExecutablePath,
                healthSystemAuthentication = healthSystemAuthentication(params.username, params.password),
                excelFilePath = params.excelPath
            )
        )
    }

    fun startContractImportTask(
        params: UserExcelTaskStartParams,
    ) {
        startIndexLogTask(
            ContractImportTask(
                importTaskParam(
                    browserExecutablePath = AppPreferences.browserExecutablePath,
                    username = params.username,
                    password = params.password,
                    excelPath = params.excelPath
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
                    browserExecutablePath = AppPreferences.browserExecutablePath,
                    username = params.username,
                    password = params.password,
                    excelPath = params.excelPath
                )
            ),
        )
    }

    private fun startIndexLogTask(
        task: ProgressTask,
    ) {
        viewModelScope.launch {
            _taskDescription.value = task.taskDescription
            _taskState.value = TaskState.Running
            _taskLog.update { emptyList() }
            currentJob = launch {
                try {
                    task.execute().collect {
                        when (it) {
                            is TaskProgress.TotalCount -> _totalCount.value = it.value
                            is TaskProgress.ProgressUpdate -> _finishCount.value = it.value
                            is TaskProgress.Log -> log(it.value)
                        }
                    }
                } catch (_: CancellationException) {
                    log("任务已取消")
                } catch (e: Exception) {
                    e.printStackTrace()
                    log(e.message ?: "")
                    log("任务启动失败")
                } finally {
                    _taskState.value = TaskState.Finished
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