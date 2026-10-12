package com.urikai.app.data

import com.urikai.app.platform.KeyValueStore
import com.urikai.app.game.AudioLevel
import com.urikai.app.game.ChartLineStyle
import com.urikai.app.game.DisplayLanguage
import com.urikai.app.game.HudMode
import com.urikai.app.game.LcdThemeMode
import com.urikai.app.game.PanelContrast
import com.urikai.app.game.RelicId
import com.urikai.app.game.RelicUpgradeGroup
import com.urikai.app.game.ScanlineStrength
import com.urikai.app.game.ScrapbookSort
import com.urikai.app.game.SurfaceTone

class SettingsRepository(openStore: (String) -> KeyValueStore) {

    private val prefs = openStore(PREFS_NAME)

    fun getHudMode(): HudMode {
        return when (prefs.getString(KEY_HUD_MODE, HudMode.FULL.name)) {
            HudMode.FOCUS.name -> HudMode.FOCUS
            else -> HudMode.FULL
        }
    }

    fun setHudMode(mode: HudMode) {
        prefs.edit().putString(KEY_HUD_MODE, mode.name).apply()
    }

    fun getLcdTheme(): LcdThemeMode {
        return when (prefs.getString(KEY_LCD_THEME, LcdThemeMode.GAME_BOY.name)) {
            LcdThemeMode.E_PAPER.name -> LcdThemeMode.E_PAPER
            LcdThemeMode.E_PAPER_INVERT.name -> LcdThemeMode.E_PAPER_INVERT
            else -> LcdThemeMode.GAME_BOY
        }
    }

    fun setLcdTheme(mode: LcdThemeMode) {
        prefs.edit().putString(KEY_LCD_THEME, mode.name).apply()
    }

    fun getDisplayLanguage(): DisplayLanguage {
        prefs.getString(KEY_DISPLAY_LANGUAGE, null)?.let { stored ->
            return parseDisplayLanguage(stored)
        }
        // 旧 KEY_BUTTON_LABEL からの移行。未設定の初回は JP。
        return when (prefs.getString(KEY_BUTTON_LABEL, null)) {
            "EN" -> DisplayLanguage.EN
            "JP" -> DisplayLanguage.JP
            else -> DisplayLanguage.JP
        }
    }

    fun setDisplayLanguage(language: DisplayLanguage) {
        prefs.edit().putString(KEY_DISPLAY_LANGUAGE, language.name).apply()
    }

    fun getChartLineStyle(): ChartLineStyle {
        return when (prefs.getString(KEY_CHART_LINE, ChartLineStyle.STEP.name)) {
            ChartLineStyle.SMOOTH.name -> ChartLineStyle.SMOOTH
            else -> ChartLineStyle.STEP
        }
    }

    fun setChartLineStyle(style: ChartLineStyle) {
        prefs.edit().putString(KEY_CHART_LINE, style.name).apply()
    }

    fun getScanlineStrength(): ScanlineStrength {
        return when (prefs.getString(KEY_SCANLINE, ScanlineStrength.LOW.name)) {
            ScanlineStrength.OFF.name -> ScanlineStrength.OFF
            ScanlineStrength.HIGH.name -> ScanlineStrength.HIGH
            else -> ScanlineStrength.LOW
        }
    }

    fun setScanlineStrength(strength: ScanlineStrength) {
        prefs.edit().putString(KEY_SCANLINE, strength.name).apply()
    }

    fun getPanelContrast(): PanelContrast {
        return when (prefs.getString(KEY_PANEL_CONTRAST, PanelContrast.NORMAL.name)) {
            PanelContrast.SOFT.name -> PanelContrast.SOFT
            PanelContrast.HARD.name -> PanelContrast.HARD
            else -> PanelContrast.NORMAL
        }
    }

    fun setPanelContrast(contrast: PanelContrast) {
        prefs.edit().putString(KEY_PANEL_CONTRAST, contrast.name).apply()
    }

    fun getSurfaceTone(): SurfaceTone {
        return when (prefs.getString(KEY_SURFACE_TONE, SurfaceTone.NORMAL.name)) {
            SurfaceTone.DIM.name -> SurfaceTone.DIM
            SurfaceTone.BRIGHT.name -> SurfaceTone.BRIGHT
            else -> SurfaceTone.NORMAL
        }
    }

    fun setSurfaceTone(tone: SurfaceTone) {
        prefs.edit().putString(KEY_SURFACE_TONE, tone.name).apply()
    }

    fun getScrapbookSort(): ScrapbookSort {
        return when (prefs.getString(KEY_SCRAPBOOK_SORT, ScrapbookSort.BY_ORDER.name)) {
            ScrapbookSort.BY_BEST.name -> ScrapbookSort.BY_BEST
            else -> ScrapbookSort.BY_ORDER
        }
    }

    fun setScrapbookSort(sort: ScrapbookSort) {
        prefs.edit().putString(KEY_SCRAPBOOK_SORT, sort.name).apply()
    }

