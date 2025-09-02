package tech.kotlinhero.autohelper.core.task

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import tech.kotlinhero.autohelper.core.ExecuteScope
import tech.kotlinhero.autohelper.core.IndexLogExecuteTask

class MockTask : IndexLogExecuteTask {

    override val taskDescription: String = "测试任务"

    override suspend fun execute(
        block: ExecuteScope.() -> Unit
    ) = withContext(Dispatchers.Default) {
        val executeScope = ExecuteScope().apply { block() }
        executeScope.onTotalCountAccessible(100)
        for (i in 1..100) {
            executeScope.onProgressUpdate(i)
            1.rangeTo(1000000000).forEach { _ -> 1 + 1 }
            executeScope.onLogAppend("第 $i 轮执行完毕")
        }
    }
}