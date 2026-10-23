"""全ステージ flavor 推敲 + 地続き ep2 を6本追加。"""
from __future__ import annotations

import csv
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
META = ROOT / "content" / "stages_meta.csv"
KF = ROOT / "content" / "keyframes.csv"

# --- フレーバー推敲（全件） ---
FLAVOR: dict[str, tuple[str, str]] = {
    "v01": (
        "新味「まぐろ風いちご」に全国のネコがそっぽを向いた。全品回収の会見で社長が一缶を食べてみせると、好感度だけが先に戻った。",
        "Cats nationwide refused the new 'tuna-strawberry' flavor. At the recall press conference the CEO ate a can on camera—and goodwill came back first.",
    ),
    "v02": (
        "創業200年のぬか床が「元気がない」と店を閉じた。三日三晩かき混ぜると、ぬか床は閉店前より元気になった。",
        "The 200-year-old pickle bed looked 'tired,' so the shop closed. After three days of nonstop stirring, it was livelier than before.",
    ),
    "v03": (
        "森のリスたちが冬のエサ代にドングリコインを売り続け、値段は二度も底を打った。春、余ったドングリを換金して買い戻した。",
        "Forest squirrels sold Acorn Coin all winter for food money, and the price bottomed twice. In spring they cashed leftover acorns and bought back.",
    ),
    "v04": (
        "売りは「火山が見える露天風呂」。その火山が噴火し、飛行機は全便欠航になった。島の人が溶岩で焼いたピザが話題になると、客はじわじわ戻ってきた。",
        "Its selling point: a bath with a volcano view. Then the volcano erupted and every flight was canceled. When islanders' lava-baked pizza went viral, guests slowly returned.",
    ),
    "v05": (
        "宇宙ステーション初のラーメン店。スープは浮き、麺は飛んだ。改良版はゼリー状。「ラーメンじゃない」と一度は言われたが、みんな食べた。",
        "The first ramen shop on a space station. Broth floated, noodles flew. The jelly version drew 'that's not ramen'—then everyone ate it anyway.",
    ),
    "v06": (
        "新型ロボ掃除機、ネコに上に乗られて一歩も動けず。開発は「ネコ乗せモード」を追加した。予想外に売れ、公園の散歩道が渋滞した。",
        "The new robot vacuum froze under any cat. The team added 'cat ride mode.' It sold so well that park paths clogged with riding cats.",
    ),
    "v07": (
        "当たり確率を「0.3%」と書くはずが「3%」と書いていた。謝るたびに売られたが、おわびの無料ガチャを配ると前より人が増えた。",
        "Jackpot odds should have said 0.3%. They said 3%. Every apology brought selling—until free apology pulls brought more players than before.",
    ),
    "v08": (
        "豊作で冷凍庫が足りず、倉庫のドアが内側から開いた。値段はほぼタダ。町の「冷凍ミカン祭り」で山は一夜で消えた。",
        "Too big a harvest, not enough freezers—the warehouse door burst open and prices hit near zero. A town festival cleared the mountain overnight.",
    ),
    "v09": (
        "全国の自販機が「おつりを考え中」で止まった。原因は、1台のボタンを全部押した小学生。復旧の瞬間、止まっていた売上もまとめて戻った。",
        "Every vending machine froze on 'calculating change'—one kid had mashed every button on one unit. When they restarted, the lost sales came back at once.",
    ),
    "v10": (
        "「パスワードを忘れた」を押すと、銀行のほうがパスワードを忘れた。直った直後にもう一度止まり、復旧のお知らせは紙の手紙で届いた。",
        "Pressing 'forgot password' made the bank forget it instead. Right after the fix it went down again. The 'all fixed' notice arrived by paper letter.",
    ),
    "z01": (
        "「辛さ100倍」のポテト。食べた人の半分は泣き、半分は泣きながらおかわりした。「控えめ版」は両方から怒られ、すぐ元に戻した。",
        "Chips sold as '100x spicier.' Half cried; the other half cried and asked for seconds. A 'mild' version angered both sides, so it was pulled fast.",
    ),
    "z02": (
        "温泉が入浴料を上げると、カピバラたちが「なら出演料を払え」と湯船から出なくなった。値上げは取りやめになったが、話し合いは今も湯の中だ。",
        "When the spa raised bath fees, the capybaras refused to leave the water: 'Then pay us an appearance fee.' The hike was canceled; talks continue in the hot water.",
    ),
    "z03": (
        "的中率はちょうど50%。大当たりの翌日に大外れ。「晴れ」と「雨」を交互に言っているだけだと、まだ誰も気づいていない。",
        "Accuracy: exactly 50%. A big hit, then a big miss the next day. Nobody has noticed it just alternates 'sunny' and 'rain.'",
    ),
    "z04": (
        "配信の最後に「大事なお知らせがあります…来週」。ファンの考察は一週間で300本を超えた。発表はただの新衣装だったが、みんな喜んだ。",
        "End of stream: 'Big news… next week.' Fans posted 300 theories in a week. It was just a new outfit, and everyone still loved it.",
    ),
    "z05": (
        "中身ほぼ非公開の新作。体験版を遊んだ大物が「神ゲー」と言った翌日、別の配信者が「クソゲー」と言った。",
        "A barely revealed new game. A star streamer called the demo a masterpiece. The next day another called it trash.",
    ),
    "z06": (
        "最短ルートか、氷の上を滑るルートか。投票で滑るルートに決まり、手紙は速く、少し濡れて届くようになった。",
        "Shortest route, or slide across the ice? The vote picked sliding. Letters now arrive faster—and a little wet.",
    ),
    "z07": (
        "1回目の試乗会、3メートル浮いて2秒後に池へ。それでも「次こそ」と期待が集まった2回目、5メートル浮いて3秒後に池へ。",
        "First test ride: up 3 meters, into the pond in 2 seconds. Hopes still rose for round two: up 5 meters, into the pond in 3.",
    ),
    "z08": (
        "「値上げします」「やっぱりしません」「タコを小さくします」。発表のたびに、客はたこ焼きを割って中を確かめた。",
        "'Prices up.' 'Never mind.' 'Smaller octopus instead.' After each update, customers split their takoyaki open to check.",
    ),
    "z09": (
        "「雨でも安心」の屋外イベント保険。立て直しプランの中身は、「晴れ予報の日だけ引き受ける」だった。",
        "Outdoor event insurance sold as 'covered even in rain.' The rescue plan: only cover days the forecast says sunny.",
    ),
    "z10": (
        "社長のインコが逃げ出した。目撃情報が出るたびに株が上がり、「見間違いでした」のたびに下がった。",
        "The CEO's parrot escaped. Every sighting sent the stock up. Every 'sorry, wrong bird' sent it down.",
    ),
    "b01": (
        "金箔を巻いただけのたい焼きに3時間の行列。並んだ人の多くは自分の写真だけ撮って帰り、写真が広がると誰も並ばなくなった。",
        "Gold-leaf taiyaki drew a 3-hour line. Most people only photographed themselves in it—once the photos spread, the line vanished.",
    ),
    "b02": (
        "なんでも10分配送の通販。売れば売るほど赤字だと分かったのは、決算の日だった。",
        "Anything delivered in 10 minutes. Only on earnings day did anyone notice: the more it sold, the more it lost.",
    ),
    "b03": (
        "冬ごもり前、リスたちがドングリコインを買い集め、人間もまねして森はお祭りに。巣にこもると、買う人はいなくなった。",
        "Before winter, squirrels hoarded Acorn Coin and humans copied them. When the squirrels holed up, no buyers were left.",
    ),
    "b04": (
        "かむと「キュッ」と鳴るグミが品切れ続き。工場を3つ増やした月に、みんなその音に飽きた。",
        "Gummies that squeak when you chew kept selling out. The month they built three new factories, everyone got tired of the squeak.",
    ),
    "b05": (
        "初競りで1玉100万円。落札した社長は、切ってみたら普通のメロンの味だったと正直に話した。",
        "First auction of the year: one melon, 1 million yen. The winning bidder admitted it tasted like a normal melon.",
    ),
    "b06": (
        "SNSで「固すぎて逆にうまい」とバズったプリン屋。翌週、隣にもっと固い店ができた。",
        "A pudding shop went viral for being 'so firm it's great.' The next week, a firmer shop opened next door.",
    ),
    "b07": (
        "目標は「全部の駅に1店舗」。借金で店を増やし続けたある朝、全店のシャッターに同じ貼り紙が出た。",
        "The goal: one shop at every station, all on borrowed money. One morning, every shutter had the same notice taped to it.",
    ),
    "b08": (
        "「丸い家は運気が上がる」と言われ、値段は毎月上がった。ローン審査が厳しくなった日、丸い家はただの丸い家に戻った。",
        "'Round houses bring good luck,' they said, and prices rose every month. The day loans got stricter, round houses became just round houses again.",
    ),
    "b09": (
        "ランキング1位の記念に、全社員でキックボード通勤を始めた。翌日、半分が筋肉痛で休んだ。",
        "To celebrate hitting #1, the whole company commuted by kickboard. The next day, half called in sore.",
    ),
    "b10": (
        "「飲むと10歳若返る」健康茶。専門家が中身を調べたら、ほぼ麦茶だった。",
        "Health tea that 'makes you 10 years younger.' Experts tested it. It was mostly barley tea.",
    ),
    "e01": (
        "2周目では全キャラが逆立ちで歩く。「深い」派と「バグ」派が騒いだ末、公式が「バグです」と発表した。",
        "In New Game+, every character walks on their hands. Deep vs bug arguments ended when the developer said: 'It's a bug.'",
    ),
    "e02": (
        "晴れ予報の日だけ引き受けたはずが、予報がことごとく外れ、ゲリラ豪雨で野外イベントが全滅。2度目の会見は傘の下だった。",
        "They only covered sunny-forecast days. Then every forecast failed and outdoor events drowned. The second presser was under umbrellas.",
    ),
    "e03": (
        "時速20キロののんびり鉄道は、景色目当てで乗客がじわじわ増えていた。「来月から運賃3倍」の発表で、乗客も株も一気に逃げた。",
        "A 20 km/h railway grew riders for the scenery. The day they announced triple fares from next month, riders and investors fled at once.",
    ),
    "e04": (
        "暖冬で、倉庫の雪だるまが全部溶けかけていた。最後の週に寒波が来て、注文が一気に殺到した。",
        "A warm winter left the warehouse snowmen half-melted. Then a cold wave hit in the final week, and orders flooded in.",
    ),
    "e05": (
        "在庫がやっと消えたと思ったら、翌年も裏山が大豊作。倉庫の前に、またミカンの山ができた。",
        "Just as stock cleared, the hills had another bumper crop. A new mountain of mandarins rose outside the warehouse.",
    ),
    "e06": (
        "試作ロボが工場から歩いて出ていき、戻ってこなかった。それでも銀行は「夢がある」と助け舟を出した。",
        "The prototype robot walked out of the factory and never came back. The bank stepped in anyway: 'There's still a dream here.'",
    ),
    "e07": (
        "人口80人の島が独立を宣言し、島のコインは大混乱。島名物のサザエと交換できると分かり、一気に買われた。",
        "An island of 80 people declared independence, and its coin went haywire. Then word spread it bought the island's shellfish—and buyers rushed in.",
    ),
    "e08": (
        "散歩もトイレもいらないAIペットが大ブーム。半年後、飼い主の多くは電源を切ったまま忘れていた。",
        "AI pets that never need walks or litter boxes became a craze. Six months later, most owners had switched them off and forgotten.",
    ),
    "e09": (
        "隣の固い店に負け、2号店は閉店セール。開店日より長い行列ができたが、最終日が過ぎると何も残らなかった。",
        "Beaten by the firmer neighbor, shop #2 held a closing sale. The line beat opening day—then the last day ended, and nothing was left.",
    ),
    "e10": (
        "駅前の株価ボードが、数分だけ全部の銘柄を「1円」と表示した。同社株は急落したが、「1円の瞬間」の写真が話題になり、ボード注文は増えた。",
        "For a few minutes the station price board showed every stock at 1 yen. Its maker's stock plunged—but photos of the glitch went viral, and board orders rose.",
    ),
    "e11": (
        "金箔が足りなくなり、金色の紙を巻いて売り始めた。気づいた客から順に、来なくなった。",
        "Gold leaf ran short, so they wrapped taiyaki in gold paper. Customers stopped coming as they noticed, one by one.",
    ),
    "e12": (
        "赤字穴埋めのため、倉庫在庫を9割引きで放出。会社の株も、同じくらい値引きされた。",
        "To plug losses they dumped warehouse stock at 90% off. The company's stock got about the same discount.",
    ),
    "e13": (
        "お茶はこぼす、ドアには挟まる、欠点だらけのロボ執事。それが「かわいい」と言われ、予約が殺到した。",
        "A robot butler that spills tea and gets stuck in doors. People called it adorable, and preorders poured in.",
    ),
    "e14": (
        "年越し直前、リスたちが「もう十分」と一斉に売った。それでも、お祭り前よりはまだ高かった。",
        "Right before New Year the squirrels decided they'd had enough and sold together. It still sat above where the party began.",
    ),
    "e15": (
        "限定版が予約だけで全国品切れ。発売前で中身は誰も知らないのに、予約の権利が高値で転売された。",
        "The limited edition sold out on preorders alone. It isn't out and nobody knows what's inside—but preorder rights sell high.",
    ),
    "v11": (
        "好感度が戻ったあと出した「安全な新味」も、ネコには不評だった。社長はまた会見で一缶食べたが、今度は誰も泣かなかった。",
        "After goodwill returned, the 'safe new flavor' flopped with cats too. The CEO ate another can on camera—this time nobody cried.",
    ),
    "v12": (
        "かき混ぜすぎて元気になったぬか床は、夜中も泡を立てた。近所の苦情で営業時間を縮めると、落ち着きを取り戻した。",
        "The over-stirred pickle bed bubbled all night. Neighbors complained, hours were cut, and it finally calmed down.",
    ),
    "v13": (
        "復旧のあと、今度はおつりの計算が丁寧すぎて行列ができた。ソフト更新で「考え中」が短くなると、また普通に売れた。",
        "After the fix, change calculation got too careful and lines formed. A patch shortened 'thinking,' and sales went normal again.",
    ),
    "z11": (
        "また「大事なお知らせ」と言ったので、みんなが新衣装だと思った。発表は卒業だった。考察は少なかったが、チャットは長かった。",
        "Another 'big news' tease—fans assumed a new outfit. It was a graduation. Fewer theories, longer chat.",
    ),
}

