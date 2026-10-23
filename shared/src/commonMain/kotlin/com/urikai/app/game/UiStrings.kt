package com.urikai.app.game

object UiStrings {

    fun buy(lang: DisplayLanguage): String = when (lang) {
        DisplayLanguage.EN -> "BUY"
        DisplayLanguage.JP -> "買う"
    }

    fun sell(lang: DisplayLanguage): String = when (lang) {
        DisplayLanguage.EN -> "SELL"
        DisplayLanguage.JP -> "売る"
    }

    fun start(lang: DisplayLanguage): String = when (lang) {
        DisplayLanguage.EN -> "START"
        DisplayLanguage.JP -> "開始"
    }

    /** RESULT の主ボタン。即リスタートではなく START（銘柄選択）へ戻る。 */
    fun retry(lang: DisplayLanguage): String = when (lang) {
        DisplayLanguage.EN -> "HOME"
        DisplayLanguage.JP -> "ホームへ"
    }

    fun back(lang: DisplayLanguage): String = when (lang) {
        DisplayLanguage.EN -> "BACK"
        DisplayLanguage.JP -> "戻る"
    }

    fun orderProcessing(lang: DisplayLanguage): String = when (lang) {
        DisplayLanguage.EN -> "ORDER..."
        DisplayLanguage.JP -> "注文中..."
    }

    fun scrapButton(lang: DisplayLanguage): String = when (lang) {
        DisplayLanguage.EN -> "SCRAP"
        DisplayLanguage.JP -> "図鑑"
    }

    fun configButton(lang: DisplayLanguage): String = when (lang) {
        DisplayLanguage.EN -> "CONFIG"
        DisplayLanguage.JP -> "設定"
    }

    fun timeLabel(lang: DisplayLanguage): String = "TIME"

    fun assetLabel(lang: DisplayLanguage): String = when (lang) {
        DisplayLanguage.EN -> "BALANCE"
        DisplayLanguage.JP -> "口座残高"
    }

    /** 終盤・FINISH 中の残高マスク（入金演出まで隠す）。 */
    fun assetConcealed(lang: DisplayLanguage): String = when (lang) {
        DisplayLanguage.EN -> "BALANCE  ???"
        DisplayLanguage.JP -> "口座残高  ???"
    }

    /** プレイ中 HUD: 見せ株価と開始時点からの増減率。 */
    fun quoteHud(price: Int, changePercent: Float, lang: DisplayLanguage): String {
        val label = when (lang) {
            DisplayLanguage.EN -> "PRICE"
            DisplayLanguage.JP -> "株価"
        }
        return "$label ¥%,d  %s".fmt(price, formatSignedPercent(changePercent))
    }

    /** 保有中 HUD: 取得単価（数字を増やさず一行）。 */
    fun costBasisHud(price: Int, lang: DisplayLanguage): String = when (lang) {
        DisplayLanguage.EN -> "BUY ¥%,d".fmt(price)
        DisplayLanguage.JP -> "買 ¥%,d".fmt(price)
    }

    fun shareButton(lang: DisplayLanguage): String = when (lang) {
        DisplayLanguage.EN -> "SHARE"
        DisplayLanguage.JP -> "共有"
    }

    fun shareChooserTitle(lang: DisplayLanguage): String = when (lang) {
        DisplayLanguage.EN -> "Share URIKAI"
        DisplayLanguage.JP -> "URIKAIを共有"
    }

    fun lifeMinusOne(): String = "LIFE -1"

    /** yenLeft は ¥10,000 換算の差額。¥100 = 1%。 */
    fun chaseHintStar(nextStars: Int, yenLeft: Int, lang: DisplayLanguage): String {
        val pct = "+%.1f%%".fmt(yenLeft / 100f)
        val label = if (nextStars > 5) "5+" else nextStars.toString()
        return when (lang) {
            DisplayLanguage.EN -> "★$label in $pct"
            DisplayLanguage.JP -> "★${label}まであと$pct"
        }
    }

    fun locked(lang: DisplayLanguage): String = when (lang) {
        DisplayLanguage.EN -> "LOCKED"
        DisplayLanguage.JP -> "未解放"
    }

    fun bestLabel(lang: DisplayLanguage): String = "BEST"

    /** 初回クリア直後の出来事カード（自動で図鑑に入る）。 */
    fun resultNewsLabel(lang: DisplayLanguage): String = when (lang) {
        DisplayLanguage.EN -> "NEWS"
        DisplayLanguage.JP -> "ニュース"
    }

    /** 自己ベスト更新スタンプ（リザルト星付近）。 */
    fun bestStamp(lang: DisplayLanguage): String = when (lang) {
        DisplayLanguage.EN -> "BEST!"
        DisplayLanguage.JP -> "新記録"
    }

    fun newBadge(): String = "NEW!"

    /** 結果タイトルがニュース（図鑑詳細）を開けることの合図。 */
    fun resultNewsOpenCue(lang: DisplayLanguage): String = when (lang) {
        DisplayLanguage.EN -> "NEWS"
        DisplayLanguage.JP -> "ニュース"
    }

    fun sortByOrder(lang: DisplayLanguage): String = when (lang) {
        DisplayLanguage.EN -> "NO."
        DisplayLanguage.JP -> "番号順"
    }

    fun sortByScore(lang: DisplayLanguage): String = when (lang) {
        DisplayLanguage.EN -> "BEST"
        DisplayLanguage.JP -> "BEST順"
    }

    /** リザルト直後のボタン（英語固定） */
    fun timeUp(): String = "TIME UP"

    fun screenTitle(appLayer: AppLayer, lang: DisplayLanguage): String = when (appLayer) {
        AppLayer.OPTION -> if (lang == DisplayLanguage.JP) "設定" else "CONFIG"
        AppLayer.SCRAPBOOK -> if (lang == DisplayLanguage.JP) "図鑑" else "SCRAP"
        AppLayer.PLAY -> "URIKAI"
    }

    fun settingLabel(key: String, lang: DisplayLanguage): String =
        if (lang == DisplayLanguage.JP) SETTING_LABELS_JP[key] ?: key else key

    private val SETTING_LABELS_JP = mapOf(
        "LANG" to "言語",
        "TONE" to "明るさ",
        "SE" to "効果音",
        "BGM" to "BGM",
        "HAPTIC" to "振動",
        "HUD" to "表示",
        "SCAN" to "走査線",
        "CONTRAST" to "コントラスト",
        "DIM" to "暗い",
        "NORM" to "標準",
        "BRIGHT" to "明るい",
        "ON" to "オン",
        "OFF" to "オフ",
        "LOW" to "弱",
        "MID" to "中",
        "HIGH" to "強",
        "FULL" to "すべて",
        "FOCUS" to "シンプル",
        "SOFT" to "弱",
        "HARD" to "強",
        "ADS" to "広告",
        "REMOVE" to "なしにする",
        "OWNED" to "購入済",
        "RESTORE" to "復元",
    )

    fun creditsBody(lang: DisplayLanguage): String = when (lang) {
        DisplayLanguage.EN ->
            "Font: DotGothic16 (SIL OFL 1.1) © The DotGothic16 Project Authors.\n" +
                "SE: original WAV by URIKAI.\n" +
                "BGM: original tracks by URIKAI."
        DisplayLanguage.JP ->
            "Font: DotGothic16 (SIL OFL 1.1) © The DotGothic16 Project Authors.\n" +
                "SE: URIKAI オリジナルWAV。\n" +
                "BGM: URIKAI オリジナル。"
    }

    fun resultFlavorGhost(lang: DisplayLanguage): String = when (lang) {
        DisplayLanguage.EN -> "GHOST"
        DisplayLanguage.JP -> "取引なし"
    }

    fun resultFlavorBreakEven(lang: DisplayLanguage): String = when (lang) {
        DisplayLanguage.EN -> "BREAK EVEN"
        DisplayLanguage.JP -> "プラマイゼロ"
    }

    val retireFlavorCount: Int get() = 6

    fun retireFlavorLine1(index: Int, lang: DisplayLanguage): String {
        val i = index.coerceIn(0, retireFlavorCount - 1)
        return when (lang) {
            DisplayLanguage.JP -> when (i) {
                0 -> "下落への不安と損切り"
                1 -> "焦りによる連続売却"
                2 -> "度重なる狼狽売り"
                3 -> "損失での売却オーバー"
                4 -> "下落に耐え切れず断念"
                else -> "感情的な売却の繰り返し"
            }
            DisplayLanguage.EN -> when (i) {
                0 -> "Fear of the drop"
                1 -> "Panic sells in a row"
                2 -> "Repeated capitulation"
                3 -> "Too many loss exits"
                4 -> "Couldn't ride the dip"
                else -> "Emotional selling"
            }
        }
    }

    fun retireFlavorLine2(index: Int, lang: DisplayLanguage): String {
        val i = index.coerceIn(0, retireFlavorCount - 1)
        return when (lang) {
            DisplayLanguage.JP -> when (i) {
                0 -> "値下がりの恐怖で売却を重ねてしまいました。"
                1 -> "マイナスでの売却が続き、運用失敗となりました。"
                2 -> "一時的な下落に耐えず、手放してしまいました。"
                3 -> "MISSが重なり、途中でリタイアとなりました。"
                4 -> "安値での売却を繰り返し、完走失敗です。"
                else -> "値下がり時の売却が増え、リタイアしました。"
            }
            DisplayLanguage.EN -> when (i) {
                0 -> "Fear of falling prices led to repeated sells."
                1 -> "Loss exits piled up — the run failed."
                2 -> "Couldn't hold through a temporary drop."
                3 -> "MISSes stacked and forced a mid-run retire."
                4 -> "Selling low again and again — DNF."
                else -> "More sells on dips ended in retirement."
            }
        }
    }

