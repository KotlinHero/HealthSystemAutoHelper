package tech.kotlinhero.autohelper.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import tech.kotlinhero.autohelper.core.ProgressTask
import tech.kotlinhero.autohelper.core.TaskProgress
import tech.kotlinhero.autohelper.core.TaskState
import tech.kotlinhero.autohelper.logger
import tech.kotlinhero.autohelper.ui.task.TaskCatalog
import tech.kotlinhero.autohelper.ui.task.TaskCredentialsStore
import tech.kotlinhero.autohelper.ui.task.TaskDefinition
import java.time.LocalTime
import java.util.logging.Level

/** 上一次启动过的任务与参数，用于「再跑一次」 */
data class LastLaunch(
    val definition: TaskDefinition,
    val params: UserExcelTaskStartParams,
)

class TaskExecuteViewModel : ViewModel() {

    private val history = TaskRunHistory()

    private val _taskState = MutableStateFlow(TaskState.Idle)
    private val _totalCount = MutableStateFlow(0)
    private val _finishCount = MutableStateFlow(0)
    private val _taskDescription = MutableStateFlow("")
    private val _taskLog = MutableStateFlow(emptyList<TaskLogLine>())
    private val _failures = MutableStateFlow(emptyList<TaskFailure>())
    private val _activeTask = MutableStateFlow<TaskDefinition?>(null)
    private val _activeRow = MutableStateFlow<Int?>(null)
    private val _runStartedAt = MutableStateFlow<Long?>(null)
    private val _runHistory = MutableStateFlow(history.all())
    private val _lastLaunch = MutableStateFlow<LastLaunch?>(null)

    val taskState = _taskState.asStateFlow()
    val totalCount = _totalCount.asStateFlow()
    val finishCount = _finishCount.asStateFlow()
    val taskDescription = _taskDescription.asStateFlow()
    val taskLog = _taskLog.asStateFlow()
    val failures = _failures.asStateFlow()
    val activeTask = _activeTask.asStateFlow()
    val activeRow = _activeRow.asStateFlow()
    val runStartedAt = _runStartedAt.asStateFlow()
    val runHistory = _runHistory.asStateFlow()
    val lastLaunch = _lastLaunch.asStateFlow()

    private var currentJob: Job? = null

    /** 任务运行中为 true：任务台上其余 7 张卡片据此置灰，避免并发启动 */
    val isRunning: Boolean get() = currentJob != null

    fun cancelCurrentTask() {
        currentJob?.let {
            if (it.isActive) {
                it.cancel()
                _taskState.value = TaskState.Cancelling
                appendSystemLog("已取消，正在关闭浏览器…", LogLevel.Warn)
            }
        }
    }

    /**
     * 启动任务。返回 false 表示被拒绝（已有任务在跑，或任务构造失败），调用方据此决定是否关闭抽屉。
     */
    fun startTask(definition: TaskDefinition, params: UserExcelTaskStartParams): Boolean {
        if (currentJob != null) {
            appendSystemLog("已有任务正在运行，请等它结束或先取消", LogLevel.Warn)
            return false
        }
        val task: ProgressTask = try {
            definition.create(params)
        } catch (e: Exception) {
            logger.log(Level.SEVERE, "任务创建失败：${definition.title}", e)
            _activeTask.value = definition
            _taskState.value = TaskState.Failed
            _taskLog.value = listOf(
                TaskLogLine(timeNow(), LogLevel.Err, null, null, e.message ?: "任务启动失败：${e.javaClass.simpleName}"),
            )
            return false
        }
        launchTask(definition, params, task)
        return true
    }

    /** 用上次的任务与参数整体重跑（结果页的「再跑一次」），补录留下的行号不参与 */
    fun retryLast(): Boolean {
        val launch = _lastLaunch.value ?: return false
        return startTask(launch.definition, launch.params.copy(onlyRows = null))
    }

    /**
     * 补录计划：这条任务现在还能只重跑哪些行。
     * 返回 null 表示补录不了——没有未录入的记录，或找不到上次用的 Excel 与账号。
     */
    fun retryRowsFor(taskId: String): List<Int>? {
        val record = _runHistory.value[taskId] ?: return null
        if (record.failed <= 0) return null
        // 本次会话的失败明细最准（含刚跑完这一轮）；程序重启后只能依赖上次运行记下的行号
        val fresh = if (_activeTask.value?.id == taskId) _failures.value.mapNotNull { it.row } else emptyList()
        val rows = fresh.ifEmpty { record.failedRows }
        if (rows.isEmpty()) return null
        if (resolvedParams(taskId) == null) return null
        return rows.distinct().sorted()
    }

    /**
     * 补录用的抽屉初始参数：沿用上次的账号与 Excel，行号限定为未录入的那几行。
     * 走抽屉而不是直接开跑，用户可以顺手确认 Excel 还在原地——补录最容易踩的坑就是文件被挪走。
     */
    fun retryParamsFor(taskId: String): UserExcelTaskStartParams? {
        val rows = retryRowsFor(taskId) ?: return null
        val params = resolvedParams(taskId) ?: return null
        return params.copy(onlyRows = rows)
    }

