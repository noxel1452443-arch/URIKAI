package com.urikai.app.ui

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.urikai.app.game.HomeTrophies
import com.urikai.app.game.HomeTrophy
import com.urikai.app.game.HomeTrophyId
import com.urikai.app.ui.theme.Ink
import com.urikai.app.ui.theme.LcdBackground
import com.urikai.app.ui.theme.LocalDisplayLanguage
import com.urikai.app.ui.theme.PixelFont
import kotlinx.coroutines.delay
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * タイトル▼下余白の原石列。
 * 取得分だけ中央に詰めて並べ、未所持の空き枠は作らない。
 * [showAll]=true（LAB ON）なら確認用に全原石を出す。
 * タップで突破証＋フレーバーを短く表示。
 */
@Composable
fun HomeTrophyStrip(
    peakBalance: Long,
    showAll: Boolean = false,
    modifier: Modifier = Modifier,
) {
    val items = if (showAll) HomeTrophies.ALL else HomeTrophies.reached(peakBalance)
    if (items.isEmpty()) return
    val lang = LocalDisplayLanguage.current
    var tip by remember { mutableStateOf<HomeTrophy?>(null) }
    LaunchedEffect(tip) {
        val shown = tip ?: return@LaunchedEffect
        delay(2_800)
        if (tip == shown) tip = null
    }
    Box(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.BottomCenter,
    ) {
        tip?.let { t ->
            Text(
                text = t.tapLine(lang),
                color = Ink,
                fontSize = 10.sp,
                fontFamily = PixelFont,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                lineHeight = 13.sp,
                maxLines = 4,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 22.dp)
                    .zIndex(2f)
                    .widthIn(max = 220.dp)
                    .border(1.dp, Ink, RectangleShape)
                    .background(LcdBackground)
                    .padding(horizontal = 8.dp, vertical = 5.dp),
            )
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(18.dp),
            horizontalArrangement = Arrangement.spacedBy(5.dp, Alignment.CenterHorizontally),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            items.forEach { t ->
                Box(
                    modifier = Modifier
                        .size(trophyIconDp(t.id))
                        .uiClickable(kind = UiSoundKind.SELECT) { tip = t },
                    contentAlignment = Alignment.Center,
                ) {
                    HomeTrophyIcon(
                        id = t.id,
                        modifier = Modifier.fillMaxWidth().height(trophyIconDp(t.id)),
                    )
                }
            }
        }
    }
}

private fun trophyIconDp(id: HomeTrophyId) = when (id) {
    HomeTrophyId.PEARL -> 14.dp
    HomeTrophyId.COPPER, HomeTrophyId.SILVER, HomeTrophyId.GOLD -> 16.dp
    else -> 15.dp
}

@Composable
fun HomeTrophyIcon(
    id: HomeTrophyId,
    modifier: Modifier = Modifier,
) {
    val pulse = rememberInfiniteTransition(label = "stonePulse")
    val phase by pulse.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = stonePeriodMs(id), easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "stonePhase",
    )
    val palette = stonePalette(id, phase)
    Canvas(modifier = modifier) {
        when (id) {
            HomeTrophyId.COPPER,
            HomeTrophyId.SILVER,
            HomeTrophyId.GOLD,
            -> drawIngot(palette, phase)
            HomeTrophyId.PEARL -> drawPearl(palette, phase)
            else -> drawBitGem(palette, phase, id)
        }
    }
}

private fun stonePeriodMs(id: HomeTrophyId): Int = when (id) {
    HomeTrophyId.ALEXANDRITE -> 3_600
    HomeTrophyId.DIAMOND, HomeTrophyId.GRANDIDIERITE -> 2_600
    HomeTrophyId.PEARL -> 3_200
    else -> 2_900
}

/** ビット風の硬い輪郭色（LCDインク寄り）。 */
private val StoneInk = Color(0xFF2C2418)

private data class StonePalette(
    /** グラデ停止色（先頭＝ハイライト寄り）。 */
    val stops: List<Color>,
    val edge: Color,
    val spark: Color,
)

