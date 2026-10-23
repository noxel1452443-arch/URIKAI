package com.urikai.app.ui

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.urikai.app.game.DisplayLanguage
import com.urikai.app.game.PlayBuffSlot
import com.urikai.app.game.RelicId
import com.urikai.app.game.RelicUpgradeGroup
import com.urikai.app.game.UiStrings
import com.urikai.app.res.Res
import com.urikai.app.res.allDrawableResources
import com.urikai.app.ui.theme.Ink
import com.urikai.app.ui.theme.InkFaint
import com.urikai.app.ui.theme.InkMuted
import com.urikai.app.ui.theme.LcdBackground
import com.urikai.app.ui.theme.LocalDisplayLanguage
import com.urikai.app.ui.theme.PixelFont
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.imageResource
import kotlin.math.hypot

/**
 * カード外寸固定の表裏切替（③ 角から虹彩ワイプ）。
 * 角タップで表裏。右下めくりは [dogEarVisible] のときだけ出す
 * （プレイ強化切手を1枚でも受け取ったあと）。
 */
@Composable
fun TitleCardFaceReveal(
    showingBack: Boolean,
    onToggleFace: () -> Unit,
    dogEarVisible: Boolean,
    highlightDogEar: Boolean,
    modifier: Modifier = Modifier,
    front: @Composable BoxScope.() -> Unit,
    back: @Composable BoxScope.() -> Unit,
) {
    val progress by animateFloatAsState(
        targetValue = if (showingBack && dogEarVisible) 1f else 0f,
        animationSpec = tween(durationMillis = 460, easing = FastOutSlowInEasing),
        label = "iris_reveal"
    )

    BoxWithConstraints(modifier = modifier) {
        val hPx = constraints.maxHeight.toFloat().coerceAtLeast(1f)
        val wPx = constraints.maxWidth.toFloat().coerceAtLeast(1f)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clipToBounds()
        ) {
            IrisFaces(
                progress = progress,
                wPx = wPx,
                hPx = hPx,
                front = front,
                back = back
            )
            if (dogEarVisible) {
                TitleCardDogEar(
                    onClick = onToggleFace,
                    highlight = highlightDogEar && !showingBack,
                    modifier = Modifier.align(Alignment.BottomEnd)
                )
            }
        }
    }
}

@Composable
private fun IrisFaces(
    progress: Float,
    wPx: Float,
    hPx: Float,
    front: @Composable BoxScope.() -> Unit,
    back: @Composable BoxScope.() -> Unit,
) {
    Box(modifier = Modifier.fillMaxSize(), content = front)
    val maxR = hypot(wPx, hPx) * 1.15f
    val shape = remember(progress, wPx, hPx) {
        object : Shape {
            override fun createOutline(
                size: Size,
                layoutDirection: LayoutDirection,
                density: Density
            ): Outline {
                val r = (progress * maxR).coerceAtLeast(0.5f)
                val cx = size.width
                val cy = size.height
                val path = Path().apply {
                    addOval(Rect(cx - r, cy - r, cx + r, cy + r))
                }
                return Outline.Generic(path)
            }
        }
    }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .clip(shape),
        content = back
    )
}

/**
 * ストックブック風の切手帳（タイトル文言なし）。
 * OFF=空マウント / ON1=1枚貼り（角度ずれ） / ON2=重ね貼り。
 */
@Composable
fun TitleCardPlayBuffBack(
    claimed: List<RelicId>,
    stagePrefs: Map<RelicUpgradeGroup, RelicId>,
    disabledGroups: Set<RelicUpgradeGroup>,
    disabledRelics: Set<RelicId>,
    onCycle: (PlayBuffSlot) -> Unit,
    modifier: Modifier = Modifier
) {
    val lang = LocalDisplayLanguage.current
    val claimedSet = claimed.toSet()
    val slots = PlayBuffSlot.entries
    var tipSlot by remember { mutableStateOf<PlayBuffSlot?>(null) }
    val ink = Ink
    val faint = InkFaint
    val muted = InkMuted
    val bg = LcdBackground

    Row(
        modifier = modifier
            .fillMaxSize()
            .background(bg)
            .padding(start = 4.dp, end = 10.dp, top = 10.dp, bottom = 8.dp)
    ) {
        BinderHoles(color = faint, modifier = Modifier.fillMaxHeight().width(14.dp))
        Spacer(Modifier.width(4.dp))
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            StockbookStrip(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                Row(
                    modifier = Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    slots.take(3).forEach { slot ->
                        StampMount(
                            slot = slot,
                            claimedSet = claimedSet,
                            stagePrefs = stagePrefs,
                            disabledGroups = disabledGroups,
                            disabledRelics = disabledRelics,
                            ink = ink,
                            muted = muted,
                            faint = faint,
                            selected = tipSlot == slot,
                            onClick = {
                                tipSlot = slot
                                if (slot.ownedIn(claimedSet).isNotEmpty()) onCycle(slot)
                            }
                        )
                    }
                }
            }
            Spacer(Modifier.height(6.dp))
            StockbookStrip(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                Row(
                    modifier = Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    slots.drop(3).forEach { slot ->
                        StampMount(
                            slot = slot,
                            claimedSet = claimedSet,
                            stagePrefs = stagePrefs,
                            disabledGroups = disabledGroups,
                            disabledRelics = disabledRelics,
                            ink = ink,
                            muted = muted,
                            faint = faint,
                            selected = tipSlot == slot,
                            onClick = {
                                tipSlot = slot
                                if (slot.ownedIn(claimedSet).isNotEmpty()) onCycle(slot)
                            }
                        )
                    }
                }
            }
            CatalogueCaption(
                tipSlot = tipSlot,
                claimedSet = claimedSet,
                stagePrefs = stagePrefs,
                disabledGroups = disabledGroups,
                disabledRelics = disabledRelics,
                lang = lang,
                muted = muted,
                faint = faint,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 6.dp)
            )
        }
    }
}

