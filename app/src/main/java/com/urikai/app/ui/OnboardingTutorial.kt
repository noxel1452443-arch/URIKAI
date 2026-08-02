package com.urikai.app.ui

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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.urikai.app.game.TutorialGuideKind
import com.urikai.app.game.UiStrings
import com.urikai.app.ui.theme.Ink
import com.urikai.app.ui.theme.InkMuted
import com.urikai.app.ui.theme.LcdBackground
import com.urikai.app.ui.theme.LocalDisplayLanguage
import com.urikai.app.ui.theme.PixelFont

/** 初回 START 後・タイマー開始前の遊び方（3枚）。 */
@Composable
fun FirstPlayTutorialOverlay(
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val lang = LocalDisplayLanguage.current
    var step by remember { mutableIntStateOf(0) }
    val lastStep = 2
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Ink.copy(alpha = 0.55f))
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() },
                onClick = {}
            ),
        contentAlignment = Alignment.Center
    ) {
        TutorialCard(
            title = UiStrings.firstPlayTutorialTitle(lang),
            body = UiStrings.firstPlayTutorialStep(step, lang),
            stepLabel = "${step + 1}/${lastStep + 1}",
            actionLabel = if (step < lastStep) {
                UiStrings.firstPlayTutorialNext(lang)
            } else {
                UiStrings.firstPlayTutorialStart(lang)
            },
            onAction = {
                if (step < lastStep) step += 1 else onDismiss()
            },
            modifier = Modifier.fillMaxWidth(0.9f)
        )
    }
}

/**
 * リザルト演出後の見方ガイド。
 * @param step 0=導入, 1=％★, 2=資産+損益, 3=売買+利子, 4=締め
 */
@Composable
fun ResultTutorialCard(
    step: Int,
    totalSteps: Int,
    onNext: () -> Unit,
    modifier: Modifier = Modifier
) {
    val lang = LocalDisplayLanguage.current
    val last = totalSteps - 1
    TutorialCard(
        title = UiStrings.resultTutorialTitle(lang),
        body = UiStrings.resultTutorialStep(step, lang),
        stepLabel = "${step + 1}/$totalSteps",
        actionLabel = if (step < last) {
            UiStrings.resultTutorialNext(lang)
        } else {
            UiStrings.resultTutorialOk(lang)
        },
        onAction = onNext,
        modifier = modifier.fillMaxWidth(0.92f)
    )
}

@Composable
internal fun TutorialCard(
    title: String,
    body: String,
    stepLabel: String,
    actionLabel: String,
    onAction: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .lcdDoubleBorder()
            .background(LcdBackground)
            .padding(horizontal = 16.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(
            text = title,
            color = Ink,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = PixelFont,
            textAlign = TextAlign.Center
        )
        Text(
            text = stepLabel,
            color = InkMuted,
            fontSize = 11.sp,
            fontFamily = PixelFont
        )
        Text(
            text = body,
            color = Ink,
            fontSize = 13.sp,
            fontFamily = PixelFont,
            lineHeight = 19.sp,
            textAlign = TextAlign.Center
        )
        Text(
            text = actionLabel,
            color = Ink,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = PixelFont,
            modifier = Modifier
                .lcdDoubleBorder()
                .uiClickable(kind = UiSoundKind.CONFIRM, onClick = onAction)
                .padding(horizontal = 18.dp, vertical = 10.dp)
        )
    }
}

