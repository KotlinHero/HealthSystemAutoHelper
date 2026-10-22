package tech.kotlinhero.autohelper.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.GridView
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import tech.kotlinhero.autohelper.ui.theme.AppColors
import tech.kotlinhero.autohelper.ui.theme.AppShapes
import tech.kotlinhero.autohelper.ui.theme.AppType

enum class TopRoute { Board, Settings }

/**
 * 顶栏：品牌 + 运行胶囊 + 任务台/设置切换。
 * 导航从左侧 NavigationRail 改成顶部 3 段开关，是因为原布局里 56px 的竖栏占了卡片宽度又不承载任何任务信息。
 */
@Composable
fun AppTopBar(
    route: TopRoute,
    onRouteChange: (TopRoute) -> Unit,
    runningTitle: String?,
    runningFraction: Float,
    onRunPillClick: () -> Unit,
    modifier: Modifier = Modifier,
    compact: Boolean = false,
) {
    Column(modifier.fillMaxWidth().background(AppColors.Surface)) {
        Row(
            modifier = Modifier.fillMaxWidth().height(56.dp).padding(horizontal = 24.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            BrandMark(28.dp)
            Spacer(Modifier.width(9.dp))
            Text("卫生信息系统自动助手", style = AppType.BrandName, color = AppColors.Ink, maxLines = 1)
            if (!compact) {
                Box(Modifier.padding(horizontal = 12.dp).width(1.dp).height(16.dp).background(AppColors.Line))
                Text("Excel 批量录入", style = AppType.Meta, color = AppColors.Ink3, maxLines = 1)
            }
            Spacer(Modifier.weight(1f))
            if (runningTitle != null) {
                RunPill(title = runningTitle, fraction = runningFraction, onClick = onRunPillClick)
                Spacer(Modifier.width(12.dp))
            }
            NavSeg(
                route = route,
                onChange = onRouteChange,
            )
        }
        HairLine()
    }
}

@Composable
private fun RunPill(title: String, fraction: Float, onClick: () -> Unit) {
    val hover = rememberHoverState()
    val hovered = hover.hovered()
    Row(
        modifier = Modifier
            .height(32.dp)
            .clip(AppShapes.Pill)
            .background(if (hovered) AppColors.Surface2 else AppColors.Surface)
            .border(1.dp, AppColors.Line, AppShapes.Pill)
            .clickable(interactionSource = hover.interactionSource, indication = null, onClick = onClick)
            .padding(horizontal = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        PulseDot()
        Spacer(Modifier.width(7.dp))
        Text(title, style = AppType.MetaStrong, color = AppColors.Ink2, maxLines = 1)
        Spacer(Modifier.width(8.dp))
        MiniBar(fraction)
        Spacer(Modifier.width(6.dp))
        Icon(Icons.Outlined.ChevronRight, null, tint = AppColors.Ink3, modifier = Modifier.size(13.dp))
    }
}

@Composable
private fun MiniBar(fraction: Float) {
    Box(Modifier.width(36.dp).height(4.dp).clip(AppShapes.Pill).background(AppColors.Surface3)) {
        Box(
            Modifier
                .fillMaxHeight()
                .fillMaxWidth(fraction.coerceIn(0f, 1f))
                .clip(AppShapes.Pill)
                .background(AppColors.Brand),
        )
    }
}

@Composable
private fun NavSeg(route: TopRoute, onChange: (TopRoute) -> Unit) {
    Row(
        modifier = Modifier.clip(AppShapes.Md).background(AppColors.Surface2).padding(3.dp),
        horizontalArrangement = Arrangement.spacedBy(2.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        NavSegItem("任务台", Icons.Outlined.GridView, route == TopRoute.Board) { onChange(TopRoute.Board) }
        NavSegItem("设置", Icons.Outlined.Settings, route == TopRoute.Settings) { onChange(TopRoute.Settings) }
    }
}

@Composable
private fun NavSegItem(label: String, icon: ImageVector, active: Boolean, onClick: () -> Unit) {
    val hover = rememberHoverState()
    val hovered = hover.hovered()
    val background = when {
        active -> AppColors.Surface
        hovered -> AppColors.Surface3
        else -> androidx.compose.ui.graphics.Color.Transparent
    }
    Row(
        modifier = Modifier
            .height(30.dp)
            .clip(AppShapes.Sm)
            .background(background)
            .clickable(interactionSource = hover.interactionSource, indication = null, onClick = onClick)
            .padding(horizontal = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Icon(icon, null, tint = if (active) AppColors.Brand else AppColors.Ink2, modifier = Modifier.size(15.dp))
        Text(label, style = AppType.Nav, color = if (active) AppColors.Brand else AppColors.Ink2, maxLines = 1)
    }
}
