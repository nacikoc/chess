package com.hilspot.chess.ui

import android.content.pm.PackageManager
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.github.bhlangonijr.chesslib.Piece
import com.github.bhlangonijr.chesslib.PieceType
import com.github.bhlangonijr.chesslib.Side
import com.github.bhlangonijr.chesslib.Square
import com.hilspot.chess.R
import com.hilspot.chess.game.GameMode
import com.hilspot.chess.game.GameResult
import com.hilspot.chess.game.GameViewModel
import com.hilspot.chess.ui.theme.BoardDark
import com.hilspot.chess.ui.theme.BoardLight
import com.hilspot.chess.ui.theme.FocusColor
import com.hilspot.chess.ui.theme.Gold
import com.hilspot.chess.ui.theme.HighlightLast
import com.hilspot.chess.ui.theme.HighlightSelected
import com.hilspot.chess.ui.theme.SelectedBorder
import com.hilspot.chess.ui.theme.TableGradient

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

/** Telefon düzeninin ortak metin rengi; masa zemini tema dosyasında. */
private val Ink = Color(0xFFEDE6DA)

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
            // Terfi diyaloğu yalnızca yatay dalda (TV/tablet). Telefonda terfi,
            // alt yuvanın kendisine dönüşür; ekran ortasında diyalog açılmaz.
            PromotionDialog(vm)
        } else {
            PortraitGame(vm, onExit)
        }
    }
}

/**
 * TELEFON (DİKEY) DÜZENİ — "aşağıya yaslanmış masa".
 *
 * Üstten alta: başlık şeridi · hamle şeridi · [esneme boşluğu] · rakip şeridi ·
 * TAHTA · kendi şeridin · yuvarlak kumanda yuvası.
 *
 * Tahta ile rakip şeridi tek blok halinde alta yaslanır: cihazdan cihaza değişen
 * fazlalık yükseklik TEK bir noktada, başparmağın zaten erişemediği üst banda
 * düşer. S24'te (kullanılabilir ~790dp) tahtanın alt kenarı ekran altından
 * ~154dp yukarıda kalır; önceki düzende bu 235dp idi.
 *
 * Taşma güvenliği: tahtanın büyüklüğü BoxWithConstraints ile min(genişlik,
 * kalan yükseklik) olarak ölçülür, sabit bir "chrome" sayısına dayanmaz — kısa
 * ekranlarda tahta küçülür, kumanda yuvası asla ekran dışına itilmez.
 */
