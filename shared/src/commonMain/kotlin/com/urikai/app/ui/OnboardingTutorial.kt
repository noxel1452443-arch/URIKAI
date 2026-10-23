package com.urikai.app.ui

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.urikai.app.game.NotebookEntryId
import com.urikai.app.game.UiStrings
import com.urikai.app.ui.theme.Ink
import com.urikai.app.ui.theme.InkMuted
import com.urikai.app.ui.theme.LcdBackground
import com.urikai.app.ui.theme.LocalDisplayLanguage
import com.urikai.app.ui.theme.PixelFont

/** 初回 START 後・タイマー開始前（カイの吹き出し・2枚）。 */
@Composable
fun FirstPlayTutorialOverlay(
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    ForcedNotebookSpeechOverlay(
        entryId = NotebookEntryId.HOW_TO_PLAY,
        finalActionLabel = { lang -> UiStrings.firstPlayTutorialStart(lang) },
        onDismiss = onDismiss,
        modifier = modifier
    )
}

/** LIFE 導入（2戦目 START 後）。 */
@Composable
fun LifeTutorialOverlay(
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    ForcedNotebookSpeechOverlay(
        entryId = NotebookEntryId.LIFE,
        finalActionLabel = { lang -> UiStrings.firstPlayTutorialStart(lang) },
        onDismiss = onDismiss,
        modifier = modifier
    )
}

/** 口座を初めて開いたとき（2枚）。 */
@Composable
fun InvestOpenTutorialOverlay(
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    ForcedNotebookSpeechOverlay(
        entryId = NotebookEntryId.BALANCE,
        pageCountOverride = 2,
        finalActionLabel = { lang -> UiStrings.notebookClose(lang) },
        onDismiss = onDismiss,
        modifier = modifier
    )
}

/** カイのすみかシートを初めて開いたとき（3枚）。 */
@Composable
fun SumikaSheetTutorialOverlay(
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val lang = LocalDisplayLanguage.current
    var page by remember { mutableIntStateOf(0) }
    val pageCount = UiStrings.sumikaSheetTutorialPageCount()
    val spotlight = when (page) {
        1 -> GuideSpotlightTarget.SUMIKA_PAY_STATS
        2 -> GuideSpotlightTarget.SUMIKA_UPGRADE
        else -> GuideSpotlightTarget.NONE
    }
    rememberGuideSpotlightBinding(spotlight)
    val ctrl = LocalGuideSpotlight.current
    val hole = ctrl?.holeInRoot
    val isLast = page >= pageCount - 1
    Box(modifier = modifier.fillMaxSize()) {
        GuideSpotlightScrim(holeInRoot = hole)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() },
                    onClick = {}
                )
        )
        GuideSpeechStage(
            name = UiStrings.guideBuddyName(lang),
            body = UiStrings.sumikaSheetTutorialPage(page, lang),
            stepLabel = "${page + 1}/$pageCount",
            actionLabel = if (isLast) {
                UiStrings.notebookClose(lang)
            } else {
                UiStrings.firstPlayTutorialNext(lang)
            },
            mood = when (page) {
                0 -> GuideBuddyMood.THINK
                1 -> GuideBuddyMood.TALK
                else -> GuideBuddyMood.SMILE
            },
            // 吹き出し＋カイを中央寄せ。シート上端にキャラが少し重なる高さ。
            onAction = {
                if (isLast) onDismiss() else page += 1
            },
            modifier = Modifier
                .fillMaxSize()
                .offset(y = (-12).dp)
        )
    }
}

/** 口座を初めて閉じたとき（2枚＝ノート BALANCE の 3・4枚目）。 */
@Composable
fun InvestCloseTipOverlay(
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val lang = LocalDisplayLanguage.current
    var page by remember { mutableIntStateOf(0) }
    val pageCount = 2
    rememberGuideSpotlightBinding(GuideSpotlightTarget.NONE)
    Box(modifier = modifier.fillMaxSize()) {
        GuideSpotlightScrim(holeInRoot = null)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() },
                    onClick = {}
                )
        )
        val isLast = page >= pageCount - 1
        GuideSpeechStage(
            name = UiStrings.guideBuddyName(lang),
            body = UiStrings.notebookPage(NotebookEntryId.BALANCE, page + 2, lang),
            stepLabel = "${page + 1}/$pageCount",
            actionLabel = if (isLast) {
                UiStrings.notebookClose(lang)
            } else {
                UiStrings.firstPlayTutorialNext(lang)
            },
            mood = if (page == 0) GuideBuddyMood.THINK else GuideBuddyMood.TALK,
            onAction = {
                if (isLast) onDismiss() else page += 1
            },
            modifier = Modifier.fillMaxSize()
        )
    }
}

