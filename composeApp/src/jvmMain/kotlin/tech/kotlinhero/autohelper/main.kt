package tech.kotlinhero.autohelper

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import tech.kotlinhero.autohelper.koin.initKoin

fun main() = application {
    initKoin()
    Window(
        onCloseRequest = ::exitApplication,
        title = "HealthSystemAutoHelper",
    ) {
        App()
    }
}