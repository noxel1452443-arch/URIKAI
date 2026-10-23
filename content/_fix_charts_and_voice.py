# -*- coding: utf-8 -*-
"""1) Give saga sequels distinct keyframes/shapes
2) Fix mandarin economics
3) Rewrite flavor voice (less dry past-tense stack)
"""
from __future__ import annotations

import csv
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
sys.path.insert(0, str(ROOT / "tools"))

import rewrite_keyframes as rk  # noqa: E402

META = Path(__file__).resolve().parent / "stages_meta.csv"
KF = Path(__file__).resolve().parent / "keyframes.csv"

# sequel stage_id -> meta field updates (shape must match scenario)
SEQUEL_SHAPE: dict[str, dict[str, str]] = {
    # にゃんフーズ ep2: 再下落→弱い戻し（二底）
    "v11": {
        "macro_shape": "W_DOUBLE",
        "scenario_type": "DROP_REBOUND",
        "pivot_timing": "MID",
        "trick": "DEAD_CAT",
        "anchor_band_start": "30",
        "anchor_band_end": "30",
        "anchor_label": "二度下げて弱く戻る",
        "anchor_label_en": "Second dip, weak bounce",
        "history_comment": "一度戻ったあとの再下落は、戻りが弱いことが多い。",
        "history_comment_en": "A second slide after a rebound often bounces weakly.",
    },
    # ぬか床 ep2: 騒ぎのあと収束（上がって収まる）
    "v12": {
        "macro_shape": "INV_N",
        "scenario_type": "WHIPSAW",
        "pivot_timing": "LATE",
        "trick": "",
        "anchor_band_start": "44",
        "anchor_band_end": "44",
        "anchor_label": "上げたあと落ち着く",
        "anchor_label_en": "Up, then settles",
        "history_comment": "騒ぎすぎたあとは、静かに戻ることもある。",
        "history_comment_en": "After too much fuss, things can settle quietly.",
    },
    # 自販機 ep2: 遅くなって沈み、短縮で戻る
    "v13": {
        "macro_shape": "W_DOUBLE",
        "scenario_type": "DROP_REBOUND",
        "pivot_timing": "LATE",
        "trick": "",
        "anchor_band_start": "44",
        "anchor_band_end": "44",
        "anchor_label": "遅れて沈み、直って戻る",
        "anchor_label_en": "Slows, sinks, then recovers",
        "history_comment": "直しすぎも、また直せる。",
        "history_comment_en": "Over-fixing can be fixed again.",
    },
    # カピバラ ep2: 妥結で上げ→制限で落とす
    "z12": {
        "macro_shape": "N_RALLY",
        "scenario_type": "WHIPSAW",
        "pivot_timing": "MID",
        "trick": "BULL_TRAP",
        "anchor_band_start": "30",
        "anchor_band_end": "30",
        "anchor_label": "妥結のあと急落",
        "anchor_label_en": "Deal, then a drop",
        "history_comment": "権利を増やしすぎると、湯も株も薄まる。",
        "history_comment_en": "Too many rights thin both baths and the stock.",
    },
    # Vtuber ep2: 期待で上げ→卒業で崩す
    "z11": {
        "macro_shape": "N_RALLY",
        "scenario_type": "WHIPSAW",
        "pivot_timing": "MID",
        "trick": "",
        "anchor_band_start": "30",
        "anchor_band_end": "30",
        "anchor_label": "期待のあと崩れる",
        "anchor_label_en": "Hope, then a break",
        "history_comment": "同じ言い回しでも、中身が違えば動きは違う。",
        "history_comment_en": "Same wording, different news—different move.",
    },
    # 激辛 ep2: 限定盛り上げ→中身バレで戻る
    "z14": {
        "macro_shape": "INV_N",
        "scenario_type": "WHIPSAW",
        "pivot_timing": "MID",
        "trick": "BULL_TRAP",
        "anchor_band_start": "30",
        "anchor_band_end": "30",
        "anchor_label": "盛り上げて失速",
        "anchor_label_en": "Hype, then fade",
        "history_comment": "名前だけ新しい材料は、長く持たない。",
        "history_comment_en": "A new name on old stock doesn't hold.",
    },
    # インコ ep2: 捕獲報道で跳ね→別人で戻る
    "z13": {
        "macro_shape": "N_RALLY",
        "scenario_type": "WHIPSAW",
        "pivot_timing": "EARLY",
        "trick": "",
        "anchor_band_start": "18",
        "anchor_band_end": "18",
        "anchor_label": "誤報で跳ねて戻る",
        "anchor_label_en": "False catch, then reset",
        "history_comment": "誤報だけで株価が大きく動くこともある。",
        "history_comment_en": "Even a wrong report can whip the price around.",
    },
    # ロボ掃除機 ep2: 禁止で沈み、屋内用途で戻る
    "b11": {
        "macro_shape": "W_DOUBLE",
        "scenario_type": "DROP_REBOUND",
        "pivot_timing": "LATE",
        "trick": "BEAR_TRAP",
        "anchor_band_start": "44",
        "anchor_band_end": "44",
        "anchor_label": "規制のあと戻り",
        "anchor_label_en": "Bounce after the ban",
        "history_comment": "屋外がダメなら、屋内の売り方がある。",
        "history_comment_en": "Ban it outdoors, and an indoor pitch appears.",
    },
    # グミ ep2: 学習グミで急騰→糖分問題で失速
    "b12": {
        "macro_shape": "BUBBLE",
        "scenario_type": "RUNAWAY",
        "pivot_timing": "MID",
        "trick": "",
        "anchor_band_start": "36",
        "anchor_band_end": "36",
        "anchor_label": "再ブームのあと失速",
        "anchor_label_en": "Second boom, then stall",
        "history_comment": "在庫の再利用も、中身がバレると脆い。",
        "history_comment_en": "Recycling stock is fragile once the contents show.",
    },
}

