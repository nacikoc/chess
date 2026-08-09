package com.nakitasarim.chess.engine

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.File

/**
 * Stockfish'i ayrı bir süreç olarak çalıştırıp UCI protokolüyle konuşur.
 * Binary, jniLibs içinde "libstockfish.so" adıyla paketlenir ve Android
 * tarafından nativeLibraryDir'e çıkarılır (useLegacyPackaging = true).
 */
class StockfishEngine(private val binary: File) : ChessAi {

    private var process: Process? = null
    private var writer: BufferedWriter? = null
    private var reader: BufferedReader? = null
    private val mutex = Mutex()

    // level 1..8 -> Stockfish "Skill Level" (0..20) + arama sınırı.
    // Alt seviyelerde derinlik 1-3 ile sınırlanır ki motor gerçekten
    // taş sarksın ve sıradan bir oyuncu tarafından yenilebilsin.
    private val skillLevels = intArrayOf(0, 1, 2, 5, 9, 13, 17, 20)
    private val goCommands = arrayOf(
        "go depth 1",
        "go depth 2",
        "go depth 3",
        "go movetime 150",
        "go movetime 300",
        "go movetime 500",
        "go movetime 800",
        "go movetime 1200"
    )

    private fun ensureStarted() {
        if (process?.isAlive == true) return
        val p = ProcessBuilder(binary.absolutePath)
            .redirectErrorStream(true)
            .start()
        process = p
        writer = p.outputStream.bufferedWriter()
        reader = p.inputStream.bufferedReader()
        send("uci")
        waitFor("uciok")
        send("setoption name Threads value 2")
    }

    private fun send(cmd: String) {
        writer?.apply {
            write(cmd)
            newLine()
            flush()
        }
    }

    private fun waitFor(prefix: String): String? {
        while (true) {
            val line = reader?.readLine() ?: return null
            if (line.startsWith(prefix)) return line
        }
    }

    override suspend fun bestMove(fen: String, level: Int): String? =
        mutex.withLock {
            withContext(Dispatchers.IO) {
                try {
                    ensureStarted()
                    val idx = (level - 1).coerceIn(0, 7)
                    send("setoption name Skill Level value ${skillLevels[idx]}")
                    send("position fen $fen")
                    send(goCommands[idx])
                    val line = waitFor("bestmove") ?: return@withContext null
                    line.split(" ").getOrNull(1)?.takeIf { it != "(none)" }
                } catch (_: Exception) {
                    runCatching { process?.destroy() }
                    process = null
                    null
                }
            }
        }

    override fun close() {
        runCatching { send("quit") }
        runCatching { process?.destroy() }
        process = null
    }
}
