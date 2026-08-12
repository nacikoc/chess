package com.nakitasarim.chess

import android.graphics.Color as AndroidColor
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.nakitasarim.chess.game.GameViewModel
import com.nakitasarim.chess.ui.GameScreen
import com.nakitasarim.chess.ui.HomeScreen
import com.nakitasarim.chess.ui.theme.ChessTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        // Android 15+ zaten kenardan kenara çizmeye zorluyor; şeffaf sistem
        // çubuklarını açıkça isteyip içeriği safeDrawing ile içeri alıyoruz.
        // Uygulama her zaman koyu tema olduğu için ikonlar açık renk olmalı.
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(AndroidColor.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(AndroidColor.TRANSPARENT)
        )
        super.onCreate(savedInstanceState)
        setContent {
            ChessTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    ChessApp()
                }
            }
        }
    }
}

@Composable
fun ChessApp(vm: GameViewModel = viewModel()) {
    var inGame by rememberSaveable { mutableStateOf(false) }
    if (inGame) {
        BackHandler { inGame = false }
        GameScreen(vm = vm, onExit = { inGame = false })
    } else {
        HomeScreen(onStart = { mode, side, level ->
            vm.newGame(mode, side, level)
            inGame = true
        })
    }
}
