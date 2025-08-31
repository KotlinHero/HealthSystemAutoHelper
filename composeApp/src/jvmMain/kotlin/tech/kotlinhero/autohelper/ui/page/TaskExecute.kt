package tech.kotlinhero.autohelper.ui.page

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.unit.dp
import tech.kotlinhero.autohelper.ui.viewmodel.TaskExecuteViewModel

@Composable
fun TaskExecute(
    taskExecuteViewModel: TaskExecuteViewModel
) {

    val totalCount by taskExecuteViewModel.totalCount
    val finishCount by taskExecuteViewModel.finishCount

    val progress by animateFloatAsState(
        targetValue = if (totalCount == 0) {
            1f
        } else {
            finishCount.toFloat() / totalCount.toFloat()
        },
        animationSpec = tween(durationMillis = 1000, easing = FastOutSlowInEasing)
    )

    Column {
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier.fillMaxWidth().height(10.dp),
            strokeCap = StrokeCap.Butt
        )
    }
}

private fun taskExecuteHeader(isTaskFinish: Boolean): String {
    return if (isTaskFinish) {
        "任务完成"
    } else {
        "任务进行中"
    }
}