# (stage_id, copy_kf_from, meta without stage_id)
NEW: list[tuple[str, str, dict[str, str]]] = [
    (
        "b11",
        "v06",
        {
            "brand_name": "ロボ掃除機、ネコ乗せで道が埋まる",
            "brand_name_en": "Cat-ride vacuums clog the paths",
            "period": "家電",
            "period_en": "Gadgets",
            "market_kind": "STOCK",
            "instrument_name": "クルクル電機",
            "instrument_name_en": "Kurukuru Electric",
            "base_price": "3240",
            "calendar_start": "家電",
            "calendar_start_en": "Gadgets",
            "calendar_end": "家電",
            "calendar_end_en": "Gadgets",
            "anchor_when": "公園でネコ乗せ禁止",
            "anchor_when_en": "Parks ban cat rides",
            "anchor_band_start": "30",
            "anchor_band_end": "30",
            "scenario_type": "DROP_REBOUND",
            "macro_shape": "V_RECOVER",
            "pivot_timing": "MID",
            "trick": "",
            "flavor_text": "ネコ乗せモードが流行し、散歩道がロボで埋まった。公園が禁止を出すと売れ行きは落ちたが、屋内専用としてまた動き出した。",
            "flavor_text_en": "Cat-ride mode clogged the walking paths. Park bans hurt sales—then the indoor-only pitch brought them back.",
            "anchor_label": "規制のあと戻り",
            "anchor_label_en": "Bounce after the ban",
            "history_comment": "流行の止め方を間違えると、別の売り方が見つかる。",
            "history_comment_en": "Ban the fad outdoors, and a new pitch appears indoors.",
            "seed": "311",
            "saga_ep": "2",
            "tier": "",
        },
    ),
    (
        "z12",
        "z02",
        {
            "brand_name": "カピバラ温泉、出演料で湯が足りない",
            "brand_name_en": "Capybara spa: fees drain the baths",
            "period": "旅行",
            "period_en": "Travel",
            "market_kind": "STOCK",
            "instrument_name": "ぬくぬく温泉郷",
            "instrument_name_en": "Nukunuku Onsen Resorts",
            "base_price": "2760",
            "calendar_start": "旅行",
            "calendar_start_en": "Travel",
            "calendar_end": "旅行",
            "calendar_end_en": "Travel",
            "anchor_when": "湯量制限を発表",
            "anchor_when_en": "Bath water rationed",
            "anchor_band_start": "43",
            "anchor_band_end": "43",
            "scenario_type": "WHIPSAW",
            "macro_shape": "RANGE",
            "pivot_timing": "MID",
            "trick": "",
            "flavor_text": "出演料で妥結したカピバラは、湯船に長く居座る権利まで取った。湯が足りなくなり制限を出すと、いったんは静かになった。",
            "flavor_text_en": "After settling appearance fees, the capybaras also won the right to linger. Water ran short; rationing quieted things—for a while.",
            "anchor_label": "妥結のあと揺れる",
            "anchor_label_en": "Wobbles after the deal",
            "history_comment": "権利を増やしすぎると、湯も株も薄まる。",
            "history_comment_en": "Too many rights thin both the baths and the stock.",
            "seed": "212",
            "saga_ep": "2",
            "tier": "",
        },
    ),
    (
        "z13",
        "z10",
        {
            "brand_name": "迷子のインコ、本物が帰ってきた？",
            "brand_name_en": "Lost parrot: the real one returns?",
            "period": "ペット",
            "period_en": "Pets",
            "market_kind": "STOCK",
            "instrument_name": "ことりペット商会",
            "instrument_name_en": "Kotori Pet Co.",
            "base_price": "560",
            "calendar_start": "ペット",
            "calendar_start_en": "Pets",
            "calendar_end": "ペット",
            "calendar_end_en": "Pets",
            "anchor_when": "社長が違う鳥を連れて帰る",
            "anchor_when_en": "CEO brings the wrong bird",
            "anchor_band_start": "28",
            "anchor_band_end": "28",
            "scenario_type": "WHIPSAW",
            "macro_shape": "RANGE",
            "pivot_timing": "EARLY",
            "trick": "",
            "flavor_text": "「本物発見」の投稿で株が跳ねた。社長が連れて帰った鳥は、よく見ると色が違った。また見間違いの季節が始まった。",
            "flavor_text_en": "A 'we found it' post sent the stock flying. The bird the CEO brought home was the wrong color. Wrong-bird season began again.",
            "anchor_label": "発見のあと揺れる",
            "anchor_label_en": "Wobbles after the 'find'",
            "history_comment": "本物っぽい話ほど、確認が必要。",
            "history_comment_en": "The more 'real' it sounds, the more you need to check.",
            "seed": "213",
            "saga_ep": "2",
            "tier": "",
        },
    ),
    (
        "v14",
        "v04",
        {
            "brand_name": "火山島リゾート、噴火観光が常態化",
            "brand_name_en": "Volcano resort makes eruptions routine",
            "period": "旅行",
            "period_en": "Travel",
            "market_kind": "STOCK",
            "instrument_name": "南海アイランド観光",
            "instrument_name_en": "Nankai Island Tours",
            "base_price": "1560",
            "calendar_start": "旅行",
            "calendar_start_en": "Travel",
            "calendar_end": "旅行",
            "calendar_end_en": "Travel",
            "anchor_when": "噴火カレンダーを公開",
            "anchor_when_en": "Eruption calendar published",
            "anchor_band_start": "10",
            "anchor_band_end": "10",
            "scenario_type": "DROP_REBOUND",
            "macro_shape": "EARLY_SHOCK",
            "pivot_timing": "EARLY",
            "trick": "",
            "flavor_text": "溶岩ピザが当たった島は、次に「噴火が見える日程表」を出した。欠航は減ったが、予定どおり噴火しない日のクレームが増えた。",
            "flavor_text_en": "After lava pizza hit, the island published an 'eruption viewing calendar.' Cancellations fell—complaints rose on days that didn't erupt on schedule.",
            "anchor_label": "予定外で揺れる",
            "anchor_label_en": "Wobbles off-schedule",
            "history_comment": "自然を予定にすると、予定外れが商品になる。",
            "history_comment_en": "Schedule nature, and missed schedules become the product.",
            "seed": "114",
            "saga_ep": "2",
            "tier": "",
        },
    ),
    (
        "b12",
        "b04",
        {
            "brand_name": "人気グミ、無音版でまた品切れ",
            "brand_name_en": "Silent gummies sell out again",
            "period": "食品",
            "period_en": "Food",
            "market_kind": "STOCK",
            "instrument_name": "もちもち製菓",
            "instrument_name_en": "Mochimochi Confectionery",
            "base_price": "2050",
            "calendar_start": "食品",
            "calendar_start_en": "Food",
            "calendar_end": "食品",
            "calendar_end_en": "Food",
            "anchor_when": "無音グミが品切れ",
            "anchor_when_en": "Silent gummies sell out",
            "anchor_band_start": "39",
            "anchor_band_end": "39",
            "scenario_type": "RUNAWAY",
            "macro_shape": "GRIND_UP",
            "pivot_timing": "MID",
            "trick": "BULL_TRAP",
            "flavor_text": "キュッに飽きたあと出た「無音グミ」が、静かに品切れした。音がないのに、また工場が足りなくなった。",
            "flavor_text_en": "After the squeak wore off, 'silent gummies' sold out quietly. No sound—and somehow, not enough factories again.",
            "anchor_label": "静かに過熱",
            "anchor_label_en": "Quiet overheating",
            "history_comment": "流行の逆を取っても、また足りなくなる。",
            "history_comment_en": "Even the opposite of a fad can run short.",
            "seed": "312",
            "saga_ep": "2",
            "tier": "",
        },
    ),
    (
        "z14",
        "z01",
        {
            "brand_name": "激辛ポテト、さらに辛い限定が来る",
            "brand_name_en": "Hot chips: an even hotter limited run",
            "period": "食品",
            "period_en": "Food",
            "market_kind": "STOCK",
            "instrument_name": "ホットスナック工業",
            "instrument_name_en": "Hot Snack Industries",
            "base_price": "1350",
            "calendar_start": "食品",
            "calendar_start_en": "Food",
            "calendar_end": "食品",
            "calendar_end_en": "Food",
            "anchor_when": "限定200倍を発売",
            "anchor_when_en": "Limited 200x launched",
            "anchor_band_start": "33",
            "anchor_band_end": "33",
            "scenario_type": "WHIPSAW",
            "macro_shape": "RANGE",
            "pivot_timing": "MID",
            "trick": "BULL_TRAP",
            "flavor_text": "元の辛さに戻した翌月、「200倍限定」が出た。泣く人もおかわりする人も同じ顔で、株だけが先に動いた。",
            "flavor_text_en": "A month after restoring the original heat, a 'limited 200x' dropped. Criers and second-helpers wore the same face—the stock moved first.",
            "anchor_label": "限定で乱高下",
            "anchor_label_en": "Limited-run whipsaw",
            "history_comment": "一度落ち着いても、限定はまた揺らす。",
            "history_comment_en": "Calm never lasts when a limited drop arrives.",
            "seed": "214",
            "saga_ep": "2",
            "tier": "",
        },
    ),
]


