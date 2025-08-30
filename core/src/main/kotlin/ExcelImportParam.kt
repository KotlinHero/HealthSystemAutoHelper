package tech.kotlinhero.autohelper.core

data class ExcelImportParam(
    val filePath: String,
    val account: Account
) {
    data class Account(
        val username: String,
        val password: String
    )
}
