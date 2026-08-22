package com.hilspot.chess

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
import com.hilspot.chess.game.GameViewModel
import com.hilspot.chess.ui.AboutScreen
import com.hilspot.chess.ui.GameScreen
import com.hilspot.chess.ui.HomeScreen
import com.hilspot.chess.ui.SupportScreen
import com.hilspot.chess.ui.theme.ChessTheme

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

private enum class Screen { HOME, GAME, ABOUT, SUPPORT }

@Composable
fun ChessApp(vm: GameViewModel = viewModel()) {
    var screen by rememberSaveable { mutableStateOf(Screen.HOME) }
    when (screen) {
        Screen.GAME -> {
            BackHandler { screen = Screen.HOME }
            GameScreen(vm = vm, onExit = { screen = Screen.HOME })
        }

        Screen.ABOUT -> {
            BackHandler { screen = Screen.HOME }
            AboutScreen(onBack = { screen = Screen.HOME })
        }

        Screen.SUPPORT -> {
            BackHandler { screen = Screen.HOME }
            SupportScreen(onBack = { screen = Screen.HOME })
        }

        Screen.HOME -> HomeScreen(
            onStart = { mode, side, level ->
                vm.newGame(mode, side, level)
                screen = Screen.GAME
            },
            onAbout = { screen = Screen.ABOUT },
            onSupport = { screen = Screen.SUPPORT }
        )
    }
}