private fun stonePalette(id: HomeTrophyId, phase: Float): StonePalette {
    val a = phase * PI.toFloat() * 2f
    val wobble = 0.5f + 0.5f * sin(a)
    val wobble2 = 0.5f + 0.5f * cos(a * 0.73f + 0.4f)
    return when (id) {
        HomeTrophyId.COPPER -> StonePalette(
            stops = listOf(
                lerpColor(Color(0xFFFFD2A8), Color(0xFFFFE8C8), wobble),
                lerpColor(Color(0xFFE09050), Color(0xFFC86830), wobble2),
                lerpColor(Color(0xFFA05028), Color(0xFF7A3818), 1f - wobble),
                lerpColor(Color(0xFF5A2810), Color(0xFF3E1C0C), wobble2),
            ),
            edge = StoneInk,
            spark = Color(0xFFFFF0D8).copy(alpha = 0.55f + 0.35f * wobble),
        )
        HomeTrophyId.SILVER -> StonePalette(
            stops = listOf(
                lerpColor(Color(0xFFFFFFFF), Color(0xFFE8EEF4), wobble),
                lerpColor(Color(0xFFC8D0DA), Color(0xFFAEB8C4), wobble2),
                lerpColor(Color(0xFF88929E), Color(0xFF6A7480), 1f - wobble),
                lerpColor(Color(0xFF4A5460), Color(0xFF343C48), wobble2),
            ),
            edge = StoneInk,
            spark = Color(0xFFFFFFFF).copy(alpha = 0.50f + 0.40f * wobble),
        )
        HomeTrophyId.GOLD -> StonePalette(
            stops = listOf(
                lerpColor(Color(0xFFFFF6C8), Color(0xFFFFE08A), wobble),
                lerpColor(Color(0xFFF0C040), Color(0xFFE0A820), wobble2),
                lerpColor(Color(0xFFC88810), Color(0xFFA06808), 1f - wobble),
                lerpColor(Color(0xFF6A4808), Color(0xFF4A3004), wobble2),
            ),
            edge = StoneInk,
            spark = Color(0xFFFFF8E0).copy(alpha = 0.58f + 0.32f * wobble),
        )
        HomeTrophyId.PEARL -> StonePalette(
            stops = listOf(
                lerpColor(Color(0xFFFFFCF8), Color(0xFFFFF0E8), wobble),
                lerpColor(Color(0xFFF0E4D4), Color(0xFFE0D0C0), wobble2),
                lerpColor(Color(0xFFC8B8A4), Color(0xFFB0A090), 1f - wobble),
                lerpColor(Color(0xFF8A7A68), Color(0xFF6E6050), wobble2),
            ),
            edge = StoneInk.copy(alpha = 0.85f),
            spark = Color(0xFFFFFFFF).copy(alpha = 0.62f + 0.28f * wobble),
        )
        HomeTrophyId.DIAMOND -> StonePalette(
            stops = listOf(
                lerpColor(Color(0xFFFFFFFF), Color(0xFFE8F6FF), wobble),
                lerpColor(Color(0xFFB8DCF0), Color(0xFF88C4E8), wobble2),
                lerpColor(Color(0xFF5898C8), Color(0xFF3A78A8), 1f - wobble),
                lerpColor(Color(0xFF1E4A6A), Color(0xFF103048), wobble2),
                lerpColor(Color(0xFFE8D0F8), Color(0xFFC8E8FF), sin(a + 1.2f) * 0.5f + 0.5f),
            ),
            edge = StoneInk,
            spark = Color(0xFFFFFFFF).copy(alpha = 0.70f + 0.25f * wobble),
        )
        HomeTrophyId.GRANDIDIERITE -> StonePalette(
            stops = listOf(
                lerpColor(Color(0xFFB8F0E0), Color(0xFF88E0C8), wobble),
                lerpColor(Color(0xFF40B898), Color(0xFF2A9880), wobble2),
                lerpColor(Color(0xFF187860), Color(0xFF0E5848), 1f - wobble),
                lerpColor(Color(0xFF083828), Color(0xFF042820), wobble2),
                lerpColor(Color(0xFF68C8E0), Color(0xFF48A8C0), sin(a * 1.1f) * 0.5f + 0.5f),
            ),
            edge = StoneInk,
            spark = Color(0xFFE0FFF4).copy(alpha = 0.50f + 0.35f * wobble),
        )
        HomeTrophyId.ALEXANDRITE -> {
            val u = 0.5f + 0.5f * sin(a)
            val v = 0.5f + 0.5f * cos(a * 0.85f)
            StonePalette(
                stops = listOf(
                    lerpColor(Color(0xFFC8F0D0), Color(0xFFF0C8F0), u),
                    lerpColor(Color(0xFF48B878), Color(0xFFB868B8), v),
                    lerpColor(Color(0xFF1A6840), Color(0xFF683068), 1f - u),
                    lerpColor(Color(0xFF0C3820), Color(0xFF381838), v),
                    lerpColor(Color(0xFF88E0A8), Color(0xFFD090D0), sin(a + 0.9f) * 0.5f + 0.5f),
                ),
                edge = StoneInk,
                spark = lerpColor(Color(0xFFE8FFE8), Color(0xFFFFE8FF), u)
                    .copy(alpha = 0.55f + 0.35f * v),
            )
        }
    }
}

