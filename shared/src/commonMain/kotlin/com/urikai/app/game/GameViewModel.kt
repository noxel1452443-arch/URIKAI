package com.urikai.app.game

import com.urikai.app.platform.currentTimeMillis
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.urikai.app.audio.GameSoundPlayer
import com.urikai.app.platform.PlatformServices
import com.urikai.app.data.AssetRankBoard
import com.urikai.app.data.BalancePoint
import com.urikai.app.data.DailyAccrualResult
import com.urikai.app.data.ChartCsvParser
import com.urikai.app.data.ClearRecord
import com.urikai.app.data.LifetimePnlRepository
import com.urikai.app.data.ScrapbookEntry
import com.urikai.app.data.ScrapbookRepository
import com.urikai.app.data.SessionPnlHistoryEntry
import com.urikai.app.data.SettingsRepository
import com.urikai.app.data.StageChart
import com.urikai.app.feedback.HapticPlayer
import kotlin.math.roundToInt
import kotlin.random.Random
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/**
 * Step C: 1秒ラグ / 間に合わない× / 約定良否SE / HUDモード。
 * GameUiState の6変数は変更しない。
 */
class GameViewModel(
    private val services: PlatformServices,
    chartsCsv: String,
) : ViewModel() {

    val isDebugBuild: Boolean get() = services.isDebugBuild

    private val scrapbookRepository = ScrapbookRepository(services.openStore)
    private val settingsRepository = SettingsRepository(services.openStore)
    private val lifetimePnlRepository = LifetimePnlRepository(services.openStore)
    private val soundPlayer = GameSoundPlayer(services.sound).also {
        val se = settingsRepository.getSeLevel()
        it.enabled = se.isOn
        it.volumeScale = se.seScale()
    }
    private val bgmPlayer = services.bgm.also {
        val bgm = settingsRepository.getBgmLevel()
        it.setTargetVolume(bgm.bgmVolume())
    }
    private val hapticPlayer = HapticPlayer(services.haptics).also {
        it.enabled = settingsRepository.isHapticEnabled()
    }

    private val _chartsLoadError = MutableStateFlow<String?>(null)
    val chartsLoadError: StateFlow<String?> = _chartsLoadError.asStateFlow()

    private val allStages: List<StageChart> = runCatching {
        ChartCsvParser.parse(chartsCsv)
    }.getOrElse { error ->
        _chartsLoadError.value = error.message ?: "Failed to load charts.csv"
        emptyList()
    }
    private val stages: List<StageChart> = if (PILOT_STAGE_FILTER_ENABLED) {
        allStages.filter { it.stageId in PILOT_STAGE_IDS }
    } else {
        allStages
    }

    private val _uiState = MutableStateFlow(GameUiState())
    val uiState: StateFlow<GameUiState> = _uiState.asStateFlow()

    private val _chartFrame = MutableStateFlow(ChartFrame())
    val chartFrame: StateFlow<ChartFrame> = _chartFrame.asStateFlow()

    private val _appLayer = MutableStateFlow(AppLayer.PLAY)
    val appLayer: StateFlow<AppLayer> = _appLayer.asStateFlow()

    private val _scrapbookEntries = MutableStateFlow<List<ScrapbookEntry>>(emptyList())
    val scrapbookEntries: StateFlow<List<ScrapbookEntry>> = _scrapbookEntries.asStateFlow()

    private val _hudMode = MutableStateFlow(settingsRepository.getHudMode())
    val hudMode: StateFlow<HudMode> = _hudMode.asStateFlow()

    private val _lcdTheme = MutableStateFlow(settingsRepository.getLcdTheme())
    val lcdTheme: StateFlow<LcdThemeMode> = _lcdTheme.asStateFlow()

    private val _displayLanguage = MutableStateFlow(settingsRepository.getDisplayLanguage())
    val displayLanguage: StateFlow<DisplayLanguage> = _displayLanguage.asStateFlow()

    private val _chartLineStyle = MutableStateFlow(settingsRepository.getChartLineStyle())
    val chartLineStyle: StateFlow<ChartLineStyle> = _chartLineStyle.asStateFlow()

    private val _scanlineStrength = MutableStateFlow(settingsRepository.getScanlineStrength())
    val scanlineStrength: StateFlow<ScanlineStrength> = _scanlineStrength.asStateFlow()

    private val _panelContrast = MutableStateFlow(settingsRepository.getPanelContrast())
    val panelContrast: StateFlow<PanelContrast> = _panelContrast.asStateFlow()

    private val _surfaceTone = MutableStateFlow(settingsRepository.getSurfaceTone())
    val surfaceTone: StateFlow<SurfaceTone> = _surfaceTone.asStateFlow()

    private val _scrapbookSort = MutableStateFlow(settingsRepository.getScrapbookSort())
    val scrapbookSort: StateFlow<ScrapbookSort> = _scrapbookSort.asStateFlow()

    private val _startSmallText = MutableStateFlow(
        StartScreenCopy.launchTutorial(settingsRepository.getDisplayLanguage(), ORDER_LAG_MS)
    )
    val startSmallText: StateFlow<String> = _startSmallText.asStateFlow()

    /** 口座残高。プレイ中は「今回の元手」のまま（入金で更新）。 */
    private val _balance = MutableStateFlow(lifetimePnlRepository.getBalance())
    val balance: StateFlow<Long> = _balance.asStateFlow()

    /** 残高推移（古い順）と最高到達額。START の残高画面で使う。 */
    private val _balanceHistory = MutableStateFlow(lifetimePnlRepository.getBalanceHistory())
    val balanceHistory: StateFlow<List<BalancePoint>> = _balanceHistory.asStateFlow()
    private val _peakBalance = MutableStateFlow(lifetimePnlRepository.getPeakBalance())
    val peakBalance: StateFlow<Long> = _peakBalance.asStateFlow()

    /** 選択中の運用プラン（日付が進むたびに残高へ反映）。 */
    private val _investPlan = MutableStateFlow(lifetimePnlRepository.getInvestPlan())
    val investPlan: StateFlow<InvestPlan> = _investPlan.asStateFlow()

    /** 留守の間の運用結果（START で一度だけ見せる）。 */
    private val _awayReport = MutableStateFlow<DailyAccrualResult?>(null)
    val awayReport: StateFlow<DailyAccrualResult?> = _awayReport.asStateFlow()

    /** START で破産演出を出すか。 */
    private val _showBankrupt = MutableStateFlow(lifetimePnlRepository.isBankrupt())
    val showBankrupt: StateFlow<Boolean> = _showBankrupt.asStateFlow()

    /** 今回の元手（開始時の口座残高）。 */
    private var stakeBalance: Long = lifetimePnlRepository.getBalance()

    private val _sessionPnlHistory =
        MutableStateFlow(lifetimePnlRepository.getSessionPnlHistory())
    val sessionPnlHistory: StateFlow<List<SessionPnlHistoryEntry>> =
        _sessionPnlHistory.asStateFlow()

    private val _cashOutCount = MutableStateFlow(lifetimePnlRepository.getCashOutCount())
    val cashOutCount: StateFlow<Int> = _cashOutCount.asStateFlow()

    private val _pendingRelics = MutableStateFlow(lifetimePnlRepository.getPendingRelics())
    val pendingRelics: StateFlow<List<RelicId>> = _pendingRelics.asStateFlow()

    /** 今回の出金で新たに受取待ちになったレリック（RESULT誘導ポップ用）。 */
    private val _newlyPendingRelics = MutableStateFlow<List<RelicId>>(emptyList())
    val newlyPendingRelics: StateFlow<List<RelicId>> = _newlyPendingRelics.asStateFlow()

    private val _resultIntroFinished = MutableStateFlow(false)
    val resultIntroFinished: StateFlow<Boolean> = _resultIntroFinished.asStateFlow()

    private val _claimedRelics = MutableStateFlow(lifetimePnlRepository.getClaimedRelics().toList().let { claimed ->
        RelicId.ordered.filter { it in claimed.toSet() }
    })
    val claimedRelics: StateFlow<List<RelicId>> = _claimedRelics.asStateFlow()

    private val _relicEffects = MutableStateFlow(
        RelicEffects.fromClaimed(
            lifetimePnlRepository.getClaimedRelics(),
            settingsRepository.getRelicStagePrefs()
        )
    )
    val relicEffects: StateFlow<RelicEffects> = _relicEffects.asStateFlow()

    private val _relicStagePrefs =
        MutableStateFlow(settingsRepository.getRelicStagePrefs())
    val relicStagePrefs: StateFlow<Map<RelicUpgradeGroup, RelicId>> =
        _relicStagePrefs.asStateFlow()

    /**
     * 開発用レリックラボ。null=本番。非null=各レリックON/OFF（セーブは汚さない）。
     */
    private val _devRelicLab = MutableStateFlow<Map<RelicId, Boolean>?>(null)
    val devRelicLab: StateFlow<Map<RelicId, Boolean>?> = _devRelicLab.asStateFlow()

    private val _showFirstPlayTutorial = MutableStateFlow(false)
    val showFirstPlayTutorial: StateFlow<Boolean> = _showFirstPlayTutorial.asStateFlow()

    private val _showContentDisclaimer = MutableStateFlow(!settingsRepository.hasSeenContentDisclaimer())
    val showContentDisclaimer: StateFlow<Boolean> = _showContentDisclaimer.asStateFlow()

    private val _seLevel = MutableStateFlow(settingsRepository.getSeLevel())
    val seLevel: StateFlow<AudioLevel> = _seLevel.asStateFlow()

    private val _bgmLevel = MutableStateFlow(settingsRepository.getBgmLevel())
    val bgmLevel: StateFlow<AudioLevel> = _bgmLevel.asStateFlow()

    private val _hapticEnabled = MutableStateFlow(settingsRepository.isHapticEnabled())
    val hapticEnabled: StateFlow<Boolean> = _hapticEnabled.asStateFlow()

    /** 起動後最初のSTART表示か（RETRY前はチュートリアル固定） */
    private var isFirstStartAfterLaunch = true

    init {
        lifetimePnlRepository.clearLegacyBonusFlags()
        refreshStartSmallTextForCurrentState()
        sanitizeLockedCosmetics()
        accrueDailyPlan()
        syncBgmAmbient()
    }

    fun selectInvestPlan(plan: InvestPlan) {
        if (_uiState.value.gameState != GameState.START) return
        if (lifetimePnlRepository.setInvestPlan(plan)) {
            _investPlan.value = plan
        }
    }

    fun dismissAwayReport() {
        _awayReport.value = null
    }

    /** 日付が変わっていれば運用プランを反映。START に戻るたびにも確認する。 */
    private fun accrueDailyPlan() {
        val effects = _relicEffects.value
        val result = lifetimePnlRepository.accrueDailyPlan(
            yieldMult = effects.planYieldMult,
            maxDays = effects.maxAwayDays
        ) ?: return
        _balance.value = lifetimePnlRepository.getBalance()
        _peakBalance.value = lifetimePnlRepository.getPeakBalance()
        _balanceHistory.value = lifetimePnlRepository.getBalanceHistory()
        _pendingRelics.value = lifetimePnlRepository.getPendingRelics()
        _showBankrupt.value = lifetimePnlRepository.isBankrupt()
        stakeBalance = _balance.value
        if (result.after != result.before) _awayReport.value = result
    }

    private var finishSequenceJob: Job? = null
    private var tickJob: Job? = null
    private var orderJob: Job? = null
    private var orderTickJob: Job? = null
    private var chargeCompleteJob: Job? = null

    private lateinit var stage: StageChart
    /** マクロ + セッション・ミクロを重ねた、この run 専用の price 列 */
    private var sessionPriceRatios: FloatArray = floatArrayOf()
    private var startOffsetSeconds: Int = 0
    private var sessionStartEpochMs: Long = 0L
    private var cash: Int = INITIAL_ASSET
    private var shares: Float = 0f
    private var pendingBuy: Boolean = false
    private val markers = mutableListOf<TradeMarker>()
    private var lastCountdownDigitPlayed: Int = -1
    private var lastEndgameHeartElapsedMs: Long = -1L
    private var maxFillCombo: Int = 0
    private var sessionHealth: Int = MAX_HEALTH
    private var sessionMaxHealth: Int = MAX_HEALTH
    private var isRetiredRun: Boolean = false
    private var retireJob: Job? = null

    init {
        if (stages.isEmpty() && _chartsLoadError.value == null) {
            _chartsLoadError.value = if (PILOT_STAGE_FILTER_ENABLED) {
                "Pilot filter enabled but none of $PILOT_STAGE_IDS found in charts.csv"
            } else {
                "No stages found in charts.csv"
            }
        }
        if (stages.isNotEmpty()) {
            refreshScrapbook()
        }
    }

    fun onPrimaryButtonClick() {
        when (_appLayer.value) {
            AppLayer.SCRAPBOOK -> {
                soundPlayer.playUiConfirm()
                closeScrapbook()
            }
            AppLayer.OPTION -> {
                soundPlayer.playUiConfirm()
                closeOption()
            }
            AppLayer.PLAY -> when (_uiState.value.gameState) {
                GameState.START -> {
                    if (_showContentDisclaimer.value) return
                    if (_showBankrupt.value) return
                    if (_showFirstPlayTutorial.value) return
                    soundPlayer.playUiConfirm()
                    startGame()
                }
                GameState.PLAYING -> {
                    if (_showFirstPlayTutorial.value) return
                    if (_chartFrame.value.retireLocked) return
                    if (_uiState.value.isOrderProcessing) {
                        nudgePendingOrder()
                    } else {
                        placeOrder()
                    }
                }
                GameState.FINISHING -> {
                    soundPlayer.playUiConfirm()
                    // FINISH 表示のまま出金 → 結果確定＋演出
                    finishGame()
                    claimResultPayout()
                }
                GameState.RESULT -> {
                    if (!_chartFrame.value.resultPayoutClaimed) {
                        soundPlayer.playUiConfirm()
                        claimResultPayout()
                    } else {
                        if (!_resultIntroFinished.value) return
                        if (_newlyPendingRelics.value.isNotEmpty()) return
                        soundPlayer.playUiConfirm()
                        resetToStart()
                    }
                }
            }
        }
    }

    fun openScrapbook() {
        if (_showFirstPlayTutorial.value) return
        if (_chartFrame.value.retireLocked) return
        if (_uiState.value.gameState == GameState.PLAYING ||
            _uiState.value.gameState == GameState.FINISHING
        ) return
        if (isResultPresentationBlocking()) return
        refreshScrapbook()
        _appLayer.value = AppLayer.SCRAPBOOK
        syncBgmAmbient()
    }

    fun closeScrapbook() {
        _appLayer.value = AppLayer.PLAY
        syncBgmAmbient()
    }

    fun openOption() {
        if (_showFirstPlayTutorial.value) return
        if (_chartFrame.value.retireLocked) return
        if (_uiState.value.gameState == GameState.PLAYING ||
            _uiState.value.gameState == GameState.FINISHING
        ) return
        if (isResultPresentationBlocking()) return
        _appLayer.value = AppLayer.OPTION
        syncBgmAmbient()
    }

    fun closeOption() {
        _appLayer.value = AppLayer.PLAY
        syncBgmAmbient()
    }

    fun dismissContentDisclaimer() {
        settingsRepository.setContentDisclaimerSeen()
        _showContentDisclaimer.value = false
    }

    /** 破産演出のボタン：口座残高を初期額へ戻す。 */
    fun restartAfterBankruptcy() {
        if (!_showBankrupt.value) return
        lifetimePnlRepository.restartBalance()
        _balance.value = lifetimePnlRepository.getBalance()
        _balanceHistory.value = lifetimePnlRepository.getBalanceHistory()
        stakeBalance = _balance.value
        _showBankrupt.value = false
        soundPlayer.playUiConfirm()
    }

    fun dismissFirstPlayTutorial() {
        if (!_showFirstPlayTutorial.value) return
        settingsRepository.setFirstPlayTutorialSeen()
        _showFirstPlayTutorial.value = false
        beginSessionClock()
    }

    fun onResultIntroFinished() {
        if (_uiState.value.gameState != GameState.RESULT) return
        if (!_chartFrame.value.resultPayoutClaimed) return
        _resultIntroFinished.value = true
    }

    fun dismissRelicRewardToast() {
        _newlyPendingRelics.value = emptyList()
    }

    /** 出金〜リザルト導入（＋未消化の新規レリック誘導）中は画面遷移させない。 */
    private fun isResultPresentationBlocking(): Boolean {
        if (_uiState.value.gameState != GameState.RESULT) return false
        if (!_chartFrame.value.resultPayoutClaimed) return false
        if (!_resultIntroFinished.value) return true
        if (_newlyPendingRelics.value.isNotEmpty()) return true
        return false
    }

    fun cuePayoutCount(progress: Float, profitOrEven: Boolean) {
        soundPlayer.playPayoutCount(progress, profitOrEven)
    }

    fun cuePayoutClang(profitOrEven: Boolean) {
        soundPlayer.playPayoutClang(profitOrEven)
    }

    fun cueResultType() = soundPlayer.playResultType()

    fun cueResultStar(index: Int, total: Int) = soundPlayer.playResultStar(index, total)

    fun cueResultStamp() = soundPlayer.playResultStamp()

    fun cueResultPanelIn() = soundPlayer.playResultPanelIn()

    fun cueUiConfirm() = soundPlayer.playUiConfirm()

    fun cueUiSelect() = soundPlayer.playUiSelect()

    fun assetRankBoardForCurrentResult(): AssetRankBoard? {
        val result = _chartFrame.value.resultInfo ?: return null
        return lifetimePnlRepository.buildAssetRankBoard(
            result.finalAsset,
            result.endBalance - result.startBalance
        )
    }

    fun setHudMode(mode: HudMode) {
        if (_hudMode.value == mode) return
        settingsRepository.setHudMode(mode)
        _hudMode.value = mode
    }

    fun setLcdTheme(mode: LcdThemeMode) {
        if (!isLcdThemeAllowed(mode)) return
        if (_lcdTheme.value == mode) return
        settingsRepository.setLcdTheme(mode)
        _lcdTheme.value = mode
    }

    fun setDisplayLanguage(language: DisplayLanguage) {
        if (_displayLanguage.value == language) return
        settingsRepository.setDisplayLanguage(language)
        _displayLanguage.value = language
        refreshStartSmallTextForCurrentState()
        if (stages.isNotEmpty()) {
            refreshScrapbook()
        }
        relocalizeOpenSessionCopy()
    }

    fun setChartLineStyle(style: ChartLineStyle) {
        if (!isChartStyleAllowed(style)) return
        if (_chartLineStyle.value == style) return
        settingsRepository.setChartLineStyle(style)
        _chartLineStyle.value = style
    }

    /** 報酬箱から見た目レリックを適用／既定に戻す。 */
    fun toggleRelicVisual(id: RelicId) {
        when (id) {
            RelicId.PAPER -> {
                if (_lcdTheme.value == LcdThemeMode.E_PAPER) {
                    applyDefaultLcd()
                } else {
                    setLcdTheme(LcdThemeMode.E_PAPER)
                }
            }
            RelicId.DARK -> {
                if (_lcdTheme.value == LcdThemeMode.E_PAPER_INVERT) {
                    applyDefaultLcd()
                } else {
                    setLcdTheme(LcdThemeMode.E_PAPER_INVERT)
                }
            }
            RelicId.SMOOTH -> {
                if (_chartLineStyle.value == ChartLineStyle.SMOOTH) {
                    applyDefaultChart()
                } else {
                    setChartLineStyle(ChartLineStyle.SMOOTH)
                }
            }
            else -> Unit
        }
    }

    fun applyDefaultLcd() = setLcdTheme(LcdThemeMode.GAME_BOY)

    fun applyDefaultChart() = setChartLineStyle(ChartLineStyle.STEP)

    fun claimPendingRelic(id: RelicId): Boolean {
        if (!lifetimePnlRepository.claimRelic(id)) return false
        refreshRelicState()
        when (id) {
            RelicId.PAPER -> setLcdTheme(LcdThemeMode.E_PAPER)
            RelicId.DARK -> setLcdTheme(LcdThemeMode.E_PAPER_INVERT)
            RelicId.SMOOTH -> setChartLineStyle(ChartLineStyle.SMOOTH)
            else -> Unit
        }
        soundPlayer.playFinish()
        hapticPlayer.tickPayoutClaim(big = true)
        return true
    }

    private fun refreshRelicState() {
        val claimed = effectiveClaimedRelics()
        val prefs = settingsRepository.getRelicStagePrefs()
        _relicStagePrefs.value = prefs
        val lab = _devRelicLab.value
        _claimedRelics.value = if (lab != null) {
            RelicId.ordered
        } else {
            RelicId.ordered.filter { it in lifetimePnlRepository.getClaimedRelics() }
        }
        _pendingRelics.value = if (lab != null) {
            emptyList()
        } else {
            lifetimePnlRepository.getPendingRelics()
        }
        _relicEffects.value = RelicEffects.fromClaimed(claimed, prefs)
    }

    /** 報酬箱の段階切替。効果グループは実行時補正も更新。 */
    fun cycleRelicStage(group: RelicUpgradeGroup) {
        val owned = group.members.filter { it in effectiveClaimedRelics() }
        if (owned.size < 2) return
        val prefs = settingsRepository.getRelicStagePrefs()
        val current = prefs[group]?.takeIf { it in owned } ?: owned.maxBy { it.threshold }
        val idx = owned.indexOf(current).coerceAtLeast(0)
        val next = owned[(idx + 1) % owned.size]
        settingsRepository.setRelicStagePref(group, next)
        refreshRelicState()
        soundPlayer.playUiSelect()
    }

    private fun effectiveClaimedRelics(): Set<RelicId> {
        val lab = _devRelicLab.value
        if (lab != null) {
            return lab.filterValues { it }.keys
        }
        return lifetimePnlRepository.getClaimedRelics()
    }

    fun enableDevRelicLab() {
        _devRelicLab.value = RelicId.entries.associateWith { true }
        refreshRelicState()
    }

    fun disableDevRelicLab() {
        _devRelicLab.value = null
        refreshRelicState()
        sanitizeLockedCosmetics()
    }

    fun setDevRelicEnabled(id: RelicId, enabled: Boolean) {
        val lab = _devRelicLab.value ?: return
        _devRelicLab.value = lab + (id to enabled)
        refreshRelicState()
        if (!enabled) sanitizeLockedCosmetics()
    }

    private fun isLcdThemeAllowed(mode: LcdThemeMode): Boolean {
        val fx = _relicEffects.value
        return when (mode) {
            LcdThemeMode.GAME_BOY -> true
            LcdThemeMode.E_PAPER -> fx.paperUnlocked
            LcdThemeMode.E_PAPER_INVERT -> fx.darkUnlocked
        }
    }

    private fun isChartStyleAllowed(style: ChartLineStyle): Boolean = when (style) {
        ChartLineStyle.STEP -> true
        ChartLineStyle.SMOOTH -> _relicEffects.value.smoothUnlocked
    }

    private fun sanitizeLockedCosmetics() {
        if (!isLcdThemeAllowed(_lcdTheme.value)) {
            settingsRepository.setLcdTheme(LcdThemeMode.GAME_BOY)
            _lcdTheme.value = LcdThemeMode.GAME_BOY
        }
        if (!isChartStyleAllowed(_chartLineStyle.value)) {
            settingsRepository.setChartLineStyle(ChartLineStyle.STEP)
            _chartLineStyle.value = ChartLineStyle.STEP
        }
    }

    private fun orderLagMs(): Long =
        (ORDER_LAG_MS * _relicEffects.value.lagFactor).toLong().coerceAtLeast(250L)

    private fun orderCooldownMs(): Long =
        (ORDER_COOLDOWN_MS * _relicEffects.value.cooldownFactor).toLong().coerceAtLeast(250L)

    fun setScanlineStrength(strength: ScanlineStrength) {
        if (_scanlineStrength.value == strength) return
        settingsRepository.setScanlineStrength(strength)
        _scanlineStrength.value = strength
    }

    fun setPanelContrast(contrast: PanelContrast) {
        if (_panelContrast.value == contrast) return
        settingsRepository.setPanelContrast(contrast)
        _panelContrast.value = contrast
    }

    fun setSurfaceTone(tone: SurfaceTone) {
        if (_surfaceTone.value == tone) return
        settingsRepository.setSurfaceTone(tone)
        _surfaceTone.value = tone
    }

    fun setSeLevel(level: AudioLevel) {
        if (_seLevel.value == level) return
        val wasOn = _seLevel.value.isOn
        settingsRepository.setSeLevel(level)
        _seLevel.value = level
        soundPlayer.enabled = level.isOn
        soundPlayer.volumeScale = level.seScale()
        // OFF→ON はチップ側が無音なので、有効化後に選択音でフィードバック
        if (level.isOn && !wasOn) soundPlayer.playUiSelect()
    }

    fun setBgmLevel(level: AudioLevel) {
        if (_bgmLevel.value == level) return
        settingsRepository.setBgmLevel(level)
        _bgmLevel.value = level
        bgmPlayer.setTargetVolume(level.bgmVolume())
        syncBgmAmbient()
    }

    fun setAppForeground(foreground: Boolean) {
        bgmPlayer.setForeground(foreground)
    }

    /** START / RESULT / 図鑑 / CONFIG では流し、PLAYING / FINISHING では止める。 */
    private fun syncBgmAmbient() {
        val allow = when {
            !_bgmLevel.value.isOn -> false
            _appLayer.value != AppLayer.PLAY -> true
            else -> when (_uiState.value.gameState) {
                GameState.PLAYING, GameState.FINISHING -> false
                GameState.START, GameState.RESULT -> true
            }
        }
        bgmPlayer.setAmbientAllowed(allow)
    }

    fun setHapticEnabled(enabled: Boolean) {
        if (_hapticEnabled.value == enabled) return
        settingsRepository.setHapticEnabled(enabled)
        _hapticEnabled.value = enabled
        hapticPlayer.enabled = enabled
    }

    fun setScrapbookSort(sort: ScrapbookSort) {
        if (_scrapbookSort.value == sort) return
        settingsRepository.setScrapbookSort(sort)
        _scrapbookSort.value = sort
        refreshScrapbook()
    }

    /** タイトルカードを弾いたときのヒント差し替え */
    fun shuffleStartAttractTip() {
        if (_uiState.value.gameState != GameState.START) return
        _startSmallText.value = StartScreenCopy.randomSmallText(_displayLanguage.value)
        soundPlayer.playUiSelect()
    }

    /** チャート無しのシェア文面。Activity 側で Intent に載せる。 */
    fun buildShareText(): String? {
        val result = _chartFrame.value.resultInfo ?: return null
        return ShareTextBuilder.build(result, _displayLanguage.value)
    }

    private fun refreshScrapbook() {
        val entries = scrapbookRepository.buildEntries(stages, _displayLanguage.value)
        _scrapbookEntries.value = sortScrapbookEntries(entries, _scrapbookSort.value)
    }

    /** LANG 切替時、進行中/RESULT のステージ文言を差し替える。 */
    private fun relocalizeOpenSessionCopy() {
        if (!::stage.isInitialized) return
        val lang = _displayLanguage.value
        _chartFrame.update { frame ->
            frame.copy(
                calendarStart = stage.calendarStart(lang),
                calendarEnd = stage.calendarEnd(lang),
                anchorWhen = stage.anchorWhen(lang),
                anchorLabel = stage.anchorLabel(lang),
                resultInfo = frame.resultInfo?.copy(
                    brandName = stage.brandName(lang),
                    calendarStart = stage.calendarStart(lang),
                    calendarEnd = stage.calendarEnd(lang),
                    historyComment = stage.historyComment(lang)
                )
            )
        }
    }

    private fun sortScrapbookEntries(
        entries: List<ScrapbookEntry>,
        sort: ScrapbookSort
    ): List<ScrapbookEntry> {
        return when (sort) {
            ScrapbookSort.BY_ORDER -> entries
            ScrapbookSort.BY_BEST -> entries.sortedWith(
                compareByDescending<ScrapbookEntry> { it.bestAsset ?: -1 }
                    .thenBy { it.stageId }
            )
        }
    }

    private fun pickRandomStage(): StageChart {
        val standard = stages.filter { !isExtremeStageId(it.stageId) }
        val extreme = stages.filter { isExtremeStageId(it.stageId) }
        require(standard.isNotEmpty()) { "No standard stages in charts.csv" }
        if (extreme.isNotEmpty() && Random.nextFloat() < EXTREME_STAGE_SPAWN_CHANCE) {
            return extreme.random(Random)
        }
        return standard.random(Random)
    }

    private fun isExtremeStageId(stageId: String): Boolean =
        stageId.length == 3 && stageId[0] == 'e' && stageId[1].isDigit() && stageId[2].isDigit()

    private fun startGame() {
        if (stages.isEmpty() || _chartsLoadError.value != null) return
        prepareSession()
        if (!settingsRepository.hasSeenFirstPlayTutorial()) {
            _chartFrame.update { it.copy(isRunning = false) }
            _showFirstPlayTutorial.value = true
        } else {
            beginSessionClock()
        }
    }

    /** ステージ抽選・初期表示まで。タイマーはまだ回さない。 */
    private fun prepareSession() {
        cancelJobs()
        _appLayer.value = AppLayer.PLAY

        stage = pickRandomStage()
        startOffsetSeconds = Random.nextInt(0, MAX_START_OFFSET_INCLUSIVE + 1)
        sessionPriceRatios = SessionMicroOverlay.apply(
            macro = stage.priceRatios,
            seed = Random.nextLong()
        )
        stakeBalance = _balance.value
        cash = INITIAL_ASSET
        shares = 0f
        pendingBuy = false
        markers.clear()
        lastCountdownDigitPlayed = -1
        lastEndgameHeartElapsedMs = -1L
        maxFillCombo = 0
        sessionMaxHealth = MAX_HEALTH + _relicEffects.value.maxHealthBonus
        sessionHealth = sessionMaxHealth
        isRetiredRun = false
        retireJob?.cancel()
        retireJob = null

        val openingRatio = PriceMath.ratioAt(sessionPriceRatios, startOffsetSeconds.toFloat())
        val openingPrice = PriceMath.ratioToDisplayPrice(openingRatio)

        _uiState.value = GameUiState(
            gameState = GameState.PLAYING,
            currentSecond = 0,
            currentPriceDisplay = openingPrice,
            totalAsset = INITIAL_ASSET,
            hasStock = false,
            isOrderProcessing = false
        )

        val lang = _displayLanguage.value
        _chartFrame.value = ChartFrame(
            priceRatios = sessionPriceRatios.copyOf(),
            startOffsetSeconds = startOffsetSeconds,
            elapsedMs = 0L,
            calendarStart = stage.calendarStart(lang),
            calendarEnd = stage.calendarEnd(lang),
            marketKind = stage.marketKind,
            anchorWhen = stage.anchorWhen(lang),
            anchorLabel = stage.anchorLabel(lang),
            quoteOpenPrice = openingPrice,
            quoteBasePrice = stage.basePrice,
            pivotBandStart = stage.anchorBandStart,
            pivotBandEnd = stage.anchorBandEnd,
            isRunning = false,
            markers = emptyList(),
            hasStock = false,
            resultInfo = null,
            interestTickAmount = 0,
            interestTickUntilEpochMs = 0L,
            orderReadyAtEpochMs = 0L,
            orderCooldownStartedAtEpochMs = 0L,
            healthPoints = sessionHealth,
            maxHealthPoints = sessionMaxHealth,
            retirePhase = RetirePhase.NONE,
        )

        syncBgmAmbient()
    }

    /** チュートリアル終了後／通常開始でタイマーを回す。 */
    private fun beginSessionClock() {
        if (_uiState.value.gameState != GameState.PLAYING) return
        if (tickJob?.isActive == true) return
        sessionStartEpochMs = currentTimeMillis()
        _chartFrame.update { it.copy(isRunning = true, elapsedMs = 0L) }
        tickJob = viewModelScope.launch {
            while (isActive) {
                val elapsedMs = (currentTimeMillis() - sessionStartEpochMs)
                    .coerceAtLeast(0L)

                if (elapsedMs >= GAME_DURATION_MS) {
                    publishFrame(GAME_DURATION_MS)
                    beginFinishSequence()
                    break
                }

                publishFrame(elapsedMs)
                delay(FRAME_DELAY_MS)
            }
        }
    }

    private fun scheduleOrderChargeCooldown() {
        val now = currentTimeMillis()
        val cooldownMs = orderCooldownMs()
        _chartFrame.update {
            it.copy(
                orderCooldownStartedAtEpochMs = now,
                orderReadyAtEpochMs = now + cooldownMs,
            )
        }
        chargeCompleteJob?.cancel()
        chargeCompleteJob = viewModelScope.launch {
            delay(cooldownMs)
            if (_uiState.value.gameState != GameState.PLAYING) return@launch
            soundPlayer.playChargeReady(_relicEffects.value.coolFx)
            hapticPlayer.tickChargeReady()
        }
    }

    private fun publishFrame(elapsedMs: Long) {
        val elapsedSec = elapsedMs / 1_000f
        val absoluteSec = startOffsetSeconds + elapsedSec
        val ratio = PriceMath.ratioAt(sessionPriceRatios, absoluteSec)
        val price = PriceMath.ratioToDisplayPrice(ratio)
        val second = (elapsedMs / 1_000L).toInt().coerceIn(0, GAME_DURATION_SECONDS - 1)
        if (second >= ENDGAME_COUNTDOWN_FROM_SECOND && second < GAME_DURATION_SECONDS) {
            val digit = GAME_DURATION_SECONDS - second
            if (digit != lastCountdownDigitPlayed) {
                lastCountdownDigitPlayed = digit
                soundPlayer.playCountdown(digit)
            }
        }
        maybeEndgameHeart(elapsedMs, second)
        val asset = currentTotalAsset(price)
        val hasStock = shares > 0f

        _uiState.update { state ->
            if (state.gameState != GameState.PLAYING) state
            else state.copy(
                currentSecond = second,
                currentPriceDisplay = price,
                totalAsset = asset,
                hasStock = hasStock
            )
        }

        val now = currentTimeMillis()
        _chartFrame.update { frame ->
            val expired = frame.fillCombo > 0 &&
                frame.fillComboExpiresAtEpochMs > 0L &&
                now >= frame.fillComboExpiresAtEpochMs
            frame.copy(
                elapsedMs = elapsedMs,
                isRunning = true,
                markers = markers.toList(),
                hasStock = hasStock,
                fillCombo = if (expired) 0 else frame.fillCombo,
                fillComboExpiresAtEpochMs = if (expired) 0L else frame.fillComboExpiresAtEpochMs,
                fillComboBounceEpochMs = if (expired) 0L else frame.fillComboBounceEpochMs
            )
        }
    }

    /** 注文ラグ中の連打 → タイマー微震のみ（約定には影響しない） */
    private fun nudgePendingOrder() {
        if (!_uiState.value.isOrderProcessing) return
        if (markers.none { it.isPending }) return
        _chartFrame.update {
            it.copy(pendingShakeEpochMs = currentTimeMillis())
        }
    }

    private fun placeOrder() {
        val state = _uiState.value
        if (state.gameState != GameState.PLAYING) return
        if (_chartFrame.value.retireLocked) return
        if (state.isOrderProcessing) return
        val now = currentTimeMillis()
        if (!OrderCharge.isReady(now, _chartFrame.value.orderReadyAtEpochMs)) return

        // 方向だけ押下時に確定。約定価格はラグ後の相場で決まる（実注文の遅延感）。
        pendingBuy = !state.hasStock
        val submitSec = _chartFrame.value.elapsedSeconds
        val submitAbs = startOffsetSeconds + submitSec
        val submitRatio = PriceMath.ratioAt(sessionPriceRatios, submitAbs)
        val submitPrice = PriceMath.ratioToDisplayPrice(submitRatio)

        markers.removeAll { it.isPending }
        markers.add(
            TradeMarker(
                gameTimeSec = submitSec,
                priceRatio = submitRatio,
                displayPrice = submitPrice,
                isBuy = pendingBuy,
                isPending = true,
                submittedAtEpochMs = currentTimeMillis()
            )
        )

        _uiState.update { it.copy(isOrderProcessing = true) }
        val lagMs = orderLagMs()
        _chartFrame.update {
            it.copy(
                markers = markers.toList(),
                activeOrderLagMs = lagMs
            )
        }

        orderJob?.cancel()
        orderTickJob?.cancel()
        orderJob = viewModelScope.launch {
            orderTickJob = launch {
                val tickInterval = lagMs / ORDER_TICK_COUNT
                repeat(ORDER_TICK_COUNT) { index ->
                    if (index > 0) delay(tickInterval)
                    if (_uiState.value.gameState != GameState.PLAYING) return@launch
                    soundPlayer.playOrderTick()
                }
            }

            delay(lagMs)
            orderTickJob?.cancel()
            orderTickJob = null
            if (_uiState.value.gameState != GameState.PLAYING) return@launch

            val fillSec = _chartFrame.value.elapsedSeconds
            val fillAbs = startOffsetSeconds + fillSec
            val fillRatio = PriceMath.ratioAt(sessionPriceRatios, fillAbs)
            val fillPrice = PriceMath.ratioToDisplayPrice(fillRatio)

            settleOrder(
                isBuy = pendingBuy,
                submitPrice = submitPrice,
                submitGameTimeSec = submitSec,
                submitPriceRatio = submitRatio,
                executionPrice = fillPrice,
                executionRatio = fillRatio,
                gameTimeSec = fillSec
            )
        }
    }

    private fun settleOrder(
        isBuy: Boolean,
        submitPrice: Int,
        executionPrice: Int,
        executionRatio: Float,
        gameTimeSec: Float,
        submitGameTimeSec: Float = Float.NaN,
        submitPriceRatio: Float = Float.NaN,
        showLabel: Boolean = true,
        playSound: Boolean = true,
        isForcedClose: Boolean = false
    ) {
        if (isBuy) {
            shares = cash.toFloat() / executionPrice.toFloat()
            cash = 0
        } else {
            cash = (shares * executionPrice).roundToInt()
            shares = 0f
        }

        val entryPrice = if (!isBuy && !isForcedClose) {
            markers.asReversed()
                .firstOrNull { it.isBuy && !it.isPending && !it.isCancelled }
                ?.displayPrice
                ?.takeIf { it > 0 }
        } else {
            null
        }

        val now = currentTimeMillis()
        // 判定スタンプ／COMBO は売りのみ（建値比）。買いには付けない。
        val quality = when {
            isForcedClose -> null
            isBuy -> null
            entryPrice == null -> FillQuality.OK
            else -> FillEvaluator.evaluateSell(entryPrice, executionPrice)
        }
        val lagFeel = if (!isForcedClose) {
            FillEvaluator.evaluateLag(isBuy, submitPrice, executionPrice)
        } else {
            FillQuality.OK
        }
        val judgmentMs = quality?.let { fillJudgmentDurationMs(it) } ?: 0L
        val judgmentUntil = if (quality != null && !isBuy && !isForcedClose) {
            now + judgmentMs
        } else {
            0L
        }

        markers.removeAll { it.isPending }
        markers.add(
            TradeMarker(
                gameTimeSec = gameTimeSec,
                priceRatio = executionRatio,
                displayPrice = executionPrice,
                isBuy = isBuy,
                isPending = false,
                isForcedClose = isForcedClose,
                submitGameTimeSec = submitGameTimeSec,
                submitPriceRatio = submitPriceRatio,
                submitDisplayPrice = if (submitPrice > 0) submitPrice else 0,
                fillQuality = quality,
                labelUntilEpochMs = if (showLabel) {
                    now + LABEL_VISIBLE_MS
                } else {
                    0L
                }
            )
        )

        if (playSound) {
            val judgeOn = _relicEffects.value.judgeFx == JudgeFxTier.ON
            if (isBuy) {
                soundPlayer.playBuy(lagFeel, judgeEnhanced = judgeOn)
                hapticPlayer.tickBuy(lagFeel)
            } else {
                val q = quality ?: FillQuality.OK
                soundPlayer.playSell(q, judgeEnhanced = judgeOn)
                hapticPlayer.tickSell(q)
            }
            if (!soundPlayer.enabled) {
                _chartFrame.update {
                    it.copy(edgeFlashUntilEpochMs = now + EDGE_FLASH_MS)
                }
            }
        }

        val markPrice = _uiState.value.currentPriceDisplay
        _uiState.update { current ->
            current.copy(
                hasStock = isBuy,
                isOrderProcessing = false,
                totalAsset = currentTotalAsset(markPrice)
            )
        }

        val prevCombo = _chartFrame.value.fillCombo
        // MISS 以外（PERFECT / GREAT / OK）で加算。買い・強制決済は不変。
        val nextCombo = when (quality) {
            FillQuality.FANTASTIC, FillQuality.PERFECT, FillQuality.GREAT, FillQuality.OK -> prevCombo + 1
            FillQuality.MISS -> 0
            null -> prevCombo
        }
        val comboIncreased = nextCombo > prevCombo
        if (nextCombo > maxFillCombo) {
            maxFillCombo = nextCombo
        }
        val nextExpires = when {
            nextCombo <= 0 -> 0L
            comboIncreased -> now + FILL_COMBO_HOLD_MS
            else -> _chartFrame.value.fillComboExpiresAtEpochMs
        }
        val nextBounce = if (comboIncreased) now else _chartFrame.value.fillComboBounceEpochMs

        var healthAfter = sessionHealth
        var shouldRetire = false
        var lifeLossToken = _chartFrame.value.lifeLossTokenEpochMs
        var lifeLossOrb = _chartFrame.value.lifeLossOrbIndex
        if (quality == FillQuality.MISS &&
            !isForcedClose &&
            _uiState.value.gameState == GameState.PLAYING &&
            !isRetiredRun
        ) {
            sessionHealth = (sessionHealth - 1).coerceAtLeast(0)
            healthAfter = sessionHealth
            lifeLossToken = now
            lifeLossOrb = healthAfter
            if (sessionHealth <= 0) {
                shouldRetire = true
            }
            viewModelScope.launch {
                delay(LIFE_LOSS_FEEDBACK_DELAY_MS)
                if (_uiState.value.gameState != GameState.PLAYING) return@launch
                hapticPlayer.tickLifeLoss()
            }
        }
        val healEvery = _relicEffects.value.comboHealEvery
        if (comboIncreased &&
            healEvery > 0 &&
            nextCombo % healEvery == 0 &&
            !shouldRetire &&
            _uiState.value.gameState == GameState.PLAYING
        ) {
            sessionHealth = (sessionHealth + 1).coerceAtMost(sessionMaxHealth)
            healthAfter = sessionHealth
        }

        _chartFrame.update {
            it.copy(
                markers = markers.toList(),
                hasStock = isBuy,
                fillCombo = nextCombo,
                fillComboExpiresAtEpochMs = nextExpires,
                fillComboBounceEpochMs = if (nextCombo <= 0) 0L else nextBounce,
                fillJudgmentQuality = if (!isBuy) quality else null,
                fillJudgmentUntilEpochMs = judgmentUntil,
                healthPoints = healthAfter,
                maxHealthPoints = sessionMaxHealth,
                lifeLossTokenEpochMs = lifeLossToken,
                lifeLossOrbIndex = lifeLossOrb
            )
        }

        if (!isForcedClose && _uiState.value.gameState == GameState.PLAYING && !shouldRetire) {
            scheduleOrderChargeCooldown()
        }
        if (shouldRetire) {
            beginRetireSequence()
        }
    }

    /** 体力切れ：タイマー停止→ショック→RETIRE→自動出金。 */
    private fun beginRetireSequence() {
        if (isRetiredRun) return
        isRetiredRun = true
        tickJob?.cancel()
        tickJob = null
        orderJob?.cancel()
        orderJob = null
        orderTickJob?.cancel()
        orderTickJob = null
        chargeCompleteJob?.cancel()
        chargeCompleteJob = null
        cancelPendingOrdersOnTimeout()

        _uiState.update { it.copy(isOrderProcessing = false) }
        _chartFrame.update {
            it.copy(
                isRunning = false,
                healthPoints = 0,
                retirePhase = RetirePhase.SHOCK,
                showFinishBanner = false
            )
        }

        retireJob?.cancel()
        retireJob = viewModelScope.launch {
            delay(RETIRE_SHOCK_MS)
            if (_uiState.value.gameState != GameState.PLAYING) return@launch
            _chartFrame.update { it.copy(retirePhase = RetirePhase.BANNER) }
            delay(RETIRE_BANNER_MS)
            if (_uiState.value.gameState != GameState.PLAYING) return@launch
            finishGame(retired = true)
            claimResultPayout()
        }
    }

    fun claimResultPayout() {
        if (_uiState.value.gameState != GameState.RESULT) return
        if (_chartFrame.value.resultPayoutClaimed) return
        val result = _chartFrame.value.resultInfo ?: return
        hapticPlayer.tickPayoutClaim(big = result.finalAsset >= INITIAL_ASSET)
        lifetimePnlRepository.recordFinalAsset(
            result.finalAsset,
            result.endBalance - result.startBalance
        )
        val persist = lifetimePnlRepository.onCashOut(result.endBalance)
        lifetimePnlRepository.recordSessionPnlHistory(
            brandNameJp = if (result.isRetired) UiStrings.retireBanner() else stage.brandName,
            brandNameEn = if (result.isRetired) UiStrings.retireBanner() else stage.brandNameEn,
            pnl = persist.balance - persist.previousBalance
        )
        _balance.value = persist.balance
        _balanceHistory.value = lifetimePnlRepository.getBalanceHistory()
        _peakBalance.value = persist.peakBalance
        _sessionPnlHistory.value = lifetimePnlRepository.getSessionPnlHistory()
        _cashOutCount.value = persist.cashOutCount
        _pendingRelics.value = persist.pendingRelics
        _newlyPendingRelics.value = persist.newlyPending
        _resultIntroFinished.value = false
        refreshRelicState()
        _chartFrame.update { it.copy(resultPayoutClaimed = true) }
    }

    private fun currentTotalAsset(markPrice: Int): Int {
        return if (shares > 0f) {
            (shares * markPrice).roundToInt()
        } else {
            cash
        }
    }

    private fun beginFinishSequence() {
        if (_uiState.value.gameState != GameState.PLAYING) return

        tickJob?.cancel()
        tickJob = null
        orderJob?.cancel()
        orderJob = null
        orderTickJob?.cancel()
        orderTickJob = null
        cancelPendingOrdersOnTimeout()

        val absoluteEnd = startOffsetSeconds + GAME_DURATION_SECONDS.toFloat()
        val endRatio = PriceMath.ratioAt(sessionPriceRatios, absoluteEnd)
        val finalPrice = PriceMath.ratioToDisplayPrice(endRatio)
        val finalAsset = currentTotalAsset(finalPrice)

        _uiState.update {
            it.copy(
                gameState = GameState.FINISHING,
                currentSecond = GAME_DURATION_SECONDS,
                currentPriceDisplay = finalPrice,
                totalAsset = finalAsset,
                isOrderProcessing = false
            )
        }
        _chartFrame.update {
            it.copy(
                isRunning = false,
                elapsedMs = GAME_DURATION_MS,
                showFinishBanner = true,
                markers = markers.toList(),
                hasStock = shares > 0f
            )
        }
        soundPlayer.playFinish()
        syncBgmAmbient()
        // 自動で RESULT に進まない。FINISH 表示のまま「出金」待ち。
        finishSequenceJob?.cancel()
        finishSequenceJob = null
    }

    private fun finishGame(retired: Boolean = false) {
        finishSequenceJob?.cancel()
        finishSequenceJob = null
        retireJob?.cancel()
        retireJob = null
        orderJob?.cancel()
        orderJob = null
        orderTickJob?.cancel()
        orderTickJob = null
        tickJob?.cancel()
        tickJob = null

        cancelPendingOrdersOnTimeout()

        for (i in markers.indices) {
            markers[i] = markers[i].copy(labelUntilEpochMs = 0L)
        }

        val elapsedMs = if (retired) {
            _chartFrame.value.elapsedMs.coerceIn(0L, GAME_DURATION_MS)
        } else {
            GAME_DURATION_MS
        }
        val endSec = (elapsedMs / 1_000f).coerceAtMost(GAME_DURATION_SECONDS.toFloat())
        val absoluteEnd = startOffsetSeconds + endSec
        val endRatio = PriceMath.ratioAt(sessionPriceRatios, absoluteEnd)
        val finalPrice = PriceMath.ratioToDisplayPrice(endRatio)

        if (shares > 0f) {
            settleOrder(
                isBuy = false,
                submitPrice = finalPrice,
                executionPrice = finalPrice,
                executionRatio = endRatio,
                gameTimeSec = endSec,
                showLabel = false,
                playSound = false,
                isForcedClose = true
            )
        }

        val reviewedMarkers = TradeTimingEvaluator.applyReviews(
            priceRatios = sessionPriceRatios,
            startOffsetSeconds = startOffsetSeconds,
            markers = markers.toList()
        )
        markers.clear()
        markers.addAll(reviewedMarkers)

        var finalAsset = cash
        val rawTradeGainLoss = finalAsset - INITIAL_ASSET
        val tradeMult = _relicEffects.value.tradePnlMult
        val tradeBoostApplied = !retired && rawTradeGainLoss > 0 && tradeMult > 1f
        if (tradeBoostApplied) {
            val boostedTrade = (rawTradeGainLoss * tradeMult).roundToInt()
            val bonus = boostedTrade - rawTradeGainLoss
            cash += bonus
            finalAsset += bonus
        }
        val tradeGainLoss = finalAsset - INITIAL_ASSET
        val endBalance = (stakeBalance * finalAsset / INITIAL_ASSET).coerceAtLeast(0L)
        val returnPercent = SyncRating.returnPercent(finalAsset)
        val theoretical = SyncCalculator.stageTargetAsset(stage.priceRatios)
        val sync = if (retired) {
            0f
        } else {
            SyncCalculator.syncPercent(
                playerAsset = finalAsset,
                theoreticalMax = theoretical,
            )
        }

        val priorBestAsset = scrapbookRepository.getBestAsset(stage.stageId)
        val clearRecord = if (retired) {
            ClearRecord(firstUnlock = false, newBest = false)
        } else {
            scrapbookRepository.recordClear(stage.stageId, finalAsset).also { refreshScrapbook() }
        }

        if (!retired) {
            soundPlayer.playTimeUp(sync)
            hapticPlayer.tickTimeUp(highSync = sync >= 70f)
        } else {
            hapticPlayer.tickTimeUp(highSync = false)
        }

        val filledCount = reviewedMarkers.count { !it.isPending && !it.isCancelled }
        val halfSync = !retired && "%.1f".fmt(sync) == "50.0"
        val lang = _displayLanguage.value
        val retireFlavor = if (retired) {
            Random.nextInt(UiStrings.retireFlavorCount)
        } else {
            -1
        }
        val result = ResultInfo(
            stageId = stage.stageId,
            brandName = stage.brandName(lang),
            calendarStart = stage.calendarStart(lang),
            calendarEnd = stage.calendarEnd(lang),
            instrumentName = stage.instrumentName(lang),
            scenarioType = stage.scenarioType,
            historyComment = stage.historyComment(lang),
            finalAsset = finalAsset,
            startBalance = stakeBalance,
            endBalance = endBalance,
            returnPercent = returnPercent,
            tradeGainLoss = tradeGainLoss,
            theoreticalMaxAsset = theoretical,
            syncPercent = sync,
            niceTradeCount = TradeTimingEvaluator.countNice(reviewedMarkers),
            missTradeCount = TradeTimingEvaluator.countMiss(reviewedMarkers),
            filledTradeCount = filledCount,
            isGhostRun = filledCount == 0,
            isBreakEven = finalAsset == INITIAL_ASSET,
            isHalfSync = halfSync,
            isFirstClear = clearRecord.firstUnlock,
            isNewBest = clearRecord.newBest,
            priorBestAsset = priorBestAsset,
            tradePnlBoostApplied = tradeBoostApplied,
            tradePnlBoostMult = if (tradeBoostApplied) tradeMult else 1f,
            maxCombo = maxFillCombo,
            isRetired = retired,
            retireFlavorIndex = retireFlavor,
        )

        val displaySecond = if (retired) {
            (endSec.toInt()).coerceIn(0, GAME_DURATION_SECONDS - 1)
        } else {
            GAME_DURATION_SECONDS - 1
        }
        _uiState.update { current ->
            current.copy(
                gameState = GameState.RESULT,
                currentSecond = displaySecond,
                currentPriceDisplay = finalPrice,
                totalAsset = finalAsset,
                isOrderProcessing = false,
                hasStock = false
            )
        }
        _chartFrame.update {
            it.copy(
                isRunning = false,
                elapsedMs = elapsedMs,
                showFinishBanner = false,
                retirePhase = RetirePhase.NONE,
                markers = reviewedMarkers,
                hasStock = false,
                resultInfo = result,
                healthPoints = if (retired) 0 else sessionHealth,
                maxHealthPoints = sessionMaxHealth
            )
        }
        syncBgmAmbient()
    }

    private fun maybeEndgameHeart(elapsedMs: Long, second: Int) {
        if (second < ENDGAME_TENSION_FROM_SECOND || second >= GAME_DURATION_SECONDS) return
        val intervalMs = when {
            second >= ENDGAME_COUNTDOWN_FROM_SECOND -> ENDGAME_HEART_INTERVAL_COUNTDOWN_MS
            second >= ENDGAME_PULSE_FROM_SECOND -> ENDGAME_HEART_INTERVAL_PULSE_MS
            else -> ENDGAME_HEART_INTERVAL_TENSION_MS
        }
        if (lastEndgameHeartElapsedMs >= 0L &&
            elapsedMs - lastEndgameHeartElapsedMs < intervalMs
        ) {
            return
        }
        lastEndgameHeartElapsedMs = elapsedMs
        val intensity = when {
            second >= ENDGAME_COUNTDOWN_FROM_SECOND -> 0.9f
            second >= ENDGAME_PULSE_FROM_SECOND -> 0.55f
            else -> 0.28f
        }
        // 55〜はカウントダウン SE 優先（心拍は触覚のみ）
        if (second < ENDGAME_COUNTDOWN_FROM_SECOND) {
            soundPlayer.playEndgameHeart(intensity)
        }
        hapticPlayer.tickEndgameHeart(intensity)
    }

    private fun resetToStart() {
        cancelJobs()
        cash = INITIAL_ASSET
        shares = 0f
        markers.clear()
        lastCountdownDigitPlayed = -1
        lastEndgameHeartElapsedMs = -1L
        maxFillCombo = 0
        sessionMaxHealth = MAX_HEALTH
        sessionHealth = MAX_HEALTH
        isRetiredRun = false
        isFirstStartAfterLaunch = false
        _startSmallText.value = StartScreenCopy.randomSmallText(_displayLanguage.value)
        _uiState.value = GameUiState()
        _chartFrame.value = ChartFrame()
        _resultIntroFinished.value = false
        _newlyPendingRelics.value = emptyList()
        stakeBalance = _balance.value
        _showBankrupt.value = lifetimePnlRepository.isBankrupt()
        accrueDailyPlan()
        syncBgmAmbient()
    }

    private fun refreshStartSmallTextForCurrentState() {
        if (_uiState.value.gameState != GameState.START) return
        _startSmallText.value = if (isFirstStartAfterLaunch) {
            StartScreenCopy.launchTutorial(_displayLanguage.value, orderLagMs())
        } else {
            StartScreenCopy.randomSmallText(_displayLanguage.value)
        }
    }

    private fun cancelPendingOrdersOnTimeout() {
        var hadPending = false
        for (index in markers.indices) {
            val marker = markers[index]
            if (marker.isPending) {
                hadPending = true
                markers[index] = marker.copy(isPending = false, isCancelled = true)
            }
        }
        if (hadPending) {
            _uiState.update { it.copy(isOrderProcessing = false) }
        }
    }

    private fun cancelJobs() {
        finishSequenceJob?.cancel()
        finishSequenceJob = null
        retireJob?.cancel()
        retireJob = null
        tickJob?.cancel()
        tickJob = null
        orderJob?.cancel()
        orderJob = null
        orderTickJob?.cancel()
        orderTickJob = null
        chargeCompleteJob?.cancel()
        chargeCompleteJob = null
    }

    override fun onCleared() {
        cancelJobs()
        soundPlayer.release()
        bgmPlayer.release()
        super.onCleared()
    }

    companion object {
        /** 内部 Extreme ステージ（e01..）の出現率。標準30本との抽選は別プール。 */
        private const val EXTREME_STAGE_SPAWN_CHANCE = 0.08f

        /** 試験用: true の間はパイロット3本（v01/z01/b01）のみ抽選・図鑑表示 */
        private const val PILOT_STAGE_FILTER_ENABLED = false
        private val PILOT_STAGE_IDS = setOf("v01", "z01", "b01")

        const val INITIAL_ASSET = 10_000
        const val MAX_HEALTH = 4
        /** MISS 判定スタンプのあと、LIFE 球が消える／触覚までの間 */
        const val LIFE_LOSS_FEEDBACK_DELAY_MS = 320L
        const val RETIRE_SHOCK_MS = 480L
        const val RETIRE_BANNER_MS = 1_600L
        const val GAME_DURATION_SECONDS = 60
        const val GAME_DURATION_MS = GAME_DURATION_SECONDS * 1_000L
        const val ORDER_LAG_MS = 1_000L
        const val ORDER_COOLDOWN_MS = 1_000L
        const val ORDER_TICK_COUNT = 4
        const val CHARGE_READY_POP_MS = 180L
        const val EDGE_FLASH_MS = 220L
        /** COMBO 常駐。増加でリセット、未延長で切れる。 */
        const val FILL_COMBO_HOLD_MS = 10_000L
        const val FRAME_DELAY_MS = 16L
        const val LABEL_VISIBLE_MS = 2_200L
        const val MAX_START_OFFSET_INCLUSIVE = 10
        const val Y_ZOOM_ANIM_MS = 200
        const val NOW_X_FRACTION = 0.78f
        const val Y_PAD_RATIO = 0.10f
        const val Y_PAD_MIN_RATIO = 1.0f
        const val INTEREST_TICK_VISIBLE_MS = 900L
        const val FINISH_HOLD_MS = 1_000L
        /** 終盤緊張（TIME弱パルス＋心拍）開始。 */
        const val ENDGAME_TENSION_FROM_SECOND = 45
        /** 資産隠し開始。 */
        const val ENDGAME_PULSE_FROM_SECOND = 50
        const val ENDGAME_COUNTDOWN_FROM_SECOND = 55
        private const val ENDGAME_HEART_INTERVAL_TENSION_MS = 900L
        private const val ENDGAME_HEART_INTERVAL_PULSE_MS = 650L
        private const val ENDGAME_HEART_INTERVAL_COUNTDOWN_MS = 420L
    }
}
