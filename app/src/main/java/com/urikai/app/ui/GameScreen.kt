package com.urikai.app.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.roundToInt
import com.urikai.app.data.AssetRankBoard
import com.urikai.app.data.ScrapbookEntry
import com.urikai.app.game.AppLayer
import com.urikai.app.game.AudioLevel
import com.urikai.app.game.ChartFrame
import com.urikai.app.game.ChartLineStyle
import com.urikai.app.game.DisplayLanguage
import com.urikai.app.game.FillQuality
import com.urikai.app.game.GameState
import com.urikai.app.game.GameUiState
import com.urikai.app.game.GameViewModel
import com.urikai.app.game.HoldingPnlMood
import com.urikai.app.game.HudMode
import com.urikai.app.game.LcdThemeMode
import com.urikai.app.game.OrderCharge
import com.urikai.app.game.PanelContrast
import com.urikai.app.game.RelicEffects
import com.urikai.app.game.RelicId
import com.urikai.app.game.RelicUpgradeGroup
import com.urikai.app.game.ResultInfo
import com.urikai.app.game.RetirePhase
import com.urikai.app.game.ScanlineStrength
import com.urikai.app.game.ScrapbookSort
import com.urikai.app.game.StartScreenCopy
import com.urikai.app.game.SurfaceTone
import com.urikai.app.game.SyncRating
import com.urikai.app.game.TimingFocus
import com.urikai.app.game.TutorialGuideKind
import com.urikai.app.game.VisibleWindow
import com.urikai.app.game.ResultChaseHint
import com.urikai.app.game.ResultChaseHints
import com.urikai.app.game.holdingEntryPrice
import com.urikai.app.game.holdingPnlMood
import com.urikai.app.game.toTimingFocus
import com.urikai.app.game.UiStrings
import com.urikai.app.ui.theme.Ink
import com.urikai.app.ui.theme.InkFaint
import com.urikai.app.ui.theme.InkMuted
import com.urikai.app.ui.theme.LcdBackground
import com.urikai.app.ui.theme.LcdButtonFill
import com.urikai.app.ui.theme.LcdButtonLabel
import com.urikai.app.ui.theme.LcdInvertedText
import com.urikai.app.ui.theme.LocalDisplayLanguage
import com.urikai.app.ui.theme.PixelFont
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private const val RESULT_ASSET_ANIM_MS = 280
private const val RESULT_SYNC_ANIM_MS = 700
/** FINISH 直後。連打での出金スキップ防止。 */
private const val FINISH_TALLY_MS = 1_000L
private const val FINISH_TALLY_DOT_MS = 160L

/**
 * ローファイ液晶UI。巨大操作ボタンは常に1つ。
 */
@Composable
fun GameScreen(
    uiState: GameUiState,
    chartFrame: ChartFrame,
    appLayer: AppLayer,
    scrapbookEntries: List<ScrapbookEntry>,
    hudMode: HudMode,
    lcdTheme: LcdThemeMode,
    displayLanguage: DisplayLanguage,
    chartLineStyle: ChartLineStyle,
    scanlineStrength: ScanlineStrength,
    panelContrast: PanelContrast,
    surfaceTone: SurfaceTone,
    seLevel: AudioLevel,
    bgmLevel: AudioLevel,
    hapticEnabled: Boolean,
    scrapbookSort: ScrapbookSort,
    startSmallText: String,
    lifetimePnl: Int = 0,
    sessionPnlHistory: List<com.urikai.app.data.SessionPnlHistoryEntry> = emptyList(),
    cashOutCount: Int = 0,
    seenTutorialGuides: List<TutorialGuideKind> = emptyList(),
    pendingRelics: List<RelicId> = emptyList(),
    newlyPendingRelics: List<RelicId> = emptyList(),
    claimedRelics: List<RelicId> = emptyList(),
    relicEffects: RelicEffects = RelicEffects(),
    isBonusRun: Boolean = false,
    showBonusTutorial: Boolean = false,
    showFirstPlayTutorial: Boolean = false,
    showResultTutorial: Boolean = false,
    showContentDisclaimer: Boolean,
    chartsLoadError: String?,
    onDismissContentDisclaimer: () -> Unit,
    onDismissRelicRewardToast: () -> Unit = {},
    onPrimaryButtonClick: () -> Unit,
    onOpenScrapbook: () -> Unit,
    onOpenOption: () -> Unit,
    onHudModeChange: (HudMode) -> Unit,
    onLcdThemeChange: (LcdThemeMode) -> Unit,
    onDisplayLanguageChange: (DisplayLanguage) -> Unit,
    onChartLineStyleChange: (ChartLineStyle) -> Unit,
    onScanlineStrengthChange: (ScanlineStrength) -> Unit,
    onPanelContrastChange: (PanelContrast) -> Unit,
    onSurfaceToneChange: (SurfaceTone) -> Unit,
    onSeLevelChange: (AudioLevel) -> Unit,
    onBgmLevelChange: (AudioLevel) -> Unit,
    onHapticEnabledChange: (Boolean) -> Unit,
    onScrapbookSortChange: (ScrapbookSort) -> Unit,
    onShare: () -> Unit,
    onShuffleStartTip: () -> Unit = {},
    onClaimRelic: (RelicId) -> Boolean = { false },
    onToggleRelicVisual: (RelicId) -> Unit = {},
    onCycleRelicStage: (RelicUpgradeGroup) -> Unit = {},
    relicStagePrefs: Map<RelicUpgradeGroup, RelicId> = emptyMap(),
    onDismissBonusTutorial: () -> Unit = {},
    onDismissFirstPlayTutorial: () -> Unit = {},
    onResultIntroFinished: () -> Unit = {},
    onDismissResultTutorial: () -> Unit = {},
    onRequestAssetRank: () -> com.urikai.app.data.AssetRankBoard? = { null },
    devRelicLab: Map<RelicId, Boolean>? = null,
    onEnableDevRelicLab: () -> Unit = {},
    onDisableDevRelicLab: () -> Unit = {},
    onSetDevRelicEnabled: (RelicId, Boolean) -> Unit = { _, _ -> },
    onPayoutCountTick: (Float) -> Unit = {},
    onPayoutClang: (Boolean) -> Unit = {},
    onResultPanelIn: () -> Unit = {},
    onResultType: () -> Unit = {},
    onResultStar: (index: Int, total: Int) -> Unit = { _, _ -> },
    onResultStamp: () -> Unit = {},
    onUiConfirm: () -> Unit = {},
    onUiSelect: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    CompositionLocalProvider(
        LocalUiSound provides UiSoundBus(confirm = onUiConfirm, select = onUiSelect)
    ) {
        GameScreenContent(
            uiState = uiState,
            chartFrame = chartFrame,
            appLayer = appLayer,
            scrapbookEntries = scrapbookEntries,
            hudMode = hudMode,
            lcdTheme = lcdTheme,
            displayLanguage = displayLanguage,
            chartLineStyle = chartLineStyle,
            scanlineStrength = scanlineStrength,
            panelContrast = panelContrast,
            surfaceTone = surfaceTone,
            seLevel = seLevel,
            bgmLevel = bgmLevel,
            hapticEnabled = hapticEnabled,
            scrapbookSort = scrapbookSort,
            startSmallText = startSmallText,
            lifetimePnl = lifetimePnl,
            sessionPnlHistory = sessionPnlHistory,
            cashOutCount = cashOutCount,
            seenTutorialGuides = seenTutorialGuides,
            pendingRelics = pendingRelics,
            newlyPendingRelics = newlyPendingRelics,
            claimedRelics = claimedRelics,
            relicEffects = relicEffects,
            isBonusRun = isBonusRun,
            showBonusTutorial = showBonusTutorial,
            showFirstPlayTutorial = showFirstPlayTutorial,
            showResultTutorial = showResultTutorial,
            showContentDisclaimer = showContentDisclaimer,
            chartsLoadError = chartsLoadError,
            onDismissContentDisclaimer = onDismissContentDisclaimer,
            onDismissRelicRewardToast = onDismissRelicRewardToast,
            onPrimaryButtonClick = onPrimaryButtonClick,
            onOpenScrapbook = onOpenScrapbook,
            onOpenOption = onOpenOption,
            onHudModeChange = onHudModeChange,
            onLcdThemeChange = onLcdThemeChange,
            onDisplayLanguageChange = onDisplayLanguageChange,
            onChartLineStyleChange = onChartLineStyleChange,
            onScanlineStrengthChange = onScanlineStrengthChange,
            onPanelContrastChange = onPanelContrastChange,
            onSurfaceToneChange = onSurfaceToneChange,
            onSeLevelChange = onSeLevelChange,
            onBgmLevelChange = onBgmLevelChange,
            onHapticEnabledChange = onHapticEnabledChange,
            onScrapbookSortChange = onScrapbookSortChange,
            onShare = onShare,
            onShuffleStartTip = onShuffleStartTip,
            onClaimRelic = onClaimRelic,
            onToggleRelicVisual = onToggleRelicVisual,
            onCycleRelicStage = onCycleRelicStage,
            relicStagePrefs = relicStagePrefs,
            onDismissBonusTutorial = onDismissBonusTutorial,
            onDismissFirstPlayTutorial = onDismissFirstPlayTutorial,
            onResultIntroFinished = onResultIntroFinished,
            onDismissResultTutorial = onDismissResultTutorial,
            onRequestAssetRank = onRequestAssetRank,
            devRelicLab = devRelicLab,
            onEnableDevRelicLab = onEnableDevRelicLab,
            onDisableDevRelicLab = onDisableDevRelicLab,
            onSetDevRelicEnabled = onSetDevRelicEnabled,
            onPayoutCountTick = onPayoutCountTick,
            onPayoutClang = onPayoutClang,
            onResultPanelIn = onResultPanelIn,
            onResultType = onResultType,
            onResultStar = onResultStar,
            onResultStamp = onResultStamp,
            modifier = modifier
        )
    }
}