    fun retireBanner(): String = "RETIRE"

    fun chartsLoadErrorTitle(lang: DisplayLanguage): String = when (lang) {
        DisplayLanguage.EN -> "DATA ERROR"
        DisplayLanguage.JP -> "データエラー"
    }

    fun chartsLoadErrorBody(detail: String, lang: DisplayLanguage): String = when (lang) {
        DisplayLanguage.EN -> "Could not load charts.csv.\nReinstall the app.\n\n$detail"
        DisplayLanguage.JP -> "charts.csv を読めませんでした。\nアプリを再インストールしてください。\n\n$detail"
    }

    fun markerLabel(isBuy: Boolean, price: Int, lang: DisplayLanguage): String {
        val side = if (isBuy) buy(lang) else sell(lang)
        return "$side ¥%,d".fmt(price)
    }

    /** 約定の良し悪し（＋得・−損）。▲▽ は買い・売りの印なので使わない。 */
    fun fillQualityMark(isBuy: Boolean, quality: FillQuality): String =
        lagQualityMark(isBuy, quality)

    fun lagQualityMark(isBuy: Boolean, quality: FillQuality): String = when (quality) {
        FillQuality.OK -> "·"
        FillQuality.FANTASTIC, FillQuality.PERFECT, FillQuality.GREAT -> "+"
        FillQuality.MISS -> "−"
    }

    fun lagQualityMark(isBuy: Boolean, submitPrice: Int, fillPrice: Int): String =
        lagQualityMark(isBuy, FillEvaluator.evaluateLag(isBuy, submitPrice, fillPrice))

    fun fillJudgmentLabel(quality: FillQuality, lang: DisplayLanguage): String = when (quality) {
        FillQuality.FANTASTIC -> "FANTASTIC!"
        FillQuality.PERFECT -> "PERFECT"
        FillQuality.GREAT -> "GREAT"
        FillQuality.OK -> "OK"
        FillQuality.MISS -> "MISS"
    }

    fun fillComboLabel(combo: Int, lang: DisplayLanguage): String = when (lang) {
        DisplayLanguage.EN -> "COMBO x$combo"
        DisplayLanguage.JP -> "COMBO x$combo"
    }

    fun payoutClaimTitle(lang: DisplayLanguage): String = when (lang) {
        DisplayLanguage.EN -> "BALANCE"
        DisplayLanguage.JP -> "口座残高"
    }

    /** 符号付き金額のみ（例: +¥1,200 / −¥300）。ラベルなし。 */
    fun formatSignedYen(amount: Int): String = formatSignedYen(amount.toLong())

    fun formatSignedYen(amount: Long): String {
        val sign = if (amount >= 0) "+" else "−"
        return "$sign¥%,d".fmt(kotlin.math.abs(amount))
    }

    /** 増減率（例: +12.3% / −4.0%）。 */
    fun formatSignedPercent(percent: Float): String {
        val sign = if (percent >= 0f) "+" else "−"
        return "$sign%.1f%%".fmt(kotlin.math.abs(percent))
    }

    fun balanceLabel(lang: DisplayLanguage): String = when (lang) {
        DisplayLanguage.EN -> "BALANCE"
        DisplayLanguage.JP -> "口座残高"
    }

    /** START 口座残高1行。 */
    fun balanceLine(balance: Long, lang: DisplayLanguage): String =
        "${balanceLabel(lang)}  ¥%,d".fmt(balance)

    /** レリック閾値（初期残高からの増加額）を残高表記に。 */
    private fun relicBalanceYen(threshold: Int): String =
        "¥%,d".fmt(threshold + com.urikai.app.data.LifetimePnlRepository.INITIAL_BALANCE)

    fun relicThresholdReached(threshold: Int, lang: DisplayLanguage): String {
        val yen = relicBalanceYen(threshold)
        return when (lang) {
            DisplayLanguage.EN -> "Balance $yen reached"
            DisplayLanguage.JP -> "口座残高 $yen 到達"
        }
    }

    /** 報酬箱向け。値札に見えないよう「残高」付き・控えめ。 */
    fun relicThresholdQuiet(threshold: Int, lang: DisplayLanguage): String {
        val yen = relicBalanceYen(threshold)
        return when (lang) {
            DisplayLanguage.EN -> "Balance $yen"
            DisplayLanguage.JP -> "口座残高 $yen"
        }
    }

    fun bankruptTitle(lang: DisplayLanguage): String = when (lang) {
        DisplayLanguage.EN -> "BANKRUPT"
        DisplayLanguage.JP -> "破産"
    }

    fun bankruptBody(balance: Long, lang: DisplayLanguage): String = when (lang) {
        DisplayLanguage.EN -> "Your balance fell to ¥%,d.\nStart over from ¥10,000.".fmt(balance)
        DisplayLanguage.JP -> "口座残高が ¥%,d になってしまった…\n¥10,000 から出直そう。".fmt(balance)
    }

    fun bankruptRestart(lang: DisplayLanguage): String = when (lang) {
        DisplayLanguage.EN -> "RESTART WITH ¥10,000"
        DisplayLanguage.JP -> "¥10,000 で再スタート"
    }

    fun relicGiftBadge(lang: DisplayLanguage): String = when (lang) {
        DisplayLanguage.EN -> "CLAIM"
        DisplayLanguage.JP -> "受取"
    }

    /** RESULT 導入後の新規解禁誘導ポップ。 */
    fun relicRewardToastTitle(lang: DisplayLanguage): String = when (lang) {
        DisplayLanguage.EN -> "★ UNLOCK! ★"
        DisplayLanguage.JP -> "★ 解禁！ ★"
    }

    fun relicRewardToastBody(lang: DisplayLanguage): String = when (lang) {
        DisplayLanguage.EN -> "Claim it with the button on the START screen!"
        DisplayLanguage.JP -> "開始画面の「受取」から受け取ろう！"
    }

    /** 受取直後のバナー。 */
    fun relicGetBanner(lang: DisplayLanguage): String = when (lang) {
        DisplayLanguage.EN -> "★ GET! ★"
        DisplayLanguage.JP -> "★ ゲット！ ★"
    }

    /** 受取後の置き場誘導（カテゴリ分割後）。 */
    fun relicPlacementHint(id: RelicId, lang: DisplayLanguage): String = when (id.placement()) {
        UnlockPlacement.LOOK -> when (lang) {
            DisplayLanguage.EN -> "Open ＊ (bottom-left) to change look / FX"
            DisplayLanguage.JP -> "左下の＊から見た目・演出を変えられる"
        }
        UnlockPlacement.PLAY -> when (lang) {
            DisplayLanguage.EN -> "Peel the title-card corner for the stamp album"
            DisplayLanguage.JP -> "タイトルカードの角をめくると切手帳へ"
        }
        UnlockPlacement.INVEST -> when (lang) {
            DisplayLanguage.EN -> "Check it under Balance → Policy"
            DisplayLanguage.JP -> "口座の「運用方針」で確認できる"
        }
    }

    fun relicBoxLabel(lang: DisplayLanguage): String = when (lang) {
        DisplayLanguage.EN -> "LOOK"
        DisplayLanguage.JP -> "見た目"
    }

    fun stylePageTitle(lang: DisplayLanguage): String = when (lang) {
        DisplayLanguage.EN -> "＊"
        DisplayLanguage.JP -> "＊"
    }

    fun stylePageEmpty(lang: DisplayLanguage): String = when (lang) {
        DisplayLanguage.EN -> "Nothing here… yet."
        DisplayLanguage.JP -> "まだ何もない…"
    }

    fun styleLookName(id: RelicId, lang: DisplayLanguage): String = when (lang) {
        DisplayLanguage.JP -> when (id) {
            RelicId.PAPER -> "電子ペーパー"
            RelicId.SMOOTH -> "なめらか線"
            RelicId.DARK -> "反転"
            RelicId.JUDGE -> "判定の華やかさ"
            RelicId.COMBO -> "COMBOの華やかさ"
            else -> relicName(id, lang)
        }
        DisplayLanguage.EN -> when (id) {
            RelicId.PAPER -> "E-paper"
            RelicId.SMOOTH -> "Smooth line"
            RelicId.DARK -> "Invert"
            RelicId.JUDGE -> "Judge flourish"
            RelicId.COMBO -> "COMBO flourish"
            else -> relicName(id, lang)
        }
    }

    fun styleLookBlurb(id: RelicId, lang: DisplayLanguage): String = when (lang) {
        DisplayLanguage.JP -> when (id) {
            RelicId.PAPER -> "画面の質感が変わる。"
            RelicId.SMOOTH -> "チャートの結び方が変わる。"
            RelicId.DARK -> "（廃止）以前の反転テーマ用。"
            RelicId.JUDGE -> "判定の演出が派手になる。"
            RelicId.COMBO -> "COMBO表示が派手になる。"
            else -> relicDescription(id, lang)
        }
        DisplayLanguage.EN -> when (id) {
            RelicId.PAPER -> "Changes the screen texture."
            RelicId.SMOOTH -> "Changes how the chart connects."
            RelicId.DARK -> "(Retired) Former invert theme."
            RelicId.JUDGE -> "Judgment FX gets flashier."
            RelicId.COMBO -> "COMBO display gets flashier."
            else -> relicDescription(id, lang)
        }
    }

