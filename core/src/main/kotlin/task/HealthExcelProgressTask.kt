package tech.kotlinhero.autohelper.core.task

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
    }

    private suspend fun readRecords(): List<T> =
        readExcel(excelFilePath) { workbook ->
            val sheet = workbook.getSheetAt(excelRowMapper.sheetIndex)
            sheet.drop(excelRowMapper.dropCount).map { row ->
                excelRowMapper.mapRowTo(row)
            }
        }
}