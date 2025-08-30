package tech.kotlinhero.autohelper.core

import tech.kotlinhero.autohelper.excel.readExcel

class ExcelImportTask(
    private val param: ExcelImportParam
) : IndexExecuteTask {

    override val totalCount: Int
        get() = TODO("Not yet implemented")

    override fun execute(block: (index: Int) -> Unit) {
        readExcel(param.filePath) { sheet ->
            sheet.lastRowNum - 1
            sheet.drop(1)
        }
    }
}