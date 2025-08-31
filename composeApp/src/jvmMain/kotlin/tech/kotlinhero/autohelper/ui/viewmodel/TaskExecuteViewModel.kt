package tech.kotlinhero.autohelper.ui.viewmodel

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import tech.kotlinhero.autohelper.core.settings.AppSettingsPreferences
import tech.kotlinhero.autohelper.core.task.HypertensionVisitImportTask
import tech.kotlinhero.autohelper.core.task.HypertensionVisitImportTaskParams

class TaskExecuteViewModel : ViewModel() {
    private val _hasTaskExecuting = mutableStateOf(false)

    private val _totalCount = mutableStateOf(0)

    private val _finishCount = mutableStateOf(0)

    val hasTaskExecuting: State<Boolean> = _hasTaskExecuting

    val totalCount: State<Int> = _totalCount

    val finishCount: State<Int> = _finishCount

    fun startHypertensionVisitImportTask(params: UserExcelTaskStartParams) {
        viewModelScope.launch {
            _hasTaskExecuting.value = true
            runCatching {
                HypertensionVisitImportTask(params.toHypertensionVisitImportTaskParams()).execute(
                    useTotalCount = {
                        _totalCount.value = it
                    },
                    useFinishCount = {
                        _finishCount.value = it
                    }
                )
            }
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