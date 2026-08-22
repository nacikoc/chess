package com.hilspot.chess.ui

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.hilspot.chess.R
import com.hilspot.chess.support.SupportViewModel
import com.hilspot.chess.ui.theme.Gold
import com.hilspot.chess.ui.theme.TableGradient

private tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}

/**
 * Bağış ekranı. Bağış tamamen isteğe bağlıdır; hiçbir özelliği açmaz.
 * Bu, ekranda da açıkça yazılıdır — kullanıcı ödemezse bir şey kaçırmıyor.
 */
@Composable
fun SupportScreen(onBack: () -> Unit, vm: SupportViewModel = viewModel()) {
    val context = LocalContext.current
    val activity = remember(context) { context.findActivity() }
    val chessFont = rememberChessFont()
    val isTv = rememberIsTv()
    val backFocus = remember { FocusRequester() }
    LaunchedEffect(Unit) { if (isTv) runCatching { backFocus.requestFocus() } }

    Box(Modifier.fillMaxSize().background(TableGradient)) {
        Column(
            Modifier
                .fillMaxSize()
                .safeDrawingPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                Modifier.fillMaxWidth().widthIn(max = 520.dp),
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
                    stringResource(R.string.support),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(Modifier.height(24.dp))

            Box(
                Modifier
                    .size(88.dp)
                    .background(Gold.copy(alpha = 0.12f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text("♛", fontFamily = chessFont, fontSize = 46.sp, color = Gold)
            }

            Spacer(Modifier.height(16.dp))
            Column(
                Modifier.widthIn(max = 520.dp).fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    stringResource(R.string.support_title),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    stringResource(R.string.support_body),
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF9A9184),
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(24.dp))

                when (val s = vm.state) {
                    SupportViewModel.UiState.Loading -> CircularProgressIndicator(color = Gold)

                    SupportViewModel.UiState.Unavailable -> Notice(
                        text = stringResource(R.string.support_unavailable),
                        actionLabel = stringResource(R.string.retry),
                        onAction = vm::retry
                    )

                    is SupportViewModel.UiState.Ready -> Column(
                        Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        s.tiers.forEachIndexed { i, tier ->
                            FocusButton(
                                onClick = { activity?.let { vm.donate(it, tier) } },
                                modifier = Modifier.fillMaxWidth(),
                                primary = i == s.tiers.lastIndex,
                                contentPadding = PaddingValues(horizontal = 18.dp, vertical = 14.dp)
                            ) {
                                Text(
                                    text = "♟♞♛".getOrNull(i)?.toString() ?: "♟",
                                    fontFamily = chessFont,
                                    fontSize = 20.sp
                                )
                                Spacer(Modifier.width(12.dp))
                                Text(tier.name, fontSize = 16.sp)
                                Spacer(Modifier.weight(1f))
                                Text(tier.price, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }

                if (vm.purchasePending) {
                    Spacer(Modifier.height(14.dp))
                    Text(
                        stringResource(R.string.support_pending),
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF9A9184)
                    )
                }

                Spacer(Modifier.height(24.dp))
                Text(
                    stringResource(R.string.support_footnote),
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF7E766A),
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(28.dp))
            }
        }

        if (vm.thanks) {
            ThanksVeil(onDismiss = vm::dismissThanks)
        }
    }
}

@Composable
private fun Notice(text: String, actionLabel: String, onAction: () -> Unit) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = Color(0xFF23201B),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text,
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFF9A9184),
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(12.dp))
            FocusButton(onClick = onAction) { Text(actionLabel, fontSize = 14.sp) }
        }
    }
}

@Composable
private fun ThanksVeil(onDismiss: () -> Unit) {
    val chessFont = rememberChessFont()
    Box(
        Modifier
            .fillMaxSize()
            .background(Color(0xE6141210)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            Modifier.padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("♔", fontFamily = chessFont, fontSize = 72.sp, color = Gold)
            Spacer(Modifier.height(16.dp))
            Text(
                stringResource(R.string.support_thanks),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(8.dp))
            Text(
                stringResource(R.string.support_thanks_body),
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFF9A9184),
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(24.dp))
            FocusButton(onClick = onDismiss, primary = true) {
                Text(stringResource(R.string.close), fontSize = 16.sp)
            }
        }
    }
}
