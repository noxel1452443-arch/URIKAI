# -*- coding: utf-8 -*-
"""Apply flavor edits from .idea/フレーバーテキスト変更案.txt into stages_meta.csv."""
from __future__ import annotations

import csv
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
META = Path(__file__).resolve().parent / "stages_meta.csv"

# stage_id -> field updates (JP/EN as available)
# anchor_when is capped at 14 JP chars by generate_charts_csv.py
UPDATES: dict[str, dict[str, str]] = {
    # --- priority ---
    "v02": {
        "brand_name": "老舗ぬか床、異変で営業停止…まさかの復活",
        "brand_name_en": "Old pickle shop shuts down… then returns",
        "anchor_when": "三日三晩の手入れで営業再開",
        "anchor_when_en": "Reopens after three-day care",
        "flavor_text": (
            "創業200年のぬか床から、ある日突然いつもの香りが消えた。"
            "店は営業を停止し、常連客も離れ始める。"
            "三日三晩の総点検で原因を突き止めると、看板商品の味が復活。"
            "待ちわびた客が戻ってきた。"
        ),
        "flavor_text_en": (
            "A strange smell vanished from the shop's 200-year-old pickle bed. "
            "The shop shut down, and regulars drifted away. "
            "After three days of inspections, the owner found the problem and restored "
            "the signature flavor. Long-lost customers returned."
        ),
    },
    "v04": {
        "anchor_when": "安全宣言と溶岩ピザが話題に",
        "anchor_when_en": "Safety OK and lava pizza buzz",
        "flavor_text": (
            "「火山が見える露天風呂」が売りの島を噴火が直撃。"
            "全便欠航で予約は消えたが、数日後、安全が確認された地域から営業を再開した。"
            "地元の窯で焼く名物「溶岩ピザ」も話題になり、客足はゆっくり戻り始めた。"
        ),
        "flavor_text_en": (
            "An eruption grounded every flight to the island famous for its volcano-view baths. "
            "Bookings vanished. Days later, business resumed in areas cleared as safe, "
            "and the local lava-style pizza became a hit. Visitors slowly returned."
        ),
    },
    "v05": {
        "anchor_when": "ゼリー麺の次にスープ問題",
        "anchor_when_en": "Jelly hit, then soup trouble",
        "flavor_text": (
            "宇宙ステーション初のラーメン店は、浮遊するスープと飛び散る麺で大失敗。"
            "そこで麺もスープもゼリー状に改良すると、今度は大ヒットした。"
            "ところが追加された「温めると液体に戻る機能」が、無重力では思わぬ騒ぎを招いた。"
        ),
        "flavor_text_en": (
            "The space station's first ramen shop failed when broth floated away and noodles "
            "drifted everywhere. A jelly version became a hit. Then a new feature turned it "
            "back into liquid when heated, causing another mess in zero gravity."
        ),
    },
    "v07": {
        "anchor_when": "補償ガチャ配布を発表",
        "anchor_when_en": "Free compensation pulls announced",
        "flavor_text": (
            "当たり確率を「0.3%」と書くはずが「3%」と表記。"
            "訂正のたびに株は売られ、運営への不信感も広がった。"
            "対象者への補償に加え、全員に無料ガチャを配ると発表。"
            "売上はすぐには戻らなかったが、離れていたプレイヤーが少しずつ戻り始めた。"
        ),
        "flavor_text_en": (
            "The jackpot rate was supposed to be 0.3%, but the game advertised 3%. "
            "Each correction triggered more selling and damaged trust. "
            "The company offered compensation to affected players and free pulls for everyone. "
            "Sales didn't recover overnight, but some players gradually returned."
        ),
    },
    "v08": {
        "flavor_text": (
            "ミカンが予想外の大豊作。冷凍庫は満杯になり、倉庫の前まで段ボールが積み上がった。"
            "値下げしても売れず、社員が頭を抱えていたところ、町が「冷凍ミカン祭り」を開催。"
            "大量の在庫ははけたが、利益まで凍りついた。"
        ),
        "flavor_text_en": (
            "An unexpected bumper crop filled every freezer and stacked boxes outside the warehouse. "
            "Even deep discounts couldn't shift the stock. Then the town held a frozen-mandarin festival. "
            "The surplus finally cleared—but so did most of the profit."
        ),
        "history_comment": "急落後の反発が、本物かどうかを見極めよう。",
        "history_comment_en": "After a crash, ask if the bounce is real.",
    },
    "v09": {
        "flavor_text": (
            "全国の自販機が一斉に「おつりを計算中」のまま停止。"
            "原因は、共通ソフトの更新に潜んでいた不具合だった。"
            "復旧までの間、飲み物を買えない利用者が続出。"
            "修正プログラムの配信が完了すると、止まっていた販売機が次々と動き出した。"
        ),
        "flavor_text_en": (
            "Vending machines nationwide froze while calculating change. "
            "A bug in a shared software update was to blame. "
            "Customers couldn't buy drinks until a patch was distributed, "
            "and the machines gradually came back online."
        ),
    },
    "v10": {
        "brand_name": "銀行アプリ復旧直後、まさかの再停止",
        "brand_name_en": "Bank app restored—then down again",
        "anchor_when": "二度目の障害で信頼が急落",
        "anchor_when_en": "Second outage shreds trust",
        "flavor_text": (
            "「パスワードを忘れた」を押すと、銀行のほうがパスワードを忘れた。"
            "復旧のお知らせが届いた直後、今度は送金機能が停止。"
            "二度目の障害で利用者の不満が爆発し、会社は紙の手紙で謝罪を送った。"
        ),
        "flavor_text_en": (
            "Pressing 'forgot password' made the bank forget its own password. "
            "Just after service was restored, transfers stopped working. "
            "The second outage sent customer frustration soaring, "
            "and the bank apologized by paper letter."
        ),
    },
    "z03": {
        "flavor_text": (
            "「業界最高精度」をうたう天気予報AI。"
            "晴れの予報が三日続けば、次は必ず雨と答える仕組みだった。"
            "たまたま当たる日が続いて株価は上昇。"
            "しかし、大型イベントの日に晴れと予報した空から豪雨が降り、精度を疑う声が一気に広がった。"
        ),
        "flavor_text_en": (
            "A weather AI promised industry-leading accuracy. "
            "After three sunny forecasts, it always predicted rain. "
            "A lucky streak sent the stock higher—until a major outdoor event was hit by "
            "torrential rain despite a sunny forecast, triggering widespread doubts."
        ),
    },
    "z04": {
        "flavor_text": (
            "配信の最後に「大事なお知らせがあります……来週」。"
            "ファンの考察は一週間で300本を超え、卒業や活動休止の噂まで飛び交った。"
            "発表は新衣装のお披露目。安心したファンが記念グッズを買い、会社の株価も跳ね上がった。"
        ),
        "flavor_text_en": (
            "At the end of a stream came the words: 'Big news… next week.' "
            "Fans posted over 300 theories, including rumors of graduation or a hiatus. "
            "The reveal was a new outfit. Relieved fans bought commemorative merchandise, "
            "and the stock jumped."
        ),
    },
    "z06": {
        "flavor_text": (
            "最短ルートか、氷の上を滑るルートか。"
            "投票では滑るルートが選ばれたが、試験運用では手紙が何通も雪原の外へ飛んでいった。"
            "配達遅延で株価は下落。それでも封筒を防水加工し、滑走路に目印をつけると、配達は以前より速くなった。"
        ),
        "flavor_text_en": (
            "The vote chose an ice-slide route over the shortest path. "
            "During testing, letters flew off across the snow, delays mounted, and the stock fell. "
            "Waterproof envelopes and marked lanes finally made deliveries faster than before."
        ),
    },
    "z08": {
        "anchor_when": "新製法で原価を抑える",
        "anchor_when_en": "New method cuts costs",
        "flavor_text": (
            "「値上げします」「やっぱりしません」「タコを少し小さくします」。"
            "発表のたびに客はたこ焼きを割って中を確認した。"
            "批判を受けた店は、タコの切り方と焼き方を見直し、価格を据え置いたまま原価を抑える方法を見つけた。"
        ),
        "flavor_text_en": (
            "'Prices are going up.' 'Actually, no.' 'We'll use slightly smaller octopus pieces.' "
            "Customers split open their takoyaki after every announcement. "
            "After facing criticism, the company changed how it cut and cooked the octopus, "
            "lowering costs while keeping prices unchanged."
        ),
    },
    "z09": {
        "flavor_text": (
            "「雨でも安心」が売りの屋外イベント保険。"
            "赤字続きの会社が再建策を発表し、株価は反発した。"
            "ところが、今後の新規契約では雨天時の補償を半額にする方針が判明。"
            "保険料は据え置きだったため、イベント主催者の不満が広がり、株価は再び急落した。"
        ),
        "flavor_text_en": (
            "The company sold outdoor event insurance as 'peace of mind, even in the rain.' "
            "Its turnaround plan lifted the stock. But new policies would pay only half the usual "
            "amount for rainy-day claims, with premiums unchanged. "
            "Event organizers protested, and the stock plunged again."
        ),
    },
    "z10": {
        "brand_name": "社長の迷子インコ、会社の宣伝塔に？",
        "brand_name_en": "CEO's lost parrot: company mascot?",
        "flavor_text": (
            "ペット用品の広告に出演していた社長のインコが逃げ出した。"
            "SNSに「見つけた」という投稿が出るたびに会社の公式アカウントが拡散し、株価も上昇。"
            "ところが、写真の鳥は別のインコだと判明するたびに期待がしぼみ、株価は元の位置へ戻った。"
        ),
        "flavor_text_en": (
            "The CEO's parrot, a star in the company's pet-product ads, escaped. "
            "Each 'I've found it' post was amplified by the company account, and the stock rose. "
            "Every time the bird turned out to be a different parrot, the excitement faded "
            "and the price fell back."
        ),
    },
    "b01": {
        "flavor_text": (
            "金箔を巻いただけのたい焼きに、最長3時間の行列ができた。"
            "ところが、客の多くは食べるより写真を撮るのが目的。"
            "SNSに写真があふれ、珍しさが薄れた翌週には行列が消え、売上も急落した。"
        ),
        "flavor_text_en": (
            "Gold-leaf taiyaki drew lines up to three hours long. "
            "But most customers cared more about photos than eating. "
            "Once social media was flooded with pictures, the novelty faded, "
            "the line vanished, and sales plunged."
        ),
    },
    "b03": {
        "brand_name": "ドングリコイン、冬ごもり前の争奪戦",
        "brand_name_en": "Acorn Coin: pre-hibernation scramble",
        "flavor_text": (
            "前の冬に売って悔しい思いをしたリスたちは、今度こそと冬ごもり前にコインを買い集めた。"
            "人間も「去年の教訓」をまねて参戦し、価格は急騰。"
            "だが、巣穴にこもったリスたちは取引しなくなり、買い手が消えるとお祭りは終わった。"
        ),
        "flavor_text_en": (
            "After regretting their sales last winter, squirrels hoarded Acorn Coin before hibernation. "
            "Humans copied last year's supposed lesson, and the price surged. "
            "But once the squirrels settled into their nests and stopped trading, "
            "buyers disappeared and the party ended."
        ),
    },
    "b04": {
        "flavor_text": (
            "かむと「キュッ」と鳴るグミが品切れ続き。"
            "会社は需要が永遠に続くと信じ、工場を一気に3つ増設した。"
            "ところが、新工場が動き出すころにはブームが終了。"
            "倉庫には、誰も鳴らしたくないグミが山積みになった。"
        ),
        "flavor_text_en": (
            "Squeaky gummies kept selling out, so the company built three new factories, "
            "convinced demand would last forever. By the time production started, the fad was over. "
            "Warehouses filled with gummies nobody wanted to squeak anymore."
        ),
    },
    "b08": {
        "flavor_text": (
            "「丸い家は運気が上がる」という噂で、ドーム住宅の注文が急増。"
            "会社は値上げと増産を進めたが、銀行が住宅ローン審査を厳格化すると、"
            "契約予定者が次々と融資を受けられなくなった。"
            "モデルハウスは満員なのに、契約書だけが減っていった。"
        ),
        "flavor_text_en": (
            "A rumor that round homes brought good luck sent orders soaring. "
            "The company raised prices and expanded production, but stricter mortgage approvals "
            "left prospective buyers unable to borrow. "
            "Show homes stayed crowded while signed contracts dwindled."
        ),
    },
    "b09": {
        "flavor_text": (
            "キックボードの人気ランキングで1位を獲得。"
            "会社は記念キャンペーンを打ち、投資家も次の大ヒットを期待して株を買った。"
            "ところが、順位は一週間限定の投票企画によるものと判明。"
            "全社員がキックボード通勤を始めた翌日には、半分が筋肉痛で休んだ。"
        ),
        "flavor_text_en": (
            "The company topped a kickboard popularity poll, prompting a promotional campaign "
            "and a stock-buying frenzy. Then investors learned the ranking came from a one-week "
            "online vote. The next day, half the staff called in sore after commuting by kickboard."
        ),
    },
    "e01": {
        "flavor_text": (
            "2周目では全キャラが逆立ちで歩き始めた。"
            "「隠された哲学だ」「いや、バグだ」と考察が過熱し、動画は大バズり。"
            "ところが公式が「想定外の不具合です。次回更新で修正します」と発表。"
            "話題の中心だった逆立ちが消えると、配信も売上も一気に冷え込んだ。"
        ),
        "flavor_text_en": (
            "In New Game+, every character walked on their hands. "
            "Fans argued that it was hidden philosophy or just a bug, and videos went viral. "
            "The developer announced it would be fixed in the next patch. "
            "Once the upside-down walking disappeared, streams and sales cooled rapidly."
        ),
    },
    "e02": {
        "flavor_text": (
            "補償を半額にして立て直しを図った屋外イベント保険。"
            "しかし、豪雨が予想外の地域にも広がり、半額でも支払いが契約件数を上回る事態に。"
            "再建計画は見直しとなり、会社は資金繰りの悪化を認めた。"
            "株価は前回の安値をさらに割り込んだ。"
        ),
        "flavor_text_en": (
            "The outdoor event insurer cut payouts in half to recover. "
            "But torrential rain hit more areas than expected, and even reduced claims "
            "overwhelmed the company's resources. Its turnaround plan was revised as cash "
            "pressures worsened, pushing the stock below its previous low."
        ),
    },
    "e06": {
        "flavor_text": (
            "試作ロボが工場から歩いて出ていき、そのまま帰ってこなかった。"
            "会社は開発費が底をつき、倒産寸前に。"
            "それでも銀行の調査団が確認したところ、ロボットは自力で山奥の災害現場までたどり着き、"
            "救助物資を運んでいた。銀行は追加融資を決めた。"
        ),
        "flavor_text_en": (
            "A prototype robot walked out of the factory and never came back. "
            "The company ran out of development funds and neared bankruptcy. "
            "But a bank inspection team discovered that the robot had reached a remote disaster "
            "site and was delivering supplies on its own. The bank approved additional funding."
        ),
    },
    "e07": {
        "flavor_text": (
            "人口80人の島が独立を宣言し、島独自のコインは乱高下。"
            "島の商店組合が「コイン10枚でサザエ一皿」と交換レートを保証すると発表した。"
            "観光客も地元民もコインを買い始め、投機だけだった市場に初めて使い道が生まれた。"
        ),
        "flavor_text_en": (
            "An island of 80 people declared independence, sending its local coin into chaos. "
            "The island's merchants then guaranteed a plate of shellfish for every ten coins. "
            "Tourists and residents began buying in, giving the coin a practical use beyond speculation."
        ),
    },
    "e10": {
        "flavor_text": (
            "駅前の株価ボードが数分間、すべての銘柄を「1円」と表示。"
            "製造元の株も巻き添えで急落した。"
            "原因は表示ソフトの不具合だったと判明し、会社は無償修正を発表。"
            "さらに「全銘柄1円」の写真が拡散し、珍しい表示を求めるイベント会場から注文が舞い込んだ。"
        ),
        "flavor_text_en": (
            "For several minutes, a station price board displayed every stock at one yen, "
            "dragging down its maker's shares. The company traced the glitch to its display "
            "software and offered a free fix. Photos of the one-yen screen went viral, "
            "bringing orders from event venues wanting the novelty display."
        ),
    },
    "e13": {
        "flavor_text": (
            "お茶はこぼす、ドアには挟まる、欠点だらけのロボ執事。"
            "それが「不器用でかわいい」と話題になり、予約が殺到した。"
            "株価は急騰したが、手作業の修理が多すぎて生産が追いつかず、"
            "納期の延期が発表されると少し値を戻した。"
        ),
        "flavor_text_en": (
            "A robot butler spilled tea and got stuck in doors, but people found its clumsiness adorable. "
            "Preorders flooded in and the stock soared. Yet repairs required so much manual work "
            "that production fell behind. When delivery delays were announced, the shares pulled back."
        ),
    },
    "e15": {
        "flavor_text": (
            "限定版は予約開始から数分で完売。"
            "発売前でゲームの中身はまだ誰も知らないが、限定版にだけ付く特典の生産数は追加できないと発表された。"
            "予約権の転売価格まで急騰し、会社は増産を求める声に「限定の約束は守る」と回答した。"
        ),
        "flavor_text_en": (
            "The limited edition sold out within minutes. Nobody had played the game yet, "
            "but the company confirmed that its exclusive bonus could not be reproduced in larger quantities. "
            "Resale prices soared, and the company promised to keep the edition truly limited."
        ),
    },
    "v14": {
        "brand_name": "火山島リゾート、目玉イベントが空振り",
        "brand_name_en": "Volcano resort: headline tour flops",
        "anchor_when": "噴火見学ツアーが中止に",
        "anchor_when_en": "Volcano-view tour canceled",
        "flavor_text": (
            "溶岩ピザで客が戻った島は、火山の安全な観測地点を巡る「火山観察ツアー」を企画した。"
            "ところが当日は噴煙が見えず、目玉を期待していた観光客から不満が続出。"
            "安全上の問題はなかったものの、予約のキャンセルが相次いだ。"
        ),
        "flavor_text_en": (
            "After lava pizza revived tourism, the island launched a volcano-viewing tour "
            "from a safe observation point. But no plume appeared on the scheduled day, "
            "disappointing visitors who had come for the spectacle. "
            "There was no safety incident, but cancellations mounted."
        ),
    },
    "b12": {
        "flavor_text": (
            "キュッと鳴るグミのブームが終わり、会社は在庫を抱えた。"
            "そこで発売したのが、会議中でも図書館でも食べられる「無音グミ」。"
            "予想外の大ヒットとなったが、製造工程が従来品と異なり、生産ラインをすぐには増やせなかった。"
        ),
        "flavor_text_en": (
            "The squeaky-gummy craze faded, leaving the company with excess stock. "
            "It launched silent gummies for meetings and libraries, and demand unexpectedly surged. "
            "But their different manufacturing process made it impossible to expand production quickly."
        ),
    },
    # --- shorter polish ---
    "v01": {
        "anchor_when": "社長が試食し改良を約束",
        "anchor_when_en": "CEO tastes it, vows a fix",
        "flavor_text": (
            "新味「まぐろ風いちご」に全国のネコがそっぽを向いた。"
            "品質に問題はなかったが、売れ行きは急落。"
            "全品回収の会見で社長が自ら試食し、味の全面改良を約束すると、まずは消費者の信頼が戻り始めた。"
        ),
        "flavor_text_en": (
            "Cats nationwide refused the new 'tuna-strawberry' flavor. "
            "Quality was fine, but sales crashed. At a recall press conference the CEO tasted "
            "a can and vowed a full recipe overhaul—and consumer trust began to return first."
        ),
    },
    "v03": {
        "flavor_text": (
            "真冬、食料を確保するためリスたちはドングリコインを売り続け、価格は二度底を打った。"
            "春になると食料に余裕が生まれ、売りすぎたことに気づいたリスたちが一斉に買い戻した。"
        ),
        "flavor_text_en": (
            "In midwinter, squirrels sold Acorn Coin for food money and the price bottomed twice. "
            "Come spring, with food secured, they realized they had sold too much and bought it all back."
        ),
    },
    "v06": {
        "flavor_text": (
            "新型ロボ掃除機はネコに乗られると動けなくなった。"
            "そこで開発陣は、ネコを乗せて走る「ネコ乗せモード」を追加。"
            "ネコを乗せた掃除機が家中を走り回り、飼い主の動画投稿をきっかけに大ヒットした。"
        ),
        "flavor_text_en": (
            "The new robot vacuum froze whenever a cat sat on it. "
            "So the team added 'cat ride mode.' Vacuums racing around homes with cats aboard "
            "went viral in owner videos—and sales exploded."
        ),
    },
    "z01": {
        "flavor_text": (
            "辛さ100倍のポテトは、半分が泣いて諦め、半分が泣きながらおかわりする大ヒット商品になった。"
            "万人向けを狙って控えめ版を出したものの、激辛派には物足りず、初心者にはまだ辛すぎた。"
            "結局、元の激辛版が主役に返り咲いた。"
        ),
        "flavor_text_en": (
            "'100x spicy' chips became a hit: half quit crying, half cried and asked for seconds. "
            "A milder bag meant to please everyone left heat fans bored and beginners still burning. "
            "In the end, the original extreme flavor reclaimed the spotlight."
        ),
    },
    "z02": {
        "flavor_text": (
            "温泉が入浴料の値上げを発表すると、名物のカピバラたちが"
            "「客寄せに使うなら出演料を払え」とばかりに湯船から動かなくなった。"
            "値上げは撤回されたが、今度は出演料を巡る交渉が始まった。"
        ),
        "flavor_text_en": (
            "When the spa announced higher bath fees, the famous capybaras refused to leave the water—"
            "'If you use us as bait, pay an appearance fee.' The hike was canceled; "
            "now the talks are about those fees."
        ),
    },
    "z05": {
        "flavor_text": (
            "中身がほとんど公開されていない新作ゲーム。"
            "体験版を遊んだ大物配信者が「これは神ゲー」と絶賛し、株価は急騰。"
            "翌日、別の配信者が同じ体験版を遊び、「期待したほどではない」と酷評した。"
        ),
        "flavor_text_en": (
            "A barely revealed new game. A star streamer called the demo a masterpiece "
            "and the stock soared. The next day another played the same demo and called it "
            "'not what I expected.'"
        ),
    },
    "z07": {
        "flavor_text": (
            "1回目の試乗会では3メートル浮いて2秒後に池へ。"
            "それでも「次こそ成功する」と期待が集まった2回目、5メートル浮いて3秒後にまた池へ。"
            "性能は少し向上したが、肝心の着陸方法はまだ誰も知らなかった。"
        ),
        "flavor_text_en": (
            "First test ride: up 3 meters, into the pond in 2 seconds. "
            "Hopes still rose for round two: up 5 meters, into the pond in 3. "
            "Performance improved a bit—but nobody had figured out landing."
        ),
    },
    "b05": {
        "flavor_text": (
            "初競りで1玉100万円の値がついたメロン。"
            "落札した社長は「普通のメロンの味でした」と正直にコメントした。"
            "高額落札が一度きりの話題だったと分かり、株価は急落した。"
        ),
        "flavor_text_en": (
            "First auction of the year: one melon for 1 million yen. "
            "The winning bidder admitted it tasted like a normal melon. "
            "Once the record bid looked like a one-day stunt, the stock plunged."
        ),
    },
    "b07": {
        "flavor_text": (
            "目標は「すべての駅に1店舗」。"
            "借金を重ねて出店を続けたが、ブームが去ると客足は急減。"
            "返済日を迎えたある朝、全店舗のシャッターに同じ貼り紙が掲げられた。"
        ),
        "flavor_text_en": (
            "The goal: one shop at every station, all on borrowed money. "
            "When the boom faded, foot traffic collapsed. "
            "On repayment day, every shutter carried the same notice."
        ),
    },
    "b10": {
        "anchor_when": "検査結果「ほぼ麦茶」",
        "anchor_when_en": "Lab: mostly barley tea",
        "flavor_text": (
            "「飲むと10歳若返る」と宣伝していた健康茶。"
            "専門家が成分と臨床データを調べたところ、特別な健康効果を裏付ける証拠はなく、"
            "主成分もほぼ麦茶だった。宣伝文句を信じていた消費者の失望が広がった。"
        ),
        "flavor_text_en": (
            "Health tea advertised as making you '10 years younger.' "
            "Experts checked the ingredients and clinical claims: no special anti-aging evidence, "
            "and it was mostly barley tea. Believers were crushed."
        ),
    },
    "e03": {
        "flavor_text": (
            "時速20キロののんびり鉄道は、景色を楽しめる路線として乗客を増やしていた。"
            "ところが「来月から運賃3倍」と発表すると、乗客は別の交通手段へ。"
            "運賃収入が増えるという会社の期待も外れ、株価は急落した。"
        ),
        "flavor_text_en": (
            "A 20 km/h railway grew riders for the scenery. "
            "Then came 'fares triple next month'—passengers switched to other rides. "
            "The hoped-for revenue jump never arrived, and the stock plunged."
        ),
    },
    "e05": {
        "flavor_text": (
            "在庫が減り、ようやく値段が戻った冷凍ミカン。"
            "会社は今度こそ大丈夫と考え、農家との契約を増やした。"
            "ところが翌年は予想外の大豊作。倉庫の前にまた箱が積み上がり、"
            "前回の教訓を生かせなかった会社の株価は再び崩れた。"
        ),
        "flavor_text_en": (
            "Stock cleared and frozen-mandarin prices recovered. "
            "Convinced they were safe, the company signed more farmers. "
            "Next year brought another bumper crop, boxes piled up again, "
            "and the stock broke for failing to learn the last lesson."
        ),
    },
    "e08": {
        "flavor_text": (
            "散歩もトイレもいらないAIペットが大ブーム。"
            "半年後、飼い主の多くは電源を切ったまま忘れていた。"
            "追加コンテンツの購入も止まり、新型への買い替えも進まず、"
            "会社は成長計画の見直しを迫られた。"
        ),
        "flavor_text_en": (
            "AI pets that never need walks or litter boxes became a craze. "
            "Six months later, most owners had switched them off and forgotten. "
            "Add-on purchases stalled, upgrades dried up, "
            "and the company had to rewrite its growth plan."
        ),
    },
    "e14": {
        "flavor_text": (
            "冬の入り口に始まったドングリコイン祭りは、大晦日に転機を迎えた。"
            "「もう十分に儲かった」と考えたリスたちが一斉に売り、価格は急落。"
            "それでも、祭りが始まる前よりは高い水準を保っていた。"
        ),
        "flavor_text_en": (
            "The early-winter Acorn Coin party turned on New Year's Eve. "
            "Squirrels who figured they had made enough sold together and the price plunged—"
            "still above where the festival began."
        ),
    },
    "v11": {
        "brand_name": "ネコ缶、改良版もまた不評",
        "brand_name_en": "Cat food relaunch flops again",
        "anchor_when": "社長がまた試食し再改良約束",
        "anchor_when_en": "CEO tastes again, vows redo",
        "flavor_text": (
            "好感度が戻ったあと発売した「安全な新味」も、ネコには不評だった。"
            "今度は品質問題ではなく、香りと食感が原因。"
            "社長はまた会見で試食し、再改良を約束したが、前回ほど話題にはならなかった。"
        ),
        "flavor_text_en": (
            "After goodwill returned, the 'safe new flavor' flopped with cats too—this time "
            "over aroma and texture, not safety. The CEO tasted another can on camera and vowed "
            "another redo, but fewer people cared."
        ),
    },
    "z11": {
        "flavor_text": (
            "また「大事なお知らせ」と告知したので、ファンは新衣装だと思った。"
            "発表されたのは看板Vtuberの卒業。"
            "考察投稿は少なかったが、配信のチャットは長く流れ続けた。"
            "関連グッズの売上と今後の配信収入への不安から、株価は大きく揺れた。"
        ),
        "flavor_text_en": (
            "Another 'big news' tease—fans assumed a new outfit. "
            "It was the star VTuber's graduation. Fewer theories, a long-running chat. "
            "Worries over merch sales and future stream income sent the stock swinging."
        ),
    },
    "z12": {
        "flavor_text": (
            "出演料を巡る交渉の末、温泉側はカピバラたちの待遇改善を約束。"
            "ところが、長時間湯船に居座るようになり、温泉の湯量が不足した。"
            "施設は入浴時間の制限を発表し、騒ぎはいったん収まった。"
        ),
        "flavor_text_en": (
            "After appearance-fee talks, the spa promised better treatment for the capybaras. "
            "They then lingered so long that bath water ran short. "
            "Timed bathing quieted the fuss—for a while."
        ),
    },
    "z13": {
        "anchor_when": "捕獲鳥が別のインコと判明",
        "anchor_when_en": "Caught bird is the wrong parrot",
        "flavor_text": (
            "社長のインコを「捕まえた」という速報で株価が跳ねた。"
            "ところが、連れて帰った鳥は羽の色が違う別のインコ。"
            "訂正が出ると株価は元の位置へ戻った。"
            "前回と同じく、誤報だけで株価が大きく動いた。"
        ),
        "flavor_text_en": (
            "A 'we caught it' flash sent the stock flying. "
            "The bird's colors were wrong—another parrot. "
            "The correction sent the price back. Same rumor-driven whipsaw as before."
        ),
    },
}

