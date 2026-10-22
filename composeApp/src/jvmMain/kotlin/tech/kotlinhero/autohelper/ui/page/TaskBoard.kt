package tech.kotlinhero.autohelper.ui.page

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Cancel
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.WarningAmber
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import tech.kotlinhero.autohelper.core.TaskState
import tech.kotlinhero.autohelper.ui.component.AppButton
import tech.kotlinhero.autohelper.ui.component.AppProgressBar
import tech.kotlinhero.autohelper.ui.component.BtnSize
import tech.kotlinhero.autohelper.ui.component.BtnVariant
import tech.kotlinhero.autohelper.ui.component.DonePanel
import tech.kotlinhero.autohelper.ui.component.ExpandedCardShell
import tech.kotlinhero.autohelper.ui.component.FailedPanel
import tech.kotlinhero.autohelper.ui.component.PillKind
import tech.kotlinhero.autohelper.ui.component.RunHead
import tech.kotlinhero.autohelper.ui.component.RunningPanel
import tech.kotlinhero.autohelper.ui.component.TaskCard
import tech.kotlinhero.autohelper.ui.task.TaskCatalog
import tech.kotlinhero.autohelper.ui.task.TaskDefinition
import tech.kotlinhero.autohelper.ui.task.TaskGroup
import tech.kotlinhero.autohelper.ui.task.formatDuration
import tech.kotlinhero.autohelper.ui.task.formatRunMoment
import tech.kotlinhero.autohelper.ui.theme.AppColors
import tech.kotlinhero.autohelper.ui.theme.AppShapes
import tech.kotlinhero.autohelper.ui.theme.AppType
import tech.kotlinhero.autohelper.ui.viewmodel.TaskExecuteViewModel
import tech.kotlinhero.autohelper.ui.viewmodel.TaskRunRecord
import tech.kotlinhero.autohelper.ui.viewmodel.UserExcelTaskStartParams

/**
 * 任务台：一句话说明上次结局（汇总条）+ 8 个业务任务（2 列 4 组）+ 原位展开的运行跑道。
 * 原来「进度放独立页、要主动跳过去看」的结构是首屏最贵的问题，这里把跑道搬回卡片原位。
 */
