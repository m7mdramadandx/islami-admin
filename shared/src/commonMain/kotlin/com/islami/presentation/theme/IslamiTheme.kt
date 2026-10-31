package com.islami.presentation.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val sage = Color(0xFF4A6B52)
private val darkForest = Color(0xFF052A22)
private val vibrantYellow = Color(0xFFFFBA00)

private val sageDarkTheme = Color(0xFF5A7F62)
private val vibrantYellowDarkTheme = Color(0xFFFFC233)

private val appBar = darkForest
private val appBarDark = Color(0xFF040F0C)

private val primary = sage
private val primaryDark = sageDarkTheme

private val secondary = darkForest
private val secondaryDark = darkForest

private val accent = vibrantYellow
private val accentDark = vibrantYellowDarkTheme

private val background = Color(0xFFF7F4EF)
private val backgroundDark = Color(0xFF040F0C)

private val whiteSolid = Color(0xFFFFFFFF)
private val blackSolid = Color(0xFF000000)

private val white = Color(0xFFFFFFFF)
private val whiteDark = Color(0xFF000000)

private val black = Color(0xFF000000)
private val blackDark = Color(0xFFFFFFFF)

private val grey = Color(0xFF5C5A56)
private val greyDark = Color(0xFFA8B8B2)

private val popUp = Color(0xFFFFFCF9)
private val popUpDark = Color(0xFF0F352C)

private val buttonSecondary = Color(0xFFEEE9E0)
private val buttonSecondaryDark = Color(0xFF18423A)

private val dimmed = Color(0xFFEEE9E0)
private val dimmedDark = Color(0x6618423A)


private val border = Color(0xFFE5E0D6)
private val borderDark = Color(0xFF245447)

// Align with core `values` / `values-night` color resources (natural scale, info, frames).
private val neutral50Light = Color(0xFFFFFCF9)
private val neutral50Dark = Color(0xFF040F0C)

private val neutral100Light = Color(0xFFF7F4EF)
private val neutral100Dark = Color(0xFF052A22)

private val neutral200Light = Color(0xFFEEE9E0)
private val neutral200Dark = Color(0xFF0F352C)

private val neutral500Light = Color(0xFF6B6660)
private val neutral500Dark = Color(0xFF7A9188)

private val neutral600Light = Color(0xFF4A4742)
private val neutral600Dark = Color(0xFFA8B8B2)

private val neutral700Light = Color(0xFF383532)
private val neutral700Dark = Color(0xFFC5D1CC)

private val neutral800Light = Color(0xFF242220)
private val neutral800Dark = Color(0xFFE0E8E5)

private val neutral900Light = Color(0xFF141211)
private val neutral900Dark = Color(0xFFF5F9F7)

private val silverGreyLight = Color(0xFFEEE9E0)
private val silverGreyDark = Color(0xFF18423A)

private val whiteSilverLight = Color(0xFFFFFFFF)
private val whiteSilverDark = Color(0xFFE8F0ED)

private val infoLight = Color(0xFF3b82f6)
private val infoDark = Color(0xFFA8B8B2)

private val successLight = Color(0xFF198754)
private val successDark = Color(0xFF5A7F62)

private val frameRed = Color(0xFFD72323)
private val frameGreen = Color(0xFF1F612A)
private val frameBlueLight = Color(0xFF3b82f6)
private val frameBlueDark = Color(0xFF445DEB)

private val error = Color(0xFFAC0303)

// Native MaterialTheme Colors
val LightColorScheme = lightColorScheme(
    primary = primary,
    onPrimary = white,
    primaryContainer = sage.copy(alpha = 0.85f),
    onPrimaryContainer = darkForest,
    secondary = secondary,
    onSecondary = white,
    secondaryContainer = darkForest.copy(alpha = 0.85f),
    onSecondaryContainer = white,
    tertiary = accent,
    onTertiary = darkForest,
    background = background,
    onBackground = Color(0xFF242220),
    surface = whiteSolid,
    onSurface = Color(0xFF242220),
    surfaceVariant = popUp,
    onSurfaceVariant = grey,
    error = error,
    onError = white,
    outline = border,
    outlineVariant = border.copy(alpha = 0.7f),
    inverseSurface = darkForest,
    inverseOnSurface = white,
)

val DarkColorScheme = darkColorScheme(
    primary = primaryDark,
    onPrimary = darkForest,
    primaryContainer = sageDarkTheme.copy(alpha = 0.35f),
    onPrimaryContainer = Color(0xFFE0E8E5),
    secondary = secondaryDark,
    onSecondary = white,
    secondaryContainer = Color(0xFF18423A),
    onSecondaryContainer = Color(0xFFE0E8E5),
    tertiary = accentDark,
    onTertiary = darkForest,
    background = backgroundDark,
    onBackground = Color(0xFFF5F9F7),
    surface = appBarDark,
    onSurface = Color(0xFFF5F9F7),
    surfaceVariant = popUpDark,
    onSurfaceVariant = greyDark,
    error = error,
    onError = white,
    outline = borderDark,
    outlineVariant = borderDark.copy(alpha = 0.7f),
    inverseSurface = sageDarkTheme,
    inverseOnSurface = darkForest,
)

@Composable
fun IslamiTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography(),
        content = content
    )
}