    fun investYieldChip(mult: Float, lang: DisplayLanguage): String {
        val tenths = kotlin.math.round(mult * 10f).toInt()
        val value = if (tenths % 10 == 0) {
            "×${tenths / 10}"
        } else {
            "×${tenths / 10}.${tenths % 10}"
        }
        return when (lang) {
            DisplayLanguage.EN -> "Yield $value"
            DisplayLanguage.JP -> "利回り$value"
        }
    }

    fun investAwayChip(days: Int, lang: DisplayLanguage): String = when (lang) {
        DisplayLanguage.EN -> "Cap ${days}d"
        DisplayLanguage.JP -> "反映${days}日"
    }

    fun investYieldTip(mult: Float, lang: DisplayLanguage): String {
        val pctUp = kotlin.math.round((mult - 1f) * 100f).toInt().coerceAtLeast(0)
        return when (lang) {
            DisplayLanguage.EN ->
                if (pctUp <= 0) "Daily plan gains: normal"
                else "Daily plan gains up $pctUp%"
            DisplayLanguage.JP ->
                if (pctUp <= 0) "毎日の運用益はそのまま"
                else "毎日の運用益が${pctUp}%アップする"
        }
    }

    fun investAwayTip(days: Int, lang: DisplayLanguage): String {
        val chip = investAwayChip(days, lang)
        val next = days + 1
        return when (lang) {
            DisplayLanguage.EN ->
                "$chip\nWhen you return, at most $days days of\ndaily plan moves are applied.\nDay $next and beyond are skipped."
            DisplayLanguage.JP ->
                "$chip\nアプリを開けなかったあいだの値動きは、\n最大${days}日分までまとめて反映。\n${next}日目以降は切り捨て。"
        }
    }

    fun styleOn(lang: DisplayLanguage): String = when (lang) {
        DisplayLanguage.EN -> "ON"
        DisplayLanguage.JP -> "ON"
    }

    fun styleOff(lang: DisplayLanguage): String = when (lang) {
        DisplayLanguage.EN -> "OFF"
        DisplayLanguage.JP -> "OFF"
    }

    /** きせかえ行の操作ボタン。常に「押したあとの行為」を書く（状態表示ではない）。 */
    fun styleActionUse(lang: DisplayLanguage): String = when (lang) {
        DisplayLanguage.EN -> "USE"
        DisplayLanguage.JP -> "使う"
    }

    fun styleActionRemove(lang: DisplayLanguage): String = when (lang) {
        DisplayLanguage.EN -> "REMOVE"
        DisplayLanguage.JP -> "外す"
    }

    fun milestoneNextShort(balanceYen: Long, lang: DisplayLanguage): String = when (lang) {
        DisplayLanguage.EN -> "Next: ¥%,d".fmt(balanceYen)
        DisplayLanguage.JP -> "Next: ¥%,d".fmt(balanceYen)
    }

    fun milestoneCompleteShort(lang: DisplayLanguage): String = when (lang) {
        DisplayLanguage.EN -> "Max"
        DisplayLanguage.JP -> "Max"
    }

    fun playBuffBackHint(lang: DisplayLanguage): String = when (lang) {
        DisplayLanguage.EN -> "Tap a mount"
        DisplayLanguage.JP -> "マウントをタップ"
    }

    fun playBuffStampLocked(slot: PlayBuffSlot, lang: DisplayLanguage): String = when (lang) {
        DisplayLanguage.EN -> "${playBuffSlotName(slot, lang)}: locked"
        DisplayLanguage.JP -> "${playBuffSlotName(slot, lang)}: 未解禁"
    }

    fun playBuffTip(
        slot: PlayBuffSlot,
        active: RelicId?,
        lang: DisplayLanguage
    ): String {
        if (active == null) {
            return when (lang) {
                DisplayLanguage.EN -> "${playBuffSlotName(slot, lang)}: OFF (tap to enable)"
                DisplayLanguage.JP -> "${playBuffSlotName(slot, lang)}: OFF（タップでON）"
            }
        }
        return "${playBuffSlotName(slot, lang)}: ${relicName(active, lang)}"
    }

    fun playBuffSlotName(slot: PlayBuffSlot, lang: DisplayLanguage): String = when (lang) {
        DisplayLanguage.EN -> when (slot) {
            PlayBuffSlot.COOLDOWN -> "Charge"
            PlayBuffSlot.LAG -> "Lag"
            PlayBuffSlot.PNL -> "Win bonus"
            PlayBuffSlot.LIFE -> "LIFE"
            PlayBuffSlot.COMBO_HEAL -> "COMBO heal"
        }
        DisplayLanguage.JP -> when (slot) {
            PlayBuffSlot.COOLDOWN -> "チャージ"
            PlayBuffSlot.LAG -> "ラグ"
            PlayBuffSlot.PNL -> "勝ち幅"
            PlayBuffSlot.LIFE -> "LIFE"
            PlayBuffSlot.COMBO_HEAL -> "COMBO回復"
        }
    }

    fun relicReceive(lang: DisplayLanguage): String = when (lang) {
        DisplayLanguage.EN -> "RECEIVE"
        DisplayLanguage.JP -> "受け取る"
    }

    fun relicApply(lang: DisplayLanguage): String = when (lang) {
        DisplayLanguage.EN -> "APPLY"
        DisplayLanguage.JP -> "適用"
    }

    fun relicCycleStage(lang: DisplayLanguage): String = when (lang) {
        DisplayLanguage.EN -> "SWAP"
        DisplayLanguage.JP -> "切替"
    }

    /** 段階グループで、いま効果に乗っている段階。 */
    fun relicStageActive(lang: DisplayLanguage): String = when (lang) {
        DisplayLanguage.EN -> "ACTIVE"
        DisplayLanguage.JP -> "適用中"
    }

    fun relicStageStacked(lang: DisplayLanguage): String = when (lang) {
        DisplayLanguage.EN -> "STACKED"
        DisplayLanguage.JP -> "累積"
    }

    fun relicRevertToGb(lang: DisplayLanguage): String = when (lang) {
        DisplayLanguage.EN -> "Back to GB"
        DisplayLanguage.JP -> "GBに戻す"
    }

    fun relicRevertToStep(lang: DisplayLanguage): String = when (lang) {
        DisplayLanguage.EN -> "Back to STEP"
        DisplayLanguage.JP -> "STEPに戻す"
    }

    fun sessionPnlHistoryTitle(lang: DisplayLanguage): String = when (lang) {
        DisplayLanguage.EN -> "STATEMENT"
        DisplayLanguage.JP -> "お取引明細"
    }

    fun sessionHistoryRestartLabel(lang: DisplayLanguage): String = when (lang) {
        DisplayLanguage.EN -> "Bankrupt restart → ¥10,000"
        DisplayLanguage.JP -> "破産リセット → ¥10,000"
    }

    fun balanceFromStartLine(returnPercent: Float, lang: DisplayLanguage): String = when (lang) {
        DisplayLanguage.EN -> "FROM START ${formatSignedPercent(returnPercent)}"
        DisplayLanguage.JP -> "開始から ${formatSignedPercent(returnPercent)}"
    }

    fun balancePeakLine(peak: Long, lang: DisplayLanguage): String = when (lang) {
        DisplayLanguage.EN -> "PEAK ¥%,d".fmt(peak)
        DisplayLanguage.JP -> "最高 ¥%,d".fmt(peak)
    }

    fun investPlanTitle(lang: DisplayLanguage): String = when (lang) {
        DisplayLanguage.EN -> "POLICY"
        DisplayLanguage.JP -> "運用方針"
    }

    fun investPlanDetailChip(lang: DisplayLanguage): String = when (lang) {
        DisplayLanguage.EN -> "?"
        DisplayLanguage.JP -> "?"
    }

    /**
     * カード上の唯一の数値タグ。性格ラベルとの重複は出さない。
     * 確定（預金）は1日あたり利率、変動はぶれ幅 ±N%。
     */
    fun investPlanTemperTag(plan: InvestPlan, lang: DisplayLanguage): String {
        if (plan.isGuaranteed) {
            val rate = formatDailyRate(plan.dailyMeanPercent)
            return when (lang) {
                DisplayLanguage.EN -> "+$rate%/day"
                DisplayLanguage.JP -> "+$rate%/日"
            }
        }
        val vol = plan.dailyVolPercent.toInt()
        return "±$vol%"
    }

    private fun formatDailyRate(percent: Float): String {
        val t = "%.1f".fmt(percent)
        return t.trimEnd('0').trimEnd('.')
    }

    /** 後方互換。 temper タグへ。 */
    fun investPlanRate(plan: InvestPlan, lang: DisplayLanguage): String =
        investPlanTemperTag(plan, lang)

    fun investPlanSwingRange(plan: InvestPlan, lang: DisplayLanguage): String =
        investPlanTemperTag(plan, lang)

    /**
     * 商品そのものの短いフレーバー（くどくしない）。
     * カード1文目／説明チップの1文目。
     */
    fun investPlanFlavor(plan: InvestPlan, lang: DisplayLanguage): String = when (lang) {
        DisplayLanguage.EN -> when (plan) {
            InvestPlan.DEPOSIT -> "Park cash in a bank book."
            InvestPlan.BALANCED -> "A fund mixing stocks and bonds."
            InvestPlan.GROWTH -> "A fund tilted toward growing companies."
            InvestPlan.EMERGING -> "A fund invested in emerging markets."
            InvestPlan.ACORN -> "Drip-buy Acorn Coin over time."
        }
        DisplayLanguage.JP -> when (plan) {
            InvestPlan.DEPOSIT -> "銀行の通帳に預けるタイプ。"
            InvestPlan.BALANCED -> "株と債券をまぜた投資信託。"
            InvestPlan.GROWTH -> "成長株に寄せた投資信託。"
            InvestPlan.EMERGING -> "新興国の相場に投資するファンド。"
            InvestPlan.ACORN -> "ドングリコインを少しずつ買い足す積立。"
        }
    }

