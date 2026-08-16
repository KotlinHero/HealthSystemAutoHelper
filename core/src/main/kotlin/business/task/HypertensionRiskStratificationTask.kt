package tech.kotlinhero.autohelper.core.business.task

import com.microsoft.playwright.Page
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import tech.kotlinhero.autohelper.core.HealthSystemAuthentication
import tech.kotlinhero.autohelper.core.ProgressTask
import tech.kotlinhero.autohelper.core.TaskProgress
import tech.kotlinhero.autohelper.core.business.gotoHypertensionRecordListPage
import tech.kotlinhero.autohelper.core.business.loginHealthSystem
import tech.kotlinhero.autohelper.core.emitLog
import tech.kotlinhero.autohelper.core.emitProgressUpdate
import tech.kotlinhero.autohelper.core.emitTotalCount
import tech.kotlinhero.autohelper.core.extension.flowOnDefault
import tech.kotlinhero.autohelper.core.task.HealthRecordDescription
import tech.kotlinhero.autohelper.core.task.SimpleRecordDescription
import tech.kotlinhero.autohelper.excel.get
import tech.kotlinhero.autohelper.excel.readExcel
import tech.kotlinhero.autohelper.webdriver.browserSession
import tech.kotlinhero.autohelper.webdriver.css
import tech.kotlinhero.autohelper.webdriver.firstByName
import tech.kotlinhero.autohelper.webdriver.firstByXpath
import tech.kotlinhero.autohelper.webdriver.name
import tech.kotlinhero.autohelper.webdriver.xpath

class HypertensionRiskStratificationTask(
    private val browserExecutablePath: String,
    private val healthSystemAuthentication: HealthSystemAuthentication,
    private val excelFilePath: String,
) : ProgressTask {

    companion object {
        const val HEAD_ROW_COUNT = 1
    }

    override val taskDescription: String = "导入高血压分组评估任务"

    override fun execute(): Flow<TaskProgress> = flowOnDefault {
        browserSession(browserExecutablePath).use { session ->
            with(session.page) {
                val records = coroutineScope {
                    launch { prepareImport() }
                    readRecords()
                }
                emitTotalCount(records.size)
                records.forEachIndexed { index, record ->
                    runCatching {
                        emitProgressUpdate(index)
                        importRecord(record)
                    }.onFailure {
                        it.printStackTrace()
                        emitLog("导入失败:${record.id}-${record.name}")
                        prepareImport()
                    }
                }
            }
        }
    }

    private suspend fun Page.importRecord(record: HypertensionRiskStratificationRecord) {
        name("idCard").fill(record.id)
        css("button.x-btn-text.query").click()
        css("table[class='x-grid3-row-table']").first().dblclick()
        xpath("//*[text() = '分组评估']").click()
        // 元素点击过快会有时序问题（JS代码初始化导致）
        delay(500)
        xpath("//*[text() = '新增(F2)']").click()
        name("fixDate").fill(record.fixDate)
        firstByName("constriction")?.fill(record.constriction)
        firstByName("diastolic")?.fill(record.diastolic)
        firstByName("height")?.fill(record.height)
        firstByName("weight")?.fill(record.weight)
        firstByXpath("//*[text() = '确定(F1)']")?.click()
        delay(500)
        css("button[id='CLOSE']").click()
    }

    private suspend fun Page.prepareImport() {
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

    private data class HypertensionRiskStratificationRecord(
        val fixDate: String,
        val name: String,
        val id: String,
        val constriction: String,
        val diastolic: String,
        val height: String,
        val weight: String,
    ) : HealthRecordDescription by SimpleRecordDescription(name, id)
}
