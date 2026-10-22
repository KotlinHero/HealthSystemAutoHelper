package tech.kotlinhero.autohelper.core

/**
 * 补录未录入的记录：只保留 Excel 中指定的数据行（从 1 开始，不含表头）。
 *
 * 任务台运行失败后，会记住未录入的行号。补录时把行号传进来，任务只处理这几行，
 * 进度条按「选中的行数」计数，其余行不会重跑。
 *
 * 传 null 或空列表表示处理全部行；行号超出范围（Excel 被改过）时跳过该行。
 */
fun <T> List<T>.selectRows(onlyRows: List<Int>?): List<T> =
    if (onlyRows.isNullOrEmpty()) this else onlyRows.mapNotNull { getOrNull(it - 1) }
