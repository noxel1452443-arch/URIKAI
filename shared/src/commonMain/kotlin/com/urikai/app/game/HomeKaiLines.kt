package com.urikai.app.game

import kotlin.random.Random

/**
 * ホーム常駐カイの一言台帳。
 *
 * - 総量は多め（見たことない文がしばらく出る）
 * - プレイ回数・最高残高で解放
 * - 発話のたびに使うのは解放済みから組んだ 5〜10 本のテーブル
 * - テーブルには必ず「目標／増やす動機」系を1本以上入れる（金額はそれとなく）
 */
enum class KaiLineKind {
    /** 雑談・空気 */
    IDLE,
    /** 次の残高めど・増やす動機（{nextYen} 可） */
    GOAL,
    /** プレイのコツ */
    PLAY,
    /** 状況差し込み（未読ノートなど） */
    SITUATION,
}

data class KaiLineDef(
    val id: String,
    val kind: KaiLineKind,
    val minPlays: Int = 0,
    /** 最高口座残高（絶対額） */
    val minPeak: Long = 10_000L,
    val jp: String,
    val en: String,
)

object HomeKaiLines {

    private const val TABLE_MIN = 6
    private const val TABLE_MAX = 9

    fun nextGoalYen(peakBalance: Long): Long? =
        RelicId.nextBalanceTarget(peakBalance)

    fun formatYen(amount: Long): String = "¥%,d".fmt(amount)

    /**
     * 今回ホームで使う発話テーブルを組む。
     * GOAL を必ず含め、残りは解放済みから抽選。
     */
    fun buildActiveTable(
        playCount: Int,
        peakBalance: Long,
        balance: Long,
        notebookHasUnread: Boolean,
        lang: DisplayLanguage,
        bestCombo: Int = 0,
        random: Random = Random.Default,
    ): List<String> {
        val nextYen = nextGoalYen(peakBalance)
        val unlocked = ALL.filter { playCount >= it.minPlays && peakBalance >= it.minPeak }
            .filter {
                val needsCombo = it.jp.contains("{最大コンボ数}") || it.en.contains("{maxCombo}")
                !needsCombo || bestCombo > 0
            }

        val goals = unlocked.filter { it.kind == KaiLineKind.GOAL }.shuffled(random)
        val plays = unlocked.filter { it.kind == KaiLineKind.PLAY }.shuffled(random)
        val idles = unlocked.filter { it.kind == KaiLineKind.IDLE }.shuffled(random)

        val picked = ArrayList<KaiLineDef>(TABLE_MAX)

        // 必ず GOAL。次目標があるときは {nextYen} セリフを優先
        val goalPick = when {
            nextYen != null ->
                goals.firstOrNull { it.jp.contains("{nextYen}") }
                    ?: goals.firstOrNull()
                    ?: ALL.firstOrNull {
                        it.kind == KaiLineKind.GOAL && it.jp.contains("{nextYen}")
                    }
            else ->
                goals.firstOrNull { !it.jp.contains("{nextYen}") }
                    ?: goals.firstOrNull()
        }
        if (goalPick != null) picked.add(goalPick)

        // PLAY を1本
        plays.firstOrNull { it !in picked }?.let { picked.add(it) }

        // 状況
        if (notebookHasUnread) {
            picked.add(NOTEBOOK_UNREAD)
        }
        if (balance < 8_000L) {
            LOW_BALANCE.firstOrNull()?.let { if (it !in picked) picked.add(it) }
        }

        // IDLE で埋める
        for (line in idles) {
            if (picked.size >= TABLE_MAX) break
            if (line !in picked) picked.add(line)
        }
        // 足りなければ PLAY / GOAL を追加
        if (picked.size < TABLE_MIN) {
            for (line in plays + goals) {
                if (picked.size >= TABLE_MIN) break
                if (line !in picked) picked.add(line)
            }
        }

        val yenLabel = nextYen?.let { formatYen(it) }
        return picked.shuffled(random).map {
            resolve(it, lang, yenLabel, playCount = playCount, maxCombo = bestCombo)
        }
    }

    fun pickFromTable(table: List<String>, index: Int, lang: DisplayLanguage): String {
        if (table.isEmpty()) {
            return when (lang) {
                DisplayLanguage.JP -> "……ん。"
                DisplayLanguage.EN -> "……hm."
            }
        }
        return table[index.mod(table.size)]
    }

