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
import com.urikai.app.ui.theme.LocalEnableAdFeatures
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

    CompositionLocalProvider(
        LocalIsDebugBuild provides services.isDebugBuild,
        LocalEnableAdFeatures provides services.enableAdFeatures,
    ) {
        UriKaiRoot(
            viewModel = viewModel,
            onShare = {
                val text = viewModel.buildShareText()
                if (text != null) {
                    services.shareText(
                        text,
                        UiStrings.shareChooserTitle(viewModel.displayLanguage.value),
                    )
                }
            },
            onOpenStoreListing = services.openStoreListing,
        )
    }
}

@Composable
private fun UriKaiRoot(
    viewModel: GameViewModel,
    onShare: () -> Unit,
    onOpenStoreListing: () -> Unit,
) {
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
        val sessionPnlHistory by viewModel.sessionPnlHistory.collectAsStateWithLifecycle()
        val balanceHistory by viewModel.balanceHistory.collectAsStateWithLifecycle()
        val peakBalance by viewModel.peakBalance.collectAsStateWithLifecycle()
        val investPlan by viewModel.investPlan.collectAsStateWithLifecycle()
        val awayReport by viewModel.awayReport.collectAsStateWithLifecycle()
        val cashOutCount by viewModel.cashOutCount.collectAsStateWithLifecycle()
        val bestCombo by viewModel.bestCombo.collectAsStateWithLifecycle()
        val pendingRelics by viewModel.pendingRelics.collectAsStateWithLifecycle()
        val newlyPendingRelics by viewModel.newlyPendingRelics.collectAsStateWithLifecycle()
        val claimedRelics by viewModel.claimedRelics.collectAsStateWithLifecycle()
        val relicEffects by viewModel.relicEffects.collectAsStateWithLifecycle()
        val relicStagePrefs by viewModel.relicStagePrefs.collectAsStateWithLifecycle()
        val disabledUpgradeGroups by viewModel.disabledUpgradeGroups.collectAsStateWithLifecycle()
        val disabledRelics by viewModel.disabledRelics.collectAsStateWithLifecycle()
        val showFirstPlayTutorial by viewModel.showFirstPlayTutorial.collectAsStateWithLifecycle()
        val showLifeTutorial by viewModel.showLifeTutorial.collectAsStateWithLifecycle()
        val showInvestOpenTutorial by viewModel.showInvestOpenTutorial.collectAsStateWithLifecycle()
        val showInvestCloseTip by viewModel.showInvestCloseTip.collectAsStateWithLifecycle()
        val showSumikaSheetTutorial by viewModel.showSumikaSheetTutorial.collectAsStateWithLifecycle()
        val showScrapbookTutorial by viewModel.showScrapbookTutorial.collectAsStateWithLifecycle()
        val showStampCornerHint by viewModel.showStampCornerHint.collectAsStateWithLifecycle()
        val showStampBackTutorial by viewModel.showStampBackTutorial.collectAsStateWithLifecycle()
        val showKaiHomeIntro by viewModel.showKaiHomeIntro.collectAsStateWithLifecycle()
        val showStartBang by viewModel.showStartBang.collectAsStateWithLifecycle()
        val showStartEncounterWipe by viewModel.showStartEncounterWipe.collectAsStateWithLifecycle()
        val startIntroNowXFraction by viewModel.startIntroNowXFraction.collectAsStateWithLifecycle()
        val lifeMechanicsActive by viewModel.lifeMechanicsActive.collectAsStateWithLifecycle()
        val notebookUnlocked by viewModel.notebookUnlocked.collectAsStateWithLifecycle()
        val notebookRead by viewModel.notebookRead.collectAsStateWithLifecycle()
        val notebookHasUnread by viewModel.notebookHasUnread.collectAsStateWithLifecycle()
        val showContentDisclaimer by viewModel.showContentDisclaimer.collectAsStateWithLifecycle()
        val seLevel by viewModel.seLevel.collectAsStateWithLifecycle()
        val bgmLevel by viewModel.bgmLevel.collectAsStateWithLifecycle()
        val hapticEnabled by viewModel.hapticEnabled.collectAsStateWithLifecycle()
        val adsRemoved by viewModel.adsRemoved.collectAsStateWithLifecycle()
        val removeAdsPriceLabel by viewModel.removeAdsPriceLabel.collectAsStateWithLifecycle()
        val sumikaLevel by viewModel.sumikaLevel.collectAsStateWithLifecycle()
        val sumikaPendingYen by viewModel.sumikaPendingYen.collectAsStateWithLifecycle()
        val sumikaUpgradeDiscountStacks by viewModel.sumikaUpgradeDiscountStacks.collectAsStateWithLifecycle()
        val sumikaDiscountUsesLeft by viewModel.sumikaDiscountUsesLeft.collectAsStateWithLifecycle()
        val sumikaPayDoubled by viewModel.sumikaPayDoubled.collectAsStateWithLifecycle()
        val sumikaDoubleUsesLeft by viewModel.sumikaDoubleUsesLeft.collectAsStateWithLifecycle()
        val sumikaRewardBusy by viewModel.sumikaRewardBusy.collectAsStateWithLifecycle()
        val sumikaCostReveal by viewModel.sumikaCostReveal.collectAsStateWithLifecycle()
        val sumikaUnlockedGrade by viewModel.sumikaUnlockedGrade.collectAsStateWithLifecycle()
        val sumikaPendingGrade by viewModel.sumikaPendingGrade.collectAsStateWithLifecycle()
        val sumikaGradeIntroDone by viewModel.sumikaGradeIntroDone.collectAsStateWithLifecycle()
        val sumikaGradeIntroHeld by viewModel.sumikaGradeIntroHeld.collectAsStateWithLifecycle()
        val sumikaClaimCelebrationGrade by viewModel.sumikaClaimCelebrationGrade.collectAsStateWithLifecycle()
        val balanceCountFrom by viewModel.balanceCountFrom.collectAsStateWithLifecycle()
        val kaiAllowanceYen by viewModel.kaiAllowanceYen.collectAsStateWithLifecycle()
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
            sessionPnlHistory = sessionPnlHistory,
            balanceHistory = balanceHistory,
            peakBalance = peakBalance,
            investPlan = investPlan,
            onSelectInvestPlan = viewModel::selectInvestPlan,
            awayReport = awayReport,
            onDismissAwayReport = viewModel::dismissAwayReport,
            cashOutCount = cashOutCount,
            bestCombo = bestCombo,
            pendingRelics = pendingRelics,
            newlyPendingRelics = newlyPendingRelics,
            claimedRelics = claimedRelics,
            relicEffects = relicEffects,
            showFirstPlayTutorial = showFirstPlayTutorial,
            showLifeTutorial = showLifeTutorial,
            showInvestOpenTutorial = showInvestOpenTutorial,
            showInvestCloseTip = showInvestCloseTip,
            showSumikaSheetTutorial = showSumikaSheetTutorial,
            showScrapbookTutorial = showScrapbookTutorial,
            showStampCornerHint = showStampCornerHint,
            showStampBackTutorial = showStampBackTutorial,
            showKaiHomeIntro = showKaiHomeIntro,
            showStartBang = showStartBang,
            showStartEncounterWipe = showStartEncounterWipe,
            startIntroNowXFraction = startIntroNowXFraction,
            lifeMechanicsActive = lifeMechanicsActive,
            notebookUnlocked = notebookUnlocked,
            notebookRead = notebookRead,
            notebookHasUnread = notebookHasUnread,
            onMarkNotebookRead = viewModel::markNotebookRead,
            onDismissLifeTutorial = viewModel::dismissLifeTutorial,
            onDismissInvestOpenTutorial = viewModel::dismissInvestOpenTutorial,
            onDismissInvestCloseTip = viewModel::dismissInvestCloseTip,
            onSumikaSheetOpened = viewModel::onSumikaSheetOpened,
            onDismissSumikaSheetTutorial = viewModel::dismissSumikaSheetTutorial,
            onDismissScrapbookTutorial = viewModel::dismissScrapbookTutorial,
            onDismissStampCornerHint = viewModel::dismissStampCornerHint,
            onDismissStampBackTutorial = viewModel::dismissStampBackTutorial,
            onDismissKaiHomeIntro = viewModel::dismissKaiHomeIntro,
            onStartBangFinished = viewModel::onStartBangFinished,
            onStartEncounterCovered = viewModel::onStartEncounterCovered,
            onStartEncounterOpenStarted = viewModel::onStartEncounterOpenStarted,
            onStartEncounterRevealed = viewModel::onStartEncounterRevealed,
            onBalanceOpened = viewModel::onBalanceOpened,
            onBalanceClosed = viewModel::onBalanceClosed,
            onFirstClearNewsDismissed = viewModel::onFirstClearNewsDismissed,
            onStampBackOpened = viewModel::onStampBackOpened,
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
            adsRemoved = adsRemoved,
            removeAdsPriceLabel = removeAdsPriceLabel,
            onPurchaseRemoveAds = viewModel::purchaseRemoveAds,
            onRestorePurchases = viewModel::restorePurchases,
            onOpenStoreListing = onOpenStoreListing,
            onDebugSetAdsRemoved = viewModel::debugSetAdsRemoved,
            onDebugResetSumika = viewModel::debugResetSumika,
            onDebugForceSumikaGrade = viewModel::debugForceSumikaGrade,
            onDebugWipeAllProgress = viewModel::debugWipeAllProgressAndRestart,
            sumikaLevel = sumikaLevel,
            sumikaPendingYen = sumikaPendingYen,
            sumikaUnlockedGrade = sumikaUnlockedGrade,
            sumikaPendingGrade = sumikaPendingGrade,
            sumikaGradeIntroDone = sumikaGradeIntroDone,
            sumikaGradeIntroHeld = sumikaGradeIntroHeld,
            sumikaClaimCelebrationGrade = sumikaClaimCelebrationGrade,
            onMarkSumikaGradeIntroDone = viewModel::markSumikaGradeIntroDone,
            onClaimSumikaGrade = viewModel::claimSumikaGradeReward,
            onDismissSumikaClaimCelebration = viewModel::dismissSumikaClaimCelebration,
            balanceCountFrom = balanceCountFrom,
            kaiAllowanceYen = kaiAllowanceYen,
            onUpgradeSumika = viewModel::upgradeSumika,
            onCollectSumikaPay = viewModel::collectSumikaPay,
            onCollectKaiAllowance = viewModel::collectKaiAllowance,
            onRefreshSumika = viewModel::refreshSumikaUi,
            sumikaUpgradeDiscountStacks = sumikaUpgradeDiscountStacks,
            sumikaDiscountUsesLeft = sumikaDiscountUsesLeft,
            sumikaPayDoubled = sumikaPayDoubled,
            sumikaDoubleUsesLeft = sumikaDoubleUsesLeft,
            sumikaRewardBusy = sumikaRewardBusy,
            sumikaCostReveal = sumikaCostReveal,
            onRequestSumikaUpgradeDiscount = viewModel::requestSumikaUpgradeDiscount,
            shouldOfferSumikaPayDoubleChoice = viewModel::shouldOfferSumikaPayDoubleChoice,
            onMarkSumikaPayDoubleChoiceShown = viewModel::markSumikaPayDoubleChoiceShown,
            onCollectSumikaPayDoubledViaAd = viewModel::requestSumikaPayDoubleAndCollect,
            onClearBalanceCountFrom = viewModel::clearBalanceCountFrom,
            onScrapbookSortChange = viewModel::setScrapbookSort,
            onShare = onShare,
            onShuffleStartTip = viewModel::shuffleStartAttractTip,
            onClaimRelic = viewModel::claimPendingRelic,
            onToggleRelicVisual = viewModel::toggleRelicVisual,
            onToggleFxRelic = viewModel::toggleFxRelic,
            onCycleRelicStage = viewModel::cycleRelicStage,
            onCyclePlayBuff = viewModel::cyclePlayBuff,
            relicStagePrefs = relicStagePrefs,
            disabledUpgradeGroups = disabledUpgradeGroups,
            disabledRelics = disabledRelics,
            onDismissFirstPlayTutorial = viewModel::dismissFirstPlayTutorial,
            onResultIntroFinished = viewModel::onResultIntroFinished,
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
            onUiSelect = viewModel::cueUiSelect,
            onUiBuff = viewModel::cueUiBuff
        )
    }
}
