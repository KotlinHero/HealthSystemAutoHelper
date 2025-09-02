package tech.kotlinhero.autohelper.core

import org.openqa.selenium.WebDriver
import tech.kotlinhero.autohelper.webdriver.css
import tech.kotlinhero.autohelper.webdriver.xpath

interface HealthSystemImportTask {
    fun WebDriver.loginHealthSystem(username: String, password: String) {
        get(HEALTH_SYSTEM_WEBSITE_URL)
        css("#ext-comp-1001").sendKeys(username)
        css("#pwd").sendKeys(password)
        css("#select-role").click()
        xpath("//li[text()='责任医生']").click()
        css("#logon").click()
    }
}

internal class HealthSystemImportTaskInstance : HealthSystemImportTask

fun healthSystemImportTask(block: HealthSystemImportTask.() -> Unit) {
    HealthSystemImportTaskInstance().block()
}