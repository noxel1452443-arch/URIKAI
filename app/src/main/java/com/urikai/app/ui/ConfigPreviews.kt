package com.urikai.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.urikai.app.game.HudMode
import com.urikai.app.ui.theme.Ink
import com.urikai.app.ui.theme.InkMuted
import com.urikai.app.ui.theme.LcdBackground
import com.urikai.app.ui.theme.PixelFont

@Composable
fun HudModePreview(hudMode: HudMode, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .width(72.dp)
            .height(40.dp)
            .lcdBorder(InkMuted)
            .background(LcdBackground)
            .padding(horizontal = 4.dp, vertical = 3.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(1.dp)
        ) {
            Text("TIME 42", color = Ink, fontSize = 6.sp, fontFamily = PixelFont, maxLines = 1, softWrap = false)
            Text(
                text = "¥1,234",
                color = Ink,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = PixelFont,
                maxLines = 1,
                softWrap = false
            )
            if (hudMode == HudMode.FULL) {
                Text(
                    text = "ASSET",
                    color = InkMuted,
                    fontSize = 6.sp,
                    fontFamily = PixelFont,
                    maxLines = 1,
                    softWrap = false
                )
            }
        }
    }
}