/** 金属系: 短いインゴット（角ばった小判）。 */
private fun DrawScope.drawIngot(p: StonePalette, phase: Float) {
    val w = size.width
    val h = size.height
    val path = Path().apply {
        moveTo(px(w, 0.22f), px(h, 0.32f))
        lineTo(px(w, 0.78f), px(h, 0.32f))
        lineTo(px(w, 0.90f), px(h, 0.48f))
        lineTo(px(w, 0.82f), px(h, 0.72f))
        lineTo(px(w, 0.18f), px(h, 0.72f))
        lineTo(px(w, 0.10f), px(h, 0.48f))
        close()
    }
    drawLivingFill(path, p, phase, nugget = true)
    drawPath(path, color = p.edge, style = Stroke(width = outlineW()))
    drawLine(
        color = p.edge.copy(alpha = 0.55f),
        start = Offset(px(w, 0.28f), px(h, 0.48f)),
        end = Offset(px(w, 0.72f), px(h, 0.48f)),
        strokeWidth = 1f,
    )
}

/** 真珠: まん丸。 */
private fun DrawScope.drawPearl(p: StonePalette, phase: Float) {
    val w = size.width
    val h = size.height
    val r = minOf(w, h) * 0.42f
    val c = Offset(w * 0.5f, h * 0.52f)
    val a = phase * PI.toFloat() * 2f
    val hx = c.x + cos(a) * r * 0.22f
    val hy = c.y + sin(a * 0.9f) * r * 0.18f - r * 0.12f
    drawCircle(
        brush = Brush.radialGradient(
            colors = p.stops,
            center = Offset(hx, hy),
            radius = r * 1.45f,
        ),
        radius = r,
        center = c,
    )
    drawCircle(
        brush = Brush.sweepGradient(
            colors = listOf(
                p.stops[1].copy(alpha = 0.0f),
                p.stops.getOrElse(2) { p.stops.last() }.copy(alpha = 0.35f),
                p.spark.copy(alpha = 0.25f),
                p.stops[1].copy(alpha = 0.0f),
            ),
            center = c,
        ),
        radius = r * 0.92f,
        center = c,
        style = Stroke(width = r * 0.18f),
    )
    drawCircle(color = p.edge, radius = r, center = c, style = Stroke(width = outlineW()))
    drawCircle(
        color = p.spark,
        radius = r * 0.16f,
        center = Offset(hx - r * 0.08f, hy - r * 0.06f),
    )
}

