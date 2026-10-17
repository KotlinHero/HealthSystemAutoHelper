package tech.kotlinhero.autohelper.core.business.task

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import org.openqa.selenium.WebDriver
import tech.kotlinhero.autohelper.core.*
import tech.kotlinhero.autohelper.core.business.contract.*
import tech.kotlinhero.autohelper.core.business.loginHealthSystem
import tech.kotlinhero.autohelper.core.excel.toContractRecord
import tech.kotlinhero.autohelper.core.extension.flowOnDefault
import tech.kotlinhero.autohelper.excel.readExcel
import tech.kotlinhero.autohelper.webdriver.*
import kotlin.time.Duration.Companion.milliseconds

class ContractSaveTask(
    private val params: ImportTaskParam
) : ProgressTask {

    override val taskDescription: String = "档案保存"

    override fun execute(): Flow<TaskProgress> = flowOnDefault {
        params.buildWebDriver().use {
            readExcel(params.excelPath) {
                val sheet = getSheetAt(0)
                val headRowCount = 1
                val rows = sheet.drop(headRowCount).toList()
                emitTotalCount(rows.size)
                emitLog("正在登录系统准备导入")
                prepareImport()
                rows.forEachIndexed { rowIndex, row ->
                    val record = row.toContractRecord()
                    val currentDataIdentifier = "${record.name}-${record.id}"
                    emitLog("开始导入：$currentDataIdentifier")
                    try {
                        saveRecord(record)
                        emitLog("导入成功：$currentDataIdentifier")
                        emitProgressUpdate(rowIndex + 1)
                    } catch (e: CancellationException) {
                        throw e
                    } catch (e: Exception) {
                        emitLog("导入失败：$currentDataIdentifier（${e.javaClass.simpleName}: ${e.message}）")
                        emitProgressUpdate(rowIndex + 1)
                        prepareImport()
                    }
                }
            }
        }
    }

    private suspend fun WebDriver.saveRecord(record: tech.kotlinhero.autohelper.core.excel.ContractRecord) {
        prepareImport()
        searchContractRecord(record.id)
        delay(500.milliseconds)
        xpath("//button[text()='保存(F1)']").click()
        delay(500.milliseconds)
        xpath("//button[text()='确定']").click()
        delay(500.milliseconds)
        id("CLOSE").click()
    }

    private suspend fun WebDriver.prepareImport() {
        loginHealthSystem(params.username, params.password)
        gotoContractRecordListPage()
    }
}
