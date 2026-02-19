package tech.kotlinhero.autohelper.core.task

import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import org.openqa.selenium.WebDriver
import tech.kotlinhero.autohelper.core.*
import tech.kotlinhero.autohelper.core.business.gotoHypertensionRecordListPage
import tech.kotlinhero.autohelper.core.business.loginHealthSystem
import tech.kotlinhero.autohelper.core.extension.flowOnDefault
import tech.kotlinhero.autohelper.excel.get
import tech.kotlinhero.autohelper.excel.readExcel
import tech.kotlinhero.autohelper.webdriver.*

class HypertensionRiskStratificationTask(
    private val browserDriverConfig: BrowserDriverConfig,
    private val healthSystemAuthentication: HealthSystemAuthentication,
    private val excelFilePath: String,
) : ProgressTask {

    companion object {
        const val HEAD_ROW_COUNT = 1
    }

    override val taskDescription: String = "导入高血压分组评估任务"

    override fun execute(): Flow<TaskProgress> = flowOnDefault {
        browserDriverConfig.buildWebDriver().use {
            val records = coroutineScope {
                launch { prepareImport() }
                readRecords()
            }
            totalCount(records.size)
            records.forEachIndexed { index, record ->
                runCatching {
                    progressUpdate(index)
                    importRecord(record)
                }.onFailure {
                    it.printStackTrace()
                    log("导入失败:${record.id}-${record.name}")
                    prepareImport()
                }
            }
        }
    }

    private suspend fun WebDriver.importRecord(record: HypertensionRiskStratificationRecord) {
        name("idCard").clearSendKeys(record.id)
        css("button.x-btn-text.query").click()
        doubleClick {
            css("table[class='x-grid3-row-table']")
        }
        xpath("//*[text() = '分组评估']").click()
        //元素点击过快会有时序问题（JS代码初始化导致）
        delay(500)
        xpath("//*[text() = '新增(F2)']").click()
        name("fixDate").clearSendKeys(record.fixDate)
        firstByName("constriction")?.clearSendKeys(record.constriction)
        firstByName("diastolic")?.clearSendKeys(record.diastolic)
        firstByName("height")?.clearSendKeys(record.height)
        firstByName("weight")?.clearSendKeys(record.weight)
        firstByXpath("//*[text() = '确定(F1)']")?.click()
        delay(500)
        css("button[id='CLOSE']").click()
    }

    private fun WebDriver.prepareImport() {
        loginHealthSystem(
            healthSystemAuthentication.username,
            healthSystemAuthentication.password
        )
        gotoHypertensionRecordListPage()
    }

    private suspend fun readRecords(): List<HypertensionRiskStratificationRecord> =
        readExcel(excelFilePath) { workbook ->
            val sheet = workbook.getSheetAt(1)
            sheet.drop(HEAD_ROW_COUNT).map { row ->
                HypertensionRiskStratificationRecord(
                    fixDate = row[11],
                    name = row[1],
                    id = row[2],
                    constriction = row[16],
                    diastolic = row[17],
                    height = (((row[9].toDoubleOrNull() ?: 0.0) * 100)).toString(),
                    weight = row[18],
                )
            }
        }

    data class HypertensionRiskStratificationRecord(
        val fixDate: String,
        val name: String,
        val id: String,
        val constriction: String,
        val diastolic: String,
        val height: String,
        val weight: String,
    )
}