    /**
     * 毎日どう動くか（挙動）。カード2文目。
     * 旧キャッチ／旧説明1文目をここへ寄せる。
     */
    fun investPlanCatch(plan: InvestPlan, lang: DisplayLanguage): String = when (lang) {
        DisplayLanguage.EN -> when (plan) {
            InvestPlan.DEPOSIT -> "Grows a set bit each day. Almost no swing."
            InvestPlan.BALANCED -> "Tends to grow; swings stay mild."
            InvestPlan.GROWTH -> "Chases upside—sometimes misses."
            InvestPlan.EMERGING -> "Bigger drift, bigger daily weather."
            InvestPlan.ACORN -> "Feast possible. Empty nest too."
        }
        DisplayLanguage.JP -> when (plan) {
            InvestPlan.DEPOSIT -> "毎日ほぼ決まった分だけ増える。"
            InvestPlan.BALANCED -> "増えやすく、ぶれは小さめ。"
            InvestPlan.GROWTH -> "伸びやすいが、空振りもある。"
            InvestPlan.EMERGING -> "伸びもぶれも大きめ。"
            InvestPlan.ACORN -> "大勝ちも、巣が空も。"
        }
    }

    /**
     * 説明チップ用。1文目=商品フレーバー、2文目=挙動。
     * 預金だけ留守反映の一文を足す。
     */
    fun investPlanBlurb(plan: InvestPlan, lang: DisplayLanguage): String {
        val flavor = investPlanFlavor(plan, lang)
        val behavior = investPlanCatch(plan, lang)
        val away = when {
            plan != InvestPlan.DEPOSIT -> ""
            lang == DisplayLanguage.EN ->
                " Away days land in a lump when you return."
            else ->
                " 開けていなかった日の分も、戻った朝にまとめて入る。"
        }
        return "$flavor $behavior$away"
    }

    fun investPlanLocked(unlockPeak: Long, lang: DisplayLanguage): String = when (lang) {
        DisplayLanguage.EN -> "PEAK ¥%,d to unlock".fmt(unlockPeak)
        DisplayLanguage.JP -> "最高口座残高 ¥%,d で解放".fmt(unlockPeak)
    }

    fun investPlanSelect(lang: DisplayLanguage): String = when (lang) {
        DisplayLanguage.EN -> "TAP TO USE"
        DisplayLanguage.JP -> "タップで選ぶ"
    }

    fun investPlanSelected(lang: DisplayLanguage): String = when (lang) {
        DisplayLanguage.EN -> "IN USE"
        DisplayLanguage.JP -> "選択中"
    }

    fun investPlanStripLine(plan: InvestPlan, lang: DisplayLanguage): String =
        "${investPlanTitle(lang)} · ${plan.displayName(lang)}  ${investPlanTemperTag(plan, lang)}"

    fun awayReportTitle(days: Int, lang: DisplayLanguage): String = when (lang) {
        DisplayLanguage.EN -> if (days == 1) "While you were away (1 day)" else "While you were away ($days days)"
        DisplayLanguage.JP -> "留守の間（${days}日）の運用"
    }

    fun balanceHistoryEmpty(lang: DisplayLanguage): String = when (lang) {
        DisplayLanguage.EN -> "Play a round to start the chart"
        DisplayLanguage.JP -> "勝負するとグラフが始まります"
    }

    fun historyNewestMark(): String = "NEW"

    fun sessionPnlHistoryEmpty(lang: DisplayLanguage): String = when (lang) {
        DisplayLanguage.EN -> "No history yet"
        DisplayLanguage.JP -> "まだ履歴がありません"
    }

    /** チュート／ガイドの説明役。 */
    fun guideBuddyName(lang: DisplayLanguage): String = when (lang) {
        DisplayLanguage.EN -> "KAI"
        DisplayLanguage.JP -> "カイ"
    }

    fun firstPlayTutorialNext(lang: DisplayLanguage): String = when (lang) {
        DisplayLanguage.EN -> "NEXT"
        DisplayLanguage.JP -> "つぎ"
    }

    fun firstPlayTutorialStart(lang: DisplayLanguage): String = when (lang) {
        DisplayLanguage.EN -> "START"
        DisplayLanguage.JP -> "はじめる"
    }

    fun notebookClose(lang: DisplayLanguage): String = when (lang) {
        DisplayLanguage.EN -> "CLOSE"
        DisplayLanguage.JP -> "とじる"
    }

    /** ガイド枠右下の進むサイン（大ボタンの代わり）。画素フォントでも欠けない記号。 */
    fun guideAdvanceCue(actionLabel: String, lang: DisplayLanguage): String = when (lang) {
        DisplayLanguage.EN -> "> $actionLabel"
        DisplayLanguage.JP -> "> $actionLabel"
    }

    fun notebookButton(lang: DisplayLanguage): String = when (lang) {
        DisplayLanguage.EN -> "NOTES"
        DisplayLanguage.JP -> "ノート"
    }

    /** カイの節目ひとこと（Away / 破産 / RETIRE）。 */
    fun kaiAwayLine(diff: Long, lang: DisplayLanguage): String = when (lang) {
        DisplayLanguage.JP -> if (diff >= 0) {
            "日付、進んでたよ。\nそのぶん、口座にも入ってる。"
        } else {
            "日付、進んでたよ。\n……口座のほうも、動いてる。"
        }
        DisplayLanguage.EN -> if (diff >= 0) {
            "Days moved on.\nThe account got the update."
        } else {
            "Days moved on.\n……The account moved too."
        }
    }

    fun kaiBankruptLine(lang: DisplayLanguage): String = when (lang) {
        DisplayLanguage.JP -> "……出直しだね。\nここから、また積み直せるよ。"
        DisplayLanguage.EN -> "……A fresh start.\nYou can rebuild from here."
    }

    fun kaiRetireLine(lang: DisplayLanguage): String = when (lang) {
        DisplayLanguage.JP -> "気力が尽きた……\nこの勝負は、ここで終わり。"
        DisplayLanguage.EN -> "Nerve's gone……\nThis round ends here."
    }

    fun notebookHubTitle(lang: DisplayLanguage): String = when (lang) {
        DisplayLanguage.EN -> "NOTES"
        DisplayLanguage.JP -> "ノート"
    }

    fun notebookEmpty(lang: DisplayLanguage): String = when (lang) {
        DisplayLanguage.EN -> "Nothing here yet.\nTips show up as you play."
        DisplayLanguage.JP -> "まだ何もないよ。\n遊んでいるうちに、メモが増えていく。"
    }

    fun notebookEntryTitle(id: NotebookEntryId, lang: DisplayLanguage): String = when (lang) {
        DisplayLanguage.JP -> when (id) {
            NotebookEntryId.HOW_TO_PLAY -> "遊び方"
            NotebookEntryId.LIFE -> "LIFE"
            NotebookEntryId.BALANCE -> "口座と残高"
            NotebookEntryId.SCRAPBOOK -> "図鑑"
            NotebookEntryId.STAMPS -> "強化の切手"
            NotebookEntryId.LAG -> "買う／売るの遅れ"
            NotebookEntryId.AWAY -> "留守のあいだ"
            NotebookEntryId.LOOK -> "見た目"
            NotebookEntryId.RESULT -> "結果の見方"
            NotebookEntryId.COMBO -> "COMBO"
            NotebookEntryId.BANK -> "破産"
        }
        DisplayLanguage.EN -> when (id) {
            NotebookEntryId.HOW_TO_PLAY -> "How to play"
            NotebookEntryId.LIFE -> "LIFE"
            NotebookEntryId.BALANCE -> "Balance"
            NotebookEntryId.SCRAPBOOK -> "Scrapbook"
            NotebookEntryId.STAMPS -> "Stamp album"
            NotebookEntryId.LAG -> "Order delay"
            NotebookEntryId.AWAY -> "While away"
            NotebookEntryId.LOOK -> "Looks"
            NotebookEntryId.RESULT -> "Reading results"
            NotebookEntryId.COMBO -> "COMBO"
            NotebookEntryId.BANK -> "Bankrupt"
        }
    }

    fun notebookPageCount(id: NotebookEntryId): Int = when (id) {
        NotebookEntryId.HOW_TO_PLAY -> 2
        NotebookEntryId.LIFE -> 2
        NotebookEntryId.BALANCE -> 3
        NotebookEntryId.STAMPS -> 2
        else -> 1
    }

