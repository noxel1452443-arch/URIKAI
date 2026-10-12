package com.urikai.app.game

import kotlin.random.Random

/**
 * スタート画面の固定コピーと、RETRY後のランダム表示（アドバイス / 株の用語・格言）。
 * 文言は後日 content 側へ移す想定で、ここに集約。
 */
object StartScreenCopy {

    fun title(): String = "URIKAI"

    fun timeLimit(lang: DisplayLanguage): String = when (lang) {
        DisplayLanguage.EN -> "60s TIME ATTACK"
        DisplayLanguage.JP -> "制限時間 60秒"
    }

    fun launchTutorial(lang: DisplayLanguage, lagMs: Long): String {
        val sec = formatLagSeconds(lagMs)
        return when (lang) {
            DisplayLanguage.EN -> "ALL ORDERS TAKE ${sec}s. TAP ${sec}s BEFORE THE WAVE."
            DisplayLanguage.JP -> "すべての注文は${sec}秒後。波の${sec}秒手前を叩け。"
        }
    }

    private fun formatLagSeconds(lagMs: Long): String =
        "%.2f".fmt(lagMs / 1000f).trimEnd('0').trimEnd('.')

    fun randomSmallText(lang: DisplayLanguage, random: Random = Random.Default): String {
        return if (random.nextBoolean()) {
            advice(lang, random.nextInt(adviceCount()))
        } else {
            tip(lang, random.nextInt(tipCount()))
        }
    }

    private fun adviceCount(): Int = ADVICE_JP.size

    private fun tipCount(): Int = TIPS_JP.size

    private fun advice(lang: DisplayLanguage, index: Int): String {
        val list = if (lang == DisplayLanguage.EN) ADVICE_EN else ADVICE_JP
        return list[index.coerceIn(list.indices)]
    }

    private fun tip(lang: DisplayLanguage, index: Int): String {
        val list = if (lang == DisplayLanguage.EN) TIPS_EN else TIPS_JP
        return list[index.coerceIn(list.indices)]
    }

    // ── アドバイス（勝つためのヒント） ──

    private val ADVICE_JP = listOf(
        "下げが続くときは、売ったまま待つのも手。",
        "終了間際の注文は約定しない。",
        "約定は、押してから少し後の価格で決まる。",
        "注文中はもう一度押せない。",
        "買いは上向きの手前、売りは下向きの手前。",
        "連打より、折り返しの一瞬を待て。",
        "急落のあとは、底の手前で買って戻りで売る。",
        "行ったり来たりの相場は、小さな往復を狙え。",
        "上がり続けた相場は、天井の手前で売り抜けよう。",
        "大きな山と谷を取ると、増減率と★が伸びやすい。",
        "毎回、口座残高の全額で勝負する。勝つほど次の元手が増える。",
        "口座残高が¥5,000を下回ると破産。",
        "運用プランは、開始画面の口座残高から選べる。",
        "高値圏で株を持ち続けるのは危険。",
        "図鑑で、クリアした相場を見返せる。",
        "リザルトの％をタップすると、★の目安が見られる。"
    )

    private val ADVICE_EN = listOf(
        "Long slide? Sitting out in cash is fine too.",
        "Orders right before the end won't fill.",
        "Fills use the price a moment after you tap.",
        "You can't tap again while an order is pending.",
        "Buy before the upturn, sell before the downturn.",
        "Wait for the turn—don't mash the button.",
        "After a crash, buy before the bottom and sell the bounce.",
        "Back-and-forth market? Trade the small swings.",
        "After a long climb, sell before the top.",
        "Catch the big waves to grow your return and stars.",
        "Every run bets your whole balance. Wins grow your next stake.",
        "Fall below ¥5,000 and you go bankrupt.",
        "Pick a daily plan from the balance on the START screen.",
        "Holding stock at the peak is risky.",
        "Check SCRAP to replay markets you've cleared.",
        "Tap the % on the result to see the ★ targets."
    )

