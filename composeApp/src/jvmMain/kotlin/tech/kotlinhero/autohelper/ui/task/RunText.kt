package tech.kotlinhero.autohelper.ui.task

import tech.kotlinhero.autohelper.ui.viewmodel.TaskRunRecord
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/** 运行时刻：今天 16:56 / 3 月 12 日 16:42 / 2025 年 3 月 12 日 */
fun formatRunMoment(millis: Long, zone: ZoneId = ZoneId.systemDefault()): String {
    val time = LocalDateTime.ofInstant(Instant.ofEpochMilli(millis), zone)
    val today = LocalDate.now(zone)
    val clock = time.format(DateTimeFormatter.ofPattern("HH:mm"))
    return when {
        time.toLocalDate() == today -> "今天 $clock"
        time.year == today.year -> "${time.monthValue} 月 ${time.dayOfMonth} 日 $clock"
        else -> "${time.year} 年 ${time.monthValue} 月 ${time.dayOfMonth} 日"
    }
}

/** 耗时：14 分 02 秒 / 38 秒 / 1 小时 06 分 */
fun formatDuration(millis: Long): String {
    val totalSeconds = (millis / 1000).coerceAtLeast(0)
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60
    return when {
        hours > 0 -> "$hours 小时 ${minutes.toString().padStart(2, '0')} 分"
        minutes > 0 -> "$minutes 分 ${seconds.toString().padStart(2, '0')} 秒"
        else -> "$seconds 秒"
    }
}

/** 卡片第三行：上次 3 月 12 日 · 131 条 */
fun formatRecordMeta(record: TaskRunRecord?): String? {
    if (record == null) return null
    val moment = formatRunMoment(record.finishedAtMillis).removeSuffix(" ")
    return "上次 $moment · ${record.handled} 条"
}

/** 卡片第三行右侧的状态词：只有出问题才发红 */
fun statusTextOf(record: TaskRunRecord?): String? {
    if (record == null) return null
    return if (record.allSucceeded) "全部成功" else "${record.failed} 条未录入"
}
