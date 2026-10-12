package com.urikai.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.compose.LifecycleStartEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.urikai.app.game.GameViewModel
import com.urikai.app.game.UiStrings
import com.urikai.app.platform.PlatformServices
import com.urikai.app.res.Res
import com.urikai.app.ui.theme.LocalIsDebugBuild
import com.urikai.app.ui.theme.UriKaiTheme
import com.urikai.app.ui.theme.installPixelFont
import com.urikai.app.ui.theme.rememberPixelFontFamily

/** Android / Web 共通のルート。フォントと charts.csv を読み終えてから画面を組む。 */
@Composable
fun UriKaiApp(services: PlatformServices) {
    val font = rememberPixelFontFamily()
    var chartsCsv by remember { mutableStateOf<String?>(null) }
    LaunchedEffect(Unit) {
        chartsCsv = runCatching { Res.readBytes("files/charts.csv").decodeToString() }.getOrElse { "" }
    }

    val loadedFont = font
    val csv = chartsCsv
    if (loadedFont == null || csv == null) {
        Box(Modifier.fillMaxSize().background(Color(0xFF9BBC0F)))
        return
    }
    remember(loadedFont) { installPixelFont(loadedFont) }

    val viewModel = viewModel { GameViewModel(services, csv) }
    LifecycleStartEffect(viewModel) {
        viewModel.setAppForeground(true)
        onStopOrDispose { viewModel.setAppForeground(false) }
    }

    CompositionLocalProvider(LocalIsDebugBuild provides services.isDebugBuild) {
        UriKaiRoot(viewModel) {
            val text = viewModel.buildShareText() ?: return@UriKaiRoot
            services.shareText(text, UiStrings.shareChooserTitle(viewModel.displayLanguage.value))
        }
    }
}

@Composable
private fun UriKaiRoot(viewModel: GameViewModel, onShare: () -> Unit) {
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
        val balance by viewModel.balance.collectAsStateWithLifecycle()
        val showBankrupt by viewModel.showBankrupt.collectAsStateWithLifecycle()
        val sessionPnlHistory by viewModel.sessionPnlHistory.collectAsStateWithLifecycle()
        val balanceHistory by viewModel.balanceHistory.collectAsStateWithLifecycle()
        val peakBalance by viewModel.peakBalance.collectAsStateWithLifecycle()
        val investPlan by viewModel.investPlan.collectAsStateWithLifecycle()
        val awayReport by viewModel.awayReport.collectAsStateWithLifecycle()
        val cashOutCount by viewModel.cashOutCount.collectAsStateWithLifecycle()
        val pendingRelics by viewModel.pendingRelics.collectAsStateWithLifecycle()
        val newlyPendingRelics by viewModel.newlyPendingRelics.collectAsStateWithLifecycle()
        val claimedRelics by viewModel.claimedRelics.collectAsStateWithLifecycle()
        val relicEffects by viewModel.relicEffects.collectAsStateWithLifecycle()
        val relicStagePrefs by viewModel.relicStagePrefs.collectAsStateWithLifecycle()
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
            balance = balance,
            showBankrupt = showBankrupt,
            onRestartAfterBankruptcy = viewModel::restartAfterBankruptcy,
            sessionPnlHistory = sessionPnlHistory,
            balanceHistory = balanceHistory,
            peakBalance = peakBalance,
            investPlan = investPlan,
            onSelectInvestPlan = viewModel::selectInvestPlan,
            awayReport = awayReport,
            onDismissAwayReport = viewModel::dismissAwayReport,
            cashOutCount = cashOutCount,
            pendingRelics = pendingRelics,
            newlyPendingRelics = newlyPendingRelics,
            claimedRelics = claimedRelics,
            relicEffects = relicEffects,
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
            onShare = onShare,
            onShuffleStartTip = viewModel::shuffleStartAttractTip,
            onClaimRelic = viewModel::claimPendingRelic,
            onToggleRelicVisual = viewModel::toggleRelicVisual,
            onCycleRelicStage = viewModel::cycleRelicStage,
            relicStagePrefs = relicStagePrefs,
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
