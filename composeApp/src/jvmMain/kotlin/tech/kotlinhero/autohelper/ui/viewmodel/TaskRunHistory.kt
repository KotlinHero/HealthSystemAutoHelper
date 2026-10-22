package tech.kotlinhero.autohelper.ui.viewmodel

import java.nio.charset.StandardCharsets
import java.util.Base64
import java.util.prefs.Preferences

/**
 * 一次任务的运行结果，卡片第三行的「上次 3 月 12 日 · 131 条 · 全部成功」由它提供。
 *
 * [failedRows] 与 [excelPath] 是为补录服务的：失败后即使关掉程序，
 * 重新打开仍能只重跑那几行，不必手动另存一份 Excel。
 */
data class TaskRunRecord(
    val taskId: String,
    val finishedAtMillis: Long,
    val total: Int,
    val handled: Int,
    val failed: Int,
    val durationMillis: Long,
    val excelPath: String = "",
    val failedRows: List<Int> = emptyList(),
) {
    val allSucceeded: Boolean get() = failed == 0
}

/**
 * 运行记录的本地存档（java.util.prefs，与账号、Chrome 路径同一套存储）。
 * 只保留每个任务最近一次结果——任务台要回答「上次跑到哪」，不是做报表。
 */
class TaskRunHistory {

    private val preferences = Preferences.userNodeForPackage(TaskRunHistory::class.java)

    fun all(): Map<String, TaskRunRecord> {
        val ids = preferences.get("run_ids", "").split(',').filter { it.isNotBlank() }
        return ids.mapNotNull { id ->
            val finishedAt = preferences.getLong(key(id, "at"), 0L)
            if (finishedAt <= 0L) null else id to TaskRunRecord(
                taskId = id,
                finishedAtMillis = finishedAt,
                total = preferences.getInt(key(id, "total"), 0),
                handled = preferences.getInt(key(id, "handled"), 0),
                failed = preferences.getInt(key(id, "failed"), 0),
                durationMillis = preferences.getLong(key(id, "duration"), 0L),
                excelPath = decodePath(preferences.get(key(id, "excel"), "")),
                failedRows = preferences.get(key(id, "rows"), "")
                    .split(',')
                    .mapNotNull { it.trim().toIntOrNull() },
            )
        }.toMap()
    }

    fun record(record: TaskRunRecord) {
        preferences.putLong(key(record.taskId, "at"), record.finishedAtMillis)
        preferences.putInt(key(record.taskId, "total"), record.total)
        preferences.putInt(key(record.taskId, "handled"), record.handled)
        preferences.putInt(key(record.taskId, "failed"), record.failed)
        preferences.putLong(key(record.taskId, "duration"), record.durationMillis)
        preferences.put(key(record.taskId, "excel"), encodePath(record.excelPath))
        preferences.put(key(record.taskId, "rows"), record.failedRows.joinToString(","))
        val ids = preferences.get("run_ids", "").split(',').filter { it.isNotBlank() }.toSet() + record.taskId
        preferences.put("run_ids", ids.joinToString(","))
    }

    private fun key(taskId: String, field: String) = "run_${taskId}_$field"

    // java.util.prefs 在 Windows 上按系统代码页写注册表，中文路径往返会变成乱码（实测「数据」→「鏁版嵁」），
    // 所以 Excel 路径用 Base64（纯 ASCII）存；读不出 Base64 时按原样返回，兼容旧值。
    private fun encodePath(path: String): String =
        if (path.isEmpty()) "" else Base64.getEncoder().encodeToString(path.toByteArray(StandardCharsets.UTF_8))

    private fun decodePath(value: String): String {
        if (value.isEmpty()) return ""
        return runCatching { String(Base64.getDecoder().decode(value), StandardCharsets.UTF_8) }.getOrDefault(value)
    }
}
