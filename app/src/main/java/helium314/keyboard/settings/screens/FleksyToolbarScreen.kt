// SPDX-License-Identifier: GPL-3.0-only
package helium314.keyboard.settings.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import helium314.keyboard.keyboard.FleksyColors
import helium314.keyboard.latin.suggestions.FleksyToolbarSizes
import helium314.keyboard.latin.utils.BackButton
import helium314.keyboard.latin.utils.prefs
import helium314.keyboard.latin.settings.Defaults
import helium314.keyboard.latin.settings.Settings
import androidx.core.content.edit
import kotlin.math.roundToInt

/** Taille de la barre d'outils et du texte des suggestions, avec aperçu. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FleksyToolbarScreen(onClickBack: () -> Unit) {
    val ctx = LocalContext.current
    var height by remember { mutableFloatStateOf(FleksyToolbarSizes.heightScale(ctx)) }
    var text by remember { mutableFloatStateOf(FleksyToolbarSizes.textScale(ctx)) }
    var preview by remember { mutableFloatStateOf(FleksyToolbarSizes.previewHeight(ctx).toFloat()) }
    val prefs = ctx.prefs()
    var symbolsDelay by remember { mutableFloatStateOf(
        prefs.getInt(Settings.PREF_KEY_LONGPRESS_TIMEOUT, Defaults.PREF_KEY_LONGPRESS_TIMEOUT).toFloat()) }
    var accentDelay by remember { mutableFloatStateOf(FleksyToolbarSizes.accentDelay(ctx).toFloat()) }
    val barColor = Color(FleksyColors.baseColor(ctx) ?: 0xFF2B2B2B.toInt())

    Scaffold(
        contentWindowInsets = WindowInsets.safeDrawing,
        topBar = { TopAppBar(title = { Text("Barre d'outils et appui long") }, navigationIcon = { BackButton(onClickBack) }) }
    ) { innerPadding ->
        Column(Modifier.padding(innerPadding).verticalScroll(rememberScrollState()).padding(horizontal = 16.dp)) {
            // aperçu : la barre des chiffres
            Row(
                Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp)).background(barColor).height((40 * height).dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                "1234567890".forEach { Text(it.toString(), color = Color.White, fontSize = (20 * text).sp) }
            }
            if (preview >= 1f) Row(
                Modifier.fillMaxWidth().background(barColor).height(preview.roundToInt().dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                val fs = (preview * 0.68f).sp
                Text("salur", color = Color.Gray, fontSize = fs)
                Text("salut", color = Color.White, fontSize = fs)
                Text("salir", color = Color.Gray, fontSize = fs)
            }
            Text(
                "q  w  e  r  t  z  u  i  o  p",
                color = Color.White, fontSize = 20.sp,
                modifier = Modifier.fillMaxWidth().background(barColor).padding(vertical = 10.dp),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            )

            Spacer(Modifier.height(24.dp))
            Text("Hauteur de la barre : ${(height * 100).roundToInt()} %", style = MaterialTheme.typography.titleMedium)
            Slider(
                value = height,
                onValueChange = { height = it },
                onValueChangeFinished = { FleksyToolbarSizes.setHeightScale(ctx, height) },
                valueRange = FleksyToolbarSizes.MIN_SCALE..FleksyToolbarSizes.MAX_SCALE,
            )

            Spacer(Modifier.height(16.dp))
            Text("Taille du texte : ${(text * 100).roundToInt()} %", style = MaterialTheme.typography.titleMedium)
            Slider(
                value = text,
                onValueChange = { text = it },
                onValueChangeFinished = { FleksyToolbarSizes.setTextScale(ctx, text) },
                valueRange = FleksyToolbarSizes.MIN_SCALE..FleksyToolbarSizes.MAX_SCALE,
            )

            Spacer(Modifier.height(16.dp))
            Text(
                if (preview < 1f) "Ligne d'aperçu des mots : masquée"
                else "Ligne d'aperçu des mots : ${preview.roundToInt()} dp",
                style = MaterialTheme.typography.titleMedium,
            )
            Slider(
                value = preview,
                onValueChange = { preview = it.roundToInt().toFloat() },
                onValueChangeFinished = { FleksyToolbarSizes.setPreviewHeight(ctx, preview.roundToInt()) },
                valueRange = 0f..FleksyToolbarSizes.PREVIEW_HEIGHT_MAX.toFloat(),
            )
            Text("Tout à gauche : ligne masquée.", style = MaterialTheme.typography.bodySmall)

            Spacer(Modifier.height(24.dp))
            Text("Appui long", style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(8.dp))
            Text("Délai avant les symboles : ${symbolsDelay.roundToInt()} ms", style = MaterialTheme.typography.titleMedium)
            Slider(
                value = symbolsDelay,
                onValueChange = { symbolsDelay = (it / 50f).roundToInt() * 50f },
                onValueChangeFinished = { prefs.edit { putInt(Settings.PREF_KEY_LONGPRESS_TIMEOUT, symbolsDelay.roundToInt()) } },
                valueRange = 150f..1500f,
            )
            Text(
                "Puis, doigt immobile, délai avant les lettres spéciales : ${accentDelay.roundToInt()} ms",
                style = MaterialTheme.typography.titleMedium,
            )
            Slider(
                value = accentDelay,
                onValueChange = { accentDelay = (it / 50f).roundToInt() * 50f },
                onValueChangeFinished = { FleksyToolbarSizes.setAccentDelay(ctx, accentDelay.roundToInt()) },
                valueRange = 150f..2000f,
            )

            Spacer(Modifier.height(16.dp))
            OutlinedButton(onClick = {
                height = 1f; text = 1f
                preview = FleksyToolbarSizes.PREVIEW_HEIGHT_DEFAULT.toFloat()
                symbolsDelay = Defaults.PREF_KEY_LONGPRESS_TIMEOUT.toFloat()
                accentDelay = FleksyToolbarSizes.ACCENT_DELAY_DEFAULT.toFloat()
                FleksyToolbarSizes.setHeightScale(ctx, 1f)
                FleksyToolbarSizes.setTextScale(ctx, 1f)
                FleksyToolbarSizes.setPreviewHeight(ctx, FleksyToolbarSizes.PREVIEW_HEIGHT_DEFAULT)
                FleksyToolbarSizes.setAccentDelay(ctx, FleksyToolbarSizes.ACCENT_DELAY_DEFAULT)
                prefs.edit { putInt(Settings.PREF_KEY_LONGPRESS_TIMEOUT, Defaults.PREF_KEY_LONGPRESS_TIMEOUT) }
            }) { Text("Valeurs par défaut") }
            Spacer(Modifier.height(8.dp))
            Text(
                "Les tailles s'appliquent la prochaine fois que le clavier s'affiche ; les délais, immédiatement.",
                style = MaterialTheme.typography.bodySmall,
            )
            Spacer(Modifier.height(32.dp))
        }
    }
}
