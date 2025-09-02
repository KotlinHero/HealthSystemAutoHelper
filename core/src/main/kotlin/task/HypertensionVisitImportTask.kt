package tech.kotlinhero.autohelper.core.task

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.openqa.selenium.WebDriver
import tech.kotlinhero.autohelper.core.ExecuteScope
import tech.kotlinhero.autohelper.core.HEALTH_SYSTEM_WEBSITE_URL
import tech.kotlinhero.autohelper.core.IndexLogExecuteTask
import tech.kotlinhero.autohelper.core.excel.HypertensionVisitRecord
import tech.kotlinhero.autohelper.core.excel.toHypertensionVisitRecord
import tech.kotlinhero.autohelper.core.healthSystemImportTask
import tech.kotlinhero.autohelper.excel.readExcel
import tech.kotlinhero.autohelper.webdriver.*

data class HypertensionVisitImportTaskParams(
    val username: String,
    val password: String,
    val excelPath: String,
    val browserBinaryPath: String,
    val driverPath: String
)

class HypertensionVisitImportTask(
    private val params: HypertensionVisitImportTaskParams,
    override val taskDescription: String = "导入高血压随访"
) : IndexLogExecuteTask {

    override suspend fun execute(
        block: ExecuteScope.() -> Unit
    ) = withContext(Dispatchers.Default) {
        val executeScope = ExecuteScope().apply { block() }
        val driver = params.buildWebDriver()

        readExcel(params.excelPath) {
            val sheet = getSheetAt(1)
            val headRowCount = 1
            executeScope.onTotalCountAccessible(sheet.lastRowNum)

            driver.run {
                prepareImport()
                sheet.drop(headRowCount).forEachIndexed { rowIndex, row ->
                    importHyperVisitRecord(row.toHypertensionVisitRecord())
                    executeScope.onProgressUpdate(rowIndex + 1)
                }
            }
        }
    }

    private fun WebDriver.importHyperVisitRecord(visitRecord: HypertensionVisitRecord) {
        name("idCard") {
            clear()
            sendKeys(visitRecord.id)
        }
        css("button.x-btn-text.query").click()
        doubleClick {
            css("table[class='x-grid3-row-table']")
        }
        xpath("//*[text() = '高血压随访']").click()
        allByCss("td.x-grid3-col.x-grid3-cell.x-grid3-td-0.x-grid3-cell-first").find { element ->
            element.findElement { xpath("./div") }.text == visitRecord.planDate
        }?.click()
        xpath("//*[text() = '确定']").click()
        css("[name*='visitDate']").sendKeys(visitRecord.visitDate)
    }

    private fun WebDriver.prepareImport() {
        get(HEALTH_SYSTEM_WEBSITE_URL)
        healthSystemImportTask { loginHealthSystem(params.username, params.password) }
        css(
            "html > body > div:nth-of-type(1) > div > div > div:nth-of-type(1) > ul > li:nth-of-type(2) > a"
        ).click()
        xpath("//a[text()='高血压管理']").click()
        css("a[title='高血压档案管理']").click()
        allByCss("img.x-form-trigger.x-form-arrow-trigger").getOrNull(0)?.click()
        css("html > body > div:nth-of-type(8) > div > div:nth-of-type(6)").click()
    }

    private fun HypertensionVisitImportTaskParams.buildWebDriver(): WebDriver {
        return webDriver {
            chrome {
                driver(driverPath)
                binary(browserBinaryPath)
                silent()
            }
        }
    }
}