package tech.kotlinhero.autohelper.core.business.diabetes

import org.openqa.selenium.WebDriver
import tech.kotlinhero.autohelper.webdriver.allByCss
import tech.kotlinhero.autohelper.webdriver.css
import tech.kotlinhero.autohelper.webdriver.xpath

fun WebDriver.gotoDiabetesRecordListPage() {
    css(
        "html > body > div:nth-of-type(1) > div > div > div:nth-of-type(1) > ul > li:nth-of-type(2) > a"
    ).click()
    xpath("//a[text()='糖尿病管理']").click()
    css("a[title='糖尿病档案管理']").click()
    allByCss("img.x-form-trigger.x-form-arrow-trigger").getOrNull(0)?.click()
    css("html > body > div:nth-of-type(8) > div > div:nth-of-type(6)").click()
}