package com.urikai.app.game

import kotlin.random.Random

/**
 * スタート画面タイトル帯のコピー。
 *
 * - 既定表示は注文ラグの一行（ルール掲示）
 * - フリック時だけ格言に差し替え（コツ・用語はカイ一言側へ）
 */
object StartScreenCopy {

    fun title(): String = "URIKAI"

    fun timeLimit(lang: DisplayLanguage): String = when (lang) {
        DisplayLanguage.EN -> "60s TIME ATTACK"
        DisplayLanguage.JP -> "制限時間 60秒"
    }

    /** タイトル帯の既定文（ルール）。 */
    fun launchTutorial(lang: DisplayLanguage, lagMs: Long): String {
        val sec = formatLagSeconds(lagMs)
        return when (lang) {
            DisplayLanguage.EN -> "ALL ORDERS TAKE ${sec}s. TAP ${sec}s BEFORE THE WAVE."
            DisplayLanguage.JP -> "すべての注文は${sec}秒後。波の${sec}秒手前を叩け。"
        }
    }

    private fun formatLagSeconds(lagMs: Long): String =
        "%.2f".fmt(lagMs / 1000f).trimEnd('0').trimEnd('.')

    /** タイトルカード・フリック時の格言。 */
    fun randomSmallText(lang: DisplayLanguage, random: Random = Random.Default): String {
        val list = if (lang == DisplayLanguage.EN) PROVERBS_EN else PROVERBS_JP
        return list[random.nextInt(list.size)]
    }

    // ── 格言（掲示板。プレイコツは載せない） ──

    private val PROVERBS_JP = listOf(
        "「他人が欲しがる時に恐れ、恐れる時に欲しがれ」— バフェット",
        "「噂で買って、事実で売る」— 相場の古い格言",
        "「市場は短期的には投票機、長期的には体重計」— グラハム",
        "「豚は殺される」— 欲張りすぎた投機家の末路",
        "「落ちるナイフを掴むな」— 暴落中の底値拾いへの警告",
        "「トレンドは友」— 流れに逆らうな、という戒め",
        "「利食い急ぐな、損切り急げ」— 古い相場の作法",
        "「相場に絶対はない」— いちばん古い戒め",
        "「二度あることは三度ある」— 相場の繰り返し癖への警告",
        "「欲は深淵」— 勝ち続けたあとの落とし穴",
        "「休むも相場」— 動かない選択も一手",
        "「噂は買い、事実は売り」— 材料出尽くしへの戒め",
    )

    private val PROVERBS_EN = listOf(
        "\"Be fearful when others are greedy.\" — Buffett",
        "\"Buy the rumor, sell the news.\" — old market saying",
        "\"In the short run, the market is a voting machine.\" — Graham",
        "\"Pigs get slaughtered.\" — Wall Street proverb",
        "\"Don't catch a falling knife.\" — crash warning",
        "\"The trend is your friend.\" — until it bends",
        "\"Cut losses short, let winners run.\" — old tape rule",
        "\"Nothing is certain in markets.\" — the oldest caution",
        "\"What happens twice may happen thrice.\" — rhyme warning",
        "\"Greed is a pit.\" — after a hot streak",
        "\"Sitting out is also a trade.\" — rest as a move",
        "\"Buy the rumor, sell the fact.\" — priced-in caution",
    )
}
