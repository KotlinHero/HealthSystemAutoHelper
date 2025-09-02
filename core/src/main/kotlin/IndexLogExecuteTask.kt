package tech.kotlinhero.autohelper.core

interface IndexLogExecuteTask {
    val taskDescription: String

    suspend fun execute(block: ExecuteScope.() -> Unit)
}

class ExecuteScope {
    var onTotalCountAccessible: (Int) -> Unit = {}

    var onProgressUpdate: (Int) -> Unit = {}

    var onLogAppend: (String) -> Unit = {}
}