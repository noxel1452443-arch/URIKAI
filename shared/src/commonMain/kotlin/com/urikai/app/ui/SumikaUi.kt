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
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.layout.wrapContentSize
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.urikai.app.game.DisplayLanguage
import com.urikai.app.game.Sumika
import com.urikai.app.game.SumikaCostReveal
import com.urikai.app.game.SumikaSnapshot
import com.urikai.app.game.UiStrings
import com.urikai.app.game.fmt
import com.urikai.app.res.Res
import com.urikai.app.res.allDrawableResources
import com.urikai.app.ui.theme.Ink
import com.urikai.app.ui.theme.InkMuted
import com.urikai.app.ui.theme.LcdBackground
import com.urikai.app.ui.theme.LocalDisplayLanguage
import com.urikai.app.ui.theme.LocalEnableAdFeatures
import com.urikai.app.ui.theme.PixelFont
import kotlin.math.roundToInt
import kotlin.math.roundToLong
import kotlin.random.Random
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.imageResource

/** SumikaHomeSpot の親 Box 下端からの余白（GameScreen と一致）。 */
internal val SumikaHomeBottomPad = 8.dp
private val SumikaHomeColPadV = 4.dp
private val SumikaHomeColPadH = 6.dp

/** 家具上端からポップ下端までのすきま（大きい家具ほど少し広げる）。 */
private fun sumikaPayPopGapDp(grade: Int): Int = when (grade) {
    in 4..5 -> 52
    in 2..3 -> 46
    else -> 40
}

/** ホーム表示の基準サイズ（g1）。g2/g3=1.5倍、g4/g5=2倍。 */
internal fun sumikaHomeArtSize(grade: Int): Pair<Dp, Dp> {
    val baseW = 88.dp
    val baseH = 44.dp
    val scale = when (grade) {
        in 4..5 -> 2f
        in 2..3 -> 1.5f
        else -> 1f
    }
    return (baseW * scale) to (baseH * scale)
}

/** 巣アートの上端・下端 Y（親 Box 座標）。 */
internal fun sumikaNestArtYRangePx(
    grade: Int,
    parentH: Float,
    density: Density,
): Pair<Float, Float> = with(density) {
    val (_, artH) = sumikaHomeArtSize(grade.coerceIn(1, 5))
    val nestH = artH.toPx()
    val bottomPad = SumikaHomeBottomPad.toPx() + SumikaHomeColPadV.toPx()
    val nestArtBottom = parentH - bottomPad
    val nestArtTop = nestArtBottom - nestH
    nestArtTop to nestArtBottom
}

/**
 * ホーム親 Box 座標でのカイ top-left（dp家具基準・画面サイズ非依存）。
 * [seatFromTop] = 家具上端から足元までの割合（0=屋根、1=床）。
 * [xBias] = -1…1 で左右オフセット（サイド待機用）。
 */
internal fun sumikaOccupyKaiTopLeftPx(
    grade: Int,
    parentW: Float,
    parentH: Float,
    kaiPx: Float,
    density: Density,
    seatFromTop: Float,
    xBias: Float = 0f,
): Pair<Float, Float> = with(density) {
    val (artW, _) = sumikaHomeArtSize(grade.coerceIn(1, 5))
    val nestW = artW.toPx()
    val (nestArtTop, nestArtBottom) = sumikaNestArtYRangePx(grade, parentH, density)
    val nestH = nestArtBottom - nestArtTop
    val feetY = nestArtTop + nestH * seatFromTop.coerceIn(0.05f, 0.98f)
    val nestCx = parentW * 0.5f
    val bias = xBias.coerceIn(-1f, 1f)
    val x = if (bias == 0f) {
        (nestCx - kaiPx * 0.5f).roundToInt().toFloat()
    } else {
        // 横すれすれ: 巣の外縁＋カイ半身ぶん
        val clear = nestW * 0.5f + kaiPx * 0.55f
        val targetCx = nestCx + clear * bias
        (targetCx - kaiPx * 0.5f).roundToInt().toFloat()
    }.coerceIn(0f, (parentW - kaiPx).coerceAtLeast(0f))
    val y = (feetY - kaiPx).roundToInt().toFloat()
    x to y
}

