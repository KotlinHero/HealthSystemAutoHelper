package tech.kotlinhero.autohelper.excel

import org.apache.poi.ss.usermodel.CellType
import org.apache.poi.ss.usermodel.DateUtil
import org.apache.poi.ss.usermodel.Row
import java.math.BigDecimal
import java.time.format.DateTimeFormatter

operator fun Row.get(oneBasedColumn: Int): String = getDisplayString(oneBasedColumn - 1)

private fun Row.getDisplayString(zeroBasedColumn: Int): String {
    val cell = this.getCell(zeroBasedColumn) ?: return ""
    return when (cell.cellType) {
        CellType.STRING -> cell.stringCellValue
        CellType.NUMERIC -> {
            if (DateUtil.isCellDateFormatted(cell)) {
                formatDateCell(cell.numericCellValue)
            } else {
                formatNumericCell(cell.numericCellValue)
            }
        }

        CellType.BOOLEAN -> cell.booleanCellValue.toString()
        CellType.FORMULA -> {
            val evaluator = cell.sheet.workbook.creationHelper.createFormulaEvaluator()
            when (evaluator.evaluateFormulaCell(cell)) {
                CellType.STRING -> cell.stringCellValue
                CellType.NUMERIC -> formatNumericCell(cell.numericCellValue)
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

private fun formatDateCell(value: Double): String =
    DateUtil.getLocalDateTime(value).toLocalDate().format(DateTimeFormatter.ISO_LOCAL_DATE)

/**
 * 把数字单元格格式化为普通文本：不使用科学计数法，并去掉无意义的尾随零。
 *
 * 之前的实现用 `Double.toInt()` 输出整数，超出 Int 范围时会饱和成 2147483647；
 * 公式分支又用 `numericCellValue.toString()`，会得到 `1.7E7` 这类格式。
 * 注意：Excel 以“常规”格式存储的数字本身只有双精度精度，18 位身份证等长数字
 * 仍应在表格里以文本格式保存，本函数只保证输出格式稳定、不截断成错误整数。
 */
private fun formatNumericCell(value: Double): String =
    BigDecimal.valueOf(value).stripTrailingZeros().toPlainString()