    // ── 株の用語・格言（実在の事件は扱わない） ──

    private val TIPS_JP = listOf(
        "「他人が欲しがる時に恐れ、恐れる時に欲しがれ」— バフェット",
        "「買いは煽り、売りは張り」— 株式の古い格言",
        "「市場は短期は投票機、長期は体重計」— グラハム経由でバフェット",
        "円キャリー解消：低金利円で調達した資金の逆回転",
        "ショートスクイーズ：空売り勢の買い戻しで急騰",
        "フラッシュクラッシュ：アルゴが一瞬で市場を歪める",
        "サーキットブレーカー：暴落時に取引を一時停止",
        "ストップ高・ストップ安：1日の値幅制限",
        "配当利回り：株価に対する配当の割合",
        "PER：株価が利益の何倍か",
        "PBR：株価が純資産の何倍か",
        "EPS：1株あたり利益",
        "ROE：自己資本利益率",
        "信用取引：借りた株・借りた金での売買",
        "空売り：持たない株を売ってから買い戻す",
        "買い余力・売り余力：需給の温度計",
        "VIX：恐怖指数と呼ばれるボラティリティ指数",
        "日経平均：日本を代表する株価指数",
        "ダウ平均：米国を代表する株価指数",
        "S&P500：米国大型株500社の指数",
        "ナスダック：米国ハイテク株中心の指数",
        "東証マザーズ／グロース：新興・成長企業市場",
        "IPO：新規公開で初めて株が売られる日",
        "TOB：公開買い付けで会社を買い占める",
        "M&A：合併・買収で株価が動く",
        "決算発表：四半期ごとの業績で株が動く",
        "ガイダンス修正：業績予想の上方・下方修正",
        "株主総会：配当や経営方針が焦点に",
        "インサイダー取引：内部者の株式売買",
        "日銀会合：金融政策で為替と株が揺れる",
        "FOMC：米国の金融政策で世界が揺れる",
        "CPI・雇用統計：インフレと景気で利下げ観測が動く",
        "「豚は殺される」— 欲張りすぎた投機家の末路",
        "「落ちるナイフを掴むな」— 暴落中の底値拾いへの警告"
    )

    private val TIPS_EN = listOf(
        "\"Be fearful when others are greedy.\" — Buffett",
        "\"Buy the rumor, sell the news.\" — old market saying",
        "\"In the short run, the market is a voting machine.\" — Graham",
        "Yen carry unwind: cheap-yen trades reversing hard.",
        "Short squeeze: bears forced to buy, price spikes.",
        "Flash crash: algorithms distort price in seconds.",
        "Circuit breaker: trading halts on sharp drops.",
        "Limit up / limit down: daily price bands.",
        "Dividend yield: cash flow vs. price.",
        "P/E: price vs. earnings.",
        "P/B: price vs. book value.",
        "EPS: earnings per share.",
        "ROE: return on equity.",
        "Margin trading: leverage long and short.",
        "Short selling: sell first, buy back later.",
        "Bid-ask depth: supply and demand heat.",
        "VIX: the \"fear index\" of volatility.",
        "Nikkei 225: Japan's flagship index.",
        "Dow Jones: America's classic index.",
        "S&P 500: 500 large U.S. stocks.",
        "Nasdaq: tech-heavy U.S. index.",
        "Growth markets: smaller, faster listings.",
        "IPO: when a company first sells stock.",
        "Tender offer: buying control of a firm.",
        "M&A: mergers move share prices.",
        "Earnings: quarterly reports move stocks.",
        "Guidance cuts: forecasts revised down—or up.",
        "Shareholder meetings: dividends and votes.",
        "Insider trading: executives buying or selling.",
        "BoJ meetings: policy moves FX and stocks.",
        "FOMC: U.S. rates move the world.",
        "CPI & jobs data: inflation vs. rate cuts.",
        "\"Pigs get slaughtered.\" — Wall Street proverb",
        "\"Don't catch a falling knife.\" — crash warning"
    )
}
