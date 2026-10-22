package tech.kotlinhero.autohelper

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import tech.kotlinhero.autohelper.ui.component.AppTopBar
import tech.kotlinhero.autohelper.ui.component.LogOverlay
import tech.kotlinhero.autohelper.ui.component.TopRoute
import tech.kotlinhero.autohelper.ui.component.rememberHoverState
import tech.kotlinhero.autohelper.ui.page.SettingsPage
import tech.kotlinhero.autohelper.ui.page.StartTaskDrawer
import tech.kotlinhero.autohelper.ui.page.TaskBoard
import tech.kotlinhero.autohelper.ui.task.TaskDefinition
import tech.kotlinhero.autohelper.ui.theme.AppColors
import tech.kotlinhero.autohelper.ui.theme.DynamicConfigTheme
import tech.kotlinhero.autohelper.ui.viewmodel.TaskExecuteViewModel
import tech.kotlinhero.autohelper.ui.viewmodel.UserExcelTaskStartParams

/**
 * 外壳：顶部两段导航（任务台 / 设置）+ 任务台 + 两个浮层（启动抽屉、完整日志）。
 * 原来的左侧 NavigationRail 与「任务执行」独立页都被移除：任务进度改为在原位卡片里展开。
 */
@Composable
fun App(taskExecuteViewModel: TaskExecuteViewModel = viewModel { TaskExecuteViewModel() }) {
    DynamicConfigTheme {
        var route by remember { mutableStateOf(TopRoute.Board) }
        var drawerTask by remember { mutableStateOf<TaskDefinition?>(null) }
        var drawerParams by remember { mutableStateOf<UserExcelTaskStartParams?>(null) }
        var logOpen by remember { mutableStateOf(false) }
        var panelsCollapsed by remember { mutableStateOf(false) }

        val taskState by taskExecuteViewModel.taskState.collectAsState()
        val activeTask by taskExecuteViewModel.activeTask.collectAsState()
        val finishCount by taskExecuteViewModel.finishCount.collectAsState()
        val totalCount by taskExecuteViewModel.totalCount.collectAsState()
        val log by taskExecuteViewModel.taskLog.collectAsState()

        val running = taskState.isActive
        LaunchedEffect(running) {
            if (running) panelsCollapsed = false
        }
        val fraction = if (totalCount > 0) finishCount.toFloat() / totalCount else 0f
        val scrimHover = rememberHoverState()

        Box(Modifier.fillMaxSize().background(AppColors.Bg)) {
            Column(Modifier.fillMaxSize()) {
                AppTopBar(
                    route = route,
                    onRouteChange = { route = it },
                    runningTitle = if (running) activeTask?.title else null,
                    runningFraction = fraction,
                    onRunPillClick = {
                        route = TopRoute.Board
                        panelsCollapsed = false
                    },
                )
                when (route) {
                    TopRoute.Board -> TaskBoard(
                        viewModel = taskExecuteViewModel,
                        panelsCollapsed = panelsCollapsed,
                        onShowPanels = { panelsCollapsed = false },
                        onDismissPanels = { panelsCollapsed = true },
                        onOpenDrawer = { definition, params ->
                            drawerParams = params
                            drawerTask = definition
                        },
                        onOpenLog = { logOpen = true },
                    )
                    TopRoute.Settings -> SettingsPage()
                }
            }

            drawerTask?.let { definition ->
                Box(
                    Modifier
                        .fillMaxSize()
                        .background(AppColors.Scrim)
                        .clickable(
                            interactionSource = scrimHover.interactionSource,
                            indication = null,
                            onClick = { drawerTask = null },
                        ),
                )
                Row(Modifier.fillMaxSize(), horizontalArrangement = Arrangement.End) {
                    StartTaskDrawer(
                        definition = definition,
                        initialParams = drawerParams,
                        onDismiss = { drawerTask = null },
                        onStart = { params -> taskExecuteViewModel.startTask(definition, params) },
                        modifier = Modifier.fillMaxHeight(),
                    )
                }
            }

            if (logOpen) {
                LogOverlay(
                    title = activeTask?.title ?: "任务",
                    lines = log,
                    onDismiss = { logOpen = false },
                )
            }
        }
    }
}
