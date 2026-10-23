package com.urikai.app.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.EaseInOutCubic
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.urikai.app.game.DisplayLanguage
import com.urikai.app.game.Sumika
import com.urikai.app.game.SumikaSnapshot
import com.urikai.app.game.UiStrings
import com.urikai.app.game.fmt
import com.urikai.app.ui.theme.Ink
import com.urikai.app.ui.theme.InkMuted
import com.urikai.app.ui.theme.LcdBackground
import com.urikai.app.ui.theme.LocalDisplayLanguage
import com.urikai.app.ui.theme.PixelFont
import kotlin.math.roundToLong
import kotlin.random.Random
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/** 家具上端からポップ下端までのすきま。 */
private const val POP_GAP_ABOVE_NEST_DP = 40

/**
 * ホームのすみか。本体タップ→詳細／金額ポップタップ→受取。
 */
@Composable
fun SumikaHomeSpot(
    level: Int,
    pendingYen: Long,
    onOpenSheet: () -> Unit,
    onCollect: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val ink = Ink
    var flyAmount by remember { mutableStateOf<Long?>(null) }
    var popGone by remember { mutableStateOf(false) }
    val showPop = pendingYen > 0L && !popGone && flyAmount == null

    LaunchedEffect(pendingYen) {
        if (pendingYen <= 0L) {
            popGone = false
            flyAmount = null
        }
    }

    Box(modifier = modifier, contentAlignment = Alignment.BottomCenter) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .uiClickable(kind = UiSoundKind.SELECT, onClick = onOpenSheet)
                .padding(horizontal = 6.dp, vertical = 4.dp)
        ) {
            Canvas(modifier = Modifier.size(width = 88.dp, height = 44.dp)) {
                drawSumikaNest(level, ink)
            }
        }
        if (showPop) {
            val lang = LocalDisplayLanguage.current
            SumikaPayPop(
                amount = pendingYen,
                caption = if (lang == DisplayLanguage.EN) "PAY" else "お給料",
                onClick = {
                    flyAmount = pendingYen
                    popGone = true
                },
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .offset(y = (-POP_GAP_ABOVE_NEST_DP).dp)
            )
        }
        flyAmount?.let { amt ->
            CollectFlyBurst(
                amount = amt,
                onFinished = {
                    onCollect()
                    flyAmount = null
                },
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .offset(y = (-POP_GAP_ABOVE_NEST_DP).dp)
            )
        }
    }
}

/**
 * 受取ポップ＝すみかからの吹き出し。
 * アイコンや二重線チップは使わず、「誰かが置いていった金額」感を出す。
 */
@Composable
fun SumikaPayPop(
    amount: Long,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    /** null なら金額だけ（お小遣いなど）。すみか給料は呼び出し側で渡す。 */
    caption: String? = null,
) {
    val bob = rememberInfiniteTransition(label = "pay_bob")
    val dy by bob.animateFloat(
        initialValue = 0f,
        targetValue = -4f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pay_bob_y"
    )
    val ink = Ink
    val paper = LcdBackground
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .graphicsLayer { translationY = dy }
            .uiClickable(kind = UiSoundKind.CONFIRM, onClick = onClick)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Canvas(modifier = Modifier.matchParentSize()) {
                val tail = 5.dp.toPx()
                val w = size.width
                val h = (size.height - tail).coerceAtLeast(1f)
                val r = 6.dp.toPx()
                drawRoundRect(
                    color = paper,
                    topLeft = Offset.Zero,
                    size = Size(w, h),
                    cornerRadius = CornerRadius(r, r)
                )
                drawRoundRect(
                    color = ink,
                    topLeft = Offset.Zero,
                    size = Size(w, h),
                    cornerRadius = CornerRadius(r, r),
                    style = Stroke(width = 1.5f)
                )
                val tipX = w * 0.5f
                val path = Path().apply {
                    moveTo(tipX - tail, h - 0.5f)
                    lineTo(tipX, h + tail)
                    lineTo(tipX + tail, h - 0.5f)
                    close()
                }
                drawPath(path, paper)
                drawLine(ink, Offset(tipX - tail, h - 0.5f), Offset(tipX, h + tail), 1.5f)
                drawLine(ink, Offset(tipX + tail, h - 0.5f), Offset(tipX, h + tail), 1.5f)
            }
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(start = 12.dp, end = 12.dp, top = 7.dp, bottom = 12.dp)
            ) {
                if (caption != null) {
                    Text(
                        text = caption,
                        color = InkMuted,
                        fontSize = 8.sp,
                        fontFamily = PixelFont,
                        letterSpacing = 1.sp
                    )
                }
                Text(
                    text = "+¥%,d".fmt(amount),
                    color = Ink,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = PixelFont,
                    letterSpacing = 0.5.sp
                )
            }
        }
    }
}

