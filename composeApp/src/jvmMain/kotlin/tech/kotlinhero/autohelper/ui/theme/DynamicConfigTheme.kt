package tech.kotlinhero.autohelper.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable

@Composable
fun DynamicConfigTheme(content: @Composable () -> Unit) {
    MaterialTheme {
        content()
    }
}