package com.urikai.app.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.urikai.app.data.BalancePoint
import com.urikai.app.data.DailyAccrualResult
import com.urikai.app.game.ChartLineStyle
import com.urikai.app.game.InvestPlan
import com.urikai.app.game.LcdThemeMode
import com.urikai.app.game.NotebookDisplayOrder
import com.urikai.app.game.NotebookEntryId
import com.urikai.app.game.RelicEffects
import com.urikai.app.game.RelicId
import com.urikai.app.game.RelicUpgradeGroup
import com.urikai.app.data.SessionPnlHistoryEntry
import com.urikai.app.game.Sumika
import com.urikai.app.game.SumikaCostReveal
import com.urikai.app.game.SumikaSnapshot
import com.urikai.app.game.UnlockPlacement
import com.urikai.app.game.placement
import com.urikai.app.ui.theme.LocalEnableAdFeatures

/**
 * ホーム START 画面。GameScreenContent から分離して DEX VerifyError を防ぐ。
 */
@Composable
fun HomeStartScene(
    startSmallText: String,
    onShuffleStartTip: () -> Unit,
    titleSecretInvert: Boolean,
    onTitleSecretInvert: () -> Unit,
    claimedRelics: List<RelicId>,
    pendingRelics: List<RelicId>,
    relicStagePrefs: Map<RelicUpgradeGroup, RelicId>,
    disabledUpgradeGroups: Set<RelicUpgradeGroup>,
    disabledRelics: Set<RelicId>,
    onCyclePlayBuff: (com.urikai.app.game.PlayBuffSlot) -> Unit,
    onStampBackOpened: () -> Unit,
    balance: Long,
    peakBalance: Long,
    balanceCountFrom: Long?,
    onClearBalanceCountFrom: () -> Unit,
    onPayoutCountTick: (Float) -> Unit,
    investPlan: InvestPlan,
    onSelectInvestPlan: (InvestPlan) -> Unit,
    onBalanceOpened: () -> Unit,
    onBalanceClosed: () -> Unit,
    balanceHistory: List<BalancePoint>,
    sessionPnlHistory: List<SessionPnlHistoryEntry>,
    cashOutCount: Int,
    bestCombo: Int,
    relicEffects: RelicEffects,
    showStampCornerHint: Boolean,
    onDismissStampCornerHint: () -> Unit,
    forceSpeechOpen: Boolean,
    /** カイ引っ越し導入中は本体を隠し、セリフ側の顔だけ見せる。 */
    hideKaiForHomeIntro: Boolean = false,
    notebookUnlocked: Set<NotebookEntryId>,
    notebookRead: Set<NotebookEntryId>,
    onMarkNotebookRead: (NotebookEntryId) -> Unit,
    showInvestOpenTutorial: Boolean,
    onDismissInvestOpenTutorial: () -> Unit,
    showInvestCloseTip: Boolean,
    onDismissInvestCloseTip: () -> Unit,
    showSumikaSheetTutorial: Boolean,
    onSumikaSheetOpened: () -> Unit,
    onDismissSumikaSheetTutorial: () -> Unit,
    showStampBackTutorial: Boolean,
    onDismissStampBackTutorial: () -> Unit,
    showContentDisclaimer: Boolean,
    onDismissContentDisclaimer: () -> Unit,
    awayReport: DailyAccrualResult?,
    onDismissAwayReport: () -> Unit,
    lcdTheme: LcdThemeMode,
    chartLineStyle: ChartLineStyle,
    onToggleRelicVisual: (RelicId) -> Unit,
    onToggleFxRelic: (RelicId) -> Unit,
    onClaimRelic: (RelicId) -> Boolean,
    sumikaLevel: Int,
    sumikaPendingYen: Long,
    sumikaUnlockedGrade: Int,
    sumikaPendingGrade: Int,
    sumikaGradeClaimReady: Boolean,
    onClaimSumikaGrade: () -> Unit,
    onUpgradeSumika: () -> Unit,
    onCollectSumikaPay: () -> Unit,
    onRefreshSumika: () -> Unit,
    sumikaUpgradeDiscountStacks: Int = 0,
    sumikaDiscountUsesLeft: Int = 3,
    sumikaPayDoubled: Boolean = false,
    sumikaDoubleUsesLeft: Int = 3,
    sumikaRewardBusy: Boolean = false,
    sumikaCostReveal: SumikaCostReveal? = null,
    adsRemoved: Boolean = false,
    onRequestSumikaUpgradeDiscount: () -> Unit = {},
    shouldOfferSumikaPayDoubleChoice: () -> Boolean = { false },
    onMarkSumikaPayDoubleChoiceShown: () -> Unit = {},
    onCollectSumikaPayDoubledViaAd: () -> Unit = {},
    kaiAllowanceYen: Long,
    onCollectKaiAllowance: () -> Unit,
    showSumika: Boolean,
    onShowSumikaChange: (Boolean) -> Unit,
    showTutorialGuide: Boolean,
    onShowTutorialGuideChange: (Boolean) -> Unit,
    showStyleLook: Boolean,
    onShowStyleLookChange: (Boolean) -> Unit,
    showSessionPnlHistory: Boolean,
    onShowSessionPnlHistoryChange: (Boolean) -> Unit,
    claimRelicTarget: RelicId?,
    onClaimRelicTargetChange: (RelicId?) -> Unit,
    claimCelebrating: Boolean,
    onClaimCelebratingChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier.align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StartAttractPanel(
                smallText = startSmallText,
                onFlingShuffle = onShuffleStartTip,
                secretInvert = titleSecretInvert,
                onSecretInvert = onTitleSecretInvert,
                claimed = claimedRelics,
                stagePrefs = relicStagePrefs,
                disabledGroups = disabledUpgradeGroups,
                disabledRelics = disabledRelics,
                onCyclePlayBuff = onCyclePlayBuff,
                onStampBackOpened = onStampBackOpened
            )
            LifetimeLedgerStrip(
                balance = balance,
                balanceCountFrom = balanceCountFrom,
                onBalanceCountFinished = onClearBalanceCountFrom,
                onCountTick = onPayoutCountTick,
                investPlan = investPlan,
                onOpen = {
                    onShowSessionPnlHistoryChange(true)
                    onBalanceOpened()
                }
            )
        }
        if (showStampCornerHint) {
            StampCornerHintBubble(
                onDismiss = onDismissStampCornerHint,
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(top = 200.dp)
            )
        }
        StyleEntryButton(
            unlocked = claimedRelics.any { it.placement() == UnlockPlacement.LOOK },
            onClick = { onShowStyleLookChange(true) },
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(start = 8.dp, bottom = 8.dp)
        )
        HomeSumikaAndKai(
            sumikaLevel = sumikaLevel,
            sumikaPendingYen = sumikaPendingYen,
            sumikaPendingGrade = sumikaPendingGrade,
            sumikaGradeClaimReady = sumikaGradeClaimReady,
            onClaimSumikaGrade = onClaimSumikaGrade,
            onOpenSumikaSheet = {
                onRefreshSumika()
                onShowSumikaChange(true)
            },
            onCollectSumikaPay = onCollectSumikaPay,
            sumikaPayDoubled = sumikaPayDoubled,
            sumikaDoubleUsesLeft = sumikaDoubleUsesLeft,
            sumikaRewardBusy = sumikaRewardBusy,
            adsRemoved = adsRemoved,
            shouldOfferSumikaPayDoubleChoice = shouldOfferSumikaPayDoubleChoice,
            onMarkSumikaPayDoubleChoiceShown = onMarkSumikaPayDoubleChoiceShown,
            onCollectSumikaPayDoubledViaAd = onCollectSumikaPayDoubledViaAd,
            hideKai = forceSpeechOpen ||
                hideKaiForHomeIntro ||
                showTutorialGuide ||
                showSumika ||
                showStyleLook ||
                showSessionPnlHistory ||
                cashOutCount < 1,
            notebookHasUnread = notebookUnlocked.any { it !in notebookRead },
            balance = balance,
            peakBalance = peakBalance,
            playCount = cashOutCount,
            bestCombo = bestCombo,
            kaiAllowanceYen = kaiAllowanceYen,
            onCollectKaiAllowance = onCollectKaiAllowance,
        )
        RelicGiftBadge(
            pendingCount = pendingRelics.size,
            onClick = {
                onClaimCelebratingChange(false)
                onClaimRelicTargetChange(pendingRelics.firstOrNull())
            },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 8.dp, bottom = 8.dp)
        )
        if (showSessionPnlHistory) {
            BalanceHistoryOverlay(
                balance = balance,
                peakBalance = peakBalance,
                investPlan = investPlan,
                onSelectInvestPlan = onSelectInvestPlan,
                history = balanceHistory,
                entries = sessionPnlHistory,
                playCount = cashOutCount,
                claimedUnlocks = claimedRelics,
                pendingUnlocks = pendingRelics,
                planYieldMult = relicEffects.planYieldMult,
                maxAwayDays = relicEffects.maxAwayDays,
                onDismiss = {
                    onShowSessionPnlHistoryChange(false)
                    onBalanceClosed()
                },
                modifier = Modifier.fillMaxSize()
            )
        }
        if (showTutorialGuide) {
            NotebookOverlay(
                unlocked = NotebookDisplayOrder.filter { it in notebookUnlocked },
                read = notebookRead,
                onMarkRead = onMarkNotebookRead,
                onDismiss = { onShowTutorialGuideChange(false) },
                modifier = Modifier.fillMaxSize()
            )
        }
        if (showInvestOpenTutorial) {
            InvestOpenTutorialOverlay(
                onDismiss = onDismissInvestOpenTutorial,
                modifier = Modifier.fillMaxSize()
            )
        }
        if (showInvestCloseTip) {
            InvestCloseTipOverlay(
                onDismiss = onDismissInvestCloseTip,
                modifier = Modifier.fillMaxSize()
            )
        }
        if (showStampBackTutorial) {
            StampBackTutorialOverlay(
                onDismiss = onDismissStampBackTutorial,
                modifier = Modifier.fillMaxSize()
            )
        }
        if (showContentDisclaimer) {
            ContentDisclaimerOverlay(
                onDismiss = onDismissContentDisclaimer,
                modifier = Modifier.fillMaxSize()
            )
        }
        if (awayReport != null && !showContentDisclaimer) {
            AwayReportOverlay(
                report = awayReport,
                onDismiss = onDismissAwayReport,
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
                        onClaimCelebratingChange(true)
                    }
                },
                onDismiss = {
                    if (claimCelebrating) {
                        onClaimCelebratingChange(false)
                        onClaimRelicTargetChange(pendingRelics.firstOrNull())
                    } else {
                        onClaimRelicTargetChange(null)
                    }
                },
                modifier = Modifier.fillMaxSize()
            )
        }
        if (showStyleLook) {
            StyleLookOverlay(
                claimed = claimedRelics,
                lcdTheme = lcdTheme,
                chartLineStyle = chartLineStyle,
                disabledRelics = disabledRelics,
                onToggleVisual = onToggleRelicVisual,
                onToggleFx = onToggleFxRelic,
                onDismiss = { onShowStyleLookChange(false) },
                modifier = Modifier
                    .fillMaxSize()
                    .zIndex(20f)
            )
        }
        if (showSumika) {
            SumikaSheet(
                snapshot = SumikaSnapshot(
                    level = sumikaLevel,
                    pendingYen = sumikaPendingYen,
                    balance = balance,
                    cashOutCount = cashOutCount,
                    unlockedGrade = sumikaUnlockedGrade,
                    pendingGrade = sumikaPendingGrade,
                    upgradeDiscountStacks = sumikaUpgradeDiscountStacks,
                    discountUsesLeftToday = sumikaDiscountUsesLeft,
                    payDoubled = sumikaPayDoubled,
                    doubleUsesLeftToday = sumikaDoubleUsesLeft,
                ),
                onUpgrade = onUpgradeSumika,
                onDismiss = {
                    if (!showSumikaSheetTutorial) onShowSumikaChange(false)
                },
                showTutorial = showSumikaSheetTutorial,
                onDismissTutorial = onDismissSumikaSheetTutorial,
                adsRemoved = adsRemoved,
                rewardBusy = sumikaRewardBusy,
                costReveal = sumikaCostReveal,
                onRequestDiscount = onRequestSumikaUpgradeDiscount,
                modifier = Modifier
                    .fillMaxSize()
                    .zIndex(20f)
            )
        }
    }
}