@Composable
private fun CollectFlyBurst(
    amount: Long,
    onFinished: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val progress = remember { Animatable(0f) }
    LaunchedEffect(amount) {
        progress.snapTo(0f)
        progress.animateTo(1f, tween(420, easing = FastOutSlowInEasing))
        delay(40)
        onFinished()
    }
    val p = progress.value
    Text(
        text = "+¥%,d".fmt(amount),
        color = Ink,
        fontSize = (15f + 10f * (1f - p)).sp,
        fontWeight = FontWeight.Bold,
        fontFamily = PixelFont,
        modifier = modifier.graphicsLayer {
            translationY = -64f * p
            scaleX = 1f + 0.5f * p
            scaleY = 1f + 0.5f * p
            alpha = 1f - p
        }
    )
}

/** ホーム中央下: 次グレード家具の受け取り（出現ポップ付き）。 */
@Composable
fun SumikaGradeRewardChip(
    grade: Int,
    onClaim: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val lang = LocalDisplayLanguage.current
    val ink = Ink
    val appearScale = remember { Animatable(0.62f) }
    val appearAlpha = remember { Animatable(0f) }
    LaunchedEffect(grade) {
        appearScale.snapTo(0.62f)
        appearAlpha.snapTo(0f)
        launch { appearAlpha.animateTo(1f, tween(160)) }
        appearScale.animateTo(1.12f, tween(200, easing = FastOutSlowInEasing))
        appearScale.animateTo(0.96f, tween(90))
        appearScale.animateTo(1.04f, tween(90, easing = FastOutSlowInEasing))
        appearScale.animateTo(1f, tween(100))
    }
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .graphicsLayer {
                scaleX = appearScale.value
                scaleY = appearScale.value
                alpha = appearAlpha.value
            }
            .lcdDoubleBorder()
            .background(LcdBackground)
            .uiClickable(kind = UiSoundKind.CONFIRM, onClick = onClaim)
            .padding(horizontal = 14.dp, vertical = 10.dp)
    ) {
        Text(
            text = UiStrings.sumikaGradeRewardTitle(grade, lang),
            color = ink,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = PixelFont,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(4.dp))
        Canvas(modifier = Modifier.size(width = 96.dp, height = 40.dp)) {
            drawSumikaNest(Sumika.startLevelForGrade(grade), ink)
        }
        Spacer(Modifier.height(6.dp))
        Text(
            text = UiStrings.sumikaGradeRewardClaim(lang),
            color = LcdBackground,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = PixelFont,
            modifier = Modifier
                .background(Ink)
                .padding(horizontal = 16.dp, vertical = 6.dp)
        )
    }
}

/** 条件達成後ホーム復帰時: カイが前面で受け取り根拠を語る（ノートあそびかた同型）。 */
@Composable
fun SumikaGradeIntroOverlay(
    grade: Int,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val lang = LocalDisplayLanguage.current
    var step by remember(grade) { mutableIntStateOf(0) }
    val total = UiStrings.sumikaGradeIntroPageCount(grade)
    val lastStep = total - 1
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Ink.copy(alpha = 0.45f))
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() },
                onClick = {}
            )
    ) {
        GuideSpeechStage(
            name = UiStrings.guideBuddyName(lang),
            body = UiStrings.sumikaGradeIntroPage(grade, step, lang),
            stepLabel = "${step + 1}/$total",
            actionLabel = if (step < lastStep) {
                UiStrings.firstPlayTutorialNext(lang)
            } else {
                UiStrings.notebookClose(lang)
            },
            mood = when {
                step >= lastStep -> GuideBuddyMood.WINK
                step == 0 -> GuideBuddyMood.THINK
                else -> GuideBuddyMood.TALK
            },
            onAction = {
                if (step < lastStep) step += 1 else onDismiss()
            },
            modifier = Modifier.fillMaxSize()
        )
    }
}