/**
 * g1–g3: 家具に深く沈む。
 * g4–g5: ドーム入口（穴）の高さ。大きすぎると床より下に座ってしまう。
 */
/**
 * 巣上端からの足元位置。大きいほど下（画面では下方向）。
 * g1–g3 は家具に深く座る。浅くしすぎると「巣の上に浮く」見え方になる。
 */
internal fun sumikaOccupySeatFromTop(grade: Int): Float = when (grade) {
    1 -> 0.78f
    2 -> 0.72f
    3 -> 0.70f
    4 -> 0.62f
    5 -> 0.64f
    else -> 0.72f
}

/** 巣中心までの距離判定用（カイ足元付近）。 */
internal fun sumikaNestCenterPx(
    grade: Int,
    parentW: Float,
    parentH: Float,
    density: Density,
): Pair<Float, Float> = with(density) {
    val (artW, artH) = sumikaHomeArtSize(grade.coerceIn(1, 5))
    val bottomPad = SumikaHomeBottomPad.toPx() + SumikaHomeColPadV.toPx()
    val nestArtBottom = parentH - bottomPad
    val nestArtTop = nestArtBottom - artH.toPx()
    val cx = parentW * 0.5f
    val cy = (nestArtTop + nestArtBottom) * 0.5f
    cx to cy
}

/** 給料ポップをカイより上に出すための追加ギャップ（占用中）。 */
internal fun sumikaPayPopOccupyExtraDp(grade: Int): Int = when (grade) {
    in 4..5 -> 36
    in 2..3 -> 40
    else -> 44
}

@Composable
fun SumikaNestArt(
    level: Int,
    modifier: Modifier = Modifier,
    width: Dp? = null,
    height: Dp? = null,
    showInside: Boolean = false,
) {
    val grade = Sumika.gradeIndex(level)
    val (autoW, autoH) = sumikaHomeArtSize(grade)
    val w = width ?: autoW
    val h = height ?: autoH
    val key = when {
        showInside && grade >= 4 -> "sumika_g${grade}_inside"
        grade in 1..5 -> "sumika_g$grade"
        else -> null
    }
    val res = key?.let { Res.allDrawableResources[it] }
    if (res != null) {
        Image(
            bitmap = imageResource(res),
            contentDescription = null,
            contentScale = ContentScale.Fit,
            filterQuality = FilterQuality.None,
            modifier = modifier.size(width = w, height = h)
        )
    } else {
        val ink = Ink
        Canvas(modifier = modifier.size(width = w, height = h)) {
            drawSumikaNest(level, ink)
        }
    }
}

/**
 * ホームのすみか。本体タップ→詳細／金額ポップタップ→受取。
 */
@Composable
fun SumikaHomeSpot(
    level: Int,
    pendingYen: Long,
    onOpenSheet: () -> Unit,
    onCollect: () -> Unit,
    /** false なら家具のみ（給料ポップは親がカイより前面に描く）。 */
    embedPayPop: Boolean = true,
    /** カイ占用中はポップをさらに上へ。 */
    kaiOccupying: Boolean = false,
    modifier: Modifier = Modifier,
) {
    var flyAmount by remember { mutableStateOf<Long?>(null) }
    var popGone by remember { mutableStateOf(false) }
    val showPop = embedPayPop && pendingYen > 0L && !popGone && flyAmount == null
    val grade = Sumika.gradeIndex(level)
    val popGap = sumikaPayPopGapDp(grade) +
        if (kaiOccupying) sumikaPayPopOccupyExtraDp(grade) else 0

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
                .padding(horizontal = SumikaHomeColPadH, vertical = SumikaHomeColPadV)
        ) {
            SumikaNestArt(level = level)
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
                    .offset(y = (-popGap).dp)
            )
        }
        if (embedPayPop) {
            flyAmount?.let { amt ->
                CollectFlyBurst(
                    amount = amt,
                    onFinished = {
                        onCollect()
                        flyAmount = null
                    },
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .offset(y = (-popGap).dp)
                )
            }
        }
    }
}

