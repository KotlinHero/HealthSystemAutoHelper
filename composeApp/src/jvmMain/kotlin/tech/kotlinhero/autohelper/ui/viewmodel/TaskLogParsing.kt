package tech.kotlinhero.autohelper.ui.viewmodel

import java.time.LocalTime
import java.time.format.DateTimeFormatter

enum class LogLevel { Neutral, Ok, Err, Warn }

/**
 * 运行日志的一行。core 只 emit 纯文本，这里把它拆成「第几行 + 记录 + 结论」，
 * 任务台才能显示成「第 128 行 李桂芝-3301 · 未录入：…」而不是原始日志。
 */
data class TaskLogLine(
    val time: String,
    val level: LogLevel,
    val row: Int?,
    val label: String?,
    val message: String,
    /** 「开始导入：…」这类只为定位当前行、不值得占屏幕的日志 */
    val noise: Boolean = false,
)

/** 失败明细的一行：第几行、哪条记录、什么原因 */
data class TaskFailure(
    val row: Int?,
    val label: String,
    val reason: String,
)

/**
 * 把 core 的日志文本翻译成任务台能用的结构化日志。
 *
 * 已知日志文案（见 core 各任务 emitLog）：
 * - 「正在登录系统准备导入」
 * - 「开始导入：<姓名>-<身份证>」/「导入成功：<姓名>-<身份证>」/「导入失败：<姓名>-<身份证>（<异常>: <原因>）」
 * - 分组评估与健康体检任务只 emit「导入失败:<身份证>-<姓名>（…）」（半角冒号、无「开始导入」）
 *
 * core 的 ProgressUpdate 在「处理完一行之后」发出，且失败也会 +1，
 * 所以收到失败日志时 finishCount 仍是上一行的值，行号取 max(已尝试次数, 已处理数 + 1)。
 */
class TaskLogInterpreter(
    /** 补录时进度号是「本次选中的第几条」，这里把它映射回原始 Excel 行号 */
    private val rowMap: List<Int>? = null,
) {

    private var attempts = 0

    private val collected = mutableListOf<TaskFailure>()

    val failures: List<TaskFailure> get() = collected

    fun reset() {
        attempts = 0
        collected.clear()
    }

    fun interpret(raw: String, handled: Int, time: LocalTime): TaskLogLine {
        val text = raw.trim()
        val stamp = time.format(TIME_FORMAT)
        return when {
            text.startsWith(START_PREFIX) -> {
                attempts += 1
                TaskLogLine(
                    time = stamp,
                    level = LogLevel.Neutral,
                    row = displayRow(attempts),
                    label = text.afterPrefix(START_PREFIX),
                    message = "开始导入",
                    noise = true,
                )
            }
            text.startsWith(SUCCESS_PREFIX) -> TaskLogLine(
                time = stamp,
                level = LogLevel.Ok,
                row = displayRow(attempts),
                label = text.afterPrefix(SUCCESS_PREFIX),
                message = "已保存",
            )
            text.startsWith(FAILURE_PREFIX_FULL) || text.startsWith(FAILURE_PREFIX_HALF) -> {
                // 分组评估 / 健康体检任务用的是半角冒号；两种前缀各剥各的，剥错会把整句当记录
                val body = if (text.startsWith(FAILURE_PREFIX_FULL)) {
                    text.afterPrefix(FAILURE_PREFIX_FULL)
                } else {
                    text.afterPrefix(FAILURE_PREFIX_HALF)
                }
                val split = splitFailure(body)
                val row = displayRow(maxOf(attempts, handled + 1))
                collected += TaskFailure(row = row, label = split.first, reason = split.second)
                TaskLogLine(
                    time = stamp,
                    level = LogLevel.Err,
                    row = row,
                    label = split.first,
                    message = split.second,
                )
            }
            text == "正在登录系统准备导入" -> TaskLogLine(stamp, LogLevel.Neutral, null, null, "正在登录系统")
            text.startsWith("任务取消") -> TaskLogLine(stamp, LogLevel.Warn, null, null, text)
            else -> TaskLogLine(stamp, LogLevel.Neutral, null, null, text)
        }
    }

    private fun displayRow(index: Int): Int? {
        if (index <= 0) return null
        return rowMap?.getOrNull(index - 1) ?: index
    }

    private fun String.afterPrefix(prefix: String): String = removePrefix(prefix).trim().trimStart('：', ':', ' ').trim()

    /** 「姓名-身份证（异常类: 原因）」→ (姓名-身份证, 原因)；没有括号时整段当记录，原因留空 */
    private fun splitFailure(body: String): Pair<String, String> {
        val open = body.indexOf('（')
        if (open < 0) {
            return body.trim() to "未记录原因"
        }
        val label = body.substring(0, open).trim()
        val tail = body.substring(open).trim().trimStart('（').trimEnd('）').trim()
        return label to shortReason(tail)
    }

    private companion object {
        const val START_PREFIX = "开始导入"
        const val SUCCESS_PREFIX = "导入成功"
        const val FAILURE_PREFIX_FULL = "导入失败："
        const val FAILURE_PREFIX_HALF = "导入失败:"
        val TIME_FORMAT: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss")
    }
}

/** 原因只留人能读懂的部分：去掉异常类名前缀，超长截断 */
internal fun shortReason(reason: String): String {
    val trimmed = reason.replace(Regex("^[A-Za-z][A-Za-z0-9_.]*(Exception|Error):\\s*"), "").trim().ifEmpty { reason.trim() }
    return if (trimmed.length <= 42) trimmed else trimmed.take(41) + "…"
}
