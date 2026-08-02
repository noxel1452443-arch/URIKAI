package com.urikai.app.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed

/** UI操作音の種類（KH風の決定／選択）。 */
enum class UiSoundKind {
    /** 大きめボタン・画面を進める系 */
    CONFIRM,
    /** トグル・適用・図鑑項目など軽いフィードバック */
    SELECT,
}

data class UiSoundBus(
    val confirm: () -> Unit = {},
    val select: () -> Unit = {},
) {
    fun play(kind: UiSoundKind) {
        when (kind) {
            UiSoundKind.CONFIRM -> confirm()
            UiSoundKind.SELECT -> select()
        }
    }
}

val LocalUiSound = staticCompositionLocalOf { UiSoundBus() }

/** clickable + UI操作音。 */
fun Modifier.uiClickable(
    kind: UiSoundKind = UiSoundKind.SELECT,
    enabled: Boolean = true,
    onClick: () -> Unit,
): Modifier = composed {
    val bus = LocalUiSound.current
    val interaction = remember { MutableInteractionSource() }
    clickable(
        enabled = enabled,
        interactionSource = interaction,
        indication = null,
        onClick = {
            if (enabled) bus.play(kind)
            onClick()
        }
    )
}

@Composable
fun rememberUiSound(): UiSoundBus = LocalUiSound.current
