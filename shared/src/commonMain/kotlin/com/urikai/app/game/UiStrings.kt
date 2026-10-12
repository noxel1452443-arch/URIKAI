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

    fun chaseHintBest(yenLeft: Int, lang: DisplayLanguage): String {
        val pct = "+%.1f%%".fmt(yenLeft / 100f)
        return when (lang) {
            DisplayLanguage.EN -> "BEST in $pct"
            DisplayLanguage.JP -> "BESTまであと$pct"
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
        DisplayLanguage.EN -> "REWARD"
        DisplayLanguage.JP -> "報酬"
    }

    /** RESULT 導入後の新規レリック誘導ポップ。 */
    fun relicRewardToastTitle(lang: DisplayLanguage): String = when (lang) {
        DisplayLanguage.EN -> "★ REWARD! ★"
        DisplayLanguage.JP -> "★ 報酬獲得！ ★"
    }

    fun relicRewardToastBody(lang: DisplayLanguage): String = when (lang) {
        DisplayLanguage.EN -> "Claim it from the reward box on the START screen!"
        DisplayLanguage.JP -> "開始画面の報酬箱から受け取ろう！"
    }

    /** 報酬箱で受け取った直後のバナー。 */
    fun relicGetBanner(lang: DisplayLanguage): String = when (lang) {
        DisplayLanguage.EN -> "★ GET! ★"
        DisplayLanguage.JP -> "★ ゲット！ ★"
    }

    fun relicBoxLabel(lang: DisplayLanguage): String = when (lang) {
        DisplayLanguage.EN -> "REWARDS"
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

    fun sessionPnlHistoryTitle(lang: DisplayLanguage): String = when (lang) {
        DisplayLanguage.EN -> "RECENT ROUNDS"
        DisplayLanguage.JP -> "勝負ごとの増減"
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
        DisplayLanguage.EN -> "DAILY PLAN"
        DisplayLanguage.JP -> "運用プラン（毎日反映）"
    }

    fun investPlanRate(plan: InvestPlan, lang: DisplayLanguage): String {
        val mean = formatSignedPercent(plan.dailyMeanPercent)
        return if (plan.isGuaranteed) {
            when (lang) {
                DisplayLanguage.EN -> "$mean/day fixed"
                DisplayLanguage.JP -> "$mean/日 確定"
            }
        } else {
            val vol = "±%.0f%%".fmt(plan.dailyVolPercent)
            when (lang) {
                DisplayLanguage.EN -> "avg $mean/day $vol"
                DisplayLanguage.JP -> "平均$mean/日 $vol"
            }
        }
    }

    fun investPlanLocked(unlockPeak: Long, lang: DisplayLanguage): String = when (lang) {
        DisplayLanguage.EN -> "PEAK ¥%,d to unlock".fmt(unlockPeak)
        DisplayLanguage.JP -> "最高口座残高 ¥%,d で解放".fmt(unlockPeak)
    }

    fun investPlanStripLine(plan: InvestPlan, lang: DisplayLanguage): String =
        "${plan.displayName(lang)}  ${investPlanRate(plan, lang)}"

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
            1 -> "今回、残高が何％増えた（減った）かと★です。\nタップすると、この試合の目標と★の内訳が見られます。"
            2 -> "口座残高です。毎回、残高の全額で勝負します。\n横の数字は今回増えた（減った）金額です。"
            3 -> "勝つほど次の勝負の元手が大きくなり、残高は複利で増えていきます。\n残高が¥5,000を下回ると破産です。"
            else -> "注文は1秒遅れて届きます。先の波を想像して押すほど、うまくいきやすい。\n何度でも挑戦して、自分のタイミングを見つけてね。"
        }
        DisplayLanguage.EN -> when (step) {
            0 -> "That's one full game!\nLet's check how much you made."
            1 -> "How much your balance grew (or shrank) this run, and your ★.\nTap for this run's target and ★ breakdown."
            2 -> "Your account balance. Every run bets the whole balance.\nThe signed amount beside it is what you gained or lost."
            3 -> "Win and your next stake grows — your balance compounds.\nFall below ¥5,000 and you go bankrupt."
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
    }

    fun tutorialGuidePageCount(kind: TutorialGuideKind): Int = when (kind) {
        TutorialGuideKind.FIRST_PLAY -> 3
        TutorialGuideKind.RESULT -> 5
    }

    fun tutorialGuidePage(
        kind: TutorialGuideKind,
        step: Int,
        lang: DisplayLanguage
    ): String = when (kind) {
        TutorialGuideKind.FIRST_PLAY -> firstPlayTutorialStep(step, lang)
        TutorialGuideKind.RESULT -> resultTutorialStep(step, lang)
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
            RelicId.PNL_BONUS_150 -> "勝ちボーナス 1.5×"
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
            RelicId.PNL_BONUS_150 -> "Win bonus 1.5×"
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
            RelicId.PNL_BONUS_150 -> "勝った回の増え幅が 1.5 倍になる。"
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
            RelicId.PNL_BONUS_150 -> "Gains on a winning round become 1.5×."
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
}

/** START のガイドから読み返せるチュートリアル。 */
enum class TutorialGuideKind {
    FIRST_PLAY,
    RESULT,
}