@Composable
private fun BinderHoles(color: androidx.compose.ui.graphics.Color, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val n = 5
        val r = 2.4.dp.toPx()
        val cx = size.width * 0.45f
        for (i in 0 until n) {
            val cy = size.height * (0.12f + i * 0.19f)
            drawCircle(color = color, radius = r, center = Offset(cx, cy), style = Stroke(1.4f))
            drawCircle(color = color.copy(alpha = 0.35f), radius = r * 0.35f, center = Offset(cx, cy))
        }
        drawLine(
            color = color.copy(alpha = 0.5f),
            start = Offset(size.width - 1f, size.height * 0.06f),
            end = Offset(size.width - 1f, size.height * 0.94f),
            strokeWidth = 1f
        )
    }
}

@Composable
private fun StockbookStrip(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    val faint = InkFaint
    Box(modifier = modifier) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val yTop = size.height * 0.12f
            val yBot = size.height * 0.88f
            drawLine(faint.copy(alpha = 0.55f), Offset(0f, yTop), Offset(size.width, yTop), 1f)
            drawLine(faint.copy(alpha = 0.55f), Offset(0f, yBot), Offset(size.width, yBot), 1f)
            var x = 0f
            while (x < size.width) {
                drawLine(
                    faint.copy(alpha = 0.12f),
                    Offset(x, yTop),
                    Offset(x, yBot),
                    1f
                )
                x += 6.dp.toPx()
            }
        }
        content()
    }
}

@Composable
private fun CatalogueCaption(
    tipSlot: PlayBuffSlot?,
    claimedSet: Set<RelicId>,
    stagePrefs: Map<RelicUpgradeGroup, RelicId>,
    disabledGroups: Set<RelicUpgradeGroup>,
    disabledRelics: Set<RelicId>,
    lang: DisplayLanguage,
    muted: androidx.compose.ui.graphics.Color,
    faint: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Canvas(modifier = Modifier.fillMaxWidth().height(1.dp)) {
            drawLine(faint, Offset(size.width * 0.1f, 0f), Offset(size.width * 0.9f, 0f), 1f)
        }
        Spacer(Modifier.height(4.dp))
        val text = when {
            tipSlot == null -> UiStrings.playBuffBackHint(lang)
            tipSlot.ownedIn(claimedSet).isEmpty() -> UiStrings.playBuffStampLocked(tipSlot, lang)
            else -> {
                val active = tipSlot.activeId(
                    claimedSet, stagePrefs, disabledGroups, disabledRelics
                )
                UiStrings.playBuffTip(tipSlot, active, lang)
            }
        }
        Text(
            text = text,
            color = if (tipSlot == null) faint else muted,
            fontSize = 9.sp,
            fontFamily = PixelFont,
            textAlign = TextAlign.Center,
            lineHeight = 12.sp,
            modifier = Modifier
                .fillMaxWidth()
                .height(24.dp)
        )
    }
}

/** スロットごとの擬似ランダムな貼り角度・ずれ（再コンポーズでブレない）。 */
private data class PastePose(val rot: Float, val ox: Float, val oy: Float)

private fun pastePose(slot: PlayBuffSlot, layer: Int): PastePose {
    val s = slot.ordinal * 31 + layer * 17
    val rot = when (layer) {
        0 -> -9f + (s % 11) // -9..+1
        else -> 4f + (s % 9) // +4..+12
    }
    val ox = ((s * 3) % 7) - 3f // -3..+3 dp
    val oy = ((s * 5) % 5) - 2f
    return PastePose(rot, ox, oy)
}

