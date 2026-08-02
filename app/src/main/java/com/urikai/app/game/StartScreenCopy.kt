package com.urikai.app.game

import kotlin.random.Random

/**
 * スタート画面の固定コピーと、RETRY後のランダム表示（アドバイス15 / Tips45）。
 * 文言は後日 content 側へ移す想定で、ここに集約。
 */
object StartScreenCopy {

    fun title(): String = "URIKAI"

    fun timeLimit(lang: DisplayLanguage): String = when (lang) {
        DisplayLanguage.EN -> "60s TIME ATTACK"
        DisplayLanguage.JP -> "制限時間 60秒"
    }

    fun launchTutorial(lang: DisplayLanguage): String = when (lang) {
        DisplayLanguage.EN -> "ALL ORDERS TAKE 1s. TAP 1s BEFORE THE WAVE."
        DisplayLanguage.JP -> "すべての注文は1秒後。波の1秒手前を叩け。"
    }

    fun randomSmallText(lang: DisplayLanguage, random: Random = Random.Default): String {
        return if (random.nextBoolean()) {
            advice(lang, random.nextInt(adviceCount()))
        } else {
            tip(lang, random.nextInt(tipCount()))
        }
    }

    fun adviceCount(): Int = ADVICE_JP.size

    fun tipCount(): Int = TIPS_JP.size

    private fun advice(lang: DisplayLanguage, index: Int): String {
        val list = if (lang == DisplayLanguage.EN) ADVICE_EN else ADVICE_JP
        return list[index.coerceIn(list.indices)]
    }

    private fun tip(lang: DisplayLanguage, index: Int): String {
        val list = if (lang == DisplayLanguage.EN) TIPS_EN else TIPS_JP
        return list[index.coerceIn(list.indices)]
    }

    // ── アドバイス（勝つためのヒント）15 ──

    private val ADVICE_JP = listOf(
        "下降が長いときは現金で利子を稼ごう。",
        "最後の1秒の注文は約定しない。",
        "約定は押した1秒後の価格で決まる。",
        "注文中はもう一度押せない。",
        "買いは上向きの手前、売りは下向きの手前。",
        "連打より、折り返しの一瞬を待て。",
        "V字相場は底の手前で買い、戻りで売り。",
        "ジグザグ相場は短期の往復を狙え。",
        "バブル相場は天井付近の売りが効く。",
        "大きな山と谷を取ると、最終資産と星が伸びやすい。",
        "現金保有中だけ利子が入る（複利なし）。",
        "60秒ずっと現金でも、約4.5%の利子がある。",
        "高値圏で株を持ち続けるのは危険。",
        "SCRAPで過去の相場を予習できる。",
        "目標額を超えると OVER。資産の余韻が残る。"
    )

    private val ADVICE_EN = listOf(
        "Long drops? Earn interest in cash.",
        "Orders in the last second won't fill.",
        "Fills use the price 1 second after you tap.",
        "You can't tap again while an order is pending.",
        "Buy before the upturn, sell before the downturn.",
        "Wait for the turn—don't mash the button.",
        "V-shapes: buy near the bottom, sell the bounce.",
        "Zigzags: trade the short swings.",
        "Bubbles: selling near the top pays off.",
        "Catch the big waves to grow your final asset and stars.",
        "Interest accrues only while you hold cash.",
        "Even 60 seconds in cash earns about 4.5% interest.",
        "Holding stock at the peak is risky.",
        "Check SCRAP to preview past markets.",
        "Beat the target asset for OVER — a short afterglow."
    )

    // ── 株ネタ Tips 45 ──

    private val TIPS_JP = listOf(
        "「他人が欲しがる時に恐れ、恐れる時に欲しがれ」— バフェット",
        "「買いは煽り、売りは張り」— 株式の古い格言",
        "「市場は短期は投票機、長期は体重計」— グラハム経由でバフェット",
        "リーマン・ショック（2008）：証券会社破綻が世界を襲う",
        "コロナショック（2020）：約1か月で日経は約30%急落",
        "チャイナ・ショック（2015）：上海株が連日サーキット",
        "ブラックマンデー（1987）：1日でダウが約22%下落",
        "ITバブル崩壊（2000）：ナスダックは数年かけて沈む",
        "ジャパン・バブル（1989）：日経38957円が伝説の天井",
        "失われた30年：日本株の長い横ばい時代",
        "バーナンキ・ショック（2013）： tapering 示唆で新興国が震える",
        "英トラス・ショック（2022）：ポンドが政策で急落",
        "LTCM危機（1998）：巨大ヘッジファンドが連鎖倒れ",
        "原油マイナス（2020）：需給崩壊で先物がマイナスに",
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
        "Lehman Shock (2008): a brokerage collapse shook the world.",
        "COVID crash (2020): Nikkei fell about 30% in a month.",
        "China Shock (2015): Shanghai stocks hit limit-down repeatedly.",
        "Black Monday (1987): Dow fell about 22% in one day.",
        "Dot-com bust (2000): Nasdaq sank over years.",
        "Japan bubble (1989): Nikkei 38,957—the legendary peak.",
        "Lost decades: Japan's long sideways market.",
        "Taper tantrum (2013): one hint moved global markets.",
        "Truss shock (2022): UK policy slammed the pound.",
        "LTCM (1998): a hedge fund crisis spread fast.",
        "Negative oil (2020): futures went below zero.",
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
