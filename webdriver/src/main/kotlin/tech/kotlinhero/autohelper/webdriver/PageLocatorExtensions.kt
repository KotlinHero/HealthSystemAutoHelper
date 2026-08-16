package tech.kotlinhero.autohelper.webdriver

import com.microsoft.playwright.Locator
import com.microsoft.playwright.Page
import com.microsoft.playwright.options.WaitForSelectorState

fun Page.css(selector: String): Locator = locator(selector)

fun Page.xpath(xpath: String): Locator = locator("xpath=$xpath")

fun Page.name(name: String): Locator =
    locator("[name=${cssEscape(name)}]")

fun Page.id(id: String): Locator =
    locator("#${cssEscape(id)}")

fun Page.allByCss(css: String): List<Locator> {
    val target = locator(css)
    target.first().waitFor(
        Locator.WaitForOptions().setState(WaitForSelectorState.ATTACHED)
    )
    return target.all()
}

fun Page.firstByXpath(xpath: String): Locator? {
    val target = xpath(xpath)
    target.first().waitFor(
        Locator.WaitForOptions().setState(WaitForSelectorState.ATTACHED)
    )
    return target.all().firstOrNull { it.isVisible() }
}

fun Page.firstByName(name: String): Locator? {
    val target = name(name)
    target.first().waitFor(
        Locator.WaitForOptions().setState(WaitForSelectorState.ATTACHED)
    )
    return target.all().firstOrNull { it.isVisible() }
}

fun Page.byNameValue(name: String, value: String): Locator =
    locator("[name=${cssEscape(name)}][value=${cssEscape(value)}]")

fun Locator.fillWhenNotEmpty(value: String) {
    if (value.isNotEmpty()) fill(value)
}

fun Locator.fillWhenInputEmpty(value: String) {
    if (inputValue().isEmpty()) fill(value)
}

fun Locator.selectWhenNotChecked() {
    if (!isChecked) click()
}

private fun cssEscape(value: String): String =
    value.replace("\\", "\\\\").replace("'", "\\'")
