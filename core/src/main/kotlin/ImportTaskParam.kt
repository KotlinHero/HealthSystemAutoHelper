package tech.kotlinhero.autohelper.core

/**
 * 带 Excel 文件的导入任务参数。
 *
 * 浏览器相关信息复用 [BrowserDriverConfig]（`browserBinaryPath` / `driverPath`），
 * 创建 WebDriver 直接使用 `BrowserDriverConfig.buildWebDriver()`，不再重复实现一份。
 */
interface ImportTaskParam : BrowserDriverConfig {
    val username: String
    val password: String
    val excelPath: String
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
