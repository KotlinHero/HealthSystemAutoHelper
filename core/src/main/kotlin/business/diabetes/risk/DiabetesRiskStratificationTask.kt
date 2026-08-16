package tech.kotlinhero.autohelper.core.business.diabetes.risk

import com.microsoft.playwright.Page
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.onCompletion
import tech.kotlinhero.autohelper.core.HealthSystemAuthentication
import tech.kotlinhero.autohelper.core.ProgressTask
import tech.kotlinhero.autohelper.core.TaskProgress
import tech.kotlinhero.autohelper.core.business.diabetes.gotoDiabetesRecordListPage
import tech.kotlinhero.autohelper.core.business.loginHealthSystem
import tech.kotlinhero.autohelper.core.excel.DiabetesRecord
import tech.kotlinhero.autohelper.core.excel.diabetesRecordMapper
import tech.kotlinhero.autohelper.core.task.HealthExcelListProgressTask
import tech.kotlinhero.autohelper.core.task.HealthRecordImporter
import tech.kotlinhero.autohelper.webdriver.browserSession
import tech.kotlinhero.autohelper.webdriver.css
import tech.kotlinhero.autohelper.webdriver.firstByName
import tech.kotlinhero.autohelper.webdriver.firstByXpath
import tech.kotlinhero.autohelper.webdriver.name
import tech.kotlinhero.autohelper.webdriver.xpath

class DiabetesRiskStratificationTask(
    private val browserExecutablePath: String,
    private val healthSystemAuthentication: HealthSystemAuthentication,
    private val excelFilePath: String,
) : ProgressTask {

    override val taskDescription: String = "导入糖尿病分组评估任务"

    override fun execute(): Flow<TaskProgress> {
        val session = browserSession(browserExecutablePath)
        return HealthExcelListProgressTask(
            page = session.page,
            importer = DiabetesRiskStratificationImporter(
                page = session.page,
                authentication = healthSystemAuthentication
            ),
            excelRowMapper = diabetesRecordMapper(),
            excelFilePath = excelFilePath
        ).execute().onCompletion {
            session.close()
        }
    }
}

private class DiabetesRiskStratificationImporter(
    private val page: Page,
    private val authentication: HealthSystemAuthentication,
) : HealthRecordImporter<DiabetesRecord> {

    override suspend fun prepareImport() = page.run {
        loginHealthSystem(authentication)
        gotoDiabetesRecordListPage()
    }

    override suspend fun importRecord(record: DiabetesRecord) {
        page.run {
            name("idCard").fill(record.id)
            css("button.x-btn-text.query").click()
            css("table[class='x-grid3-row-table']").first().dblclick()
            xpath("//*[text() = '糖尿病分组']").click()
            firstByName("fixDate")?.fill(record.planDate)
            firstByName("fbs")?.fill(record.bloodGlucose)
            firstByXpath("//*[text() = '确定(F1)']")?.click()
            delay(500)
            css("button[id='CLOSE']").click()
        }
    }
}
