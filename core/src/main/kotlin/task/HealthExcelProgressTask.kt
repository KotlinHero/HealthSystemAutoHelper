package tech.kotlinhero.autohelper.core.task

import com.microsoft.playwright.Page
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import tech.kotlinhero.autohelper.core.IndependentTask
import tech.kotlinhero.autohelper.core.TaskProgress
import tech.kotlinhero.autohelper.core.emitLog
import tech.kotlinhero.autohelper.core.emitProgressUpdate
import tech.kotlinhero.autohelper.core.emitTotalCount
import tech.kotlinhero.autohelper.core.excel.ExcelRowMapper
import tech.kotlinhero.autohelper.core.extension.flowOnDefault
import tech.kotlinhero.autohelper.excel.readExcel

interface HealthRecordImporter<T> :
    HealthImportPrepare,
    HealthSingleRecordImport<T>

class HealthExcelListProgressTask<T : HealthRecordDescription>(
    private val page: Page,
    private val importer: HealthRecordImporter<T>,
    private val excelRowMapper: ExcelRowMapper<T>,
    private val excelFilePath: String,
) : IndependentTask<Flow<TaskProgress>> {

    override fun execute(): Flow<TaskProgress> = flowOnDefault {
        val records = coroutineScope {
            launch { importer.prepareImport() }
            readRecords()
        }
        emitTotalCount(records.size)
        records.forEachIndexed { index, record ->
            runCatching {
                emitProgressUpdate(index)
                importer.importRecord(record)
            }.onFailure {
                it.printStackTrace()
                emitLog("导入失败: ${record.recordDescription}")
                importer.prepareImport()
            }
        }
    }

    private suspend fun readRecords(): List<T> =
        readExcel(excelFilePath) { workbook ->
            val sheet = workbook.getSheetAt(excelRowMapper.sheetIndex)
            sheet.drop(excelRowMapper.dropCount).map { row ->
                excelRowMapper.mapRowTo(row)
            }
        }
}
