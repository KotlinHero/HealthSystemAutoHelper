package tech.kotlinhero.autohelper.core

import org.openqa.selenium.WebDriver
import tech.kotlinhero.autohelper.webdriver.webDriver

interface BrowserDriverConfig {
    val browserBinaryPath: String
    val driverPath: String
}

data class SimpleBrowserDriverConfig(
    override val browserBinaryPath: String,
    override val driverPath: String
) : BrowserDriverConfig

fun browserDriverConfig(browserBinaryPath: String, driverPath: String): BrowserDriverConfig =
    SimpleBrowserDriverConfig(browserBinaryPath, driverPath)

fun BrowserDriverConfig.buildWebDriver(): WebDriver = webDriver {
    chrome {
        driver(driverPath)
        binary(browserBinaryPath)
        silent()
    }
}
