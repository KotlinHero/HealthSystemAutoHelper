package tech.kotlinhero.autohelper

import java.io.File
import java.util.logging.FileHandler
import java.util.logging.Handler
import java.util.logging.Level
import java.util.logging.LogRecord
import java.util.logging.Logger
import java.util.logging.SimpleFormatter

/** 全局日志：写入用户主目录下的 AuthHelper.log，超过 1 MB 自动轮转，最多保留 2 个历史文件。 */
val logger: Logger by lazy {
    val logFile = File(System.getProperty("user.home"), "AuthHelper.log")
    Logger.getLogger("AutoHelper").apply {
        useParentHandlers = false
        level = Level.ALL
        addHandler(RotatingFileHandler(logFile))
    }
}

private const val LOG_MAX_BYTES = 1_000_000L
private const val LOG_HISTORY_FILES = 2

/**
 * 按大小轮转的日志 Handler。
 *
 * java.util.logging.FileHandler 的多文件轮转会把文件名变成 `*.log.0`、`*.log.1`，
 * 且只在写入过程中判断大小；这里用固定文件名 + 手动重命名，保证始终是 AuthHelper.log。
 */
internal class RotatingFileHandler(
    private val file: File,
    private val maxBytes: Long = LOG_MAX_BYTES,
    private val maxHistoryFiles: Int = LOG_HISTORY_FILES,
) : Handler() {

    private var delegate: FileHandler = openLogFile()

    @Synchronized
    override fun publish(record: LogRecord) {
        if (file.length() > maxBytes) {
            rotate()
        }
        delegate.publish(record)
    }

    @Synchronized
    override fun flush() {
        delegate.flush()
    }

    @Synchronized
    override fun close() {
        delegate.close()
    }

    private fun openLogFile(): FileHandler = FileHandler(file.absolutePath, true).apply {
        formatter = SimpleFormatter()
    }

    private fun rotate() {
        delegate.close()
        for (index in maxHistoryFiles downTo 2) {
            val older = File("${file.absolutePath}.${index - 1}")
            val oldest = File("${file.absolutePath}.$index")
            if (oldest.exists()) {
                oldest.delete()
            }
            if (older.exists()) {
                older.renameTo(oldest)
            }
        }
        val previous = File("${file.absolutePath}.1")
        if (previous.exists()) {
            previous.delete()
        }
        if (file.exists() && !file.renameTo(previous)) {
            file.delete()
        }
        delegate = openLogFile()
    }
}