/** 受け取り直後の獲得演出。 */
@Composable
fun SumikaGradeClaimToast(
    grade: Int,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    if (grade <= 0) return
    val lang = LocalDisplayLanguage.current
    val ink = Ink
    val alpha = remember { Animatable(0f) }
    val scale = remember { Animatable(0.62f) }
    val sparkle = rememberInfiniteTransition(label = "sumika_claim_sparkle")
    val sparkleAlpha by sparkle.animateFloat(
        initialValue = 1f,
        targetValue = 0.25f,
        animationSpec = infiniteRepeatable(
            animation = tween(380, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "sumika_claim_sparkle_alpha"
    )

    LaunchedEffect(grade) {
        alpha.snapTo(0f)
        scale.snapTo(0.62f)
        launch { alpha.animateTo(1f, tween(120)) }
        scale.animateTo(1.14f, tween(180, easing = FastOutSlowInEasing))
        scale.animateTo(0.96f, tween(90))
        scale.animateTo(1.04f, tween(90, easing = FastOutSlowInEasing))
        scale.animateTo(1f, tween(110))
        delay(2_600)
        alpha.animateTo(0f, tween(220))
        onDismiss()
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Ink.copy(alpha = 0.42f * alpha.value))
            .clickable(onClick = onDismiss),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth(0.86f)
                .graphicsLayer {
                    this.alpha = alpha.value
                    scaleX = scale.value
                    scaleY = scale.value
                }
                .lcdDoubleBorder()
                .background(LcdBackground)
                .clickable(enabled = false, onClick = {})
                .padding(horizontal = 18.dp, vertical = 22.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = "◆  ◆  ◆",
                color = Ink,
                fontSize = 14.sp,
                fontFamily = PixelFont,
                modifier = Modifier.alpha(sparkleAlpha)
            )
            Text(
                text = UiStrings.sumikaGradeClaimTitle(lang),
                color = Ink,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = PixelFont,
                textAlign = TextAlign.Center
            )
            Text(
                text = UiStrings.sumikaGradeClaimName(grade, lang),
                color = Ink,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = PixelFont,
                textAlign = TextAlign.Center
            )
            Canvas(modifier = Modifier.size(width = 140.dp, height = 64.dp)) {
                drawSumikaNest(Sumika.startLevelForGrade(grade), ink)
            }
            Text(
                text = UiStrings.sumikaGradeClaimFlavor(grade, lang),
                color = InkMuted,
                fontSize = 12.sp,
                fontFamily = PixelFont,
                textAlign = TextAlign.Center,
                lineHeight = 17.sp
            )
        }
    }
}

