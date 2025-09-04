package tech.kotlinhero.autohelper

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import java.io.File
import java.util.logging.FileHandler
import java.util.logging.Logger
import java.util.logging.SimpleFormatter

fun main()  {
    Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
        logger.severe("Uncaught exception in thread ${thread.name}: ${throwable.message}")
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

val logger: Logger by lazy {
    val logFile = File(System.getProperty("user.home"), "AuthHelper.log").absolutePath
    val logger = Logger.getLogger("YourAppLogger")
    val fileHandler = FileHandler(logFile, true) // true 表示追加模式
    fileHandler.formatter = SimpleFormatter()
    logger.addHandler(fileHandler)
    logger
}