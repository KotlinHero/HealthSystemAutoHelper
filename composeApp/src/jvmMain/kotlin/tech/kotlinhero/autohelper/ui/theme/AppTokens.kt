package tech.kotlinhero.autohelper.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * 任务台设计稿（design/app-redesign）里的颜色令牌。
 * MaterialTheme.colorScheme 覆盖了通用语义色，这里放它没有槽位的部分：业务域色、成功/告警色与它们的浅底。
 */
object AppColors {
    val Bg = Color(0xFFFBFAF8)
    val Surface = Color(0xFFFFFFFF)
    val Surface2 = Color(0xFFF5F2ED)
    val Surface3 = Color(0xFFEDE9E1)
    val FieldBg = Color(0xFFFCFBF9)

    val Ink = Color(0xFF171A1F)
    val Ink2 = Color(0xFF4B525D)
    val Ink3 = Color(0xFF6B7280)

    val Line = Color(0xFFE7E2D9)
    val Line2 = Color(0xFFD8D2C6)
    val LineHover = Color(0xFFC9C2B4)

    val Brand = Color(0xFF1857D6)
    val BrandDeep = Color(0xFF123F9E)
    val BrandTint = Color(0xFFEDF2FE)
    val BrandTintHover = Color(0xFFE1EAFD)

    val Ok = Color(0xFF17703C)
    val OkTint = Color(0xFFE7F3EB)
    val Warn = Color(0xFF8A5A00)
    val WarnTint = Color(0xFFFAEFDC)
    val Err = Color(0xFFBE3327)
    val ErrTint = Color(0xFFFBEDEA)
    val ErrLine = Color(0xFFEFCFC9)

    /** 抽屉遮罩：rgba(23,26,31,.28) */
    val Scrim = Color(0x47171A1F)
}

/** 四个业务域：组头竖条、卡片图标底色都取这里，避免 8 张卡片长得一样 */
enum class TaskDomain(
    val color: Color,
    val tint: Color,
) {
    Hypertension(Color(0xFFD93A3A), Color(0xFFFBECEA)),
    Diabetes(Color(0xFFD98324), Color(0xFFFBF0DE)),
    HealthForm(Color(0xFF1F8A4C), Color(0xFFE8F4EC)),
    Contract(Color(0xFF5B5BD6), Color(0xFFEDF0FD)),
}

/** 字阶：中文界面用系统字体，日志与路径用等宽 */
object AppType {
    val PageTitle = TextStyle(fontSize = 20.sp, lineHeight = 28.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 0.1.sp)
    val CardTitle = TextStyle(fontSize = 15.sp, lineHeight = 21.sp, fontWeight = FontWeight.SemiBold)
    val CardDesc = TextStyle(fontSize = 13.sp, lineHeight = 19.sp, fontWeight = FontWeight.Normal)
    val Meta = TextStyle(fontSize = 12.5.sp, lineHeight = 18.sp, fontWeight = FontWeight.Normal)
    val MetaStrong = TextStyle(fontSize = 12.5.sp, lineHeight = 18.sp, fontWeight = FontWeight.SemiBold)
    val Small = TextStyle(fontSize = 12.sp, lineHeight = 17.sp, fontWeight = FontWeight.Normal)
    val SmallStrong = TextStyle(fontSize = 12.sp, lineHeight = 17.sp, fontWeight = FontWeight.SemiBold)
    val Body = TextStyle(fontSize = 14.sp, lineHeight = 21.sp, fontWeight = FontWeight.Normal)
    val Label = TextStyle(fontSize = 13.sp, lineHeight = 19.sp, fontWeight = FontWeight.SemiBold)
    val Button = TextStyle(fontSize = 13.5.sp, lineHeight = 18.sp, fontWeight = FontWeight.SemiBold)
    val ButtonSmall = TextStyle(fontSize = 12.5.sp, lineHeight = 17.sp, fontWeight = FontWeight.SemiBold)
    val ButtonLarge = TextStyle(fontSize = 14.sp, lineHeight = 19.sp, fontWeight = FontWeight.SemiBold)
    val Nav = TextStyle(fontSize = 13.5.sp, lineHeight = 18.sp, fontWeight = FontWeight.SemiBold)
    val Pill = TextStyle(fontSize = 11.5.sp, lineHeight = 15.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 0.1.sp)
    val BrandName = TextStyle(fontSize = 14.5.sp, lineHeight = 20.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 0.1.sp)
    val BigNum = TextStyle(fontSize = 22.sp, lineHeight = 24.sp, fontWeight = FontWeight.SemiBold, letterSpacing = (-0.2).sp)

    fun logStyle() = TextStyle(fontFamily = FontFamily.Monospace, fontSize = 12.5.sp, lineHeight = 22.sp)
    fun monoStyle(size: Int = 13) = TextStyle(fontFamily = FontFamily.Monospace, fontSize = size.sp, lineHeight = 19.sp)
}

/** 圆角：6 / 8 / 10 / 14 */
object AppShapes {
    val Xs = RoundedCornerShape(6.dp)
    val Sm = RoundedCornerShape(8.dp)
    val Md = RoundedCornerShape(10.dp)
    val Lg = RoundedCornerShape(14.dp)
    val Pill = RoundedCornerShape(999.dp)
}
