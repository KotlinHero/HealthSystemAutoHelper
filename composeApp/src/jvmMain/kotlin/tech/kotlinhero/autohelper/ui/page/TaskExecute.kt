package tech.kotlinhero.autohelper.ui.page

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt
import tech.kotlinhero.autohelper.core.TaskState
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
        } ?: 0f,
        animationSpec = tween(durationMillis = 1000, easing = FastOutSlowInEasing),
    )
    val hasTask = taskDescription.isNotBlank()
    Surface(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(2.dp),
                ) {
                    Text(
                        text = taskDescription.ifBlank { "任务执行" },
                        style = MaterialTheme.typography.headlineSmall,
                    )
                    Text(
                        text = if (hasTask) taskState.description else "尚未启动任务",
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (taskState == TaskState.Failed) {
                            MaterialTheme.colorScheme.error
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        },
                    )
                }
                if (taskState.isActive) {
                    Button(
                        enabled = taskState == TaskState.Running,
                        onClick = { taskExecuteViewModel.cancelCurrentTask() },
                    ) {
                        Text(if (taskState == TaskState.Cancelling) "取消中" else "取消任务")
                    }
                }
            }
            OutlinedCard(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.Bottom,
                    ) {
                        Text(
                            text = "$finishCount / $totalCount",
                            style = MaterialTheme.typography.headlineSmall,
                        )
                        Text(
                            modifier = Modifier.weight(1f),
                            text = if (totalCount > 0) "${(progress * 100).roundToInt()}%" else "—",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary,
                            textAlign = androidx.compose.ui.text.style.TextAlign.End,
                        )
                    }
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant,
                        strokeCap = StrokeCap.Round,
                    )
                }
            }
            OutlinedCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    Text(
                        text = "运行日志",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(start = 16.dp, top = 12.dp, bottom = 8.dp),
                    )
                    HorizontalDivider()
                    if (taskLog.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = "任务开始后日志会显示在这里",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    } else {
                        val listState = rememberLazyListState()
                        LaunchedEffect(taskLog.size) {
                            listState.animateScrollToItem(taskLog.lastIndex)
                        }
                        LazyColumn(
                            state = listState,
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(12.dp),
                            verticalArrangement = Arrangement.spacedBy(2.dp),
                        ) {
                            items(taskLog) { log ->
                                Text(
                                    text = log,
                                    style = MaterialTheme.typography.bodySmall,
                                    fontFamily = FontFamily.Monospace,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