/** 宝石系: 角ばったビット結晶。 */
private fun DrawScope.drawBitGem(p: StonePalette, phase: Float, id: HomeTrophyId) {
    val w = size.width
    val h = size.height
    val path = when (id) {
        HomeTrophyId.DIAMOND -> Path().apply {
            moveTo(px(w, 0.50f), px(h, 0.08f))
            lineTo(px(w, 0.82f), px(h, 0.36f))
            lineTo(px(w, 0.68f), px(h, 0.88f))
            lineTo(px(w, 0.32f), px(h, 0.88f))
            lineTo(px(w, 0.18f), px(h, 0.36f))
            close()
        }
        HomeTrophyId.GRANDIDIERITE -> Path().apply {
            moveTo(px(w, 0.50f), px(h, 0.10f))
            lineTo(px(w, 0.86f), px(h, 0.40f))
            lineTo(px(w, 0.74f), px(h, 0.82f))
            lineTo(px(w, 0.26f), px(h, 0.82f))
            lineTo(px(w, 0.14f), px(h, 0.40f))
            close()
        }
        else -> Path().apply {
            moveTo(px(w, 0.50f), px(h, 0.10f))
            lineTo(px(w, 0.88f), px(h, 0.34f))
            lineTo(px(w, 0.80f), px(h, 0.72f))
            lineTo(px(w, 0.50f), px(h, 0.90f))
            lineTo(px(w, 0.20f), px(h, 0.72f))
            lineTo(px(w, 0.12f), px(h, 0.34f))
            close()
        }
    }
    drawLivingFill(path, p, phase, nugget = false)
    drawPath(path, color = p.edge, style = Stroke(width = outlineW()))
    drawLine(
        color = p.edge.copy(alpha = 0.40f),
        start = Offset(px(w, 0.50f), px(h, 0.12f)),
        end = Offset(px(w, 0.50f), px(h, 0.78f)),
        strokeWidth = 1f,
    )
    drawLine(
        color = p.edge.copy(alpha = 0.32f),
        start = Offset(px(w, 0.22f), px(h, 0.38f)),
        end = Offset(px(w, 0.78f), px(h, 0.38f)),
        strokeWidth = 1f,
    )
}

private fun DrawScope.drawLivingFill(
    path: Path,
    p: StonePalette,
    phase: Float,
    nugget: Boolean,
) {
    val w = size.width
    val h = size.height
    val a = phase * PI.toFloat() * 2f
    val lx = 0.28f + 0.44f * (0.5f + 0.5f * cos(a))
    val ly = 0.22f + 0.36f * (0.5f + 0.5f * sin(a * 0.87f + 0.3f))
    val light = Offset(w * lx, h * ly)
    clipPath(path) {
        drawRect(
            brush = Brush.radialGradient(
                colors = p.stops,
                center = light,
                radius = maxOf(w, h) * (if (nugget) 1.15f else 1.35f),
            ),
        )
        val bandAngle = a * 0.5f
        val dx = cos(bandAngle)
        val dy = sin(bandAngle)
        drawRect(
            brush = Brush.linearGradient(
                colorStops = arrayOf(
                    0.0f to Color.Transparent,
                    0.35f to p.stops.getOrElse(1) { p.stops.first() }.copy(alpha = 0.20f),
                    0.50f to p.spark.copy(alpha = 0.38f),
                    0.65f to p.stops.getOrElse(2) { p.stops.last() }.copy(alpha = 0.22f),
                    1.0f to Color.Transparent,
                ),
                start = Offset(w * (0.5f - dx), h * (0.5f - dy)),
                end = Offset(w * (0.5f + dx), h * (0.5f + dy)),
            ),
        )
        drawRect(
            brush = Brush.radialGradient(
                colors = listOf(
                    Color.Transparent,
                    p.stops.last().copy(alpha = 0.45f),
                ),
                center = Offset(w * (1f - lx * 0.7f), h * (1f - ly * 0.55f)),
                radius = maxOf(w, h) * 0.85f,
            ),
        )
        drawCircle(
            color = p.spark,
            radius = minOf(w, h) * 0.10f,
            center = Offset(light.x - w * 0.04f, light.y - h * 0.03f),
        )
        if (!nugget) {
            val facet = Path().apply {
                moveTo(px(w, 0.50f), px(h, 0.16f))
                lineTo(px(w, 0.66f), px(h, 0.34f))
                lineTo(px(w, 0.50f), px(h, 0.40f))
                lineTo(px(w, 0.38f), px(h, 0.34f))
                close()
            }
            drawPath(facet, color = p.spark.copy(alpha = 0.35f))
        }
    }
}

private fun DrawScope.outlineW(): Float = maxOf(1.2f, size.minDimension * 0.07f)

private fun px(span: Float, t: Float): Float = (span * t).toInt().toFloat()

private fun lerpColor(a: Color, b: Color, t: Float): Color {
    val u = t.coerceIn(0f, 1f)
    return Color(
        red = a.red + (b.red - a.red) * u,
        green = a.green + (b.green - a.green) * u,
        blue = a.blue + (b.blue - a.blue) * u,
        alpha = a.alpha + (b.alpha - a.alpha) * u,
    )
}
