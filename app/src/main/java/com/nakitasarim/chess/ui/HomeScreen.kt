package com.nakitasarim.chess.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.github.bhlangonijr.chesslib.Side
import com.nakitasarim.chess.R
import com.nakitasarim.chess.game.GameMode
import com.nakitasarim.chess.ui.theme.Gold

@Composable
fun HomeScreen(onStart: (GameMode, Side, Int) -> Unit) {
    var showOptions by remember { mutableStateOf(false) }
    var level by remember { mutableIntStateOf(4) }
    var side by remember { mutableStateOf(Side.WHITE) }
    val chessFont = rememberChessFont()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("♞", fontSize = 72.sp, fontFamily = chessFont, color = Gold)
        Text(
            text = stringResource(R.string.app_name),
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(32.dp))

        Button(
            onClick = { showOptions = !showOptions },
            modifier = Modifier
                .widthIn(max = 420.dp)
                .fillMaxWidth()
        ) {
            Text(stringResource(R.string.play_vs_computer), fontSize = 18.sp)
        }

        AnimatedVisibility(visible = showOptions) {
            Card(
                modifier = Modifier
                    .widthIn(max = 420.dp)
                    .fillMaxWidth()
                    .padding(top = 12.dp)
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text(stringResource(R.string.difficulty), style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        for (l in 1..8) {
                            val selectedLevel = l == level
                            if (selectedLevel) {
                                FilledTonalButton(
                                    onClick = { level = l },
                                    modifier = Modifier.weight(1f),
                                    contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp)
                                ) { Text("$l") }
                            } else {
                                OutlinedButton(
                                    onClick = { level = l },
                                    modifier = Modifier.weight(1f),
                                    contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp)
                                ) { Text("$l") }
                            }
                        }
                    }
                    Spacer(Modifier.height(16.dp))
                    Text(stringResource(R.string.play_as), style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        val whiteSel = side == Side.WHITE
                        if (whiteSel) {
                            FilledTonalButton(onClick = { side = Side.WHITE }, modifier = Modifier.weight(1f)) {
                                Text("♔ " + stringResource(R.string.white))
                            }
                        } else {
                            OutlinedButton(onClick = { side = Side.WHITE }, modifier = Modifier.weight(1f)) {
                                Text("♔ " + stringResource(R.string.white))
                            }
                        }
                        if (!whiteSel) {
                            FilledTonalButton(onClick = { side = Side.BLACK }, modifier = Modifier.weight(1f)) {
                                Text("♚ " + stringResource(R.string.black))
                            }
                        } else {
                            OutlinedButton(onClick = { side = Side.BLACK }, modifier = Modifier.weight(1f)) {
                                Text("♚ " + stringResource(R.string.black))
                            }
                        }
                    }
                    Spacer(Modifier.height(16.dp))
                    Button(
                        onClick = { onStart(GameMode.VS_COMPUTER, side, level) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(stringResource(R.string.start_game), fontSize = 16.sp)
                    }
                }
            }
        }

        Spacer(Modifier.height(12.dp))
        OutlinedButton(
            onClick = { onStart(GameMode.TWO_PLAYERS, Side.WHITE, 1) },
            modifier = Modifier
                .widthIn(max = 420.dp)
                .fillMaxWidth()
        ) {
            Text(stringResource(R.string.play_two_players), fontSize = 18.sp)
        }
    }
}
