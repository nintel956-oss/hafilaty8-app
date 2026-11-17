package com.haflati.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * لوحة ألوان حفلتي وفق معايير Material Design 3
 * اللون الأساسي: بنفسجي غامق (#4E1F7A)
 * اللون الثانوي: ذهبي ناعم (#C89B3C)
 * الخلفية: فاتحة دافئة (#FAF7F2)
 */
val HaflatiPurple = Color(0xFF4E1F7A)
val HaflatiPurpleDarkVariant = Color(0xFF3B155F)
val HaflatiSoftGold = Color(0xFFC89B3C)
val HaflatiGoldLight = Color(0xFFDFB75B)
val HaflatiWarmCanvas = Color(0xFFFAF7F2)
val HaflatiCardLight = Color(0xFFFFFFFF)
val HaflatiDarkCanvas = Color(0xFF14101B)
val HaflatiCardDark = Color(0xFF1E1828)

private val LightColorScheme = lightColorScheme(
    primary = HaflatiPurple,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFF1E7FA),
    onPrimaryContainer = HaflatiPurple,
    secondary = HaflatiSoftGold,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFF8EED6),
    onSecondaryContainer = Color(0xFF5D430C),
    background = HaflatiWarmCanvas,
    onBackground = Color(0xFF1D1626),
    surface = HaflatiCardLight,
    onSurface = Color(0xFF1D1626),
    surfaceVariant = Color(0xFFF3EDE3),
    onSurfaceVariant = Color(0xFF665C73),
    outline = Color(0x1F4E1F7A)
)

private val DarkColorScheme = darkColorScheme(
    primary = HaflatiPurple,
    onPrimary = Color.White,
    primaryContainer = Color(0xFF33184D),
    onPrimaryContainer = Color(0xFFEAD9FA),
    secondary = HaflatiGoldLight,
    onSecondary = Color(0xFF1D1626),
    secondaryContainer = Color(0xFF3B2E12),
    onSecondaryContainer = HaflatiGoldLight,
    background = HaflatiDarkCanvas,
    onBackground = Color(0xFFF6F1FA),
    surface = HaflatiCardDark,
    onSurface = Color(0xFFF6F1FA),
    surfaceVariant = Color(0xFF282035),
    onSurfaceVariant = Color(0xFFB6ACC4),
    outline = Color(0x24DFB75B)
)

/**
 * الخط الرسمي لتطبيق حفلتي: IBM Plex Sans Arabic
 * ملاحظة للبناء: عند إضافة ملفات ibm_plex_sans_arabic_*.ttf في res/font/
 * استبدل FontFamily.SansSerif بتعريف Font(R.font.ibm_plex_sans_arabic_*, ...)
 */
val IbmPlexSansArabicFontFamily: FontFamily = FontFamily.SansSerif

val HaflatiTypography = Typography(
    displaySmall = TextStyle(
        fontFamily = IbmPlexSansArabicFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 26.sp,
        lineHeight = 34.sp
    ),
    headlineMedium = TextStyle(
        fontFamily = IbmPlexSansArabicFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 20.sp,
        lineHeight = 28.sp
    ),
    titleLarge = TextStyle(
        fontFamily = IbmPlexSansArabicFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 18.sp,
        lineHeight = 26.sp
    ),
    titleMedium = TextStyle(
        fontFamily = IbmPlexSansArabicFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 15.sp,
        lineHeight = 22.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = IbmPlexSansArabicFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 15.sp,
        lineHeight = 22.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = IbmPlexSansArabicFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 13.sp,
        lineHeight = 19.sp
    ),
    labelLarge = TextStyle(
        fontFamily = IbmPlexSansArabicFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 13.sp,
        lineHeight = 18.sp
    )
)

val HaflatiShapes = Shapes(
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(18.dp),
    large = RoundedCornerShape(24.dp)
)

/**
 * ثيم حفلتي الرئيسي مع إجبار اتجاه الواجهة على RTL لدعم اللغة العربية بالكامل على الجوال
 */
@Composable
fun HaflatiTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit
) {
    val colors = if (darkTheme) DarkColorScheme else LightColorScheme
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        MaterialTheme(
            colorScheme = colors,
            typography = HaflatiTypography,
            shapes = HaflatiShapes,
            content = content
        )
    }
}
