package tech.kotlinhero.autohelper.core.business.contract

import com.microsoft.playwright.Page
import tech.kotlinhero.autohelper.webdriver.allByCss
import tech.kotlinhero.autohelper.webdriver.css
import tech.kotlinhero.autohelper.webdriver.id
import tech.kotlinhero.autohelper.webdriver.name
import tech.kotlinhero.autohelper.webdriver.xpath

fun Page.gotoContractRecordListPage() {
    id("HR").click()
    id("WL_module_B08").click()
    allByCss("img.x-form-trigger.x-form-arrow-trigger").firstOrNull()?.click()
    css("html > body > div:nth-of-type(8) > div > div:nth-of-type(7)").click()
}

fun Page.clickByPossibleXpathList(xpathList: List<String>) {
    xpathList.forEach { xpathStr ->
        runCatching {
            xpath(xpathStr).click()
        }.onSuccess {
            return
        }
    }
}

fun Page.searchContractRecord(recordId: String) {
    name("idCard").fill(recordId)
    css("button.x-btn-text.query").click()
    css("table[class='x-grid3-row-table']").first().dblclick()
}
