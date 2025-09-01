package tech.kotlinhero.autohelper.core

interface IndexLogExecuteTask {
    val taskDescription: String

    suspend fun execute(
        useTotalCount: (totalCount: Int) -> Unit,
        useFinishCount: (currentCount: Int) -> Unit,
        useLog: (log: String) -> Unit
    )
}