package com.hilspot.chess.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp
import com.hilspot.chess.ui.theme.FocusColor
import com.hilspot.chess.ui.theme.Gold
import com.hilspot.chess.ui.theme.Surface1

/**
 * TV kumandası (D-pad) ile gezinirken odağın nerede olduğunu net gösteren buton:
 * odaklanınca %5 büyür, kalın camgöbeği çerçeve alır ve zemini aydınlanır.
 * [selected]/[primary] doluysa altın zeminle vurgulanır (seçili seçenek / ana eylem).
 * [shape] varsayılanı korur; telefonun alt yuvası yuvarlak butonlar için CircleShape verir.
 */
@Composable
fun FocusButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    selected: Boolean = false,
    primary: Boolean = false,
    contentPadding: PaddingValues = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
    shape: Shape = RoundedCornerShape(12.dp),
    content: @Composable () -> Unit
) {
    val interaction = remember { MutableInteractionSource() }
    val focused by interaction.collectIsFocusedAsState()
    val scale by animateFloatAsState(if (focused) 1.05f else 1f, label = "focusScale")
    val filled = selected || primary
    val bg = when {
        filled && focused -> Color(0xFFF6E3B4)
        filled -> Gold
        focused -> Color(0xFF37474F)
        else -> Surface1
    }
    val contentColor = if (filled) Color(0xFF2A2415) else Color(0xFFEDE6DA)
    Row(
        modifier = modifier
            .scale(scale)
            .clip(shape)
            .background(bg)
            .border(
                width = if (focused) 3.dp else 1.dp,
                color = if (focused) FocusColor else Color(0x33FFFFFF),
                shape = shape
            )
            .clickable(interactionSource = interaction, indication = null, onClick = onClick)
            .padding(contentPadding),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        CompositionLocalProvider(LocalContentColor provides contentColor) {
            content()
        }
    }
}
