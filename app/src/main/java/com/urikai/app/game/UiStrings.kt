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

    fun retry(lang: DisplayLanguage): String = when (lang) {
        DisplayLanguage.EN -> "RETRY"
        DisplayLanguage.JP -> "再挑戦"
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
        DisplayLanguage.EN -> "CONF"
        DisplayLanguage.JP -> "設定"
    }

    fun timeLabel(lang: DisplayLanguage): String = "TIME"

    fun assetLabel(lang: DisplayLanguage): String = when (lang) {
        DisplayLanguage.EN -> "ASSET"
        DisplayLanguage.JP -> "資産"
    }

    /** 終盤・FINISH 中の資産マスク（出金演出まで隠す）。 */
    fun assetConcealed(lang: DisplayLanguage): String = when (lang) {
        DisplayLanguage.EN -> "ASSET  ???"
        DisplayLanguage.JP -> "資産  ???"
    }

    /** 保有中 HUD: 取得単価（数字を増やさず一行）。 */
    fun costBasisHud(price: Int, lang: DisplayLanguage): String = when (lang) {
        DisplayLanguage.EN -> "BUY ¥%,d".format(price)
        DisplayLanguage.JP -> "買 ¥%,d".format(price)
    }

    fun tradeLabel(lang: DisplayLanguage): String = when (lang) {
        DisplayLanguage.EN -> "TRADE"
        DisplayLanguage.JP -> "売買"
    }

    fun interestLabel(lang: DisplayLanguage): String = when (lang) {
        DisplayLanguage.EN -> "INT"
        DisplayLanguage.JP -> "利子"
    }

    fun formatTradeGainLoss(gainLoss: Int, lang: DisplayLanguage): String {
        val sign = if (gainLoss >= 0) "+" else "−"
        val amount = "%,d".format(kotlin.math.abs(gainLoss))
        return when (lang) {
            DisplayLanguage.EN -> "${tradeLabel(lang)} $sign¥$amount"
            DisplayLanguage.JP -> "${tradeLabel(lang)}　$sign¥$amount"
        }
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

    fun overSyncStamp(lang: DisplayLanguage): String = when (lang) {
        DisplayLanguage.EN -> "OVER"
        DisplayLanguage.JP -> "OVER"
    }

    fun chaseHintStar(nextStars: Int, yenLeft: Int, lang: DisplayLanguage): String {
        val yen = "¥%,d".format(yenLeft)
        return when (lang) {
            DisplayLanguage.EN -> "★$nextStars in $yen"
            DisplayLanguage.JP -> "★${nextStars}まであと$yen"
        }
    }

    fun chaseHintBest(yenLeft: Int, lang: DisplayLanguage): String {
        val yen = "¥%,d".format(yenLeft)
        return when (lang) {
            DisplayLanguage.EN -> "BEST in $yen"
            DisplayLanguage.JP -> "BESTまであと$yen"
        }
    }

    fun locked(lang: DisplayLanguage): String = when (lang) {
        DisplayLanguage.EN -> "LOCKED"
        DisplayLanguage.JP -> "未解放"
    }

    fun bestLabel(lang: DisplayLanguage): String = "BEST"

    /** 自己ベスト更新スタンプ（リザルト星付近）。 */
    fun bestStamp(lang: DisplayLanguage): String = when (lang) {
        DisplayLanguage.EN -> "BEST!"
        DisplayLanguage.JP -> "新記録"
    }

    fun newBadge(): String = "NEW!"

    fun sortByOrder(lang: DisplayLanguage): String = when (lang) {
        DisplayLanguage.EN -> "ORDER"
        DisplayLanguage.JP -> "順"
    }

    fun sortByScore(lang: DisplayLanguage): String = when (lang) {
        DisplayLanguage.EN -> "SCORE"
        DisplayLanguage.JP -> "スコア"
    }

    /** リザルト直後のボタン（英語固定） */
    fun timeUp(): String = "TIME UP"

    /** 画面上部タイトル（英語固定） */
    fun screenTitle(appLayer: AppLayer): String = when (appLayer) {
        AppLayer.OPTION -> "CONFIG"
        AppLayer.SCRAPBOOK -> "SCRAP"
        AppLayer.PLAY -> "URIKAI"
    }

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
        DisplayLanguage.JP -> "GHOST"
    }

    fun resultFlavorBreakEven(lang: DisplayLanguage): String = when (lang) {
        DisplayLanguage.EN -> "BREAK EVEN"
        DisplayLanguage.JP -> "BREAK EVEN"
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
        return "$side ¥%,d".format(price)
    }

    /**
     * 売り判定の矢印（建値より安く売った＝▼ など）。
     * 買いラベルはラグ矢印 [lagQualityMark] を使う。
     */
    fun fillQualityMark(isBuy: Boolean, quality: FillQuality): String {
        if (isBuy) return lagQualityMark(isBuy = true, quality = quality)
        return when (quality) {
            FillQuality.OK -> "·"
            FillQuality.FANTASTIC, FillQuality.PERFECT, FillQuality.GREAT -> "▲"
            FillQuality.MISS -> "▼"
        }
    }

    /** ラグ帰属の矢印（押下→約定）。 */
    fun lagQualityMark(isBuy: Boolean, quality: FillQuality): String = when (quality) {
        FillQuality.OK -> "·"
        FillQuality.FANTASTIC, FillQuality.PERFECT, FillQuality.GREAT -> if (isBuy) "▼" else "▲"
        FillQuality.MISS -> if (isBuy) "▲" else "▼"
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
        DisplayLanguage.EN -> "ASSET"
        DisplayLanguage.JP -> "資産"
    }

    /** 符号付き金額のみ（例: +¥1,200 / −¥300）。ラベルなし。 */
    fun formatSignedYen(amount: Int): String {
        val sign = if (amount >= 0) "+" else "−"
        return "$sign¥%,d".format(kotlin.math.abs(amount))
    }

    /** START 通算1行（報酬ティアなし）。 */
    fun lifetimeLedgerLine(amount: Int, lang: DisplayLanguage): String {
        val label = when (lang) {
            DisplayLanguage.EN -> "LEDGER"
            DisplayLanguage.JP -> "通算"
        }
        return "$label  ${formatSignedYen(amount)}"
    }

    fun relicThresholdReached(threshold: Int, lang: DisplayLanguage): String {
        val yen = "¥%,d".format(threshold)
        return when (lang) {
            DisplayLanguage.EN -> "Ledger $yen reached"
            DisplayLanguage.JP -> "通算 $yen 到達"
        }
    }

    /** 報酬箱向け。値札に見えないよう「通算」付き・控えめ。 */
    fun relicThresholdQuiet(threshold: Int, lang: DisplayLanguage): String {
        val yen = "¥%,d".format(threshold)
        return when (lang) {
            DisplayLanguage.EN -> "Ledger $yen"
            DisplayLanguage.JP -> "通算 $yen"
        }
    }

    fun relicGiftBadge(lang: DisplayLanguage): String = when (lang) {
        DisplayLanguage.EN -> "GIFT"
        DisplayLanguage.JP -> "受取"
    }

    /** RESULT 導入後の新規レリック誘導ポップ。 */
    fun relicRewardToastTitle(lang: DisplayLanguage): String = when (lang) {
        DisplayLanguage.EN -> "★ REWARD! ★"
        DisplayLanguage.JP -> "★ 報酬獲得！ ★"
    }

    fun relicRewardToastBody(lang: DisplayLanguage): String = when (lang) {
        DisplayLanguage.EN -> "Claim it from the START screen!"
        DisplayLanguage.JP -> "START画面から受け取ろう！"
    }

    /** 報酬箱で受け取った直後のバナー。 */
    fun relicGetBanner(lang: DisplayLanguage): String = when (lang) {
        DisplayLanguage.EN -> "★ GET! ★"
        DisplayLanguage.JP -> "★ ゲット！ ★"
    }

    fun relicBoxLabel(lang: DisplayLanguage): String = when (lang) {
        DisplayLanguage.EN -> "RELICS"
        DisplayLanguage.JP -> "報酬箱"
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

    fun relicRevertToGb(lang: DisplayLanguage): String = when (lang) {
        DisplayLanguage.EN -> "Back to GB"
        DisplayLanguage.JP -> "GBに戻す"
    }

    fun relicRevertToStep(lang: DisplayLanguage): String = when (lang) {
        DisplayLanguage.EN -> "Back to STEP"
        DisplayLanguage.JP -> "STEPに戻す"
    }

    fun ledgerBonusMultLabel(mult: Float): String {
        val text = if (mult == mult.toInt().toFloat()) {
            "×${mult.toInt()}"
        } else {
            "×${"%.1f".format(mult)}"
        }
        return text
    }

    fun ledgerBonusStartCaption(mult: Float, lang: DisplayLanguage): String {
        val m = ledgerBonusMultLabel(mult)
        return when (lang) {
            DisplayLanguage.EN -> "BONUS ACTIVE $m"
            DisplayLanguage.JP -> "ボーナス発生中 $m"
        }
    }

    fun sessionPnlHistoryTitle(lang: DisplayLanguage): String = when (lang) {
        DisplayLanguage.EN -> "P&L HISTORY"
        DisplayLanguage.JP -> "損益履歴"
    }

    fun historyNewestMark(): String = "NEW"

    fun sessionPnlHistoryEmpty(lang: DisplayLanguage): String = when (lang) {
        DisplayLanguage.EN -> "No history yet"
        DisplayLanguage.JP -> "まだ履歴がありません"
    }

    fun ledgerBonusHud(mult: Float, lang: DisplayLanguage): String {
        val m = ledgerBonusMultLabel(mult)
        return when (lang) {
            DisplayLanguage.EN -> "BONUS $m"
            DisplayLanguage.JP -> "ボーナス $m"
        }
    }

    fun ledgerBonusTutorialTitle(lang: DisplayLanguage): String = when (lang) {
        DisplayLanguage.EN -> "BONUS!"
        DisplayLanguage.JP -> "ボーナス発生！"
    }

    fun ledgerBonusTutorialStep(step: Int, mult: Float, lang: DisplayLanguage): String {
        val m = ledgerBonusMultLabel(mult).removePrefix("×").let { "x$it" }
        val exampleBase = 450
        val exampleBoosted = (exampleBase * mult).toInt()
        return when (lang) {
            DisplayLanguage.JP -> when (step) {
                0 -> "おめでとうございます！\n次のゲームはボーナス回です。"
                1 -> "ボーナス回では、プレイ後に獲得予定の損益に${m}が乗せられます。\n例）　損益 +¥${exampleBase} ${m} = +¥${exampleBoosted}"
                else -> "ボーナスは一定の確率で発生します。\nこの機を逃さず、たくさん稼ぎましょう！"
            }
            DisplayLanguage.EN -> when (step) {
                0 -> "Congratulations!\nYour next run is a bonus round."
                1 -> "On a bonus round, the P&L added after play gets ${m}.\nEx.)  P&L +¥${exampleBase} ${m} = +¥${exampleBoosted}"
                else -> "Bonus rounds appear at random.\nDon't miss the chance to earn big!"
            }
        }
    }

    fun ledgerBonusTutorialNext(lang: DisplayLanguage): String = when (lang) {
        DisplayLanguage.EN -> "NEXT"
        DisplayLanguage.JP -> "つぎへ"
    }

    fun ledgerBonusTutorialOk(lang: DisplayLanguage): String = when (lang) {
        DisplayLanguage.EN -> "GOT IT"
        DisplayLanguage.JP -> "わかった"
    }

    fun firstPlayTutorialTitle(lang: DisplayLanguage): String = when (lang) {
        DisplayLanguage.EN -> "HOW TO PLAY"
        DisplayLanguage.JP -> "遊び方"
    }

    fun firstPlayTutorialStep(step: Int, lang: DisplayLanguage): String = when (lang) {
        DisplayLanguage.JP -> when (step) {
            0 -> "URIKAIへようこそ！\nこのゲームは、1分間のチャートを見ながら、1つのボタンで買い・売りをするタイミングゲームです。\n安い所で買って、高い所で売ると、お金が増えるよ。"
            1 -> "ボタンを押してから、注文されるまでには1秒かかります。\n山と谷をよく見て、いちばんいいタイミングをねらってみよう！"
            else -> "買った時より安く売ると、左上のLIFEが１つ減ります。すべてなくなるとRETIREになってしまうので気を付けよう！"
        }
        DisplayLanguage.EN -> when (step) {
            0 -> "Welcome to URIKAI!\nIt's a 1-minute timing game: one button to buy and sell on a live chart.\nBuy low, sell high, and your money grows."
            1 -> "After you press the button, it takes 1 second for the order to fill.\nWatch the peaks and valleys, and aim for the best moment!"
            else -> "Sell cheaper than you bought and you lose one LIFE (top-left). Lose them all and it's RETIRE — so watch out!"
        }
    }

    fun firstPlayTutorialNext(lang: DisplayLanguage): String = when (lang) {
        DisplayLanguage.EN -> "NEXT"
        DisplayLanguage.JP -> "つぎへ"
    }

    fun firstPlayTutorialStart(lang: DisplayLanguage): String = when (lang) {
        DisplayLanguage.EN -> "START"
        DisplayLanguage.JP -> "はじめる"
    }

    fun resultTutorialTitle(lang: DisplayLanguage): String = when (lang) {
        DisplayLanguage.EN -> "READING RESULTS"
        DisplayLanguage.JP -> "結果の見方"
    }

    fun resultTutorialStep(step: Int, lang: DisplayLanguage): String = when (lang) {
        DisplayLanguage.JP -> when (step) {
            0 -> "これで1ゲームが終了です！\nどれだけ稼げたか確認してみましょう。"
            1 -> "％と★です。波の取りきり目安。\nタップすると、この試合の目標額と★の内訳が見られます。"
            2 -> "資産は、最初の¥10,000がいくらになったか。\n横の数字は今回の損益で、通算に積み上がります。"
            3 -> "売買は、チャートの取引で増えた（減った）分。\n利子は、株を持たず現金のままのときに、少しずつ付くお金です。"
            else -> "注文は1秒遅れて届きます。先の波を想像して押すほど、うまくいきやすい。\n何度でも挑戦して、自分のタイミングを見つけてね。"
        }
        DisplayLanguage.EN -> when (step) {
            0 -> "That's one full game!\nLet's check how much you made."
            1 -> "% and ★ — a rough read on how much of the wave you caught.\nTap for this run's target and ★ breakdown."
            2 -> "Asset is what happened to your starting ¥10,000.\nThe signed amount beside it is this run's P&L — and it adds to your lifetime ledger."
            3 -> "Trades are what you gained (or lost) on the chart.\nInterest is a little money that accrues while you hold cash instead of shares."
            else -> "Orders arrive 1 second late. The more you read ahead, the better you'll do.\nPlay again and find your own timing."
        }
    }

    fun resultTutorialNext(lang: DisplayLanguage): String = when (lang) {
        DisplayLanguage.EN -> "NEXT"
        DisplayLanguage.JP -> "つぎへ"
    }

    fun resultTutorialOk(lang: DisplayLanguage): String = when (lang) {
        DisplayLanguage.EN -> "GOT IT"
        DisplayLanguage.JP -> "わかった"
    }

    fun tutorialGuideButton(lang: DisplayLanguage): String = when (lang) {
        DisplayLanguage.EN -> "GUIDE"
        DisplayLanguage.JP -> "ガイド"
    }

    fun tutorialGuideHubTitle(lang: DisplayLanguage): String = when (lang) {
        DisplayLanguage.EN -> "GUIDES"
        DisplayLanguage.JP -> "ガイド"
    }

    fun tutorialGuideEmpty(lang: DisplayLanguage): String = when (lang) {
        DisplayLanguage.EN -> "No guides yet.\nFinish a tutorial in play to unlock."
        DisplayLanguage.JP -> "まだ見られるガイドがありません。\nプレイ中の説明を終えるとここに残ります。"
    }

    fun tutorialGuideName(kind: TutorialGuideKind, lang: DisplayLanguage): String = when (kind) {
        TutorialGuideKind.FIRST_PLAY -> firstPlayTutorialTitle(lang)
        TutorialGuideKind.RESULT -> resultTutorialTitle(lang)
        TutorialGuideKind.BONUS -> ledgerBonusTutorialTitle(lang)
    }

    fun tutorialGuidePageCount(kind: TutorialGuideKind): Int = when (kind) {
        TutorialGuideKind.FIRST_PLAY -> 3
        TutorialGuideKind.RESULT -> 5
        TutorialGuideKind.BONUS -> 3
    }

    fun tutorialGuidePage(
        kind: TutorialGuideKind,
        step: Int,
        bonusMult: Float,
        lang: DisplayLanguage
    ): String = when (kind) {
        TutorialGuideKind.FIRST_PLAY -> firstPlayTutorialStep(step, lang)
        TutorialGuideKind.RESULT -> resultTutorialStep(step, lang)
        TutorialGuideKind.BONUS -> ledgerBonusTutorialStep(step, bonusMult, lang)
    }

    fun totalPlayCountLine(count: Int, lang: DisplayLanguage): String = when (lang) {
        DisplayLanguage.EN -> "PLAYS  $count"
        DisplayLanguage.JP -> "総プレイ  $count"
    }

    fun maxComboLabel(lang: DisplayLanguage): String = when (lang) {
        DisplayLanguage.EN -> "MAX COMBO"
        DisplayLanguage.JP -> "MAX COMBO"
    }

    fun assetRankTitle(lang: DisplayLanguage): String = when (lang) {
        DisplayLanguage.EN -> "ASSET TOP 10"
        DisplayLanguage.JP -> "資産 TOP 10"
    }

    fun assetRankOutOfRank(lang: DisplayLanguage): String = when (lang) {
        DisplayLanguage.EN -> "OUT"
        DisplayLanguage.JP -> "ランク外"
    }

    fun assetRankCurrent(lang: DisplayLanguage): String = when (lang) {
        DisplayLanguage.EN -> "NOW"
        DisplayLanguage.JP -> "今回"
    }

    fun syncBreakdownTitle(lang: DisplayLanguage): String = when (lang) {
        DisplayLanguage.EN -> "SYNC / ★ GUIDE"
        DisplayLanguage.JP -> "％と★の目安"
    }

    fun syncBreakdownYourResult(lang: DisplayLanguage): String = when (lang) {
        DisplayLanguage.EN -> "YOUR RUN"
        DisplayLanguage.JP -> "今回の結果"
    }

    fun syncBreakdownTargetLabel(lang: DisplayLanguage): String = when (lang) {
        DisplayLanguage.EN -> "TARGET"
        DisplayLanguage.JP -> "目標額"
    }

    fun syncBreakdownFormula(lang: DisplayLanguage): String = when (lang) {
        DisplayLanguage.EN -> "SYNC = asset ÷ target"
        DisplayLanguage.JP -> "SYNC = 最終資産 ÷ 目標額"
    }

    fun syncBreakdownLadderTitle(lang: DisplayLanguage): String = when (lang) {
        DisplayLanguage.EN -> "★ LADDER (THIS CHART)"
        DisplayLanguage.JP -> "★の目安（この試合）"
    }

    fun syncBreakdownOver(lang: DisplayLanguage): String = when (lang) {
        DisplayLanguage.EN -> "OVER SYNC"
        DisplayLanguage.JP -> "OVER SYNC"
    }

    fun syncBreakdownNote(lang: DisplayLanguage): String = when (lang) {
        DisplayLanguage.EN -> "★ from final asset vs this run's target."
        DisplayLanguage.JP -> "★は最終資産と、この試合の目標額から決まります。"
    }

    fun relicClose(lang: DisplayLanguage): String = when (lang) {
        DisplayLanguage.EN -> "CLOSE"
        DisplayLanguage.JP -> "閉じる"
    }

    fun relicName(id: RelicId, lang: DisplayLanguage): String = when (lang) {
        DisplayLanguage.JP -> when (id) {
            RelicId.PAPER -> "PAPER 液晶"
            RelicId.INTEREST_120 -> "利子 1.2×"
            RelicId.SMOOTH -> "SMOOTH チャート"
            RelicId.JUDGE -> "JUDGE"
            RelicId.BONUS_10_150 -> "通算ボーナス 1.5×"
            RelicId.COMBO -> "COMBO"
            RelicId.COOLDOWN_25 -> "クール −25%"
            RelicId.PNL_BONUS_120 -> "損益ボーナス 1.2×"
            RelicId.DARK -> "DARK 液晶"
            RelicId.MISS_LIFE_1 -> "MISS ガード +1"
            RelicId.LAG_25 -> "ラグ −25%"
            RelicId.INTEREST_150 -> "利子 1.5×"
            RelicId.MISS_COMBO_HEAL -> "COMBO 回復"
            RelicId.COOLDOWN_50 -> "クール −50%"
            RelicId.PNL_BONUS_150 -> "損益ボーナス 1.5×"
            RelicId.BONUS_10_200 -> "通算ボーナス 2.0×"
            RelicId.LAG_50 -> "ラグ −50%"
        }
        DisplayLanguage.EN -> when (id) {
            RelicId.PAPER -> "PAPER LCD"
            RelicId.INTEREST_120 -> "Interest 1.2×"
            RelicId.SMOOTH -> "SMOOTH chart"
            RelicId.JUDGE -> "JUDGE"
            RelicId.BONUS_10_150 -> "Ledger bonus 1.5×"
            RelicId.COMBO -> "COMBO"
            RelicId.COOLDOWN_25 -> "Cooldown −25%"
            RelicId.PNL_BONUS_120 -> "Trade P&L 1.2×"
            RelicId.DARK -> "DARK LCD"
            RelicId.MISS_LIFE_1 -> "MISS guard +1"
            RelicId.LAG_25 -> "Lag −25%"
            RelicId.INTEREST_150 -> "Interest 1.5×"
            RelicId.MISS_COMBO_HEAL -> "COMBO heal"
            RelicId.COOLDOWN_50 -> "Cooldown −50%"
            RelicId.PNL_BONUS_150 -> "Trade P&L 1.5×"
            RelicId.BONUS_10_200 -> "Ledger bonus 2.0×"
            RelicId.LAG_50 -> "Lag −50%"
        }
    }

    fun relicDescription(id: RelicId, lang: DisplayLanguage): String = when (lang) {
        DisplayLanguage.JP -> when (id) {
            RelicId.PAPER -> "電子ペーパー風の画面テーマを解禁する。"
            RelicId.INTEREST_120 -> "現金利子が 1.2 倍になる（獲得時・リザルトに倍率表示）。"
            RelicId.SMOOTH -> "チャート線を斜め結び（SMOOTH）にできる。"
            RelicId.JUDGE -> "ゲーム中の判定演出が豪華になる。"
            RelicId.BONUS_10_150 -> "通算ボーナス回の加算が 1.5 倍になる。"
            RelicId.COMBO -> "ゲーム中の COMBO 表示が豪華になる。"
            RelicId.COOLDOWN_25 -> "注文クールが 25% 短くなり、チャージ演出が進化する。"
            RelicId.PNL_BONUS_120 -> "終了時のプラス株損益が 1.2 倍になる。"
            RelicId.DARK -> "反転ダークの画面テーマを解禁する。"
            RelicId.MISS_LIFE_1 -> "LIFE が 1 つ増え、MISS に耐えられる回数が増える。"
            RelicId.LAG_25 -> "注文ラグが 25% 短くなり、待ち演出が進化する。"
            RelicId.INTEREST_150 -> "現金利子が 1.5 倍になる。"
            RelicId.MISS_COMBO_HEAL -> "COMBO が 3 回貯まるごとに LIFE が 1 回復する。"
            RelicId.COOLDOWN_50 -> "注文クールが 50% 短くなり、チャージ演出がさらに進化する。"
            RelicId.PNL_BONUS_150 -> "終了時のプラス株損益が 1.5 倍になる。"
            RelicId.BONUS_10_200 -> "通算ボーナス回の加算が 2.0 倍になる。"
            RelicId.LAG_50 -> "注文ラグが 50% 短くなり、待ち演出がさらに進化する。"
        }
        DisplayLanguage.EN -> when (id) {
            RelicId.PAPER -> "Unlock the e-paper screen theme."
            RelicId.INTEREST_120 -> "Cash interest becomes 1.2× (shown on gain and result)."
            RelicId.SMOOTH -> "Unlock diagonal SMOOTH chart lines."
            RelicId.JUDGE -> "In-game judgment FX becomes flashier."
            RelicId.BONUS_10_150 -> "Ledger bonus rounds add at 1.5×."
            RelicId.COMBO -> "In-game COMBO display becomes flashier."
            RelicId.COOLDOWN_25 -> "Cooldown is 25% shorter and charge FX upgrades."
            RelicId.PNL_BONUS_120 -> "Positive end trade P&L becomes 1.2×."
            RelicId.DARK -> "Unlock the inverted dark screen theme."
            RelicId.MISS_LIFE_1 -> "Gain +1 LIFE so you can take one more MISS."
            RelicId.LAG_25 -> "Lag is 25% shorter and wait FX upgrades."
            RelicId.INTEREST_150 -> "Cash interest becomes 1.5×."
            RelicId.MISS_COMBO_HEAL -> "Every 3 COMBO restores 1 LIFE."
            RelicId.COOLDOWN_50 -> "Cooldown is 50% shorter and charge FX evolves further."
            RelicId.PNL_BONUS_150 -> "Positive end trade P&L becomes 1.5×."
            RelicId.BONUS_10_200 -> "Ledger bonus rounds add at 2.0×."
            RelicId.LAG_50 -> "Lag is 50% shorter and wait FX evolves further."
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
        DisplayLanguage.EN -> "CASH OUT"
        DisplayLanguage.JP -> "出金"
    }

    /** FINISH 直後の集計待ち。dots は 1..3。 */
    fun tallying(lang: DisplayLanguage, dots: Int): String {
        val suffix = ".".repeat(dots.coerceIn(1, 3))
        return when (lang) {
            DisplayLanguage.EN -> "TALLYING$suffix"
            DisplayLanguage.JP -> "集計中$suffix"
        }
    }

    fun shareSyncLine(syncPercent: Float, lang: DisplayLanguage): String = when (lang) {
        DisplayLanguage.EN -> "Sync ${"%.1f".format(syncPercent)}%"
        DisplayLanguage.JP -> "到達率 ${"%.1f".format(syncPercent)}%"
    }

    fun timingMarkNice(): String = "◎"

    /** タイミング惜しみ（▲▼・未約定×と区別する ×） */
    fun timingMarkMiss(): String = "×"

    fun chartLegendTimingNice(lang: DisplayLanguage): String = when (lang) {
        DisplayLanguage.EN -> "◎ NICE"
        DisplayLanguage.JP -> "◎ 狙い通り"
    }

    fun chartLegendTimingMiss(lang: DisplayLanguage): String = when (lang) {
        DisplayLanguage.EN -> "× MISS"
        DisplayLanguage.JP -> "× 惜しい"
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
        DisplayLanguage.EN -> "%02ds".format(gameTimeSec.toInt().coerceIn(0, 99))
        DisplayLanguage.JP -> "%02d秒".format(gameTimeSec.toInt().coerceIn(0, 99))
    }

    fun resultRegretHeadline(moment: ResultRegretMoment, lang: DisplayLanguage): String = when (moment.kind) {
        ResultRegretKind.MISS -> resultRegretMiss(moment, lang)
        ResultRegretKind.NEAR_MISS -> resultRegretNearMiss(moment.gameTimeSec, moment.isBuy, lang)
        ResultRegretKind.BEST_NICE -> resultRegretBestNice(moment, lang)
    }

    private fun resultRegretMiss(moment: ResultRegretMoment, lang: DisplayLanguage): String {
        val t = tradeReviewTimeSec(moment.gameTimeSec, lang)
        val priorExit = RegretTradeContextFlag.VS_PRIOR_EXIT in moment.tradeContexts
        val priorEntry = RegretTradeContextFlag.VS_PRIOR_ENTRY in moment.tradeContexts
        val body = when (lang) {
            DisplayLanguage.EN -> when {
                moment.isBuy && priorExit -> pickRegretPhrase(
                    moment.gameTimeSec,
                    "$t high zone AND above your last exit",
                    "$t local peak plus re-entry above sell",
                    "$t chased high after taking profit",
                )
                moment.isBuy -> pickRegretPhrase(
                    moment.gameTimeSec,
                    "$t bought too high",
                    "$t chased the high",
                    "$t entered near the top",
                )
                !moment.isBuy && priorEntry -> pickRegretPhrase(
                    moment.gameTimeSec,
                    "$t low zone AND below your last entry",
                    "$t local trough plus exit under buy",
                    "$t cut below where you bought",
                )
                else -> pickRegretPhrase(
                    moment.gameTimeSec,
                    "$t sold too low",
                    "$t dumped near the low",
                    "$t exited at the bottom",
                )
            }
            DisplayLanguage.JP -> when {
                moment.isBuy && priorExit -> pickRegretPhrase(
                    moment.gameTimeSec,
                    "$t 高値圏かつ前回売りより上",
                    "$t 局所天井＋利確水準より高い買い",
                    "$t 上値追いかつ逃げ値より再仕込み",
                    "$t 高値掴み＋前回利確を上回る買い",
                )
                moment.isBuy -> pickRegretPhrase(
                    moment.gameTimeSec,
                    "$t 高値をつかまされた",
                    "$t 高値圏で仕込んだ",
                    "$t 天井付近で約定",
                )
                !moment.isBuy && priorEntry -> pickRegretPhrase(
                    moment.gameTimeSec,
                    "$t 安値圏かつ仕込みより下",
                    "$t 局所底値＋取得単価を割る売り",
                    "$t 底値圏で買い値を下回る手放し",
                    "$t 安値売り＋前回仕込みを下回る",
                )
                else -> pickRegretPhrase(
                    moment.gameTimeSec,
                    "$t 安値で手放した",
                    "$t 底値圏で利確",
                    "$t 安値で投げた",
                )
            }
        }
        return "${timingMarkMiss()} $body"
    }

    private fun resultRegretBestNice(moment: ResultRegretMoment, lang: DisplayLanguage): String {
        val t = tradeReviewTimeSec(moment.gameTimeSec, lang)
        val priorExit = RegretTradeContextFlag.VS_PRIOR_EXIT in moment.tradeContexts
        val priorEntry = RegretTradeContextFlag.VS_PRIOR_ENTRY in moment.tradeContexts
        val body = when (lang) {
            DisplayLanguage.EN -> when {
                moment.isBuy && priorExit -> pickRegretPhrase(
                    moment.gameTimeSec,
                    "$t low zone AND under your last exit",
                    "$t dip plus re-entry below sell",
                )
                moment.isBuy -> pickRegretPhrase(
                    moment.gameTimeSec,
                    "$t bought near the low",
                    "$t entered at the dip",
                )
                !moment.isBuy && priorEntry -> pickRegretPhrase(
                    moment.gameTimeSec,
                    "$t high zone AND above your last entry",
                    "$t peak plus exit over buy price",
                )
                else -> pickRegretPhrase(
                    moment.gameTimeSec,
                    "$t sold near the high",
                    "$t took profit at the peak",
                )
            }
            DisplayLanguage.JP -> when {
                moment.isBuy && priorExit -> pickRegretPhrase(
                    moment.gameTimeSec,
                    "$t 底値圏かつ前回売りより安い買い",
                    "$t 安値＋利確水準以下で再仕込み",
                    "$t 局所底値で逃げ値より拾えた",
                )
                moment.isBuy -> pickRegretPhrase(
                    moment.gameTimeSec,
                    "$t 底値で拾えた",
                    "$t 安値圏で仕込めた",
                )
                !moment.isBuy && priorEntry -> pickRegretPhrase(
                    moment.gameTimeSec,
                    "$t 高値圏かつ仕込みより上で売れた",
                    "$t 天井＋取得単価以上で利確",
                    "$t 局所高値で買い値を上回る売り",
                )
                else -> pickRegretPhrase(
                    moment.gameTimeSec,
                    "$t 高値で利確",
                    "$t 天井圏で売れた",
                )
            }
        }
        return "${timingMarkNice()} $body"
    }

    fun resultRegretNearMiss(gameTimeSec: Float, isBuy: Boolean, lang: DisplayLanguage): String {
        val t = tradeReviewTimeSec(gameTimeSec, lang)
        val body = when (lang) {
            DisplayLanguage.EN -> when {
                isBuy -> pickRegretPhrase(
                    gameTimeSec,
                    "$t almost at the low",
                    "$t just missed the dip",
                    "$t one step from the bottom",
                )
                else -> pickRegretPhrase(
                    gameTimeSec,
                    "$t almost at the high",
                    "$t just missed the peak",
                    "$t one step from the top",
                )
            }
            DisplayLanguage.JP -> when {
                isBuy -> pickRegretPhrase(
                    gameTimeSec,
                    "$t 底値の手前",
                    "$t 安値圏まであと一歩",
                    "$t 底付近を逃した",
                    "$t 下値支持の手前",
                )
                else -> pickRegretPhrase(
                    gameTimeSec,
                    "$t 高値の手前",
                    "$t 天井圏まであと一歩",
                    "$t 高値圏を逃した",
                    "$t 上値余地の手前",
                )
            }
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
            "URIKAI is a 60-second timing game inspired by real market events.\n\n" +
                "Chart shapes are compressed for play—they are not actual price data.\n" +
                "Titles, dates, and notes are based on historical sources.\n\n" +
                "This app is not investment advice."
        DisplayLanguage.JP ->
            "URIKAIは、史実の出来事をもとにした60秒のタイミングゲームです。\n\n" +
                "チャートの形はゲーム用に圧縮したもので、実際の株価とは一致しません。\n" +
                "タイトル・年月日・説明は史実の資料に基づいています。\n\n" +
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
}

/** STARTのガイド見直し用。一度見たチュートのみ列挙。 */
enum class TutorialGuideKind {
    FIRST_PLAY,
    RESULT,
    BONUS,
}