    /** 补录时用的参数：优先沿用上次启动的账号与文件，重启后回落到上次运行记下的 Excel 与已保存账号 */
    private fun resolvedParams(taskId: String): UserExcelTaskStartParams? {
        val record = _runHistory.value[taskId] ?: return null
        val definition = TaskCatalog.byId(taskId) ?: return null
        val last = _lastLaunch.value
        if (last != null && last.definition.id == taskId) return last.params
        if (record.excelPath.isBlank()) return null
        val credentials = TaskCredentialsStore.load(definition.accountGroup)
        return UserExcelTaskStartParams(credentials.username, credentials.password, record.excelPath)
    }

    private fun launchTask(definition: TaskDefinition, params: UserExcelTaskStartParams, task: ProgressTask) {
        val interpreter = TaskLogInterpreter(rowMap = params.onlyRows)
        val startedAt = System.currentTimeMillis()
        _activeTask.value = definition
        _taskDescription.value = definition.title
        _taskState.value = TaskState.Running
        _taskLog.value = emptyList()
        _failures.value = emptyList()
        _activeRow.value = null
        _totalCount.value = 0
        _finishCount.value = 0
        _runStartedAt.value = startedAt
        _lastLaunch.value = LastLaunch(definition, params)
        currentJob = viewModelScope.launch {
            var state = TaskState.Finished
            try {
                task.execute().collect { progress ->
                    when (progress) {
                        is TaskProgress.TotalCount -> _totalCount.value = progress.value
                        is TaskProgress.ProgressUpdate -> _finishCount.value = progress.value
                        is TaskProgress.Log -> {
                            val line = interpreter.interpret(progress.value, _finishCount.value, LocalTime.now())
                            _taskLog.update { it + line }
                            if (line.noise && line.row != null) {
                                _activeRow.value = line.row
                            }
                            if (line.level == LogLevel.Err) {
                                _failures.value = interpreter.failures.toList()
                            }
                        }
                    }
                }
            } catch (_: CancellationException) {
                appendSystemLog("任务已取消", LogLevel.Warn)
                state = TaskState.Cancelled
            } catch (e: Exception) {
                logger.log(Level.SEVERE, "任务执行失败：${definition.title}", e)
                appendSystemLog(e.message?.takeIf { it.isNotBlank() } ?: "任务执行异常：${e.javaClass.simpleName}", LogLevel.Err)
                appendSystemLog("堆栈已写入 ~/AuthHelper.log", LogLevel.Neutral)
                state = TaskState.Failed
            } finally {
                _taskState.value = state
                _activeRow.value = null
                _runStartedAt.value = null
                recordHistory(definition, params, startedAt)
                currentJob = null
            }
        }
    }

    /**
     * 记录本次结果。补录只跑了几行，不能把「131 条全部成功」写成「3 条成功」：
     * 把本轮结果并回上一次记录，卡片与汇总条继续用整批的口径回答「上次跑到哪」。
     */
    private fun recordHistory(definition: TaskDefinition, params: UserExcelTaskStartParams, startedAt: Long) {
        val now = System.currentTimeMillis()
        val elapsed = (now - startedAt).coerceAtLeast(0L)
        val failedRows = _failures.value.mapNotNull { it.row }.distinct().sorted()
        val previous = _runHistory.value[definition.id]
        val retried = params.onlyRows.orEmpty()
        val record = if (retried.isEmpty() || previous == null) {
            TaskRunRecord(
                taskId = definition.id,
                finishedAtMillis = now,
                total = _totalCount.value,
                handled = _finishCount.value,
                failed = failedRows.size,
                durationMillis = elapsed,
                excelPath = params.excelPath,
                failedRows = failedRows,
            )
        } else {
            val succeeded = retried.toSet() - failedRows.toSet()
            val remaining = (previous.failedRows.toSet() - succeeded) + failedRows.toSet()
            TaskRunRecord(
                taskId = definition.id,
                finishedAtMillis = now,
                total = previous.total,
                handled = (previous.handled + succeeded.size).coerceAtMost(previous.total),
                failed = remaining.size,
                durationMillis = previous.durationMillis + elapsed,
                excelPath = params.excelPath,
                failedRows = remaining.sorted(),
            )
        }
        _runHistory.update { it + (definition.id to record) }
        runCatching { history.record(record) }
    }

    private fun appendSystemLog(message: String, level: LogLevel) {
        _taskLog.update { it + TaskLogLine(timeNow(), level, null, null, message) }
    }

    private fun timeNow(): String = LocalTime.now().format(java.time.format.DateTimeFormatter.ofPattern("HH:mm:ss"))
}

data class UserExcelTaskStartParams(
    val username: String,
    val password: String,
    val excelPath: String,
    /** 只处理这些数据行（从 1 开始，不含表头）；null 表示整份 Excel 全跑 */
    val onlyRows: List<Int>? = null,
)
