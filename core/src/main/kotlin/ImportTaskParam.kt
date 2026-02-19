package tech.kotlinhero.autohelper.core

import org.openqa.selenium.WebDriver
import tech.kotlinhero.autohelper.webdriver.webDriver

interface ImportTaskParam {
    val username: String
    val password: String
    val excelPath: String
    val browserBinaryPath: String
    val driverPath: String
}

internal data class DefaultImportTaskParam(
    override val browserBinaryPath: String,
    override val driverPath: String,
    override val username: String,
    override val password: String,
    override val excelPath: String
) : ImportTaskParam

fun importTaskParam(
    browserBinaryPath: String,
    driverPath: String,
    username: String,
    password: String,
    excelPath: String
): ImportTaskParam {
    return DefaultImportTaskParam(
        browserBinaryPath,
        driverPath,
        username,
        password,
        excelPath
    )
}

fun ImportTaskParam.buildWebDriver(): WebDriver = webDriver {
    chrome {
        driver(driverPath)
        binary(browserBinaryPath)
        silent()
    }
}