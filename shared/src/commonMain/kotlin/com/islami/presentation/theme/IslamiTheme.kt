package com.islami.presentation.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import islami_kmp.shared.generated.resources.Res
import islami_kmp.shared.generated.resources.cairo_bold
import islami_kmp.shared.generated.resources.cairo_medium
import islami_kmp.shared.generated.resources.cairo_regular
import islami_kmp.shared.generated.resources.cairo_semibold
import org.jetbrains.compose.resources.Font

/**
 * Brand colors that stay the same in light and dark mode, for the deep-green header bands on the
 * login and home screens. Everything else should use [MaterialTheme.colorScheme].
 */
object IslamiBrand {
    val Forest = Color(0xFF052A22)
    val OnForest = Color(0xFFFFFFFF)
    val Gold = Color(0xFFFFBA00)
}

// Every Material 3 role is set: unset ones fall back to the default purple/pink palette.
// Built from the mobile app's sage, forest and gold, on warm cream neutrals.
val LightColorScheme: ColorScheme = lightColorScheme(
    primary = Color(0xFF4A6B52),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFCCE8D2),
    onPrimaryContainer = Color(0xFF0A2614),
    inversePrimary = Color(0xFFA6D0AE),
    secondary = Color(0xFF052A22),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFD5E8E0),
    onSecondaryContainer = Color(0xFF052A22),
    // Brand gold (#FFBA00) is unreadable as text on cream, so the role uses a deep amber;
    // IslamiBrand.Gold is for gold on the forest header.
    tertiary = Color(0xFF7D5800),
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFFFDEA6),
    onTertiaryContainer = Color(0xFF271900),
    error = Color(0xFFAC0303),
    onError = Color(0xFFFFFFFF),
    errorContainer = Color(0xFFFFDAD5),
    onErrorContainer = Color(0xFF410001),
    background = Color(0xFFF7F4EF),
    onBackground = Color(0xFF1E1C19),
    surface = Color(0xFFFCFAF6),
    onSurface = Color(0xFF1E1C19),
    surfaceVariant = Color(0xFFEDE7DC),
    onSurfaceVariant = Color(0xFF57534C),
    surfaceTint = Color(0xFF4A6B52),
    inverseSurface = Color(0xFF33302B),
    inverseOnSurface = Color(0xFFF6F0E7),
    outline = Color(0xFF857F75),
    outlineVariant = Color(0xFFD8D1C5),
    scrim = Color(0xFF000000),
    surfaceBright = Color(0xFFFCFAF6),
    surfaceDim = Color(0xFFE2DCD2),
    surfaceContainerLowest = Color(0xFFFFFFFF),
    surfaceContainerLow = Color(0xFFF8F4EE),
    surfaceContainer = Color(0xFFF2EDE5),
    surfaceContainerHigh = Color(0xFFECE6DD),
    surfaceContainerHighest = Color(0xFFE6E0D6),
)

// Forest-tinted neutrals; accents are lightened so text keeps contrast on the dark background.
val DarkColorScheme: ColorScheme = darkColorScheme(
    primary = Color(0xFFA6D0AE),
    onPrimary = Color(0xFF0F3A1E),
    primaryContainer = Color(0xFF2F5238),
    onPrimaryContainer = Color(0xFFCCE8D2),
    inversePrimary = Color(0xFF4A6B52),
    secondary = Color(0xFFB4CCC3),
    onSecondary = Color(0xFF0B2A22),
    secondaryContainer = Color(0xFF18423A),
    onSecondaryContainer = Color(0xFFD5E8E0),
    tertiary = Color(0xFFFFC233),
    onTertiary = Color(0xFF3F2D00),
    tertiaryContainer = Color(0xFF5C4200),
    onTertiaryContainer = Color(0xFFFFDEA6),
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6),
    background = Color(0xFF07110E),
    onBackground = Color(0xFFE1E9E5),
    surface = Color(0xFF07110E),
    onSurface = Color(0xFFE1E9E5),
    surfaceVariant = Color(0xFF263A33),
    onSurfaceVariant = Color(0xFFAFC1BA),
    surfaceTint = Color(0xFFA6D0AE),
    inverseSurface = Color(0xFFE1E9E5),
    inverseOnSurface = Color(0xFF1A2420),
    outline = Color(0xFF7A8E87),
    outlineVariant = Color(0xFF2C413A),
    scrim = Color(0xFF000000),
    surfaceBright = Color(0xFF2B3833),
    surfaceDim = Color(0xFF07110E),
    surfaceContainerLowest = Color(0xFF040C0A),
    surfaceContainerLow = Color(0xFF0E1A16),
    surfaceContainer = Color(0xFF12201B),
    surfaceContainerHigh = Color(0xFF1B2A25),
    surfaceContainerHighest = Color(0xFF26352F),
)

/**
 * Cairo covers Arabic and Latin in one family. On the web it is also what makes Arabic render at
 * all, since Compose there draws text with bundled fonts only.
 */
@Composable
private fun cairoTypography(): Typography {
    val cairo = FontFamily(
        Font(Res.font.cairo_regular, FontWeight.Normal),
        Font(Res.font.cairo_medium, FontWeight.Medium),
        Font(Res.font.cairo_semibold, FontWeight.SemiBold),
        Font(Res.font.cairo_bold, FontWeight.Bold),
    )
    fun TextStyle.cairo() = copy(fontFamily = cairo)
    return with(Typography()) {
        Typography(
            displayLarge = displayLarge.cairo(),
            displayMedium = displayMedium.cairo(),
            displaySmall = displaySmall.cairo(),
            headlineLarge = headlineLarge.cairo(),
            headlineMedium = headlineMedium.cairo(),
            headlineSmall = headlineSmall.cairo(),
            titleLarge = titleLarge.cairo(),
            titleMedium = titleMedium.cairo(),
            titleSmall = titleSmall.cairo(),
            bodyLarge = bodyLarge.cairo(),
            bodyMedium = bodyMedium.cairo(),
            bodySmall = bodySmall.cairo(),
            labelLarge = labelLarge.cairo(),
            labelMedium = labelMedium.cairo(),
            labelSmall = labelSmall.cairo(),
        )
    }
}

@Composable
fun IslamiTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme,
        typography = cairoTypography(),
        content = content
    )
}