@Composable
fun TaskBoard(
    viewModel: TaskExecuteViewModel,
    panelsCollapsed: Boolean,
    onShowPanels: () -> Unit,
    onDismissPanels: () -> Unit,
    onOpenDrawer: (TaskDefinition, UserExcelTaskStartParams?) -> Unit,
    onOpenLog: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val taskState by viewModel.taskState.collectAsState()
    val finishCount by viewModel.finishCount.collectAsState()
    val totalCount by viewModel.totalCount.collectAsState()
    val log by viewModel.taskLog.collectAsState()
    val failures by viewModel.failures.collectAsState()
    val activeTask by viewModel.activeTask.collectAsState()
    val activeRow by viewModel.activeRow.collectAsState()
    val runStartedAt by viewModel.runStartedAt.collectAsState()
    val history by viewModel.runHistory.collectAsState()
    val lastLaunch by viewModel.lastLaunch.collectAsState()

    val running = taskState.isActive
    val finished = taskState == TaskState.Finished
    val failedRun = taskState == TaskState.Failed || taskState == TaskState.Cancelled

    var nowMs by remember { mutableStateOf(System.currentTimeMillis()) }
    LaunchedEffect(running) {
        while (running) {
            nowMs = System.currentTimeMillis()
            delay(1000)
        }
    }
    val elapsed = runStartedAt?.let { (nowMs - it).coerceAtLeast(0L) } ?: 0L

    var peekId by remember { mutableStateOf<String?>(null) }
    LaunchedEffect(running, finished, failedRun) {
        if (running || finished || failedRun) peekId = null
    }
    val expandedId = when {
        panelsCollapsed -> null
        running || finished || failedRun -> activeTask?.id
        else -> peekId
    }
    val lastRecord = remember(history) { history.values.maxByOrNull { it.finishedAtMillis } }

    /** 某个任务现在能不能只补录未录入的行：能则给出 Excel 行号，不能给 null */
    val retryRowsOf: (String) -> List<Int>? = viewModel::retryRowsFor
    /** 本轮失败条数：跑完取失败明细，重启后用历史记录里的条数 */
    val failedCount = if (failures.isNotEmpty()) failures.size else activeTask?.let { history[it.id]?.failed } ?: 0
    val successCount = (finishCount - failedCount).coerceAtLeast(0)
    /** 补录入口：把「只跑这几行」作为抽屉初始参数，用户在抽屉里确认 Excel 后再启动 */
    val retryOpen: (TaskDefinition) -> Unit = { definition ->
        viewModel.retryParamsFor(definition.id)?.let { params -> onOpenDrawer(definition, params) }
    }
    val retryDefinition = activeTask?.takeIf { retryRowsOf(it.id) != null }
    val retryAction: (() -> Unit)? = retryDefinition?.let { definition ->
        val action: () -> Unit = { retryOpen(definition) }
        action
    }

    Column(modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(start = 24.dp, end = 24.dp, top = 20.dp, bottom = 24.dp)) {
        Text("任务台", style = AppType.PageTitle, color = AppColors.Ink)
        Spacer(Modifier.height(4.dp))
        Text(
            if (running) "本轮进行中 · 其他 7 个任务在本轮结束后可启动" else "8 个任务 · 4 组系统账号 · 同一时间只跑一个",
            style = AppType.CardDesc,
            color = AppColors.Ink3,
        )
        Spacer(Modifier.height(16.dp))

        when {
            running -> RunningStrip(
                title = activeTask?.title ?: "任务",
                finishCount = finishCount,
                totalCount = totalCount,
                activeRow = activeRow,
                label = log.lastOrNull { it.label != null }?.label,
                elapsed = elapsed,
                cancelling = taskState == TaskState.Cancelling,
                onCancel = viewModel::cancelCurrentTask,
            )
            finished -> FinishedStrip(
                title = activeTask?.title ?: "任务",
                totalCount = totalCount,
                elapsed = elapsed,
                onBack = onDismissPanels,
                onAgain = {
                    activeTask?.let { onOpenDrawer(it, lastLaunch?.params) }
                },
            )
            failedRun -> FailedStrip(
                title = activeTask?.title ?: "任务",
                failed = failedCount,
                success = successCount,
                momentMillis = System.currentTimeMillis(),
                cancelledOnly = failedCount == 0,
                onBack = onDismissPanels,
                onShow = onShowPanels,
                onRetry = retryAction,
            )
            else -> IdleStrip(
                record = lastRecord,
                retryRows = lastRecord?.let { retryRowsOf(it.taskId) },
                onPeek = { id -> peekId = if (peekId == id) null else id },
                onRunAgain = { definition ->
                    onOpenDrawer(
                        definition,
                        lastLaunch?.takeIf { it.definition.id == definition.id }?.params?.copy(onlyRows = null),
                    )
                },
                onRetry = { id -> TaskCatalog.byId(id)?.let(retryOpen) },
            )
        }
        Spacer(Modifier.height(16.dp))

        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(20.dp)) {
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(20.dp)) {
                TaskCatalog.leftColumn.forEach { group ->
                    GroupBlock(
                        group = group,
                        expandedId = expandedId,
                        dimAll = running,
                        taskState = taskState,
                        activeTask = activeTask,
                        finishCount = finishCount,
                        totalCount = totalCount,
                        activeRow = activeRow,
                        activeLabel = log.lastOrNull { it.label != null }?.label,
                        logTail = log,
                        failures = failures,
                        durationMillis = elapsed,
                        history = history,
                        retryRowsOf = retryRowsOf,
                        onRetryTask = retryOpen,
                        onCardClick = { definition ->
                            peekId = null
                            onOpenDrawer(definition, lastLaunch?.takeIf { it.definition.id == definition.id }?.params)
                        },
                        onOpenLog = onOpenLog,
                    )
                }
            }
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(20.dp)) {
                TaskCatalog.rightColumn.forEach { group ->
                    GroupBlock(
                        group = group,
                        expandedId = expandedId,
                        dimAll = running,
                        taskState = taskState,
                        activeTask = activeTask,
                        finishCount = finishCount,
                        totalCount = totalCount,
                        activeRow = activeRow,
                        activeLabel = log.lastOrNull { it.label != null }?.label,
                        logTail = log,
                        failures = failures,
                        durationMillis = elapsed,
                        history = history,
                        retryRowsOf = retryRowsOf,
                        onRetryTask = retryOpen,
                        onCardClick = { definition ->
                            peekId = null
                            onOpenDrawer(definition, lastLaunch?.takeIf { it.definition.id == definition.id }?.params)
                        },
                        onOpenLog = onOpenLog,
                    )
                }
            }
        }
    }
}