    private fun resolve(
        def: KaiLineDef,
        lang: DisplayLanguage,
        nextYenLabel: String?,
        playCount: Int,
        maxCombo: Int?,
    ): String {
        var raw = when (lang) {
            DisplayLanguage.JP -> def.jp
            DisplayLanguage.EN -> def.en
        }
        if (raw.contains("{nextYen}")) {
            val yen = nextYenLabel ?: when (lang) {
                DisplayLanguage.JP -> "もうちょい先"
                DisplayLanguage.EN -> "a bit further"
            }
            raw = raw.replace("{nextYen}", yen)
        }
        if (raw.contains("{プレイ回数}") || raw.contains("{playCount}")) {
            raw = raw.replace("{プレイ回数}", playCount.toString())
                .replace("{playCount}", playCount.toString())
        }
        if (raw.contains("{最大コンボ数}") || raw.contains("{maxCombo}")) {
            val combo = maxCombo?.takeIf { it > 0 }?.toString() ?: "—"
            raw = raw.replace("{最大コンボ数}", combo).replace("{maxCombo}", combo)
        }
        return raw
    }

    private val NOTEBOOK_UNREAD = KaiLineDef(
        id = "sit_notebook",
        kind = KaiLineKind.SITUATION,
        jp = "ノート、まだ読んでないページがあるよ。",
        en = "There's still an unread page in the notes.",
    )

    private val LOW_BALANCE = listOf(
        KaiLineDef(
            id = "sit_low",
            kind = KaiLineKind.SITUATION,
            jp = "焦らなくていいよ。下のでかボタンからで。",
            en = "No rush. The big button below is enough.",
        ),
    )