@Composable
private fun PortraitGame(vm: GameViewModel, onExit: () -> Unit) {
    var confirmNew by remember { mutableStateOf(false) }
    // Sonuç perdesi kapatılabilir olmalı ki mat pozisyonu incelenebilsin.
    var resultSeen by remember(vm.result) { mutableStateOf(false) }

    val promoting = vm.pendingPromotion != null
    val resultVeil = vm.result != null && !resultSeen
    // Geri tuşu önce perdeyi/terfiyi kapatsın; oyundan çıkmak en son çare olsun.
    BackHandler(enabled = promoting || resultVeil) {
        if (promoting) vm.cancelPromotion() else resultSeen = true
    }

    Box(Modifier.fillMaxSize().background(TableGradient)) {
        BoxWithConstraints(
            Modifier
                .fillMaxSize()
                .safeDrawingPadding()
                .padding(horizontal = 6.dp, vertical = 6.dp)
        ) {
            // Çok kısa ekranlarda (katlanabilir kapak ekranı vb.) hamle şeridi
            // düşer ve butonlar küçülür; tahta yaşayacak yeri korur.
            val compact = maxHeight < 660.dp
            val inset = Modifier.padding(horizontal = 6.dp)
            val topSide = if (vm.flipped) Side.WHITE else Side.BLACK

            Column(Modifier.fillMaxSize()) {
                GameTopBar(vm, onExit, inset)
                if (!compact) {
                    Spacer(Modifier.height(8.dp))
                    MoveStrip(vm, inset)
                }
                Box(Modifier.fillMaxWidth().weight(1f)) {
                    Column(
                        Modifier.fillMaxWidth().align(Alignment.BottomCenter),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        PlayerBarFor(vm, topSide, inset.fillMaxWidth())
                        Spacer(Modifier.height(6.dp))
                        BoardSlot(vm, resultVeil) { resultSeen = true }
                    }
                }
                Spacer(Modifier.height(6.dp))
                PlayerBarFor(vm, topSide.flip(), inset.fillMaxWidth())
                Spacer(Modifier.height(if (compact) 8.dp else 10.dp))
                PortraitDock(
                    vm = vm,
                    compact = compact,
                    onNew = {
                        // Sürmekte olan oyun tek dokunuşla silinmesin.
                        if (vm.sans.isEmpty() || vm.result != null) vm.restart()
                        else confirmNew = true
                    },
                    modifier = inset
                )
            }
        }
    }

    if (confirmNew) {
        AlertDialog(
            onDismissRequest = { confirmNew = false },
            title = { Text(stringResource(R.string.new_game)) },
            text = { Text(stringResource(R.string.new_game_confirm)) },
            confirmButton = {
                FocusButton(
                    onClick = {
                        confirmNew = false
                        vm.restart()
                    },
                    primary = true
                ) { Text(stringResource(R.string.new_game)) }
            },
            dismissButton = {
                FocusButton(onClick = { confirmNew = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }
}

/**
 * Tahta yuvası: kalan yerin izin verdiği en büyük kare. Kare boyutu burada
 * ölçüldüğü için gölge ve perde tam tahtanın üstüne oturur.
 */
@Composable
private fun BoardSlot(
    vm: GameViewModel,
    showResult: Boolean,
    onDismissResult: () -> Unit
) {
    BoxWithConstraints(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
        val boardSize = minOf(maxWidth, maxHeight).coerceAtLeast(72.dp)
        Box(
            Modifier
                .size(boardSize)
                .shadow(14.dp, RoundedCornerShape(10.dp), clip = false)
        ) {
            ChessBoard(vm, Modifier.fillMaxSize())
            BoardVeil(vm, showResult, onDismissResult)
        }
    }
}

/**
 * Tahta üstü perde: terfi sırasında yönerge, oyun bitince kutlama.
 * Dikey düzende hiç yer kaplamaz; dokununca kapanır.
 */
@Composable
private fun BoxScope.BoardVeil(
    vm: GameViewModel,
    showResult: Boolean,
    onDismissResult: () -> Unit
) {
    val chessFont = rememberChessFont()
    val promoting = vm.pendingPromotion != null
    AnimatedVisibility(
        visible = promoting || showResult,
        enter = fadeIn(tween(if (promoting) 140 else 420)),
        exit = fadeOut(tween(140)),
        modifier = Modifier.matchParentSize()
    ) {
        val interaction = remember { MutableInteractionSource() }
        Box(
            Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(10.dp))
                .background(if (promoting) Color(0x8C0E0C09) else Color(0xD60E0C09))
                .clickable(interactionSource = interaction, indication = null) {
                    if (promoting) vm.cancelPromotion() else onDismissResult()
                },
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                if (promoting) {
                    Text(
                        text = stringResource(R.string.promotion_title),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = Ink
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = stringResource(R.string.cancel),
                        style = MaterialTheme.typography.labelMedium,
                        color = Ink.copy(alpha = 0.45f)
                    )
                } else {
                    Text(
                        text = when (vm.result) {
                            GameResult.WHITE_WINS -> "♔"
                            GameResult.BLACK_WINS -> "♚"
                            else -> "½"
                        },
                        fontFamily = chessFont,
                        fontSize = 62.sp,
                        color = Gold
                    )
                    Spacer(Modifier.height(10.dp))
                    Text(
                        text = statusText(vm),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold,
                        textAlign = TextAlign.Center,
                        color = Color(0xFFF2EADB),
                        modifier = Modifier.padding(horizontal = 20.dp)
                    )
                    Spacer(Modifier.height(12.dp))
                    Text(
                        text = stringResource(R.string.tap_to_close),
                        style = MaterialTheme.typography.labelMedium,
                        color = Ink.copy(alpha = 0.45f)
                    )
                }
            }
        }
    }
}

private enum class DockMode { PLAY, PROMOTION, OVER }

/**
 * Alt kumanda yuvası. Ekranın dokunulabilir her şeyi (tahta dışında) burada,
 * başparmağın dinlenme yayında toplanır. Duruma göre biçim değiştirir:
 * oyun sırasında üç yuvarlak düğme, terfide dört taş, oyun bitince geniş
 * "Yeni Oyun". Sık kullanılan "Geri Al" en sağda — sağ başparmağa en yakın.
 */
@Composable
private fun PortraitDock(
    vm: GameViewModel,
    compact: Boolean,
    onNew: () -> Unit,
    modifier: Modifier = Modifier
) {
    val chessFont = rememberChessFont()
    val diameter = if (compact) 50.dp else 56.dp
    val dockMin = if (compact) 54.dp else 76.dp
    val mode = when {
        vm.pendingPromotion != null -> DockMode.PROMOTION
        vm.result != null -> DockMode.OVER
        else -> DockMode.PLAY
    }
    Box(
        modifier.fillMaxWidth().heightIn(min = dockMin),
        contentAlignment = Alignment.Center
    ) {
        AnimatedContent(
            targetState = mode,
            transitionSpec = { fadeIn(tween(220)) togetherWith fadeOut(tween(120)) },
            label = "dock"
        ) { current ->
            when (current) {
                DockMode.PLAY -> Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RoundAction(
                        glyph = "↻",
                        label = stringResource(R.string.new_short),
                        diameter = diameter,
                        chessFont = chessFont,
                        showLabel = !compact,
                        onClick = onNew
                    )
                    RoundAction(
                        glyph = "⇅",
                        label = stringResource(R.string.flip_short),
                        diameter = diameter,
                        chessFont = chessFont,
                        showLabel = !compact,
                        onClick = { vm.toggleFlip() }
                    )
                    RoundAction(
                        glyph = "↺",
                        label = stringResource(R.string.undo),
                        diameter = diameter,
                        chessFont = chessFont,
                        showLabel = !compact,
                        dim = vm.sans.isEmpty() || vm.thinking,
                        onClick = { vm.undo() }
                    )
                }

                DockMode.PROMOTION -> Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    listOf(
                        PieceType.QUEEN to "♛",
                        PieceType.ROOK to "♜",
                        PieceType.BISHOP to "♝",
                        PieceType.KNIGHT to "♞"
                    ).forEach { (type, glyph) ->
                        RoundAction(
                            glyph = glyph,
                            label = "",
                            diameter = diameter,
                            chessFont = chessFont,
                            showLabel = false,
                            primary = type == PieceType.QUEEN,
                            glyphSize = 26.sp,
                            onClick = { vm.promote(type) }
                        )
                    }
                }

                DockMode.OVER -> Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RoundAction(
                        glyph = "↺",
                        label = "",
                        diameter = diameter,
                        chessFont = chessFont,
                        showLabel = false,
                        dim = vm.sans.isEmpty(),
                        onClick = { vm.undo() }
                    )
                    FocusButton(
                        onClick = onNew,
                        modifier = Modifier.weight(1f).height(diameter),
                        primary = true,
                        contentPadding = PaddingValues(horizontal = 12.dp),
                        shape = RoundedCornerShape(diameter / 2)
                    ) {
                        Text(
                            text = stringResource(R.string.new_game),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1
                        )
                    }
                }
            }
        }
    }
}

