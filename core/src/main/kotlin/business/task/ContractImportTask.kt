package tech.kotlinhero.autohelper.core.business.task

import com.microsoft.playwright.Page
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import tech.kotlinhero.autohelper.core.ImportTaskParam
import tech.kotlinhero.autohelper.core.ProgressTask
import tech.kotlinhero.autohelper.core.TaskProgress
import tech.kotlinhero.autohelper.core.business.contract.clickByPossibleXpathList
import tech.kotlinhero.autohelper.core.business.contract.gotoContractRecordListPage
import tech.kotlinhero.autohelper.core.business.contract.searchContractRecord
import tech.kotlinhero.autohelper.core.business.loginHealthSystem
import tech.kotlinhero.autohelper.core.excel.ContractRecord
import tech.kotlinhero.autohelper.core.excel.toContractRecord
import tech.kotlinhero.autohelper.core.emitLog
import tech.kotlinhero.autohelper.core.emitProgressUpdate
import tech.kotlinhero.autohelper.core.emitTotalCount
import tech.kotlinhero.autohelper.core.extension.flowOnDefault
import tech.kotlinhero.autohelper.excel.readExcel
import tech.kotlinhero.autohelper.webdriver.browserSession
import tech.kotlinhero.autohelper.webdriver.css
import tech.kotlinhero.autohelper.webdriver.firstByXpath
import tech.kotlinhero.autohelper.webdriver.id
import tech.kotlinhero.autohelper.webdriver.name
import tech.kotlinhero.autohelper.webdriver.xpath
import kotlin.time.Duration.Companion.milliseconds

class ContractImportTask(
    private val params: ImportTaskParam,
) : ProgressTask {

    override val taskDescription: String = "导入签约服务"

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
                            importContractRecord(record)
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

    private suspend fun Page.importContractRecord(record: ContractRecord) {
        searchContractRecord(record.id)
        signContract(record)
    }

    private suspend fun Page.signContract(record: ContractRecord) {
        // 进入档案页面时，左侧边栏是否出现是随机的
        // 如果左侧边栏没有出现则需要点击左上角的展开小图标打开左侧边栏
        runCatching {
            xpath("//*[text()='家医服务']").first().dblclick()
        }.onFailure {
            sequenceOf(
                "//*[@class='x-tool x-tool-expand-west']",
                "//*[@class='x-tool x-tool-expand-west x-tool-expand-west-over']"
            ).map { xpathStr ->
                runCatching { xpath(xpathStr).click() }
            }.first {
                it.isSuccess
            }
        }
        delay(500.milliseconds)
        xpath("//*[text()='签约服务']").click()
        delay(500.milliseconds)
        xpath("//button[text()='签约(F1)']").click()
        delay(500.milliseconds)
        name("scDate").fill(record.date)
        clickByPossibleXpathList(
            listOf(
                "/html/body/div[12]/div[2]/div[1]/div/div/div/div/div/div/div[3]/div[2]/div/div[4]/div[2]/div[1]/div/div/div/div/div/div/div[2]/div/div/div/div[2]/div[1]/div/div/div/div[2]/div/div/div/div[2]/div[1]/div/div/div[2]/div/div[1]/div[1]/div[1]/div/table/thead/tr/td[1]/div/div",
                "/html/body/div[11]/div[2]/div[1]/div/div/div/div/div/div/div[3]/div[2]/div/div[4]/div[2]/div[1]/div/div/div/div/div/div/div[2]/div/div/div/div[2]/div[1]/div/div/div/div[2]/div/div/div/div[2]/div[1]/div/div/div[2]/div/div[1]/div[1]/div[1]/div/table/thead/tr/td[1]/div/div",
                "/html/body/div[10]/div[2]/div[1]/div/div/div/div/div/div/div[3]/div[2]/div/div[4]/div[2]/div[1]/div/div/div/div/div/div/div[2]/div/div/div/div[2]/div[1]/div/div/div/div[2]/div/div/div/div[2]/div[1]/div/div/div[2]/div/div[1]/div[1]/div[1]/div/table/thead/tr/td[1]/div/div"
            )
        )
        delay(500.milliseconds)
        firstByXpath("//button[text()='保存(F1)']")?.click()
        delay(500.milliseconds)
        xpath("//button[text()='是']").click()
        delay(1000.milliseconds)
        id("CLOSE").click()
    }

    private suspend fun Page.prepareImport() {
        loginHealthSystem(params.username, params.password)
        gotoContractRecordListPage()
    }
}
