package tech.kotlinhero.autohelper.ui.component

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.UploadFile
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import tech.kotlinhero.autohelper.ui.theme.AppColors
import tech.kotlinhero.autohelper.ui.theme.AppShapes
import tech.kotlinhero.autohelper.ui.theme.AppType

/** 悬停/按压交互源，供自绘控件（按钮、卡片）实现桌面端 hover 反馈 */
class HoverState internal constructor() {
    val interactionSource = MutableInteractionSource()

    @Composable
    fun hovered(): Boolean = interactionSource.collectIsHoveredAsState().value

    @Composable
    fun pressed(): Boolean = interactionSource.collectIsPressedAsState().value
}

@Composable
fun rememberHoverState(): HoverState = remember { HoverState() }

enum class BtnVariant { Primary, Soft, Ghost, Quiet, DangerGhost }

enum class BtnSize { Sm, Md, Lg }

private data class BtnMetrics(val height: Dp, val padH: Dp, val shape: RoundedCornerShape, val style: TextStyle)

private fun BtnSize.metrics(): BtnMetrics = when (this) {
    BtnSize.Sm -> BtnMetrics(28.dp, 10.dp, AppShapes.Xs, AppType.ButtonSmall)
    BtnSize.Md -> BtnMetrics(32.dp, 13.dp, AppShapes.Sm, AppType.Button)
    BtnSize.Lg -> BtnMetrics(38.dp, 18.dp, AppShapes.Md, AppType.ButtonLarge)
}

private data class BtnColors(val container: Color, val containerHover: Color, val content: Color, val border: Color?, val borderHover: Color?)

private fun BtnVariant.colors(): BtnColors = when (this) {
    BtnVariant.Primary -> BtnColors(
        container = AppColors.Brand,
        containerHover = AppColors.BrandDeep,
        content = Color.White,
        border = null,
        borderHover = null,
    )
    BtnVariant.Soft -> BtnColors(
        container = AppColors.BrandTint,
        containerHover = AppColors.BrandTintHover,
        content = AppColors.Brand,
        border = null,
        borderHover = null,
    )
    BtnVariant.Ghost -> BtnColors(
        container = Color.Transparent,
        containerHover = AppColors.Surface2,
        content = AppColors.Ink2,
        border = AppColors.Line2,
        borderHover = AppColors.LineHover,
    )
    BtnVariant.Quiet -> BtnColors(
        container = Color.Transparent,
        containerHover = AppColors.Surface2,
        content = AppColors.Ink2,
        border = null,
        borderHover = null,
    )
    BtnVariant.DangerGhost -> BtnColors(
        container = Color.Transparent,
        containerHover = AppColors.ErrTint,
        content = AppColors.Err,
        border = AppColors.ErrLine,
        borderHover = AppColors.ErrLine,
    )
}

/**
 * 任务台按钮。不复用 Material3 Button：设计稿的按钮高度 28/32/38、内边距与 hover 反馈都需要精确控制，
 * 且 M3 按钮会强制 48dp 最小触控区，会把紧凑的卡片动作区撑开。
 */
@Composable
fun AppButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    variant: BtnVariant = BtnVariant.Ghost,
    size: BtnSize = BtnSize.Md,
    enabled: Boolean = true,
    icon: ImageVector? = null,
    minWidth: Dp? = null,
) {
    val metrics = size.metrics()
    val colors = variant.colors()
    val hover = rememberHoverState()
    val hovered = hover.hovered()
    val pressed = hover.pressed()
    val container by animateColorAsState(
        targetValue = when {
            !enabled -> if (variant == BtnVariant.Primary) AppColors.Surface3 else Color.Transparent
            hovered -> colors.containerHover
            else -> colors.container
        },
        animationSpec = tween(120),
        label = "btn-container",
    )
    val content by animateColorAsState(
        targetValue = when {
            !enabled -> AppColors.Ink3
            else -> colors.content
        },
        animationSpec = tween(120),
        label = "btn-content",
    )
    val borderColor = when {
        !enabled -> colors.border ?: Color.Transparent
        hovered -> colors.borderHover ?: Color.Transparent
        else -> colors.border ?: Color.Transparent
    }
    val clickModifier = if (enabled) {
        Modifier.clickable(
            interactionSource = hover.interactionSource,
            indication = null,
            onClick = onClick,
        )
    } else {
        Modifier
    }
    Row(
        modifier = modifier
            .then(if (minWidth != null) Modifier.width(minWidth) else Modifier)
            .height(metrics.height)
            .clip(metrics.shape)
            .background(container)
            .then(if (colors.border != null) Modifier.border(1.dp, borderColor, metrics.shape) else Modifier)
            .then(clickModifier)
            .padding(horizontal = metrics.padH),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
    ) {
        if (icon != null) {
            Icon(icon, null, tint = content, modifier = Modifier.size(15.dp))
            Box(Modifier.width(6.dp))
        }
        Text(text = text, style = metrics.style, color = content, maxLines = 1)
    }
}

