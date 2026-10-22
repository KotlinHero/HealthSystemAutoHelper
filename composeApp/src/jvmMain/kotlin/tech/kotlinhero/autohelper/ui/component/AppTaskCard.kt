package tech.kotlinhero.autohelper.ui.component

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.WarningAmber
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import tech.kotlinhero.autohelper.ui.task.TaskDefinition
import tech.kotlinhero.autohelper.ui.task.formatRecordMeta
import tech.kotlinhero.autohelper.ui.task.statusTextOf
import tech.kotlinhero.autohelper.ui.theme.AppColors
import tech.kotlinhero.autohelper.ui.theme.AppShapes
import tech.kotlinhero.autohelper.ui.theme.AppType
import tech.kotlinhero.autohelper.ui.viewmodel.TaskFailure
import tech.kotlinhero.autohelper.ui.viewmodel.TaskLogLine
import tech.kotlinhero.autohelper.ui.viewmodel.TaskRunRecord
import tech.kotlinhero.autohelper.ui.viewmodel.LogLevel
import tech.kotlinhero.autohelper.ui.task.formatDuration
import java.awt.Toolkit
import java.awt.datatransfer.StringSelection

/**
 * 常态任务卡：一行三档信息（任务名 / 一句话说明 / 上次结果），
 * 第三行固定语序，8 张卡竖着扫过去就能对比。
 */
@Composable
fun TaskCard(
    definition: TaskDefinition,
    record: TaskRunRecord?,
    dimmed: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val hover = rememberHoverState()
    val hovered = hover.hovered()
    val lift by animateDpAsState(if (hovered && !dimmed) (-1).dp else 0.dp, tween(140), label = "card-lift")
    val goAlpha by animateFloatAsState(if (hovered && !dimmed) 1f else 0f, tween(140), label = "card-go")
    Box(
        modifier = modifier
            .offset(y = lift)
            .shadow(
                elevation = if (hovered && !dimmed) 10.dp else 1.dp,
                shape = AppShapes.Lg,
                ambientColor = Color(0x14171A1F),
                spotColor = Color(0x1A171A1F),
            )
            .clip(AppShapes.Lg)
            .background(AppColors.Surface)
            .border(1.dp, if (hovered && !dimmed) AppColors.Line2 else AppColors.Line, AppShapes.Lg)
            .clickable(interactionSource = hover.interactionSource, indication = null, enabled = !dimmed, onClick = onClick)
            .alpha(if (dimmed) 0.62f else 1f)
            .padding(horizontal = 15.dp, vertical = 14.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            DomainIcon(definition)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(definition.title, style = AppType.CardTitle, color = AppColors.Ink, maxLines = 1)
                Text(
                    definition.description,
                    style = AppType.CardDesc,
                    color = AppColors.Ink2,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    val failed = record != null && !record.allSucceeded
                    Dot(if (record == null) AppColors.Line2 else if (failed) AppColors.Err else AppColors.Ok)
                    Spacer(Modifier.width(6.dp))
                    Text(
                        formatRecordMeta(record) ?: "还没有运行过",
                        style = AppType.Meta,
                        color = AppColors.Ink3,
                        maxLines = 1,
                    )
                    val status = statusTextOf(record)
                    if (status != null) {
                        Spacer(Modifier.width(8.dp))
                        Text(
                            status,
                            style = AppType.MetaStrong,
                            color = if (failed) AppColors.Err else AppColors.Ink3,
                            maxLines = 1,
                        )
                    }
                }
            }
            Spacer(Modifier.width(10.dp))
            Box(
                Modifier
                    .size(32.dp)
                    .alpha(goAlpha)
                    .clip(AppShapes.Sm)
                    .background(AppColors.Surface2),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Outlined.ChevronRight, null, tint = AppColors.Ink2, modifier = Modifier.size(16.dp))
            }
        }
    }
}

@Composable
fun DomainIcon(definition: TaskDefinition, size: androidx.compose.ui.unit.Dp = 36.dp) {
    Box(
        Modifier.size(size).clip(AppShapes.Md).background(definition.domain.tint),
        contentAlignment = Alignment.Center,
    ) {
        Icon(definition.icon, null, tint = definition.domain.color, modifier = Modifier.size(size * 0.53f))
    }
}

/** 外壳：原位展开的卡片共用同样的边框与内边距，避免展开时整块跳变 */
@Composable
fun ExpandedCardShell(modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier = modifier
            .shadow(4.dp, AppShapes.Lg, ambientColor = Color(0x14171A1F), spotColor = Color(0x1A171A1F))
            .clip(AppShapes.Lg)
            .background(AppColors.Surface)
            .border(1.dp, AppColors.Line2, AppShapes.Lg)
            .padding(start = 16.dp, end = 16.dp, top = 14.dp, bottom = 15.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        content = content,
    )
}

@Composable
fun RunHead(
    definition: TaskDefinition,
    pillText: String,
    pillKind: PillKind,
    subText: String? = null,
    action: @Composable () -> Unit = {},
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        DomainIcon(definition)
        Spacer(Modifier.width(11.dp))
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(definition.title, style = AppType.CardTitle, color = AppColors.Ink, maxLines = 1)
                Spacer(Modifier.width(8.dp))
                StatusPill(pillText, pillKind)
            }
            if (subText != null) {
                Text(subText, style = AppType.Meta, color = AppColors.Ink3, maxLines = 1)
            }
        }
        action()
    }
}

