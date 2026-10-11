// SPDX-License-Identifier: GPL-3.0-only
package helium314.keyboard.latin.suggestions

import android.content.Context
import androidx.core.content.edit
import helium314.keyboard.keyboard.KeyboardSwitcher
import helium314.keyboard.latin.utils.prefs

/** Taille de la barre d'outils et du texte des suggestions (en pourcentage de la taille d'origine). */
object FleksyToolbarSizes {
    const val PREF_HEIGHT_SCALE = "fleksy_toolbar_height_scale"
    const val PREF_TEXT_SCALE = "fleksy_suggestion_text_scale"
    const val MIN_SCALE = 0.7f
    const val MAX_SCALE = 1.8f

    @JvmStatic
    fun heightScale(context: Context) = context.prefs().getFloat(PREF_HEIGHT_SCALE, 1f).coerceIn(MIN_SCALE, MAX_SCALE)

    @JvmStatic
    fun textScale(context: Context) = context.prefs().getFloat(PREF_TEXT_SCALE, 1f).coerceIn(MIN_SCALE, MAX_SCALE)

    fun setHeightScale(context: Context, scale: Float) = save(context, PREF_HEIGHT_SCALE, scale)
    fun setTextScale(context: Context, scale: Float) = save(context, PREF_TEXT_SCALE, scale)

    // --- ligne d'aperçu des mots ---
    const val PREF_PREVIEW_HEIGHT = "fleksy_preview_line_height_dp"
    const val PREVIEW_HEIGHT_DEFAULT = 16
    const val PREVIEW_HEIGHT_MAX = 36

    /** hauteur de la ligne d'aperçu en dp (0 : masquée) */
    @JvmStatic
    fun previewHeight(context: Context) = context.prefs().getInt(PREF_PREVIEW_HEIGHT, PREVIEW_HEIGHT_DEFAULT).coerceIn(0, PREVIEW_HEIGHT_MAX)

    fun setPreviewHeight(context: Context, dp: Int) {
        context.prefs().edit { putInt(PREF_PREVIEW_HEIGHT, dp.coerceIn(0, PREVIEW_HEIGHT_MAX)) }
        runCatching { KeyboardSwitcher.getInstance().setThemeNeedsReload() }
    }

    // --- appui long : délai avant les lettres spéciales (le délai avant les symboles est celui de HeliBoard) ---
    const val PREF_ACCENT_DELAY = "fleksy_accent_delay_ms"
    const val ACCENT_DELAY_DEFAULT = 500

    /** valeur mémorisée pour le code du clavier, qui n'a pas de Context sous la main */
    @JvmStatic
    var accentDelayMs = ACCENT_DELAY_DEFAULT
        private set

    fun accentDelay(context: Context) = context.prefs().getInt(PREF_ACCENT_DELAY, ACCENT_DELAY_DEFAULT).coerceIn(150, 2000)
        .also { accentDelayMs = it }

    fun setAccentDelay(context: Context, ms: Int) {
        context.prefs().edit { putInt(PREF_ACCENT_DELAY, ms.coerceIn(150, 2000)) }
        accentDelayMs = ms.coerceIn(150, 2000)
    }

    private fun save(context: Context, key: String, scale: Float) {
        context.prefs().edit { putFloat(key, scale.coerceIn(MIN_SCALE, MAX_SCALE)) }
        // la vue du clavier est recréée à la prochaine ouverture avec les nouvelles tailles
        runCatching { KeyboardSwitcher.getInstance().setThemeNeedsReload() }
    }
}
