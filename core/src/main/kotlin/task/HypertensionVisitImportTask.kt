package tech.kotlinhero.autohelper.core.task

import org.apache.poi.ss.usermodel.Row
import org.openqa.selenium.WebDriver
import tech.kotlinhero.autohelper.core.HEALTH_SYSTEM_WEBSITE_URL
import tech.kotlinhero.autohelper.core.excel.HypertensionVisitRecord
import tech.kotlinhero.autohelper.excel.readExcel
import tech.kotlinhero.autohelper.webdriver.doubleClick
import tech.kotlinhero.autohelper.webdriver.findElement
import tech.kotlinhero.autohelper.webdriver.findElements
import tech.kotlinhero.autohelper.webdriver.webDriver

data class HypertensionVisitImportTaskParams(
    val username: String,
    val password: String,
    val excelPath: String,
    val browserBinaryPath: String,
    val driverPath: String
)

class HypertensionVisitImportTask(
    private val params: HypertensionVisitImportTaskParams
) {
    fun execute(
        useTotalCount: (totalCount: Int) -> Unit,
        useFinishCount: (currentCount: Int) -> Unit
    ) {
        val driver = params.buildWebDriver()

        readExcel(params.excelPath) {
            val sheet = getSheetAt(1)
            val headRowCount = 1
            useTotalCount(sheet.lastRowNum - headRowCount)

            driver.run {
                get(HEALTH_SYSTEM_WEBSITE_URL)
                findElement { css("#ext-comp-1001") }.sendKeys("320922197806044437")
                findElement { css("#pwd") }.sendKeys("jp123456")
                findElement { css("#select-role") }.click()
                findElement { xpath("//li[text()='责任医生']") }.click()
                findElement { css("#logon") }.click()
                findElement { css("html > body > div:nth-of-type(1) > div > div > div:nth-of-type(1) > ul > li:nth-of-type(2) > a") }.click()
                findElement { xpath("//a[text()='高血压管理']") }.click()
                findElement { css("a[title='高血压档案管理']") }.click()
                findElements { css("img.x-form-trigger.x-form-arrow-trigger") }.getOrNull(0)?.click()
                findElement { css("html > body > div:nth-of-type(8) > div > div:nth-of-type(6)") }.click()

                headRowCount.rangeTo(sheet.lastRowNum).forEach { rowIndex ->
                    val visitRecord = sheet.getRow(rowIndex).toHypertensionVisitRecord()
                    findElement { name("idCard") }.apply {
                        clear()
                        sendKeys(visitRecord.id)
                    }
                    findElement { css("button.x-btn-text.query") }.click()
                    doubleClick {
                        findElement { css("table[class='x-grid3-row-table']") }
                    }
                    findElement { xpath("//*[text() = '高血压随访']") }.click()
                    findElements {
                        css("td.x-grid3-col.x-grid3-cell.x-grid3-td-0.x-grid3-cell-first")
                    }.find { element ->
                        element.findElement { xpath("./div") }.text == visitRecord.planDate
                    }?.click()
                    findElement { xpath("//*[text() = '确定']") }.click()
                    findElement { css("[name*='visitDate']") }.sendKeys(visitRecord.visitDate)
                    useFinishCount(rowIndex)
                }
            }
        }
    }

    private fun Row.toHypertensionVisitRecord(): HypertensionVisitRecord {
        return HypertensionVisitRecord(
            name = this.getCell(0).stringCellValue,
            id = this.getCell(1).stringCellValue,
            planDate = this.getCell(2).stringCellValue,
            visitDate = this.getCell(3).stringCellValue,
            visitWay = this.getCell(4).stringCellValue,
            visitNature = this.getCell(5).stringCellValue,
            currentSymptom = this.getCell(6).stringCellValue
        )
    }

    private fun HypertensionVisitImportTaskParams.buildWebDriver(): WebDriver {
        return webDriver {
            chrome {
                driver(driverPath)
                binary(browserBinaryPath)
                silent()
                headless()
            }
        }
    }
}

