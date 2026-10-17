package tech.kotlinhero.autohelper

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import java.awt.Dimension
import java.util.logging.Level

fun main() {
    Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
        logger.log(Level.SEVERE, "线程 ${thread.name} 未捕获异常", throwable)
        throwable.printStackTrace()
    }

    application {
        val windowState = rememberWindowState(width = 1200.dp, height = 800.dp)
        Window(
            onCloseRequest = ::exitApplication,
            title = "卫生信息系统自动助手",
            state = windowState,
        ) {
            LaunchedEffect(Unit) {
                window.minimumSize = Dimension(960, 640)
            }
            App()
        }
    }
}
