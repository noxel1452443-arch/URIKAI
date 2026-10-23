package com.urikai.app.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.urikai.app.data.LifetimePnlRepository
import com.urikai.app.game.ChartLineStyle
import com.urikai.app.game.LcdThemeMode
import com.urikai.app.game.PlayBuffSlot
import com.urikai.app.game.RelicId
import com.urikai.app.game.RelicKind
import com.urikai.app.game.RelicUpgradeGroup
import com.urikai.app.game.UiStrings
import com.urikai.app.game.UnlockPlacement
import com.urikai.app.game.placement
import com.urikai.app.res.Res
import com.urikai.app.res.allDrawableResources
import com.urikai.app.ui.theme.Ink
import com.urikai.app.ui.theme.InkFaint
import com.urikai.app.ui.theme.InkMuted
import com.urikai.app.ui.theme.LcdBackground
import com.urikai.app.ui.theme.LcdInvertedText
import com.urikai.app.ui.theme.LocalDisplayLanguage
import com.urikai.app.ui.theme.PixelFont
import org.jetbrains.compose.resources.imageResource

/**
 * ホーム左下：きせかえ入口。
 * 解禁後にだけ出現。四角枠は付けず、イラストそのものをタップする。
 */
@Composable
fun StyleEntryButton(
    unlocked: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (!unlocked) return
    val res = Res.allDrawableResources["style_entry"]
    Box(
        modifier = modifier
            .size(52.dp)
            .guideSpotlightTarget(GuideSpotlightTarget.STYLE_ENTRY)
            .uiClickable(kind = UiSoundKind.SELECT, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        if (res != null) {
            Image(
                bitmap = imageResource(res),
                contentDescription = null,
                contentScale = ContentScale.Fit,
                filterQuality = FilterQuality.None,
                colorFilter = ColorFilter.tint(Ink),
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

/**
 * ホームに生えるプレイ強化。解禁されたスロットだけ並び、タップで段階／OFF。
 * （見た目は仮。モチーフ案は別途提案中）
 */
@Composable
fun HomePlayBuffRail(
    claimed: List<RelicId>,
    stagePrefs: Map<RelicUpgradeGroup, RelicId>,
    disabledGroups: Set<RelicUpgradeGroup>,
    disabledRelics: Set<RelicId>,
    onCycle: (PlayBuffSlot) -> Unit,
    modifier: Modifier = Modifier
) {
    val lang = LocalDisplayLanguage.current
    val claimedSet = claimed.toSet()
    val slots = PlayBuffSlot.entries.filter { it.ownedIn(claimedSet).isNotEmpty() }
    if (slots.isEmpty()) return

    var tipSlot by remember { mutableStateOf<PlayBuffSlot?>(null) }

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            slots.forEach { slot ->
                val owned = slot.ownedIn(claimedSet)
                val active = playBuffActiveId(slot, owned, stagePrefs, disabledGroups, disabledRelics)
                val on = active != null
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .then(if (on) Modifier.background(Ink) else Modifier.lcdBorder())
                        .uiClickable(kind = UiSoundKind.SELECT) {
                            tipSlot = slot
                            onCycle(slot)
                        }
                        .padding(5.dp),
                    contentAlignment = Alignment.Center
                ) {
                    PlayBuffGlyph(
                        slot = slot,
                        ink = if (on) LcdInvertedText else Ink,
                        tier = when (active) {
                            RelicId.COOLDOWN_50, RelicId.LAG_50, RelicId.PNL_BONUS_150 -> 2
                            RelicId.COOLDOWN_25, RelicId.LAG_25, RelicId.PNL_BONUS_120 -> 1
                            else -> if (on) 1 else 0
                        }
                    )
                }
            }
        }
        // 位置がぶれないよう説明は固定高さ
        Box(modifier = Modifier.height(24.dp)) {
            val tip = tipSlot
            if (tip != null && tip in slots) {
                val owned = tip.ownedIn(claimedSet)
                val active = playBuffActiveId(tip, owned, stagePrefs, disabledGroups, disabledRelics)
                Text(
                    text = UiStrings.playBuffTip(tip, active, lang),
                    color = InkMuted,
                    fontSize = 9.sp,
                    fontFamily = PixelFont,
                    lineHeight = 12.sp
                )
            }
        }
    }
}

private fun playBuffActiveId(
    slot: PlayBuffSlot,
    owned: List<RelicId>,
    stagePrefs: Map<RelicUpgradeGroup, RelicId>,
    disabledGroups: Set<RelicUpgradeGroup>,
    disabledRelics: Set<RelicId>,
): RelicId? {
    if (owned.isEmpty()) return null
    val group = slot.group
    if (group != null) {
        if (group in disabledGroups) return null
        val pref = stagePrefs[group]?.takeIf { it in owned }
        return pref ?: owned.maxBy { it.threshold }
    }
    val single = slot.single ?: return null
    return if (single in disabledRelics) null else single
}

@Composable
private fun PlayBuffGlyph(slot: PlayBuffSlot, ink: androidx.compose.ui.graphics.Color, tier: Int) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height
        when (slot) {
            PlayBuffSlot.COOLDOWN -> {
                drawCircle(ink, radius = w * 0.42f, center = Offset(w / 2, h / 2), style = Stroke(1.8f))
                val sweep = if (tier >= 2) 0.72f else 0.45f
                drawArc(
                    color = ink,
                    startAngle = -90f,
                    sweepAngle = 360f * sweep,
                    useCenter = false,
                    topLeft = Offset(w * 0.12f, h * 0.12f),
                    size = Size(w * 0.76f, h * 0.76f),
                    style = Stroke(2.4f)
                )
            }
            PlayBuffSlot.LAG -> {
                val layers = if (tier >= 2) 3 else 2
                for (i in 0 until layers) {
                    val x = w * (0.18f + i * 0.18f)
                    val path = Path().apply {
                        moveTo(x, h * 0.22f)
                        lineTo(x + w * 0.28f, h * 0.50f)
                        lineTo(x, h * 0.78f)
                    }
                    drawPath(path, ink, style = Stroke(1.8f))
                }
            }
            PlayBuffSlot.PNL -> {
                val cx = w / 2
                val cy = h / 2
                val r = if (tier >= 2) w * 0.42f else w * 0.34f
                val path = Path().apply {
                    moveTo(cx, cy - r)
                    lineTo(cx + r, cy)
                    lineTo(cx, cy + r)
                    lineTo(cx - r, cy)
                    close()
                }
                drawPath(path, ink, style = Stroke(1.8f))
            }
            PlayBuffSlot.LIFE -> {
                val cx = w / 2
                val cy = h / 2
                val r = w * 0.38f
                val path = Path()
                for (i in 0 until 8) {
                    val a = kotlin.math.PI * 2.0 * i / 8.0 - kotlin.math.PI / 8.0
                    val x = cx + (r * kotlin.math.cos(a)).toFloat()
                    val y = cy + (r * kotlin.math.sin(a)).toFloat()
                    if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
                }
                path.close()
                drawPath(path, ink, style = Stroke(1.8f))
                drawLine(ink, Offset(cx, cy - r * 0.35f), Offset(cx, cy + r * 0.35f), 1.6f)
                drawLine(ink, Offset(cx - r * 0.35f, cy), Offset(cx + r * 0.35f, cy), 1.6f)
            }
            PlayBuffSlot.COMBO_HEAL -> {
                for (i in 0 until 3) {
                    val y = h * (0.22f + i * 0.22f)
                    val path = Path().apply {
                        moveTo(w * 0.50f, y)
                        lineTo(w * 0.22f, y + h * 0.18f)
                        lineTo(w * 0.78f, y + h * 0.18f)
                        close()
                    }
                    drawPath(path, ink, style = Stroke(1.4f))
                }
            }
        }
    }
}

