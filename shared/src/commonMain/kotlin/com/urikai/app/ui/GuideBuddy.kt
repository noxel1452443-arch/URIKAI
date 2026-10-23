package com.urikai.app.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.urikai.app.res.Res
import com.urikai.app.res.allDrawableResources
import com.urikai.app.ui.theme.Ink
import com.urikai.app.ui.theme.LcdBackground
import org.jetbrains.compose.resources.imageResource

/**
 * カイ（A4）。
 *
 * - `*_sil`: キャラ輪郭の不透明マスク → 液晶下地色（透け防止・明るさの土台）
 * - 本体: 図鑑と同じ薄いインクマスク → テーマの Ink
 * ColorMatrix は使わない（効かない環境で全面インク＝真っ黒になるため）。
 */
@Composable
fun GuideBuddyFace(
    modifier: Modifier = Modifier,
    size: Dp = 56.dp,
    mood: GuideBuddyMood = GuideBuddyMood.TALK,
    @Suppress("UNUSED_PARAMETER") silhouetteBack: Boolean = true
) {
    KaiSprite(
        inkKey = mood.drawableKey(),
        silKey = mood.silhouetteKey(),
        modifier = modifier.size(size)
    )
}

@Composable
fun GuideBuddyPose(
    pose: GuideBuddyPose,
    modifier: Modifier = Modifier,
    size: Dp = 96.dp
) {
    KaiSprite(
        inkKey = pose.drawableKey,
        silKey = "${pose.drawableKey}_sil",
        modifier = modifier.size(size)
    )
}

@Composable
private fun KaiSprite(inkKey: String, silKey: String, modifier: Modifier = Modifier) {
    val inkRes = Res.allDrawableResources[inkKey]
    val silRes = Res.allDrawableResources[silKey]
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        if (silRes != null) {
            Image(
                bitmap = imageResource(silRes),
                contentDescription = null,
                contentScale = ContentScale.Fit,
                filterQuality = FilterQuality.None,
                colorFilter = ColorFilter.tint(LcdBackground),
                modifier = Modifier.fillMaxSize()
            )
        }
        if (inkRes != null) {
            Image(
                bitmap = imageResource(inkRes),
                contentDescription = null,
                contentScale = ContentScale.Fit,
                filterQuality = FilterQuality.None,
                colorFilter = ColorFilter.tint(Ink),
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

enum class GuideBuddyMood {
    TALK,
    SMILE,
    THINK,
    WINK,
    /** 壁などにぶつかったときの痛そう顔。 */
    OUCH,
    /** 何度もぶつかったあとの泣き顔。 */
    CRY;

    fun drawableKey(): String = when (this) {
        TALK -> "kai_face_talk"
        SMILE -> "kai_face_smile"
        THINK -> "kai_face_think"
        WINK -> "kai_face_wink"
        OUCH -> "kai_face_ouch"
        CRY -> "kai_face_cry"
    }

    fun silhouetteKey(): String = "${drawableKey()}_sil"
}

enum class GuideBuddyPose(val drawableKey: String) {
    WAVE("kai_pose_wave"),
    POINT_UP("kai_pose_point_up"),
    SIT("kai_pose_sit"),
    SLEEP("kai_pose_sleep"),
}

fun guideMoodFor(entryId: com.urikai.app.game.NotebookEntryId, step: Int, isLast: Boolean): GuideBuddyMood {
    if (isLast && entryId != com.urikai.app.game.NotebookEntryId.STAMPS) {
        return GuideBuddyMood.SMILE
    }
    return when (entryId) {
        com.urikai.app.game.NotebookEntryId.HOW_TO_PLAY -> GuideBuddyMood.TALK
        com.urikai.app.game.NotebookEntryId.LIFE ->
            if (step == 0) GuideBuddyMood.THINK else GuideBuddyMood.TALK
        com.urikai.app.game.NotebookEntryId.BALANCE ->
            if (step == 0) GuideBuddyMood.TALK else GuideBuddyMood.THINK
        com.urikai.app.game.NotebookEntryId.STAMPS ->
            if (step == 0) GuideBuddyMood.THINK else GuideBuddyMood.WINK
        com.urikai.app.game.NotebookEntryId.SCRAPBOOK -> GuideBuddyMood.SMILE
        else -> GuideBuddyMood.TALK
    }
}

/** ガイド中は顔差分のみ使い、ポーズ差し替えによるサイズずれを避ける。 */
fun guidePoseFor(entryId: com.urikai.app.game.NotebookEntryId, step: Int): GuideBuddyPose? = null
