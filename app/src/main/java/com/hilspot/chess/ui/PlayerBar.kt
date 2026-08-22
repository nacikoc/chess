package com.hilspot.chess.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.github.bhlangonijr.chesslib.Piece
import com.github.bhlangonijr.chesslib.PieceType
import com.github.bhlangonijr.chesslib.Side
import com.hilspot.chess.R
import com.hilspot.chess.game.GameMode
import com.hilspot.chess.game.GameViewModel
import com.hilspot.chess.ui.theme.Gold

/** Taş tipinin Unicode glifi (renkten bağımsız; renk fill ile verilir). */
internal fun chessGlyph(type: PieceType): String = when (type) {
    PieceType.KING -> "♚"
    PieceType.QUEEN -> "♛"
    PieceType.ROOK -> "♜"
    PieceType.BISHOP -> "♝"
    PieceType.KNIGHT -> "♞"
    else -> "♟"
}

/**
 * Verilen taraf için oyuncu şeridini oyun durumundan türetir.
 * Hem telefon (tahtanın alt/üstü) hem yatay panel bunu kullanır.
 */
@Composable
fun PlayerBarFor(vm: GameViewModel, side: Side, modifier: Modifier = Modifier) {
    val difficultyNames = stringArrayResource(R.array.difficulty_names)
    val isComputer = vm.mode == GameMode.VS_COMPUTER && side != vm.playerSide
    val name = when {
        vm.mode == GameMode.TWO_PLAYERS ->
            stringResource(if (side == Side.WHITE) R.string.white else R.string.black)
        isComputer -> stringResource(R.string.computer)
        else -> stringResource(R.string.you)
    }
    val subtitle = if (isComputer) difficultyNames.getOrNull(vm.level - 1) else null
    val captured = if (side == Side.WHITE) vm.capturedByWhite else vm.capturedByBlack
    val advantage =
        if (side == Side.WHITE) vm.materialBalance else -vm.materialBalance
    val isTurn = vm.result == null && vm.sideToMove == side
    PlayerBar(
        name = name,
        side = side,
        captured = captured,
        advantage = advantage.coerceAtLeast(0),
        isTurn = isTurn,
        thinking = isTurn && vm.thinking,
        inCheck = isTurn && vm.inCheck,
        subtitle = subtitle,
        modifier = modifier
    )
}

/**
 * Tahtanın üstünde/altında duran oyuncu şeridi: avatar, isim, aldığı taşlar ve
 * materyal üstünlüğü. Sırası gelen oyuncunun şeridi altın çerçeveyle vurgulanır.
 * Tamamen bilgilendirici — dokunma hedefi değil, bu yüzden ekranın üst (zor
 * erişilen) bölgesinde durması sorun değil.
 */
@Composable
fun PlayerBar(
    name: String,
    side: Side,
    captured: List<Piece>,
    advantage: Int,
    isTurn: Boolean,
    thinking: Boolean = false,
    inCheck: Boolean = false,
    subtitle: String? = null,
    modifier: Modifier = Modifier
) {
    val chessFont = rememberChessFont()
    val borderColor by animateColorAsState(
        if (isTurn) Gold.copy(alpha = 0.85f) else Color(0x1AFFFFFF),
        label = "turnBorder"
    )
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(Color(0xFF23201B))
            .border(if (isTurn) 2.dp else 1.dp, borderColor, RoundedCornerShape(14.dp))
            .padding(horizontal = 10.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(
                    if (side == Side.WHITE) Color(0xFFF2EADA) else Color(0xFF2C2822),
                    CircleShape
                )
                .border(
                    1.dp,
                    if (side == Side.WHITE) Color(0xFFBFB49C) else Color(0xFF4A443A),
                    CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "♚",
                fontFamily = chessFont,
                fontSize = 22.sp,
                color = if (side == Side.WHITE) Color(0xFF2A2415) else Color(0xFFE8E1D4)
            )
        }
        Spacer(Modifier.width(10.dp))
        Column(Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = name,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFFEDE6DA)
                )
                if (subtitle != null) {
                    Spacer(Modifier.width(6.dp))
                    LevelChip(subtitle)
                }
                if (inCheck) {
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = stringResource(R.string.check),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFE86A5A)
                    )
                }
            }
            Spacer(Modifier.height(2.dp))
            CapturedRow(captured, advantage, chessFont)
        }
        TurnIndicator(isTurn = isTurn, thinking = thinking)
    }
}

/** Bilgisayarın zorluk seviyesi rozeti ("Normal"). */
@Composable
private fun LevelChip(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelSmall,
        color = Gold,
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(Gold.copy(alpha = 0.14f))
            .padding(horizontal = 6.dp, vertical = 1.dp)
    )
}

/**
 * Sıra göstergesi: düşünürken üç kayan nokta, sırası gelmişken nabız gibi
 * atan tek altın nokta, aksi halde sönük boş nokta. Metin okumadan anlaşılır.
 */
@Composable
private fun TurnIndicator(isTurn: Boolean, thinking: Boolean) {
    val transition = rememberInfiniteTransition(label = "turn")
    Row(
        modifier = Modifier.width(34.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (thinking) {
            repeat(3) { i ->
                val alpha by transition.animateFloat(
                    initialValue = 0.25f,
                    targetValue = 1f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(600, delayMillis = i * 200),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "dot$i"
                )
                Box(
                    Modifier
                        .padding(horizontal = 1.5.dp)
                        .size(5.dp)
                        .background(Gold.copy(alpha = alpha), CircleShape)
                )
            }
        } else if (isTurn) {
            val alpha by transition.animateFloat(
                initialValue = 0.45f,
                targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(900),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "pulse"
            )
            Box(Modifier.size(9.dp).background(Gold.copy(alpha = alpha), CircleShape))
        } else {
            Box(
                Modifier
                    .size(9.dp)
                    .border(1.dp, Color(0x33FFFFFF), CircleShape)
            )
        }
    }
}

/** Alınan taşlar: üst üste binen küçük glifler + "+N" materyal farkı. */
@Composable
private fun CapturedRow(captured: List<Piece>, advantage: Int, chessFont: FontFamily) {
    if (captured.isEmpty() && advantage <= 0) {
        // Boş şerit yüksekliği sabit kalsın ki sıra değişince düzen zıplamasın
        Spacer(Modifier.height(16.dp))
        return
    }
    Row(verticalAlignment = Alignment.CenterVertically) {
        // Aynı tipteki taşlar hafif üst üste binerek yer kaplamasın
        Row(horizontalArrangement = Arrangement.spacedBy((-3).dp)) {
            captured.take(12).forEach { piece ->
                Text(
                    text = chessGlyph(piece.pieceType),
                    fontFamily = chessFont,
                    fontSize = 15.sp,
                    color = if (piece.pieceSide == Side.WHITE) Color(0xFFCFC7B8) else Color(0xFF7E766A),
                    modifier = Modifier.offset(y = 1.dp)
                )
            }
        }
        if (advantage > 0) {
            Spacer(Modifier.width(6.dp))
            Text(
                text = "+$advantage",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = Gold
            )
        }
    }
}
