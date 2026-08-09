package com.nakitasarim.chess.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.github.bhlangonijr.chesslib.Piece
import com.github.bhlangonijr.chesslib.PieceType
import com.github.bhlangonijr.chesslib.Side
import com.github.bhlangonijr.chesslib.Square
import com.nakitasarim.chess.R
import com.nakitasarim.chess.game.GameResult
import com.nakitasarim.chess.game.GameViewModel
import com.nakitasarim.chess.ui.theme.BoardDark
import com.nakitasarim.chess.ui.theme.BoardLight
import com.nakitasarim.chess.ui.theme.FocusColor
import com.nakitasarim.chess.ui.theme.HighlightLast
import com.nakitasarim.chess.ui.theme.HighlightSelected
import com.nakitasarim.chess.ui.theme.SelectedBorder

@Composable
fun rememberChessFont(): FontFamily =
    remember { FontFamily(Font(R.font.chess_font)) }

private fun pieceGlyph(piece: Piece): String? = when (piece.pieceType) {
    PieceType.KING -> "♚"
    PieceType.QUEEN -> "♛"
    PieceType.ROOK -> "♜"
    PieceType.BISHOP -> "♝"
    PieceType.KNIGHT -> "♞"
    PieceType.PAWN -> "♟"
    else -> null
}

@Composable
fun GameScreen(vm: GameViewModel, onExit: () -> Unit) {
    BoxWithConstraints(Modifier.fillMaxSize()) {
        val isWide = maxWidth > maxHeight
        if (isWide) {
            // Tablet/TV: panel genişliği ekranla orantılı büyür
            val panelWidth = (maxWidth * 0.28f).coerceIn(260.dp, 400.dp)
            Row(Modifier.fillMaxSize().padding(12.dp)) {
                ChessBoard(vm, Modifier.weight(1f).fillMaxHeight())
                Spacer(Modifier.width(12.dp))
                SidePanel(vm, onExit, Modifier.width(panelWidth).fillMaxHeight())
            }
        } else {
            Column(Modifier.fillMaxSize().padding(8.dp)) {
                ChessBoard(vm, Modifier.fillMaxWidth())
                Spacer(Modifier.height(8.dp))
                SidePanel(vm, onExit, Modifier.weight(1f).fillMaxWidth())
            }
        }
    }
    PromotionDialog(vm)
}

@Composable
private fun statusText(vm: GameViewModel): String = when (vm.result) {
    GameResult.WHITE_WINS -> stringResource(R.string.checkmate_white_wins)
    GameResult.BLACK_WINS -> stringResource(R.string.checkmate_black_wins)
    GameResult.STALEMATE -> stringResource(R.string.stalemate)
    GameResult.DRAW -> stringResource(R.string.draw)
    null -> {
        val turn = if (vm.sideToMove == Side.WHITE) {
            stringResource(R.string.white_to_move)
        } else {
            stringResource(R.string.black_to_move)
        }
        when {
            vm.thinking -> turn + "  •  " + stringResource(R.string.thinking)
            vm.inCheck -> turn + "  •  " + stringResource(R.string.check)
            else -> turn
        }
    }
}

