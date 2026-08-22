package com.hilspot.chess.engine

import android.content.Context
import java.io.File

/**
 * Satranç yapay zekâsı arayüzü. Hamleler UCI formatında döner ("e2e4", "e7e8q").
 */
interface ChessAi {
    /** level: 1 (en kolay) .. 8 (en zor) */
    suspend fun bestMove(fen: String, level: Int): String?
    fun close() {}
}

object EngineFactory {
    /**
     * Cihaz ABI'sine uygun Stockfish binary'si paketlenmişse onu, yoksa
     * (örn. x86_64 emülatör) Kotlin ile yazılmış yedek motoru kullanır.
     */
    fun create(context: Context): ChessAi {
        val stockfish = File(context.applicationInfo.nativeLibraryDir, "libstockfish.so")
        return if (stockfish.canExecute()) StockfishEngine(stockfish) else BuiltInEngine()
    }
}