# All flavor rewrites: local-news + dry gag, clearer, less した-stack
FLAVOR: dict[str, tuple[str, str]] = {
    "v01": (
        "新味は「まぐろ風いちご」。全国のネコが、そろって顔を背ける。"
        "回収会見で社長が一缶空けると、なぜか好感だけが先に戻ってくる。",
        "New flavor: tuna-strawberry. Cats turn away in unison. "
        "At the recall, the CEO empties a can on camera—and goodwill comes back first.",
    ),
    "v02": (
        "創業200年のぬか床から、いつもの香りがすっと消える。店は帳を下ろす。"
        "三日三晩の手入れで味が戻ると、待ちわびた客がまた列をつくる。",
        "The usual scent slips out of a 200-year pickle bed. The shop pulls its curtain. "
        "Three days of care bring the flavor back—and the waiting line with it.",
    ),
    "v03": (
        "真冬のリスは、エサ代のためにドングリコインを売り続ける。値段は二度、底を打つ。"
        "春になって余裕が生まれると、「売りすぎた」と気づいて買い戻しが始まる。",
        "Midwinter squirrels keep selling Acorn Coin for food money. The price bottoms twice. "
        "Spring brings spare cash—and the sudden thought they sold too much.",
    ),
    "v04": (
        "売り文句は「火山が見える露天風呂」。その火山が噴火し、島への便が止まる。"
        "安全確認のあと、地元窯の溶岩ピザが話題になり、客足がじわじわ戻る。",
        "The pitch: baths with a volcano view. Then the volcano erupts, and flights stop. "
        "Once cleared safe, kiln-baked lava pizza buzzes—and visitors trickle back.",
    ),
    "v05": (
        "宇宙ステーション初のラーメン店。スープは浮き、麺は飛んでいく。"
        "ゼリー状に直すと大ヒット。ところが「温めると液体に戻る」機能で、また無重力が騒がしくなる。",
        "First ramen shop on the station: broth floats, noodles flee. "
        "A jelly fix sells big—then 'melts when heated' turns zero-g noisy again.",
    ),
    "v06": (
        "新型ロボ掃除機は、ネコに乗られた瞬間ストップする。"
        "開発陣が「ネコ乗せモード」を足すと、飼い主の動画つきでなぜか売れ始める。",
        "The new robot vacuum freezes the moment a cat sits down. "
        "Add a ride-along mode, and owner videos somehow start the sales.",
    ),
    "v07": (
        "当たりは0.3%。画面に書いてあるのは3%。"
        "訂正のたびに売られるが、補償ガチャを配ると離れていた人が少しずつ戻ってくる。",
        "The jackpot is 0.3%. The screen says 3%. "
        "Each correction sells off—until compensation pulls nudge players back.",
    ),
    "v08": (
        "ミカンが大豊作。冷凍庫も倉庫前も箱で埋まり、卸値が崩れる。"
        "町の冷凍ミカン祭りで箱は消えるが、ほとんど仕入れ値割れ。在庫は減っても、帳簿は青いままだ。",
        "A bumper crop of mandarins. Freezers and the yard fill with boxes, and wholesale prices crack. "
        "A town festival clears the boxes—mostly below cost. Stock falls; the books stay blue.",
    ),
    "v09": (
        "全国の自販機が、いっせいに「おつりを計算中」で固まる。"
        "共通ソフトの更新バグが原因。修正が届くたび、一台ずつカシャンと起き上がる。",
        "Vending machines nationwide freeze on 'calculating change.' "
        "A shared software update is to blame. As patches land, they wake one by one—clunk.",
    ),
    "v10": (
        "「パスワードを忘れた」を押すと、銀行のほうまでパスワードを忘れる。"
        "復旧の直後に送金がまた止まり、謝罪は紙の手紙で届く。",
        "'Forgot password' makes the bank forget too. "
        "Right after recovery, transfers die again—and the apology arrives by paper letter.",
    ),
    "v11": (
        "「完全な新味」と謳った缶も、ネコには二度目のそっぽ。"
        "社長はまた会見で試食し、改良を約束する。前回ほど、カメラは集まらない。",
        "Another 'perfectly new' can—and cats turn away again. "
        "The CEO retastes on camera and vows a fix. Fewer cameras this time.",
    ),
    "v12": (
        "かき混ぜすぎたぬか床が、夜中まで泡を立てて騒ぐ。"
        "近所の苦情で営業時間を縮めると、店もぬか床もようやく静かになる。",
        "An over-stirred pickle bed foams and fusses into the night. "
        "Shorter hours after neighbor complaints, and both shop and bed finally quiet down.",
    ),
    "v13": (
        "復旧した自販機は、おつり確認に一人あたり約1分かかる。"
        "「考え中」を短くする更新が来ると、列はふつうの長さに戻る。",
        "After the fix, each machine spends about a minute on change. "
        "A shorter 'thinking' update lands—and the line goes back to normal.",
    ),
    "v14": (
        "溶岩ピザで客が戻った島が、次は火山観察ツアーを売り出す。"
        "肝心の当日、噴煙は出ない。期待外れのキャンセルが続く。",
        "The lava-pizza island next sells volcano-watching tours. "
        "On the day, no plume. Disappointed cancellations follow.",
    ),
    "z01": (
        "辛さ100倍ポテト。半分は泣いて諦め、半分は泣きながらおかわりする。"
        "万人向けの控えめ版を出すと、激辛派にも初心者にも怒られる。",
        "Heat ×100 fries: half quit crying, half order more while crying. "
        "A milder 'for everyone' pack angers both heat fans and newbies.",
    ),
    "z02": (
        "入浴料を上げると宣言した瞬間、名物カピバラが湯から動かない。"
        "会社が折れると、湯気の向こうから今度は出演料の話が出てくる。",
        "The moment a bath-fee hike is announced, the star capybaras refuse to leave the water. "
        "The company blinks—and appearance-fee talks rise out of the steam.",
    ),
    "z03": (
        "「業界最高精度」の天気AIは、晴れが三日続くと次は必ず雨と答える。"
        "当たりが続いたあと、大型イベントの晴れ予報の日に豪雨が落ちる。",
        "The 'top accuracy' weather AI always says rain after three sunny calls. "
        "A lucky streak ends when a sunny outdoor-event forecast brings a downpour.",
    ),
    "z04": (
        "配信の終わり。「大事なお知らせがあります……来週」。考察は一週間で300本を超える。"
        "蓋を開けると新衣装。安心したファンがグッズを買い、会社も一緒に跳ねる。",
        "End of stream: 'Big news… next week.' Fans post over 300 theories. "
        "The reveal is a new outfit. Relieved fans buy merch—and the company jumps with them.",
    ),
    "z05": (
        "中身がほとんど見えない新作。大物配信者が体験版を「神ゲー」と絶賛する。"
        "翌日、別の配信者が同じ体験版を「期待したほどではない」と切り捨てる。",
        "A mostly sealed new game. A star streamer's demo call: 'divine.' "
        "Next day, another plays the same demo and cuts it down: 'not what I hoped.'",
    ),
    "z06": (
        "最短ルートか、氷の上を滑るルートか。投票は滑るほうに決まる。"
        "試験運用では手紙が雪原へ飛び、遅配が続く。防水と目印を足すと、前より速く届く。",
        "Shortest path or ice slide? Votes choose the slide. "
        "Test mail flies into the snow and lags—then waterproofing and lane marks beat the old speed.",
    ),
    "z07": (
        "1回目の試乗会は、3メートル浮いて2秒後に池へ。"
        "2回目は5メートル。高度は伸びるが、着陸の仕方はまだ誰も知らない。",
        "First hop: three meters up, pond in two seconds. "
        "Second: five meters. Better height—landing still nobody's art.",
    ),
    "z08": (
        "「値上げします」「やっぱりしません」「タコを少し小さくします」。客は毎回たこ焼きを割る。"
        "店は切り方と焼き方を変え、値段そのままで原価だけを落とす道を見つける。",
        "'Prices up.' 'Never mind.' 'Slightly smaller octopus.' Customers split every ball. "
        "The shop changes the cut and cook—and keeps the price while trimming cost.",
    ),
    "z09": (
        "売りは「雨でも安心」の屋外イベント保険。再建策が出ると、いったん人気が戻る。"
        "ただし新規契約の雨天補償は半額。保険料は同じなので、主催者の顔が曇る。",
        "Pitch: outdoor event cover, 'safe even in rain.' A turnaround plan briefly restores buzz. "
        "New policies pay half on rainy days at the same premium—organizers cloud over.",
    ),
    "z10": (
        "ペット用品の広告に出ていた社長のインコが、屋根の向こうへ消える。"
        "「見つけた」投稿のたびに株が上がり、別の鳥だと分かるたびに元の枝へ戻る。",
        "The CEO's ad-star parrot vanishes over the rooftops. "
        "Each 'found it' post lifts the stock; each wrong bird returns it to the same branch.",
    ),
    "z11": (
        "また「大事なお知らせ」。ファンは新衣装の続きを想像する。"
        "発表は看板Vtuberの卒業。チャットだけが長く流れ、先の配信とグッズ見通しが揺れる。",
        "Another 'big news' tease. Fans picture another costume. "
        "It is the star VTuber's graduation. Chat keeps scrolling; stream and merch outlook wobble.",
    ),
    "z12": (
        "出演料を払うと約束すると、カピバラはまた湯に戻る。"
        "同時に出た湯量・時間の制限で、妥結の余韻はすぐに薄まる。",
        "Promise appearance fees, and the capybaras return to the bath. "
        "Water and hours limits arrive with the deal—the afterglow thins fast.",
    ),
    "z13": (
        "「社長のインコ、捕獲」の速報で株が跳ねる。"
        "連れて帰った鳥の羽色が違うと分かった瞬間、値段も元の止まり木へ戻る。",
        "Breaking: the CEO's parrot is caught. The stock jumps. "
        "The bird that comes home wears the wrong colors—and the price returns to its perch.",
    ),
    "z14": (
        "「さらに辛くした」大宣伝の新袋が店頭に並ぶ。"
        "開けてみれば中身は前と同じ100倍。新しいのはシールだけ、という声が広がる。",
        "Shelves fill with an 'even hotter' splash bag. "
        "Open it: the same ×100. Word spreads that only the sticker is new.",
    ),
    "b01": (
        "金箔を巻いただけのたい焼きに、最長3時間の列ができる。"
        "目的は写真。SNSに写真があふれるころには、珍しさも列も消えている。",
        "Gold-leaf taiyaki draws lines up to three hours. "
        "Most come for photos. Once feeds are full, novelty—and the line—are gone.",
    ),
    "b02": (
        "なんでも10分で届く通販。利用者も株も伸びていく。"
        "決算の日、「1件届けるたびに赤字」と書かれ、伸びるほど損する話になる。",
        "Anything in ten minutes. Users and the stock both climb. "
        "Earnings day prints 'loss per delivery'—growth means deeper red.",
    ),
    "b03": (
        "前の冬に売って損したリスたちは、冬ごもり前にコインを買い集める。"
        "人間もまねてお祭りになるが、巣にこもった瞬間、買い手が消える。",
        "Squirrels burned by last winter's sales hoard coin before hibernation. "
        "Humans copy the party—until the nests close and buyers vanish.",
    ),
    "b04": (
        "かむと「キュッ」と鳴るグミが品切れ続き。会社は工場を一気に3つ増やす。"
        "新工場が動き出すころ、ブームは終わり、倉庫に鳴らないグミが残る。",
        "Squeaky gummies keep selling out, so three plants go up at once. "
        "By the time they run, the fad is over—and silent gummies fill the warehouses.",
    ),
    "b05": (
        "初競りで1玉100万円のメロン。"
        "落札した社長が「普通の味でした」とだけ言うと、話題はそこでぷつりと切れる。",
        "Opening auction: one melon for a million yen. "
        "The buyer-CEO says only, 'Ordinary melon'—and the story snaps shut.",
    ),
    "b06": (
        "「固すぎてうまい」とバズったプリン屋。"
        "翌週、隣にもっと固い店が開き、列はそのドアの前へ移る。",
        "Buzz: pudding too firm, somehow perfect. "
        "Next week a firmer shop opens next door—and the line moves with it.",
    ),
    "b07": (
        "目標は「すべての駅に1店舗」。借金を重ねて看板を増やしていく。"
        "ブームが去り返済の朝になると、全店のシャッターに同じ貼り紙がかかる。",
        "Goal: one shop in every station. Debt funds more signs. "
        "When the boom fades and repayment morning comes, every shutter wears the same notice.",
    ),
    "b08": (
        "「丸い家は運気が上がる」と評判になり、ドーム住宅の注文が走る。"
        "ローン審査が厳しくなると、モデルハウスは満員なのに契約書だけが減っていく。",
        "Rumor says round homes bring luck, and dome orders race ahead. "
        "When mortgages tighten, show homes stay crowded—signed contracts thin out.",
    ),
    "b09": (
        "キックボード人気ランキング1位。記念キャンペーンに乗って期待も買われる。"
        "順位は一週間限定の投票。全社員通勤を始めた翌日、半分が筋肉痛で休む。",
        "Kickboard poll #1. Promo launches; expectation gets bought too. "
        "It is a one-week online vote. Staff commute day two: half call in sore.",
    ),
    "b10": (
        "「飲むと10歳若返る」健康茶、という触れ込み。"
        "成分を開けると特別な根拠はなく、中身はほぼ麦茶。",
        "Pitch: tea that 'takes ten years off.' "
        "Open the lab notes: no special proof—mostly barley tea.",
    ),
    "b11": (
        "ネコ乗せモードが流行し、公園の道までロボで埋まる。"
        "条例で禁止されると売れ行きは落ちるが、屋内専用としてまた動き出す。",
        "Cat-ride mode catches on until park paths fill with bots. "
        "An ordinance ban hurts sales—then the indoor-only pitch starts them again.",
    ),
    "b12": (
        "鳴るグミのブームが終わり、在庫は「学習用の無音グミ」として売り直される。"
        "図書館では予想外に人気。ただし糖分は、勉強より先にバレる。",
        "When the squeak fad ends, leftover stock returns as 'silent study gummies.' "
        "Libraries love them. The sugar content shows before the studying does.",
    ),
    "e01": (
        "2周目では全キャラが逆立ちで歩き、「哲学かバグか」で動画が燃える。"
        "公式が不具合と直すと、話題の芯が消え、配信も売上も急に冷える。",
        "New Game+: everyone walks on their hands. Philosophy-or-bug videos catch fire. "
        "Official patches it as a bug—the gimmick vanishes, and streams and sales go cold.",
    ),
    "e02": (
        "補償半額で立て直した屋外イベント保険。"
        "想定外の地域まで豪雨が広がり、半額でも支払いが契約の山を越えていく。",
        "Outdoor cover rebuilt on half rainy-day payouts. "
        "Rain spreads farther than the map—even half the claims climb past the contract pile.",
    ),
    "e03": (
        "時速20キロののんびり鉄道は、景色目当てで乗客を増やしていく。"
        "「来月から運賃3倍」の一文が出た瞬間、乗客も株もいっせいに降りる。",
        "A 20 km/h scenic railway keeps filling cars. "
        "One line—'fares triple next month'—and passengers and the stock both get off.",
    ),
    "e04": (
        "暖冬で、倉庫の雪だるまが全部ゆるく溶けかける。"
        "最後の週に寒波が来ると、注文が一気に押し寄せる。",
        "A warm winter leaves every warehouse snowman half-melted. "
        "A late cold snap, and orders rush in at once.",
    ),
    "e05": (
        "値段が戻ったので、会社は農家との買い取り契約を増やす。"
        "翌年も豊作。約束どおり箱は届き、卸値はまた沈む。倉庫の前に、同じ景色が戻る。",
        "Prices recover, so the company signs more farm buy deals. "
        "Next year another bumper crop. Boxes arrive as promised, wholesale sinks again—and the same yard returns.",
    ),
    "e06": (
        "試作ロボが工場を歩いて出ていき、戻ってこない。会社は倒産寸前。"
        "山奥の災害現場で物資を運んでいると分かり、銀行が追加融資を出す。",
        "The prototype walks out of the plant and does not return. Bankruptcy looms. "
        "It turns up hauling relief in the mountains—and the bank adds credit.",
    ),
    "e07": (
        "人口80人の島が独立し、島のコインは乱高下する。"
        "「10枚でサザエ一皿」と交換が決まると、やっと使い道のある値段がつく。",
        "An island of eighty declares independence; its coin thrashes. "
        "Ten coins for a plate of turban shell—and the price finally has a use.",
    ),
    "e08": (
        "散歩もトイレもいらないAIペットが、しばらく街を席巻する。"
        "半年後、多くの電源は切られたまま忘れられ、追加課金も止まる。",
        "AI pets that need no walks and no litter take over for a while. "
        "Half a year on, most sit powered off and forgotten—and the upsells stop.",
    ),
    "e09": (
        "隣に客を取られたプリン屋が、閉店セールを出す。"
        "開店のときより長い列のあと、最終日を過ぎると店は空になる。",
        "Beaten by the neighbor, the pudding shop runs a closing sale. "
        "A longer line than opening day—then, after the last day, an empty shop.",
    ),
    "e10": (
        "駅前の株価ボードが数分間、全銘柄を「1円」と出す。製造元の株も巻き添えになる。"
        "表示ソフトの不具合と分かったあと、「珍表示」を求める注文がむしろ増える。",
        "The station board prices every ticker at ¥1 for a few minutes—maker stock gets hit too. "
        "It is a display bug. Afterward, orders for the odd lookout actually rise.",
    ),
    "e11": (
        "金箔の仕入れが止まり、普通のたい焼きを同じ値段で売り続ける。"
        "写真映えがなくなると、並ぶ理由もなくなり、列は消える。",
        "Gold leaf runs out, so plain taiyaki stays at the gold price. "
        "Without the shine, nobody lines up.",
    ),
    "e12": (
        "赤字埋めに、倉庫を9割引きで放つ。"
        "荷物はどんどん動く。利益は動かない。株も安売りに付き合う。",
        "To plug the red, the warehouse goes ninety percent off. "
        "Goods move fast. Profit does not—and the stock joins the clearance.",
    ),
    "e13": (
        "お茶はこぼす、ドアには挟まる、欠点だらけのロボ執事。"
        "「不器用でかわいい」で予約が殺到するが、修理が追いつかず納期だけが先へ滑る。",
        "A butler-bot that spills tea and pinches doors. "
        "'Clumsy-cute' sells out the waitlist—then repairs lag and ship dates slide.",
    ),
    "e14": (
        "冬の入り口に始まったドングリコイン祭りは、大晦日に景色が変わる。"
        "「もう十分」と考えたリスたちが一斉に売り、祭りの前よりは高いまま急落する。",
        "The early-winter Acorn Coin party changes on New Year's Eve. "
        "Squirrels sell together saying 'enough'—a plunge, still above where the party began.",
    ),
    "e15": (
        "限定版は予約開始から数分で完売。中身はまだ誰も知らない。"
        "特典は増産しないと発表され、予約権の転売まで値が跳ねる。",
        "The limited edition sells out minutes after preorders open. Nobody has played it. "
        "Extras will not be reprinted—and even reservation rights spike.",
    ),
}