    fun notebookPage(id: NotebookEntryId, step: Int, lang: DisplayLanguage): String = when (id) {
        NotebookEntryId.HOW_TO_PLAY -> when (lang) {
            DisplayLanguage.JP -> when (step) {
                0 -> "この勝負、60秒だよ。\n下のでかボタンで『買う』と『売る』を切り替えてね。"
                else -> "増やしても減らしても、最後に残ったお金がすべてだよ。\nいってみよう。"
            }
            DisplayLanguage.EN -> when (step) {
                0 -> "This round's 60 seconds.\nBig button down there flips BUY and SELL."
                else -> "Up or down — the money left at the end is everything.\nLet's go."
            }
        }
        NotebookEntryId.LIFE -> when (lang) {
            DisplayLanguage.JP -> when (step) {
                0 -> "うえのほうに、まるい印が出るようになったよ。\n気力のゲージみたいなもの。\n売りがひどく外れると、ひとつ減る。"
                else -> "なくなったら、その勝負はそこで終わり。\n気をつけていってみよう。"
            }
            DisplayLanguage.EN -> when (step) {
                0 -> "Round marks show up up top now.\nLike a nerve gauge.\nA bad sell knocks one off."
                else -> "When they're gone, that round ends there.\nTake care out there."
            }
        }
        NotebookEntryId.BALANCE -> when (lang) {
            DisplayLanguage.JP -> when (step) {
                0 -> "あ、口座ね。\n口座残高画面の一番上に見える大きな数字が、残高だよ。"
                1 -> "1回やるごとに増えたり減ったりする、その合計がここ。\nくわしいことは、また今度。"
                else -> "あとひとつ。\n現実で1日進むごとに、残高は少し動くよ。\n変えたいときは、また口座からどうぞ。"
            }
            DisplayLanguage.EN -> when (step) {
                0 -> "Ah — the account.\nThe big number at the top of the balance screen is the balance."
                1 -> "It goes up and down each round; the total lives here.\nDetails later."
                else -> "One more thing.\nEach real-world day, the balance shifts a little.\nWant a change? Open the account again."
            }
        }
        NotebookEntryId.SCRAPBOOK -> when (lang) {
            DisplayLanguage.JP ->
                "クリアした銘柄は、図鑑に残るよ。\nまた読みたくなったら、ここへきてね。"
            DisplayLanguage.EN ->
                "Cleared names stay in the scrapbook.\nCome back here whenever you want a reread."
        }
        NotebookEntryId.STAMPS -> when (lang) {
            DisplayLanguage.JP -> when (step) {
                0 -> "……ふうん。\nこんなのがあったんだ。"
                else -> "受け取った強化が、ここに貼られてるみたい。\nつけ外しもできるよ。\n……ほかにも、まだ空きがあるね。"
            }
            DisplayLanguage.EN -> when (step) {
                0 -> "……Huh.\nThis was here?"
                else -> "Claimed upgrades get stuck here.\nYou can toggle them.\n……And look — empty slots left."
            }
        }
        NotebookEntryId.LAG -> when (lang) {
            DisplayLanguage.JP ->
                "買う／売るは、下にあるでっかいボタンを押した直後には決まらないよ。\n少しあとに反映される。\n急いで連打しても、待ちが発生することがある。"
            DisplayLanguage.EN ->
                "BUY / SELL don't land the instant you hit the big button down below.\nThey settle a beat later.\nMash all you want — you'll still wait sometimes."
        }
        NotebookEntryId.AWAY -> when (lang) {
            DisplayLanguage.JP ->
                "しばらく開けてなかった日のぶんは、口座にまとめて入るよ。\n方針は口座の画面から変えられる。\n日付のほうは、待ってくれないね。"
            DisplayLanguage.EN ->
                "Days you didn't open get applied to the account in a lump.\nChange policy on the account screen.\nThe calendar doesn't wait."
        }
        NotebookEntryId.LOOK -> when (lang) {
            DisplayLanguage.JP ->
                "見た目の切り替えは、スタート画面の左下のマークからできるよ。\n強さは変わらない。\n気分の問題……いちおう、大事。"
            DisplayLanguage.EN ->
                "Looks swap from the mark at the bottom-left of the start screen.\nPower stays the same.\nMood stuff……still counts."
        }
        NotebookEntryId.RESULT -> when (lang) {
            DisplayLanguage.JP ->
                "結果の大きな表示は、その一回の増減と評価だよ。\n口座の数字をタップすると、収益のランキングも見られる。\n見なくても遊べるけどね。"
            DisplayLanguage.EN ->
                "The big result line is that run's gain/loss and rating.\nTap the balance number for a returns ranking.\nOptional, really."
        }
        NotebookEntryId.COMBO -> when (lang) {
            DisplayLanguage.JP ->
                "売りがうまく続くと COMBO が伸びるよ。\nしばらく間を空けると消えるし、MISSでも消える。\n派手さ用だね。"
            DisplayLanguage.EN ->
                "Good sells string into a COMBO.\nLeave it idle and it fades; a MISS kills it too.\nMostly for flair."
        }
        NotebookEntryId.BANK -> when (lang) {
            DisplayLanguage.JP ->
                "口座が下がりすぎると破産になるよ。\nそこからの立て直しもある。\nニュースみたいに、静かに終わるとは限らない。"
            DisplayLanguage.EN ->
                "Drop the account too low and it's bankrupt.\nThere is a restart after.\nLike the news — endings aren't always quiet."
        }
    }

    /** G5 閉じた直後の1枚（ノート3枚目と同じ文）。 */
    fun investCloseTipBody(lang: DisplayLanguage): String =
        notebookPage(NotebookEntryId.BALANCE, 2, lang)

    /** G8-1 ホーム一言（ノート非収録）。 */
    fun stampCornerHint(lang: DisplayLanguage): String = when (lang) {
        DisplayLanguage.EN -> "Huh……something feels different today……?"
        DisplayLanguage.JP -> "おや……いつもと、何かちがうような……？"
    }

    // 後方互換エイリアス（呼び出し側の段階移行用）
    fun firstPlayTutorialStep(step: Int, lang: DisplayLanguage): String =
        notebookPage(NotebookEntryId.HOW_TO_PLAY, step, lang)

    fun tutorialGuideButton(lang: DisplayLanguage): String = notebookButton(lang)
    fun tutorialGuideHubTitle(lang: DisplayLanguage): String = notebookHubTitle(lang)

    fun totalPlayCountLine(count: Int, lang: DisplayLanguage): String = when (lang) {
        DisplayLanguage.EN -> "PLAYS  $count"
        DisplayLanguage.JP -> "総プレイ  $count"
    }

    fun maxComboLabel(lang: DisplayLanguage): String = when (lang) {
        DisplayLanguage.EN -> "MAX COMBO"
        DisplayLanguage.JP -> "MAX COMBO"
    }

    fun assetRankTitle(lang: DisplayLanguage): String = when (lang) {
        DisplayLanguage.EN -> "RETURN TOP 10"
        DisplayLanguage.JP -> "収益 TOP 10"
    }

    fun assetRankOutOfRank(lang: DisplayLanguage): String = when (lang) {
        DisplayLanguage.EN -> "OUT"
        DisplayLanguage.JP -> "ランク外"
    }

    fun assetRankCurrent(lang: DisplayLanguage): String = when (lang) {
        DisplayLanguage.EN -> "NOW"
        DisplayLanguage.JP -> "今回"
    }

    fun relicClose(lang: DisplayLanguage): String = when (lang) {
        DisplayLanguage.EN -> "CLOSE"
        DisplayLanguage.JP -> "閉じる"
    }

    fun relicName(id: RelicId, lang: DisplayLanguage): String = when (lang) {
        DisplayLanguage.JP -> when (id) {
            RelicId.PAPER -> "PAPER 液晶"
            RelicId.INTEREST_120 -> "運用利回り 1.2×"
            RelicId.SMOOTH -> "SMOOTH チャート"
            RelicId.JUDGE -> "判定演出"
            RelicId.BONUS_10_150 -> "留守番 45日"
            RelicId.COMBO -> "COMBO 演出"
            RelicId.COOLDOWN_25 -> "チャージ −25%"
            RelicId.PNL_BONUS_120 -> "勝ちボーナス 1.2×"
            RelicId.DARK -> "DARK 液晶"
            RelicId.MISS_LIFE_1 -> "LIFE +1"
            RelicId.LAG_25 -> "ラグ −25%"
            RelicId.INTEREST_150 -> "運用利回り 1.5×"
            RelicId.MISS_COMBO_HEAL -> "COMBO 回復"
            RelicId.COOLDOWN_50 -> "チャージ −50%"
            RelicId.PNL_BONUS_150 -> "勝ちボーナス 1.3×"
            RelicId.BONUS_10_200 -> "留守番 60日"
            RelicId.LAG_50 -> "ラグ −50%"
        }
        DisplayLanguage.EN -> when (id) {
            RelicId.PAPER -> "PAPER LCD"
            RelicId.INTEREST_120 -> "Plan yield 1.2×"
            RelicId.SMOOTH -> "SMOOTH chart"
            RelicId.JUDGE -> "Judge FX"
            RelicId.BONUS_10_150 -> "Away cap 45 days"
            RelicId.COMBO -> "COMBO FX"
            RelicId.COOLDOWN_25 -> "Charge −25%"
            RelicId.PNL_BONUS_120 -> "Win bonus 1.2×"
            RelicId.DARK -> "DARK LCD"
            RelicId.MISS_LIFE_1 -> "LIFE +1"
            RelicId.LAG_25 -> "Lag −25%"
            RelicId.INTEREST_150 -> "Plan yield 1.5×"
            RelicId.MISS_COMBO_HEAL -> "COMBO heal"
            RelicId.COOLDOWN_50 -> "Charge −50%"
            RelicId.PNL_BONUS_150 -> "Win bonus 1.3×"
            RelicId.BONUS_10_200 -> "Away cap 60 days"
            RelicId.LAG_50 -> "Lag −50%"
        }
    }