HISTORY_POLISH = {
    "v10": (
        "終盤の急落では、慌てた手が損をしやすい。",
        "In a late drop, panic hands often lose first.",
    ),
    "e06": (
        "悲観が広がる局面では、反転の兆しにも目を向けよう。",
        "When gloom spreads, still watch for a turn.",
    ),
    "b09": (
        "好材料が出尽くしたあとの値動きにも注意。",
        "After good news is priced in, watch the next move.",
    ),
    "b03": (
        "買い手が細る瞬間が、祭りの終わりになりやすい。",
        "When buyers thin out, the party often ends.",
    ),
    "v02": (
        "怖い売りのあとは、戻りも速いことが多い。",
        "After panic selling, rebounds often come fast.",
    ),
}

LABEL_EN_POLISH = {
    "v02": "Panic selling, then a rebound",
    "v01": "From rock bottom to new highs",
    "v10": "Late crash, quick bounce",
    "z11": "Same tease, very different news",
    "b03": "The rally ends when buyers dry up",
}


def main() -> None:
    with META.open(encoding="utf-8-sig", newline="") as f:
        reader = csv.DictReader(f)
        fieldnames = list(reader.fieldnames or [])
        rows = list(reader)

    changed = 0
    for row in rows:
        sid = row["stage_id"]
        if sid in UPDATES:
            for k, v in UPDATES[sid].items():
                if row.get(k) != v:
                    row[k] = v
                    changed += 1
        if sid in HISTORY_POLISH:
            jp, en = HISTORY_POLISH[sid]
            if row.get("history_comment") != jp:
                row["history_comment"] = jp
                changed += 1
            if row.get("history_comment_en") != en:
                row["history_comment_en"] = en
                changed += 1
        if sid in LABEL_EN_POLISH:
            en = LABEL_EN_POLISH[sid]
            if row.get("anchor_label_en") != en:
                row["anchor_label_en"] = en
                changed += 1

    # validate anchor lengths
    overs = []
    for row in rows:
        aw = row["anchor_when"]
        if len(aw) > 14:
            overs.append((row["stage_id"], len(aw), aw))
    if overs:
        raise SystemExit(f"anchor_when too long: {overs}")

    with META.open("w", encoding="utf-8", newline="") as f:
        writer = csv.DictWriter(f, fieldnames=fieldnames, lineterminator="\n")
        writer.writeheader()
        writer.writerows(rows)

    print(f"updated {META} field-writes={changed} stages={len(UPDATES)}")


if __name__ == "__main__":
    main()
