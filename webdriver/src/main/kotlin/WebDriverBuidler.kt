package tech.kotlinhero.autohelper.webdriver

import org.openqa.selenium.WebDriver
import org.openqa.selenium.chrome.ChromeDriver
import org.openqa.selenium.chrome.ChromeDriverService
import org.openqa.selenium.chrome.ChromeOptions

@WebDriverDSL
interface WebDriverBuilder {
    fun chrome(block: ChromeDriverBuilder.() -> Unit)
}

internal class DefaultWebDriverBuilder(
    private val chromeDriverBuilder: DefaultChromeDriverBuilder = DefaultChromeDriverBuilder()
) : WebDriverBuilder {
    override fun chrome(block: ChromeDriverBuilder.() -> Unit) {
        chromeDriverBuilder.block()
    }

    fun build(): WebDriver = chromeDriverBuilder.build()
}


@WebDriverDSL
interface ChromeDriverBuilder {
    fun driver(path: String)
    fun binary(path: String)
    fun silent()
    fun headless()
}

internal class DefaultChromeDriverBuilder(
    private val serviceBuilder: ChromeDriverService.Builder = ChromeDriverService.Builder(),
    private val options: ChromeOptions = ChromeOptions()
) : ChromeDriverBuilder {

    override fun driver(path: String) {
        System.setProperty("webdriver.chrome.driver", path)
    }

    override fun binary(path: String) {
        options.setBinary(path)
    }

    override fun silent() {
        serviceBuilder.withSilent(true)
    }

    override fun headless() {
        options.addArguments("--headless=new")
    }

    private fun ChromeOptions.addDefaultArguments() {
        addArguments("--remote-allow-origins=*")
        addArguments("--window-size=1920,1080")
    }

    fun build(): WebDriver = SafeQuitWebDriver(
        ChromeDriver(
            serviceBuilder.build(),
            options.apply { addDefaultArguments() }
        )
    )
}