    fun relicDescription(id: RelicId, lang: DisplayLanguage): String = when (lang) {
        DisplayLanguage.JP -> when (id) {
            RelicId.PAPER -> "電子ペーパー風の画面テーマを解禁する。"
            RelicId.INTEREST_120 -> "運用プランの平均利回りが 1.2 倍になる（ぶれ幅はそのまま）。"
            RelicId.SMOOTH -> "チャート線を斜め結び（SMOOTH）にできる。"
            RelicId.JUDGE -> "ゲーム中の判定演出が豪華になる。"
            RelicId.BONUS_10_150 -> "留守の間の運用を、最大 45 日分まで反映する（通常 30 日）。"
            RelicId.COMBO -> "ゲーム中の COMBO 表示が豪華になる。"
            RelicId.COOLDOWN_25 -> "注文後、次に押せるまでのチャージが 25% 短くなり、演出が進化する。"
            RelicId.PNL_BONUS_120 -> "勝った回の増え幅が 1.2 倍になる。"
            RelicId.DARK -> "反転ダークの画面テーマを解禁する。"
            RelicId.MISS_LIFE_1 -> "LIFE が 1 つ増え、MISS に耐えられる回数が増える。"
            RelicId.LAG_25 -> "押してから約定するまでのラグが 25% 短くなり、演出が進化する。"
            RelicId.INTEREST_150 -> "運用プランの平均利回りが 1.5 倍になる（ぶれ幅はそのまま）。"
            RelicId.MISS_COMBO_HEAL -> "COMBO が 3 回貯まるごとに LIFE が 1 回復する。"
            RelicId.COOLDOWN_50 -> "注文後、次に押せるまでのチャージが 50% 短くなり、演出がさらに進化する。"
            RelicId.PNL_BONUS_150 -> "勝った回の増え幅が 1.3 倍になる。"
            RelicId.BONUS_10_200 -> "留守の間の運用を、最大 60 日分まで反映する。"
            RelicId.LAG_50 -> "押してから約定するまでのラグが 50% 短くなり、演出がさらに進化する。"
        }
        DisplayLanguage.EN -> when (id) {
            RelicId.PAPER -> "Unlock the e-paper screen theme."
            RelicId.INTEREST_120 -> "Daily plan average yield becomes 1.2× (swings unchanged)."
            RelicId.SMOOTH -> "Unlock diagonal SMOOTH chart lines."
            RelicId.JUDGE -> "In-game judgment FX becomes flashier."
            RelicId.BONUS_10_150 -> "Days away are applied up to 45 (normally 30)."
            RelicId.COMBO -> "In-game COMBO display becomes flashier."
            RelicId.COOLDOWN_25 -> "The recharge before your next order is 25% shorter, with upgraded FX."
            RelicId.PNL_BONUS_120 -> "Gains on a winning round become 1.2×."
            RelicId.DARK -> "Unlock the inverted dark screen theme."
            RelicId.MISS_LIFE_1 -> "Gain +1 LIFE so you can take one more MISS."
            RelicId.LAG_25 -> "The lag from tap to fill is 25% shorter, with upgraded FX."
            RelicId.INTEREST_150 -> "Daily plan average yield becomes 1.5× (swings unchanged)."
            RelicId.MISS_COMBO_HEAL -> "Every 3 COMBO restores 1 LIFE."
            RelicId.COOLDOWN_50 -> "The recharge before your next order is 50% shorter, with further FX."
            RelicId.PNL_BONUS_150 -> "Gains on a winning round become 1.3×."
            RelicId.BONUS_10_200 -> "Days away are applied up to 60."
            RelicId.LAG_50 -> "The lag from tap to fill is 50% shorter, with further FX."
        }
    }

    fun relicVisualGb(lang: DisplayLanguage): String = when (lang) {
        DisplayLanguage.EN -> "GB (default)"
        DisplayLanguage.JP -> "GB（既定）"
    }

    fun relicVisualStep(lang: DisplayLanguage): String = when (lang) {
        DisplayLanguage.EN -> "STEP (default)"
        DisplayLanguage.JP -> "STEP（既定）"
    }

    fun claim(lang: DisplayLanguage): String = when (lang) {
        DisplayLanguage.EN -> "DEPOSIT"
        DisplayLanguage.JP -> "入金"
    }

    /** FINISH 直後の集計待ち。dots は 1..3。 */
    fun tallying(lang: DisplayLanguage, dots: Int): String {
        val suffix = ".".repeat(dots.coerceIn(1, 3))
        return when (lang) {
            DisplayLanguage.EN -> "TALLYING$suffix"
            DisplayLanguage.JP -> "集計中$suffix"
        }
    }

    fun shareReturnLine(returnPercent: Float, lang: DisplayLanguage): String = when (lang) {
        DisplayLanguage.EN -> "Return ${formatSignedPercent(returnPercent)}"
        DisplayLanguage.JP -> "増減 ${formatSignedPercent(returnPercent)}"
    }

    fun timingMarkNice(): String = "◎"

    /** タイミング惜しい（× は未約定、MISS はプレイ中の判定と区別） */
    fun timingMarkMiss(): String = "△"

    fun chartLegendTimingNice(lang: DisplayLanguage): String = when (lang) {
        DisplayLanguage.EN -> "◎ NICE"
        DisplayLanguage.JP -> "◎ 狙い通り"
    }

    fun chartLegendTimingMiss(lang: DisplayLanguage): String = when (lang) {
        DisplayLanguage.EN -> "△ CLOSE"
        DisplayLanguage.JP -> "△ 惜しい"
    }

    fun tradeReviewLabel(
        grade: TradeTimingGrade,
        isBuy: Boolean,
        lang: DisplayLanguage
    ): String? = when (grade) {
        TradeTimingGrade.NICE -> tradeReviewNice(isBuy, lang)
        TradeTimingGrade.MISS -> tradeReviewMiss(isBuy, lang)
        TradeTimingGrade.NONE -> null
    }

    /** チャート上の極短ラベル（peek 時は非表示・下帯で説明） */
    fun tradeReviewNice(isBuy: Boolean, lang: DisplayLanguage): String = when (lang) {
        DisplayLanguage.EN -> if (isBuy) "LOW" else "HIGH"
        DisplayLanguage.JP -> if (isBuy) "底" else "高"
    }

    fun tradeReviewMiss(isBuy: Boolean, lang: DisplayLanguage): String = when (lang) {
        DisplayLanguage.EN -> if (isBuy) "HIGH?" else "LOW?"
        DisplayLanguage.JP -> if (isBuy) "高値" else "安値"
    }

    fun tradeReviewTimeSec(gameTimeSec: Float, lang: DisplayLanguage): String = when (lang) {
        DisplayLanguage.EN -> "%02ds".fmt(gameTimeSec.toInt().coerceIn(0, 99))
        DisplayLanguage.JP -> "%02d秒".fmt(gameTimeSec.toInt().coerceIn(0, 99))
    }

    fun resultRegretHeadline(moment: ResultRegretMoment, lang: DisplayLanguage): String = when (moment.kind) {
        ResultRegretKind.MISS -> resultRegretMiss(moment, lang)
        ResultRegretKind.NEAR_MISS -> resultRegretNearMiss(moment.gameTimeSec, moment.isBuy, lang)
        ResultRegretKind.BEST_NICE -> resultRegretBestNice(moment, lang)
    }

    private fun resultRegretMiss(moment: ResultRegretMoment, lang: DisplayLanguage): String {
        val t = tradeReviewTimeSec(moment.gameTimeSec, lang)
        val body = when (lang) {
            DisplayLanguage.EN -> if (moment.isBuy) {
                pickRegretPhrase(moment.gameTimeSec, "$t bought high", "$t bought near the top")
            } else {
                pickRegretPhrase(moment.gameTimeSec, "$t sold low", "$t sold near the bottom")
            }
            DisplayLanguage.JP -> if (moment.isBuy) {
                pickRegretPhrase(moment.gameTimeSec, "$t 高いところで買った", "$t 天井の近くで買った")
            } else {
                pickRegretPhrase(moment.gameTimeSec, "$t 安いところで売った", "$t 底の近くで売った")
            }
        }
        return "${timingMarkMiss()} $body"
    }

    private fun resultRegretBestNice(moment: ResultRegretMoment, lang: DisplayLanguage): String {
        val t = tradeReviewTimeSec(moment.gameTimeSec, lang)
        val body = when (lang) {
            DisplayLanguage.EN -> if (moment.isBuy) {
                pickRegretPhrase(moment.gameTimeSec, "$t bought low", "$t bought at the dip")
            } else {
                pickRegretPhrase(moment.gameTimeSec, "$t sold high", "$t sold at the peak")
            }
            DisplayLanguage.JP -> if (moment.isBuy) {
                pickRegretPhrase(moment.gameTimeSec, "$t 安いところで買えた", "$t 底で買えた")
            } else {
                pickRegretPhrase(moment.gameTimeSec, "$t 高いところで売れた", "$t 天井で売れた")
            }
        }
        return "${timingMarkNice()} $body"
    }

    fun resultRegretNearMiss(gameTimeSec: Float, isBuy: Boolean, lang: DisplayLanguage): String {
        val t = tradeReviewTimeSec(gameTimeSec, lang)
        val body = when (lang) {
            DisplayLanguage.EN -> if (isBuy) "$t almost at the low" else "$t almost at the high"
            DisplayLanguage.JP -> if (isBuy) "$t 底まであと一歩" else "$t 天井まであと一歩"
        }
        return "${timingMarkMiss()} $body"
    }

