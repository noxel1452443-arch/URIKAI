package com.urikai.app

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.urikai.app.game.GameViewModel
import com.urikai.app.game.UiStrings
import com.urikai.app.ui.GameScreen
import com.urikai.app.ui.theme.UriKaiTheme

class MainActivity : ComponentActivity() {

    private val viewModel: GameViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val lcdTheme by viewModel.lcdTheme.collectAsStateWithLifecycle()
            val displayLanguage by viewModel.displayLanguage.collectAsStateWithLifecycle()
            val chartLineStyle by viewModel.chartLineStyle.collectAsStateWithLifecycle()
            val scanlineStrength by viewModel.scanlineStrength.collectAsStateWithLifecycle()
            val panelContrast by viewModel.panelContrast.collectAsStateWithLifecycle()
            val surfaceTone by viewModel.surfaceTone.collectAsStateWithLifecycle()

            UriKaiTheme(
                lcdTheme = lcdTheme,
                displayLanguage = displayLanguage,
                chartLineStyle = chartLineStyle,
                scanlineStrength = scanlineStrength,
                panelContrast = panelContrast,
                surfaceTone = surfaceTone
            ) {
                val uiState by viewModel.uiState.collectAsStateWithLifecycle()
                val chartFrame by viewModel.chartFrame.collectAsStateWithLifecycle()
                val appLayer by viewModel.appLayer.collectAsStateWithLifecycle()
                val scrapbookEntries by viewModel.scrapbookEntries.collectAsStateWithLifecycle()
                val hudMode by viewModel.hudMode.collectAsStateWithLifecycle()
                val scrapbookSort by viewModel.scrapbookSort.collectAsStateWithLifecycle()
                val startSmallText by viewModel.startSmallText.collectAsStateWithLifecycle()
                val lifetimePnl by viewModel.lifetimePnl.collectAsStateWithLifecycle()
                val sessionPnlHistory by viewModel.sessionPnlHistory.collectAsStateWithLifecycle()
                val cashOutCount by viewModel.cashOutCount.collectAsStateWithLifecycle()
                val seenTutorialGuides by viewModel.seenTutorialGuides.collectAsStateWithLifecycle()
                val pendingRelics by viewModel.pendingRelics.collectAsStateWithLifecycle()
                val newlyPendingRelics by viewModel.newlyPendingRelics.collectAsStateWithLifecycle()
                val claimedRelics by viewModel.claimedRelics.collectAsStateWithLifecycle()
                val relicEffects by viewModel.relicEffects.collectAsStateWithLifecycle()
                val relicStagePrefs by viewModel.relicStagePrefs.collectAsStateWithLifecycle()
                val isBonusRun by viewModel.isBonusRun.collectAsStateWithLifecycle()
                val showBonusTutorial by viewModel.showBonusTutorial.collectAsStateWithLifecycle()
                val showFirstPlayTutorial by viewModel.showFirstPlayTutorial.collectAsStateWithLifecycle()
                val showResultTutorial by viewModel.showResultTutorial.collectAsStateWithLifecycle()
                val showContentDisclaimer by viewModel.showContentDisclaimer.collectAsStateWithLifecycle()
                val seLevel by viewModel.seLevel.collectAsStateWithLifecycle()
                val bgmLevel by viewModel.bgmLevel.collectAsStateWithLifecycle()
                val hapticEnabled by viewModel.hapticEnabled.collectAsStateWithLifecycle()
                val chartsLoadError by viewModel.chartsLoadError.collectAsStateWithLifecycle()
                val devRelicLab by viewModel.devRelicLab.collectAsStateWithLifecycle()

                GameScreen(
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
                    onDismissContentDisclaimer = viewModel::dismissContentDisclaimer,
                    onDismissRelicRewardToast = viewModel::dismissRelicRewardToast,
                    onPrimaryButtonClick = viewModel::onPrimaryButtonClick,
                    onOpenScrapbook = viewModel::openScrapbook,
                    onOpenOption = viewModel::openOption,
                    onHudModeChange = viewModel::setHudMode,
                    onLcdThemeChange = viewModel::setLcdTheme,
                    onDisplayLanguageChange = viewModel::setDisplayLanguage,
                    onChartLineStyleChange = viewModel::setChartLineStyle,
                    onScanlineStrengthChange = viewModel::setScanlineStrength,
                    onPanelContrastChange = viewModel::setPanelContrast,
                    onSurfaceToneChange = viewModel::setSurfaceTone,
                    onSeLevelChange = viewModel::setSeLevel,
                    onBgmLevelChange = viewModel::setBgmLevel,
                    onHapticEnabledChange = viewModel::setHapticEnabled,
                    onScrapbookSortChange = viewModel::setScrapbookSort,
                    onShare = ::shareResult,
                    onShuffleStartTip = viewModel::shuffleStartAttractTip,
                    onClaimRelic = viewModel::claimPendingRelic,
                    onToggleRelicVisual = viewModel::toggleRelicVisual,
                    onCycleRelicStage = viewModel::cycleRelicStage,
                    relicStagePrefs = relicStagePrefs,
                    onDismissBonusTutorial = viewModel::dismissBonusTutorial,
                    onDismissFirstPlayTutorial = viewModel::dismissFirstPlayTutorial,
                    onResultIntroFinished = viewModel::onResultIntroFinished,
                    onDismissResultTutorial = viewModel::dismissResultTutorial,
                    onRequestAssetRank = viewModel::assetRankBoardForCurrentResult,
                    devRelicLab = devRelicLab,
                    onEnableDevRelicLab = viewModel::enableDevRelicLab,
                    onDisableDevRelicLab = viewModel::disableDevRelicLab,
                    onSetDevRelicEnabled = viewModel::setDevRelicEnabled,
                    onPayoutCountTick = { p ->
                        val profit = (viewModel.chartFrame.value.resultInfo?.finalAsset
                            ?: GameViewModel.INITIAL_ASSET) >= GameViewModel.INITIAL_ASSET
                        viewModel.cuePayoutCount(p, profit)
                    },
                    onPayoutClang = viewModel::cuePayoutClang,
                    onResultPanelIn = viewModel::cueResultPanelIn,
                    onResultType = viewModel::cueResultType,
                    onResultStar = viewModel::cueResultStar,
                    onResultStamp = viewModel::cueResultStamp,
                    onUiConfirm = viewModel::cueUiConfirm,
                    onUiSelect = viewModel::cueUiSelect
                )
            }
        }
    }

    private fun shareResult() {
        val text = viewModel.buildShareText() ?: return
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, text)
        }
        val title = UiStrings.shareChooserTitle(viewModel.displayLanguage.value)
        startActivity(Intent.createChooser(intent, title))
    }

    override fun onStart() {
        super.onStart()
        viewModel.setAppForeground(true)
    }

    override fun onStop() {
        viewModel.setAppForeground(false)
        super.onStop()
    }
}