@Composable
fun SumikaSheet(
    snapshot: SumikaSnapshot,
    onUpgrade: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val lang = LocalDisplayLanguage.current
    val uiSound = LocalUiSound.current
    val ink = Ink
    val level = snapshot.level
    val nextNormal = snapshot.nextNormalLevel
    val nextGrade = snapshot.nextGradeUpLevel
    val upgradeTarget = nextNormal ?: nextGrade
    var confirming by remember { mutableStateOf(false) }
    val afterUpgrade = upgradeTarget?.let { snapshot.balance - Sumika.costToReach(it) }
    var kaiLineVisible by remember { mutableStateOf(false) }
    var kaiLine by remember { mutableStateOf("") }
    var kaiMood by remember { mutableStateOf(GuideBuddyMood.TALK) }
    var pokeNonce by remember { mutableIntStateOf(0) }
    var forceNonce by remember { mutableIntStateOf(0) }
    var forceText by remember { mutableStateOf<String?>(null) }
    var kaiScaleX by remember { mutableFloatStateOf(1f) }
    var kaiScaleY by remember { mutableFloatStateOf(1f) }
    var lastPayPerPlay by remember { mutableLongStateOf(snapshot.payPerPlay) }
    val latestSnapshot = rememberUpdatedState(snapshot)
    val latestUpgradeTarget = rememberUpdatedState(upgradeTarget)
    val latestAfterUpgrade = rememberUpdatedState(afterUpgrade)
    val latestLang = rememberUpdatedState(lang)

    LaunchedEffect(pokeNonce) {
        if (pokeNonce <= 0) return@LaunchedEffect
        // ホームのカイ・タップと同系の控えめスカッシュ
        kaiScaleX = 1.08f
        kaiScaleY = 0.90f
        delay(100)
        kaiScaleX = 0.94f
        kaiScaleY = 1.08f
        delay(120)
        kaiScaleX = 1.04f
        kaiScaleY = 0.96f
        delay(140)
        kaiScaleX = 1f
        kaiScaleY = 1f
    }

    // レベルアップ後: 稼ぎアップを即セリフ化
    LaunchedEffect(snapshot.payPerPlay) {
        val next = snapshot.payPerPlay
        val prev = lastPayPerPlay
        lastPayPerPlay = next
        if (prev > 0L && next > prev) {
            pokeNonce += 1
            forceText = UiStrings.sumikaKaiLevelUpLine(next, lang)
            forceNonce += 1
        }
    }

    LaunchedEffect(confirming, forceNonce, lang) {
        val snap = latestSnapshot.value
        val after = latestAfterUpgrade.value
        if (confirming && after != null) {
            kaiLine = UiStrings.sumikaKaiConfirmLine(snap.balance, after, latestLang.value)
            kaiMood = GuideBuddyMood.THINK
            kaiLineVisible = true
            return@LaunchedEffect
        }
        val forced = forceText
        if (forceNonce > 0 && forced != null) {
            kaiLine = forced
            kaiMood = GuideBuddyMood.WINK
            kaiLineVisible = true
            delay(4_000) // ホームタップと同程度
            kaiLineVisible = false
            delay(4_000)
        } else {
            kaiLineVisible = false
            delay(2_000)
        }
        while (true) {
            val s = latestSnapshot.value
            val up = latestUpgradeTarget.value
            val pool = UiStrings.sumikaKaiIdleLines(
                grade = Sumika.gradeIndex(s.level),
                balance = s.balance,
                payPerPlay = s.payPerPlay,
                passivePerTick = s.passivePerTick,
                upgradeCost = up?.let { Sumika.costToReach(it) },
                lang = latestLang.value,
            )
            if (pool.isEmpty()) {
                delay(4_000)
                continue
            }
            kaiLine = pool[Random.nextInt(pool.size)]
            kaiMood = if (
                up != null && s.balance < Sumika.costToReach(up) * 3L
            ) {
                GuideBuddyMood.THINK
            } else {
                GuideBuddyMood.TALK
            }
            kaiLineVisible = true
            delay(6_000)
            kaiLineVisible = false
            delay(4_000)
        }
    }

    fun onBuyTap() {
        pokeNonce += 1
        confirming = true
    }

    fun onKaiTap() {
        uiSound.play(UiSoundKind.SELECT)
        pokeNonce += 1
        if (confirming) return
        val s = snapshot
        val up = upgradeTarget
        val pool = UiStrings.sumikaKaiIdleLines(
            grade = Sumika.gradeIndex(s.level),
            balance = s.balance,
            payPerPlay = s.payPerPlay,
            passivePerTick = s.passivePerTick,
            upgradeCost = up?.let { Sumika.costToReach(it) },
            lang = lang,
        )
        if (pool.isEmpty()) return
        val next = pool.filter { it != kaiLine }.randomOrNull() ?: pool.random()
        forceText = next
        forceNonce += 1
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Ink.copy(alpha = 0.5f))
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() },
                onClick = {
                    if (confirming) confirming = false else onDismiss()
                }
            ),
        contentAlignment = Alignment.BottomCenter
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .lcdDoubleBorder()
                    .background(LcdBackground)
                    .clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() },
                        onClick = {}
                    )
                    .padding(horizontal = 18.dp, vertical = 14.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Box(
                    modifier = Modifier
                        .width(36.dp)
                        .height(3.dp)
                        .background(Ink.copy(alpha = 0.25f))
                )
                Spacer(Modifier.height(10.dp))
                Text(
                    text = UiStrings.sumikaTitle(lang),
                    color = Ink,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = PixelFont,
                    letterSpacing = 1.sp
                )
                Spacer(Modifier.height(10.dp))
                Canvas(modifier = Modifier.size(width = 160.dp, height = 72.dp)) {
                    drawSumikaNest(level, ink)
                }
                Spacer(Modifier.height(8.dp))
                Text(
                    text = UiStrings.sumikaLevelLine(level, lang),
                    color = Ink,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = PixelFont
                )
                Text(
                    text = if (lang == DisplayLanguage.EN) {
                        Sumika.flavorEn(level)
                    } else {
                        Sumika.flavorJp(level)
                    },
                    color = InkMuted,
                    fontSize = 11.sp,
                    fontFamily = PixelFont,
                    textAlign = TextAlign.Center,
                    lineHeight = 16.sp,
                    modifier = Modifier.padding(top = 6.dp, bottom = 12.dp)
                )

                if (level > 0) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        PayStatPlain(
                            yen = snapshot.payPerPlay,
                            label = UiStrings.sumikaStatPlayLabel(lang),
                            playBuffSound = true,
                        )
                        Box(
                            modifier = Modifier
                                .width(1.dp)
                                .height(28.dp)
                                .background(Ink.copy(alpha = 0.2f))
                        )
                        PayStatPlain(
                            yen = snapshot.passivePerTick,
                            label = UiStrings.sumikaStatTimeLabel(lang),
                            playBuffSound = false,
                        )
                    }
                    Spacer(Modifier.height(14.dp))
                }

                when {
                    confirming && upgradeTarget != null && afterUpgrade != null -> {
                        Text(
                            text = UiStrings.sumikaConfirmBody(
                                Sumika.costToReach(upgradeTarget),
                                afterUpgrade,
                                lang
                            ),
                            color = Ink,
                            fontSize = 12.sp,
                            fontFamily = PixelFont,
                            textAlign = TextAlign.Center,
                            lineHeight = 17.sp,
                            modifier = Modifier.padding(bottom = 10.dp)
                        )
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = UiStrings.sumikaConfirmCancel(lang),
                                color = Ink,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = PixelFont,
                                textAlign = TextAlign.Center,
                                modifier = Modifier
                                    .weight(1f)
                                    .lcdBorder()
                                    .uiClickable(
                                        kind = UiSoundKind.SELECT,
                                        onClick = { confirming = false }
                                    )
                                    .padding(vertical = 10.dp)
                            )
                            Text(
                                text = UiStrings.sumikaConfirmOk(lang),
                                color = LcdBackground,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = PixelFont,
                                textAlign = TextAlign.Center,
                                modifier = Modifier
                                    .weight(1f)
                                    .background(Ink)
                                    .lcdBorder()
                                    .uiClickable(
                                        kind = UiSoundKind.CONFIRM,
                                        onClick = {
                                            confirming = false
                                            onUpgrade()
                                        }
                                    )
                                    .padding(vertical = 10.dp)
                            )
                        }
                    }
                    nextGrade != null -> {
                        val cost = Sumika.costToReach(nextGrade)
                        val enabled = snapshot.canGradeUp
                        val g = Sumika.gradeIndex(nextGrade)
                        Text(
                            text = UiStrings.sumikaGradeUp(g, cost, lang),
                            color = if (enabled) LcdBackground else InkMuted,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = PixelFont,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(if (enabled) Ink else Ink.copy(alpha = 0.08f))
                                .then(if (enabled) Modifier.lcdBorder() else Modifier)
                                .then(
                                    if (enabled) {
                                        Modifier.lcdSquish(
                                            onTap = { onBuyTap() },
                                            tapSound = UiSoundKind.CONFIRM,
                                        )
                                    } else {
                                        Modifier
                                    }
                                )
                                .padding(vertical = 12.dp)
                        )
                        if (!enabled) {
                            Text(
                                text = UiStrings.sumikaFundsShort(lang),
                                color = InkMuted,
                                fontSize = 10.sp,
                                fontFamily = PixelFont,
                                modifier = Modifier.padding(top = 6.dp)
                            )
                        }
                    }
                    nextNormal != null -> {
                        val cost = Sumika.costToReach(nextNormal)
                        val enabled = snapshot.canNormalLevelUp
                        Text(
                            text = UiStrings.sumikaUpgrade(cost, lang),
                            color = if (enabled) LcdBackground else InkMuted,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = PixelFont,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(if (enabled) Ink else Ink.copy(alpha = 0.08f))
                                .then(if (enabled) Modifier.lcdBorder() else Modifier)
                                .then(
                                    if (enabled) {
                                        Modifier.lcdSquish(
                                            onTap = { onBuyTap() },
                                            tapSound = UiSoundKind.CONFIRM,
                                        )
                                    } else {
                                        Modifier
                                    }
                                )
                                .padding(vertical = 12.dp)
                        )
                        if (!enabled) {
                            Text(
                                text = UiStrings.sumikaFundsShort(lang),
                                color = InkMuted,
                                fontSize = 10.sp,
                                fontFamily = PixelFont,
                                modifier = Modifier.padding(top = 6.dp)
                            )
                        }
                    }
                    level >= Sumika.MAX_LEVEL -> {
                        Text(
                            text = UiStrings.sumikaMaxed(lang),
                            color = InkMuted,
                            fontSize = 12.sp,
                            fontFamily = PixelFont
                        )
                    }
                    else -> {
                        // グレード内キャップ: 次を見せず達成感を残す
                        Text(
                            text = UiStrings.sumikaGradeCapped(lang),
                            color = InkMuted,
                            fontSize = 12.sp,
                            fontFamily = PixelFont
                        )
                    }
                }

                if (!confirming) {
                    Spacer(Modifier.height(10.dp))
                    Text(
                        text = UiStrings.relicClose(lang),
                        color = InkMuted,
                        fontSize = 12.sp,
                        fontFamily = PixelFont,
                        modifier = Modifier
                            .uiClickable(kind = UiSoundKind.SELECT, onClick = onDismiss)
                            .padding(horizontal = 14.dp, vertical = 6.dp)
                    )
                }
                Spacer(Modifier.height(2.dp))
            }

            // 前面: カイ＋セリフ。「カイのすみか」タイトルより上に載せ、タップで次セリフ
            val kaiFaceDp = 88.dp
            Row(
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .offset(x = 10.dp, y = (-96).dp)
                    .zIndex(5f)
                    .widthIn(max = 300.dp)
                    .padding(start = 4.dp, end = 12.dp, top = 8.dp, bottom = 12.dp)
                    .clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() },
                        onClick = { onKaiTap() }
                    )
            ) {
                Box(
                    modifier = Modifier
                        .size(kaiFaceDp)
                        .graphicsLayer {
                            scaleX = -kaiScaleX
                            scaleY = kaiScaleY
                            transformOrigin = androidx.compose.ui.graphics.TransformOrigin(0.5f, 1f)
                        }
                ) {
                    GuideBuddyFace(mood = kaiMood, size = kaiFaceDp)
                }
                if (kaiLineVisible && kaiLine.isNotEmpty()) {
                    Text(
                        text = kaiLine,
                        color = Ink,
                        fontSize = 11.sp,
                        fontFamily = PixelFont,
                        lineHeight = 15.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .offset(y = (-28).dp)
                            .lcdBorder()
                            .background(LcdBackground)
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                } else {
                    // セリフなしでもタップしやすい余白
                    Spacer(Modifier.width(72.dp).height(36.dp))
                }
            }
        }
    }
}

