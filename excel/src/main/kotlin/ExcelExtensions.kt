package tech.kotlinhero.autohelper.excel

import org.apache.poi.ss.usermodel.Row

operator fun Row.get(column: Int): String? {
    return this.getCell(column)?.stringCellValue
}