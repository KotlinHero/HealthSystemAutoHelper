package tech.kotlinhero.autohelper.core

import kotlin.test.Test
import kotlin.test.assertEquals

class RetryRowsTest {

    private val rows = listOf("第1行", "第2行", "第3行", "第4行")

    @Test
    fun `没有指定行时处理全部行`() {
        assertEquals(rows, rows.selectRows(null))
        assertEquals(rows, rows.selectRows(emptyList()))
    }

    @Test
    fun `只保留指定的行并保持原顺序`() {
        assertEquals(listOf("第3行", "第1行"), rows.selectRows(listOf(3, 1)))
    }

    @Test
    fun `行号超出范围时跳过`() {
        assertEquals(listOf("第2行"), rows.selectRows(listOf(2, 9, 0, -1)))
    }
}
