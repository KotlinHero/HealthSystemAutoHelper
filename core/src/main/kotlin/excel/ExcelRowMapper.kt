package tech.kotlinhero.autohelper.core.excel

import org.apache.poi.ss.usermodel.Row

interface ExcelRowMapper<out T> {
    val dropCount: Int
    val sheetIndex: Int
    fun mapRowTo(row: Row): T
}

private class SimpleExcelRowMapper<out T>(
    override val sheetIndex: Int,
    override val dropCount: Int,
    private val mapper: (Row) -> T
) : ExcelRowMapper<T> {
    override fun mapRowTo(row: Row): T = mapper(row)
}

fun <T> excelRowMapper(
    sheetIndex: Int,
    dropCount: Int = 1,
    mapper: (Row) -> T
): ExcelRowMapper<T> = SimpleExcelRowMapper(sheetIndex, dropCount, mapper)