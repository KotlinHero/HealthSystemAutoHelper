package tech.kotlinhero.autohelper.core.business

import com.microsoft.playwright.Page
import kotlinx.coroutines.delay
import tech.kotlinhero.autohelper.core.HealthSystemAuthentication
import tech.kotlinhero.autohelper.core.config.AppPreferences
import tech.kotlinhero.autohelper.core.config.HealthSystemWebsiteConfig
import tech.kotlinhero.autohelper.webdriver.css
import tech.kotlinhero.autohelper.webdriver.xpath
import kotlin.time.Duration.Companion.milliseconds

suspend fun Page.loginHealthSystem(
    url: String,
    username: String,
    password: String,
) {
    navigate(url)
    css("#ext-comp-1001").fill(username)
    css("#pwd").fill(password)
    css("#select-role").click()
    runCatching {
        xpath("//li[text()='责任医生']").first().click()
        delay(100.milliseconds)
        xpath("//li[text()='责任医生']").first().click()
    }
    css("#logon").click()
}

suspend fun Page.loginHealthSystem(
    healthSystemAuthentication: HealthSystemAuthentication,
    url: String = AppPreferences.healthSystemWebsiteUrl,
) {
    loginHealthSystem(url, healthSystemAuthentication.username, healthSystemAuthentication.password)
}

suspend fun Page.loginHealthSystem(
    username: String,
    password: String,
    config: HealthSystemWebsiteConfig = AppPreferences,
) {
    loginHealthSystem(config.healthSystemWebsiteUrl, username, password)
}
