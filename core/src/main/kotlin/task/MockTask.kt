package tech.kotlinhero.autohelper.core.task

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import tech.kotlinhero.autohelper.core.IndexLogExecuteTask

class MockTask : IndexLogExecuteTask {

    override val taskDescription: String = "测试任务"

    override suspend fun execute(
        useTotalCount: (totalCount: Int) -> Unit,
        useFinishCount: (currentCount: Int) -> Unit,
        useLog: (log: String) -> Unit
    ) = withContext(Dispatchers.Default) {
        useTotalCount(100)
        for (i in 1..100) {
            useFinishCount(i)
            1.rangeTo(1000000000).forEach { _ -> 1 + 1 }
            useLog("正在导入第 $i 条数据")
        }
    }
}