@Composable
private fun PayStatPlain(
    yen: Long,
    label: String,
    playBuffSound: Boolean = false,
) {
    val shown = rememberAnimatedYen(yen, durationMs = 900)
    val punch = remember { Animatable(1f) }
    val uiSound = LocalUiSound.current
    var primed by remember { mutableStateOf(false) }
    var prevYen by remember { mutableLongStateOf(yen) }
    LaunchedEffect(yen) {
        val from = prevYen
        prevYen = yen
        if (!primed) {
            primed = true
            return@LaunchedEffect
        }
        if (yen <= from) return@LaunchedEffect
        if (playBuffSound) uiSound.play(UiSoundKind.BUFF)
        punch.snapTo(1f)
        punch.animateTo(1.32f, tween(90, easing = FastOutSlowInEasing))
        punch.animateTo(0.90f, tween(70))
        punch.animateTo(1.12f, tween(90, easing = FastOutSlowInEasing))
        punch.animateTo(1f, tween(110, easing = FastOutSlowInEasing))
    }
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = "+¥%,d".fmt(shown),
            color = Ink,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = PixelFont,
            letterSpacing = 0.5.sp,
            modifier = Modifier.graphicsLayer {
                scaleX = punch.value
                scaleY = punch.value
            }
        )
        Text(
            text = label,
            color = InkMuted,
            fontSize = 10.sp,
            fontFamily = PixelFont
        )
    }
}

