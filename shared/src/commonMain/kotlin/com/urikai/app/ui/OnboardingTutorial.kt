package com.urikai.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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

/** 一度見たチュートの文章見直し。 */
@Composable
fun TutorialGuideOverlay(
    available: List<TutorialGuideKind>,
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
                    body = UiStrings.tutorialGuidePage(kind, step, lang),
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
