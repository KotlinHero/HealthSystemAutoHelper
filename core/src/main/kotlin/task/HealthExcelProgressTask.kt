package tech.kotlinhero.autohelper.core.task

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import org.openqa.selenium.WebDriver
import tech.kotlinhero.autohelper.core.*
import tech.kotlinhero.autohelper.core.excel.ExcelRowMapper
import tech.kotlinhero.autohelper.core.extension.flowOnDefault
import tech.kotlinhero.autohelper.excel.readExcel
import tech.kotlinhero.autohelper.webdriver.use

interface HealthRecordImporter<T> :
    HealthImportPrepare,
    HealthSingleRecordImport<T>

class HealthExcelListProgressTask<T : HealthRecordDescription>(
    private val driver: WebDriver,
    private val importer: HealthRecordImporter<T>,
    private val excelRowMapper: ExcelRowMapper<T>,
    private val excelFilePath: String
) : IndependentTask<Flow<TaskProgress>> {

    override fun execute(): Flow<TaskProgress> = flowOnDefault {
        driver.use {
            val records = coroutineScope {
                launch { importer.prepareImport() }
                readRecords()
            }
            emitTotalCount(records.size)
            records.forEachIndexed { index, record ->
                try {
                    importer.importRecord(record)
                    emitProgressUpdate(index + 1)
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Exception) {
                    emitLog("导入失败: ${record.recordDescription}（${e.javaClass.simpleName}: ${e.message}）")
                    emitProgressUpdate(index + 1)
                    importer.prepareImport()
                }
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