import tech.kotlinhero.autohelper.core.extension.retry
import kotlin.test.Test
import kotlin.test.assertTrue

class ResultExtensionTest {
    @Test
    fun `test retry when success`() {
        var times = 0
        val result = retry {
            if (times == 2) {
                return@retry
            } else {
                times++
                throw Exception("test")
            }
        }
        assertTrue { result.isSuccess }
    }

    @Test
    fun `test retry when failure`() {
        var times = 0
        val result = retry {
            if (times == 4) {
                return@retry
            } else {
                times++
                throw Exception("test")
            }
        }
        assertTrue { result.isFailure }
    }
}