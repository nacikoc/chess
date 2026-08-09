package com.nakitasarim.chess.ui

import android.content.pm.PackageManager
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringArrayResource
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
    var level by remember { mutableIntStateOf(3) } // varsayılan: Normal
    var side by remember { mutableStateOf(Side.WHITE) }
    val chessFont = rememberChessFont()
    val difficultyNames = stringArrayResource(R.array.difficulty_names)

    // TV'de açılışta ilk butona odaklan ki kumanda hemen çalışsın
    val context = LocalContext.current
    val isTv = remember {
        context.packageManager.hasSystemFeature(PackageManager.FEATURE_LEANBACK)
    }
    val firstFocus = remember { FocusRequester() }
    LaunchedEffect(Unit) {
        if (isTv) runCatching { firstFocus.requestFocus() }
    }

    Box(
        Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    0f to Color(0xFF14261D),
                    0.5f to Color(0xFF181712),
                    1f to Color(0xFF121009)
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(104.dp)
                    .background(
                        Brush.radialGradient(
                            listOf(Color(0xFF2E5C46), Color(0xFF1E4032))
                        ),
                        CircleShape
                    )
                    .border(2.dp, Gold.copy(alpha = 0.55f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text("♞", fontSize = 58.sp, fontFamily = chessFont, color = Gold)
            }
            Spacer(Modifier.height(14.dp))
            Text(
                text = stringResource(R.string.app_name),
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = stringResource(R.string.tagline),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.55f)
            )
            Spacer(Modifier.height(32.dp))

            FocusButton(
                onClick = { showOptions = !showOptions },
                modifier = Modifier
                    .widthIn(max = 420.dp)
                    .fillMaxWidth()
                    .focusRequester(firstFocus)
            ) {
                Text("♟  ", fontSize = 18.sp, fontFamily = chessFont, color = Gold)
                Text(stringResource(R.string.play_vs_computer), fontSize = 18.sp)
            }

            AnimatedVisibility(
                visible = showOptions,
                enter = expandVertically(tween(250)) + fadeIn(tween(250)),
                exit = shrinkVertically(tween(200)) + fadeOut(tween(150))
            ) {
                Card(
                    modifier = Modifier
                        .widthIn(max = 420.dp)
                        .fillMaxWidth()
                        .padding(top = 12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xCC23201B))
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                stringResource(R.string.difficulty),
                                style = MaterialTheme.typography.titleMedium
                            )
                            Spacer(Modifier.weight(1f))
                            AnimatedContent(
                                targetState = difficultyNames.getOrElse(level - 1) { "$level" },
                                label = "difficultyName"
                            ) { name ->
                                Text(
                                    name,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = Gold,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                        Spacer(Modifier.height(10.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            for (l in 1..8) {
                                FocusButton(
                                    onClick = { level = l },
                                    modifier = Modifier.weight(1f),
                                    selected = l == level,
                                    contentPadding = PaddingValues(vertical = 10.dp)
                                ) {
                                    Text("$l", fontSize = 16.sp)
                                }
                            }
                        }
                        Spacer(Modifier.height(16.dp))
                        Text(
                            stringResource(R.string.play_as),
                            style = MaterialTheme.typography.titleMedium
                        )
                        Spacer(Modifier.height(10.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            FocusButton(
                                onClick = { side = Side.WHITE },
                                modifier = Modifier.weight(1f),
                                selected = side == Side.WHITE
                            ) {
                                Text("♔ ", fontSize = 18.sp, fontFamily = chessFont)
                                Text(stringResource(R.string.white), fontSize = 16.sp)
                            }
                            FocusButton(
                                onClick = { side = Side.BLACK },
                                modifier = Modifier.weight(1f),
                                selected = side == Side.BLACK
                            ) {
                                Text("♚ ", fontSize = 18.sp, fontFamily = chessFont)
                                Text(stringResource(R.string.black), fontSize = 16.sp)
                            }
                        }
                        Spacer(Modifier.height(16.dp))
                        FocusButton(
                            onClick = { onStart(GameMode.VS_COMPUTER, side, level) },
                            modifier = Modifier.fillMaxWidth(),
                            primary = true
                        ) {
                            Text(
                                stringResource(R.string.start_game),
                                fontSize = 17.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(12.dp))
            FocusButton(
                onClick = { onStart(GameMode.TWO_PLAYERS, Side.WHITE, 1) },
                modifier = Modifier
                    .widthIn(max = 420.dp)
                    .fillMaxWidth()
            ) {
                Text("♙♟  ", fontSize = 18.sp, fontFamily = chessFont, color = Gold)
                Text(stringResource(R.string.play_two_players), fontSize = 18.sp)
            }
        }
    }
}
