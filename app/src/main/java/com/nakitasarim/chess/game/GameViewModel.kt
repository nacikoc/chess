package com.nakitasarim.chess.game

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.github.bhlangonijr.chesslib.Board
import com.github.bhlangonijr.chesslib.Piece
import com.github.bhlangonijr.chesslib.PieceType
import com.github.bhlangonijr.chesslib.Rank
import com.github.bhlangonijr.chesslib.Side
import com.github.bhlangonijr.chesslib.Square
import com.github.bhlangonijr.chesslib.move.Move
import com.github.bhlangonijr.chesslib.move.MoveGenerator
import com.github.bhlangonijr.chesslib.move.MoveList
import com.nakitasarim.chess.engine.ChessAi
import com.nakitasarim.chess.engine.EngineFactory
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

enum class GameMode { VS_COMPUTER, TWO_PLAYERS }

enum class GameResult { WHITE_WINS, BLACK_WINS, STALEMATE, DRAW }

class GameViewModel(app: Application) : AndroidViewModel(app) {

    private val board = Board()
    private val moveList = MoveList()
    private var engine: ChessAi? = null
    private var engineJob: Job? = null

    var mode by mutableStateOf(GameMode.TWO_PLAYERS); private set
    var playerSide: Side by mutableStateOf(Side.WHITE); private set
    var level by mutableStateOf(4); private set
    var flipped by mutableStateOf(false); private set

    var squares by mutableStateOf(readSquares()); private set
    var selected by mutableStateOf<Square?>(null); private set
    var targets by mutableStateOf<Set<Square>>(emptySet()); private set
    var lastMove by mutableStateOf<Pair<Square, Square>?>(null); private set
    var sans by mutableStateOf<List<String>>(emptyList()); private set
    var thinking by mutableStateOf(false); private set
    var pendingPromotion by mutableStateOf<Pair<Square, Square>?>(null); private set
    var result by mutableStateOf<GameResult?>(null); private set
    var sideToMove: Side by mutableStateOf(Side.WHITE); private set
    var inCheck by mutableStateOf(false); private set

    fun newGame(mode: GameMode, playerSide: Side, level: Int) {
        engineJob?.cancel()
        this.mode = mode
        this.playerSide = playerSide
        this.level = level
        board.loadFromFen(Board().fen) // başlangıç pozisyonu
        moveList.clear()
        selected = null
        targets = emptySet()
        lastMove = null
        pendingPromotion = null
        thinking = false
        flipped = mode == GameMode.VS_COMPUTER && playerSide == Side.BLACK
        refresh()
        if (mode == GameMode.VS_COMPUTER && playerSide == Side.BLACK) {
            engineMove()
        }
    }

    fun restart() = newGame(mode, playerSide, level)

    fun toggleFlip() {
        flipped = !flipped
    }

    fun onSquareTapped(sq: Square) {
        if (result != null || thinking || pendingPromotion != null) return
        val piece = board.getPiece(sq)
        val humanTurn = mode == GameMode.TWO_PLAYERS || board.sideToMove == playerSide
        if (!humanTurn) return

        if (piece != Piece.NONE && piece.pieceSide == board.sideToMove) {
            selected = sq
            targets = legalMoves().filter { it.from == sq }.map { it.to }.toSet()
            return
        }
        val from = selected
        if (from != null && sq in targets) {
            if (isPromotionMove(from, sq)) {
                pendingPromotion = from to sq
            } else {
                playHumanMove(Move(from, sq))
            }
        } else {
            selected = null
            targets = emptySet()
        }
    }

    fun promote(type: PieceType) {
        val (from, to) = pendingPromotion ?: return
        pendingPromotion = null
        playHumanMove(Move(from, to, Piece.make(board.sideToMove, type)))
    }

    fun cancelPromotion() {
        pendingPromotion = null
        selected = null
        targets = emptySet()
    }

    fun undo() {
        if (thinking || moveList.isEmpty()) return
        board.undoMove()
        moveList.removeLast()
        if (mode == GameMode.VS_COMPUTER) {
            // oyuncunun sırası gelene kadar geri al
            while (moveList.isNotEmpty() && board.sideToMove != playerSide) {
                board.undoMove()
                moveList.removeLast()
            }
        }
        selected = null
        targets = emptySet()
        pendingPromotion = null
        lastMove = moveList.lastOrNull()?.let { it.from to it.to }
        refresh()
    }

    private fun playHumanMove(move: Move) {
        if (!applyMove(move)) return
        if (mode == GameMode.VS_COMPUTER && result == null) {
            engineMove()
        }
    }

    private fun engineMove() {
        engineJob = viewModelScope.launch {
            thinking = true
            try {
                val ai = engine ?: EngineFactory.create(getApplication()).also { engine = it }
                val start = System.currentTimeMillis()
                val uci = ai.bestMove(board.fen, level)
                // Hamle göz ile takip edilebilsin diye en az ~1 sn düşünme süresi göster
                val elapsed = System.currentTimeMillis() - start
                if (elapsed < 1000) delay(1000 - elapsed)
                if (uci != null && result == null) {
                    applyMove(Move(uci, board.sideToMove))
                }
            } finally {
                thinking = false
            }
        }
    }

    private fun applyMove(move: Move): Boolean {
        val ok = try {
            board.doMove(move)
        } catch (_: Exception) {
            false
        }
        if (!ok) return false
        moveList.add(move)
        lastMove = move.from to move.to
        selected = null
        targets = emptySet()
        refresh()
        return true
    }

    private fun legalMoves(): List<Move> = try {
        MoveGenerator.generateLegalMoves(board)
    } catch (_: Exception) {
        emptyList()
    }

    private fun isPromotionMove(from: Square, to: Square): Boolean {
        val piece = board.getPiece(from)
        if (piece.pieceType != PieceType.PAWN) return false
        return to.rank == Rank.RANK_8 || to.rank == Rank.RANK_1
    }

    private fun refresh() {
        squares = readSquares()
        sideToMove = board.sideToMove
        inCheck = board.isKingAttacked
        sans = try {
            moveList.toSanArray().toList()
        } catch (_: Exception) {
            moveList.map { it.toString() }
        }
        result = when {
            board.isMated -> if (board.sideToMove == Side.BLACK) GameResult.WHITE_WINS else GameResult.BLACK_WINS
            board.isStaleMate -> GameResult.STALEMATE
            board.isDraw -> GameResult.DRAW
            else -> null
        }
    }

    private fun readSquares(): List<Piece> =
        List(64) { i -> board.getPiece(Square.values()[i]) }

    override fun onCleared() {
        engine?.close()
    }
}
