package tech.kotlinhero.autohelper.core

interface ImportTaskParam {
    val username: String
    val password: String
    val excelPath: String
    val browserExecutablePath: String
}

internal data class DefaultImportTaskParam(
    override val browserExecutablePath: String,
    override val username: String,
    override val password: String,
    override val excelPath: String,
) : ImportTaskParam

fun importTaskParam(
    browserExecutablePath: String,
    username: String,
    password: String,
    excelPath: String,
): ImportTaskParam = DefaultImportTaskParam(
    browserExecutablePath = browserExecutablePath,
    username = username,
    password = password,
    excelPath = excelPath,
)
