package tech.kotlinhero.autohelper.core.business.diabetes.risk

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.onCompletion
import org.openqa.selenium.WebDriver
import tech.kotlinhero.autohelper.core.*
import tech.kotlinhero.autohelper.core.business.diabetes.gotoDiabetesRecordListPage
import tech.kotlinhero.autohelper.core.business.loginHealthSystem
import tech.kotlinhero.autohelper.core.excel.DiabetesRecord
import tech.kotlinhero.autohelper.core.excel.diabetesRecordMapper
import tech.kotlinhero.autohelper.core.task.HealthExcelListProgressTask
import tech.kotlinhero.autohelper.core.task.HealthRecordImporter
import tech.kotlinhero.autohelper.webdriver.*

class DiabetesRiskStratificationTask(
    private val browserDriverConfig: BrowserDriverConfig,
    private val healthSystemAuthentication: HealthSystemAuthentication,
    private val excelFilePath: String
) : ProgressTask {

    override val taskDescription: String = "导入糖尿病分组评估任务"

    override fun execute(): Flow<TaskProgress> {
        val driver = browserDriverConfig.buildWebDriver()
        return HealthExcelListProgressTask(
            driver = driver,
            importer = DiabetesRiskStratificationImporter(
                driver = driver,
                authentication = healthSystemAuthentication
            ),
            excelRowMapper = diabetesRecordMapper(),
            excelFilePath = excelFilePath
        ).execute().onCompletion {
            driver.quit()
        }
    }
}

private class DiabetesRiskStratificationImporter(
    private val driver: WebDriver,
    private val authentication: HealthSystemAuthentication
) : HealthRecordImporter<DiabetesRecord> {

    override fun prepareImport() = driver.run {
        loginHealthSystem(authentication)
        gotoDiabetesRecordListPage()
    }

    override suspend fun importRecord(record: DiabetesRecord) {
        driver.run {
            name("idCard").clearSendKeys(record.id)
            css("button.x-btn-text.query").click()
            doubleClick {
                css("table[class='x-grid3-row-table']")
            }
            xpath("//*[text() = '糖尿病分组']").click()
            firstByName("fixDate")?.clearSendKeys(record.planDate)
            firstByName("fbs")?.clearSendKeys(record.bloodGlucose)
            firstByXpath("//*[text() = '确定(F1)']")?.click()
            delay(500)
            css("button[id='CLOSE']").click()
        }
    }
}