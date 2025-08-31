import tech.kotlinhero.autohelper.core.HEALTH_SYSTEM_WEBSITE_URL
import tech.kotlinhero.autohelper.webdriver.doubleClick
import tech.kotlinhero.autohelper.webdriver.findElement
import tech.kotlinhero.autohelper.webdriver.findElements
import tech.kotlinhero.autohelper.webdriver.webDriver
import kotlin.test.Test

class ImportTaskTest {

    @Test
    fun test() {
        val webDriver = webDriver {
            chrome {
                binary("C:\\Users\\slowp\\Desktop\\chrome-win64\\chrome.exe")
                driver("C:\\Users\\slowp\\Desktop\\chromedriver-win64\\chromedriver.exe")
                silent()
            }
        }
        webDriver.run {
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
            findElement { name("idCard") }.sendKeys("320922196302124471")
            findElement { css("button.x-btn-text.query") }.click()
            doubleClick {
                findElement { css("table[class='x-grid3-row-table']") }
            }
            findElement { xpath("//*[text() = '高血压随访']") }.click()
            findElements {
                css("td.x-grid3-col.x-grid3-cell.x-grid3-td-0.x-grid3-cell-first")
            }.find { element ->
                element.findElement { xpath("./div") }.text == "2025-10-15"
            }?.click()
            findElement { xpath("//*[text() = '确定']") }.click()
            findElement { css("[name*='visitDate']") }.sendKeys("2025-10-15")
        }
    }
}