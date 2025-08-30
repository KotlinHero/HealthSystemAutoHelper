package tech.kotlinhero.autohelper.excel

import org.apache.poi.ss.usermodel.Sheet
import org.apache.poi.ss.usermodel.WorkbookFactory
import java.io.FileInputStream


fun <T : Any> readExcel(path: String, block: (Sheet) -> T): T {
    return FileInputStream(path).use { inputStream ->
        WorkbookFactory.create(inputStream).use { workbook ->
            block(workbook.getSheetAt(0))
        }
    }
}