@Composable
private fun GameScreenContent(
    uiState: GameUiState,
    chartFrame: ChartFrame,
    appLayer: AppLayer,
    scrapbookEntries: List<ScrapbookEntry>,
    hudMode: HudMode,
    lcdTheme: LcdThemeMode,
    displayLanguage: DisplayLanguage,
    chartLineStyle: ChartLineStyle,
    scanlineStrength: ScanlineStrength,
    panelContrast: PanelContrast,
    surfaceTone: SurfaceTone,
    seLevel: AudioLevel,
    bgmLevel: AudioLevel,
    hapticEnabled: Boolean,
    scrapbookSort: ScrapbookSort,
    startSmallText: String,
    lifetimePnl: Int = 0,
    sessionPnlHistory: List<com.urikai.app.data.SessionPnlHistoryEntry> = emptyList(),
    cashOutCount: Int = 0,
    seenTutorialGuides: List<TutorialGuideKind> = emptyList(),
    pendingRelics: List<RelicId> = emptyList(),
    newlyPendingRelics: List<RelicId> = emptyList(),
    claimedRelics: List<RelicId> = emptyList(),
    relicEffects: RelicEffects = RelicEffects(),
    isBonusRun: Boolean = false,
    showBonusTutorial: Boolean = false,
    showFirstPlayTutorial: Boolean = false,
    showResultTutorial: Boolean = false,
    showContentDisclaimer: Boolean,
    chartsLoadError: String?,
    onDismissContentDisclaimer: () -> Unit,
    onDismissRelicRewardToast: () -> Unit = {},
    onPrimaryButtonClick: () -> Unit,
    onOpenScrapbook: () -> Unit,
    onOpenOption: () -> Unit,
    onHudModeChange: (HudMode) -> Unit,
    onLcdThemeChange: (LcdThemeMode) -> Unit,
    onDisplayLanguageChange: (DisplayLanguage) -> Unit,
    onChartLineStyleChange: (ChartLineStyle) -> Unit,
    onScanlineStrengthChange: (ScanlineStrength) -> Unit,
    onPanelContrastChange: (PanelContrast) -> Unit,
    onSurfaceToneChange: (SurfaceTone) -> Unit,
    onSeLevelChange: (AudioLevel) -> Unit,
    onBgmLevelChange: (AudioLevel) -> Unit,
    onHapticEnabledChange: (Boolean) -> Unit,
    onScrapbookSortChange: (ScrapbookSort) -> Unit,
    onShare: () -> Unit,
    onShuffleStartTip: () -> Unit = {},
    onClaimRelic: (RelicId) -> Boolean = { false },
    onToggleRelicVisual: (RelicId) -> Unit = {},
    onCycleRelicStage: (RelicUpgradeGroup) -> Unit = {},
    relicStagePrefs: Map<RelicUpgradeGroup, RelicId> = emptyMap(),
    onDismissBonusTutorial: () -> Unit = {},
    onDismissFirstPlayTutorial: () -> Unit = {},
    onResultIntroFinished: () -> Unit = {},
    onDismissResultTutorial: () -> Unit = {},
    onRequestAssetRank: () -> com.urikai.app.data.AssetRankBoard? = { null },
    devRelicLab: Map<RelicId, Boolean>? = null,
    onEnableDevRelicLab: () -> Unit = {},
    onDisableDevRelicLab: () -> Unit = {},
    onSetDevRelicEnabled: (RelicId, Boolean) -> Unit = { _, _ -> },
    onPayoutCountTick: (Float) -> Unit = {},
    onPayoutClang: (Boolean) -> Unit = {},
    onResultPanelIn: () -> Unit = {},
    onResultType: () -> Unit = {},
    onResultStar: (index: Int, total: Int) -> Unit = { _, _ -> },
    onResultStamp: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var resultChartPeek by remember { mutableStateOf(false) }
    var bootScan by remember { mutableStateOf(true) }
    var titleSecretInvert by remember { mutableStateOf(false) }
    var showRelicBox by remember { mutableStateOf(false) }
    var claimRelicTarget by remember { mutableStateOf<RelicId?>(null) }
    var claimCelebrating by remember { mutableStateOf(false) }
    var showSessionPnlHistory by remember { mutableStateOf(false) }
    var showTutorialGuide by remember { mutableStateOf(false) }
    /** 出金演出が終わってから結果パネルを出す（一瞬フラッシュ防止）。 */
    var payoutAnimDone by remember { mutableStateOf(false) }
    /** リザルト導入アニメは1回だけ（チャートpeekで消えない）。 */
    var resultIntroDone by remember { mutableStateOf(false) }
    var activeFillJudgment by remember {
        mutableStateOf<FillQuality?>(null)
    }
    LaunchedEffect(Unit) {
        delay(320)
        bootScan = false
    }
    LaunchedEffect(claimCelebrating, claimRelicTarget, pendingRelics) {
        if (!claimCelebrating || claimRelicTarget == null) return@LaunchedEffect
        delay(1_250)
        claimCelebrating = false
        claimRelicTarget = pendingRelics.firstOrNull()
    }
    LaunchedEffect(titleSecretInvert) {
        if (titleSecretInvert) {
            delay(280)
            titleSecretInvert = false
        }
    }
    LaunchedEffect(uiState.gameState) {
        if (uiState.gameState != GameState.RESULT) {
            resultChartPeek = false
            payoutAnimDone = false
            resultIntroDone = false
        }
    }
    // START から離れたら受取／箱／履歴などのローカルモーダルを破棄（戻り時の張り付き防止）
    LaunchedEffect(appLayer, uiState.gameState) {
        if (appLayer != AppLayer.PLAY || uiState.gameState != GameState.START) {
            showRelicBox = false
            claimRelicTarget = null
            claimCelebrating = false
            showSessionPnlHistory = false
            showTutorialGuide = false
        }
    }
    val showRelicRewardToast =
        uiState.gameState == GameState.RESULT &&
            chartFrame.resultPayoutClaimed &&
            payoutAnimDone &&
            resultIntroDone &&
            !showResultTutorial &&
            newlyPendingRelics.isNotEmpty()
    val startModalOpen =
        claimRelicTarget != null ||
            showRelicBox ||
            showSessionPnlHistory ||
            showTutorialGuide ||
            showContentDisclaimer
    val resultPresentationOpen =
        uiState.gameState == GameState.RESULT &&
            chartFrame.resultPayoutClaimed &&
            (!payoutAnimDone || !resultIntroDone || showRelicRewardToast)
    val modalNavLocked =
        showBonusTutorial ||
            showFirstPlayTutorial ||
            showResultTutorial ||
            startModalOpen ||
            resultPresentationOpen
    LaunchedEffect(chartFrame.fillJudgmentUntilEpochMs, chartFrame.fillJudgmentQuality) {
        val quality = chartFrame.fillJudgmentQuality
        val until = chartFrame.fillJudgmentUntilEpochMs
        if (quality == null || until <= 0L) {
            activeFillJudgment = null
            return@LaunchedEffect
        }
        activeFillJudgment = quality
        val left = until - System.currentTimeMillis()
        if (left > 0) delay(left)
        activeFillJudgment = null
    }


    var edgeFlash by remember { mutableStateOf(false) }
    LaunchedEffect(chartFrame.edgeFlashUntilEpochMs) {
        val until = chartFrame.edgeFlashUntilEpochMs
        if (until <= 0L) {
            edgeFlash = false
            return@LaunchedEffect
        }
        edgeFlash = true
        val left = until - System.currentTimeMillis()
        if (left > 0) delay(left)
        edgeFlash = false
    }

    Box(modifier = modifier.fillMaxSize()) {
        LcdBackdrop()
        if (bootScan) {
            BootScanOverlay(modifier = Modifier.fillMaxSize())
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
        ) {
            TopHud(
                uiState = uiState,
                chartFrame = chartFrame,
                appLayer = appLayer,
                hudMode = hudMode,
                blockNav = modalNavLocked,
                onOpenScrapbook = onOpenScrapbook,
                onOpenOption = onOpenOption,
                onOpenTutorialGuide = { showTutorialGuide = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 12.dp)
            )

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp)
            ) {
                when (appLayer) {
                    AppLayer.SCRAPBOOK -> {
                        ScrapbookPane(
                            entries = scrapbookEntries,
                            sort = scrapbookSort,
                            onSortChange = onScrapbookSortChange
                        )
                    }

                    AppLayer.OPTION -> {
                        OptionPane(
                            hudMode = hudMode,
                            displayLanguage = displayLanguage,
                            scanlineStrength = scanlineStrength,
                            panelContrast = panelContrast,
                            surfaceTone = surfaceTone,
                            seLevel = seLevel,
                            bgmLevel = bgmLevel,
                            hapticEnabled = hapticEnabled,
                            onHudModeChange = onHudModeChange,
                            onDisplayLanguageChange = onDisplayLanguageChange,
                            onScanlineStrengthChange = onScanlineStrengthChange,
                            onPanelContrastChange = onPanelContrastChange,
                            onSurfaceToneChange = onSurfaceToneChange,
                            onSeLevelChange = onSeLevelChange,
                            onBgmLevelChange = onBgmLevelChange,
                            onHapticEnabledChange = onHapticEnabledChange,
                            devRelicLab = devRelicLab,
                            onEnableDevRelicLab = onEnableDevRelicLab,
                            onDisableDevRelicLab = onDisableDevRelicLab,
                            onSetDevRelicEnabled = onSetDevRelicEnabled
                        )
                    }

                    AppLayer.PLAY -> when {
                        chartsLoadError != null -> {
                            ChartsLoadErrorPanel(
                                detail = chartsLoadError,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                        uiState.gameState == GameState.START -> {
                            Box(modifier = Modifier.fillMaxSize()) {
                                Column(
                                    modifier = Modifier.align(Alignment.Center),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    StartAttractPanel(
                                        smallText = startSmallText,
                                        onFlingShuffle = onShuffleStartTip,
                                        secretInvert = titleSecretInvert,
                                        onSecretInvert = { titleSecretInvert = true }
                                    )
                                    LifetimeLedgerStrip(
                                        lifetimePnl = lifetimePnl,
                                        onLongPress = { showSessionPnlHistory = true }
                                    )
                                }
                                RelicBoxButton(
                                    unlocked = claimedRelics.isNotEmpty(),
                                    onClick = { showRelicBox = true },
                                    modifier = Modifier
                                        .align(Alignment.BottomStart)
                                        .padding(start = 8.dp, bottom = 8.dp)
                                )
                                RelicGiftBadge(
                                    pendingCount = pendingRelics.size,
                                    onClick = {
                                        claimCelebrating = false
                                        claimRelicTarget = pendingRelics.firstOrNull()
                                    },
                                    modifier = Modifier
                                        .align(Alignment.BottomEnd)
                                        .padding(end = 8.dp, bottom = 8.dp)
                                )
                                if (showSessionPnlHistory) {
                                    SessionPnlHistoryOverlay(
                                        entries = sessionPnlHistory,
                                        playCount = cashOutCount,
                                        onDismiss = { showSessionPnlHistory = false },
                                        modifier = Modifier.fillMaxSize()
                                    )
                                }
                                if (showTutorialGuide) {
                                    TutorialGuideOverlay(
                                        available = seenTutorialGuides,
                                        bonusMult = relicEffects.tenClearBonusMult,
                                        onDismiss = { showTutorialGuide = false },
                                        modifier = Modifier.fillMaxSize()
                                    )
                                }
                                if (showContentDisclaimer) {
                                    ContentDisclaimerOverlay(
                                        onDismiss = onDismissContentDisclaimer,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                }
                                val claiming = claimRelicTarget
                                if (claiming != null) {
                                    RelicClaimOverlay(
                                        relic = claiming,
                                        celebrating = claimCelebrating,
                                        onReceive = {
                                            if (onClaimRelic(claiming)) {
                                                claimCelebrating = true
                                            }
                                        },
                                        onDismiss = {
                                            if (claimCelebrating) {
                                                claimCelebrating = false
                                                claimRelicTarget = pendingRelics.firstOrNull()
                                            } else {
                                                claimRelicTarget = null
                                            }
                                        },
                                        modifier = Modifier.fillMaxSize()
                                    )
                                }
                                if (showRelicBox && claimedRelics.isNotEmpty()) {
                                    RelicBoxOverlay(
                                        claimed = claimedRelics,
                                        lcdTheme = lcdTheme,
                                        chartLineStyle = chartLineStyle,
                                        stagePrefs = relicStagePrefs,
                                        onToggleVisual = onToggleRelicVisual,
                                        onCycleStage = onCycleRelicStage,
                                        onDismiss = { showRelicBox = false },
                                        modifier = Modifier.fillMaxSize()
                                    )
                                }
                            }
                        }
                        else -> {
                            val isResult = uiState.gameState == GameState.RESULT
                            val isRetiredResult = chartFrame.resultInfo?.isRetired == true
                            var timingFocus by remember { mutableStateOf<TimingFocus?>(null) }
                            LaunchedEffect(uiState.gameState) {
                                if (uiState.gameState != GameState.RESULT) {
                                    timingFocus = null
                                }
                            }
                            LaunchedEffect(isRetiredResult) {
                                if (isRetiredResult) resultChartPeek = false
                            }
                            if (isResult && resultChartPeek && !isRetiredResult) {
                                Column(modifier = Modifier.fillMaxSize()) {
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .fillMaxWidth()
                                            .clickable(
                                                indication = null,
                                                interactionSource = remember { MutableInteractionSource() }
                                            ) {
                                                if (timingFocus != null) {
                                                    timingFocus = null
                                                } else {
                                                    resultChartPeek = false
                                                }
                                            }
                                    ) {
                                        PriceChart(
                                            frame = chartFrame,
                                            showFullSession = true,
                                            showTradeReview = true,
                                            focusTarget = timingFocus,
                                            lagFxTier = relicEffects.lagFx,
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .lcdBorder()
                                        )
                                    }
                                    ResultChartPeekFooter(
                                        frame = chartFrame,
                                        focusTarget = timingFocus,
                                        onHighlightClick = { moment ->
                                            val next = moment.toTimingFocus()
                                            timingFocus = if (timingFocus == next) null else next
                                        },
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }
                            } else {
                                Box(modifier = Modifier.fillMaxSize()) {
                                    PriceChart(
                                        frame = chartFrame,
                                        showFullSession = isResult,
                                        lagFxTier = relicEffects.lagFx,
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .lcdBorder()
                                    )
                                    EndgameOverlays(
                                        gameState = uiState.gameState,
                                        currentSecond = uiState.currentSecond,
                                        showFinishBanner = chartFrame.showFinishBanner
                                    )
                                    if (!isResult &&
                                        (uiState.gameState == GameState.PLAYING ||
                                            chartFrame.retireLocked)
                                    ) {
                                        HealthNanotechBar(
                                            health = chartFrame.healthPoints,
                                            maxHealth = chartFrame.maxHealthPoints,
                                            lifeLossTokenEpochMs = chartFrame.lifeLossTokenEpochMs,
                                            lifeLossOrbIndex = chartFrame.lifeLossOrbIndex,
                                            modifier = Modifier
                                                .align(Alignment.TopStart)
                                                .padding(start = 10.dp, top = 10.dp)
                                        )
                                    }
                                    if (chartFrame.retirePhase != RetirePhase.NONE) {
                                        RetireOverlay(
                                            phase = chartFrame.retirePhase,
                                            modifier = Modifier.fillMaxSize()
                                        )
                                    }
                                    if (!isResult && chartFrame.fillCombo > 0) {
                                        ComboSpotHud(
                                            combo = chartFrame.fillCombo,
                                            expiresAtEpochMs = chartFrame.fillComboExpiresAtEpochMs,
                                            bounceToken = chartFrame.fillComboBounceEpochMs,
                                            frameTick = chartFrame.elapsedMs,
                                            fxTier = relicEffects.comboFx,
                                            modifier = Modifier
                                                .align(Alignment.TopStart)
                                                .padding(start = 10.dp, top = 58.dp)
                                        )
                                    }
                                    if (!isResult &&
                                        isBonusRun &&
                                        uiState.gameState == GameState.PLAYING
                                    ) {
                                        LedgerBonusPlayBadge(
                                            mult = relicEffects.tenClearBonusMult,
                                            modifier = Modifier
                                                .align(Alignment.TopStart)
                                                .padding(start = 8.dp, top = 40.dp)
                                        )
                                    }
                                    val judgment = activeFillJudgment
                                    if (!isResult && judgment != null) {
                                        FillJudgmentOverlay(
                                            quality = judgment,
                                            token = chartFrame.fillJudgmentUntilEpochMs,
                                            fxTier = relicEffects.judgeFx,
                                            modifier = Modifier.fillMaxSize()
                                        )
                                    }
                                    if (isResult &&
                                        chartFrame.resultPayoutClaimed &&
                                        payoutAnimDone
                                    ) {
                                        if (!isRetiredResult) {
                                            ResultChartPeekScrim(
                                                onPeek = { resultChartPeek = true },
                                                modifier = Modifier.fillMaxSize()
                                            )
                                        }
                                        ResultOverlay(
                                            result = chartFrame.resultInfo,
                                            playIntro = !resultIntroDone,
                                            introDone = resultIntroDone,
                                            showResultTutorial = showResultTutorial,
                                            onIntroFinished = {
                                                resultIntroDone = true
                                                onResultIntroFinished()
                                            },
                                            onDismissResultTutorial = onDismissResultTutorial,
                                            onShare = onShare,
                                            onRequestAssetRank = onRequestAssetRank,
                                            onPanelIn = onResultPanelIn,
                                            onTypeTick = onResultType,
                                            onStar = onResultStar,
                                            onStamp = onResultStamp,
                                            modifier = Modifier.fillMaxSize()
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            PrimaryActionButton(
                uiState = uiState,
                chartFrame = chartFrame,
                appLayer = appLayer,
                isBonusRun = isBonusRun,
                bonusMult = relicEffects.tenClearBonusMult,
                coolFxTier = relicEffects.coolFx,
                resultIntroDone = resultIntroDone,
                blockStart = showBonusTutorial || startModalOpen,
                blockPlaying = showFirstPlayTutorial || chartFrame.retireLocked,
                blockResult = showResultTutorial || showRelicRewardToast,
                onClick = onPrimaryButtonClick,
                onStartLongPress = { titleSecretInvert = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
            )
        }

        if (edgeFlash) {
            SilentEdgeFlash(modifier = Modifier.fillMaxSize())
        }
        val showPayoutClaim =
            uiState.gameState == GameState.RESULT &&
                chartFrame.resultPayoutClaimed &&
                !payoutAnimDone
        if (showPayoutClaim) {
            val toYen = chartFrame.resultInfo?.finalAsset ?: GameViewModel.INITIAL_ASSET
            val profitOrEven = toYen >= GameViewModel.INITIAL_ASSET
            val retired = chartFrame.resultInfo?.isRetired == true
            PayoutClaimOverlay(
                fromYen = GameViewModel.INITIAL_ASSET,
                toYen = toYen,
                onFinished = { payoutAnimDone = true },
                onCountTick = { p -> onPayoutCountTick(p) },
                onClang = { if (!retired) onPayoutClang(profitOrEven) },
                muted = retired,
                modifier = Modifier.fillMaxSize()
            )
        }
        if (showRelicRewardToast) {
            RelicRewardToast(
                count = newlyPendingRelics.size,
                onDismiss = onDismissRelicRewardToast,
                onAppear = onResultStamp,
                modifier = Modifier.fillMaxSize()
            )
        }
        if (showBonusTutorial) {
            LedgerBonusTutorialOverlay(
                mult = relicEffects.tenClearBonusMult,
                onDismiss = onDismissBonusTutorial,
                modifier = Modifier.fillMaxSize()
            )
        }
        if (showFirstPlayTutorial) {
            FirstPlayTutorialOverlay(
                onDismiss = onDismissFirstPlayTutorial,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

@Composable
private fun BootScanOverlay(modifier: Modifier = Modifier) {
    val ink = Ink
    Canvas(modifier = modifier) {
        var y = 0f
        while (y < size.height) {
            drawLine(
                color = ink.copy(alpha = 0.22f),
                start = Offset(0f, y),
                end = Offset(size.width, y),
                strokeWidth = 2f
            )
            y += 3f
        }
    }
}

/** 音OFF時の約定フィードバック。1pxでは端末で見えないので枠3dp。 */
@Composable
private fun SilentEdgeFlash(modifier: Modifier = Modifier) {
    val ink = Ink
    Canvas(modifier = modifier) {
        val w = 3.dp.toPx()
        drawRect(color = ink.copy(alpha = 0.55f), topLeft = Offset(0f, 0f), size = Size(size.width, w))
        drawRect(color = ink.copy(alpha = 0.55f), topLeft = Offset(0f, size.height - w), size = Size(size.width, w))
        drawRect(color = ink.copy(alpha = 0.55f), topLeft = Offset(0f, 0f), size = Size(w, size.height))
        drawRect(color = ink.copy(alpha = 0.55f), topLeft = Offset(size.width - w, 0f), size = Size(w, size.height))
    }
}

@Composable
private fun ChartsLoadErrorPanel(
    detail: String,
    modifier: Modifier = Modifier
) {
    val lang = LocalDisplayLanguage.current
    Column(
        modifier = modifier
            .lcdBorder()
            .background(LcdBackground)
            .padding(horizontal = 18.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = UiStrings.chartsLoadErrorTitle(lang),
            color = Ink,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = PixelFont
        )
        Text(
            text = UiStrings.chartsLoadErrorBody(detail, lang),
            color = InkMuted,
            fontSize = 12.sp,
            fontFamily = PixelFont,
            lineHeight = 16.sp,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun ContentDisclaimerOverlay(
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val lang = LocalDisplayLanguage.current
    Box(
        modifier = modifier
            .background(LcdBackground.copy(alpha = 0.92f))
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() },
                onClick = {}
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .lcdDoubleBorder()
                .background(LcdBackground)
                .padding(horizontal = 18.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = UiStrings.contentDisclaimerTitle(lang),
                color = Ink,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = PixelFont,
                textAlign = TextAlign.Center
            )
            Text(
                text = UiStrings.contentDisclaimerBody(lang),
                color = InkMuted,
                fontSize = 11.sp,
                fontFamily = PixelFont,
                lineHeight = 16.sp,
                textAlign = TextAlign.Start,
                modifier = Modifier.fillMaxWidth()
            )
            Text(
                text = UiStrings.contentDisclaimerOk(lang),
                color = Ink,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = PixelFont,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .lcdBorder()
                    .uiClickable(kind = UiSoundKind.CONFIRM, onClick = onDismiss)
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            )
        }
    }
}

@Composable
private fun StartAttractPanel(
    smallText: String,
    onFlingShuffle: () -> Unit,
    secretInvert: Boolean,
    onSecretInvert: () -> Unit,
    modifier: Modifier = Modifier
) {
    val lang = LocalDisplayLanguage.current
    val blink = rememberInfiniteTransition(label = "start_cursor")
    val cursorAlpha by blink.animateFloat(
        initialValue = 1f,
        targetValue = 0.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 700, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "start_cursor_alpha"
    )
    val bg = if (secretInvert) Ink else LcdBackground
    val fg = if (secretInvert) LcdBackground else Ink
    val muted = if (secretInvert) LcdBackground.copy(alpha = 0.75f) else InkMuted
    val faint = if (secretInvert) LcdBackground.copy(alpha = 0.45f) else InkFaint

    Box(
        modifier = modifier
            .fillMaxWidth(0.88f)
            .lcdTitleCard(
                onFlingShuffle = onFlingShuffle,
                onLongPress = onSecretInvert
            )
    ) {
        // 通算行・ガイド等の lcdDoubleBorder と差をつける太枠（中身は従来のタイトル構成）
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .border(3.dp, fg, RectangleShape)
                .padding(4.dp)
                .border(1.dp, fg, RectangleShape)
                .background(bg)
                .padding(horizontal = 20.dp, vertical = 18.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = StartScreenCopy.title(),
                color = fg,
                fontSize = 36.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = PixelFont,
                letterSpacing = 3.sp,
                textAlign = TextAlign.Center
            )
            Text(
                text = "─────────",
                color = faint,
                fontSize = 12.sp,
                fontFamily = PixelFont
            )
            Text(
                text = StartScreenCopy.timeLimit(lang),
                color = fg,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = PixelFont,
                letterSpacing = 1.sp,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = smallText,
                color = muted,
                fontSize = 11.sp,
                fontFamily = PixelFont,
                lineHeight = 16.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 4.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "▼",
                color = fg,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = PixelFont,
                modifier = Modifier.alpha(cursorAlpha)
            )
        }
        if (secretInvert) {
            val scanLine = LcdBackground.copy(alpha = 0.22f)
            Canvas(modifier = Modifier.matchParentSize()) {
                var y = 0f
                while (y < size.height) {
                    drawLine(
                        color = scanLine,
                        start = Offset(0f, y),
                        end = Offset(size.width, y),
                        strokeWidth = 1.5f
                    )
                    y += 4f
                }
            }
        }
    }
}

@Composable
private fun LifetimeLedgerStrip(
    lifetimePnl: Int,
    onLongPress: () -> Unit,
    modifier: Modifier = Modifier
) {
    val lang = LocalDisplayLanguage.current
    val amountColor = if (lifetimePnl < 0) InkFaint else Ink
    Text(
        text = UiStrings.lifetimeLedgerLine(lifetimePnl, lang),
        color = amountColor,
        fontSize = 15.sp,
        fontWeight = FontWeight.Bold,
        fontFamily = PixelFont,
        letterSpacing = 1.sp,
        textAlign = TextAlign.Center,
        modifier = modifier
            .fillMaxWidth(0.88f)
            .lcdDoubleBorder()
            .background(LcdBackground)
            .lcdSquish(onLongPress = onLongPress)
            .padding(horizontal = 16.dp, vertical = 10.dp)
    )
}

@Composable
private fun TopHud(
    uiState: GameUiState,
    chartFrame: ChartFrame,
    appLayer: AppLayer,
    hudMode: HudMode,
    blockNav: Boolean = false,
    onOpenScrapbook: () -> Unit,
    onOpenOption: () -> Unit,
    onOpenTutorialGuide: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val lang = LocalDisplayLanguage.current
    var nowMs by remember { mutableLongStateOf(System.currentTimeMillis()) }
    LaunchedEffect(chartFrame.interestTickUntilEpochMs) {
        while (System.currentTimeMillis() < chartFrame.interestTickUntilEpochMs) {
            nowMs = System.currentTimeMillis()
            delay(50)
        }
        nowMs = System.currentTimeMillis()
    }
    val showInterestTick = chartFrame.interestTickAmount > 0 &&
        nowMs < chartFrame.interestTickUntilEpochMs
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (appLayer != AppLayer.PLAY) {
                Text(
                    text = UiStrings.screenTitle(appLayer),
                    color = Ink,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = PixelFont
                )
            } else {
                Spacer(modifier = Modifier.weight(1f))
            }
            if (appLayer == AppLayer.PLAY &&
                uiState.gameState != GameState.PLAYING &&
                uiState.gameState != GameState.FINISHING &&
                !blockNav
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    LcdChromeChip(onClick = onOpenTutorialGuide) {
                        Text(
                            text = UiStrings.tutorialGuideButton(lang),
                            color = Ink,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = PixelFont
                        )
                    }
                    LcdChromeChip(onClick = onOpenScrapbook) {
                        Text(
                            text = UiStrings.scrapButton(lang),
                            color = Ink,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = PixelFont
                        )
                    }
                    LcdChromeChip(onClick = onOpenOption) {
                        Text(
                            text = UiStrings.configButton(lang),
                            color = Ink,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = PixelFont
                        )
                    }
                }
            }
        }
        if (appLayer == AppLayer.PLAY && uiState.gameState != GameState.START) {
            when (uiState.gameState) {
                GameState.RESULT -> ReservedPlayHudSlot(hudMode = hudMode)

                else -> PlayStageHud(
                    uiState = uiState,
                    chartFrame = chartFrame,
                    hudMode = hudMode,
                    showInterestTick = showInterestTick
                )
            }
        }
    }
}

@Composable
private fun ReservedPlayHudSlot(hudMode: HudMode) {
    InvisibleHudMetrics(hudMode = hudMode)
}

@Composable
private fun InvisibleHudMetrics(hudMode: HudMode) {
    val lang = LocalDisplayLanguage.current
    Text(
        text = "${UiStrings.timeLabel(lang)}  00",
        color = Color.Transparent,
        fontSize = 16.sp,
        fontFamily = PixelFont
    )
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "¥0",
            color = Color.Transparent,
            fontSize = 36.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = PixelFont
        )
        Spacer(modifier = Modifier.size(8.dp))
        Box(modifier = Modifier.size(22.dp))
        Spacer(modifier = Modifier.weight(1f))
        Text(
            text = UiStrings.costBasisHud(0, lang),
            color = Color.Transparent,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = PixelFont
        )
    }
    if (hudMode == HudMode.FULL) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "${UiStrings.assetLabel(lang)}  ¥0",
                color = Color.Transparent,
                fontSize = 16.sp,
                fontFamily = PixelFont
            )
            Box(
                modifier = Modifier
                    .height(28.dp)
                    .defaultMinSize(minWidth = 108.dp)
            )
        }
    }
}

@Composable
private fun PlayStageHud(
    uiState: GameUiState,
    chartFrame: ChartFrame,
    hudMode: HudMode,
    showInterestTick: Boolean
) {
    val lang = LocalDisplayLanguage.current
    val finishing = uiState.gameState == GameState.FINISHING
    val pulsePeak = VisibleWindow.timePulsePeak(uiState.currentSecond, finishing)
    val shouldPulseTime = VisibleWindow.shouldPulseTime(uiState.currentSecond, finishing)
    // 終盤〜FINISH: 資産を隠して TIME / カウントへ注目（出金演出までネタバレ防止）
    val assetConcealed = finishing ||
        (uiState.gameState == GameState.PLAYING &&
            uiState.currentSecond >= GameViewModel.ENDGAME_PULSE_FROM_SECOND)
    val timePulse = remember { Animatable(1f) }
    LaunchedEffect(uiState.currentSecond, shouldPulseTime, pulsePeak) {
        if (shouldPulseTime) {
            timePulse.snapTo(pulsePeak)
            timePulse.animateTo(1f, tween(220))
        } else {
            timePulse.snapTo(1f)
        }
    }

    val entryPrice = chartFrame.holdingEntryPrice()
    val mood = entryPrice?.let { holdingPnlMood(uiState.currentPriceDisplay, it) }

    Text(
        text = "${UiStrings.timeLabel(lang)}  %02d".format(uiState.currentSecond),
        color = Ink,
        fontSize = 16.sp,
        fontFamily = PixelFont,
        modifier = Modifier
            .scale(timePulse.value)
            .then(
                if (shouldPulseTime) {
                    Modifier.lcdBorder()
                } else {
                    Modifier
                }
            )
    )
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "¥%,d".format(uiState.currentPriceDisplay),
            color = Ink,
            fontSize = 36.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = PixelFont
        )
        if (entryPrice != null && mood != null) {
            Spacer(modifier = Modifier.size(8.dp))
            HoldingMoodMark(mood = mood)
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = UiStrings.costBasisHud(entryPrice, lang),
                color = InkMuted,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = PixelFont,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
    if (hudMode == HudMode.FULL) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (assetConcealed) {
                    UiStrings.assetConcealed(lang)
                } else {
                    "${UiStrings.assetLabel(lang)}  ¥%,d".format(uiState.totalAsset)
                },
                color = if (assetConcealed) Ink else InkMuted,
                fontSize = 16.sp,
                fontWeight = if (assetConcealed) FontWeight.Bold else FontWeight.Normal,
                fontFamily = PixelFont
            )
            Box(
                modifier = Modifier
                    .height(28.dp)
                    .defaultMinSize(minWidth = 108.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                // 常に同じ占有サイズ。表示時だけ不透明にしてチャート領域のガクつきを防ぐ
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.alpha(
                        if (showInterestTick && !assetConcealed) 1f else 0f
                    )
                ) {
                    Text(
                        text = "+¥%,d".format(
                            chartFrame.interestTickAmount.coerceAtLeast(1)
                        ),
                        color = Ink,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = PixelFont,
                        modifier = Modifier
                            .lcdBorder()
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                    Text(
                        text = UiStrings.ledgerBonusMultLabel(
                            chartFrame.interestTickMult.coerceAtLeast(1.5f)
                        ),
                        color = LcdInvertedText,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = PixelFont,
                        modifier = Modifier
                            .padding(start = 4.dp)
                            .alpha(
                                if (showInterestTick &&
                                    !assetConcealed &&
                                    chartFrame.interestTickMult > 1.01f
                                ) {
                                    1f
                                } else {
                                    0f
                                }
                            )
                            .background(Ink)
                            .padding(horizontal = 4.dp, vertical = 2.dp)
                    )
                }
            }
        }
    }
}

/**
 * 含み ±5% 超の心情マーク。数字は出さず、LCD の三角／横線だけで伝える。
 * UP: 塗りつぶし▲＋軽い鼓動 / DOWN: 枠▼＋青ざめ点滅 / FLAT: 短い横線
 */
@Composable
private fun HoldingMoodMark(
    mood: HoldingPnlMood,
    modifier: Modifier = Modifier
) {
    val ink = Ink
    val inkMuted = InkMuted
    val inkFaint = InkFaint
    val pulse = rememberInfiniteTransition(label = "holding_mood")
    val upBeat by pulse.animateFloat(
        initialValue = 0.92f,
        targetValue = 1.14f,
        animationSpec = infiniteRepeatable(
            animation = tween(380, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "holding_up_beat"
    )
    val downFade by pulse.animateFloat(
        initialValue = 0.38f,
        targetValue = 0.92f,
        animationSpec = infiniteRepeatable(
            animation = tween(720, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "holding_down_fade"
    )

    val scale = when (mood) {
        HoldingPnlMood.UP -> upBeat
        HoldingPnlMood.DOWN -> 0.96f
        HoldingPnlMood.FLAT -> 1f
    }
    val alpha = when (mood) {
        HoldingPnlMood.UP -> 1f
        HoldingPnlMood.DOWN -> downFade
        HoldingPnlMood.FLAT -> 0.7f
    }

    Canvas(
        modifier = modifier
            .size(22.dp)
            .scale(scale)
            .alpha(alpha)
    ) {
        val w = size.width
        val h = size.height
        when (mood) {
            HoldingPnlMood.UP -> {
                val path = Path().apply {
                    moveTo(w * 0.5f, h * 0.12f)
                    lineTo(w * 0.08f, h * 0.88f)
                    lineTo(w * 0.92f, h * 0.88f)
                    close()
                }
                drawPath(path, color = ink)
            }
            HoldingPnlMood.DOWN -> {
                val path = Path().apply {
                    moveTo(w * 0.5f, h * 0.88f)
                    lineTo(w * 0.08f, h * 0.12f)
                    lineTo(w * 0.92f, h * 0.12f)
                    close()
                }
                drawPath(
                    path = path,
                    color = inkMuted,
                    style = Stroke(width = 2.4f, cap = StrokeCap.Square)
                )
            }
            HoldingPnlMood.FLAT -> {
                drawLine(
                    color = inkFaint,
                    start = Offset(w * 0.18f, h * 0.5f),
                    end = Offset(w * 0.82f, h * 0.5f),
                    strokeWidth = 3f,
                    cap = StrokeCap.Square
                )
            }
        }
    }
}

@Composable
private fun ResultOverlay(
    result: ResultInfo?,
    playIntro: Boolean,
    introDone: Boolean,
    showResultTutorial: Boolean = false,
    onIntroFinished: () -> Unit,
    onDismissResultTutorial: () -> Unit = {},
    onShare: () -> Unit,
    onRequestAssetRank: () -> AssetRankBoard? = { null },
    onPanelIn: () -> Unit = {},
    onTypeTick: () -> Unit = {},
    onStar: (index: Int, total: Int) -> Unit = { _, _ -> },
    onStamp: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    if (result == null) return
    val lang = LocalDisplayLanguage.current
    val scope = rememberCoroutineScope()
    var showAssetRank by remember { mutableStateOf(false) }
    var assetRankBoard by remember { mutableStateOf<AssetRankBoard?>(null) }
    var showSyncBreakdown by remember { mutableStateOf(false) }
    var resultTutStep by remember { mutableIntStateOf(0) }
    val resultTutSteps = 5
    LaunchedEffect(showResultTutorial) {
        if (showResultTutorial) resultTutStep = 0
    }
    val assetPressScale = remember { Animatable(1f) }
    val starCount = if (result.isRetired) {
        0
    } else {
        SyncRating.stars(result.finalAsset, result.theoreticalMaxAsset)
    }
    val overSync = !result.isRetired &&
        SyncRating.isOverSync(result.finalAsset, result.theoreticalMaxAsset)
    val uiSound = rememberUiSound()
    val panelChrome = when {
        result.isRetired -> Modifier.lcdBorder()
        overSync || starCount >= 5 -> Modifier.lcdDoubleBorder().lcdHatch()
        starCount >= 4 -> Modifier.lcdDoubleBorder()
        else -> Modifier.lcdBorder()
    }

    val panelAlpha = remember(result.brandName, result.isRetired) {
        Animatable(if (playIntro) 0f else 1f)
    }
    val animatedSync = remember(result.brandName, result.isRetired) {
        Animatable(if (playIntro) 0f else result.syncPercent)
    }
    val animatedAsset = remember(result.brandName, result.isRetired) {
        Animatable(
            if (playIntro) GameViewModel.INITIAL_ASSET.toFloat()
            else result.finalAsset.toFloat()
        )
    }
    val newBadgeScale = remember(result.brandName) {
        Animatable(if (playIntro) 0.6f else 1f)
    }
    val newBadgeAlpha = remember(result.brandName) {
        Animatable(if (playIntro && result.isFirstClear) 0f else if (result.isFirstClear) 1f else 0f)
    }
    val bestStampScale = remember(result.brandName) {
        Animatable(if (playIntro) 1.6f else 1f)
    }
    val bestStampAlpha = remember(result.brandName) {
        Animatable(if (playIntro) 0f else if (result.isNewBest) 1f else 0f)
    }
    val overStampScale = remember(result.brandName) {
        Animatable(if (playIntro) 1.7f else 1f)
    }
    val overStampAlpha = remember(result.brandName) {
        Animatable(
            if (playIntro) 0f
            else if (SyncRating.isOverSync(result.finalAsset, result.theoreticalMaxAsset)) 1f
            else 0f
        )
    }
    var typedName by remember(result.brandName, result.isRetired) {
        mutableStateOf(
            when {
                result.isRetired -> UiStrings.retireBanner()
                playIntro && result.isFirstClear -> ""
                else -> result.brandName
            }
        )
    }
    LaunchedEffect(result.brandName, result.syncPercent, result.finalAsset, playIntro, result.isRetired) {
        if (!playIntro) {
            panelAlpha.snapTo(1f)
            animatedSync.snapTo(result.syncPercent)
            animatedAsset.snapTo(result.finalAsset.toFloat())
            typedName = if (result.isRetired) UiStrings.retireBanner() else result.brandName
            if (result.isFirstClear && !result.isRetired) {
                newBadgeAlpha.snapTo(1f)
                newBadgeScale.snapTo(1f)
            }
            if (result.isNewBest && !result.isRetired) {
                bestStampAlpha.snapTo(1f)
                bestStampScale.snapTo(1f)
            }
            if (SyncRating.isOverSync(result.finalAsset, result.theoreticalMaxAsset)) {
                overStampAlpha.snapTo(1f)
                overStampScale.snapTo(1f)
            }
            return@LaunchedEffect
        }
        panelAlpha.snapTo(0f)
        animatedSync.snapTo(0f)
        animatedAsset.snapTo(GameViewModel.INITIAL_ASSET.toFloat())
        newBadgeScale.snapTo(0.6f)
        newBadgeAlpha.snapTo(0f)
        bestStampScale.snapTo(1.6f)
        bestStampAlpha.snapTo(0f)
        overStampScale.snapTo(1.7f)
        overStampAlpha.snapTo(0f)
        panelAlpha.animateTo(1f, tween(350))
        if (result.isRetired) {
            typedName = UiStrings.retireBanner()
            animatedSync.snapTo(0f)
            animatedAsset.snapTo(result.finalAsset.toFloat())
            onIntroFinished()
            return@LaunchedEffect
        }
        onPanelIn()
        if (result.isFirstClear) {
            typedName = ""
            newBadgeAlpha.animateTo(1f, tween(80))
            newBadgeScale.animateTo(1.2f, tween(120, easing = FastOutSlowInEasing))
            newBadgeScale.animateTo(1f, tween(100))
            for (i in 1..result.brandName.length) {
                typedName = result.brandName.take(i)
                onTypeTick()
                delay(28L)
            }
            repeat(2) {
                newBadgeAlpha.animateTo(0.45f, tween(120))
                newBadgeAlpha.animateTo(1f, tween(120))
            }
        } else {
            typedName = result.brandName
        }
        // 見た目のパンチは★帯（反転）。資産は直後の一行で簡潔に
        val targetStars = SyncRating.stars(result.finalAsset, result.theoreticalMaxAsset)
        var lastStars = 0
        animatedSync.animateTo(result.syncPercent, tween(RESULT_SYNC_ANIM_MS)) {
            val s = SyncRating.starsForSync(value, result.theoreticalMaxAsset)
            if (s > lastStars) {
                for (i in lastStars until s) {
                    onStar(i, targetStars)
                }
                lastStars = s
            }
        }
        animatedAsset.animateTo(
            result.finalAsset.toFloat(),
            tween(RESULT_ASSET_ANIM_MS, easing = FastOutSlowInEasing),
        )
        if (result.isNewBest) {
            delay(80)
            bestStampAlpha.animateTo(1f, tween(50))
            onStamp()
            bestStampScale.animateTo(0.92f, tween(110, easing = FastOutSlowInEasing))
            bestStampScale.animateTo(1f, tween(90))
        }
        if (SyncRating.isOverSync(result.finalAsset, result.theoreticalMaxAsset)) {
            delay(90)
            overStampAlpha.animateTo(1f, tween(60))
            onStamp()
            overStampScale.animateTo(1.18f, tween(120, easing = FastOutSlowInEasing))
            overStampScale.animateTo(0.96f, tween(90))
            overStampScale.animateTo(1f, tween(90))
            delay(220)
        }
        onIntroFinished()
    }

    val showHalfDot = !result.isRetired &&
        result.isHalfSync &&
        !result.isGhostRun &&
        kotlin.math.abs(animatedSync.value - 50f) < 0.2f
    val flavor = when {
        result.isRetired -> ""
        result.isBreakEven -> UiStrings.resultFlavorBreakEven(lang)
        else -> ""
    }
    val ratingSuffix = when {
        result.isRetired -> "☆☆☆☆☆"
        result.isGhostRun -> UiStrings.resultFlavorGhost(lang)
        else -> SyncRating.starsTextForSync(
            animatedSync.value,
            result.theoreticalMaxAsset,
            halfMark = showHalfDot,
        )
    }
    val netPnl = result.finalAsset - GameViewModel.INITIAL_ASSET
    val pnlBlink = rememberInfiniteTransition(label = "result_pnl_blink")
    val pnlAlpha by pnlBlink.animateFloat(
        initialValue = 1f,
        targetValue = 0.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "result_pnl_alpha"
    )

    Box(modifier = modifier) {
        Column(
            modifier = Modifier.align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (showResultTutorial && introDone) {
                ResultTutorialCard(
                    step = resultTutStep,
                    totalSteps = resultTutSteps,
                    onNext = {
                        if (resultTutStep < resultTutSteps - 1) {
                            resultTutStep += 1
                        } else {
                            onDismissResultTutorial()
                        }
                    }
                )
            }
            Column(
                modifier = Modifier
                    .fillMaxWidth(0.92f)
                    .padding(horizontal = 4.dp)
                    .then(panelChrome)
                    .background(LcdBackground.copy(alpha = 0.94f * panelAlpha.value))
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (result.isFirstClear && !result.isRetired) {
                Text(
                    text = UiStrings.newBadge(),
                    color = Ink,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = PixelFont,
                    letterSpacing = 1.sp,
                    modifier = Modifier
                        .padding(end = 8.dp)
                        .alpha(newBadgeAlpha.value)
                        .graphicsLayer {
                            scaleX = newBadgeScale.value
                            scaleY = newBadgeScale.value
                        }
                        .lcdBorder()
                        .padding(horizontal = 5.dp, vertical = 2.dp)
                )
            }
            Text(
                text = typedName.ifEmpty { " " },
                color = Ink,
                fontSize = if (result.isRetired) 18.sp else 15.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = PixelFont,
                textAlign = TextAlign.Center
            )
        }
        if (result.isRetired) {
            Text(
                text = UiStrings.retireFlavorLine1(result.retireFlavorIndex, lang),
                color = Ink,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = PixelFont,
                textAlign = TextAlign.Center
            )
            Text(
                text = UiStrings.retireFlavorLine2(result.retireFlavorIndex, lang),
                color = InkMuted,
                fontSize = 11.sp,
                fontFamily = PixelFont,
                textAlign = TextAlign.Center,
                lineHeight = 15.sp
            )
        } else {
            if (flavor.isNotBlank()) {
                Text(
                    text = flavor,
                    color = Ink,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = PixelFont,
                    letterSpacing = 1.sp
                )
            }
            Text(
                text = UiStrings.instrumentWithCalendarRange(
                    result.instrumentName,
                    result.calendarStart,
                    result.calendarEnd,
                    lang
                ),
                color = InkMuted,
                fontSize = 12.sp,
                fontFamily = PixelFont
            )
        }
        if (!result.isRetired) {
            TutorialSpotlight(active = showResultTutorial && introDone && resultTutStep == 1) {
                // 反転パネルは％＋★のみ（以前のパンチを復元）。縦を食わない一塊。
                Box {
                    LcdInvertedPanel(
                        modifier = Modifier.uiClickable(
                            kind = UiSoundKind.CONFIRM,
                            enabled = introDone && !showResultTutorial,
                            onClick = { showSyncBreakdown = true }
                        )
                    ) {
                        Text(
                            text = if (result.isGhostRun) {
                                ratingSuffix
                            } else {
                                "%.1f%%  %s".format(animatedSync.value, ratingSuffix)
                            },
                            color = LcdInvertedText,
                            fontSize = if (result.isGhostRun) 16.sp else 28.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = PixelFont,
                            textAlign = TextAlign.Center
                        )
                    }
                    if (result.isNewBest) {
                        Text(
                            text = UiStrings.bestStamp(lang),
                            color = Ink,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = PixelFont,
                            letterSpacing = 1.sp,
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .offset(x = 10.dp, y = (-10).dp)
                                .alpha(bestStampAlpha.value)
                                .graphicsLayer {
                                    scaleX = bestStampScale.value
                                    scaleY = bestStampScale.value
                                    rotationZ = -12f
                                }
                                .background(LcdBackground)
                                .lcdBorder(width = 2.dp)
                                .padding(horizontal = 5.dp, vertical = 2.dp)
                        )
                    }
                    if (overSync) {
                        Text(
                            text = UiStrings.overSyncStamp(lang),
                            color = LcdInvertedText,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = PixelFont,
                            letterSpacing = 2.sp,
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .offset(x = (-6).dp, y = 10.dp)
                                .alpha(overStampAlpha.value)
                                .graphicsLayer {
                                    scaleX = overStampScale.value
                                    scaleY = overStampScale.value
                                    rotationZ = -8f
                                }
                                .background(Ink)
                                .lcdBorder(LcdInvertedText, width = 2.dp)
                                .padding(horizontal = 7.dp, vertical = 3.dp)
                        )
                    }
                }
            }
        }
        TutorialSpotlight(active = showResultTutorial && introDone && resultTutStep == 2) {
            // 資産は二重枠の一行。反転を重ねない
            val chaseHint = remember(
                result.finalAsset,
                result.theoreticalMaxAsset,
                result.isRetired,
                result.priorBestAsset
            ) {
                ResultChaseHints.resolve(
                    finalAsset = result.finalAsset,
                    theoreticalMax = result.theoreticalMaxAsset,
                    isRetired = result.isRetired,
                    priorBestAsset = result.priorBestAsset,
                )
            }
            val chaseText = when (val h = chaseHint) {
                is ResultChaseHint.StarGap ->
                    UiStrings.chaseHintStar(h.nextStars, h.yenLeft, lang)
                is ResultChaseHint.BestGap ->
                    UiStrings.chaseHintBest(h.yenLeft, lang)
                null -> null
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(modifier = Modifier.weight(1f))
                Box {
                    Text(
                        text = "${UiStrings.assetLabel(lang)}  ¥%,d".format(
                            animatedAsset.value.roundToInt()
                        ),
                        color = Ink,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = PixelFont,
                        modifier = Modifier
                            .scale(assetPressScale.value)
                            .lcdDoubleBorder()
                            .clickable(
                                enabled = introDone && !showResultTutorial,
                                indication = null,
                                interactionSource = remember { MutableInteractionSource() }
                            ) {
                                if (introDone && !showResultTutorial) {
                                    uiSound.play(UiSoundKind.CONFIRM)
                                }
                                scope.launch {
                                    assetPressScale.snapTo(0.90f)
                                    assetPressScale.animateTo(
                                        1f,
                                        tween(140, easing = FastOutSlowInEasing)
                                    )
                                    assetRankBoard = onRequestAssetRank()
                                    showAssetRank = assetRankBoard != null
                                }
                            }
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                    if (chaseText != null && introDone && !showResultTutorial) {
                        Text(
                            text = chaseText,
                            color = Ink,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = PixelFont,
                            maxLines = 1,
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .offset(x = 6.dp, y = (-12).dp)
                                .background(LcdBackground)
                                .lcdBorder(width = 2.dp)
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
                Box(
                    modifier = Modifier.weight(1f),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Text(
                        text = UiStrings.formatSignedYen(netPnl),
                        color = InkMuted,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = PixelFont,
                        modifier = Modifier
                            .padding(start = 8.dp)
                            .alpha(if (introDone) pnlAlpha else 1f)
                    )
                }
            }
        }
        val showTradeInterestRow =
            result.filledTradeCount > 0 ||
                result.totalInterest > 0 ||
                (showResultTutorial && introDone)
        if (showTradeInterestRow) {
            TutorialSpotlight(active = showResultTutorial && introDone && resultTutStep == 3) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 2.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val showTrade =
                        result.filledTradeCount > 0 || (showResultTutorial && introDone)
                    val showInterest =
                        result.totalInterest > 0 || (showResultTutorial && introDone)
                    if (showTrade) {
                        val tradeHighlight =
                            result.ledgerBoostApplied || result.tradePnlBoostApplied
                        Row(verticalAlignment = Alignment.Bottom) {
                            Column(modifier = Modifier.width(IntrinsicSize.Max)) {
                                Text(
                                    text = UiStrings.formatTradeGainLoss(result.tradeGainLoss, lang),
                                    color = if (tradeHighlight) Ink else InkMuted,
                                    fontSize = if (tradeHighlight) 13.sp else 11.sp,
                                    fontWeight = if (tradeHighlight) {
                                        FontWeight.Bold
                                    } else {
                                        FontWeight.Normal
                                    },
                                    fontFamily = PixelFont
                                )
                                if (tradeHighlight) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(2.dp)
                                            .background(Ink)
                                    )
                                }
                            }
                            if (result.tradePnlBoostApplied) {
                                Text(
                                    text = UiStrings.ledgerBonusMultLabel(result.tradePnlBoostMult),
                                    color = LcdInvertedText,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = PixelFont,
                                    modifier = Modifier
                                        .padding(start = 6.dp)
                                        .background(Ink)
                                        .padding(horizontal = 5.dp, vertical = 2.dp)
                                )
                            }
                            if (result.ledgerBoostApplied) {
                                Text(
                                    text = UiStrings.ledgerBonusMultLabel(result.ledgerBoostMult),
                                    color = LcdInvertedText,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = PixelFont,
                                    modifier = Modifier
                                        .padding(start = 6.dp)
                                        .background(Ink)
                                        .padding(horizontal = 5.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                    if (showTrade && showInterest) {
                        Spacer(modifier = Modifier.width(16.dp))
                    }
                    if (showInterest) {
                        Row(verticalAlignment = Alignment.Bottom) {
                            Text(
                                text = "${UiStrings.interestLabel(lang)} +¥%,d".format(result.totalInterest),
                                color = if (result.interestBoostApplied) Ink else InkMuted,
                                fontSize = if (result.interestBoostApplied) 13.sp else 11.sp,
                                fontWeight = if (result.interestBoostApplied) {
                                    FontWeight.Bold
                                } else {
                                    FontWeight.Normal
                                },
                                fontFamily = PixelFont
                            )
                            if (result.interestBoostApplied) {
                                Text(
                                    text = UiStrings.ledgerBonusMultLabel(result.interestBoostMult),
                                    color = LcdInvertedText,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = PixelFont,
                                    modifier = Modifier
                                        .padding(start = 6.dp)
                                        .background(Ink)
                                        .padding(horizontal = 5.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
        if (result.maxCombo > 0) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.padding(top = 6.dp)
            ) {
                Text(
                    text = UiStrings.maxComboLabel(lang),
                    color = InkMuted,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = PixelFont,
                    letterSpacing = 2.sp
                )
                Text(
                    text = result.maxCombo.toString(),
                    color = InkMuted,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = PixelFont
                )
            }
        }
        Text(
            text = UiStrings.shareButton(lang),
            color = Ink,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = PixelFont,
            modifier = Modifier
                .lcdBorder()
                .uiClickable(
                    kind = UiSoundKind.CONFIRM,
                    enabled = introDone && !showResultTutorial,
                    onClick = onShare
                )
                .padding(horizontal = 14.dp, vertical = 5.dp)
        )
            }
        }
        if (showSyncBreakdown) {
            SyncRatingBreakdownOverlay(
                finalAsset = result.finalAsset,
                theoreticalMax = result.theoreticalMaxAsset,
                syncPercent = result.syncPercent,
                onDismiss = { showSyncBreakdown = false },
                modifier = Modifier.fillMaxSize()
            )
        }
        if (showAssetRank) {
            assetRankBoard?.let { board ->
                AssetRankOverlay(
                    board = board,
                    onDismiss = { showAssetRank = false },
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}

@Composable
private fun PrimaryActionButton(
    uiState: GameUiState,
    chartFrame: ChartFrame,
    appLayer: AppLayer,
    isBonusRun: Boolean,
    bonusMult: Float,
    coolFxTier: com.urikai.app.game.CoolFxTier = com.urikai.app.game.CoolFxTier.NONE,
    resultIntroDone: Boolean = false,
    blockStart: Boolean = false,
    blockPlaying: Boolean = false,
    blockResult: Boolean = false,
    onClick: () -> Unit,
    onStartLongPress: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val lang = LocalDisplayLanguage.current
    var cashOutReady by remember { mutableStateOf(false) }
    var tallyDots by remember { mutableIntStateOf(1) }
    var nowMs by remember { mutableLongStateOf(System.currentTimeMillis()) }
    val isPlaying = uiState.gameState == GameState.PLAYING
    val isFinishing = uiState.gameState == GameState.FINISHING
    val isStartScreen =
        appLayer == AppLayer.PLAY && uiState.gameState == GameState.START
    val showStartBonus = isStartScreen && isBonusRun
    val startBlinkMs = if (showStartBonus) 520 else 900
    val startBlink = rememberInfiniteTransition(label = "start_btn_blink_$startBlinkMs")
    val startBlinkAlpha by startBlink.animateFloat(
        initialValue = 1f,
        targetValue = 0.55f,
        animationSpec = infiniteRepeatable(
            animation = tween(startBlinkMs, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "start_btn_alpha_$startBlinkMs"
    )

    LaunchedEffect(uiState.gameState) {
        if (uiState.gameState != GameState.FINISHING) {
            cashOutReady = false
            tallyDots = 1
            return@LaunchedEffect
        }
        cashOutReady = false
        tallyDots = 1
        val deadline = System.currentTimeMillis() + FINISH_TALLY_MS
        while (System.currentTimeMillis() < deadline) {
            delay(FINISH_TALLY_DOT_MS)
            tallyDots = (tallyDots % 3) + 1
        }
        cashOutReady = true
    }

    LaunchedEffect(isPlaying, chartFrame.orderReadyAtEpochMs, chartFrame.orderCooldownStartedAtEpochMs) {
        if (!isPlaying) return@LaunchedEffect
        while (true) {
            nowMs = System.currentTimeMillis()
            delay(16L)
        }
    }

    val orderReady = !isPlaying || OrderCharge.isReady(nowMs, chartFrame.orderReadyAtEpochMs)
    val isCharging = isPlaying && !uiState.isOrderProcessing && !orderReady
    val chargeProgress = if (isCharging) {
        OrderCharge.progress(
            nowEpochMs = nowMs,
            startedAtEpochMs = chartFrame.orderCooldownStartedAtEpochMs,
            readyAtEpochMs = chartFrame.orderReadyAtEpochMs,
        )
    } else {
        1f
    }

    val readyPop = remember { Animatable(1f) }
    var wasCharging by remember { mutableStateOf(false) }
    LaunchedEffect(isCharging, isPlaying, coolFxTier) {
        if (wasCharging && !isCharging && isPlaying) {
            val peak = when (coolFxTier) {
                com.urikai.app.game.CoolFxTier.II -> 1.10f
                com.urikai.app.game.CoolFxTier.I -> 1.07f
                com.urikai.app.game.CoolFxTier.NONE -> 1.04f
            }
            readyPop.snapTo(peak)
            readyPop.animateTo(1f, tween(GameViewModel.CHARGE_READY_POP_MS.toInt()))
        }
        wasCharging = isCharging
    }

    val cashOutPop = remember { Animatable(1f) }
    LaunchedEffect(cashOutReady) {
        if (cashOutReady && isFinishing) {
            cashOutPop.snapTo(0.90f)
            cashOutPop.animateTo(1.12f, tween(110, easing = FastOutSlowInEasing))
            cashOutPop.animateTo(1f, tween(150))
        } else {
            cashOutPop.snapTo(1f)
        }
    }

    val isTallying = isFinishing && !cashOutReady
    val awaitingRetryGate =
        uiState.gameState == GameState.RESULT &&
            chartFrame.resultPayoutClaimed &&
            !resultIntroDone

    val enabled = when {
        blockStart && uiState.gameState == GameState.START -> false
        blockPlaying && uiState.gameState == GameState.PLAYING -> false
        blockResult && uiState.gameState == GameState.RESULT -> false
        awaitingRetryGate || isTallying -> false
        isFinishing -> cashOutReady
        appLayer == AppLayer.SCRAPBOOK || appLayer == AppLayer.OPTION -> true
        uiState.gameState == GameState.PLAYING ->
            uiState.isOrderProcessing || OrderCharge.isReady(nowMs, chartFrame.orderReadyAtEpochMs)
        else -> true
    }

    val label = when {
        isTallying -> UiStrings.tallying(lang, tallyDots)
        isFinishing -> UiStrings.claim(lang)
        appLayer == AppLayer.SCRAPBOOK || appLayer == AppLayer.OPTION -> UiStrings.back(lang)
        uiState.gameState == GameState.START -> UiStrings.start(lang)
        uiState.gameState == GameState.RESULT && !chartFrame.resultPayoutClaimed ->
            UiStrings.claim(lang)
        uiState.gameState == GameState.RESULT -> UiStrings.retry(lang)
        uiState.isOrderProcessing -> UiStrings.orderProcessing(lang)
        uiState.hasStock -> UiStrings.sell(lang)
        else -> UiStrings.buy(lang)
    }

    val showHatch = awaitingRetryGate || isTallying ||
        (blockPlaying && uiState.gameState == GameState.PLAYING) ||
        (blockResult && uiState.gameState == GameState.RESULT) ||
        (uiState.gameState == GameState.PLAYING && (uiState.isOrderProcessing || isCharging))

    Box(modifier = modifier.scale(readyPop.value * cashOutPop.value)) {
        Button(
            onClick = onClick,
            enabled = enabled,
            shape = RectangleShape,
            colors = ButtonDefaults.buttonColors(
                containerColor = if (showStartBonus) Ink else LcdButtonFill,
                contentColor = if (showStartBonus) LcdInvertedText else LcdButtonLabel,
                disabledContainerColor = LcdBackground,
                disabledContentColor = InkMuted,
            ),
            modifier = Modifier
                .fillMaxSize()
                .then(
                    if (showStartBonus) {
                        Modifier.lcdDoubleBorder()
                    } else {
                        Modifier.lcdBorder(width = 2.dp)
                    }
                )
                .then(
                    if (isStartScreen) {
                        Modifier.alpha(startBlinkAlpha)
                    } else {
                        Modifier
                    }
                )
                .then(
                    if (isStartScreen) {
                        Modifier.pointerInput(Unit) {
                            detectTapGestures(onLongPress = { onStartLongPress() })
                        }
                    } else {
                        Modifier
                    }
                )
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                if (showStartBonus) {
                    Text(
                        text = "◆ ${UiStrings.ledgerBonusStartCaption(bonusMult, lang)} ◆",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = PixelFont,
                        letterSpacing = 1.sp,
                        color = LcdInvertedText,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .padding(top = 10.dp, start = 8.dp, end = 8.dp)
                    )
                }
                Text(
                    text = label,
                    fontSize = if (awaitingRetryGate || isTallying) 28.sp else 36.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = PixelFont,
                    letterSpacing = if (awaitingRetryGate || isTallying) 2.sp else 4.sp
                )
            }
        }
        if (showHatch) {
            LcdHatchOverlay(modifier = Modifier.matchParentSize())
        }
        if (showStartBonus) {
            LcdHatchOverlay(modifier = Modifier.matchParentSize().alpha(0.22f))
        }
        if (isCharging) {
            OrderChargeBottomBar(
                progress = chargeProgress,
                fxTier = coolFxTier,
                modifier = Modifier.matchParentSize(),
            )
        }
    }
}
