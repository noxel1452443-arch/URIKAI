package com.urikai.app.ui

import com.urikai.app.game.fmt
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.urikai.app.data.ScrapbookEntry
import com.urikai.app.game.ScrapbookSort
import com.urikai.app.game.SyncRating
import com.urikai.app.game.UiStrings
import com.urikai.app.ui.theme.Ink
import com.urikai.app.ui.theme.InkFaint
import com.urikai.app.ui.theme.InkMuted
import com.urikai.app.ui.theme.LcdBackground
import com.urikai.app.ui.theme.LocalDisplayLanguage
import com.urikai.app.ui.theme.PixelFont
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun ScrapbookPane(
    entries: List<ScrapbookEntry>,
    sort: ScrapbookSort,
    onSortChange: (ScrapbookSort) -> Unit,
    modifier: Modifier = Modifier,
    revealAll: Boolean = false
) {
    var detailStageId by remember { mutableStateOf<String?>(null) }
    val entries = if (revealAll) {
        entries.map { if (it.unlocked) it else it.copy(unlocked = true) }
    } else {
        entries
    }

    Box(modifier = modifier.fillMaxSize()) {
        ScrapbookList(
            entries = entries,
            sort = sort,
            onSortChange = onSortChange,
            onOpen = { detailStageId = it }
        )
        entries.firstOrNull { it.stageId == detailStageId && it.unlocked }?.let { entry ->
            ScrapbookDetailOverlay(
                entry = entry,
                onDismiss = { detailStageId = null },
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

@Composable
private fun ScrapbookList(
    entries: List<ScrapbookEntry>,
    sort: ScrapbookSort,
    onSortChange: (ScrapbookSort) -> Unit,
    onOpen: (String) -> Unit,
) {
    val lang = LocalDisplayLanguage.current

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "%d / %d".fmt(
                    entries.count { it.unlocked },
                    entries.size
                ),
                color = InkMuted,
                fontSize = 11.sp,
                fontFamily = PixelFont
            )
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                SortChip(
                    label = UiStrings.sortByOrder(lang),
                    selected = sort == ScrapbookSort.BY_ORDER,
                    onClick = { onSortChange(ScrapbookSort.BY_ORDER) }
                )
                SortChip(
                    label = UiStrings.sortByScore(lang),
                    selected = sort == ScrapbookSort.BY_BEST,
                    onClick = { onSortChange(ScrapbookSort.BY_BEST) }
                )
            }
        }

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.weight(1f)
        ) {
            items(entries, key = { it.stageId }) { entry ->
                ScrapRow(entry, onOpen = { onOpen(entry.stageId) })
            }
        }

        Text(
            text = UiStrings.contentDisclaimerShort(lang),
            color = InkFaint,
            fontSize = 9.sp,
            fontFamily = PixelFont,
            lineHeight = 12.sp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 6.dp)
        )
    }
}

@Composable
private fun SortChip(
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
        modifier = mod
            .uiClickable(kind = UiSoundKind.SELECT, onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 3.dp)
    )
}

@Composable
private fun ScrapRow(entry: ScrapbookEntry, onOpen: () -> Unit) {
    val lang = LocalDisplayLanguage.current
    val isHighScore = entry.unlocked && entry.bestAsset != null &&
        SyncRating.stars(entry.bestAsset, entry.referenceTheoAsset) >= 3
    val rowModifier = when {
        isHighScore -> Modifier.lcdDoubleBorder()
        entry.unlocked -> Modifier.lcdBorder()
        else -> Modifier.lcdBorder(InkFaint)
    }
    var peekReveal by remember(entry.stageId) { mutableFloatStateOf(0f) }
    val scope = rememberCoroutineScope()

    Column(
        modifier = rowModifier
            .fillMaxWidth()
            .background(
                if (entry.unlocked) LcdBackground
                else LcdBackground.copy(alpha = 0.7f)
            )
            .then(if (!entry.unlocked) Modifier.lcdHatch() else Modifier)
            .lcdSquish(
                onTap = if (entry.unlocked) {
                    onOpen
                } else {
                    null
                },
                tapSound = if (entry.unlocked) UiSoundKind.SELECT else null,
                onLongPress = if (!entry.unlocked && entry.macroShape.isNotBlank()) {
                    {
                        scope.launch {
                            peekReveal = 1f
                            delay(220)
                            peekReveal = 0f
                        }
                    }
                } else {
                    null
                },
                longPressSound = UiSoundKind.SELECT
            )
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = if (entry.unlocked) {
                    UiStrings.instrumentWithCalendarRange(
                        entry.instrumentName,
                        entry.calendarStart,
                        entry.calendarEnd,
                        lang
                    )
                } else {
                    "----"
                },
                color = if (entry.unlocked) InkMuted else InkFaint,
                fontSize = 10.sp,
                fontFamily = PixelFont
            )
            Text(
                text = if (entry.unlocked && entry.bestAsset != null) {
                    "${UiStrings.bestLabel(lang)} %s  %s".fmt(
                        UiStrings.formatSignedPercent(SyncRating.returnPercent(entry.bestAsset)),
                        SyncRating.starsText(entry.bestAsset, entry.referenceTheoAsset)
                    )
                } else {
                    UiStrings.locked(lang)
                },
                color = if (entry.unlocked) Ink else InkFaint,
                fontSize = 11.sp,
                fontFamily = PixelFont,
                fontWeight = FontWeight.Bold
            )
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp),
            verticalAlignment = Alignment.Top
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (entry.unlocked) entry.brandName else "????????????",
                    color = if (entry.unlocked) Ink else InkFaint,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = PixelFont,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
            if (entry.unlocked) {
                StageIllustrationThumb(
                    entry = entry,
                    modifier = Modifier.padding(start = 8.dp, top = 2.dp)
                )
            } else {
                MacroShapeSilhouetteLocked(
                    peekMacroShape = entry.macroShape,
                    reveal = peekReveal,
                    modifier = Modifier.padding(start = 8.dp, top = 2.dp)
                )
            }
        }
    }
}