/** 文本链（如「导出失败明细」）：无背景、品牌色、hover 下划线 */
@Composable
fun AppLink(text: String, onClick: () -> Unit, modifier: Modifier = Modifier, enabled: Boolean = true) {
    val hover = rememberHoverState()
    val hovered = hover.hovered()
    val color = if (enabled) AppColors.Brand else AppColors.Ink3
    Text(
        text = text,
        style = AppType.ButtonSmall,
        color = color,
        maxLines = 1,
        modifier = modifier.clickable(
            interactionSource = hover.interactionSource,
            indication = null,
            enabled = enabled,
            onClick = onClick,
        ).drawBehind {
            if (hovered && enabled) {
                val y = size.height - 1.dp.toPx()
                drawLine(color = color, start = androidx.compose.ui.geometry.Offset(0f, y), end = androidx.compose.ui.geometry.Offset(size.width, y), strokeWidth = 1.dp.toPx())
            }
        },
    )
}

/** 输入框：38dp 高、白底、1dp 描边，聚焦时品牌色 + 3dp 外环 */
@Composable
fun AppTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "",
    password: Boolean = false,
    readOnly: Boolean = false,
    mono: Boolean = false,
    disabled: Boolean = false,
    trailing: (@Composable RowScope.() -> Unit)? = null,
    imeAction: ImeAction = ImeAction.Next,
    onDone: () -> Unit = {},
) {
    var focused by remember { mutableStateOf(false) }
    var revealed by remember { mutableStateOf(false) }
    val hover = rememberHoverState()
    val borderColor = when {
        disabled -> AppColors.Line
        focused -> AppColors.Brand
        else -> AppColors.Line2
    }
    val textStyle = (if (mono) AppType.monoStyle(13) else AppType.Body).copy(
        color = when {
            disabled -> AppColors.Ink3
            readOnly -> AppColors.Ink2
            else -> AppColors.Ink
        },
    )
    Box(
        modifier = modifier
            .height(38.dp)
            .clip(AppShapes.Sm)
            .background(if (disabled) AppColors.Surface2 else if (readOnly) AppColors.Surface2 else AppColors.Surface)
            .border(1.dp, borderColor, AppShapes.Sm)
            .then(
                if (focused) {
                    Modifier.drawBehind {
                        drawRoundRect(
                            color = AppColors.Brand.copy(alpha = 0.14f),
                            topLeft = androidx.compose.ui.geometry.Offset(-2.dp.toPx(), -2.dp.toPx()),
                            size = androidx.compose.ui.geometry.Size(size.width + 4.dp.toPx(), size.height + 4.dp.toPx()),
                            cornerRadius = CornerRadius(10.dp.toPx(), 10.dp.toPx()),
                            style = Stroke(width = 3.dp.toPx()),
                        )
                    }
                } else {
                    Modifier
                },
            ),
        contentAlignment = Alignment.CenterStart,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(start = 12.dp, end = if (trailing != null) 4.dp else 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(Modifier.weight(1f), contentAlignment = Alignment.CenterStart) {
                if (value.isEmpty() && placeholder.isNotEmpty()) {
                    Text(placeholder, style = textStyle, color = AppColors.Ink3, maxLines = 1)
                }
                BasicTextField(
                    value = value,
                    onValueChange = onValueChange,
                    textStyle = textStyle,
                    cursorBrush = SolidColor(AppColors.Brand),
                    singleLine = true,
                    readOnly = readOnly,
                    enabled = !disabled,
                    visualTransformation = if (password && !revealed) PasswordVisualTransformation() else VisualTransformation.None,
                    keyboardOptions = KeyboardOptions(imeAction = imeAction),
                    keyboardActions = KeyboardActions(onDone = { onDone() }),
                    interactionSource = hover.interactionSource,
                    modifier = Modifier
                        .fillMaxWidth()
                        .onFocusChanged { focused = it.isFocused },
                )
            }
            if (password && !readOnly) {
                val tip = if (revealed) "隐藏密码" else "显示密码"
                IconTap(
                    icon = if (revealed) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility,
                    contentDescription = tip,
                    tint = AppColors.Ink3,
                    onClick = { revealed = !revealed },
                )
            }
            if (trailing != null) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp), content = trailing)
            }
        }
    }
}