@Composable
private fun StampMount(
    slot: PlayBuffSlot,
    claimedSet: Set<RelicId>,
    stagePrefs: Map<RelicUpgradeGroup, RelicId>,
    disabledGroups: Set<RelicUpgradeGroup>,
    disabledRelics: Set<RelicId>,
    ink: androidx.compose.ui.graphics.Color,
    muted: androidx.compose.ui.graphics.Color,
    faint: androidx.compose.ui.graphics.Color,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val owned = slot.ownedIn(claimedSet)
    val unlocked = owned.isNotEmpty()
    val active = slot.activeId(claimedSet, stagePrefs, disabledGroups, disabledRelics)
    val on = active != null
    val tier = if (on) slot.tierOf(active) else 0

    // 重ね貼り: 下に t1、上に t2（段階2のとき）
    val underRes = if (on) stampArtRes(slot, 1) else null
    val overRes = if (on && tier >= 2) stampArtRes(slot, 2) else null
    val poseUnder = remember(slot) { pastePose(slot, 0) }
    val poseOver = remember(slot) { pastePose(slot, 1) }

    Box(
        modifier = Modifier
            .size(width = 62.dp, height = 68.dp)
            .uiClickable(kind = UiSoundKind.SELECT, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        // 空マウント（OFF / 未解禁は切手なし）
        Canvas(modifier = Modifier.fillMaxSize()) {
            val m = 4.dp.toPx()
            val mountStroke = when {
                selected -> 1.8f
                unlocked -> 1.3f
                else -> 1f
            }
            val mountColor = when {
                selected -> ink.copy(alpha = 0.45f)
                unlocked -> muted.copy(alpha = 0.55f)
                else -> faint.copy(alpha = 0.4f)
            }
            drawRect(
                color = mountColor,
                topLeft = Offset(m, m),
                size = Size(size.width - m * 2, size.height - m * 2),
                style = Stroke(width = mountStroke)
            )
            val c = 8.dp.toPx()
            val cornerInk = if (unlocked) muted else faint
            fun corner(x0: Float, y0: Float, dx: Float, dy: Float) {
                drawLine(cornerInk, Offset(x0, y0 + dy), Offset(x0, y0), 1.6f)
                drawLine(cornerInk, Offset(x0, y0), Offset(x0 + dx, y0), 1.6f)
            }
            corner(m + 1, m + 1, c, c)
            corner(size.width - m - 1, m + 1, -c, c)
            corner(m + 1, size.height - m - 1, c, -c)
            corner(size.width - m - 1, size.height - m - 1, -c, -c)
        }

        if (underRes != null) {
            PastedStamp(
                res = underRes,
                tint = if (tier >= 2) ink.copy(alpha = 0.78f) else ink,
                pose = poseUnder,
                sizeFrac = 0.88f
            )
        }
        if (overRes != null) {
            PastedStamp(
                res = overRes,
                tint = ink,
                pose = poseOver,
                sizeFrac = 0.84f
            )
        }
    }
}

@Composable
private fun PastedStamp(
    res: DrawableResource,
    tint: androidx.compose.ui.graphics.Color,
    pose: PastePose,
    sizeFrac: Float,
) {
    val paper = LcdBackground
    // 紙台を不透明で敷き、下の切手が線画の隙間から透けないようにする
    Box(
        modifier = Modifier
            .fillMaxSize(sizeFrac)
            .offset(x = pose.ox.dp, y = pose.oy.dp)
            .rotate(pose.rot),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize(0.92f)
                .background(paper)
        )
        Image(
            bitmap = imageResource(res),
            contentDescription = null,
            contentScale = ContentScale.Fit,
            filterQuality = FilterQuality.None,
            colorFilter = ColorFilter.tint(tint),
            modifier = Modifier.fillMaxSize()
        )
    }
}

private fun stampArtRes(slot: PlayBuffSlot, tier: Int): DrawableResource? {
    val t = if (tier >= 2) "t2" else "t1"
    val base = when (slot) {
        PlayBuffSlot.COOLDOWN -> "stamp_cooldown"
        PlayBuffSlot.LAG -> "stamp_lag"
        PlayBuffSlot.PNL -> "stamp_pnl"
        PlayBuffSlot.LIFE -> "stamp_life"
        PlayBuffSlot.COMBO_HEAL -> "stamp_combo"
    }
    return Res.allDrawableResources["${base}_$t"]
}

/** カード右下の紙折り返し（タップで表裏）。 */
@Composable
fun TitleCardDogEar(
    onClick: () -> Unit,
    highlight: Boolean,
    modifier: Modifier = Modifier
) {
    val ink = Ink
    val bg = LcdBackground
    val blink = rememberInfiniteTransition(label = "dog_ear")
    val alpha by blink.animateFloat(
        initialValue = 1f,
        targetValue = if (highlight) 0.4f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(700),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dog_ear_alpha"
    )
    val sound = rememberUiSound()
    Box(
        modifier = modifier
            .size(40.dp)
            .pointerInput(onClick) {
                detectTapGestures(
                    onTap = {
                        sound.play(UiSoundKind.SELECT)
                        onClick()
                    }
                )
            }
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            val path = Path().apply {
                moveTo(w * 0.12f, h)
                lineTo(w, h)
                lineTo(w, h * 0.12f)
                close()
            }
            drawPath(path, ink.copy(alpha = alpha))
            drawLine(
                color = bg,
                start = Offset(w * 0.22f, h * 0.95f),
                end = Offset(w * 0.95f, h * 0.22f),
                strokeWidth = 1.8f
            )
        }
    }
}
