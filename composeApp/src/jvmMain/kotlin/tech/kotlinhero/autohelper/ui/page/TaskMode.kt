package tech.kotlinhero.autohelper.ui.page

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import tech.kotlinhero.autohelper.core.TaskMode
import tech.kotlinhero.autohelper.ui.component.ModeCard

@Composable
fun TaskMode() {
    Surface(
        modifier = Modifier.fillMaxSize(),
    ) {
        Column {
            TaskMode.entries.forEach {
                ModeCard(mode = it)
            }
        }
    }
}

@Composable
fun TaskModeItem() {
    var showParamsDialog by remember { mutableStateOf(false) }

}