def main() -> None:
    with META.open(encoding="utf-8", newline="") as f:
        rows = list(csv.DictReader(f))
    fields = list(rows[0].keys())

    for r in rows:
        sid = r["stage_id"]
        if sid in FLAVOR:
            r["flavor_text"], r["flavor_text_en"] = FLAVOR[sid]

    existing = {r["stage_id"] for r in rows}
    for sid, _src, meta in NEW:
        if sid in existing:
            # refresh flavor/meta for existing new rows if re-run
            for r in rows:
                if r["stage_id"] == sid:
                    for k, v in meta.items():
                        r[k] = v
            continue
        row = {k: "" for k in fields}
        row["stage_id"] = sid
        row.update(meta)
        rows.append(row)
        existing.add(sid)

    with META.open("w", encoding="utf-8", newline="") as f:
        w = csv.DictWriter(f, fieldnames=fields, lineterminator="\n")
        w.writeheader()
        w.writerows(rows)

    with KF.open(encoding="utf-8", newline="") as f:
        kf_rows = list(csv.DictReader(f))
    kf_fields = list(kf_rows[0].keys())
    by_src: dict[str, list[dict[str, str]]] = {}
    for r in kf_rows:
        by_src.setdefault(r["stage_id"], []).append(r)
    have = {r["stage_id"] for r in kf_rows}
    for sid, src, _meta in NEW:
        if sid in have:
            continue
        for r in by_src[src]:
            nr = dict(r)
            nr["stage_id"] = sid
            kf_rows.append(nr)
        have.add(sid)

    with KF.open("w", encoding="utf-8", newline="") as f:
        w = csv.DictWriter(f, fieldnames=kf_fields, lineterminator="\n")
        w.writeheader()
        w.writerows(kf_rows)

    # 図鑑絵は ep1 参照で別生成すること（親コピーは一貫性・場面差の両方を損なう）
    print(f"meta stages={len(rows)} keyframes_rows={len(kf_rows)}")
    print("new:", [s for s, _, _ in NEW])
    print("NOTE: generate sequel art from ep1 reference via make_stage_art.py (do not copy parent PNG)")


if __name__ == "__main__":
    main()