@Composable
internal fun BoxScope.HomeSumikaAndKai(
    sumikaLevel: Int,
    sumikaPendingYen: Long,
    sumikaPendingGrade: Int,
    sumikaGradeClaimReady: Boolean,
    onClaimSumikaGrade: () -> Unit,
    onOpenSumikaSheet: () -> Unit,
    onCollectSumikaPay: () -> Unit,
    sumikaPayDoubled: Boolean = false,
    sumikaDoubleUsesLeft: Int = 3,
    sumikaRewardBusy: Boolean = false,
    adsRemoved: Boolean = false,
    shouldOfferSumikaPayDoubleChoice: () -> Boolean = { false },
    onMarkSumikaPayDoubleChoiceShown: () -> Unit = {},
    onCollectSumikaPayDoubledViaAd: () -> Unit = {},
    hideKai: Boolean,
    notebookHasUnread: Boolean,
    balance: Long,
    peakBalance: Long,
    playCount: Int,
    bestCombo: Int,
    kaiAllowanceYen: Long,
    onCollectKaiAllowance: () -> Unit,
) {
    var insidePeek by remember { mutableStateOf(false) }
    var insideKaiPose by remember { mutableStateOf<GuideBuddyPose?>(null) }
    var insideSpeech by remember { mutableStateOf<String?>(null) }
    var wakeInsideNonce by remember { mutableIntStateOf(0) }
    var kaiOccupying by remember { mutableStateOf(false) }
    // フェード中も描画を残す
    var peekLayerMounted by remember { mutableStateOf(false) }
    val peekAlpha = remember { Animatable(0f) }
    LaunchedEffect(insidePeek) {
        if (insidePeek) {
            peekLayerMounted = true
            peekAlpha.animateTo(1f, tween(durationMillis = 350))
        } else if (peekLayerMounted) {
            peekAlpha.animateTo(0f, tween(durationMillis = 350))
            peekLayerMounted = false
        }
    }
    if (sumikaGradeClaimReady) {
        SumikaGradeRewardChip(
            grade = sumikaPendingGrade,
            onClaim = onClaimSumikaGrade,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 8.dp)
                .zIndex(0f)
        )
    } else if (sumikaLevel > 0 && sumikaPendingGrade <= 0) {
        // 家具のみ（給料はカイより前面レイヤ）
        SumikaHomeSpot(
            level = sumikaLevel,
            pendingYen = 0L,
            onOpenSheet = onOpenSumikaSheet,
            onCollect = onCollectSumikaPay,
            embedPayPop = false,
            kaiOccupying = kaiOccupying,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = SumikaHomeBottomPad)
                .zIndex(0f)
        )
    }
    // シート表示中も破棄しない（位置・占用を維持。見た目だけ隠す）
    HomeKaiResident(
        notebookHasUnread = notebookHasUnread,
        balance = balance,
        peakBalance = peakBalance,
        playCount = playCount,
        bestCombo = bestCombo,
        allowanceYen = kaiAllowanceYen,
        onCollectAllowance = onCollectKaiAllowance,
        sumikaLevel = sumikaLevel,
        sumikaPayPending = sumikaPendingYen > 0L,
        onInsidePeekChanged = { show, pose ->
            insidePeek = show
            insideKaiPose = pose
            if (!show) insideSpeech = null
        },
        onOccupyingChanged = { kaiOccupying = it },
        wakeInsideNonce = wakeInsideNonce,
        onInsideSpeechChanged = { insideSpeech = it },
        hidden = hideKai,
        modifier = Modifier
            .fillMaxSize()
            .zIndex(1f)
    )
    // のぞき穴は巣レイアウト外。巣の右・少し上。入退室でフェード。
    if (!hideKai && peekLayerMounted && Sumika.supportsInside(sumikaLevel)) {
        val nestHalfW = sumikaHomeArtSize(Sumika.gradeIndex(sumikaLevel)).first / 2
        SumikaInsidePeekOverlay(
            level = sumikaLevel,
            kaiPose = insideKaiPose,
            onClick = {
                if (peekAlpha.value > 0.9f) wakeInsideNonce += 1
            },
            speechLine = insideSpeech,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = SumikaHomeBottomPad)
                .offset(x = nestHalfW + 4.dp, y = (-28).dp)
                .zIndex(5f)
                .graphicsLayer { alpha = peekAlpha.value }
        )
    }
    // カイより前・シート／ガイドより後ろ（hideKai 中は出さない）
    if (!hideKai && sumikaLevel > 0 && sumikaPendingGrade <= 0 && !sumikaGradeClaimReady) {
        SumikaHomePayOverlay(
            level = sumikaLevel,
            pendingYen = sumikaPendingYen,
            onCollect = onCollectSumikaPay,
            kaiOccupying = kaiOccupying,
            payDoubled = sumikaPayDoubled,
            canOfferDouble = LocalEnableAdFeatures.current &&
                sumikaPendingYen > 0L &&
                !sumikaPayDoubled &&
                sumikaDoubleUsesLeft > 0,
            adsRemoved = adsRemoved,
            rewardBusy = sumikaRewardBusy,
            shouldOfferDoubleChoice = shouldOfferSumikaPayDoubleChoice,
            onMarkDoubleChoiceShown = onMarkSumikaPayDoubleChoiceShown,
            onCollectDoubledViaAd = onCollectSumikaPayDoubledViaAd,
        )
    }
}
