package tech.kotlinhero.autohelper.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// 医疗场景的浅色主题：蓝色主色 + 中性灰蓝表面，避免默认紫色让界面显得偏粉
private val LightColors = lightColorScheme(
    primary = Color(0xFF0B63CE),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFD6E3FF),
    onPrimaryContainer = Color(0xFF001B3F),
    secondary = Color(0xFF1B7F5B),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFA8F0CB),
    onSecondaryContainer = Color(0xFF002114),
    background = Color(0xFFF7F9FC),
    onBackground = Color(0xFF1A1C1E),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF1A1C1E),
    surfaceVariant = Color(0xFFE1E7EF),
    onSurfaceVariant = Color(0xFF42474E),
    surfaceContainerLowest = Color(0xFFFFFFFF),
    surfaceContainerLow = Color(0xFFF6F8FB),
    surfaceContainer = Color(0xFFF0F3F8),
    surfaceContainerHigh = Color(0xFFEAEFF5),
    surfaceContainerHighest = Color(0xFFE3E9F0),
    surfaceTint = Color(0xFF0B63CE),
    error = Color(0xFFBA1A1A),
    onError = Color(0xFFFFFFFF),
    outline = Color(0xFF72777F),
    outlineVariant = Color(0xFFC2C7CF),
)

@Composable
fun DynamicConfigTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = LightColors,
        content = content,
    )
}
