package tech.kotlinhero.autohelper.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.composed
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import tech.kotlinhero.autohelper.ui.theme.AppColors
import tech.kotlinhero.autohelper.ui.theme.AppShapes
import tech.kotlinhero.autohelper.ui.theme.AppType
import tech.kotlinhero.autohelper.ui.viewmodel.LogLevel
import tech.kotlinhero.autohelper.ui.viewmodel.TaskLogLine

/** 完整日志：卡片里只放最近 3 条，历史在这里一次看完，每一行都带行号与结果 */
@Composable
fun LogOverlay(
    title: String,
    lines: List<TaskLogLine>,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val state = rememberLazyListState()
    LaunchedEffect(lines.size) {
        if (lines.isNotEmpty()) state.scrollToItem(lines.lastIndex)
    }
    Box(
        modifier = modifier.fillMaxSize().background(AppColors.Scrim).scrimClick(onDismiss),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            Modifier
                .width(760.dp)
                .height(560.dp)
                .shadow(24.dp, AppShapes.Lg, ambientColor = Color(0x33171A1F), spotColor = Color(0x33171A1F))
                .clip(AppShapes.Lg)
                .background(AppColors.Surface)
                .scrimClick { }, // 吞掉点击，避免点内容也关闭
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(start = 20.dp, end = 12.dp, top = 16.dp, bottom = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text("运行日志", style = AppType.CardTitle, color = AppColors.Ink)
                    Text("$title · 共 ${lines.size} 条", style = AppType.Meta, color = AppColors.Ink3)
                }
                IconTap(Icons.Outlined.Close, "关闭", onDismiss, tint = AppColors.Ink3)
            }
            HairLine()
            if (lines.isEmpty()) {
                Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Text("任务开始后日志会显示在这里", style = AppType.Meta, color = AppColors.Ink3)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f).fillMaxWidth().padding(horizontal = 20.dp, vertical = 12.dp),
                    state = state,
                    verticalArrangement = Arrangement.spacedBy(2.dp),
                ) {
                    items(lines) { line ->
                        Row(verticalAlignment = Alignment.Top) {
                            Dot(
                                color = when (line.level) {
                                    LogLevel.Ok -> AppColors.Ok
                                    LogLevel.Err -> AppColors.Err
                                    LogLevel.Warn -> AppColors.Warn
                                    LogLevel.Neutral -> AppColors.Line2
                                },
                                modifier = Modifier.padding(top = 8.dp),
                            )
                            Spacer(Modifier.width(7.dp))
                            Text(
                                text = "${line.time}　${LogText(line)}",
                                style = AppType.logStyle(),
                                color = if (line.level == LogLevel.Err) AppColors.Err else AppColors.Ink2,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun LogText(line: TaskLogLine): String {
    val row = line.row?.let { "第 $it 行 " } ?: ""
    val label = line.label?.let { "$it " } ?: ""
    val message = if (line.level == LogLevel.Err) "未录入：${line.message}" else line.message
    return if (line.label == null) line.message else "$row$label· $message"
}

/** 点遮罩关闭：只吃点击、不画涟漪 */
private fun Modifier.scrimClick(onClick: () -> Unit): Modifier = composed {
    clickable(interactionSource = rememberHoverState().interactionSource, indication = null, onClick = onClick)
}