/** 32dp 见方、圆角 8 的图标按钮（眼睛、关闭、选择文件） */
@Composable
fun IconTap(
    icon: ImageVector,
    contentDescription: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    tint: Color = AppColors.Ink3,
    size: Dp = 30.dp,
    iconSize: Dp = 18.dp,
    background: Color = Color.Transparent,
    backgroundHover: Color = AppColors.Surface2,
) {
    val hover = rememberHoverState()
    val hovered = hover.hovered()
    val bg by animateColorAsState(if (hovered) backgroundHover else background, tween(120), label = "icon-bg")
    Box(
        modifier = modifier
            .size(size)
            .clip(AppShapes.Sm)
            .background(bg)
            .clickable(interactionSource = hover.interactionSource, indication = null, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(icon, contentDescription, tint = tint, modifier = Modifier.size(iconSize))
    }
}

/**
 * Excel 文件选择区：虚线框 + 文件名。设计稿里它是抽屉里唯一「重」的控件，
 * 因为选文件是这一步真正的动作。
 */
@Composable
fun FileDropField(
    fileName: String?,
    path: String?,
    onPick: () -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "选择 Excel 文件",
    hint: String = "支持 .xlsx / .xls，读取第 1 个工作表",
) {
    val hover = rememberHoverState()
    val hovered = hover.hovered()
    val borderColor = if (hovered) AppColors.Brand else AppColors.Line2
    val bg = if (hovered) AppColors.BrandTint else AppColors.FieldBg
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(AppShapes.Md)
            .background(bg)
            .drawBehind {
                drawRoundRect(
                    color = borderColor,
                    size = size,
                    cornerRadius = CornerRadius(10.dp.toPx(), 10.dp.toPx()),
                    style = Stroke(width = 1.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(5.dp.toPx(), 4.dp.toPx()))),
                )
            }
            .clickable(interactionSource = hover.interactionSource, indication = null, onClick = onPick)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier.size(34.dp).clip(AppShapes.Sm).background(if (fileName != null) AppColors.OkTint else AppColors.Surface3),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = if (fileName != null) Icons.Outlined.Description else Icons.Outlined.UploadFile,
                contentDescription = null,
                tint = if (fileName != null) AppColors.Ok else AppColors.Ink3,
                modifier = Modifier.size(17.dp),
            )
        }
        Box(Modifier.width(10.dp))
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = fileName ?: placeholder,
                style = AppType.Body,
                color = if (fileName != null) AppColors.Ink else AppColors.Ink2,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = path ?: hint,
                style = AppType.Small,
                color = AppColors.Ink3,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Box(Modifier.width(8.dp))
        Text(
            text = if (fileName != null) "更换" else "浏览",
            style = AppType.ButtonSmall,
            color = AppColors.Brand,
            maxLines = 1,
        )
    }
}

enum class PillKind { Run, Ok, Err, Warn, Neutral }

