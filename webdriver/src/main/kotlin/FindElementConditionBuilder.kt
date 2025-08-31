package tech.kotlinhero.autohelper.webdriver

import org.openqa.selenium.By

@WebDriverDSL
interface FindElementConditionBuilder {
    fun id(id: String): By

    fun name(name: String): By

    fun css(css: String): By

    fun xpath(xpath: String): By
}

internal class DefaultFindElementConditionBuilder : FindElementConditionBuilder {
    override fun id(id: String): By = By.id(id)

    override fun name(name: String): By = By.name(name)

    override fun xpath(xpath: String): By = By.xpath(xpath)

    override fun css(css: String): By = By.cssSelector(css)
}