package com.nakitasarim.chess.ui

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nakitasarim.chess.BuildConfig
import com.nakitasarim.chess.R
import com.nakitasarim.chess.ui.theme.Gold
import com.nakitasarim.chess.ui.theme.TableGradient

private const val SOURCE_URL = "https://github.com/nacikoc/chess"

/**
 * Hakkında / lisanslar. GPLv3 yükümlülüğü gereği uygulamanın özgür yazılım
 * olduğu ve kaynak koda nereden ulaşılacağı burada açıkça belirtilir.
 */
@Composable
fun AboutScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val chessFont = rememberChessFont()
    val isTv = rememberIsTv()
    val backFocus = remember { FocusRequester() }
    LaunchedEffect(Unit) {
        if (isTv) runCatching { backFocus.requestFocus() }
    }

    Box(
        Modifier
            .fillMaxSize()
            .background(TableGradient)
    ) {
        Column(
            Modifier
                .fillMaxSize()
                .safeDrawingPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                Modifier.fillMaxWidth().widthIn(max = 560.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                FocusButton(
                    onClick = onBack,
                    modifier = Modifier.focusRequester(backFocus),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    Text("←", fontFamily = chessFont, fontSize = 17.sp)
                }
                Spacer(Modifier.width(12.dp))
                Text(
                    stringResource(R.string.about),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(Modifier.height(20.dp))

            Column(Modifier.widthIn(max = 560.dp).fillMaxWidth()) {
                Text(
                    text = stringResource(R.string.app_name),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${stringResource(R.string.version)} ${BuildConfig.VERSION_NAME}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MutedInk
                )

                Spacer(Modifier.height(18.dp))
                InfoCard {
                    Text(
                        text = stringResource(R.string.free_software_title),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = stringResource(R.string.free_software_body),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MutedInk
                    )
                    Spacer(Modifier.height(12.dp))
                    Text(
                        text = stringResource(R.string.source_code),
                        style = MaterialTheme.typography.labelLarge,
                        color = MutedInk
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = SOURCE_URL,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Gold
                    )
                    Spacer(Modifier.height(10.dp))
                    // TV'de tarayıcı olmayabilir; adres yukarıda düz metin olarak
                    // da duruyor, buton yalnızca kolaylık.
                    FocusButton(onClick = {
                        runCatching {
                            context.startActivity(
                                Intent(Intent.ACTION_VIEW, Uri.parse(SOURCE_URL))
                            )
                        }.onFailure { if (it !is ActivityNotFoundException) throw it }
                    }) {
                        Text(stringResource(R.string.open_source_page), fontSize = 14.sp)
                    }
                }

                Spacer(Modifier.height(14.dp))
                SectionTitle(stringResource(R.string.third_party))
                Spacer(Modifier.height(8.dp))
                Credit(
                    "Stockfish 17.1",
                    "GNU GPL v3 · official-stockfish/Stockfish",
                    stringResource(R.string.credit_stockfish)
                )
                Credit(
                    "chesslib 1.3.4",
                    "Apache License 2.0 · bhlangonijr/chesslib",
                    stringResource(R.string.credit_chesslib)
                )
                Credit(
                    "DejaVu Sans",
                    "DejaVu Fonts License",
                    stringResource(R.string.credit_font)
                )
                Credit(
                    "Jetpack Compose",
                    "Apache License 2.0 · AOSP",
                    stringResource(R.string.credit_compose)
                )
                Spacer(Modifier.height(24.dp))
            }
        }
    }
}

private val MutedInk = Color(0xFF9A9184)

@Composable
private fun SectionTitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.SemiBold,
        color = Gold
    )
}

@Composable
private fun InfoCard(content: @Composable () -> Unit) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = Color(0xFF23201B),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.Top) { content() }
    }
}

@Composable
private fun Credit(name: String, license: String, description: String) {
    Column(Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
        Text(name, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
        Text(license, style = MaterialTheme.typography.labelMedium, color = MutedInk)
        Spacer(Modifier.height(2.dp))
        Text(description, style = MaterialTheme.typography.bodySmall, color = MutedInk)
    }
}
