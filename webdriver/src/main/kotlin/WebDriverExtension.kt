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


fun webDriver(block: WebDriverBuilder.() -> Unit): WebDriver = DefaultWebDriverBuilder().apply { block() }.build()


fun WebDriver.findElement(block: FindElementConditionBuilder.() -> By): WebElement {
    return WebDriverWait(this, Duration.ofSeconds(5)).until(
        ExpectedConditions.elementToBeClickable(
            DefaultFindElementConditionBuilder().block()
        )
    ).also {
        Actions(this).moveToElement(it).perform()
    }
}


fun WebDriver.findElements(block: FindElementConditionBuilder.() -> By): List<WebElement> {
    return WebDriverWait(this, Duration.ofSeconds(5)).until(
        ExpectedConditions.presenceOfAllElementsLocatedBy(
            DefaultFindElementConditionBuilder().block()
        )
    )
}


fun WebElement.findElement(block: FindElementConditionBuilder.() -> By): WebElement {
    return findElement(DefaultFindElementConditionBuilder().block())
}


fun WebDriver.doubleClick(element: WebElement) {
    Actions(this).doubleClick(element).perform()
}


fun WebDriver.doubleClick(block: () -> WebElement) {
    doubleClick(block())
}


fun WebDriver.css(css: String) = findElement { css(css) }


fun WebDriver.css(css: String, block: WebElement.() -> Unit) {
    css(css).block()
}


fun WebDriver.allByCss(css: String) = findElements { css(css) }


fun WebDriver.xpath(xpath: String) = findElement { xpath(xpath) }


fun WebDriver.firstByXpath(xpath: String) = findElements { xpath(xpath) }.firstOrNull { it.isDisplayed }


fun WebDriver.name(name: String) = findElement { name(name) }


fun WebDriver.firstByName(name: String) = findElements { name(name) }.firstOrNull { it.isDisplayed }


fun WebDriver.name(name: String, block: WebElement.() -> Unit) {
    name(name).block()
}


fun WebDriver.xpathByNameWithValue(name: String, value: String): WebElement =
    xpath("//input[@name='${name}'][@value='${value}']")


fun WebDriver.id(id: String) = findElement { id(id) }


fun WebElement.clearSendKeys(keys: String) {
    click()
    clear()
    click()
    sendKeys(keys)
}


fun WebElement.selectWhenNotSelected() {
    if (!isSelected) click()
}


fun WebElement.sendKeysWhenInputEmpty(keys: String) {
    if (getAttribute("value")?.isEmpty() ?: false) sendKeys(keys)
}


fun WebElement.sendKeysWhenValueNotEmpty(keys: String) {
    if (keys.isNotEmpty()) clearSendKeys(keys)
}