@Composable
fun StatusPill(text: String, kind: PillKind, modifier: Modifier = Modifier) {
    val bg = when (kind) {
        PillKind.Run -> AppColors.BrandTint
        PillKind.Ok -> AppColors.OkTint
        PillKind.Err -> AppColors.ErrTint
        PillKind.Warn -> AppColors.WarnTint
        PillKind.Neutral -> AppColors.Surface2
    }
    val fg = when (kind) {
        PillKind.Run -> AppColors.Brand
        PillKind.Ok -> AppColors.Ok
        PillKind.Err -> AppColors.Err
        PillKind.Warn -> AppColors.Warn
        PillKind.Neutral -> AppColors.Ink2
    }
    Box(
        modifier = modifier
            .clip(AppShapes.Xs)
            .background(bg)
            .height(22.dp)
            .padding(horizontal = 8.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(text, style = AppType.Pill, color = fg, maxLines = 1)
    }
}

/** 设置页路径旁的「文件存在 / 文件不存在」小标签 */
@Composable
fun StatusChip(text: String, ok: Boolean, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(AppShapes.Xs)
            .background(if (ok) AppColors.OkTint else AppColors.ErrTint)
            .height(22.dp)
            .padding(horizontal = 7.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(text, style = AppType.Pill, color = if (ok) AppColors.Ok else AppColors.Err, maxLines = 1)
    }
}

/** 进度条：6dp 圆角轨道 + 品牌色填充 */
@Composable
fun AppProgressBar(
    fraction: Float,
    modifier: Modifier = Modifier,
    height: Dp = 6.dp,
    color: Color = AppColors.Brand,
    track: Color = AppColors.Surface3,
) {
    val animated by animateFloatAsState(fraction.coerceIn(0f, 1f), tween(320), label = "progress")
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .clip(AppShapes.Pill)
            .background(track),
    ) {
        Box(
            Modifier
                .fillMaxHeight()
                .fillMaxWidth(animated)
                .clip(AppShapes.Pill)
                .background(color),
        )
    }
}

/** 提示行：13dp 图标 + 12sp 说明文字 */
@Composable
fun HintLine(
    text: String,
    modifier: Modifier = Modifier,
    tint: Color = AppColors.Ink3,
    icon: ImageVector? = Icons.Outlined.Info,
) {
    Row(modifier = modifier, verticalAlignment = Alignment.Top, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        if (icon != null) {
            Icon(icon, null, tint = tint, modifier = Modifier.padding(top = 1.dp).size(13.dp))
        }
        Text(text, style = AppType.Small, color = tint)
    }
}

/** 1px 分隔线 */
@Composable
fun HairLine(modifier: Modifier = Modifier, color: Color = AppColors.Line) {
    Box(modifier.fillMaxWidth().height(1.dp).background(color))
}

/** 品牌标记：品牌蓝圆角方块 + 白十字 */
@Composable
fun BrandMark(size: Dp = 28.dp, modifier: Modifier = Modifier) {
    val radius = (size.value * 0.32f).dp
    Box(
        modifier = modifier.size(size).clip(RoundedCornerShape(radius)).background(AppColors.Brand),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            Modifier
                .width(size * 0.5f)
                .height(size * 0.145f)
                .background(Color.White, RoundedCornerShape(1.dp)),
        )
        Box(
            Modifier
                .width(size * 0.145f)
                .height(size * 0.5f)
                .background(Color.White, RoundedCornerShape(1.dp)),
        )
    }
}

/** 运行中的脉冲点：只在「正在跑」时用，静止文字里不要放 */
@Composable
fun PulseDot(color: Color = AppColors.Brand, size: Dp = 7.dp, modifier: Modifier = Modifier) {
    val transition = rememberInfiniteTransition(label = "pulse")
    val alpha by transition.animateFloat(
        initialValue = 1f,
        targetValue = 0.25f,
        animationSpec = infiniteRepeatable(tween(900), RepeatMode.Reverse),
        label = "pulse-alpha",
    )
    Box(modifier.size(size).clip(AppShapes.Pill).background(color.copy(alpha = alpha)))
}

/** 静态小圆点（域色 / 失败色） */
@Composable
fun Dot(color: Color, size: Dp = 6.dp, modifier: Modifier = Modifier) {
    Box(modifier.size(size).clip(AppShapes.Pill).background(color))
}
