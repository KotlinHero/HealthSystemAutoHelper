package tech.kotlinhero.autohelper.ui.page

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import io.github.vinceglb.filekit.dialogs.compose.rememberFilePickerLauncher
import io.github.vinceglb.filekit.path
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import tech.kotlinhero.autohelper.ui.viewmodel.TaskViewModel

@Composable
fun TaskExecute(
    taskViewModel: TaskViewModel = viewModel { TaskViewModel() }
) {
    val hasTaskExecuting by taskViewModel.hasTaskExecuting

    if (!hasTaskExecuting) {
        Text(text = "暂无任务执行")
    } else {

    }

    var isTaskFinish by remember { mutableStateOf(true) }
    var finishCount by remember { mutableStateOf(0) }
    var filename by remember { mutableStateOf("未选择文件") }
    val filePicker = rememberFilePickerLauncher { file ->
        filename = file?.path ?: "选择文件错误"
    }
    val scope = rememberCoroutineScope()

    val progress by animateFloatAsState(
        targetValue = finishCount / 100f,
        animationSpec = tween(durationMillis = 1000, easing = FastOutSlowInEasing)
    )

    Column {
        Text(taskExecuteHeader(isTaskFinish))
        Text("请勿关闭应用")
        Text(filename)
        Button(
            onClick = { filePicker.launch() }
        ) {
            Text("选择文件")
        }
        Row {
            Box(modifier = Modifier.weight(1f)) {
                LinearProgressIndicator(
                    progress = progress,
                    modifier = Modifier.fillMaxWidth().height(10.dp),
                    strokeCap = StrokeCap.Butt
                )
            }
            Text("$finishCount/100")
            Button(onClick = {
                scope.launch {
                    while (finishCount < 100) {
                        delay(1000)
                        finishCount++
                    }
                }
            }) {
                Text("start")
            }
            Button(onClick = {
                scope.launch {
                    finishCount = 0
                }
            }) {
                Text("clear")
            }
        }
    }
}

private fun taskExecuteHeader(isTaskFinish: Boolean): String {
    return if (isTaskFinish) {
        "任务完成"
    } else {
        "任务进行中"
    }
}