def load_meta_loose() -> list[rk.StageMeta]:
    """Like rewrite_keyframes.load_meta, but without the old 30+15 count lock."""
    rows: list[rk.StageMeta] = []
    with META.open(encoding="utf-8-sig", newline="") as f:
        for row in csv.DictReader(f):
            sid = row["stage_id"].strip()
            if not sid:
                continue
            rows.append(
                rk.StageMeta(
                    stage_id=sid,
                    macro_shape=row["macro_shape"].strip(),
                    pivot_timing=row["pivot_timing"].strip(),
                    trick=(row.get("trick") or "").strip(),
                    seed=int(row["seed"]),
                )
            )
    return rows


def rebuild_keyframes(targets: set[str]) -> None:
    metas = {m.stage_id: m for m in load_meta_loose()}
    with KF.open(encoding="utf-8-sig", newline="") as f:
        rows = list(csv.DictReader(f))
        fields = list(rows[0].keys())

    keep = [r for r in rows if r["stage_id"] not in targets]
    for sid in sorted(targets):
        meta = metas[sid]
        if rk.is_extreme_stage(sid):
            points = rk.build_extreme_stage(meta)
        else:
            points = rk.build_stage(meta)
        for sec, pr in points:
            keep.append({"stage_id": sid, "second": str(sec), "price_ratio": f"{pr:.1f}"})

    order = [m.stage_id for m in load_meta_loose()]
    rank = {sid: i for i, sid in enumerate(order)}
    keep.sort(key=lambda r: (rank.get(r["stage_id"], 999), int(r["second"])))

    with KF.open("w", encoding="utf-8", newline="") as f:
        w = csv.DictWriter(f, fieldnames=fields, lineterminator="\n")
        w.writeheader()
        w.writerows(keep)
    print(f"rebuilt keyframes for {sorted(targets)}")


def main() -> None:
    with META.open(encoding="utf-8-sig", newline="") as f:
        reader = csv.DictReader(f)
        fields = list(reader.fieldnames or [])
        rows = list(reader)

    ids = {r["stage_id"] for r in rows}
    missing = sorted(set(FLAVOR) - ids)
    extra = sorted(ids - set(FLAVOR))
    if missing or extra:
        raise SystemExit(f"flavor missing={missing} extra={extra}")

    for row in rows:
        sid = row["stage_id"]
        if sid in SEQUEL_SHAPE:
            row.update(SEQUEL_SHAPE[sid])
        jp, en = FLAVOR[sid]
        row["flavor_text"] = jp
        row["flavor_text_en"] = en
        if len(row["anchor_when"]) > 14:
            raise SystemExit(f"{sid}: anchor_when too long: {row['anchor_when']}")

    with META.open("w", encoding="utf-8", newline="") as f:
        w = csv.DictWriter(f, fieldnames=fields, lineterminator="\n")
        w.writeheader()
        w.writerows(rows)

    rebuild_keyframes(set(SEQUEL_SHAPE))
    print(f"updated meta flavors={len(FLAVOR)} sequel_shapes={len(SEQUEL_SHAPE)}")


if __name__ == "__main__":
    main()
