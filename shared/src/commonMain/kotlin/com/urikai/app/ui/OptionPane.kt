package com.urikai.app.ui

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.urikai.app.ui.theme.LocalIsDebugBuild
import com.urikai.app.game.AudioLevel
import com.urikai.app.game.DisplayLanguage
import com.urikai.app.game.HudMode
import com.urikai.app.game.PanelContrast
import com.urikai.app.game.ScanlineStrength
import com.urikai.app.game.SurfaceTone
import com.urikai.app.game.UiStrings
import com.urikai.app.ui.theme.Ink
import com.urikai.app.ui.theme.InkFaint
import com.urikai.app.ui.theme.InkMuted
import com.urikai.app.ui.theme.LcdBackground
import com.urikai.app.ui.theme.LocalDisplayLanguage
import com.urikai.app.ui.theme.PixelFont

@Composable
fun OptionPane(
    hudMode: HudMode,
    displayLanguage: DisplayLanguage,
    scanlineStrength: ScanlineStrength,
    panelContrast: PanelContrast,
    surfaceTone: SurfaceTone,
    seLevel: AudioLevel,
    bgmLevel: AudioLevel,
    hapticEnabled: Boolean,
    onHudModeChange: (HudMode) -> Unit,
    onDisplayLanguageChange: (DisplayLanguage) -> Unit,
    onScanlineStrengthChange: (ScanlineStrength) -> Unit,
    onPanelContrastChange: (PanelContrast) -> Unit,
    onSurfaceToneChange: (SurfaceTone) -> Unit,
    onSeLevelChange: (AudioLevel) -> Unit,
    onBgmLevelChange: (AudioLevel) -> Unit,
    onHapticEnabledChange: (Boolean) -> Unit,
    devRelicLab: Map<com.urikai.app.game.RelicId, Boolean>? = null,
    onEnableDevRelicLab: () -> Unit = {},
    onDisableDevRelicLab: () -> Unit = {},
    onSetDevRelicEnabled: (com.urikai.app.game.RelicId, Boolean) -> Unit = { _, _ -> },
    devBookRevealAll: Boolean = false,
    onDevBookRevealAllChange: (Boolean) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val lang = LocalDisplayLanguage.current
    val rule = InkFaint.copy(alpha = 0.55f)

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 6.dp, vertical = 4.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            SettingRow(label = UiStrings.settingLabel("LANG", lang), ruleColor = rule) {
                ConfigChip("EN", displayLanguage == DisplayLanguage.EN) {
                    onDisplayLanguageChange(DisplayLanguage.EN)
                }
                ConfigChip("JP", displayLanguage == DisplayLanguage.JP) {
                    onDisplayLanguageChange(DisplayLanguage.JP)
                }
            }
            SettingRow(label = UiStrings.settingLabel("TONE", lang), ruleColor = rule) {
                ConfigChip(UiStrings.settingLabel("DIM", lang), surfaceTone == SurfaceTone.DIM) {
                    onSurfaceToneChange(SurfaceTone.DIM)
                }
                ConfigChip(UiStrings.settingLabel("NORM", lang), surfaceTone == SurfaceTone.NORMAL) {
                    onSurfaceToneChange(SurfaceTone.NORMAL)
                }
                ConfigChip(UiStrings.settingLabel("BRIGHT", lang), surfaceTone == SurfaceTone.BRIGHT) {
                    onSurfaceToneChange(SurfaceTone.BRIGHT)
                }
            }
            SettingRow(label = UiStrings.settingLabel("SE", lang), ruleColor = rule) {
                AudioLevelChips(selected = seLevel, onChange = onSeLevelChange)
            }
            SettingRow(label = UiStrings.settingLabel("BGM", lang), ruleColor = rule) {
                AudioLevelChips(selected = bgmLevel, onChange = onBgmLevelChange)
            }
            SettingRow(label = UiStrings.settingLabel("HAPTIC", lang), ruleColor = rule) {
                ConfigChip(UiStrings.settingLabel("ON", lang), hapticEnabled) { onHapticEnabledChange(true) }
                ConfigChip(UiStrings.settingLabel("OFF", lang), !hapticEnabled) { onHapticEnabledChange(false) }
            }
            SettingRow(
                label = UiStrings.settingLabel("HUD", lang),
                ruleColor = rule,
                trailing = { HudModePreview(hudMode = hudMode) }
            ) {
                ConfigChip(UiStrings.settingLabel("FULL", lang), hudMode == HudMode.FULL) { onHudModeChange(HudMode.FULL) }
                ConfigChip(UiStrings.settingLabel("FOCUS", lang), hudMode == HudMode.FOCUS) { onHudModeChange(HudMode.FOCUS) }
            }
            SettingRow(label = UiStrings.settingLabel("SCAN", lang), ruleColor = rule) {
                ConfigChip(UiStrings.settingLabel("OFF", lang), scanlineStrength == ScanlineStrength.OFF) {
                    onScanlineStrengthChange(ScanlineStrength.OFF)
                }
                ConfigChip(UiStrings.settingLabel("LOW", lang), scanlineStrength == ScanlineStrength.LOW) {
                    onScanlineStrengthChange(ScanlineStrength.LOW)
                }
                ConfigChip(UiStrings.settingLabel("HIGH", lang), scanlineStrength == ScanlineStrength.HIGH) {
                    onScanlineStrengthChange(ScanlineStrength.HIGH)
                }
            }
            SettingRow(label = UiStrings.settingLabel("CONTRAST", lang), ruleColor = rule) {
                ConfigChip(UiStrings.settingLabel("SOFT", lang), panelContrast == PanelContrast.SOFT) {
                    onPanelContrastChange(PanelContrast.SOFT)
                }
                ConfigChip(UiStrings.settingLabel("NORM", lang), panelContrast == PanelContrast.NORMAL) {
                    onPanelContrastChange(PanelContrast.NORMAL)
                }
                ConfigChip(UiStrings.settingLabel("HARD", lang), panelContrast == PanelContrast.HARD) {
                    onPanelContrastChange(PanelContrast.HARD)
                }
            }
            if (LocalIsDebugBuild.current) {
                SettingRow(label = "DEV", ruleColor = rule) {
                    val labOn = devRelicLab != null
                    ConfigChip("LAB ON", labOn) { onEnableDevRelicLab() }
                    ConfigChip("LAB OFF", !labOn) { onDisableDevRelicLab() }
                }
                SettingRow(label = "DEV図鑑", ruleColor = rule) {
                    ConfigChip("ALL", devBookRevealAll) { onDevBookRevealAllChange(true) }
                    ConfigChip("NORMAL", !devBookRevealAll) { onDevBookRevealAllChange(false) }
                }
                if (devRelicLab != null) {
                    Text(
                        text = if (lang == DisplayLanguage.JP) {
                            "レリック効果 ON/OFF（セーブ非破壊）"
                        } else {
                            "Relic FX ON/OFF (non-destructive)"
                        },
                        color = InkMuted,
                        fontSize = 10.sp,
                        fontFamily = PixelFont,
                        modifier = Modifier.padding(top = 4.dp, bottom = 2.dp)
                    )
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(160.dp)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        com.urikai.app.game.RelicId.ordered.chunked(2).forEach { pair ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                pair.forEach { id ->
                                    val on = devRelicLab[id] == true
                                    val short = id.name.take(11)
                                    Box(modifier = Modifier.weight(1f)) {
                                        ConfigChip(
                                            label = if (on) "$short✓" else short,
                                            selected = on
                                        ) {
                                            onSetDevRelicEnabled(id, !on)
                                        }
                                    }
                                }
                                if (pair.size == 1) {
                                    Spacer(modifier = Modifier.weight(1f))
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        AboutBlock(
            title = UiStrings.contentDisclaimerTitle(lang),
            body = UiStrings.contentDisclaimerBody(lang),
            credits = UiStrings.creditsBody(lang),
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        )
    }
}

@Composable
private fun AudioLevelChips(
    selected: AudioLevel,
    onChange: (AudioLevel) -> Unit
) {
    val lang = LocalDisplayLanguage.current
    ConfigChip(UiStrings.settingLabel("OFF", lang), selected == AudioLevel.OFF) { onChange(AudioLevel.OFF) }
    ConfigChip(UiStrings.settingLabel("LOW", lang), selected == AudioLevel.LOW) { onChange(AudioLevel.LOW) }
    ConfigChip(UiStrings.settingLabel("MID", lang), selected == AudioLevel.MID) { onChange(AudioLevel.MID) }
    ConfigChip(UiStrings.settingLabel("HIGH", lang), selected == AudioLevel.HIGH) { onChange(AudioLevel.HIGH) }
}

@Composable
private fun SettingRow(
    label: String,
    ruleColor: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier,
    trailing: (@Composable () -> Unit)? = null,
    chips: @Composable () -> Unit
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .drawBehind {
                val y = size.height - 0.5f
                drawLine(
                    color = ruleColor,
                    start = Offset(0f, y),
                    end = Offset(size.width, y),
                    strokeWidth = 1f
                )
            }
            .padding(vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(
            text = label,
            color = Ink,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = PixelFont,
            maxLines = 1,
            softWrap = false,
            overflow = TextOverflow.Clip,
            modifier = Modifier.width(72.dp)
        )
        Row(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            chips()
        }
        trailing?.invoke()
    }
}

@Composable
private fun AboutBlock(
    title: String,
    body: String,
    credits: String,
    modifier: Modifier = Modifier
) {
    val scroll = rememberScrollState()
    Column(
        modifier = modifier
            .lcdBorder()
            .background(LcdBackground)
            .padding(horizontal = 10.dp, vertical = 8.dp)
    ) {
        Text(
            text = title,
            color = Ink,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = PixelFont,
            maxLines = 1,
            softWrap = false
        )
        Spacer(modifier = Modifier.height(6.dp))
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .verticalScroll(scroll),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = body,
                color = InkMuted,
                fontSize = 11.sp,
                fontFamily = PixelFont,
                lineHeight = 15.sp
            )
            Text(
                text = credits,
                color = InkFaint,
                fontSize = 9.sp,
                fontFamily = PixelFont,
                lineHeight = 12.sp
            )
        }
    }
}

@Composable
private fun ConfigChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    val mod = if (selected) Modifier.lcdDoubleBorder() else Modifier.lcdBorder(InkFaint)
    Text(
        text = label,
        color = if (selected) Ink else InkMuted,
        fontSize = 10.sp,
        fontWeight = FontWeight.Bold,
        fontFamily = PixelFont,
        maxLines = 1,
        softWrap = false,
        overflow = TextOverflow.Clip,
        modifier = mod
            .widthIn(min = 36.dp)
            .uiClickable(kind = UiSoundKind.SELECT, onClick = onClick)
            .background(if (selected) LcdBackground else LcdBackground.copy(alpha = 0.85f))
            .padding(horizontal = 7.dp, vertical = 4.dp)
    )
}