/** カイより前面に重ねる給料ポップ（HomeSumikaAndKai 用）。 */
@Composable
fun BoxScope.SumikaHomePayOverlay(
    level: Int,
    pendingYen: Long,
    onCollect: () -> Unit,
    kaiOccupying: Boolean,
    payDoubled: Boolean = false,
    canOfferDouble: Boolean = false,
    adsRemoved: Boolean = false,
    rewardBusy: Boolean = false,
    shouldOfferDoubleChoice: () -> Boolean = { false },
    onMarkDoubleChoiceShown: () -> Unit = {},
    onCollectDoubledViaAd: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    if (pendingYen <= 0L) return
    var flyAmount by remember { mutableStateOf<Long?>(null) }
    var popGone by remember { mutableStateOf(false) }
    var showChoice by remember { mutableStateOf(false) }
    /** 選択UIを外側タップで閉じただけ → 次の吹き出しタップでは必ず再提示 */
    var choiceCancelledPending by remember { mutableStateOf(false) }
    LaunchedEffect(pendingYen) {
        if (pendingYen <= 0L) {
            popGone = false
            flyAmount = null
            showChoice = false
            choiceCancelledPending = false
        }
    }
    val grade = Sumika.gradeIndex(level)
    val popGap = sumikaPayPopGapDp(grade) +
        if (kaiOccupying) sumikaPayPopOccupyExtraDp(grade) else 0
    val showPop = !popGone && flyAmount == null
    val lang = LocalDisplayLanguage.current
    Box(
        modifier = modifier
            .align(Alignment.BottomCenter)
            .padding(bottom = SumikaHomeBottomPad)
            .zIndex(2f),
        contentAlignment = Alignment.BottomCenter
    ) {
        val (w, h) = sumikaHomeArtSize(grade)
        Spacer(
            Modifier
                .size(width = w + SumikaHomeColPadH * 2, height = h + SumikaHomeColPadV * 2)
        )
        if (showPop && !showChoice) {
            SumikaPayPop(
                amount = pendingYen,
                caption = when {
                    payDoubled -> UiStrings.sumikaPayDoubledTag(lang)
                    lang == DisplayLanguage.EN -> "PAY"
                    else -> "お給料"
                },
                onClick = {
                    if (rewardBusy) return@SumikaPayPop
                    val offerChoice = canOfferDouble &&
                        (choiceCancelledPending || shouldOfferDoubleChoice())
                    if (offerChoice) {
                        choiceCancelledPending = false
                        showChoice = true
                    } else {
                        flyAmount = pendingYen
                        popGone = true
                    }
                },
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .offset(y = (-popGap).dp)
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
                    .offset(y = (-popGap).dp)
            )
        }
    }
    if (showChoice) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .zIndex(12f)
                .background(Ink.copy(alpha = 0.4f))
                .clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() },
                    onClick = {
                        showChoice = false
                        choiceCancelledPending = true
                    }
                ),
            contentAlignment = Alignment.Center
        ) {
            SumikaPayChoiceCard(
                normalAmount = pendingYen,
                doubleAmount = pendingYen * 2L,
                adsRemoved = adsRemoved,
                busy = rewardBusy,
                onCollectNormal = {
                    onMarkDoubleChoiceShown()
                    choiceCancelledPending = false
                    showChoice = false
                    flyAmount = pendingYen
                    popGone = true
                },
                onCollectDouble = {
                    onMarkDoubleChoiceShown()
                    choiceCancelledPending = false
                    showChoice = false
                    onCollectDoubledViaAd()
                },
                modifier = Modifier.clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() },
                    onClick = {}
                )
            )
        }
    }
}

/**
 * 入室中の内観のぞき穴（巣レイアウトの外に置くこと — 上ずれ防止）。
 * 背景は g4/g5 の内観アセット、カイは g1–g3 と同じ sit/sleep ポーズ絵。
 * [speechLine] があるときは穴の左上・前面に吹き出し。
 */
