package tech.kotlinhero.autohelper.excel

import org.apache.poi.ss.usermodel.Row
import org.apache.poi.ss.usermodel.WorkbookFactory
import java.io.FileInputStream

fun readExcelSkip(path: String, skipRows: Int = 1, block: (Row) -> Unit) {
    FileInputStream(path).use { inputStream ->
        WorkbookFactory.create(inputStream).use { workbook ->
            val sheet = workbook.getSheetAt(0)
            sheet.iterator().asSequence().drop(skipRows).forEach { row ->
                block(row)
            }
        }
    }
}