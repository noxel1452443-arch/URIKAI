package com.urikai.app.ui

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.urikai.app.game.UiStrings
import com.urikai.app.ui.theme.Ink
import com.urikai.app.ui.theme.InkMuted
import com.urikai.app.ui.theme.LcdBackground
import com.urikai.app.ui.theme.LocalDisplayLanguage
import com.urikai.app.ui.theme.PixelFont

/**
 * 広告リワード系ボタン共通。枠は控えめ、金銀の光沢が定期的に走る。
 * 押し込みは [lcdSquish]。
 */
@Composable
fun RewardShimmerButton(
    label: String,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    fontSize: androidx.compose.ui.unit.TextUnit = 11.sp,
    verticalPadding: androidx.compose.ui.unit.Dp = 5.dp,
) {
    val shimmer = rememberInfiniteTransition(label = "reward_shimmer")
    val phase by shimmer.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2_400, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "reward_shimmer_phase"
    )
    val gleamGold = Color(0xFFE8D48A)
    val gleamSilver = Color(0xFFD8DEE6)
    Box(
        modifier = modifier
            .background(LcdBackground)
            .border(1.dp, Ink.copy(alpha = 0.28f))
            .then(
                if (enabled) {
                    Modifier.lcdSquish(onTap = onClick, tapSound = UiSoundKind.CONFIRM)
                } else {
                    Modifier
                }
            )
            .drawWithContent {
                drawContent()
                if (!enabled) return@drawWithContent
                val band = size.width * 0.34f
                val travel = size.width + band * 2f
                val x = -band + travel * phase
                drawRect(
                    brush = Brush.linearGradient(
                        colorStops = arrayOf(
                            0f to Color.Transparent,
                            0.35f to gleamSilver.copy(alpha = 0.18f),
                            0.5f to gleamGold.copy(alpha = 0.42f),
                            0.65f to gleamSilver.copy(alpha = 0.18f),
                            1f to Color.Transparent,
                        ),
                        start = Offset(x, 0f),
                        end = Offset(x + band, size.height),
                    )
                )
            }
            .padding(horizontal = 10.dp, vertical = verticalPadding),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = if (enabled) Ink else InkMuted,
            fontSize = fontSize,
            fontWeight = FontWeight.Bold,
            fontFamily = PixelFont,
            textAlign = TextAlign.Center,
            maxLines = 2
        )
    }
}

/**
 * お給料受け取りの分岐カード。
 * 通常＝定額ボタン、2倍＝光沢ボタン。外側タップで閉じる（未受取のまま）。
 */
@Composable
fun SumikaPayChoiceCard(
    normalAmount: Long,
    doubleAmount: Long,
    adsRemoved: Boolean,
    busy: Boolean,
    onCollectNormal: () -> Unit,
    onCollectDouble: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val lang = LocalDisplayLanguage.current
    Column(
        modifier = modifier
            .widthIn(max = 300.dp)
            .lcdDoubleBorder()
            .background(LcdBackground)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(
            text = UiStrings.sumikaPayChoiceTitle(lang),
            color = Ink,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = PixelFont,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
        // 広告側を主ボタン（大きめ）、通常受取は控えめ
        RewardShimmerButton(
            label = UiStrings.sumikaPayCollectDouble(doubleAmount, adsRemoved, lang),
            enabled = !busy,
            onClick = onCollectDouble,
            modifier = Modifier.fillMaxWidth(),
            fontSize = 13.sp,
            verticalPadding = 10.dp,
        )
        Text(
            text = UiStrings.sumikaPayCollectNormal(normalAmount, lang),
            color = LcdBackground,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = PixelFont,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .background(Ink)
                .lcdBorder()
                .then(
                    if (!busy) {
                        Modifier.lcdSquish(onTap = onCollectNormal, tapSound = UiSoundKind.CONFIRM)
                    } else {
                        Modifier
                    }
                )
                .padding(vertical = 8.dp)
        )
    }
}

/** リワード実行前の確認カード。 */
@Composable
fun RewardConfirmCard(
    body: String,
    onConfirm: () -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val lang = LocalDisplayLanguage.current
    Column(
        modifier = modifier
            .widthIn(max = 280.dp)
            .lcdDoubleBorder()
            .background(LcdBackground)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = body,
            color = Ink,
            fontSize = 12.sp,
            fontFamily = PixelFont,
            textAlign = TextAlign.Center,
            lineHeight = 17.sp,
            modifier = Modifier.fillMaxWidth()
        )
        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = UiStrings.sumikaRewardConfirmCancel(lang),
                color = Ink,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = PixelFont,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .weight(1f)
                    .lcdBorder()
                    .lcdSquish(onTap = onCancel, tapSound = UiSoundKind.SELECT)
                    .padding(vertical = 10.dp)
            )
            Text(
                text = UiStrings.sumikaRewardConfirmOk(lang),
                color = LcdBackground,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = PixelFont,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .weight(1f)
                    .background(Ink)
                    .lcdBorder()
                    .lcdSquish(onTap = onConfirm, tapSound = UiSoundKind.CONFIRM)
                    .padding(vertical = 10.dp)
            )
        }
    }
}
