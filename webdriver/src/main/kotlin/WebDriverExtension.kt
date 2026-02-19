package tech.kotlinhero.autohelper.webdriver

import org.openqa.selenium.By
import org.openqa.selenium.WebDriver
import org.openqa.selenium.WebElement
import org.openqa.selenium.interactions.Actions
import org.openqa.selenium.support.ui.ExpectedConditions
import org.openqa.selenium.support.ui.WebDriverWait
import java.time.Duration

inline fun <T> WebDriver.use(block: WebDriver.() -> T): T {
    try {
        return block()
    } finally {
        quit()
    }
}

@DslMarker
annotation class WebDriverDSL

@WebDriverDSL
fun webDriver(block: WebDriverBuilder.() -> Unit): WebDriver = DefaultWebDriverBuilder().apply { block() }.build()

@WebDriverDSL
fun WebDriver.findElement(block: FindElementConditionBuilder.() -> By): WebElement {
    return WebDriverWait(this, Duration.ofSeconds(3)).until(
        ExpectedConditions.elementToBeClickable(
            DefaultFindElementConditionBuilder().block()
        )
    )
}

@WebDriverDSL
fun WebDriver.findElements(block: FindElementConditionBuilder.() -> By): List<WebElement> {
    return WebDriverWait(this, Duration.ofSeconds(5)).until(
        ExpectedConditions.presenceOfAllElementsLocatedBy(
            DefaultFindElementConditionBuilder().block()
        )
    )
}

@WebDriverDSL
fun WebElement.findElement(block: FindElementConditionBuilder.() -> By): WebElement {
    return findElement(DefaultFindElementConditionBuilder().block())
}

@WebDriverDSL
fun WebDriver.doubleClick(element: WebElement) {
    Actions(this).doubleClick(element).perform()
}

@WebDriverDSL
fun WebDriver.doubleClick(block: () -> WebElement) {
    doubleClick(block())
}

@WebDriverDSL
fun WebDriver.css(css: String) = findElement { css(css) }

@WebDriverDSL
fun WebDriver.css(css: String, block: WebElement.() -> Unit) {
    css(css).block()
}

@WebDriverDSL
fun WebDriver.allByCss(css: String) = findElements { css(css) }

@WebDriverDSL
fun WebDriver.xpath(xpath: String) = findElement { xpath(xpath) }

@WebDriverDSL
fun WebDriver.firstByXpath(xpath: String) = findElements { xpath(xpath) }.firstOrNull { it.isDisplayed }

@WebDriverDSL
fun WebDriver.name(name: String) = findElement { name(name) }

@WebDriverDSL
fun WebDriver.firstByName(name: String) = findElements { name(name) }.firstOrNull { it.isDisplayed }

@WebDriverDSL
fun WebDriver.name(name: String, block: WebElement.() -> Unit) {
    name(name).block()
}

@WebDriverDSL
fun WebElement.clearSendKeys(keys: String) {
    click()
    clear()
    click()
    sendKeys(keys)
}