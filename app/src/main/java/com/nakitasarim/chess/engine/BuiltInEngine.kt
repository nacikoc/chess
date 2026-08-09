package com.nakitasarim.chess.engine

import com.github.bhlangonijr.chesslib.Board
import com.github.bhlangonijr.chesslib.Piece
import com.github.bhlangonijr.chesslib.PieceType
import com.github.bhlangonijr.chesslib.Square
import com.github.bhlangonijr.chesslib.move.Move
import com.github.bhlangonijr.chesslib.move.MoveGenerator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.math.abs
import kotlin.random.Random

/**
 * Stockfish binary'si olmayan ABI'lar (örn. x86_64 emülatör) için
 * malzeme + konum değerlendirmeli basit alpha-beta motoru.
 */
class BuiltInEngine : ChessAi {

    private companion object {
        const val MATE = 100_000

        val PIECE_VALUES = mapOf(
            PieceType.PAWN to 100,
            PieceType.KNIGHT to 320,
            PieceType.BISHOP to 330,
            PieceType.ROOK to 500,
            PieceType.QUEEN to 900,
            PieceType.KING to 0
        )
    }

    override suspend fun bestMove(fen: String, level: Int): String? =
        withContext(Dispatchers.Default) {
            val board = Board()
            board.loadFromFen(fen)
            val moves = try {
                MoveGenerator.generateLegalMoves(board)
            } catch (_: Exception) {
                return@withContext null
            }
            if (moves.isEmpty()) return@withContext null

            // En kolay seviyede çoğunlukla rastgele oyna
            if (level <= 1 && Random.nextFloat() < 0.5f) {
                return@withContext moves.random().toUci()
            }

            val depth = when {
                level <= 2 -> 2
                level <= 5 -> 3
                else -> 4
            }
            // Düşük seviyelerde skora gürültü ekleyerek hata yaptır
            val noise = when (level) {
                1 -> 150
                2 -> 80
                3 -> 40
                else -> 0
            }

            var best = moves.first()
            var bestScore = Int.MIN_VALUE
            for (m in moves.sortedByDescending { mvvLva(board, it) }) {
                board.doMove(m)
                var score = -negamax(board, depth - 1, -MATE, MATE, 1)
                board.undoMove()
                if (noise > 0) score += Random.nextInt(-noise, noise + 1)
                if (score > bestScore) {
                    bestScore = score
                    best = m
                }
            }
            best.toUci()
        }

    private fun negamax(board: Board, depth: Int, alphaIn: Int, beta: Int, ply: Int): Int {
        val moves = try {
            MoveGenerator.generateLegalMoves(board)
        } catch (_: Exception) {
            return 0
        }
        if (moves.isEmpty()) {
            return if (board.isKingAttacked) -MATE + ply else 0
        }
        if (depth <= 0) return evaluate(board)

        var alpha = alphaIn
        for (m in moves.sortedByDescending { mvvLva(board, it) }) {
            board.doMove(m)
            val score = -negamax(board, depth - 1, -beta, -alpha, ply + 1)
            board.undoMove()
            if (score >= beta) return beta
            if (score > alpha) alpha = score
        }
        return alpha
    }

    private fun mvvLva(board: Board, move: Move): Int {
        val victim = board.getPiece(move.to)
        if (victim == Piece.NONE) return 0
        val attacker = board.getPiece(move.from)
        val v = PIECE_VALUES[victim.pieceType] ?: 0
        val a = PIECE_VALUES[attacker.pieceType] ?: 0
        return v * 10 - a
    }

    /** Sırası gelen taraf açısından skor (santipiyon). */
    private fun evaluate(board: Board): Int {
        var score = 0
        for (i in 0 until 64) {
            val sq = Square.values()[i]
            val piece = board.getPiece(sq)
            if (piece == Piece.NONE) continue
            val base = PIECE_VALUES[piece.pieceType] ?: 0
            val file = i % 8
            val rank = i / 8
            // merkez kontrolü küçük bonus
            var pos = (14 - ((abs(file * 2 - 7) + abs(rank * 2 - 7))) ) / 2
            // piyon ilerleme bonusu
            if (piece.pieceType == PieceType.PAWN) {
                pos += if (piece.pieceSide == com.github.bhlangonijr.chesslib.Side.WHITE) (rank - 1) * 4
                else (6 - rank) * 4
            }
            val total = base + pos
            score += if (piece.pieceSide == board.sideToMove) total else -total
        }
        return score
    }

    private fun Move.toUci(): String {
        val promo = if (promotion != null && promotion != Piece.NONE) {
            when (promotion.pieceType) {
                PieceType.QUEEN -> "q"
                PieceType.ROOK -> "r"
                PieceType.BISHOP -> "b"
                PieceType.KNIGHT -> "n"
                else -> ""
            }
        } else ""
        return from.value().lowercase() + to.value().lowercase() + promo
    }
}
