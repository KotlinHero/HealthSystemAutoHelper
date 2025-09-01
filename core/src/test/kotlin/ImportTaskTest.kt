import tech.kotlinhero.autohelper.core.HEALTH_SYSTEM_WEBSITE_URL
import tech.kotlinhero.autohelper.webdriver.*
import kotlin.test.Test

class ImportTaskTest {

    @Test
    fun test() {
        val webDriver = webDriver {
            chrome {
                binary("C:\\Users\\slowp\\dev\\repository\\HealthSystemAutoHelper\\composeApp\\bin\\test\\chrome-win64\\chrome.exe")
                driver("C:\\Users\\slowp\\dev\\repository\\HealthSystemAutoHelper\\composeApp\\bin\\common\\chromedriver-win64\\chromedriver.exe")
                silent()
            }
        }
        webDriver.run {
            get(HEALTH_SYSTEM_WEBSITE_URL)
            css("#ext-comp-1001").sendKeys("320922197806044437")
            css("#pwd").sendKeys("jp123456")
            css("#select-role").click()
            xpath("//li[text()='责任医生']").click()
            css("#logon").click()
            css("html > body > div:nth-of-type(1) > div > div > div:nth-of-type(1) > ul > li:nth-of-type(2) > a").click()
            xpath("//a[text()='高血压管理']").click()
            css("a[title='高血压档案管理']").click()
            allByCss("img.x-form-trigger.x-form-arrow-trigger").getOrNull(0)?.click()
            css("html > body > div:nth-of-type(8) > div > div:nth-of-type(6)").click()
            name("idCard").sendKeys("320922196302124471")
            css("button.x-btn-text.query").click()
            doubleClick {
                css("table[class='x-grid3-row-table']")
            }
            xpath("//*[text() = '高血压随访']").click()

            allByCss("td.x-grid3-col.x-grid3-cell.x-grid3-td-0.x-grid3-cell-first").find { element ->
                element.findElement { xpath("./div") }.text == "2025-10-15"
            }?.click()
            xpath("//*[text() = '确定']").click()
            css("[name*='visitDate']").sendKeys("2025-10-15")
        }
    }
}