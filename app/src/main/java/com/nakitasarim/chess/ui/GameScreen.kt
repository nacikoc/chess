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
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import com.nakitasarim.chess.ui.theme.Gold
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
            Row(Modifier.fillMaxSize().safeDrawingPadding().padding(12.dp)) {
                ChessBoard(vm, Modifier.weight(1f).fillMaxHeight())
                Spacer(Modifier.width(12.dp))
                SidePanel(vm, onExit, Modifier.width(panelWidth).fillMaxHeight())
            }
        } else {
            // Telefon (dikey): rakip kartı üstte, tahta ortada-aşağıda,
            // kendi kartın + hamleler + kontroller başparmak menzilinde.
            val topSide = if (vm.flipped) Side.WHITE else Side.BLACK
            // Tahtaya kenarlardan 6dp, diğer öğelere 12dp: tahta büyür,
            // kartlar ve kontroller kenardan rahat bir boşlukta kalır.
            val inset = Modifier.padding(horizontal = 6.dp)
            Column(
                Modifier
                    .fillMaxSize()
                    .safeDrawingPadding()
                    .padding(horizontal = 6.dp, vertical = 6.dp)
            ) {
                GameTopBar(vm, onExit, inset)
                Spacer(Modifier.height(6.dp))
                PlayerBarFor(vm, topSide, inset.fillMaxWidth())
                // Tahta artan boşluğun tamamını yutar; boşluğun çoğu üstte
                // bırakılarak tahta başparmağın rahat eriştiği banda iner.
                Column(Modifier.weight(1f).fillMaxWidth()) {
                    Spacer(Modifier.weight(0.62f))
                    ChessBoard(vm, Modifier.fillMaxWidth())
                    Spacer(Modifier.weight(0.38f))
                }
                PlayerBarFor(vm, topSide.flip(), inset.fillMaxWidth())
                Spacer(Modifier.height(8.dp))
                MoveStrip(vm, inset)
                Spacer(Modifier.height(8.dp))
                ActionDock(vm, inset)
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
private fun StatusChip(vm: GameViewModel, modifier: Modifier = Modifier) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = Color(0xFF23201B),
        modifier = modifier
    ) {
        Text(
            text = statusText(vm),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
        )
    }
}

/** Telefonun üst çubuğu: çıkış (kazara basılmasın diye en uzak köşede), başlık, hamle sayacı. */
@Composable
private fun GameTopBar(vm: GameViewModel, onExit: () -> Unit, modifier: Modifier = Modifier) {
    val chessFont = rememberChessFont()
    Box(
        modifier
            .fillMaxWidth()
            .height(44.dp)
    ) {
        FocusButton(
            onClick = onExit,
            modifier = Modifier.align(Alignment.CenterStart),
            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp)
        ) {
            Text("←", fontFamily = chessFont, fontSize = 17.sp)
        }
        Row(
            modifier = Modifier.align(Alignment.Center),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("♞", fontFamily = chessFont, fontSize = 17.sp, color = Gold)
            Spacer(Modifier.width(7.dp))
            Text(
                text = stringResource(R.string.app_name),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 2.sp
            )
        }
        Text(
            text = "${vm.sans.size / 2 + 1}.",
            style = MaterialTheme.typography.labelLarge,
            color = Color(0xFF9A9184),
            modifier = Modifier.align(Alignment.CenterEnd)
        )
    }
}

/**
 * Hamle şeridi. Oyun bitince aynı yükseklikte altın sonuç afişine dönüşür,
 * böylece sonuç "Yeni" düğmesinin hemen üstünde, başparmak menzilinde belirir.
 * Hamle yokken boş çubuk yerine ipucu gösterir.
 */
@Composable
private fun MoveStrip(vm: GameViewModel, modifier: Modifier = Modifier) {
    if (vm.result != null) {
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = Gold,
            modifier = modifier
                .fillMaxWidth()
                .height(46.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = statusText(vm),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF2A2415)
                )
            }
        }
        return
    }
    val rows = vm.sans.chunked(2)
    val state = rememberLazyListState()
    LaunchedEffect(rows.size) {
        if (rows.isNotEmpty()) state.animateScrollToItem(rows.size - 1)
    }
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = Color(0xFF23201B),
        modifier = modifier
            .fillMaxWidth()
            .height(46.dp)
    ) {
        if (rows.isEmpty()) {
            Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.first_move_hint),
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF8A8276)
                )
            }
        } else {
            LazyRow(
                state = state,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 10.dp)
            ) {
                items(rows.withIndex().toList()) { (i, pair) ->
                    val isLast = i == rows.size - 1
                    Text(
                        text = "${i + 1}. ${pair.getOrElse(0) { "" }} ${pair.getOrElse(1) { "" }}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (isLast) Gold else Color(0xFFCFC7B8),
                        fontWeight = if (isLast) FontWeight.SemiBold else FontWeight.Normal,
                        maxLines = 1,
                        modifier = Modifier
                            .clip(RoundedCornerShape(7.dp))
                            .background(if (isLast) Gold.copy(alpha = 0.14f) else Color(0x14FFFFFF))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }
    }
}