/** きせかえページ。 */
@Composable
fun StyleLookOverlay(
    claimed: List<RelicId>,
    lcdTheme: LcdThemeMode,
    chartLineStyle: ChartLineStyle,
    disabledRelics: Set<RelicId>,
    onToggleVisual: (RelicId) -> Unit,
    onToggleFx: (RelicId) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val lang = LocalDisplayLanguage.current
    // DARK（反転テーマ）は廃止のため見た目ページに出さない
    val lookItems = claimed.filter {
        it.placement() == UnlockPlacement.LOOK && it != RelicId.DARK
    }
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Ink.copy(alpha = 0.45f))
            .clickable(onClick = onDismiss),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .fillMaxSize(0.78f)
                .lcdDoubleBorder()
                .background(LcdBackground)
                .clickable(enabled = false, onClick = {})
                .padding(12.dp)
        ) {
            Text(
                text = UiStrings.stylePageTitle(lang),
                color = InkMuted,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = PixelFont,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (lookItems.isEmpty()) {
                    Text(
                        text = UiStrings.stylePageEmpty(lang),
                        color = InkMuted,
                        fontSize = 11.sp,
                        fontFamily = PixelFont
                    )
                } else {
                    lookItems.forEach { id ->
                        val isVisual = id.kind == RelicKind.VISUAL
                        val active = when (id) {
                            RelicId.PAPER -> lcdTheme == LcdThemeMode.E_PAPER
                            RelicId.DARK -> lcdTheme == LcdThemeMode.E_PAPER_INVERT
                            RelicId.SMOOTH -> chartLineStyle == ChartLineStyle.SMOOTH
                            RelicId.JUDGE, RelicId.COMBO -> id !in disabledRelics
                            else -> false
                        }
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .lcdBorder()
                                .padding(horizontal = 10.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = UiStrings.styleLookName(id, lang),
                                    color = Ink,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = PixelFont
                                )
                                Text(
                                    text = UiStrings.styleLookBlurb(id, lang),
                                    color = InkMuted,
                                    fontSize = 10.sp,
                                    fontFamily = PixelFont,
                                    lineHeight = 13.sp
                                )
                            }
                            // 状態は左の文言側で察し、右は常に「押すと起きること」
                            if (active) {
                                Text(
                                    text = UiStrings.styleOn(lang),
                                    color = LcdInvertedText,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = PixelFont,
                                    modifier = Modifier
                                        .background(Ink)
                                        .padding(horizontal = 5.dp, vertical = 2.dp)
                                )
                            }
                            Text(
                                text = if (active) {
                                    UiStrings.styleActionRemove(lang)
                                } else {
                                    UiStrings.styleActionUse(lang)
                                },
                                color = Ink,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = PixelFont,
                                modifier = Modifier
                                    .lcdBorder()
                                    .uiClickable(kind = UiSoundKind.SELECT) {
                                        if (isVisual) onToggleVisual(id) else onToggleFx(id)
                                    }
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = UiStrings.relicClose(lang),
                color = Ink,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = PixelFont,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .lcdBorder()
                    .uiClickable(kind = UiSoundKind.SELECT, onClick = onDismiss)
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            )
        }
    }
}

/** ドット列＋ Next: ¥… だけの1行。グラフ枠の中に載せる想定。 */
@Composable
fun AccountMilestoneStrip(
    peakBalance: Long,
    claimed: Set<RelicId>,
    pending: Set<RelicId>,
    modifier: Modifier = Modifier
) {
    val lang = LocalDisplayLanguage.current
    val initial = LifetimePnlRepository.INITIAL_BALANCE
    val gain = (peakBalance - initial).coerceAtLeast(0L).toInt()
    val next = RelicId.ordered.firstOrNull { it.threshold > gain }
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 6.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(2.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            RelicId.ordered.forEach { id ->
                val done = id.threshold <= gain
                val waiting = id in pending
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .then(
                            when {
                                waiting -> Modifier.background(Ink)
                                done -> Modifier.background(Ink.copy(alpha = 0.55f))
                                else -> Modifier.lcdBorder(InkFaint)
                            }
                        )
                )
            }
        }
        Text(
            text = if (next != null) {
                UiStrings.milestoneNextShort(next.threshold + initial, lang)
            } else {
                UiStrings.milestoneCompleteShort(lang)
            },
            color = InkMuted,
            fontSize = 9.sp,
            fontFamily = PixelFont,
            maxLines = 1
        )
    }
}
