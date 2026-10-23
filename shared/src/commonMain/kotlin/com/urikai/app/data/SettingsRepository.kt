package com.urikai.app.data

import com.urikai.app.platform.KeyValueStore
import com.urikai.app.game.AudioLevel
import com.urikai.app.game.ChartLineStyle
import com.urikai.app.game.DisplayLanguage
import com.urikai.app.game.HudMode
import com.urikai.app.game.LcdThemeMode
import com.urikai.app.game.NotebookEntryId
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
        unlockNotebookRead(NotebookEntryId.HOW_TO_PLAY)
    }

    fun hasSeenLifeTutorial(): Boolean =
        prefs.getBoolean(KEY_LIFE_TUTORIAL_SEEN, false)

    fun setLifeTutorialSeen() {
        prefs.edit().putBoolean(KEY_LIFE_TUTORIAL_SEEN, true).apply()
        unlockNotebookRead(NotebookEntryId.LIFE)
    }

    fun hasSeenInvestOpenTutorial(): Boolean =
        prefs.getBoolean(KEY_INVEST_OPEN_TUTORIAL_SEEN, false)

    fun setInvestOpenTutorialSeen() {
        prefs.edit().putBoolean(KEY_INVEST_OPEN_TUTORIAL_SEEN, true).apply()
        unlockNotebookRead(NotebookEntryId.BALANCE)
    }

    fun hasSeenInvestCloseTip(): Boolean =
        prefs.getBoolean(KEY_INVEST_CLOSE_TIP_SEEN, false)

    fun setInvestCloseTipSeen() {
        prefs.edit().putBoolean(KEY_INVEST_CLOSE_TIP_SEEN, true).apply()
    }

    fun hasSeenScrapbookTutorial(): Boolean =
        prefs.getBoolean(KEY_SCRAPBOOK_TUTORIAL_SEEN, false)

    fun setScrapbookTutorialSeen() {
        prefs.edit().putBoolean(KEY_SCRAPBOOK_TUTORIAL_SEEN, true).apply()
        unlockNotebookRead(NotebookEntryId.SCRAPBOOK)
    }

    fun hasSeenStampCornerHint(): Boolean =
        prefs.getBoolean(KEY_STAMP_CORNER_HINT_SEEN, false)

    fun setStampCornerHintSeen() {
        prefs.edit().putBoolean(KEY_STAMP_CORNER_HINT_SEEN, true).apply()
    }

    fun hasSeenStampBackTutorial(): Boolean =
        prefs.getBoolean(KEY_STAMP_BACK_TUTORIAL_SEEN, false)

    fun setStampBackTutorialSeen() {
        prefs.edit().putBoolean(KEY_STAMP_BACK_TUTORIAL_SEEN, true).apply()
        unlockNotebookRead(NotebookEntryId.STAMPS)
    }

    fun hasSeenSumikaSheetTutorial(): Boolean =
        prefs.getBoolean(KEY_SUMIKA_SHEET_TUTORIAL_SEEN, false)

    fun setSumikaSheetTutorialSeen() {
        prefs.edit().putBoolean(KEY_SUMIKA_SHEET_TUTORIAL_SEEN, true).apply()
    }

    /**
     * カイがホームに住み着く導入を見たか。
     * キー未作成は未視聴（ワイプ後の初回出金でもストーリーを出す）。
     * [cashOutCount] は呼び出し互換のため残す。
     */
    @Suppress("UNUSED_PARAMETER")
    fun hasSeenKaiHomeIntro(cashOutCount: Int): Boolean =
        prefs.getBoolean(KEY_KAI_HOME_INTRO_SEEN, false)

    fun setKaiHomeIntroSeen() {
        prefs.edit().putBoolean(KEY_KAI_HOME_INTRO_SEEN, true).apply()
    }

    /**
     * すみかグレード導入セリフを出すのに必要な最低出金回数。
     * 0 = 制限なし。カイ引っ越しと同タイミングで解放されたとき、+1 プレイ後まで抑止する。
     */
    fun sumikaGradeIntroMinCashOut(): Int =
        prefs.getInt(KEY_SUMIKA_GRADE_INTRO_MIN_CASHOUT, 0)

    fun deferSumikaGradeIntroUntilCashOut(minCashOut: Int) {
        if (minCashOut <= 0) return
        val cur = sumikaGradeIntroMinCashOut()
        val next = if (cur > 0) maxOf(cur, minCashOut) else minCashOut
        prefs.edit().putInt(KEY_SUMIKA_GRADE_INTRO_MIN_CASHOUT, next).apply()
    }

    fun clearSumikaGradeIntroDefer() {
        if (!prefs.contains(KEY_SUMIKA_GRADE_INTRO_MIN_CASHOUT)) return
        prefs.edit().remove(KEY_SUMIKA_GRADE_INTRO_MIN_CASHOUT).apply()
    }

    fun getUnlockedNotebookIds(): Set<NotebookEntryId> {
        migrateNotebookIfNeeded()
        val raw = prefs.getStringSet(KEY_NOTEBOOK_UNLOCKED, emptySet()) ?: emptySet()
        return raw.mapNotNull { NotebookEntryId.fromPersisted(it) }.toSet()
    }

    fun getReadNotebookIds(): Set<NotebookEntryId> {
        migrateNotebookIfNeeded()
        val raw = prefs.getStringSet(KEY_NOTEBOOK_READ, emptySet()) ?: emptySet()
        return raw.mapNotNull { NotebookEntryId.fromPersisted(it) }.toSet()
    }

    fun hasUnreadNotebook(): Boolean {
        val unlocked = getUnlockedNotebookIds()
        val read = getReadNotebookIds()
        return unlocked.any { it !in read }
    }

    /** サイレント解禁。すでに解禁済みなら何もしない。未読のまま追加。 */
    fun unlockNotebookUnread(id: NotebookEntryId): Boolean {
        migrateNotebookIfNeeded()
        val unlocked = getUnlockedNotebookIds().map { it.name }.toMutableSet()
        if (!unlocked.add(id.name)) return false
        prefs.edit().putStringSet(KEY_NOTEBOOK_UNLOCKED, unlocked).apply()
        return true
    }

    /** 強制チュート後など。解禁＋既読。 */
    fun unlockNotebookRead(id: NotebookEntryId) {
        migrateNotebookIfNeeded()
        val unlocked = getUnlockedNotebookIds().map { it.name }.toMutableSet()
        unlocked.add(id.name)
        val read = getReadNotebookIds().map { it.name }.toMutableSet()
        read.add(id.name)
        prefs.edit()
            .putStringSet(KEY_NOTEBOOK_UNLOCKED, unlocked)
            .putStringSet(KEY_NOTEBOOK_READ, read)
            .apply()
    }

    fun markNotebookRead(id: NotebookEntryId) {
        migrateNotebookIfNeeded()
        val unlocked = getUnlockedNotebookIds()
        if (id !in unlocked) return
        val read = getReadNotebookIds().map { it.name }.toMutableSet()
        if (!read.add(id.name)) return
        prefs.edit().putStringSet(KEY_NOTEBOOK_READ, read).apply()
    }

    fun getLifetimeMaxCombo(): Int = prefs.getInt(KEY_LIFETIME_MAX_COMBO, 0)

    fun noteLifetimeMaxCombo(combo: Int) {
        if (combo <= getLifetimeMaxCombo()) return
        prefs.edit().putInt(KEY_LIFETIME_MAX_COMBO, combo).apply()
    }

    /** 広告除去 IAP 済み。 */
    fun isAdsRemoved(): Boolean = prefs.getBoolean(KEY_ADS_REMOVED, false)

    fun setAdsRemoved(removed: Boolean) {
        prefs.edit().putBoolean(KEY_ADS_REMOVED, removed).apply()
    }

    /** インタースティシャル候補の出金回数カウンタ（頻度キャップ用）。 */
    fun getAdEligibleCashOuts(): Int = prefs.getInt(KEY_AD_ELIGIBLE_CASHOUTS, 0)

    fun bumpAdEligibleCashOut(): Int {
        val next = getAdEligibleCashOuts() + 1
        prefs.edit().putInt(KEY_AD_ELIGIBLE_CASHOUTS, next).apply()
        return next
    }

    private fun migrateNotebookIfNeeded() {
        if (prefs.getBoolean(KEY_NOTEBOOK_MIGRATED, false)) return
        val editor = prefs.edit().putBoolean(KEY_NOTEBOOK_MIGRATED, true)
        if (hasSeenFirstPlayTutorial()) {
            val unlocked = (prefs.getStringSet(KEY_NOTEBOOK_UNLOCKED, emptySet()) ?: emptySet())
                .toMutableSet()
            val read = (prefs.getStringSet(KEY_NOTEBOOK_READ, emptySet()) ?: emptySet())
                .toMutableSet()
            unlocked.add(NotebookEntryId.HOW_TO_PLAY.name)
            read.add(NotebookEntryId.HOW_TO_PLAY.name)
            editor
                .putStringSet(KEY_NOTEBOOK_UNLOCKED, unlocked)
                .putStringSet(KEY_NOTEBOOK_READ, read)
        }
        editor.apply()
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
        val disabled = getDisabledUpgradeGroups().map { it.name }.toMutableSet()
        disabled.remove(group.name)
        prefs.edit()
            .putStringSet(KEY_RELIC_STAGE_PREFS, encoded)
            .putStringSet(KEY_DISABLED_UPGRADE_GROUPS, disabled)
            .apply()
    }

    /** 段階グループ効果を完全オフ（ホーム強化トグル用）。 */
    fun getDisabledUpgradeGroups(): Set<RelicUpgradeGroup> {
        val raw = prefs.getStringSet(KEY_DISABLED_UPGRADE_GROUPS, emptySet()) ?: emptySet()
        return raw.mapNotNull { name -> RelicUpgradeGroup.entries.find { it.name == name } }.toSet()
    }

    fun setUpgradeGroupDisabled(group: RelicUpgradeGroup, disabled: Boolean) {
        val next = getDisabledUpgradeGroups().map { it.name }.toMutableSet()
        if (disabled) next.add(group.name) else next.remove(group.name)
        prefs.edit().putStringSet(KEY_DISABLED_UPGRADE_GROUPS, next).apply()
    }

    /** 単体強化（LIFE 等）のオフ。 */
    fun getDisabledRelics(): Set<RelicId> {
        val raw = prefs.getStringSet(KEY_DISABLED_RELICS, emptySet()) ?: emptySet()
        return raw.mapNotNull { RelicId.fromPersistedName(it) }.toSet()
    }

    fun setRelicDisabled(id: RelicId, disabled: Boolean) {
        val next = getDisabledRelics().map { it.name }.toMutableSet()
        if (disabled) next.add(id.name) else next.remove(id.name)
        prefs.edit().putStringSet(KEY_DISABLED_RELICS, next).apply()
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
        private const val KEY_LIFE_TUTORIAL_SEEN = "life_tutorial_seen"
        private const val KEY_INVEST_OPEN_TUTORIAL_SEEN = "invest_open_tutorial_seen"
        private const val KEY_INVEST_CLOSE_TIP_SEEN = "invest_close_tip_seen"
        private const val KEY_SCRAPBOOK_TUTORIAL_SEEN = "scrapbook_tutorial_seen"
        private const val KEY_STAMP_CORNER_HINT_SEEN = "stamp_corner_hint_seen"
        private const val KEY_STAMP_BACK_TUTORIAL_SEEN = "stamp_back_tutorial_seen"
        private const val KEY_KAI_HOME_INTRO_SEEN = "kai_home_intro_seen"
        private const val KEY_SUMIKA_GRADE_INTRO_MIN_CASHOUT = "sumika_grade_intro_min_cashout"
        private const val KEY_SUMIKA_SHEET_TUTORIAL_SEEN = "sumika_sheet_tutorial_seen"
        private const val KEY_NOTEBOOK_UNLOCKED = "notebook_unlocked"
        private const val KEY_NOTEBOOK_READ = "notebook_read"
        private const val KEY_NOTEBOOK_MIGRATED = "notebook_migrated"
        private const val KEY_LIFETIME_MAX_COMBO = "lifetime_max_combo"
        private const val KEY_SOUND_ENABLED = "sound_enabled"
        private const val KEY_SE_LEVEL = "se_level"
        private const val KEY_BGM_LEVEL = "bgm_level"
        private const val KEY_HAPTIC_ENABLED = "haptic_enabled"
        private const val KEY_RELIC_STAGE_PREFS = "relic_stage_prefs"
        private const val KEY_DISABLED_UPGRADE_GROUPS = "disabled_upgrade_groups"
        private const val KEY_DISABLED_RELICS = "disabled_relics"
        private const val KEY_ADS_REMOVED = "ads_removed"
        private const val KEY_AD_ELIGIBLE_CASHOUTS = "ad_eligible_cashouts"
    }
}