@Composable
private fun SidePanel(vm: GameViewModel, onExit: () -> Unit, modifier: Modifier) {
    Column(modifier) {
        Text(
            text = statusText(vm),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(vertical = 6.dp)
        )
        val rows = vm.sans.chunked(2)
        val listState = rememberLazyListState()
        LaunchedEffect(rows.size) {
            if (rows.isNotEmpty()) listState.animateScrollToItem(rows.size - 1)
        }
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            items(rows.withIndex().toList()) { (i, pair) ->
                Text(
                    text = "${i + 1}. ${pair.getOrElse(0) { "" }}  ${pair.getOrElse(1) { "" }}",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(vertical = 2.dp)
                )
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            OutlinedButton(onClick = { vm.restart() }, modifier = Modifier.weight(1f)) {
                Text(stringResource(R.string.new_game))
            }
            OutlinedButton(onClick = { vm.undo() }, modifier = Modifier.weight(1f)) {
                Text(stringResource(R.string.undo))
            }
        }
        Spacer(Modifier.height(6.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            OutlinedButton(onClick = { vm.toggleFlip() }, modifier = Modifier.weight(1f)) {
                Text(stringResource(R.string.flip_board))
            }
            OutlinedButton(onClick = onExit, modifier = Modifier.weight(1f)) {
                Text(stringResource(R.string.back))
            }
        }
    }
}

@Composable
private fun ChessBoard(vm: GameViewModel, modifier: Modifier) {
    val chessFont = rememberChessFont()
    BoxWithConstraints(modifier, contentAlignment = Alignment.Center) {
        val size = if (maxWidth < maxHeight) maxWidth else maxHeight
        val cell = size / 8
        Column(Modifier.size(size)) {
            for (row in 0 until 8) {
                Row {
                    for (col in 0 until 8) {
                        val square = squareAt(row, col, vm.flipped)
                        BoardCell(vm, square, cell, chessFont)
                    }
                }
            }
        }
    }
}

private fun squareAt(row: Int, col: Int, flipped: Boolean): Square {
    val rank = if (flipped) row else 7 - row
    val file = if (flipped) 7 - col else col
    return Square.values()[rank * 8 + file]
}

@Composable
private fun BoardCell(vm: GameViewModel, square: Square, cell: Dp, chessFont: FontFamily) {
    val index = square.ordinal
    val file = index % 8
    val rank = index / 8
    val isLight = (file + rank) % 2 == 1
    val base = if (isLight) BoardLight else BoardDark
    val piece = vm.squares[index]
    val last = vm.lastMove
    val isLast = last != null && (square == last.first || square == last.second)
    val isSelected = vm.selected == square
    val isTarget = square in vm.targets

    val interaction = remember { MutableInteractionSource() }
    val focused by interaction.collectIsFocusedAsState()

    Box(
        modifier = Modifier
            .size(cell)
            .background(base)
            .then(if (isLast) Modifier.background(HighlightLast) else Modifier)
            .then(
                if (isSelected) Modifier
                    .background(HighlightSelected)
                    .border(cell / 10, SelectedBorder)
                else Modifier
            )
            .then(
                if (focused) Modifier.border(cell / 16, FocusColor)
                else Modifier
            )
            .clickable(interactionSource = interaction, indication = null) {
                vm.onSquareTapped(square)
            },
        contentAlignment = Alignment.Center
    ) {
        if (isTarget) {
            if (piece == Piece.NONE) {
                Box(
                    Modifier
                        .size(cell * 0.30f)
                        .background(Color(0x59225522), CircleShape)
                )
            } else {
                Box(
                    Modifier
                        .size(cell * 0.92f)
                        .border(cell / 14, Color(0x88225522), CircleShape)
                )
            }
        }
        val glyph = if (piece != Piece.NONE) pieceGlyph(piece) else null
        if (glyph != null) {
            val fontSize = with(LocalDensity.current) { (cell * 0.72f).toSp() }
            val isWhite = piece.pieceSide == Side.WHITE
            Text(
                text = glyph,
                fontSize = fontSize,
                fontFamily = chessFont,
                color = if (isWhite) Color(0xFFFAF6EE) else Color(0xFF17140F),
                style = TextStyle(
                    shadow = Shadow(
                        color = if (isWhite) Color(0xCC000000) else Color(0x66FFFFFF),
                        offset = Offset(0f, 2f),
                        blurRadius = 4f
                    )
                )
            )
        }
        // koordinat etiketleri
        val labelColor = if (isLight) BoardDark else BoardLight
        val labelSize = with(LocalDensity.current) { (cell * 0.18f).toSp() }
        if ((!vm.flipped && file == 0) || (vm.flipped && file == 7)) {
            Text(
                text = "${rank + 1}",
                fontSize = labelSize,
                color = labelColor,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(1.dp)
            )
        }
        if ((!vm.flipped && rank == 0) || (vm.flipped && rank == 7)) {
            Text(
                text = "${('a' + file)}",
                fontSize = labelSize,
                color = labelColor,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(1.dp)
            )
        }
    }
}

@Composable
private fun PromotionDialog(vm: GameViewModel) {
    val pending = vm.pendingPromotion ?: return
    val chessFont = rememberChessFont()
    AlertDialog(
        onDismissRequest = { vm.cancelPromotion() },
        title = { Text(stringResource(R.string.promotion_title)) },
        confirmButton = {},
        text = {
            Row(horizontalArrangement = Arrangement.SpaceEvenly, modifier = Modifier.fillMaxWidth()) {
                listOf(
                    PieceType.QUEEN to "♛",
                    PieceType.ROOK to "♜",
                    PieceType.BISHOP to "♝",
                    PieceType.KNIGHT to "♞"
                ).forEach { (type, glyph) ->
                    TextButton(onClick = { vm.promote(type) }) {
                        Text(glyph, fontSize = 40.sp, fontFamily = chessFont)
                    }
                }
            }
        }
    )
}
