package tech.kotlinhero.autohelper.core

import org.openqa.selenium.WebDriver
import tech.kotlinhero.autohelper.core.config.AppPreferences
import tech.kotlinhero.autohelper.core.config.HealthSystemWebsiteConfig
import tech.kotlinhero.autohelper.webdriver.css
import tech.kotlinhero.autohelper.webdriver.xpath

interface HealthSystemImportTask {
    fun WebDriver.loginHealthSystem(
        username: String,
        password: String,
        config: HealthSystemWebsiteConfig = AppPreferences
    ) {
        get(config.healthSystemWebsiteUrl)
        css("#ext-comp-1001").sendKeys(username)
        css("#pwd").sendKeys(password)
        css("#select-role").click()
        xpath("//li[text()='责任医生']").click()
        css("#logon").click()
    }
}

internal class HealthSystemImportTaskInstance : HealthSystemImportTask

internal fun healthSystemImportTask(block: HealthSystemImportTask.() -> Unit) {
    HealthSystemImportTaskInstance().block()
}