/** 図鑑（初クリア後）。 */
@Composable
fun ScrapbookTutorialOverlay(
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    ForcedNotebookSpeechOverlay(
        entryId = NotebookEntryId.SCRAPBOOK,
        finalActionLabel = { lang -> UiStrings.notebookClose(lang) },
        onDismiss = onDismiss,
        modifier = modifier
    )
}

/** 初回出金後・カイがホームに引っ越す導入。 */
@Composable
fun KaiHomeIntroOverlay(
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val lang = LocalDisplayLanguage.current
    val pageCount = UiStrings.kaiHomeIntroPageCount()
    var page by remember { mutableIntStateOf(0) }
    rememberGuideSpotlightBinding(GuideSpotlightTarget.NONE)
    Box(modifier = modifier.fillMaxSize()) {
        GuideSpotlightScrim(holeInRoot = null)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() },
                    onClick = {}
                )
        )
        val isLast = page >= pageCount - 1
        GuideSpeechStage(
            name = UiStrings.guideBuddyName(lang),
            body = UiStrings.kaiHomeIntroBody(page, lang),
            stepLabel = "${page + 1}/$pageCount",
            actionLabel = if (isLast) {
                UiStrings.notebookClose(lang)
            } else {
                UiStrings.firstPlayTutorialNext(lang)
            },
            mood = when (page) {
                0 -> GuideBuddyMood.SMILE
                1 -> GuideBuddyMood.TALK
                else -> GuideBuddyMood.WINK
            },
            onAction = {
                if (isLast) onDismiss() else page += 1
            },
            modifier = Modifier.fillMaxSize()
        )
    }
}

/** 切手帳裏面を初めて開いたとき。 */
@Composable
fun StampBackTutorialOverlay(
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    ForcedNotebookSpeechOverlay(
        entryId = NotebookEntryId.STAMPS,
        finalActionLabel = { lang -> UiStrings.notebookClose(lang) },
        onDismiss = onDismiss,
        modifier = modifier
    )
}

/** G8-1 ホーム一言（ノート非収録）。 */
@Composable
fun StampCornerHintBubble(
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val lang = LocalDisplayLanguage.current
    Row(
        modifier = modifier
            .lcdDoubleBorder()
            .background(LcdBackground)
            .uiClickable(kind = UiSoundKind.SELECT, onClick = onDismiss)
            .padding(horizontal = 10.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        GuideBuddyFace(mood = GuideBuddyMood.THINK, size = 52.dp)
        Text(
            text = UiStrings.stampCornerHint(lang),
            color = Ink,
            fontSize = 12.sp,
            fontFamily = PixelFont,
            lineHeight = 17.sp,
            modifier = Modifier.widthIn(max = 220.dp)
        )
    }
}

@Composable
private fun ForcedNotebookSpeechOverlay(
    entryId: NotebookEntryId,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    pageCountOverride: Int? = null,
    finalActionLabel: (com.urikai.app.game.DisplayLanguage) -> String
) {
    val lang = LocalDisplayLanguage.current
    var step by remember { mutableIntStateOf(0) }
    val lastStep = (pageCountOverride ?: UiStrings.notebookPageCount(entryId)) - 1
    val spotlight = guideSpotlightFor(entryId, step)
    rememberGuideSpotlightBinding(spotlight)
    val ctrl = LocalGuideSpotlight.current
    val hole = ctrl?.holeInRoot
    // 初回プレイ案内はノートと同じ中央。PRIMARY_BUTTON でも上寄せしない。
    val speechAtTop =
        spotlight == GuideSpotlightTarget.PRIMARY_BUTTON &&
            entryId != NotebookEntryId.HOW_TO_PLAY
    val mood = guideMoodFor(entryId, step, isLast = step >= lastStep)
    Box(modifier = modifier.fillMaxSize()) {
        GuideSpotlightScrim(holeInRoot = hole)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() },
                    onClick = {}
                )
        )
        GuideSpeechStage(
            name = UiStrings.guideBuddyName(lang),
            body = UiStrings.notebookPage(entryId, step, lang),
            stepLabel = "${step + 1}/${lastStep + 1}",
            actionLabel = if (step < lastStep) {
                UiStrings.firstPlayTutorialNext(lang)
            } else {
                finalActionLabel(lang)
            },
            mood = mood,
            speechAtTop = speechAtTop,
            onAction = {
                if (step < lastStep) step += 1 else onDismiss()
            },
            modifier = Modifier.fillMaxSize()
        )
    }
}

/**
 * ガイド演出。
 * - 中央の枠にテキストのみ（大ボタンなし）。枠タップで進む
 * - 右下にゆっくり明滅する進むサイン（▸ つぎ / とじる 等）
 * - カイはガイド枠の左下
 */
