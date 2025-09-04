package tech.kotlinhero.autohelper.core

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector

interface ProgressLogTask {
    val taskDescription: String

    fun execute(): Flow<TaskProgress>
}

sealed interface TaskProgress {
    data class TotalCount(val value: Int) : TaskProgress
    data class ProgressUpdate(val value: Int) : TaskProgress
    data class Log(val value: String) : TaskProgress
}

suspend fun FlowCollector<TaskProgress>.totalCount(count: Int) {
    emit(TaskProgress.TotalCount(count))
}

suspend fun FlowCollector<TaskProgress>.progressUpdate(count: Int) {
    emit(TaskProgress.ProgressUpdate(count))
}

suspend fun FlowCollector<TaskProgress>.log(log: String) {
    emit(TaskProgress.Log(log))
}