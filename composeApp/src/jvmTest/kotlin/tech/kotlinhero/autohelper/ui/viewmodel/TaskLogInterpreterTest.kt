package tech.kotlinhero.autohelper.ui.viewmodel

import java.time.LocalTime
import kotlin.test.Test
import kotlin.test.assertEquals

class TaskLogInterpreterTest {

    private val time: LocalTime = LocalTime.of(10, 24, 5)

    @Test
    fun `整份 Excel 运行时进度号就是 Excel 行号`() {
        val interpreter = TaskLogInterpreter()
        val started = interpreter.interpret("开始导入：王秀兰-110101", handled = 0, time = time)
        assertEquals(1, started.row)
        val failed = interpreter.interpret("导入失败：王秀兰-110101（TimeoutException: 档案不存在）", handled = 0, time = time)
        assertEquals(LogLevel.Err, failed.level)
        assertEquals(1, failed.row)
        assertEquals("王秀兰-110101", failed.label)
        assertEquals("档案不存在", failed.message)
        assertEquals(1, interpreter.failures.size)
        assertEquals(1, interpreter.failures.first().row)
    }

    @Test
    fun `补录时行号映射回原始 Excel 行`() {
        val interpreter = TaskLogInterpreter(rowMap = listOf(42, 87, 103))
        interpreter.interpret("开始导入：李桂芝-A", handled = 0, time = time)
        interpreter.interpret("导入成功：李桂芝-A", handled = 0, time = time)
        val started = interpreter.interpret("开始导入：周强-B", handled = 1, time = time)
        assertEquals(87, started.row)
        val failed = interpreter.interpret("导入失败：周强-B（IllegalStateException: 档案不存在）", handled = 1, time = time)
        assertEquals(87, failed.row)
    }

    @Test
    fun `只有失败日志的任务用已处理数推行号`() {
        val interpreter = TaskLogInterpreter(rowMap = listOf(42, 87, 103))
        val line = interpreter.interpret("导入失败:110101-王秀兰（IllegalStateException: 身份证重复）", handled = 0, time = time)
        assertEquals(42, line.row)
        assertEquals("110101-王秀兰", line.label)
        assertEquals("身份证重复", line.message)
    }
}
