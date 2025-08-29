package tech.kotlinhero.autohelper.ui.page

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun TaskHistory() {
    Surface(
        modifier = Modifier.fillMaxSize(),
    ) {
        Text(text = "taskHistory")
    }
}