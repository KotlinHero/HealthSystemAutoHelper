package tech.kotlinhero.autohelper.excel

import org.apache.poi.ss.usermodel.CellType
import org.apache.poi.ss.usermodel.DateUtil
import org.apache.poi.ss.usermodel.Row
import java.time.format.DateTimeFormatter

operator fun Row.get(oneBasedColumn: Int): String = getDisplayString(oneBasedColumn - 1)

private fun Row.getDisplayString(zeroBasedColumn: Int): String {
    val cell = this.getCell(zeroBasedColumn) ?: return ""
    return when (cell.cellType) {
        CellType.STRING -> cell.stringCellValue
        CellType.NUMERIC -> {
            if (DateUtil.isCellDateFormatted(cell)) {
                DateUtil.getLocalDateTime(cell.numericCellValue).toLocalDate().format(DateTimeFormatter.ISO_LOCAL_DATE)
            } else {
                cell.numericCellValue.let {
                    if (it % 1 == 0.0) {
                        it.toInt().toString()
                    } else {
                        it.toString()
                    }
                }
            }
        }

        CellType.BOOLEAN -> cell.booleanCellValue.toString()
        CellType.FORMULA -> {
            val evaluator = cell.sheet.workbook.creationHelper.createFormulaEvaluator()
            when (evaluator.evaluateFormulaCell(cell)) {
                CellType.STRING -> cell.stringCellValue
                CellType.NUMERIC -> cell.numericCellValue.toString()
                CellType.BOOLEAN -> cell.booleanCellValue.toString()
                CellType.BLANK -> ""
                CellType.ERROR -> ""
                else -> ""
            }
        }

        CellType.BLANK -> ""
        CellType.ERROR -> ""
        else -> ""
    }
}