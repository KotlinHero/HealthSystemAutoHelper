package tech.kotlinhero.autohelper.core.task

import kotlinx.coroutines.flow.Flow
import tech.kotlinhero.autohelper.core.ProgressLogTask
import tech.kotlinhero.autohelper.core.TaskProgress

class DiabetesVisitImportTask(
    val params: DiabetesVisitImportTaskParams
) : ProgressLogTask {
    override val taskDescription: String = "导入糖尿病随访"

    override fun execute(): Flow<TaskProgress> {
        TODO("Not yet implemented")
    }
}

data class DiabetesVisitImportTaskParams(
    val username: String,
    val password: String,
    val excelPath: String,
    val browserBinaryPath: String,
    val driverPath: String
)