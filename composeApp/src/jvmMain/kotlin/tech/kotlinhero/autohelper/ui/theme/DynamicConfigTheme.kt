package tech.kotlinhero.autohelper.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

// 任务台设计稿的浅色主题：暖白底 + 医疗蓝主色，业务域色由 TaskDomain 单独提供。
// 只做浅色：设计稿明确没有深色主题（见 design/app-redesign/design-notes.md 第 7 节）。
private val LightColors = lightColorScheme(
    primary = AppColors.Brand,
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = AppColors.BrandTint,
    onPrimaryContainer = AppColors.BrandDeep,
    secondary = AppColors.Ok,
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = AppColors.OkTint,
    onSecondaryContainer = AppColors.Ok,
    tertiary = AppColors.Warn,
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = AppColors.WarnTint,
    onTertiaryContainer = AppColors.Warn,
    background = AppColors.Bg,
    onBackground = AppColors.Ink,
    surface = AppColors.Surface,
    onSurface = AppColors.Ink,
    surfaceVariant = AppColors.Surface2,
    onSurfaceVariant = AppColors.Ink2,
    surfaceContainerLowest = AppColors.Surface,
    surfaceContainerLow = AppColors.Bg,
    surfaceContainer = AppColors.Surface2,
    surfaceContainerHigh = AppColors.Surface3,
    surfaceContainerHighest = AppColors.Line,
    surfaceTint = AppColors.Brand,
    error = AppColors.Err,
    onError = Color(0xFFFFFFFF),
    errorContainer = AppColors.ErrTint,
    onErrorContainer = AppColors.Err,
    outline = AppColors.Line2,
    outlineVariant = AppColors.Line,
)

private val AppShapesM3 = Shapes(
    extraSmall = AppShapes.Xs,
    small = AppShapes.Sm,
    medium = AppShapes.Md,
    large = AppShapes.Lg,
    extraLarge = AppShapes.Lg,
)

private val AppTypography = Typography(
    headlineSmall = AppType.PageTitle,
    titleLarge = AppType.CardTitle,
    titleMedium = AppType.CardTitle,
    titleSmall = AppType.Label,
    bodyLarge = AppType.Body,
    bodyMedium = AppType.CardDesc,
    bodySmall = AppType.Small,
    labelLarge = AppType.Button,
    labelMedium = AppType.ButtonSmall,
    labelSmall = AppType.Pill,
)

@Composable
fun DynamicConfigTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = LightColors,
        shapes = AppShapesM3,
        typography = AppTypography,
        content = content,
    )
}