    fun resultRegretBestNice(gameTimeSec: Float, isBuy: Boolean, lang: DisplayLanguage): String =
        resultRegretBestNice(
            ResultRegretMoment(gameTimeSec, isBuy, ResultRegretKind.BEST_NICE),
            lang
        )

    private fun pickRegretPhrase(seed: Float, vararg variants: String): String {
        if (variants.isEmpty()) return ""
        val idx = kotlin.math.abs(seed.toInt() * 73 + (seed * 10f).toInt() * 17) % variants.size
        return variants[idx]
    }

    fun resultRegretIsPositive(kind: ResultRegretKind): Boolean = when (kind) {
        ResultRegretKind.BEST_NICE -> true
        ResultRegretKind.MISS, ResultRegretKind.NEAR_MISS -> false
    }

    fun chartLegendBuy(lang: DisplayLanguage): String = when (lang) {
        DisplayLanguage.EN -> "▲ BUY"
        DisplayLanguage.JP -> "▲ 買い"
    }

    fun chartLegendSell(lang: DisplayLanguage): String = when (lang) {
        DisplayLanguage.EN -> "▽ SELL"
        DisplayLanguage.JP -> "▽ 売り"
    }

    fun chartLegendForcedClose(lang: DisplayLanguage): String = when (lang) {
        DisplayLanguage.EN -> "▽ ! CLOSE"
        DisplayLanguage.JP -> "▽ ! 終了売り"
    }

    fun chartLegendCancelled(lang: DisplayLanguage): String = when (lang) {
        DisplayLanguage.EN -> "× VOID"
        DisplayLanguage.JP -> "× 未約定"
    }

    fun resultReviewSummary(niceCount: Int, missCount: Int, lang: DisplayLanguage): String =
        "${timingMarkNice()} $niceCount  /  ${timingMarkMiss()} $missCount"

    fun marketKindLabel(code: String, lang: DisplayLanguage): String = when (MarketKind.fromCode(code)) {
        MarketKind.INDEX -> when (lang) {
            DisplayLanguage.EN -> "Stock index"
            DisplayLanguage.JP -> "株価指数"
        }
        MarketKind.FX -> when (lang) {
            DisplayLanguage.EN -> "Forex"
            DisplayLanguage.JP -> "為替"
        }
        MarketKind.CRYPTO -> when (lang) {
            DisplayLanguage.EN -> "Crypto"
            DisplayLanguage.JP -> "暗号資産"
        }
        MarketKind.OIL -> when (lang) {
            DisplayLanguage.EN -> "Oil"
            DisplayLanguage.JP -> "原油"
        }
        MarketKind.STOCK -> when (lang) {
            DisplayLanguage.EN -> "Single stock"
            DisplayLanguage.JP -> "個別株"
        }
    }

    fun periodWithInstrument(period: String, instrumentName: String): String =
        "$period  ·  $instrumentName"

    fun instrumentWithCalendarRange(
        instrumentName: String,
        calendarStart: String,
        calendarEnd: String,
        lang: DisplayLanguage,
    ): String {
        val range = if (calendarStart == calendarEnd) {
            calendarStart
        } else {
            when (lang) {
                DisplayLanguage.EN -> "$calendarStart – $calendarEnd"
                DisplayLanguage.JP -> "$calendarStart〜$calendarEnd"
            }
        }
        return when (lang) {
            DisplayLanguage.EN -> "$instrumentName ($range)"
            DisplayLanguage.JP -> "$instrumentName（$range）"
        }
    }

    fun contentDisclaimerTitle(lang: DisplayLanguage): String = when (lang) {
        DisplayLanguage.EN -> "About this game"
        DisplayLanguage.JP -> "このゲームについて"
    }

    fun contentDisclaimerBody(lang: DisplayLanguage): String = when (lang) {
        DisplayLanguage.EN ->
            "URIKAI is a 60-second timing game.\n\n" +
                "All companies, headlines, and events are fictional.\n" +
                "Charts are game shapes, not real price data.\n\n" +
                "This app is not investment advice."
        DisplayLanguage.JP ->
            "URIKAIは、60秒のタイミングゲームです。\n\n" +
                "登場する会社・ニュース・出来事はすべて架空です。\n" +
                "チャートはゲーム用の形で、実際の値動きではありません。\n\n" +
                "投資の助言を目的としたものではありません。"
    }

    fun contentDisclaimerShort(lang: DisplayLanguage): String = when (lang) {
        DisplayLanguage.EN -> "Charts are abstract game shapes, not real prices. Not investment advice."
        DisplayLanguage.JP -> "チャートはゲーム用の抽象図です。投資助言ではありません。"
    }

    fun contentDisclaimerOk(lang: DisplayLanguage): String = when (lang) {
        DisplayLanguage.EN -> "OK"
        DisplayLanguage.JP -> "了解"
    }

    // --- カイのすみか（固定インカム g） ---

    fun sumikaTitle(lang: DisplayLanguage): String = when (lang) {
        DisplayLanguage.EN -> "Kai's nest"
        DisplayLanguage.JP -> "カイのすみか"
    }

    fun sumikaLevelLine(level: Int, lang: DisplayLanguage): String {
        val grade = if (lang == DisplayLanguage.EN) {
            Sumika.gradeNameEn(level)
        } else {
            Sumika.gradeNameJp(level)
        }
        return if (level <= 0) grade else "Lv.$level  $grade"
    }

    fun sumikaGradeRewardTitle(grade: Int, lang: DisplayLanguage): String {
        val name = if (lang == DisplayLanguage.EN) {
            Sumika.gradeNameEn(grade, asGradeIndex = true)
        } else {
            Sumika.gradeNameJp(grade, asGradeIndex = true)
        }
        return when (lang) {
            DisplayLanguage.EN -> "New nest: $name"
            DisplayLanguage.JP -> "新しいすみか  $name"
        }
    }

    fun sumikaGradeRewardClaim(lang: DisplayLanguage): String = when (lang) {
        DisplayLanguage.EN -> "Receive"
        DisplayLanguage.JP -> "受け取る"
    }

    fun sumikaGradeIntroPageCount(grade: Int): Int = 3

    fun sumikaGradeIntroPage(grade: Int, step: Int, lang: DisplayLanguage): String {
        val g = grade.coerceIn(1, 5)
        val s = step.coerceIn(0, 2)
        return when (lang) {
            DisplayLanguage.JP -> when (g) {
                1 -> when (s) {
                    0 -> "ねえ……ぼく、ずっと地面でねてたんだ。"
                    1 -> "きみの口座が育ってきたから、わらのすを用意できたよ。"
                    else -> "ホームの下で受け取ってね。ぼくの、はじめてのすみか。"
                }
                2 -> when (s) {
                    0 -> "わらのすもいいけど……もっとやわらかいのがいいな。"
                    1 -> "ふかふかざぶとん、用意できたよ。沈むと気持ちいいんだ。"
                    else -> "ホームの下で受け取ってね。"
                }
                3 -> when (s) {
                    0 -> "むき出しで寝るの、そろそろ卒業したい。"
                    1 -> "ちいさなドーム。正面の穴から、ひょっこり入れるよ。"
                    else -> "ホームの下で受け取ってね。"
                }
                4 -> when (s) {
                    0 -> "ドームもいいけど……耳つきの小屋がいいな。"
                    1 -> "みみや。片耳をひさしにして、入り口を守るよ。"
                    else -> "ホームの下で受け取ってね。"
                }
                else -> when (s) {
                    0 -> "最上のすみか……ヘッドセットハウスだよ。"
                    1 -> "ここまで育ててくれて、ありがとう。"
                    else -> "ホームの下で受け取ってね。ぼく、いちばんいい夢をみる。"
                }
            }
            DisplayLanguage.EN -> when (g) {
                1 -> when (s) {
                    0 -> "Hey… I've been sleeping on the ground this whole time."
                    1 -> "Your balance grew, so I got a straw nest ready."
                    else -> "Receive it at the bottom of Home. My first nest."
                }
                2 -> when (s) {
                    0 -> "Straw nest is fine, but… I want something softer."
                    1 -> "I got a plush cushion. Nice to sink into."
                    else -> "Receive it at the bottom of Home."
                }
                3 -> when (s) {
                    0 -> "Sleeping in the open… time to graduate."
                    1 -> "A little dome. I can peek in from the front hole."
                    else -> "Receive it at the bottom of Home."
                }
                4 -> when (s) {
                    0 -> "Dome is nice, but… I want an ear hut."
                    1 -> "Ear hut. One floppy ear makes the awning."
                    else -> "Receive it at the bottom of Home."
                }
                else -> when (s) {
                    0 -> "The finest nest… a headset house."
                    1 -> "Thanks for raising me this far."
                    else -> "Receive it at the bottom of Home. Best dreams ahead."
                }
            }
        }
    }

    fun sumikaGradeClaimTitle(lang: DisplayLanguage): String = when (lang) {
        DisplayLanguage.EN -> "Got it!"
        DisplayLanguage.JP -> "ゲット！"
    }

    fun sumikaGradeClaimName(grade: Int, lang: DisplayLanguage): String =
        if (lang == DisplayLanguage.EN) {
            Sumika.gradeNameEn(grade, asGradeIndex = true)
        } else {
            Sumika.gradeNameJp(grade, asGradeIndex = true)
        }

    fun sumikaGradeClaimFlavor(grade: Int, lang: DisplayLanguage): String =
        if (lang == DisplayLanguage.EN) {
            Sumika.gradeRewardFlavorEn(grade)
        } else {
            Sumika.gradeRewardFlavorJp(grade)
        }