@Composable
private fun GroupBlock(
    group: TaskGroup,
    expandedId: String?,
    dimAll: Boolean,
    taskState: TaskState,
    activeTask: TaskDefinition?,
    finishCount: Int,
    totalCount: Int,
    activeRow: Int?,
    activeLabel: String?,
    logTail: List<tech.kotlinhero.autohelper.ui.viewmodel.TaskLogLine>,
    failures: List<tech.kotlinhero.autohelper.ui.viewmodel.TaskFailure>,
    durationMillis: Long,
    history: Map<String, TaskRunRecord>,
    onCardClick: (TaskDefinition) -> Unit,
    onOpenLog: () -> Unit,
    retryRowsOf: (String) -> List<Int>?,
    onRetryTask: (TaskDefinition) -> Unit,
) {
    Column {
        Row(Modifier.height(24.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.width(3.dp).height(13.dp).clip(AppShapes.Xs).background(group.domain.color))
            Spacer(Modifier.width(8.dp))
            Text(group.title, style = AppType.Label, color = AppColors.Ink)
        }
        Spacer(Modifier.height(8.dp))
        Column(verticalArrangement = Arrangement.spacedBy(11.dp)) {
            group.tasks.forEach { definition ->
                val record = history[definition.id]
                if (definition.id == expandedId) {
                    ExpandedCard(
                        definition = definition,
                        taskState = taskState,
                        finishCount = finishCount,
                        totalCount = totalCount,
                        activeRow = activeRow,
                        activeLabel = activeLabel,
                        logTail = logTail,
                        failures = failures,
                        durationMillis = if (definition.id == activeTask?.id) durationMillis else record?.durationMillis ?: 0L,
                        record = record,
                        retryRows = retryRowsOf(definition.id),
                        onRetry = { onRetryTask(definition) },
                        onOpenLog = onOpenLog,
                    )
                } else {
                    TaskCard(
                        definition = definition,
                        record = record,
                        dimmed = dimAll,
                        onClick = { onCardClick(definition) },
                    )
                }
            }
        }
    }
}

@Composable
private fun ExpandedCard(
    definition: TaskDefinition,
    taskState: TaskState,
    finishCount: Int,
    totalCount: Int,
    activeRow: Int?,
    activeLabel: String?,
    logTail: List<tech.kotlinhero.autohelper.ui.viewmodel.TaskLogLine>,
    failures: List<tech.kotlinhero.autohelper.ui.viewmodel.TaskFailure>,
    durationMillis: Long,
    record: TaskRunRecord?,
    retryRows: List<Int>?,
    onRetry: () -> Unit,
    onOpenLog: () -> Unit,
) {
    val running = taskState.isActive
    val failedRun = taskState == TaskState.Failed || taskState == TaskState.Cancelled
    val peekFailed = !running && taskState == TaskState.Idle && record != null && !record.allSucceeded
    val showFail = failedRun || peekFailed
    val height = when {
        running -> 214.dp
        // 明细没留存时内容只有两行字 + 底部按钮，不需要和带明细时一样高
        showFail && failures.isEmpty() -> 152.dp
        showFail -> 320.dp
        else -> 186.dp
    }
    ExpandedCardShell(Modifier.fillMaxWidth().height(height)) {
        when {
            running -> {
                RunHead(
                    definition = definition,
                    pillText = if (taskState == TaskState.Cancelling) "取消中" else "运行中",
                    pillKind = if (taskState == TaskState.Cancelling) PillKind.Warn else PillKind.Run,
                    action = {
                        AppButton("展开完整日志", onOpenLog, variant = BtnVariant.Quiet, size = BtnSize.Sm)
                    },
                )
                RunningPanel(
                    finishCount = finishCount,
                    totalCount = totalCount,
                    activeRow = activeRow,
                    activeLabel = activeLabel,
                    logTail = logTail,
                    totalLogCount = logTail.size,
                    nowHint = if (taskState == TaskState.Cancelling) "正在停止" else "本轮进行中",
                )
            }
            showFail -> {
                val shown = failures
                val failCount = if (shown.isNotEmpty()) shown.size else record?.failed ?: 0
                val successCount = if (shown.isNotEmpty()) {
                    (finishCount - shown.size).coerceAtLeast(0)
                } else {
                    record?.handled ?: finishCount
                }
                RunHead(
                    definition = definition,
                    pillText = if (failCount == 0) "已取消" else "未录入",
                    pillKind = if (failCount == 0) PillKind.Warn else PillKind.Err,
                    action = {
                        if (failCount == 0) {
                            AppButton("查看完整日志", onOpenLog, variant = BtnVariant.Quiet, size = BtnSize.Sm)
                        }
                    },
                )
                if (failCount == 0) {
                    Text(
                        "本轮已停止，已录入 $successCount 条，没有留下未录入记录。",
                        style = AppType.CardDesc,
                        color = AppColors.Ink3,
                    )
                } else {
                    FailedPanel(
                        failures = shown,
                        failCount = failCount,
                        successCount = successCount,
                        onRetry = if (retryRows != null) onRetry else null,
                        modifier = Modifier.weight(1f),
                    )
                }
            }
            else -> {
                RunHead(
                    definition = definition,
                    pillText = "完成",
                    pillKind = PillKind.Ok,
                    action = {
                        AppButton("查看完整日志", onOpenLog, variant = BtnVariant.Ghost, size = BtnSize.Sm)
                    },
                )
                DonePanel(
                    totalCount = record?.total ?: totalCount,
                    durationMillis = record?.durationMillis ?: durationMillis,
                    logTail = logTail,
                )
            }
        }
    }
}