@Composable
internal fun GuideSpeechStage(
    name: String,
    body: String,
    stepLabel: String,
    actionLabel: String,
    onAction: () -> Unit,
    modifier: Modifier = Modifier,
    mood: GuideBuddyMood = GuideBuddyMood.TALK,
    speechAtTop: Boolean = false,
    /** true のときカイを画面下寄り（すみかシート上）に置く。吹き出しは上のまま。 */
    buddyDockBottom: Boolean = false,
) {
    val lang = LocalDisplayLanguage.current
    val cueBlink = rememberInfiniteTransition(label = "guide_cue")
    val cueAlpha by cueBlink.animateFloat(
        initialValue = 1f,
        targetValue = 0.22f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1100, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "guide_cue_alpha"
    )
    Box(modifier = modifier) {
        Box(
            modifier = Modifier
                .align(if (speechAtTop) Alignment.TopCenter else Alignment.Center)
                .padding(horizontal = 18.dp, vertical = 20.dp)
                .fillMaxWidth(0.88f)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .lcdDoubleBorder()
                    .background(LcdBackground)
                    .uiClickable(kind = UiSoundKind.CONFIRM, onClick = onAction)
                    .padding(horizontal = 14.dp, vertical = 14.dp)
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 18.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = name,
                            color = Ink,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = PixelFont
                        )
                        Text(
                            text = stepLabel,
                            color = InkMuted,
                            fontSize = 11.sp,
                            fontFamily = PixelFont
                        )
                    }
                    Text(
                        text = body,
                        color = Ink,
                        fontSize = 14.sp,
                        fontFamily = PixelFont,
                        lineHeight = 22.sp,
                        textAlign = TextAlign.Start,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                Text(
                    text = UiStrings.guideAdvanceCue(actionLabel, lang),
                    color = Ink,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = PixelFont,
                    letterSpacing = 1.sp,
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .alpha(cueAlpha)
                )
            }
            if (!buddyDockBottom) {
                GuideBuddyFace(
                    mood = mood,
                    size = 92.dp,
                    silhouetteBack = true,
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .offset(x = (-8).dp, y = 56.dp)
                )
            }
        }
        if (buddyDockBottom) {
            GuideBuddyFace(
                mood = mood,
                size = 92.dp,
                silhouetteBack = true,
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(start = 10.dp, bottom = 168.dp)
            )
        }
    }
}

@Composable
fun UnreadDot(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(8.dp)
            .clip(CircleShape)
            .background(Ink)
    )
}

/** カイのノート一覧／読み返し。 */
@Composable
fun NotebookOverlay(
    unlocked: List<NotebookEntryId>,
    read: Set<NotebookEntryId>,
    onMarkRead: (NotebookEntryId) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val lang = LocalDisplayLanguage.current
    var selected by remember { mutableStateOf<NotebookEntryId?>(null) }
    var step by remember { mutableIntStateOf(0) }
    val kind = selected
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Ink.copy(alpha = 0.45f))
            .clickable(onClick = {
                if (kind == null) onDismiss()
            }),
        contentAlignment = Alignment.Center
    ) {
        if (kind == null) {
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
                Text(
                    text = UiStrings.notebookHubTitle(lang),
                    color = Ink,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = PixelFont
                )
                if (unlocked.isEmpty()) {
                    Text(
                        text = UiStrings.notebookEmpty(lang),
                        color = InkMuted,
                        fontSize = 12.sp,
                        fontFamily = PixelFont,
                        lineHeight = 17.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(vertical = 12.dp)
                    )
                } else {
                    unlocked.forEach { item ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .lcdBorder()
                                .uiClickable(kind = UiSoundKind.SELECT) {
                                    selected = item
                                    step = 0
                                    onMarkRead(item)
                                }
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            if (item !in read) {
                                UnreadDot(modifier = Modifier.padding(end = 8.dp))
                            }
                            Text(
                                text = UiStrings.notebookEntryTitle(item, lang),
                                color = Ink,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = PixelFont,
                                textAlign = TextAlign.Center
                            )
                        }
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
            }
        } else {
            val total = UiStrings.notebookPageCount(kind)
            GuideSpeechStage(
                name = UiStrings.guideBuddyName(lang),
                body = UiStrings.notebookPage(kind, step, lang),
                stepLabel = "${step + 1}/$total",
                actionLabel = if (step < total - 1) {
                    UiStrings.firstPlayTutorialNext(lang)
                } else {
                    UiStrings.notebookClose(lang)
                },
                mood = guideMoodFor(kind, step, isLast = step >= total - 1),
                onAction = {
                    if (step < total - 1) {
                        step += 1
                    } else {
                        // 最終ページの「とじる」で一覧へ戻る
                        selected = null
                        step = 0
                    }
                },
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}