    fun sumikaGradeUp(grade: Int, cost: Long, lang: DisplayLanguage): String {
        val name = if (lang == DisplayLanguage.EN) {
            Sumika.gradeNameEn(grade, asGradeIndex = true)
        } else {
            Sumika.gradeNameJp(grade, asGradeIndex = true)
        }
        return when (lang) {
            DisplayLanguage.EN -> "To $name  ¥%,d".fmt(cost)
            DisplayLanguage.JP -> "${name}にする  ¥%,d".fmt(cost)
        }
    }

    fun sumikaGradeCapped(lang: DisplayLanguage): String = when (lang) {
        DisplayLanguage.EN -> "That's the top for now"
        DisplayLanguage.JP -> "いまのすみかは、いちばんだよ"
    }

    fun sumikaStatPlayLabel(lang: DisplayLanguage): String = when (lang) {
        DisplayLanguage.EN -> "Pay / play"
        DisplayLanguage.JP -> "お給料 / プレイ"
    }

    fun sumikaStatTimeLabel(lang: DisplayLanguage): String = when (lang) {
        DisplayLanguage.EN -> "Pay / 6 min"
        DisplayLanguage.JP -> "お給料 / 6ふん"
    }

    fun sumikaUpgrade(cost: Long, lang: DisplayLanguage): String = when (lang) {
        DisplayLanguage.EN -> "Level up  ¥%,d".fmt(cost)
        DisplayLanguage.JP -> "レベルアップ  ¥%,d".fmt(cost)
    }

    fun sumikaMaxed(lang: DisplayLanguage): String = when (lang) {
        DisplayLanguage.EN -> "Max level"
        DisplayLanguage.JP -> "最大レベル"
    }

    fun sumikaFundsShort(lang: DisplayLanguage): String = when (lang) {
        DisplayLanguage.EN -> "Not enough balance"
        DisplayLanguage.JP -> "残高が足りない"
    }

    fun sumikaNextGradePreview(nextLevel: Int, lang: DisplayLanguage): String {
        val name = if (lang == DisplayLanguage.EN) {
            Sumika.gradeNameEn(nextLevel)
        } else {
            Sumika.gradeNameJp(nextLevel)
        }
        return when (lang) {
            DisplayLanguage.EN -> "Next look: $name"
            DisplayLanguage.JP -> "次の見た目: $name"
        }
    }

    /** 残高が薄いときカイがくれるお小遣い。 */
    fun kaiAllowanceLine(lang: DisplayLanguage): String = when (lang) {
        DisplayLanguage.EN ->
            "You look broke…\nHere, ¥100 from me.\nJust this once till you play again."
        DisplayLanguage.JP ->
            "かわいそうだから、お小遣い分けてあげるよ…\n¥100。次の1プレイまでは、もう一回はあげられないよ。"
    }

    fun sumikaKaiBalanceLine(balance: Long, lang: DisplayLanguage): String = when (lang) {
        DisplayLanguage.EN -> "Balance is ¥%,d.\nNice nest, huh?".fmt(balance)
        DisplayLanguage.JP -> "いまの残高、¥%,d だよ。\nすみか、いいでしょ。".fmt(balance)
    }

    fun sumikaKaiLowStakeLine(balance: Long, cost: Long, lang: DisplayLanguage): String = when (lang) {
        DisplayLanguage.EN ->
            "Balance ¥%,d…\nUpgrade is ¥%,d.\nDon't leave yourself too thin.".fmt(balance, cost)
        DisplayLanguage.JP ->
            "残高 ¥%,d だね。\nレベルアップは ¥%,d。\n無理すると、つぎの元手が細くなるよ。".fmt(balance, cost)
    }

    fun sumikaKaiConfirmLine(before: Long, after: Long, lang: DisplayLanguage): String = when (lang) {
        DisplayLanguage.EN ->
            "¥%,d → ¥%,d.\nStill want to level up?".fmt(before, after)
        DisplayLanguage.JP ->
            "残高 ¥%,d → ¥%,d になるよ。\nそれでもあげる？".fmt(before, after)
    }

    /**
     * シートカイの間欠セリフ。
     * 有用（システム／稼ぎ）を多め、グレード味付けを2〜3ずつ混ぜる。
     */
    fun sumikaKaiIdleLines(
        grade: Int,
        balance: Long,
        payPerPlay: Long,
        passivePerTick: Long,
        upgradeCost: Long?,
        lang: DisplayLanguage,
    ): List<String> {
        val g = grade.coerceIn(0, 5)
        val perHour = (passivePerTick * 10L).coerceAtLeast(0L)
        val lines = mutableListOf<String>()
        if (upgradeCost != null && balance < upgradeCost * 3L) {
            lines += sumikaKaiLowStakeLine(balance, upgradeCost, lang)
        }
        when (lang) {
            DisplayLanguage.JP -> {
                lines += "いい場所で寝られると、おしごと頑張れるよ！"
                if (payPerPlay > 0L) {
                    lines += "きみが1プレイする間に、¥%,d 稼いでおくね".fmt(payPerPlay)
                }
                if (perHour > 0L) {
                    lines += "1時間で、¥%,d くらい稼げるよ".fmt(perHour)
                    lines += "いまの時給、だいたい ¥%,d かな".fmt(perHour)
                }
                lines += "ぼくの時給って高いのかなあ"
                lines += "すみかに入れられる給料には限界があるよ。\nこまめに受け取ってね"
                lines += "ホームのポップがいっぱいになったら、忘れずに受け取ってね"
                lines += "放置してても、すこしずつ貯まるんだ"
                lines += "プレイが多いほど、お給料もたまるよ"
                when (g) {
                    1 -> {
                        lines += "わらのす……ぺたんだけど、ぼくの場所だよ"
                        lines += "地面より、ぜんぜんあったかい"
                        lines += "くさのにおい、ちょっと好き"
                    }
                    2 -> {
                        lines += "ふかふか……沈むと気持ちいい"
                        lines += "わらのすより、やわらかいね"
                        lines += "このざぶとん、おしりに合うんだ"
                    }
                    3 -> {
                        lines += "ドームの中、くらいくて落ち着くよ"
                        lines += "正面の穴から、ひょっこり出てくるね"
                        lines += "ちいさなへや、ひとりじめ"
                    }
                    4 -> {
                        lines += "片耳ひさし……日よけになるんだ"
                        lines += "みみやの中、まくらがふかふか"
                        lines += "耳があると、なんだか落ち着くね"
                    }
                    5 -> {
                        lines += "ヘッドセットハウス……贅沢、かな？"
                        lines += "いちばん奥で、いい夢みるよ"
                        lines += "最上のすみかで、きみの口座も見守るね"
                    }
                }
            }
            DisplayLanguage.EN -> {
                lines += "A good nest makes work easier!"
                if (payPerPlay > 0L) {
                    lines += "While you play one run,\nI'll earn ¥%,d.".fmt(payPerPlay)
                }
                if (perHour > 0L) {
                    lines += "About ¥%,d per hour.".fmt(perHour)
                    lines += "My hourly pay is\nabout ¥%,d.".fmt(perHour)
                }
                lines += "Is my hourly wage high…?"
                lines += "Nest pay has a cap.\nCollect often, okay?"
                lines += "If the Home pop fills up,\ndon't forget to collect."
                lines += "Even while idle,\nit stacks up a little."
                lines += "More plays mean\nmore nest pay."
                when (g) {
                    1 -> {
                        lines += "Straw nest… flat, but mine."
                        lines += "Way warmer than the ground."
                        lines += "I kind of like the grass smell."
                    }
                    2 -> {
                        lines += "So soft… nice to sink into."
                        lines += "Softer than the straw nest."
                        lines += "This cushion fits my seat."
                    }
                    3 -> {
                        lines += "Inside the dome feels calm."
                        lines += "I peek out from the front hole."
                        lines += "A tiny room, all mine."
                    }
                    4 -> {
                        lines += "Floppy ear awning… shade!"
                        lines += "Pillow in the ear hut is fluffy."
                        lines += "Ears make it feel calm."
                    }
                    5 -> {
                        lines += "Headset house… a little fancy?"
                        lines += "Best dreams in the deepest nook."
                        lines += "From the finest nest,\nI'll watch your balance."
                    }
                }
            }
        }
        return lines
    }

    fun sumikaKaiLevelUpLine(payPerPlay: Long, lang: DisplayLanguage): String = when (lang) {
        DisplayLanguage.EN -> "Pay went up…\n¥%,d per play now!".fmt(payPerPlay)
        DisplayLanguage.JP -> "お給料、ちょっと上がったよ！\nいまは 1プレイで ¥%,d".fmt(payPerPlay)
    }

    fun sumikaConfirmBody(cost: Long, after: Long, lang: DisplayLanguage): String = when (lang) {
        DisplayLanguage.EN -> "Spend ¥%,d?\nBalance will be ¥%,d.".fmt(cost, after)
        DisplayLanguage.JP -> "¥%,d 使ってレベルアップする？\n残高は ¥%,d になるよ。".fmt(cost, after)
    }

    fun sumikaConfirmOk(lang: DisplayLanguage): String = when (lang) {
        DisplayLanguage.EN -> "Level up"
        DisplayLanguage.JP -> "あげる"
    }

    fun sumikaConfirmCancel(lang: DisplayLanguage): String = when (lang) {
        DisplayLanguage.EN -> "Not now"
        DisplayLanguage.JP -> "やめる"
    }
}

