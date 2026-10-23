package com.urikai.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
import com.urikai.app.ui.theme.LocalEnableAdFeatures
import com.urikai.app.ui.theme.LocalIsDebugBuild
import com.urikai.app.ui.theme.PixelFont

private enum class SettingsTab { Display, Audio, About, Dev }

/**
 * 設定。上半分はタブ（表示 / 音・触覚 / ABOUT / DEV）、下半分は常時「応援」。
 * 項目はラベル付きのまま保ち、圧縮しすぎない。
 */
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
    adsRemoved: Boolean = false,
    removeAdsPriceLabel: String = "¥290",
    onPurchaseRemoveAds: () -> Unit = {},
    onRestorePurchases: () -> Unit = {},
    onOpenStoreListing: () -> Unit = {},
    onDebugSetAdsRemoved: (Boolean) -> Unit = {},
    onDebugResetSumika: () -> Unit = {},
    onDebugForceSumikaGrade: (Int) -> Unit = {},
    onDebugWipeAllProgress: () -> Unit = {},
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
    val scroll = rememberScrollState()
    val isDebug = LocalIsDebugBuild.current
    val enableAdFeatures = LocalEnableAdFeatures.current
    var tab by remember { mutableStateOf(SettingsTab.Display) }

    val supportScroll = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 6.dp, vertical = 4.dp)
    ) {
        SettingsTabBar(
            selected = tab,
            showDev = isDebug,
            lang = lang,
            onSelect = { tab = it }
        )
        Spacer(Modifier.height(6.dp))

        // 上半分: 設定タブ内容
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .verticalScroll(scroll),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            when (tab) {
                SettingsTab.Display -> {
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
                    SettingRow(
                        label = UiStrings.settingLabel("HUD", lang),
                        ruleColor = rule,
                        trailing = { HudModePreview(hudMode = hudMode) }
                    ) {
                        ConfigChip(UiStrings.settingLabel("FULL", lang), hudMode == HudMode.FULL) {
                            onHudModeChange(HudMode.FULL)
                        }
                        ConfigChip(UiStrings.settingLabel("FOCUS", lang), hudMode == HudMode.FOCUS) {
                            onHudModeChange(HudMode.FOCUS)
                        }
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
                }

                SettingsTab.Audio -> {
                    SettingRow(label = UiStrings.settingLabel("SE", lang), ruleColor = rule) {
                        AudioLevelChips(selected = seLevel, onChange = onSeLevelChange)
                    }
                    SettingRow(label = UiStrings.settingLabel("BGM", lang), ruleColor = rule) {
                        AudioLevelChips(selected = bgmLevel, onChange = onBgmLevelChange)
                    }
                    SettingRow(label = UiStrings.settingLabel("HAPTIC", lang), ruleColor = rule) {
                        ConfigChip(UiStrings.settingLabel("ON", lang), hapticEnabled) {
                            onHapticEnabledChange(true)
                        }
                        ConfigChip(UiStrings.settingLabel("OFF", lang), !hapticEnabled) {
                            onHapticEnabledChange(false)
                        }
                    }
                }

                SettingsTab.About -> {
                    AboutBlock(
                        title = UiStrings.contentDisclaimerTitle(lang),
                        body = UiStrings.contentDisclaimerBody(lang),
                        credits = UiStrings.creditsBody(lang),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                SettingsTab.Dev -> {
                    SettingRow(label = "DEV", ruleColor = rule) {
                        val labOn = devRelicLab != null
                        ConfigChip("LAB ON", labOn) { onEnableDevRelicLab() }
                        ConfigChip("LAB OFF", !labOn) { onDisableDevRelicLab() }
                        ConfigChip("SUMIKA0", selected = false) { onDebugResetSumika() }
                        ConfigChip("WIPE", selected = false) { onDebugWipeAllProgress() }
                    }
                    SettingRow(label = "ADS", ruleColor = rule) {
                        ConfigChip(
                            if (adsRemoved) "DBG:ADS" else "DBG:NOAD",
                            selected = adsRemoved
                        ) {
                            onDebugSetAdsRemoved(!adsRemoved)
                        }
                    }
                    SettingRow(label = "SUMIKA", ruleColor = rule) {
                        (1..5).forEach { g ->
                            ConfigChip("S$g", selected = false) { onDebugForceSumikaGrade(g) }
                        }
                    }
                    SettingRow(label = "図鑑", ruleColor = rule) {
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
                                                label = if (on) "$short●" else short,
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
            Spacer(Modifier.height(4.dp))
        }

        Spacer(Modifier.height(6.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(rule)
        )
        Spacer(Modifier.height(6.dp))

        // 下半分: タブに関係なく常時「応援」（Web は広告除去購入を出さない）
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .verticalScroll(supportScroll),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = UiStrings.settingsSectionSupport(lang),
                color = Ink,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = PixelFont
            )
            if (enableAdFeatures) {
                Text(
                    text = UiStrings.removeAdsCardBody(lang),
                    color = InkMuted,
                    fontSize = 11.sp,
                    fontFamily = PixelFont,
                    lineHeight = 15.sp,
                    modifier = Modifier.fillMaxWidth()
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(IntrinsicSize.Min),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    PermanentAdsOffCard(
                        adsRemoved = adsRemoved,
                        priceLabel = removeAdsPriceLabel,
                        onPurchase = onPurchaseRemoveAds,
                        onRestore = onRestorePurchases,
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                    )
                    ReviewSupportCard(
                        onOpenReview = onOpenStoreListing,
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                    )
                }
            } else {
                ReviewSupportCard(
                    onOpenReview = onOpenStoreListing,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            Spacer(Modifier.height(4.dp))
        }
    }
}

@Composable
private fun SettingsTabBar(
    selected: SettingsTab,
    showDev: Boolean,
    lang: DisplayLanguage,
    onSelect: (SettingsTab) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        SettingsTabChip(
            label = UiStrings.settingsSectionDisplay(lang),
            selected = selected == SettingsTab.Display,
            onClick = { onSelect(SettingsTab.Display) },
            modifier = Modifier.weight(1f)
        )
        SettingsTabChip(
            label = UiStrings.settingsSectionAudio(lang),
            selected = selected == SettingsTab.Audio,
            onClick = { onSelect(SettingsTab.Audio) },
            modifier = Modifier.weight(1f)
        )
        SettingsTabChip(
            label = UiStrings.settingsSectionAbout(lang),
            selected = selected == SettingsTab.About,
            onClick = { onSelect(SettingsTab.About) },
            modifier = Modifier.weight(1f)
        )
        if (showDev) {
            SettingsTabChip(
                label = "DEV",
                selected = selected == SettingsTab.Dev,
                onClick = { onSelect(SettingsTab.Dev) },
                modifier = Modifier.weight(0.7f)
            )
        }
    }
}

@Composable
private fun SettingsTabChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Text(
        text = label,
        color = if (selected) LcdBackground else Ink,
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        fontFamily = PixelFont,
        textAlign = TextAlign.Center,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        modifier = modifier
            .then(if (selected) Modifier.background(Ink) else Modifier.lcdBorder())
            .uiClickable(kind = UiSoundKind.SELECT, onClick = onClick)
            .padding(horizontal = 4.dp, vertical = 8.dp)
    )
}

@Composable
private fun PermanentAdsOffCard(
    adsRemoved: Boolean,
    priceLabel: String,
    onPurchase: () -> Unit,
    onRestore: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val lang = LocalDisplayLanguage.current
    Column(
        modifier = modifier
            .lcdDoubleBorder()
            .background(LcdBackground)
            .padding(horizontal = 8.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        Text(
            text = UiStrings.permanentAdsOffItemName(lang),
            color = Ink,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = PixelFont,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
        PerkLine(UiStrings.permanentAdsOffPerkResult(lang))
        PerkLine(UiStrings.permanentAdsOffPerkHalf(lang))
        PerkLine(UiStrings.permanentAdsOffPerkDouble(lang))
        Spacer(Modifier.weight(1f))
        if (adsRemoved) {
            Text(
                text = "✓ ${UiStrings.removeAdsOwned(lang)}",
                color = Ink,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = PixelFont,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .lcdBorder()
                    .padding(horizontal = 8.dp, vertical = 7.dp)
            )
        } else {
            Text(
                text = UiStrings.removeAdsRestore(lang),
                color = Ink,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = PixelFont,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .lcdBorder()
                    .uiClickable(kind = UiSoundKind.SELECT, onClick = onRestore)
                    .padding(horizontal = 8.dp, vertical = 6.dp)
            )
            Text(
                text = UiStrings.removeAdsBuyWithPrice(priceLabel, lang),
                color = LcdBackground,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = PixelFont,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Ink)
                    .lcdBorder()
                    .uiClickable(kind = UiSoundKind.CONFIRM, onClick = onPurchase)
                    .padding(horizontal = 8.dp, vertical = 8.dp)
            )
        }
    }
}

@Composable
private fun ReviewSupportCard(
    onOpenReview: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val lang = LocalDisplayLanguage.current
    Column(
        modifier = modifier
            .lcdDoubleBorder()
            .background(LcdBackground)
            .padding(horizontal = 8.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        Text(
            text = UiStrings.reviewSupportTitle(lang),
            color = Ink,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = PixelFont,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = UiStrings.reviewSupportBody(lang),
            color = InkMuted,
            fontSize = 10.sp,
            fontFamily = PixelFont,
            lineHeight = 14.sp,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.weight(1f))
        Text(
            text = UiStrings.reviewSupportButton(lang),
            color = LcdBackground,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = PixelFont,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .background(Ink)
                .lcdBorder()
                .uiClickable(kind = UiSoundKind.CONFIRM, onClick = onOpenReview)
                .padding(horizontal = 8.dp, vertical = 8.dp)
        )
    }
}

@Composable
private fun PerkLine(text: String) {
    Text(
        text = "・$text",
        color = Ink,
        fontSize = 10.sp,
        fontFamily = PixelFont,
        lineHeight = 13.sp,
        modifier = Modifier.fillMaxWidth()
    )
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
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(
            text = label,
            color = Ink,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = PixelFont,
            maxLines = 1,
            softWrap = false,
            overflow = TextOverflow.Clip,
            modifier = Modifier.width(76.dp)
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
    Column(
        modifier = modifier
            .lcdBorder()
            .background(LcdBackground)
            .padding(horizontal = 10.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
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
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        fontFamily = PixelFont,
        maxLines = 1,
        softWrap = false,
        overflow = TextOverflow.Clip,
        modifier = mod
            .widthIn(min = 40.dp)
            .uiClickable(kind = UiSoundKind.SELECT, onClick = onClick)
            .background(if (selected) LcdBackground else LcdBackground.copy(alpha = 0.85f))
            .padding(horizontal = 8.dp, vertical = 6.dp)
    )
}
