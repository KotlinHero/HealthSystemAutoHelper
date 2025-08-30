package tech.kotlinhero.autohelper.ui.component

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import tech.kotlinhero.autohelper.core.TaskModeOverview
import tech.kotlinhero.autohelper.ui.viewmodel.TaskViewModel

@Composable
fun ModeCard(
    modifier: Modifier = Modifier.padding(5.dp),
    mode: TaskModeOverview,
    taskViewModel: TaskViewModel
) {
    Card(
        modifier = modifier
            .height(50.dp)
            .fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier.fillMaxSize()
        ) {
            Text(
                modifier = Modifier
                    .align(Alignment.CenterVertically)
                    .padding(5.dp),
                text = mode.description
            )
            Box(
                modifier = Modifier.fillMaxSize()
            ) {
                Button(
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .padding(5.dp),
                    onClick = {
                        taskViewModel.executeTask()
                    }
                ) {
                    Text(text = "创建任务")
                }
            }
        }
    }
}