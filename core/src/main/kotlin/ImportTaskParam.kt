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

    /** 只处理这些数据行（从 1 开始，不含表头），用于补录上一轮未录入的记录；null 表示全部行 */
    val onlyRows: List<Int>? get() = null
}

internal data class DefaultImportTaskParam(
    override val browserBinaryPath: String,
    override val driverPath: String,
    override val username: String,
    override val password: String,
    override val excelPath: String,
    override val onlyRows: List<Int>? = null
) : ImportTaskParam

fun importTaskParam(
    browserBinaryPath: String,
    driverPath: String,
    username: String,
    password: String,
    excelPath: String,
    onlyRows: List<Int>? = null
): ImportTaskParam {
    return DefaultImportTaskParam(
        browserBinaryPath,
        driverPath,
        username,
        password,
        excelPath,
        onlyRows
    )
}
