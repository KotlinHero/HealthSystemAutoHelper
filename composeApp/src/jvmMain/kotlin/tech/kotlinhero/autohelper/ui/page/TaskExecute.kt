package tech.kotlinhero.autohelper.ui.page

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
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
    val totalCount by taskExecuteViewModel.totalCount.collectAsState()
    val finishCount by taskExecuteViewModel.finishCount.collectAsState()
    val taskDescription by taskExecuteViewModel.taskDescription.collectAsState()
    val taskLog by taskExecuteViewModel.taskLog.collectAsState()
    val taskState by taskExecuteViewModel.taskState.collectAsState()

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
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            taskExecuteViewModel.cancelCurrentTask()
                        }
                    ) {
                        Text("取消任务")
                    }
                    Text(
                        modifier = Modifier.padding(8.dp),
                        text = "$taskDescription ${taskState.description}",
                        fontSize = 20.sp
                    )
                    Text(
                        modifier = Modifier.padding(8.dp),
                        text = "$finishCount / $totalCount",
                        fontSize = 20.sp
                    )
                    Row(
                        modifier = Modifier.height(50.dp)
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
            TaskExecuteLog(taskLog)
        }
    }
}

@Composable
fun TaskExecuteLog(taskLog: List<String>) {
    Card(
        modifier = Modifier
            .fillMaxSize()
            .padding(4.dp)
    ) {
        LazyColumn {
            items(taskLog) { log ->
                Text(
                    text = log,
                    modifier = Modifier.padding(4.dp)
                )
            }
        }
    }
}