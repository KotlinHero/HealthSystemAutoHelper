package tech.kotlinhero.autohelper.webdriver

import org.openqa.selenium.WebDriver
import org.openqa.selenium.chrome.ChromeDriver
import org.openqa.selenium.interactions.Interactive

class SafeQuitWebDriver(
    private val driver: ChromeDriver
) : WebDriver by driver, Interactive by driver {

    private var quit = false

    override fun quit() {
        if (!quit) {
            driver.quit()
            quit = true
        }
    }
}