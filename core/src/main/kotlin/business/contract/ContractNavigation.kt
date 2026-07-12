package tech.kotlinhero.autohelper.core.business.contract

import org.openqa.selenium.WebDriver
import tech.kotlinhero.autohelper.webdriver.*

fun WebDriver.gotoContractRecordListPage() {
    id("HR").click()
    id("WL_module_B08").click()
    allByCss("img.x-form-trigger.x-form-arrow-trigger").getOrNull(0)?.click()
    css("html > body > div:nth-of-type(8) > div > div:nth-of-type(7)").click()
}

fun WebDriver.clickByPossibleXpathList(xpathList: List<String>) {
    xpathList.forEach { xpathStr ->
        runCatching {
            xpath(xpathStr).click()
        }.onSuccess {
            return
        }
    }
}

fun WebDriver.searchContractRecord(recordId: String) {
    name("idCard").clearSendKeys(recordId)
    css("button.x-btn-text.query").click()
    doubleClick {
        css("table[class='x-grid3-row-table']")
    }
}