package com.hilspot.chess.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

val Gold = Color(0xFFE8C888)
val DarkBg = Color(0xFF171512)
val Surface1 = Color(0xFF23201B)
val BoardLight = Color(0xFFF0D9B5)
val BoardDark = Color(0xFFB58863)
val HighlightLast = Color(0x8CE8C24A)
val HighlightSelected = Color(0x9945804A)
val SelectedBorder = Color(0xFF7FE84A)
val FocusColor = Color(0xFF00E5FF)

/** Menü, oyun ve hakkında ekranlarının ortak "masa" zemini. */
val TableGradient = Brush.verticalGradient(
    0f to Color(0xFF14261D),
    0.5f to Color(0xFF181712),
    1f to Color(0xFF121009)
)

private val ColorScheme = darkColorScheme(
    primary = Gold,
    onPrimary = Color(0xFF2A2415),
    secondary = Color(0xFF9CB8A5),
    background = DarkBg,
    surface = Surface1,
    onBackground = Color(0xFFEDE6DA),
    onSurface = Color(0xFFEDE6DA)
)

@Composable
fun ChessTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = ColorScheme,
        content = content
    )
}