@Composable
fun SumikaInsidePeekOverlay(
    level: Int,
    kaiPose: GuideBuddyPose?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    speechLine: String? = null,
) {
    // HomeKaiResident の kaiDp と同じ基準。sit/sleep の倍率・オフセットも揃える。
    val kaiDp = 64.dp
    val hole = 136.dp
    val pose = kaiPose ?: GuideBuddyPose.SIT
    val grade = Sumika.gradeIndex(level).coerceIn(4, 5)
    val insideRes = Res.allDrawableResources["sumika_g${grade}_inside"]

    Box(modifier = modifier.size(hole)) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(CircleShape)
                .background(LcdBackground)
                .border(width = 2.dp, color = Ink, shape = CircleShape)
                .uiClickable(kind = UiSoundKind.SELECT, onClick = onClick),
            contentAlignment = Alignment.Center
        ) {
            if (insideRes != null) {
                Image(
                    bitmap = imageResource(insideRes),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    filterQuality = FilterQuality.None,
                    modifier = Modifier.fillMaxSize()
                )
            }
            when (pose) {
                GuideBuddyPose.SIT -> Box(Modifier.offset(x = 2.dp, y = 3.dp)) {
                    GuideBuddyPose(pose = GuideBuddyPose.SIT, size = kaiDp * 0.90f)
                }
                GuideBuddyPose.SLEEP -> Box(Modifier.offset(y = 5.dp)) {
                    GuideBuddyPose(pose = GuideBuddyPose.SLEEP, size = kaiDp * 1.06f)
                }
                else -> GuideBuddyPose(pose = pose, size = kaiDp)
            }
        }
        if (!speechLine.isNullOrBlank()) {
            Text(
                text = speechLine,
                color = Ink,
                fontSize = 11.sp,
                fontFamily = PixelFont,
                lineHeight = 14.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 3,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .offset(x = (-10).dp, y = (-40).dp)
                    .zIndex(8f)
                    .widthIn(max = 140.dp)
                    .wrapContentSize(unbounded = true)
                    .lcdDoubleBorder()
                    .background(LcdBackground)
                    .padding(horizontal = 8.dp, vertical = 5.dp)
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
        SumikaNestArt(
            level = Sumika.startLevelForGrade(grade),
            width = 96.dp,
            height = 48.dp
        )
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
            SumikaNestArt(
                level = Sumika.startLevelForGrade(grade),
                width = 140.dp,
                height = 70.dp
            )
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
    showTutorial: Boolean = false,
    onDismissTutorial: () -> Unit = {},
    adsRemoved: Boolean = false,
    rewardBusy: Boolean = false,
    costReveal: SumikaCostReveal? = null,
    onRequestDiscount: () -> Unit = {},
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
    var confirmDiscount by remember { mutableStateOf(false) }
    val upgradeCost = snapshot.nextCost
    val displayCost = rememberCountdownYen(
        from = costReveal?.fromCost ?: upgradeCost,
        to = costReveal?.toCost ?: upgradeCost,
        active = costReveal != null,
        durationMs = 1_050,
    )
    // 取り消し線は常に定価（スタック適用前）。演出中／適用後に右の数字だけ下がる。
    val strikeBase = snapshot.nextCostBase.takeIf { it > 0L } ?: upgradeCost
    val showStrike = costReveal != null || snapshot.upgradeDiscountStacks > 0
    val afterUpgrade = upgradeTarget?.let { snapshot.balance - upgradeCost }
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

    LaunchedEffect(confirming, forceNonce, lang, showTutorial) {
        if (showTutorial) {
            kaiLineVisible = false
            kaiMood = GuideBuddyMood.SMILE
            return@LaunchedEffect
        }
        val after = latestAfterUpgrade.value
        if (confirming && after != null) {
            kaiLine = UiStrings.sumikaKaiConfirmLine(latestLang.value)
            kaiMood = GuideBuddyMood.THINK
            kaiLineVisible = true
            return@LaunchedEffect
        }
        val forced = forceText
        if (forceNonce > 0 && forced != null) {
            kaiLine = forced
            kaiMood = listOf(
                GuideBuddyMood.WINK,
                GuideBuddyMood.SMILE,
                GuideBuddyMood.TALK,
            ).random()
            kaiLineVisible = true
            delay(4_000)
            kaiLineVisible = false
            kaiMood = GuideBuddyMood.SMILE
            delay(4_000)
        } else {
            kaiLineVisible = false
            kaiMood = GuideBuddyMood.SMILE
            delay(2_000)
        }
        val speakMoods = listOf(
            GuideBuddyMood.TALK,
            GuideBuddyMood.SMILE,
            GuideBuddyMood.WINK,
            GuideBuddyMood.THINK,
        )
        while (true) {
            val s = latestSnapshot.value
            val up = latestUpgradeTarget.value
            val pool = UiStrings.sumikaKaiIdleLines(
                grade = Sumika.gradeIndex(s.level),
                balance = s.balance,
                payPerPlay = s.payPerPlay,
                passivePerTick = s.passivePerTick,
                upgradeCost = s.nextCost.takeIf { it > 0L },
                lang = latestLang.value,
            )
            if (pool.isEmpty()) {
                kaiMood = GuideBuddyMood.SMILE
                delay(4_000)
                continue
            }
            kaiLine = pool[Random.nextInt(pool.size)]
            kaiMood = when {
                up != null && s.balance < s.nextCost * 3L -> GuideBuddyMood.THINK
                else -> speakMoods.random()
            }
            kaiLineVisible = true
            delay(6_000)
            kaiLineVisible = false
            // 黙っているあいだは別表情
            kaiMood = listOf(GuideBuddyMood.SMILE, GuideBuddyMood.WINK, GuideBuddyMood.THINK).random()
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
            upgradeCost = s.nextCost.takeIf { it > 0L },
            lang = lang,
        )
        if (pool.isEmpty()) return
        val next = pool.filter { it != kaiLine }.randomOrNull() ?: pool.random()
        forceText = next
        forceNonce += 1
        kaiMood = listOf(
            GuideBuddyMood.TALK,
            GuideBuddyMood.WINK,
            GuideBuddyMood.SMILE,
            GuideBuddyMood.THINK,
        ).random()
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Ink.copy(alpha = 0.5f))
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() },
                onClick = {
                    if (showTutorial) return@clickable
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
                run {
                    val g = Sumika.gradeIndex(level)
                    val (hw, hh) = sumikaHomeArtSize(g)
                    SumikaNestArt(
                        level = level,
                        width = hw * 1.2f,
                        height = hh * 1.2f
                    )
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
                        modifier = Modifier
                            .fillMaxWidth()
                            .guideSpotlightTarget(GuideSpotlightTarget.SUMIKA_PAY_STATS),
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
                                upgradeCost,
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
                        val enabled = snapshot.canGradeUp && !showTutorial && costReveal == null
                        val g = Sumika.gradeIndex(nextGrade)
                        SumikaUpgradeCostButton(
                            prefix = UiStrings.sumikaGradeUpPrefix(g, lang),
                            strikeBase = strikeBase,
                            displayCost = displayCost,
                            showStrike = showStrike,
                            enabled = enabled,
                            onTap = { onBuyTap() },
                            modifier = Modifier.guideSpotlightTarget(GuideSpotlightTarget.SUMIKA_UPGRADE),
                        )
                        SumikaDiscountOfferRow(
                            snapshot = snapshot,
                            adsRemoved = adsRemoved,
                            rewardBusy = rewardBusy || costReveal != null,
                            showTutorial = showTutorial,
                            onRequestDiscount = { confirmDiscount = true },
                        )
                        if (!enabled && costReveal == null) {
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
                        val enabled = snapshot.canNormalLevelUp && !showTutorial && costReveal == null
                        SumikaUpgradeCostButton(
                            prefix = UiStrings.sumikaUpgradePrefix(lang),
                            strikeBase = strikeBase,
                            displayCost = displayCost,
                            showStrike = showStrike,
                            enabled = enabled,
                            onTap = { onBuyTap() },
                            modifier = Modifier.guideSpotlightTarget(GuideSpotlightTarget.SUMIKA_UPGRADE),
                        )
                        SumikaDiscountOfferRow(
                            snapshot = snapshot,
                            adsRemoved = adsRemoved,
                            rewardBusy = rewardBusy || costReveal != null,
                            showTutorial = showTutorial,
                            onRequestDiscount = { confirmDiscount = true },
                        )
                        if (!enabled && costReveal == null) {
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
            if (!showTutorial) {
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
        if (confirmDiscount && !showTutorial) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .zIndex(18f)
                    .background(Ink.copy(alpha = 0.35f))
                    .clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() },
                        onClick = { confirmDiscount = false }
                    ),
                contentAlignment = Alignment.Center
            ) {
                RewardConfirmCard(
                    body = UiStrings.sumikaDiscountConfirmBody(adsRemoved, lang),
                    onConfirm = {
                        confirmDiscount = false
                        onRequestDiscount()
                    },
                    onCancel = { confirmDiscount = false },
                    modifier = Modifier.clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() },
                        onClick = {}
                    )
                )
            }
        }
        if (showTutorial) {
            SumikaSheetTutorialOverlay(
                onDismiss = onDismissTutorial,
                modifier = Modifier
                    .fillMaxSize()
                    .zIndex(20f)
            )
        }
    }
}