/** 四隅ブラケット＋軽拡縮で対象を指す（親の余白まで広げない）。 */
@Composable
fun TutorialSpotlight(
    active: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val pulse = rememberInfiniteTransition(label = "tut_spot")
    val scale by pulse.animateFloat(
        initialValue = 1f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(520, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "tut_spot_scale"
    )
    val bracketAlpha by pulse.animateFloat(
        initialValue = 0.55f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(520, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "tut_spot_alpha"
    )
    Box(
        modifier = modifier
            .wrapContentHeight()
            .graphicsLayer {
                if (active) {
                    scaleX = scale
                    scaleY = scale
                }
            }
    ) {
        content()
        if (active) {
            TutorialCornerBrackets(
                alpha = bracketAlpha,
                modifier = Modifier.matchParentSize()
            )
        }
    }
}

@Composable
private fun TutorialCornerBrackets(
    alpha: Float,
    modifier: Modifier = Modifier,
    arm: Dp = 10.dp,
    stroke: Dp = 2.5.dp
) {
    val ink = Ink
    Canvas(modifier = modifier.padding(2.dp)) {
        val a = arm.toPx()
        val sw = stroke.toPx()
        val color = ink.copy(alpha = alpha)
        drawLine(color, Offset(0f, 0f), Offset(a, 0f), sw)
        drawLine(color, Offset(0f, 0f), Offset(0f, a), sw)
        drawLine(color, Offset(size.width, 0f), Offset(size.width - a, 0f), sw)
        drawLine(color, Offset(size.width, 0f), Offset(size.width, a), sw)
        drawLine(color, Offset(0f, size.height), Offset(a, size.height), sw)
        drawLine(color, Offset(0f, size.height), Offset(0f, size.height - a), sw)
        drawLine(color, Offset(size.width, size.height), Offset(size.width - a, size.height), sw)
        drawLine(color, Offset(size.width, size.height), Offset(size.width, size.height - a), sw)
    }
}

/** 一度見たチュートの文章見直し。 */
@Composable
fun TutorialGuideOverlay(
    available: List<TutorialGuideKind>,
    bonusMult: Float,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val lang = LocalDisplayLanguage.current
    var selected by remember { mutableStateOf<TutorialGuideKind?>(null) }
    var step by remember { mutableIntStateOf(0) }
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
                .lcdDoubleBorder()
                .background(LcdBackground)
                .clickable(enabled = false, onClick = {})
                .padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            val kind = selected
            if (kind == null) {
                Text(
                    text = UiStrings.tutorialGuideHubTitle(lang),
                    color = Ink,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = PixelFont
                )
                if (available.isEmpty()) {
                    Text(
                        text = UiStrings.tutorialGuideEmpty(lang),
                        color = InkMuted,
                        fontSize = 12.sp,
                        fontFamily = PixelFont,
                        lineHeight = 17.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(vertical = 12.dp)
                    )
                } else {
                    available.forEach { item ->
                        Text(
                            text = UiStrings.tutorialGuideName(item, lang),
                            color = Ink,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = PixelFont,
                            modifier = Modifier
                                .fillMaxWidth()
                                .lcdBorder()
                                .uiClickable(kind = UiSoundKind.SELECT) {
                                    selected = item
                                    step = 0
                                }
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            textAlign = TextAlign.Center
                        )
                    }
                }
                Text(
                    text = UiStrings.relicClose(lang),
                    color = Ink,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = PixelFont,
                    modifier = Modifier
                        .lcdBorder()
                        .uiClickable(kind = UiSoundKind.SELECT, onClick = onDismiss)
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                )
            } else {
                val total = UiStrings.tutorialGuidePageCount(kind)
                TutorialCard(
                    title = UiStrings.tutorialGuideName(kind, lang),
                    body = UiStrings.tutorialGuidePage(kind, step, bonusMult, lang),
                    stepLabel = "${step + 1}/$total",
                    actionLabel = if (step < total - 1) {
                        UiStrings.firstPlayTutorialNext(lang)
                    } else {
                        UiStrings.relicClose(lang)
                    },
                    onAction = {
                        if (step < total - 1) {
                            step += 1
                        } else {
                            selected = null
                            step = 0
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )
                Text(
                    text = "← ${UiStrings.tutorialGuideHubTitle(lang)}",
                    color = InkMuted,
                    fontSize = 11.sp,
                    fontFamily = PixelFont,
                    modifier = Modifier
                        .uiClickable(kind = UiSoundKind.SELECT) {
                            selected = null
                            step = 0
                        }
                        .padding(4.dp)
                )
            }
        }
    }
}