/**
 * 目標値が変わったとき、現表示から徐々にカウントする（シート効果用）。
 */
@Composable
fun rememberAnimatedYen(target: Long, durationMs: Int = 900): Long {
    var display by remember { mutableLongStateOf(target) }
    LaunchedEffect(target) {
        val from = display
        if (from == target) return@LaunchedEffect
        val progress = Animatable(0f)
        progress.animateTo(1f, tween(durationMs, easing = EaseInOutCubic)) {
            display = from + ((target - from).toDouble() * value).roundToLong()
        }
        display = target
    }
    return display
}

/**
 * 口座残高のカウントアップ（直線・スピード重視）。
 * @return Pair(表示額, 演出中か)
 */
@Composable
fun rememberCountingBalance(
    balance: Long,
    countFrom: Long?,
    onCountTick: (Float) -> Unit = {},
    onFinished: () -> Unit = {},
): Pair<Long, Boolean> {
    var display by remember { mutableLongStateOf(balance) }
    var counting by remember { mutableStateOf(false) }
    var lastTickBucket by remember { mutableIntStateOf(-1) }

    LaunchedEffect(balance, countFrom) {
        val from = countFrom
        if (from == null || from == balance) {
            display = balance
            counting = false
            return@LaunchedEffect
        }
        fun yenAt(p: Float): Long =
            from + ((balance - from).toDouble() * p).roundToLong()
        fun maybeTick(p: Float) {
            val bucket = (p * 20f).toInt()
            if (bucket != lastTickBucket) {
                lastTickBucket = bucket
                onCountTick(p)
            }
        }

        counting = true
        display = from
        lastTickBucket = -1
        val progress = Animatable(0f)
        // 大半は直線、終端だけわずかに減速して「カチッ」止まりを防ぐ
        progress.animateTo(
            1f,
            tween(920, easing = FastOutSlowInEasing)
        ) {
            display = yenAt(value)
            maybeTick(value)
        }
        display = balance
        maybeTick(1f)
        counting = false
        onFinished()
    }
    return display to counting
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawSumikaNest(
    level: Int,
    ink: androidx.compose.ui.graphics.Color,
) {
    val grade = Sumika.gradeIndex(level)
    val w = size.width
    val h = size.height
    when (grade) {
        0 -> {
            drawLine(
                color = ink.copy(alpha = 0.35f),
                start = Offset(w * 0.2f, h * 0.75f),
                end = Offset(w * 0.8f, h * 0.75f),
                strokeWidth = 2f
            )
        }
        1 -> {
            // わらのす（浅いボウル）
            val nest = Path().apply {
                moveTo(w * 0.18f, h * 0.55f)
                quadraticBezierTo(w * 0.5f, h * 0.28f, w * 0.82f, h * 0.55f)
                quadraticBezierTo(w * 0.78f, h * 0.88f, w * 0.5f, h * 0.92f)
                quadraticBezierTo(w * 0.22f, h * 0.88f, w * 0.18f, h * 0.55f)
                close()
            }
            drawPath(nest, ink.copy(alpha = 0.16f))
            drawPath(nest, ink, style = Stroke(2.5f))
            drawOval(ink.copy(alpha = 0.35f), Offset(w * 0.34f, h * 0.48f), Size(w * 0.32f, h * 0.22f))
        }
        2 -> {
            // ふかふかざぶとん
            drawOval(ink.copy(alpha = 0.18f), Offset(w * 0.12f, h * 0.42f), Size(w * 0.76f, h * 0.48f))
            drawOval(ink, Offset(w * 0.12f, h * 0.42f), Size(w * 0.76f, h * 0.48f), style = Stroke(2.5f))
            drawOval(ink.copy(alpha = 0.25f), Offset(w * 0.28f, h * 0.52f), Size(w * 0.44f, h * 0.22f))
        }
        3 -> {
            val cx = w * 0.5f
            val cy = h * 0.58f
            val rx = w * 0.32f
            val ry = h * 0.36f
            drawOval(ink.copy(alpha = 0.14f), Offset(cx - rx, cy - ry), Size(rx * 2f, ry * 2f))
            drawOval(ink, Offset(cx - rx, cy - ry), Size(rx * 2f, ry * 2f), style = Stroke(2.5f))
            drawOval(
                ink.copy(alpha = 0.75f),
                Offset(cx - rx * 0.28f, cy - ry * 0.1f),
                Size(rx * 0.56f, ry * 0.7f)
            )
        }
        4 -> {
            // みみや（片耳ひさし）
            drawOval(ink.copy(alpha = 0.14f), Offset(w * 0.18f, h * 0.38f), Size(w * 0.64f, h * 0.5f))
            drawOval(ink, Offset(w * 0.18f, h * 0.38f), Size(w * 0.64f, h * 0.5f), style = Stroke(2.5f))
            drawOval(ink.copy(alpha = 0.8f), Offset(w * 0.36f, h * 0.52f), Size(w * 0.28f, h * 0.28f))
            // 起立耳
            drawOval(ink.copy(alpha = 0.2f), Offset(w * 0.34f, h * 0.08f), Size(w * 0.12f, h * 0.32f))
            drawOval(ink, Offset(w * 0.34f, h * 0.08f), Size(w * 0.12f, h * 0.32f), style = Stroke(2f))
            // 倒れ耳＝ひさし
            drawOval(ink.copy(alpha = 0.2f), Offset(w * 0.55f, h * 0.28f), Size(w * 0.28f, h * 0.14f))
            drawOval(ink, Offset(w * 0.55f, h * 0.28f), Size(w * 0.28f, h * 0.14f), style = Stroke(2f))
        }
        else -> {
            // ヘッドセットハウス（バンド＋右カップ＋マイク＋入口）
            drawOval(ink.copy(alpha = 0.14f), Offset(w * 0.22f, h * 0.4f), Size(w * 0.56f, h * 0.48f))
            drawOval(ink, Offset(w * 0.22f, h * 0.4f), Size(w * 0.56f, h * 0.48f), style = Stroke(2.5f))
            drawOval(ink.copy(alpha = 0.85f), Offset(w * 0.36f, h * 0.55f), Size(w * 0.28f, h * 0.26f))
            // ヘッドバンド
            drawArc(
                color = ink,
                startAngle = 200f,
                sweepAngle = 140f,
                useCenter = false,
                topLeft = Offset(w * 0.2f, h * 0.12f),
                size = Size(w * 0.6f, h * 0.4f),
                style = Stroke(3f)
            )
            // 右イヤーカップ
            drawOval(ink.copy(alpha = 0.25f), Offset(w * 0.68f, h * 0.38f), Size(w * 0.18f, h * 0.28f))
            drawOval(ink, Offset(w * 0.68f, h * 0.38f), Size(w * 0.18f, h * 0.28f), style = Stroke(2f))
            // ブームマイク → 入口
            drawLine(ink, Offset(w * 0.72f, h * 0.58f), Offset(w * 0.55f, h * 0.68f), 2f)
            drawOval(ink, Offset(w * 0.5f, h * 0.64f), Size(w * 0.08f, h * 0.08f))
        }
    }
}