/** Yuvarlak glif düğmesi + altında küçük etiket. Etiket yazı boyutuyla büyür. */
@Composable
private fun RoundAction(
    glyph: String,
    label: String,
    diameter: Dp,
    chessFont: FontFamily,
    showLabel: Boolean = true,
    dim: Boolean = false,
    primary: Boolean = false,
    glyphSize: TextUnit = 22.sp,
    onClick: () -> Unit
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        FocusButton(
            onClick = onClick,
            modifier = Modifier.size(diameter),
            primary = primary,
            contentPadding = PaddingValues(0.dp),
            shape = CircleShape
        ) {
            Text(
                text = glyph,
                fontFamily = chessFont,
                fontSize = glyphSize,
                modifier = Modifier.alpha(if (dim) 0.35f else 1f)
            )
        }
        if (showLabel && label.isNotEmpty()) {
            Spacer(Modifier.height(4.dp))
            Text(
                text = label,
                fontSize = 10.sp,
                letterSpacing = 0.4.sp,
                maxLines = 1,
                color = Ink.copy(alpha = if (dim) 0.28f else 0.60f)
            )
        }
    }
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

/**
 * Telefonun üst çubuğu: çıkış (kazara basılmasın diye en uzak köşede),
 * hangi oyunda olduğun ("Bilgisayar · Usta") ve hamle sayacı.
 */
