// SPDX-License-Identifier: GPL-3.0-only
package helium314.keyboard.latin.suggestions

import android.graphics.Typeface
import android.text.TextUtils
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.graphics.ColorUtils
import helium314.keyboard.latin.R
import helium314.keyboard.latin.SuggestedWords
import helium314.keyboard.latin.common.ColorType
import helium314.keyboard.latin.settings.Settings

/**
 * Fine ligne d'aperçu façon Fleksy, entre la barre d'outils et les lettres (non cliquable) :
 * à gauche ce que donne un glissement vers le haut (ou 📖 : ajout au dictionnaire),
 * au centre le mot retenu, à droite ce que donne un glissement vers le bas.
 */
object FleksyPreviewLine {
    private var left: TextView? = null
    private var center: TextView? = null
    private var right: TextView? = null

    /** true si l'utilisateur a masqué la ligne (hauteur 0) */
    @JvmStatic
    var isHidden = false
        private set

    fun attach(container: LinearLayout) {
        container.removeAllViews()
        FleksyToolbarSizes.accentDelay(container.context) // met à jour le délai mémorisé
        val heightDp = FleksyToolbarSizes.previewHeight(container.context)
        isHidden = heightDp == 0
        val colors = Settings.getValues().mColors
        colors.setBackground(container, ColorType.STRIP_BACKGROUND)
        container.isClickable = false
        container.importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_NO
        val density = container.resources.displayMetrics.density
        // texte proportionnel à la hauteur choisie, sans marge interne
        val textPx = heightDp * density * 0.68f
        val sideColor = ColorUtils.setAlphaComponent(colors.get(ColorType.KEY_TEXT), 150)
        fun slot(color: Int, bold: Boolean) = TextView(container.context).apply {
            gravity = Gravity.CENTER
            maxLines = 1
            ellipsize = TextUtils.TruncateAt.END
            setTextSize(TypedValue.COMPLEX_UNIT_PX, textPx)
            includeFontPadding = false
            setPadding(0, 0, 0, 0)
            setTextColor(color)
            if (bold) setTypeface(typeface, Typeface.BOLD)
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 1f)
            container.addView(this)
        }
        left = slot(sideColor, false)
        center = slot(colors.get(ColorType.KEY_TEXT), true)
        right = slot(sideColor, false)
        container.layoutParams?.let {
            it.height = (density * heightDp).toInt()
            container.layoutParams = it
        }
        // l'état de correction (après un espace ou un glissement haut/bas) a priorité sur l'aperçu de frappe
        FleksyCorrectionDisplay.onChange = {
            val state = FleksyCorrectionDisplay.state
            if (state != null) showCorrection(state) else show(null, null, null)
        }
        FleksyCorrectionDisplay.state?.let { showCorrection(it) }
    }

    /** Aperçu pendant la frappe : [mot tapé si corrigé]  correction/mot  [suivant]. */
    fun showSuggestions(suggestions: SuggestedWords) {
        if (FleksyCorrectionDisplay.state != null) return
        if (suggestions.isEmpty || suggestions.isPunctuationSuggestions) return show(null, null, null)
        val words = LinkedHashSet<String>()
        for (i in 0 until suggestions.size()) {
            val w = suggestions.getWord(i)
            if (w.isNotEmpty()) words.add(w)
        }
        val typed = suggestions.mTypedWordInfo?.mWord.orEmpty()
        if (typed.isEmpty()) {
            // pas de mot en cours : prédictions du mot suivant
            val list = words.toList()
            return show(list.getOrNull(1), list.getOrNull(0), list.getOrNull(2))
        }
        val main = if (suggestions.mWillAutoCorrect && suggestions.size() > 1) suggestions.getWord(1) else typed
        val next = words.firstOrNull { it != typed && it != main }
        show(if (main != typed) typed else null, main, next)
    }

    private fun showCorrection(state: FleksyCorrectionDisplay.State) {
        val l = left ?: return
        show(state.previous, state.current, state.next)
        if (state.upAddsToDictionary || state.justAdded) {
            val icon = ContextCompat.getDrawable(l.context, if (state.justAdded) R.drawable.ic_setup_check else R.drawable.ic_fleksy_add_word)
            icon?.let { Settings.getValues().mColors.setColor(it, ColorType.TOOL_BAR_KEY) }
            val size = (l.textSize * 1.2f).toInt()
            icon?.setBounds(0, 0, size, size)
            // icône centrée : insérée dans le texte
            l.text = if (icon == null) "" else android.text.SpannableString("\u00A0").apply {
                setSpan(android.text.style.ImageSpan(icon, android.text.style.DynamicDrawableSpan.ALIGN_BOTTOM), 0, 1, 0)
            }
        }
    }

    private fun show(l: String?, c: String?, r: String?) {
        left?.text = l.orEmpty()
        center?.text = c.orEmpty()
        right?.text = r.orEmpty()
    }
}
