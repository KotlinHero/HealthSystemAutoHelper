package tech.kotlinhero.autohelper.core.business.task

import com.microsoft.playwright.Page
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import tech.kotlinhero.autohelper.core.ImportTaskParam
import tech.kotlinhero.autohelper.core.ProgressTask
import tech.kotlinhero.autohelper.core.TaskProgress
import tech.kotlinhero.autohelper.core.business.contract.gotoContractRecordListPage
import tech.kotlinhero.autohelper.core.business.contract.searchContractRecord
import tech.kotlinhero.autohelper.core.business.loginHealthSystem
import tech.kotlinhero.autohelper.core.excel.toContractRecord
import tech.kotlinhero.autohelper.core.emitLog
import tech.kotlinhero.autohelper.core.emitProgressUpdate
import tech.kotlinhero.autohelper.core.emitTotalCount
import tech.kotlinhero.autohelper.core.extension.flowOnDefault
import tech.kotlinhero.autohelper.excel.readExcel
import tech.kotlinhero.autohelper.webdriver.browserSession
import tech.kotlinhero.autohelper.webdriver.id
import tech.kotlinhero.autohelper.webdriver.xpath
import kotlin.time.Duration.Companion.milliseconds

class ContractSaveTask(
    private val params: ImportTaskParam,
) : ProgressTask {

    override val taskDescription: String = "档案保存"

    override fun execute(): Flow<TaskProgress> = flowOnDefault {
        browserSession(params.browserExecutablePath).use { session ->
            with(session.page) {
                readExcel(params.excelPath) {
                    val sheet = getSheetAt(0)
                    val headRowCount = 1
                    emitTotalCount(sheet.lastRowNum)
                    emitLog("正在登录系统准备导入")
                    prepareImport()
                    sheet.drop(headRowCount).forEachIndexed { rowIndex, row ->
                        val record = row.toContractRecord()
                        val currentDataIdentifier = "${record.name}-${record.id}"
                        emitLog("开始导入：$currentDataIdentifier")
                        runCatching {
                            saveRecord(record)
                        }.fold(
                            onSuccess = {
                                emitLog("导入成功：$currentDataIdentifier")
                                emitProgressUpdate(rowIndex + 1)
                            },
                            onFailure = {
                                emitLog("导入失败：$currentDataIdentifier")
                                it.printStackTrace()
                                emitProgressUpdate(rowIndex + 1)
                                prepareImport()
                            }
                        )
                    }
                }
            }
        }
    }

    private suspend fun Page.saveRecord(record: tech.kotlinhero.autohelper.core.excel.ContractRecord) {
        prepareImport()
        searchContractRecord(record.id)
        delay(500.milliseconds)
        xpath("//button[text()='保存(F1)']").click()
        delay(500.milliseconds)
        xpath("//button[text()='确定']").click()
        delay(500.milliseconds)
        id("CLOSE").click()
    }

    private suspend fun Page.prepareImport() {
        loginHealthSystem(params.username, params.password)
        gotoContractRecordListPage()
    }
}