/** 运行中面板：大数字 + 当前行 + 最近 3 条日志 */
@Composable
fun RunningPanel(
    finishCount: Int,
    totalCount: Int,
    activeRow: Int?,
    activeLabel: String?,
    logTail: List<TaskLogLine>,
    totalLogCount: Int,
    nowHint: String,
) {
    Column(verticalArrangement = Arrangement.spacedBy(9.dp)) {
        Row(verticalAlignment = Alignment.Bottom) {
            Text("已录入", style = AppType.Meta, color = AppColors.Ink3, modifier = Modifier.padding(bottom = 3.dp))
            Spacer(Modifier.width(8.dp))
            Text("$finishCount", style = AppType.BigNum, color = AppColors.Ink)
            Spacer(Modifier.width(6.dp))
            Text("/ $totalCount 条", style = AppType.Meta, color = AppColors.Ink3, modifier = Modifier.padding(bottom = 3.dp))
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            PulseDot()
            Spacer(Modifier.width(7.dp))
            val rowText = if (activeRow != null) "第 $activeRow 行" else "准备中"
            val labelText = activeLabel?.let { " · $it" } ?: ""
            Text(
                "正在处理 $rowText$labelText · $nowHint",
                style = AppType.Small,
                color = AppColors.Ink2,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        LogTail(logTail)
        Text("最近 ${logTail.size} 条 · 共 $totalLogCount 条", style = AppType.Small, color = AppColors.Ink3)
    }
}

@Composable
fun DonePanel(totalCount: Int, durationMillis: Long, logTail: List<TaskLogLine>) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            OkBadge()
            Spacer(Modifier.width(11.dp))
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text("$totalCount 条全部录入成功", style = AppType.CardTitle, color = AppColors.Ink)
                Text(
                    "没有任何记录被跳过 · 全程 ${formatDuration(durationMillis)}",
                    style = AppType.Meta,
                    color = AppColors.Ink3,
                )
            }
        }
        LogTail(logTail)
    }
}

@Composable
private fun OkBadge() {
    val progress = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        progress.animateTo(1f, tween(durationMillis = 420, easing = LinearOutSlowInEasing))
    }
    val value = progress.value
    Box(
        Modifier
            .size(38.dp)
            .scale(0.78f + 0.22f * value)
            .alpha(value)
            .clip(AppShapes.Pill)
            .background(AppColors.OkTint),
        contentAlignment = Alignment.Center,
    ) {
        Icon(Icons.Outlined.Check, null, tint = AppColors.Ok, modifier = Modifier.size(20.dp))
    }
}

/** 失败面板：一行一条未录入记录，原因写清楚；条数多时在卡片内滚动 */
@Composable
fun FailedPanel(failures: List<TaskFailure>, handled: Int, modifier: Modifier = Modifier) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Outlined.WarningAmber, null, tint = AppColors.Err, modifier = Modifier.size(14.dp))
            Spacer(Modifier.width(6.dp))
            Text("${failures.size} 条记录未录入", style = AppType.MetaStrong, color = AppColors.Err)
        }
        LazyColumn(Modifier.weight(1f).fillMaxWidth()) {
            itemsIndexed(failures) { index, failure ->
                if (index > 0) HairLine()
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 7.dp),
                    verticalAlignment = Alignment.Top,
                ) {
                    Text(
                        failure.row?.let { "第 $it 行" } ?: "—",
                        style = AppType.monoStyle(12),
                        color = AppColors.Ink3,
                        modifier = Modifier.width(66.dp),
                    )
                    Text(
                        failure.label,
                        style = AppType.Meta,
                        color = AppColors.Ink2,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f),
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        failure.reason,
                        style = AppType.Small,
                        color = AppColors.Err,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1.1f),
                    )
                }
            }
        }
        val successCount = (handled - failures.size).coerceAtLeast(0)
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                "把这几行单独存成一份 Excel 再启动，成功的 $successCount 条不会重跑",
                style = AppType.Small,
                color = AppColors.Ink3,
                modifier = Modifier.weight(1f),
            )
            AppLink(text = "复制明细", onClick = { copyFailuresToClipboard(failures) })
        }
    }
}

/** 最近几条日志：等宽字体、行首圆点按结果着色 */
@Composable
fun LogTail(lines: List<TaskLogLine>, maxLines: Int = 3) {
    Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
        lines.takeLast(maxLines).forEach { line ->
            Row(verticalAlignment = Alignment.Top) {
                Box(
                    Modifier
                        .padding(top = 8.dp)
                        .size(6.dp)
                        .clip(AppShapes.Pill)
                        .background(
                            when (line.level) {
                                LogLevel.Ok -> AppColors.Ok
                                LogLevel.Err -> AppColors.Err
                                LogLevel.Warn -> AppColors.Warn
                                LogLevel.Neutral -> AppColors.Line2
                            },
                        ),
                )
                Spacer(Modifier.width(7.dp))
                Text(
                    text = linePrefix(line) + lineText(line),
                    style = AppType.logStyle(),
                    color = if (line.level == LogLevel.Err) AppColors.Err else AppColors.Ink2,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

private fun linePrefix(line: TaskLogLine): String = "${line.time}　"

private fun lineText(line: TaskLogLine): String {
    val row = line.row?.let { "第 $it 行 " } ?: ""
    val label = line.label?.let { "$it " } ?: ""
    val message = when (line.level) {
        LogLevel.Err -> "未录入：${line.message}"
        else -> line.message
    }
    return if (line.label == null) line.message else "$row$label· $message"
}

private fun copyFailuresToClipboard(failures: List<TaskFailure>) {
    val text = failures.joinToString(System.lineSeparator()) { failure ->
        listOf(failure.row?.toString() ?: "", failure.label, failure.reason).joinToString("\t")
    }
    runCatching {
        Toolkit.getDefaultToolkit().systemClipboard.setContents(StringSelection(text), null)
    }
}