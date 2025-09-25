package tech.kotlinhero.autohelper

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
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
    DynamicConfigTheme {
        Row {
            NavigationRail(
                modifier = Modifier.width(80.dp),
                containerColor = MaterialTheme.colorScheme.inverseOnSurface,
            ) {
                NavigationRouter.entries.forEach { item ->
                    NavigationRailItem(
                        icon = {
                            Icon(
                                item.icon,
                                contentDescription = item.label
                            )
                        },
                        label = { Text(item.label) },
                        selected = currentPage == item,
                        onClick = {
                            currentPage = item
                            navController.navigate(item.route)
                        }
                    )
                }
                Box(
                    modifier = Modifier.fillMaxSize()
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .align(Alignment.BottomCenter) // 水平居中
                            .clickable {
                                currentPage = TaskExecuteRouterItem
                                navController.navigate(TaskExecuteRouterItem.route)
                            },
                    ) {
                        if (taskState == TaskState.FINISHED) {
                            CircularProgressIndicator(
                                modifier = Modifier.align(Alignment.Center),
                                progress = { 1f }
                            )
                        } else {
                            CircularProgressIndicator(
                                modifier = Modifier.align(Alignment.Center)
                            )
                        }
                    }
                }
            }
            NavHost(navController, startDestination = NavigationRouter.TaskMode.route) {
                composable(NavigationRouter.TaskMode.route) {
                    TaskMode(taskExecuteViewModel)
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