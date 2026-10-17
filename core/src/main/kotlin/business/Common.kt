package tech.kotlinhero.autohelper.core.business

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import org.openqa.selenium.WebDriver
import tech.kotlinhero.autohelper.core.HealthSystemAuthentication
import tech.kotlinhero.autohelper.core.config.AppPreferences
import tech.kotlinhero.autohelper.core.config.HealthSystemWebsiteConfig
import tech.kotlinhero.autohelper.webdriver.css
import tech.kotlinhero.autohelper.webdriver.xpath
import kotlin.time.Duration.Companion.milliseconds

suspend fun WebDriver.loginHealthSystem(
    url: String,
    username: String,
    password: String,
) {
    get(url)
    css("#ext-comp-1001").sendKeys(username)
    css("#pwd").sendKeys(password)
    css("#select-role").click()
    try {
        xpath("//li[text()='责任医生']").click()
        delay(100.milliseconds)
        xpath("//li[text()='责任医生']").click()
    } catch (e: CancellationException) {
        throw e
    } catch (_: Exception) {
        // 角色下拉框尚未展开或选项已默认选中时点击会失败，继续走登录按钮即可
    }
    css("#logon").click()
}

suspend fun WebDriver.loginHealthSystem(
    healthSystemAuthentication: HealthSystemAuthentication,
    url: String = AppPreferences.healthSystemWebsiteUrl,
) {
    loginHealthSystem(url, healthSystemAuthentication.username, healthSystemAuthentication.password)
}

suspend fun WebDriver.loginHealthSystem(
    username: String,
    password: String,
    config: HealthSystemWebsiteConfig = AppPreferences
) {
    loginHealthSystem(config.healthSystemWebsiteUrl, username, password)
}