/* ---------- 汇总条 ---------- */

private enum class Tone { Idle, Brand, Ok, Err }

@Composable
private fun Strip(
    tone: Tone,
    icon: ImageVector,
    title: String,
    meta: String,
    progress: Float? = null,
    progressLabel: String? = null,
    actions: @Composable RowScope.() -> Unit = {},
) {
    val (iconBg, iconTint) = when (tone) {
        Tone.Idle -> AppColors.Surface2 to AppColors.Ink3
        Tone.Brand -> AppColors.BrandTint to AppColors.Brand
        Tone.Ok -> AppColors.OkTint to AppColors.Ok
        Tone.Err -> AppColors.ErrTint to AppColors.Err
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(AppShapes.Lg)
            .background(AppColors.Surface)
            .border(1.dp, AppColors.Line, AppShapes.Lg)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            Modifier.size(34.dp).clip(AppShapes.Md).background(iconBg),
            contentAlignment = Alignment.Center,
        ) {
            Icon(icon, null, tint = iconTint, modifier = Modifier.size(18.dp))
        }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Text(title, style = AppType.Label.copy(fontSize = 14.sp), color = AppColors.Ink, maxLines = 1)
            Text(meta, style = AppType.Meta, color = AppColors.Ink3, maxLines = 2, overflow = TextOverflow.Ellipsis)
        }
        if (progress != null) {
            Column(Modifier.width(200.dp).padding(horizontal = 8.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    progressLabel.orEmpty(),
                    style = AppType.Meta,
                    color = AppColors.Ink2,
                    maxLines = 1,
                )
                AppProgressBar(progress, color = if (tone == Tone.Ok) AppColors.Ok else AppColors.Brand)
            }
        }
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp), content = actions)
    }
}