/**
 * Alt kontrol yuvası. Sık kullanılan "Geri Al" sağda — sağ başparmağın
 * doğal dinlenme noktası; "Yeni" en solda, yanlışlıkla basmak zor.
 */
@Composable
private fun ActionDock(vm: GameViewModel, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(66.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        DockButton("↻", stringResource(R.string.new_short), Modifier.weight(1f), primary = vm.result != null) {
            vm.restart()
        }
        DockButton("⇅", stringResource(R.string.flip_short), Modifier.weight(1f)) { vm.toggleFlip() }
        DockButton("↺", stringResource(R.string.undo), Modifier.weight(1.25f)) { vm.undo() }
    }
}

@Composable
private fun DockButton(
    glyph: String,
    label: String,
    modifier: Modifier,
    primary: Boolean = false,
    onClick: () -> Unit
) {
    val chessFont = rememberChessFont()
    FocusButton(
        onClick = onClick,
        modifier = modifier.fillMaxHeight(),
        primary = primary,
        contentPadding = PaddingValues(vertical = 6.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(glyph, fontFamily = chessFont, fontSize = 20.sp)
            Spacer(Modifier.height(2.dp))
            Text(label, fontSize = 11.sp, maxLines = 1)
        }
    }
}

@Composable
private fun SidePanel(vm: GameViewModel, onExit: () -> Unit, modifier: Modifier) {
    // Tahtanın üstündeki taraf: çevrilmemişse siyah üstte durur.
    val topSide = if (vm.flipped) Side.WHITE else Side.BLACK
    Column(modifier) {
        PlayerBarFor(vm, topSide, Modifier.fillMaxWidth())
        StatusChip(vm, Modifier.fillMaxWidth().padding(vertical = 6.dp))
        val rows = vm.sans.chunked(2)
        val listState = rememberLazyListState()
        LaunchedEffect(rows.size) {
            if (rows.isNotEmpty()) listState.animateScrollToItem(rows.size - 1)
        }
        Box(
            Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            if (rows.isEmpty()) {
                Text(
                    text = stringResource(R.string.first_move_hint),
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF8A8276),
                    modifier = Modifier.align(Alignment.Center)
                )
            } else {
                LazyColumn(state = listState, modifier = Modifier.fillMaxSize()) {
                    items(rows.withIndex().toList()) { (i, pair) ->
                        Text(
                            text = "${i + 1}. ${pair.getOrElse(0) { "" }}  ${pair.getOrElse(1) { "" }}",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(vertical = 2.dp)
                        )
                    }
                }
            }
        }
        PlayerBarFor(vm, topSide.flip(), Modifier.fillMaxWidth().padding(bottom = 8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            FocusButton(onClick = { vm.restart() }, modifier = Modifier.weight(1f)) {
                Text(stringResource(R.string.new_game))
            }
            FocusButton(onClick = { vm.undo() }, modifier = Modifier.weight(1f)) {
                Text(stringResource(R.string.undo))
            }
        }
        Spacer(Modifier.height(6.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            FocusButton(onClick = { vm.toggleFlip() }, modifier = Modifier.weight(1f)) {
                Text(stringResource(R.string.flip_board))
            }
            FocusButton(onClick = onExit, modifier = Modifier.weight(1f)) {
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
        val cell = (size - 8.dp) / 8
        Column(
            Modifier
                .size(size)
                .clip(RoundedCornerShape(10.dp))
                .background(Color(0xFF6B5232))
                .border(2.dp, Color(0xFF8A6B3F), RoundedCornerShape(10.dp))
                .padding(4.dp)
        ) {
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
                    FocusButton(
                        onClick = { vm.promote(type) },
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(8.dp)
                    ) {
                        Text(glyph, fontSize = 40.sp, fontFamily = chessFont)
                    }
                }
            }
        }
    )
}