@Composable
private fun GameTopBar(vm: GameViewModel, onExit: () -> Unit, modifier: Modifier = Modifier) {
    val chessFont = rememberChessFont()
    val difficultyNames = stringArrayResource(R.array.difficulty_names)
    val label = if (vm.mode == GameMode.VS_COMPUTER) {
        stringResource(R.string.computer) + " · " + (difficultyNames.getOrNull(vm.level - 1) ?: "")
    } else {
        stringResource(R.string.play_two_players)
    }
    Box(
        modifier
            .fillMaxWidth()
            .heightIn(min = 44.dp)
    ) {
        FocusButton(
            onClick = onExit,
            modifier = Modifier.align(Alignment.CenterStart),
            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp)
        ) {
            Text("←", fontFamily = chessFont, fontSize = 17.sp)
        }
        Row(
            modifier = Modifier
                .align(Alignment.Center)
                .padding(horizontal = 62.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("♞", fontFamily = chessFont, fontSize = 15.sp, color = Gold)
            Spacer(Modifier.width(7.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                letterSpacing = 1.2.sp,
                color = Ink.copy(alpha = 0.62f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
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
 * Hamle şeridi. Tahtanın ÜSTÜNDE durur: okunur ama dokunulmaz bir bilgi olduğu
 * için başparmağın erişmediği banda aittir; tahtanın altındaki değerli yer
 * kumandaya kalır. Oyun bitince aynı yükseklikte altın sonuç afişine dönüşür,
 * böylece perde kapatıldıktan sonra da sonuç ekranda kalır.
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
                    color = Color(0xFF2A2415),
                    maxLines = 1
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
                        color = if (isLast) Color(0xFF2A2415) else Color(0xFFCFC7B8),
                        fontWeight = if (isLast) FontWeight.SemiBold else FontWeight.Normal,
                        maxLines = 1,
                        modifier = Modifier
                            .clip(RoundedCornerShape(7.dp))
                            .background(if (isLast) Gold else Color(0x14FFFFFF))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
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

/** Cihaz Android TV mi? Odak davranışı yalnızca kumandalı cihazlarda devreye girer. */
@Composable
internal fun rememberIsTv(): Boolean {
    val context = LocalContext.current
    return remember {
        context.packageManager.hasSystemFeature(PackageManager.FEATURE_LEANBACK)
    }
}

/** Verilen tarafın şahının bulunduğu kare; imlecin atlayacağı çapa. */
private fun kingSquareOf(squares: List<Piece>, side: Side): Square? {
    val i = squares.indexOfFirst { it.pieceType == PieceType.KING && it.pieceSide == side }
    return if (i >= 0) Square.values()[i] else null
}

@Composable
private fun ChessBoard(vm: GameViewModel, modifier: Modifier) {
    val chessFont = rememberChessFont()

    // İki kişilik oyunda sıra geçince imleç, oynayacak tarafın şahına atlar;
    // böylece yeni oyuncu kendi taşlarını aramak için tahtayı baştan geçmez.
    // Yalnızca TV'de: dokunmatikte imleç zaten yok, ortada çerçeve belirmesin.
    val followTurn = rememberIsTv() &&
        vm.mode == GameMode.TWO_PLAYERS &&
        vm.result == null &&
        vm.pendingPromotion == null
    val anchor = if (followTurn) kingSquareOf(vm.squares, vm.sideToMove) else null
    val anchorFocus = remember { FocusRequester() }
    LaunchedEffect(anchor, followTurn) {
        if (anchor == null) return@LaunchedEffect
        // Çapa kare yeni konumuna yerleşsin, sonra odağı iste.
        withFrameNanos { }
        runCatching { anchorFocus.requestFocus() }
    }

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
                        BoardCell(
                            vm = vm,
                            square = square,
                            cell = cell,
                            chessFont = chessFont,
                            focusRequester = if (square == anchor) anchorFocus else null
                        )
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
private fun BoardCell(
    vm: GameViewModel,
    square: Square,
    cell: Dp,
    chessFont: FontFamily,
    focusRequester: FocusRequester? = null
) {
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
            .then(
                if (focusRequester != null) Modifier.focusRequester(focusRequester)
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
    vm.pendingPromotion ?: return
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
                        contentPadding = PaddingValues(8.dp)
                    ) {
                        Text(glyph, fontSize = 40.sp, fontFamily = chessFont)
                    }
                }
            }
        }
    )
}