@Composable
private fun IdleStrip(
    record: TaskRunRecord?,
    retryRows: List<Int>?,
    onPeek: (String) -> Unit,
    onRunAgain: (TaskDefinition) -> Unit,
    onRetry: (String) -> Unit,
) {
    if (record == null) {
        Strip(
            tone = Tone.Idle,
            icon = Icons.Outlined.Schedule,
            title = "还没有运行过任务",
            meta = "选一个任务，填好账号和 Excel 就能开始",
        )
        return
    }
    val definition = TaskCatalog.byId(record.taskId)
    val failed = record.failed
    val canRetry = !record.allSucceeded && retryRows != null
    Strip(
        tone = if (record.allSucceeded) Tone.Brand else Tone.Err,
        icon = if (record.allSucceeded) Icons.Outlined.CheckCircle else Icons.Outlined.WarningAmber,
        title = "上次运行 · ${definition?.title ?: record.taskId}",
        meta = buildString {
            append(formatRunMoment(record.finishedAtMillis))
            append(" · ${record.total} 条 · ")
            append(if (record.allSucceeded) "全部成功" else "$failed 条未录入")
            append(" · 用时 ${formatDuration(record.durationMillis)}")
            if (canRetry) append(" · 补录只跑这几行")
        },
        actions = {
            AppButton("查看结果", { onPeek(record.taskId) }, variant = BtnVariant.Ghost, size = BtnSize.Sm)
            if (canRetry) {
                AppButton(
                    text = "补齐未录入的 $failed 条",
                    onClick = { onRetry(record.taskId) },
                    variant = BtnVariant.Primary,
                    size = BtnSize.Sm,
                )
            } else if (definition != null) {
                AppButton(
                    text = "再跑一次",
                    onClick = { onRunAgain(definition) },
                    variant = BtnVariant.Primary,
                    size = BtnSize.Sm,
                )
            }
        },
    )
}

@Composable
private fun RunningStrip(
    title: String,
    finishCount: Int,
    totalCount: Int,
    activeRow: Int?,
    label: String?,
    elapsed: Long,
    cancelling: Boolean,
    onCancel: () -> Unit,
) {
    val fraction = if (totalCount > 0) finishCount.toFloat() / totalCount else 0f
    Strip(
        tone = Tone.Brand,
        icon = Icons.Outlined.PlayArrow,
        title = "正在运行 · $title",
        meta = buildString {
            append(if (cancelling) "正在停止" else activeRow?.let { "第 $it 行" } ?: "准备中")
            label?.let { append(" · $it") }
            append(" · 已用 ${formatDuration(elapsed)}")
            append(" · 已录入 $finishCount / $totalCount 条")
        },
        progress = fraction,
        progressLabel = "${(fraction * 100).toInt()}%",
        actions = {
            AppButton("取消任务", onCancel, variant = BtnVariant.DangerGhost, size = BtnSize.Sm, enabled = !cancelling)
        },
    )
}

@Composable
private fun FinishedStrip(
    title: String,
    totalCount: Int,
    elapsed: Long,
    onBack: () -> Unit,
    onAgain: () -> Unit,
) {
    Strip(
        tone = Tone.Ok,
        icon = Icons.Outlined.CheckCircle,
        title = "$title 已完成",
        meta = "$totalCount 条全部录入成功 · 用时 ${formatDuration(elapsed)}",
        progress = 1f,
        progressLabel = "100%",
        actions = {
            AppButton("返回任务台", onBack, variant = BtnVariant.Ghost, size = BtnSize.Sm)
            AppButton("再跑一次", onAgain, variant = BtnVariant.Soft, size = BtnSize.Sm)
        },
    )
}

@Composable
private fun FailedStrip(
    title: String,
    failed: Int,
    success: Int,
    momentMillis: Long,
    cancelledOnly: Boolean,
    onBack: () -> Unit,
    onShow: () -> Unit,
    onRetry: (() -> Unit)?,
) {
    Strip(
        tone = if (cancelledOnly) Tone.Idle else Tone.Err,
        icon = if (cancelledOnly) Icons.Outlined.Cancel else Icons.Outlined.WarningAmber,
        title = if (cancelledOnly) "$title 已取消" else "$title 有 $failed 条未录入",
        meta = if (cancelledOnly) {
            "${formatRunMoment(momentMillis)} · 已录入 $success 条 · 本轮已停止"
        } else if (onRetry != null) {
            "${formatRunMoment(momentMillis)} · $success 条成功 · 补录只处理未录入的 $failed 条，其余不会重跑"
        } else {
            "${formatRunMoment(momentMillis)} · $success 条成功 · 把未录入的几行单独存成 Excel 再启动即可"
        },
        actions = {
            AppButton("返回任务台", onBack, variant = BtnVariant.Ghost, size = BtnSize.Sm)
            if (!cancelledOnly) {
                if (onRetry != null) {
                    AppButton("重试这 $failed 条", onRetry, variant = BtnVariant.Primary, size = BtnSize.Sm)
                } else {
                    AppButton("查看未录入记录", onShow, variant = BtnVariant.Primary, size = BtnSize.Sm)
                }
            }
        },
    )
}