@Composable
private fun SumikaUpgradeCostButton(
    prefix: String,
    strikeBase: Long,
    displayCost: Long,
    showStrike: Boolean,
    enabled: Boolean,
    onTap: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val label = if (showStrike && strikeBase != displayCost) {
        buildAnnotatedString {
            append(prefix)
            append("  ")
            withStyle(
                SpanStyle(
                    color = if (enabled) LcdBackground.copy(alpha = 0.55f) else InkMuted,
                    textDecoration = TextDecoration.LineThrough,
                    fontWeight = FontWeight.Normal,
                )
            ) {
                append("¥%,d".fmt(strikeBase))
            }
            append("  ")
            withStyle(
                SpanStyle(
                    color = if (enabled) LcdBackground else InkMuted,
                    fontWeight = FontWeight.Bold,
                )
            ) {
                append("¥%,d".fmt(displayCost))
            }
        }
    } else {
        buildAnnotatedString {
            append(prefix)
            append("  ")
            append("¥%,d".fmt(displayCost))
        }
    }
    Text(
        text = label,
        color = if (enabled) LcdBackground else InkMuted,
        fontSize = 14.sp,
        fontWeight = FontWeight.Bold,
        fontFamily = PixelFont,
        textAlign = TextAlign.Center,
        modifier = modifier
            .fillMaxWidth()
            .background(if (enabled) Ink else Ink.copy(alpha = 0.08f))
            .then(if (enabled) Modifier.lcdBorder() else Modifier)
            .then(
                if (enabled) {
                    Modifier.lcdSquish(onTap = onTap, tapSound = UiSoundKind.CONFIRM)
                } else {
                    Modifier
                }
            )
            .padding(vertical = 12.dp)
    )
}