    /**
     * 台帳本体。minPlays / minPeak で段階解放。
     * GOAL の {nextYen} は次レリック到達残高（絶対額）に置換。
     */
    val ALL: List<KaiLineDef> = listOf(
        // ── GOAL（それとなく次の額／増やす動機） ──
        g("g01", 0, 10_000, "口座が {nextYen} あたりまでいくと、なんか空気変わるって聞いたよ。", "I heard the air changes around {nextYen}."),
        g("g02", 0, 10_000, "……{nextYen}、気になる数字だね。別に急かしてないよ。", "……{nextYen} is a curious number. Not rushing you."),
        g("g03", 1, 10_000, "こっそり言うと、{nextYen} の手前で景色が少し変わるらしい。", "Between us: the view shifts a bit before {nextYen}."),
        g("g04", 2, 10_000, "最高が {nextYen} に届くと、僕もちょっとびっくりしそう。", "If the peak hits {nextYen}, I might jump a little."),
        g("g05", 3, 11_000, "増やす話？　まあ……{nextYen} が見えてきたら、何かあるかも。", "Growing it? Well……something may wait near {nextYen}."),
        g("g06", 5, 11_000, "目標とかじゃないけど、{nextYen} は一度見てみたいね。", "Not a quest or anything—but I'd like to see {nextYen} once."),
        g("g07", 8, 15_000, "……{nextYen} 超えたあたりでなにかがもらえるかも。", "……Something might drop after you pass {nextYen}."),
        g("g08", 10, 18_000, "僕の勘だと、次のヤマは {nextYen} くらい。勘だけど。", "My hunch says the next ridge is around {nextYen}. Just a hunch."),
        g("g09", 0, 10_000, "残高、ほっとくより触ったほうが育つ気がする。気のせいかも。", "Balance feels like it grows more when you touch it. Maybe."),
        g("g10", 2, 10_000, "一回の勝負が、次の元手になるんだよね。重いけど、おもしろい。", "Each run becomes the next stake. Heavy—but fun."),
        g("g11", 4, 14_000, "減らした日も、また積み直せばいいよ。口座は忘れないから。", "Bad days can be rebuilt. The account remembers."),
        g("g12", 6, 20_000, "残高を増やすのは急がなくていいけど、止まらなくてもいい。", "No rush growing the balance—but no need to freeze either."),
        g("g13", 12, 30_000, "大きな数字より、昨日より少し上、がいちばん気持ちいいって聞いた。", "A bit above yesterday beats a giant number, I hear."),
        g("g14", 15, 40_000, "{nextYen}……まだ先？　でも、見えない山ほど登りたくなるよね。", "{nextYen}……still far? Invisible hills are the climbable ones."),
        g("g15", 20, 50_000, "もう十分すごいよ。それでも {nextYen} が気になるなら、それも才能。", "You're already impressive. Still curious about {nextYen}? That's talent."),
        g("g16", 0, 10_000, "運用もプレイも、どっちも口座を動かす道だよ。", "Policy or play—both move the account."),
        g("g17", 25, 70_000, "今日のニュースはなにかなあ。", "Wonder what today's news is……"),
        g("g18", 30, 95_000, "......。", "......"),

        // ── PLAY（コツ＝同居人の感想口調。命令形・コーチ口調は避ける） ──
        p("p01", 0, 10_000, "予想が外れると、動揺するよね。未来は誰にもわからない...", "When a call misses, it shakes you. Nobody knows the future…"),
        p("p02", 0, 10_000, "たまにはグラフを見ないで挑戦してみたらどう？", "Sometimes try a run without staring at the chart?"),
        p("p03", 1, 10_000, "買いなら上に向く手前、売りなら下に向く手前……だったかな。", "Buy before it turns up, sell before it turns down……I think?"),
        p("p04", 2, 10_000, "下げが続くときは、持たずに待つのもアリだよ。", "On long slides, waiting in cash is fine too."),
        p("p05", 3, 10_000, "適当に売り買いした方が良いのは、現実も同じなのかな...", "Random taps working better……is real life like that too?"),
        p("p06", 4, 10_000, "注文中の1秒間、いちばんそわそわする。", "That one pending second feels the longest."),
        p("p07", 5, 12_000, "★がためやすいグラフと、ためづらいグラフ。何が違うのだろう。", "Some charts farm ★ easy, some don't. What's the difference?"),
        p("p08", 6, 12_000, "高値のあたりで持ち続けるの、危ないよって……誰かが言ってた。", "Someone said holding near the top is scary……"),
        p("p09", 8, 15_000, "リザルトの％表示をタップすると、★の目安が見られるよ。", "Tap the result % to see ★ targets."),
        p("p10", 10, 15_000, "変動が激しいときは、こまめな売り買いのほうが取りやすいって？", "Wild swings—small taps back and forth work better?"),
        p("p11", 12, 18_000, "急落にも高騰にも振り回されたくないなあ。", "I don't want crashes or spikes yanking me around."),
        p("p12", 15, 22_000, "現実でもMISSし続けたらと思うと、恐ろしいなあ。", "If real life MISS'd this much……scary thought."),
        p("p13", 18, 26_000, "COMBO、売りが続くと伸びるみたい。間が空くと消えちゃう。", "COMBO seems to grow on good sells. Leave a gap and it fades."),
        p("p14", 20, 30_000, "売り買いのラグを短くする方法が、どこかにあるんだって。", "There's a way to shorten the lag somewhere, I heard."),
        p("p15", 3, 10_000, "下のでかいボタンだけで、ゲームできるんだね。", "That huge button below is basically the whole game."),

        // ── IDLE（雑談）解放段階つき ──
        i("i01", 0, 10_000, "今日も、ゆっくりでいいよ。", "Slow is fine today."),
        i("i02", 0, 10_000, "僕はここにいるよ。また話そう。", "I'm here. Talk again anytime."),
        i("i03", 0, 10_000, "タイトルの文字、つまめるよ。なんで？", "You can tug the title letters. Why, though?"),
        i("i04", 0, 10_000, "ブツカツブツカツ...", "Thunk-clack thunk-clack…"),
        i("i05", 1, 10_000, "耳が、ちょっと重い。……ヘッドセットだからね。", "Ears feel heavy.……Headset tax."),
        i("i06", 1, 10_000, "液晶の中、意外とひんやりしてる。", "It's oddly cool inside the LCD."),
        i("i07", 2, 10_000, "口座、のぞいてみる？", "Peek at the account?"),
        i("i08", 2, 10_000, "タイトルパネル、なんか気になるときあるよね。", "That title panel feels different sometimes."),
        i("i09", 3, 10_000, "図鑑、たまってくると眺めたくなる。", "The scrapbook gets fun to browse as it fills."),
        i("i10", 3, 10_000, "……あ、ぼーっとしてた。今のなし。", "……Was zoning out. Forget that."),
        i("i11", 4, 11_000, "投げないでね？", "Don't throw me?"),
        i("i12", 4, 11_000, "地面に押しつけられるの、まあまあ得意。", "I'm weirdly good at being squished to the floor."),
        i("i13", 5, 12_000, "壁にぶつかると、痛いよね。", "Hitting a wall hurts, yeah."),
        i("i14", 5, 12_000, "で、どうする？", "So—what now?"),
        i("i15", 6, 12_000, "運用方針、選んだ人の性格が出るよね。", "Your policy pick kinda shows your temperament."),
        i("i16", 6, 13_000, "留守の日も、口座は動いてる。戻ってきてからのお楽しみ。", "Away days still move the account. Fun starts when you're back."),
        i("i17", 7, 14_000, "ここには、２色しか色がないんだ。", "Only two colors in here."),
        i("i18", 7, 14_000, "★を無理に狙う必要はないと思うんだ・", "You don't have to chase ★ hard, I think—"),
        i("i19", 8, 15_000, "破産って字、見たくないよね。見なくて済むといいな。", "Nobody wants to see bankrupt. Hope we don't."),
        i("i20", 8, 15_000, "英語と日本語、切り替えると僕の声の感じも少し変わる？", "Swap JP/EN—does my vibe shift a little?"),
        i("i21", 9, 16_000, "ドンチキドンチキ...", "Don-chiki don-chiki…"),
        i("i22", 9, 16_000, "パエリア食いてえ", "I want paella."),
        i("i23", 10, 18_000, "COMBO表示、出るとちょっと嬉しい。", "COMBO pop makes me a little happy."),
        i("i24", 10, 18_000, "今までで一番続いたCOMBOは、{最大コンボ数}だよ！", "Your best COMBO so far is {maxCombo}!"),
        i("i25", 12, 20_000, "{プレイ回数}回もプレイしてくれたんだ！で、いくら増えたの？", "{playCount} runs already! So—how much did it grow?"),
        i("i26", 12, 20_000, "ニュースの絵と話、なぜか不気味に感じることがあるよ...", "News art and stories feel eerie sometimes…"),
        i("i27", 14, 22_000, "心拍音が速くなると、僕もつい早口になる。", "When the heartbeat races, I talk faster too."),
        i("i28", 14, 22_000, "たまにはリザルト、シェアしてみようよ。", "Share a result once in a while?"),
        i("i29", 16, 24_000, "新しいニュースが待ってるよ！", "Fresh news is waiting!"),
        i("i30", 16, 24_000, "ぶっちゃけこのゲーム、難しいと思う？", "Honestly—do you think this game is hard?"),
        i("i31", 18, 26_000, "ライフが減る音、嫌い。", "Hate the life-loss sound."),
        i("i32", 18, 28_000, "プレイ中に広告は出ないよ。設定でオフにもできるはず。", "No ads mid-run. You can turn them off in settings, too."),
        i("i33", 20, 30_000, "ラグ短縮、慣れると元の一秒が長く感じるらしい。", "After shorter lag, the old second feels long."),
        i("i34", 20, 32_000, "画面の見た目が変えられたら、気分も変わるのになあ。", "If looks could change, the mood would too……"),
        i("i35", 22, 35_000, "実は、残高を増やすごとに面白いものをもらえるの知ってた？", "Did you know growing the balance unlocks neat stuff?"),
        i("i36", 22, 35_000, "１回もボタンを押さないなんてこと、あるはずない！", "Zero taps in a run? That can't happen!"),
        i("i37", 24, 38_000, "プロトレーダーは、このゲームでもすぐ勝てるのかなあ。", "Would a pro trader crush this game too?"),
        i("i38", 24, 40_000, "わん！", "Woof!"),
        i("i39", 26, 42_000, "わんわんお！", "Arf arf!"),
        i("i40", 26, 45_000, "僕は不眠不休だよ。それで、残業代はないのかい？", "I never sleep. So… where's the overtime pay?"),
        i("i41", 28, 48_000, "僕をぺたんこにしたあと、撫でてくれてもいいよ。", "After flattening me, a pat is allowed."),
        i("i42", 28, 50_000, "最高更新の瞬間、見てみたいなあ。", "I wanna see a new peak happen."),
        i("i43", 30, 55_000, "遊んでくれて、ありがとう。儀礼じゃなく本音。", "Thanks for playing. Not ceremony—real."),
        i("i44", 30, 58_000, "レビューしてくれると嬉しいな。これが初回作なんだ。", "A review would mean a lot. This is the first title."),
        i("i45", 35, 65_000, "人によって、ボタンを押すタイミングが全然違うみたい。", "Everyone taps on a totally different beat."),
        i("i46", 35, 70_000, "僕の名前、覚えてるかな？", "Remember my name?"),
        i("i47", 40, 80_000, "今日の一押しを楽しんで。", "Enjoy today's one clean press."),
        i("i48", 40, 85_000, "まだまだ話したいこと、たくさんあるんだ。", "Still so much I want to say."),
        i("i49", 5, 12_000, "いつでも、呼び止めてくれていいよ。", "Stop me anytime."),
        i("i50", 5, 12_000, "キャッチボールしようよ。え、僕がボール？", "Let's play catch. Wait—am I the ball?"),
        i("i51", 7, 14_000, "インクのにおい、しない。する気がする。", "No ink smell. Feels like there is."),
        i("i52", 9, 16_000, "スタート前の沈黙、好き。始まったら騒がしい。", "I like the hush before START. Then noise."),
        i("i53", 11, 18_000, "1本の線で、そんなに集中できるのすごいよ。", "One line holding that much focus—impressive."),
        i("i54", 13, 20_000, "勝ち逃げも、負け逃げも、どっちも逃げ。", "Quit-while-ahead and quit-after-a-loss are both exits."),
        i("i55", 15, 22_000, "同じ銘柄だからと言って、次に勝てる保証はないよ。", "Same ticker doesn't mean you'll win next time."),
        i("i56", 17, 25_000, "常に全残高を使って売り買い、正気じゃないよね。", "Betting the whole balance every time—kinda wild."),
        i("i57", 19, 28_000, "r>gって知ってる？このゲームはg=0だからすぐ証明できるね。", "Know r>g? Here g=0, so it's easy to prove."),
        i("i58", 21, 32_000, "静かすぎる日は、僕がうるさくするよ。　わ！", "Quiet days? I'll be the noise. Wah!"),
        i("i59", 23, 36_000, "残高の桁が増える瞬間、見てみたいなあ。", "I wanna see the balance gain a digit."),
        i("i60", 27, 44_000, "まだクリアしてないニュース、気になるなあ。", "Uncleared news still nags at me."),
        i("i61", 0, 10_000, "ん……呼んだ？", "Hm……you called?"),
        i("i62", 0, 10_000, "はい、カイです。用がなくてもいいよ。", "Kai here. No agenda needed."),
        i("i63", 1, 10_000, "今日の相場、早く見に行こうよ。", "Let's check today's tape already."),
        i("i64", 2, 10_000, "ぼんやりしてるときがいちばん幸せ。", "I'm happiest when I'm spacing out."),
        i("i65", 3, 11_000, "ヘッドセット、外さない方針。アイデンティティ。", "Headset stays on. Identity."),
        i("i66", 4, 12_000, "ところでどうやったら　ここから出られるの？", "So… how do I get out of here?"),
        i("i67", 6, 13_000, "タイトルには裏面があるらしいよ...", "The title has a backside, apparently…"),
        i("i68", 8, 15_000, "設定で言語変えられるよ。バイリンガルだからね。", "You can swap language in settings. I'm bilingual."),
        i("i69", 10, 17_000, "勝利のあとのホーム、全力で祝うよ。", "Home after a win—I'll celebrate hard."),
        i("i70", 12, 19_000, "負けのあとのホーム、精一杯やさしくするよ。", "Home after a loss—I'll be as gentle as I can."),
        i("i71", 14, 21_000, "数字が躍る日と、数字が寝る日。どっちも本物。", "Days digits dance, days they sleep. Both real."),
        i("i72", 16, 23_000, "押す勇気と、押さない勇気、両方いる。", "Courage to tap, courage not to—both count."),
        i("i73", 18, 27_000, "上達って、失敗の形を覚えることらしい。", "Getting better is memorizing failure shapes."),
        i("i74", 22, 33_000, "まだ先がある感じ、嫌いじゃないでしょ。", "That sense of more ahead—you don't hate it."),
        i("i75", 26, 41_000, "自分だけの目標を見つけられると、良いね。", "Finding your own goal feels good."),
        i("i76", 32, 60_000, "長く付き合うゲームって、沈黙の日も必要。", "Long games need quiet days too."),
        i("i77", 38, 75_000, "また遊んでくれるかな？", "Will you play again?"),
        i("i78", 0, 10_000, "タップありがとう。以上。", "Thanks for the tap. That's all."),
        i("i79", 5, 12_000, "新しいセリフ考えなきゃ...", "Gotta invent new lines…"),
        i("i80", 15, 25_000, "同じことを二回言ったら、許して。在庫がないんだ。", "If I repeat myself, forgive me. Stock's empty."),
    )

    private fun g(id: String, plays: Int, peak: Long, jp: String, en: String) =
        KaiLineDef(id, KaiLineKind.GOAL, plays, peak, jp, en)

    private fun p(id: String, plays: Int, peak: Long, jp: String, en: String) =
        KaiLineDef(id, KaiLineKind.PLAY, plays, peak, jp, en)

    private fun i(id: String, plays: Int, peak: Long, jp: String, en: String) =
        KaiLineDef(id, KaiLineKind.IDLE, plays, peak, jp, en)
}
