package tech.kotlinhero.autohelper

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import java.util.logging.Level

fun main() {
    Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
        logger.log(Level.SEVERE, "线程 ${thread.name} 未捕获异常", throwable)
        throwable.printStackTrace()
    }

    application {
        Window(
            onCloseRequest = ::exitApplication,
            title = "卫生信息系统自动助手",
        ) {
            App()
        }
    }
}