@Composable
private fun SumikaDiscountOfferRow(
    snapshot: SumikaSnapshot,
    adsRemoved: Boolean,
    rewardBusy: Boolean,
    showTutorial: Boolean,
    onRequestDiscount: () -> Unit,
) {
    if (showTutorial) return
    if (!LocalEnableAdFeatures.current) return
    // 日次上限到達時は非表示（ヒント文言も出さない）
    if (!snapshot.canOfferUpgradeDiscount) return
    val lang = LocalDisplayLanguage.current
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        RewardShimmerButton(
            label = UiStrings.sumikaDiscountOffer(adsRemoved, lang),
            enabled = !rewardBusy,
            onClick = onRequestDiscount,
        )
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

/** 半額反映前の明示カウントダウン（from → to）。 */
@Composable
fun rememberCountdownYen(
    from: Long,
    to: Long,
    active: Boolean,
    durationMs: Int = 1_050,
): Long {
    var display by remember(active, from, to) {
        mutableLongStateOf(if (active) from else to)
    }
    LaunchedEffect(active, from, to) {
        if (!active) {
            display = to
            return@LaunchedEffect
        }
        display = from
        if (from == to) return@LaunchedEffect
        val progress = Animatable(0f)
        progress.animateTo(1f, tween(durationMs, easing = EaseInOutCubic)) {
            display = from + ((to - from).toDouble() * value).roundToLong()
        }
        display = to
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
            // ほしぐさのねどこ
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
            // あさのしきぬの＋小まくら
            drawRect(ink.copy(alpha = 0.14f), Offset(w * 0.18f, h * 0.58f), Size(w * 0.64f, h * 0.22f))
            drawRect(ink, Offset(w * 0.18f, h * 0.58f), Size(w * 0.64f, h * 0.22f), style = Stroke(2.5f))
            drawOval(ink.copy(alpha = 0.2f), Offset(w * 0.38f, h * 0.48f), Size(w * 0.24f, h * 0.16f))
            drawOval(ink, Offset(w * 0.38f, h * 0.48f), Size(w * 0.24f, h * 0.16f), style = Stroke(2f))
        }
        3 -> {
            // ふかふかざぶとん＋立てかけまくら
            drawOval(ink.copy(alpha = 0.18f), Offset(w * 0.12f, h * 0.42f), Size(w * 0.76f, h * 0.48f))
            drawOval(ink, Offset(w * 0.12f, h * 0.42f), Size(w * 0.76f, h * 0.48f), style = Stroke(2.5f))
            drawOval(ink.copy(alpha = 0.25f), Offset(w * 0.28f, h * 0.52f), Size(w * 0.44f, h * 0.22f))
            drawOval(ink.copy(alpha = 0.22f), Offset(w * 0.42f, h * 0.28f), Size(w * 0.2f, h * 0.28f))
            drawOval(ink, Offset(w * 0.42f, h * 0.28f), Size(w * 0.2f, h * 0.28f), style = Stroke(2f))
        }
        4 -> {
            // うさぎドーム（両耳立ち）
            drawOval(ink.copy(alpha = 0.14f), Offset(w * 0.18f, h * 0.38f), Size(w * 0.64f, h * 0.5f))
            drawOval(ink, Offset(w * 0.18f, h * 0.38f), Size(w * 0.64f, h * 0.5f), style = Stroke(2.5f))
            drawOval(ink.copy(alpha = 0.8f), Offset(w * 0.36f, h * 0.52f), Size(w * 0.28f, h * 0.28f))
            drawOval(ink.copy(alpha = 0.2f), Offset(w * 0.32f, h * 0.06f), Size(w * 0.12f, h * 0.34f))
            drawOval(ink, Offset(w * 0.32f, h * 0.06f), Size(w * 0.12f, h * 0.34f), style = Stroke(2f))
            drawOval(ink.copy(alpha = 0.2f), Offset(w * 0.56f, h * 0.06f), Size(w * 0.12f, h * 0.34f))
            drawOval(ink, Offset(w * 0.56f, h * 0.06f), Size(w * 0.12f, h * 0.34f), style = Stroke(2f))
        }
        else -> {
            // 雪魔女の部屋（片耳倒し＋帽子＋足元残雪）
            drawOval(ink.copy(alpha = 0.14f), Offset(w * 0.22f, h * 0.42f), Size(w * 0.56f, h * 0.42f))
            drawOval(ink, Offset(w * 0.22f, h * 0.42f), Size(w * 0.56f, h * 0.42f), style = Stroke(2.5f))
            drawOval(ink.copy(alpha = 0.85f), Offset(w * 0.38f, h * 0.52f), Size(w * 0.24f, h * 0.22f))
            drawOval(ink.copy(alpha = 0.2f), Offset(w * 0.34f, h * 0.18f), Size(w * 0.1f, h * 0.26f))
            drawOval(ink, Offset(w * 0.34f, h * 0.18f), Size(w * 0.1f, h * 0.26f), style = Stroke(2f))
            drawOval(ink.copy(alpha = 0.2f), Offset(w * 0.54f, h * 0.36f), Size(w * 0.22f, h * 0.12f))
            drawOval(ink, Offset(w * 0.54f, h * 0.36f), Size(w * 0.22f, h * 0.12f), style = Stroke(2f))
            val hat = Path().apply {
                moveTo(w * 0.5f, h * 0.02f)
                lineTo(w * 0.74f, h * 0.34f)
                lineTo(w * 0.26f, h * 0.34f)
                close()
            }
            drawPath(hat, ink.copy(alpha = 0.28f))
            drawPath(hat, ink, style = Stroke(2f))
            drawOval(ink.copy(alpha = 0.25f), Offset(w * 0.16f, h * 0.3f), Size(w * 0.68f, h * 0.12f))
            drawOval(ink, Offset(w * 0.16f, h * 0.3f), Size(w * 0.68f, h * 0.12f), style = Stroke(2f))
            drawOval(ink.copy(alpha = 0.2f), Offset(w * 0.2f, h * 0.78f), Size(w * 0.18f, h * 0.08f))
            drawOval(ink.copy(alpha = 0.2f), Offset(w * 0.62f, h * 0.78f), Size(w * 0.18f, h * 0.08f))
        }
    }
}
