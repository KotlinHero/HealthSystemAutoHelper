package tech.kotlinhero.autohelper.ui.page

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Card
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import tech.kotlinhero.autohelper.ui.viewmodel.TaskExecuteViewModel

@Composable
fun TaskExecute(
    taskExecuteViewModel: TaskExecuteViewModel
) {
    val hasTaskExecuting by taskExecuteViewModel.hasTaskExecuting
    if (hasTaskExecuting) {
        Surface(
            modifier = Modifier.fillMaxSize()
        ) {
            Box {
                Text(
                    modifier = Modifier.align(Alignment.Center),
                    text = "暂无任务执行",
                    fontSize = 50.sp,
                )
            }
        }
        return
    }

    val totalCount by taskExecuteViewModel.totalCount
    val finishCount by taskExecuteViewModel.finishCount

    val progress by animateFloatAsState(
        targetValue = totalCount.takeUnless { it == 0 }?.let {
            finishCount.toFloat() / it.toFloat()
        } ?: 1f,
        animationSpec = tween(durationMillis = 1000, easing = FastOutSlowInEasing)
    )
    Surface(
        modifier = Modifier.fillMaxSize()
    ) {
        Column {
            Card(
                modifier = Modifier.padding(4.dp)
                    .fillMaxWidth()
                    .height(100.dp)
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier.padding(8.dp)
                    ) {
                        Text(
                            text = "导入高血压随访任务执行中",
                            fontSize = 20.sp
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxHeight()
                    ) {
                        Box(
                            modifier = Modifier.padding(8.dp)
                                .align(Alignment.Bottom)
                        ) {
                            LinearProgressIndicator(
                                progress = { progress },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(10.dp),
                                strokeCap = StrokeCap.Butt
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TaskExecuteLog() {

}