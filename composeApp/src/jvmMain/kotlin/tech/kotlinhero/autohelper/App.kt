package tech.kotlinhero.autohelper

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import tech.kotlinhero.autohelper.core.TaskState
import tech.kotlinhero.autohelper.ui.page.Settings
import tech.kotlinhero.autohelper.ui.page.TaskExecute
import tech.kotlinhero.autohelper.ui.page.TaskMode
import tech.kotlinhero.autohelper.ui.route.NavigationRouter
import tech.kotlinhero.autohelper.ui.route.RouterItem
import tech.kotlinhero.autohelper.ui.route.TaskExecuteRouterItem
import tech.kotlinhero.autohelper.ui.theme.DynamicConfigTheme
import tech.kotlinhero.autohelper.ui.viewmodel.TaskExecuteViewModel

@Composable
fun App(
    taskExecuteViewModel: TaskExecuteViewModel = viewModel { TaskExecuteViewModel() }
) {
    val navController = rememberNavController()
    var currentPage: RouterItem by remember { mutableStateOf(NavigationRouter.TaskMode) }
    val taskState by taskExecuteViewModel.taskState.collectAsState()
    val totalCount by taskExecuteViewModel.totalCount.collectAsState()
    val finishCount by taskExecuteViewModel.finishCount.collectAsState()

    DynamicConfigTheme {
        Row(modifier = Modifier.fillMaxSize()) {
            NavigationRail(
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                header = {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.padding(top = 20.dp, bottom = 8.dp),
                    ) {
                        Surface(
                            shape = MaterialTheme.shapes.medium,
                            color = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary,
                        ) {
                            Icon(
                                imageVector = Icons.Filled.MedicalServices,
                                contentDescription = null,
                                modifier = Modifier.padding(8.dp),
                            )
                        }
                        Text(
                            text = "自动助手",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                },
            ) {
                val navigationItems = NavigationRouter.entries + TaskExecuteRouterItem
                navigationItems.forEach { item ->
                    NavigationRailItem(
                        icon = { Icon(item.icon, contentDescription = item.label) },
                        label = { Text(item.label) },
                        selected = currentPage == item,
                        onClick = {
                            currentPage = item
                            navController.navigate(item.route) {
                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                    )
                }
                Spacer(modifier = Modifier.weight(1f))
                TaskProgressBadge(
                    taskState = taskState,
                    finishCount = finishCount,
                    totalCount = totalCount,
                    onClick = {
                        currentPage = TaskExecuteRouterItem
                        navController.navigate(TaskExecuteRouterItem.route) {
                            launchSingleTop = true
                        }
                    },
                )
            }
            NavHost(
                navController = navController,
                startDestination = NavigationRouter.TaskMode.route,
                modifier = Modifier.weight(1f).fillMaxHeight(),
            ) {
                composable(NavigationRouter.TaskMode.route) {
                    TaskMode(
                        taskExecuteViewModel = taskExecuteViewModel,
                        onTaskStarted = {
                            currentPage = TaskExecuteRouterItem
                            navController.navigate(TaskExecuteRouterItem.route) {
                                launchSingleTop = true
                            }
                        },
                    )
                }
                composable(NavigationRouter.Settings.route) {
                    Settings()
                }
                composable(TaskExecuteRouterItem.route) {
                    TaskExecute(taskExecuteViewModel)
                }
            }
        }
    }
}

/**
 * 导航栏底部常驻的进度徽标：任务运行中或已有进度时显示，点击进入任务执行页。
 */
@Composable
private fun TaskProgressBadge(
    taskState: TaskState,
    finishCount: Int,
    totalCount: Int,
    onClick: () -> Unit,
) {
    if (!taskState.isActive && totalCount == 0) {
        return
    }
    val progress = if (totalCount == 0) 0f else finishCount.toFloat() / totalCount.toFloat()
    Column(
        modifier = Modifier
            .padding(horizontal = 6.dp, vertical = 12.dp)
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.small)
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        if (taskState.isActive && totalCount == 0) {
            // 任务刚启动、总数未知时用不确定进度
            CircularProgressIndicator(
                modifier = Modifier.size(28.dp),
                strokeWidth = 3.dp,
            )
        } else {
            CircularProgressIndicator(
                progress = { progress },
                modifier = Modifier.size(28.dp),
                strokeWidth = 3.dp,
            )
        }
        Text(
            text = "$finishCount/$totalCount",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
