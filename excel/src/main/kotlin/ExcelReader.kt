package tech.kotlinhero.autohelper.excel

import org.apache.poi.ss.usermodel.Workbook
import org.apache.poi.ss.usermodel.WorkbookFactory
import java.io.FileInputStream

inline fun readExcel(path: String, block: Workbook.() -> Unit) {
    FileInputStream(path).use { inputStream ->
        WorkbookFactory.create(inputStream).use { workbook ->
            block(workbook)
        }
    }
}