    fun hasSeenContentDisclaimer(): Boolean =
        prefs.getBoolean(KEY_CONTENT_DISCLAIMER_SEEN, false)

    fun setContentDisclaimerSeen() {
        prefs.edit().putBoolean(KEY_CONTENT_DISCLAIMER_SEEN, true).apply()
    }

    fun hasSeenFirstPlayTutorial(): Boolean =
        prefs.getBoolean(KEY_FIRST_PLAY_TUTORIAL_SEEN, false)

    fun setFirstPlayTutorialSeen() {
        prefs.edit().putBoolean(KEY_FIRST_PLAY_TUTORIAL_SEEN, true).apply()
    }

    fun isSoundEnabled(): Boolean = prefs.getBoolean(KEY_SOUND_ENABLED, true)

    fun setSoundEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_SOUND_ENABLED, enabled).apply()
    }

    fun getSeLevel(): AudioLevel {
        prefs.getString(KEY_SE_LEVEL, null)?.let { return parseAudioLevel(it) }
        return if (isSoundEnabled()) AudioLevel.MID else AudioLevel.OFF
    }

    fun setSeLevel(level: AudioLevel) {
        prefs.edit()
            .putString(KEY_SE_LEVEL, level.name)
            .putBoolean(KEY_SOUND_ENABLED, level.isOn)
            .apply()
    }

    fun getBgmLevel(): AudioLevel {
        prefs.getString(KEY_BGM_LEVEL, null)?.let { return parseAudioLevel(it) }
        return if (isSoundEnabled()) AudioLevel.MID else AudioLevel.OFF
    }

    fun setBgmLevel(level: AudioLevel) {
        prefs.edit().putString(KEY_BGM_LEVEL, level.name).apply()
    }

    fun isHapticEnabled(): Boolean = prefs.getBoolean(KEY_HAPTIC_ENABLED, true)

    fun setHapticEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_HAPTIC_ENABLED, enabled).apply()
    }

    /** 段階強化グループ → 表示／効果に使う段階。 */
    fun getRelicStagePrefs(): Map<RelicUpgradeGroup, RelicId> {
        val raw = prefs.getStringSet(KEY_RELIC_STAGE_PREFS, emptySet()) ?: emptySet()
        val out = mutableMapOf<RelicUpgradeGroup, RelicId>()
        for (line in raw) {
            val parts = line.split(':', limit = 2)
            if (parts.size != 2) continue
            val group = RelicUpgradeGroup.entries.find { it.name == parts[0] } ?: continue
            val id = RelicId.fromPersistedName(parts[1]) ?: continue
            if (id in group.members) out[group] = id
        }
        return out
    }

    fun setRelicStagePref(group: RelicUpgradeGroup, id: RelicId) {
        if (id !in group.members) return
        val next = getRelicStagePrefs().toMutableMap()
        next[group] = id
        val encoded = next.map { "${it.key.name}:${it.value.name}" }.toSet()
        prefs.edit().putStringSet(KEY_RELIC_STAGE_PREFS, encoded).apply()
    }

    private fun parseDisplayLanguage(stored: String): DisplayLanguage {
        return when (stored) {
            DisplayLanguage.JP.name -> DisplayLanguage.JP
            else -> DisplayLanguage.EN
        }
    }

    private fun parseAudioLevel(stored: String): AudioLevel {
        return when (stored) {
            AudioLevel.OFF.name -> AudioLevel.OFF
            AudioLevel.LOW.name -> AudioLevel.LOW
            AudioLevel.HIGH.name -> AudioLevel.HIGH
            else -> AudioLevel.MID
        }
    }

    companion object {
        private const val PREFS_NAME = "urikai_settings"
        private const val KEY_HUD_MODE = "hud_mode"
        private const val KEY_LCD_THEME = "lcd_theme"
        private const val KEY_BUTTON_LABEL = "button_label"
        private const val KEY_DISPLAY_LANGUAGE = "display_language"
        private const val KEY_CHART_LINE = "chart_line"
        private const val KEY_SCANLINE = "scanline"
        private const val KEY_PANEL_CONTRAST = "panel_contrast"
        private const val KEY_SURFACE_TONE = "surface_tone"
        private const val KEY_SCRAPBOOK_SORT = "scrapbook_sort"
        private const val KEY_CONTENT_DISCLAIMER_SEEN = "content_disclaimer_seen"
        private const val KEY_FIRST_PLAY_TUTORIAL_SEEN = "first_play_tutorial_seen"
        private const val KEY_SOUND_ENABLED = "sound_enabled"
        private const val KEY_SE_LEVEL = "se_level"
        private const val KEY_BGM_LEVEL = "bgm_level"
        private const val KEY_HAPTIC_ENABLED = "haptic_enabled"
        private const val KEY_RELIC_STAGE_PREFS = "relic_stage_prefs"
    }
}
