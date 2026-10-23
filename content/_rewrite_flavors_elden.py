# -*- coding: utf-8 -*-
"""Rewrite flavor_text toward local-news chyron + dry punchline.

Clear first, short second. Casual URIKAI absurdism, not solemn lore.
Avoid long investor/stock lectures; keep the gag readable in one glance.
"""
from __future__ import annotations

import csv
from pathlib import Path

META = Path(__file__).resolve().parent / "stages_meta.csv"

# stage_id -> (jp, en)
FLAVOR: dict[str, tuple[str, str]] = {
    "v01": (
        "新味「まぐろ風いちご」に、全国のネコがそっぽを向いた。"
        "全品回収の会見で社長が一缶食べてみせると、なぜか好感だけが先に戻った。",
        "Cats nationwide snubbed the new 'tuna-strawberry' cans. "
        "At the recall presser the CEO ate one on camera—and goodwill came back first.",
    ),
    "v02": (
        "創業200年のぬか床から、いつもの香りが消えた。店はいったん閉まった。"
        "三日三晩手入れすると味が戻り、待ちわびた客がまた並び始めた。",
        "The usual scent vanished from a 200-year pickle bed, and the shop shut. "
        "Three days of care brought the flavor back—and the waiting regulars.",
    ),
    "v03": (
        "真冬、リスたちはエサ代のためドングリコインを売り続け、値段は二度底を打った。"
        "春になって余裕ができると、「売りすぎた」と気づいて買い戻した。",
        "In midwinter, squirrels sold Acorn Coin for food money until it bottomed twice. "
        "Come spring, with spare cash, they realized they'd sold too much—and bought it back.",
    ),
    "v04": (
        "売りは「火山が見える露天風呂」。その火山が噴火し、全便が欠航した。"
        "安全が確認されたあと、地元窯の溶岩ピザが話題になり、客はじわじわ戻った。",
        "The pitch was volcano-view baths—then the volcano erupted and flights stopped. "
        "Once cleared as safe, kiln-baked lava pizza buzzed, and visitors trickled back.",
    ),
    "v05": (
        "宇宙ステーション初のラーメン店。スープは浮き、麺は飛んで大失敗。"
        "ゼリー状に直すと大ヒットしたのに、温めると液体に戻る機能でまた騒ぎになった。",
        "First ramen shop on the station: broth floated, noodles fled. "
        "A jelly fix sold big—then 'melts when heated' made another mess in zero-g.",
    ),
    "v06": (
        "新型ロボ掃除機は、ネコに乗られると一歩も動けなくなった。"
        "そこで「ネコ乗せモード」を足すと、飼い主の動画つきでなぜか大ヒットした。",
        "The new robot vacuum froze the moment a cat sat on it. "
        "Add a ride-along mode, and owner videos somehow made it a hit.",
    ),
    "v07": (
        "当たり確率は0.3%のはずが、画面には3%と書いてあった。"
        "訂正のたびに売られたが、補償ガチャを配ると離れていた人が少しずつ戻った。",
        "The jackpot was 0.3%; the screen said 3%. "
        "Each correction sold off—until free compensation pulls nudged players back.",
    ),
    "v08": (
        "ミカンが予想外の大豊作で、冷凍庫も倉庫の前も段ボールだらけになった。"
        "町の「冷凍ミカン祭り」で在庫ははけた。利益まで、一緒に凍りついた。",
        "A bumper crop stuffed freezers and stacked boxes outside the warehouse. "
        "A town frozen-mandarin festival cleared the stock—and froze the profit too.",
    ),
    "v09": (
        "全国の自販機が一斉に「おつりを計算中」のまま止まった。"
        "原因は共通ソフトの更新バグ。修正が届くと、一台ずつ動き出した。",
        "Vending machines nationwide froze on 'calculating change.' "
        "A shared software update was to blame; patches woke them one by one.",
    ),
    "v10": (
        "「パスワードを忘れた」を押すと、銀行のほうもパスワードを忘れた。"
        "直った直後に送金がまた止まり、会社は紙の手紙で謝罪を送った。",
        "'Forgot password' made the bank forget its own password. "
        "Just after recovery, transfers died again—and the apology came by paper letter.",
    ),
    "v11": (
        "「完全な新味」と謳った缶は、またネコに不評だった。"
        "社長は会見で再試食し改良を約束したが、前回ほど話題にはならなかった。",
        "Another 'perfectly new' flavor—and cats disliked it again. "
        "The CEO retasted on camera and vowed a fix, but the buzz was quieter than last time.",
    ),
    "v12": (
        "かき混ぜすぎて元気になりすぎたぬか床は、夜中も泡を立て続けた。"
        "近所の苦情で営業時間を縮めると、ようやく落ち着きを取り戻した。",
        "Over-stirred and over-lively, the pickle bed bubbled all night. "
        "Shorter hours after neighbor complaints finally calmed it down.",
    ),
    "v13": (
        "復旧した自販機は、おつり確認に一人あたり約1分かかるようになっていた。"
        "短縮更新が届くと、並びはふつうの長さに戻った。",
        "After the fix, each machine spent about a minute checking change. "
        "A shorter update landed, and the lines went back to normal.",
    ),
    "v14": (
        "溶岩ピザで客が戻った島が、今度は火山観察ツアーを売り出した。"
        "肝心の当日は噴煙が見えず、期待外れのキャンセルが相次いだ。",
        "The lava-pizza island next sold volcano-watching tours. "
        "On the day, no plume—and disappointed cancellations piled up.",
    ),
    "z01": (
        "辛さ100倍ポテトは、半分が泣いて諦め、半分が泣きながらおかわりした。"
        "万人向けの控えめ版を出すと、激辛派にも初心者にも怒られた。",
        "Heat ×100 fries: half quit crying, half ordered more while crying. "
        "A milder 'for everyone' pack angered both the heat fans and the newbies.",
    ),
    "z02": (
        "入浴料の値上げを発表すると、名物カピバラが湯船から動かなくなった。"
        "値上げは撤回。かわりに出演料の交渉が、まだ湯の中で続いている。",
        "Announce a bath-fee hike, and the star capybaras refuse to leave the water. "
        "The hike was dropped; appearance-fee talks are still happening in the tub.",
    ),
    "z03": (
        "「業界最高精度」の天気AIは、晴れが三日続くと次は必ず雨と答える仕組みだった。"
        "たまたま当たりが続いたあと、大型イベントの晴れ予報の日に豪雨が落ちた。",
        "The 'top accuracy' weather AI always says rain after three sunny calls. "
        "A lucky streak ended when a sunny outdoor-event forecast brought a downpour.",
    ),
    "z04": (
        "配信の最後に「大事なお知らせがあります……来週」。考察は一週間で300本を超えた。"
        "発表は新衣装。安心したファンが記念グッズを買い、会社も一緒に跳ねた。",
        "End of stream: 'Big news… next week.' Fans posted over 300 theories. "
        "It was a new outfit. Relieved fans bought merch—and the company jumped with them.",
    ),
    "z05": (
        "中身がほとんど見えない新作。大物配信者が体験版を「神ゲー」と絶賛した。"
        "翌日、別の配信者が同じ体験版を「期待したほどではない」と酷評した。",
        "A mostly sealed new game. A star streamer's demo call: 'divine.' "
        "Next day, another played the same demo: 'not what I hoped.'",
    ),
    "z06": (
        "最短か、氷の上を滑るか。投票は滑るルートに決まった。"
        "試験運用では手紙が雪原へ飛び遅延。防水と目印を足すと、前より速く届いた。",
        "Shortest path or ice slide? Votes chose the slide. "
        "Test mail flew into the snow and lagged—then waterproofing and lane marks beat the old speed.",
    ),
    "z07": (
        "1回目の試乗会は、3メートル浮いて2秒後に池へ落ちた。"
        "2回目は5メートル。少しは伸びたが、着陸の仕方はまだ誰も知らない。",
        "First hop: three meters up, pond in two seconds. "
        "Second: five meters. Better height—landing still unsolved.",
    ),
    "z08": (
        "「値上げします」「やっぱりしません」「タコを少し小さくします」。客は毎回たこ焼きを割った。"
        "店は切り方と焼き方を見直し、値段を据え置いたまま原価を抑える方法を見つけた。",
        "'Prices up.' 'Never mind.' 'Slightly smaller octopus.' Customers split every ball. "
        "The shop changed the cut and cook—and kept the price while trimming cost.",
    ),
    "z09": (
        "「雨でも安心」が売りの屋外イベント保険。再建策の発表で、いったん人気が戻った。"
        "ただし新規契約の雨天補償は半額。保険料は同じだったので、主催者の不満が広がった。",
        "Outdoor event cover sold as 'safe even in rain.' A turnaround plan briefly restored buzz. "
        "New policies paid half on rainy days at the same premium—organizers were not amused.",
    ),
    "z10": (
        "ペット用品の広告に出ていた社長のインコが逃げ出した。"
        "「見つけた」投稿のたびに株が上がり、別の鳥だと分かるたびに元へ戻った。",
        "The CEO's ad-star parrot escaped. "
        "Each 'found it' post lifted the stock; each wrong bird put it right back.",
    ),
    "z11": (
        "また「大事なお知らせ」。ファンは今度も新衣装だと思った。"
        "発表は看板Vtuberの卒業。チャットは長く流れ、関連の売上見通しは揺れた。",
        "Another 'big news' tease. Fans pictured another costume. "
        "It was the star VTuber's graduation. Chat kept scrolling; merch and stream outlook wobbled.",
    ),
    "z12": (
        "出演料を払うと約束すると、カピバラはまた湯に戻った。"
        "ただし湯量と営業時間の制限も同時に出され、株は小さく揺れた。",
        "Promise appearance fees, and the capybaras return to the bath. "
        "Water and hours limits came with the deal—the stock trembled a little.",
    ),
    "z13": (
        "社長のインコが「捕まった」と報じられ、株は一気に跳ねた。"
        "連れて帰った鳥は羽の色が違う別のインコで、値段はまた元の位置へ戻った。",
        "Headline: the CEO's parrot was caught. The stock jumped. "
        "The bird that came home wore the wrong colors—and the price went home too.",
    ),
    "z14": (
        "「さらに辛くした」と大宣伝で出た新袋。"
        "開けてみたら中身は前と同じ100倍で、新しかったのはシールだけだった。",
        "Sold with a 'even hotter' splash. "
        "Inside: the same ×100. Only the sticker was new.",
    ),
    "b01": (
        "金箔を巻いただけのたい焼きに、最長3時間の行列ができた。"
        "客の多くは写真目的。SNSに写真があふれると、珍しさも行列も消えた。",
        "Gold-leaf taiyaki drew lines up to three hours. "
        "Most came for photos. Once feeds were full, novelty—and the line—vanished.",
    ),
    "b02": (
        "なんでも10分で届く通販。利用者も株価も伸びていた。"
        "決算の日、「1件届けるたびに赤字」と分かり、伸びるほど損する話になった。",
        "Anything in ten minutes. Users and the stock both grew. "
        "Earnings day: every delivery lost money. Growth meant deeper red.",
    ),
    "b03": (
        "前の冬に売って損したリスたちは、今度こそと冬ごもり前にコインを買い集めた。"
        "人間もまねて盛り上がったが、巣にこもると買い手が消えてお祭りは終わった。",
        "Squirrels burned by last winter's sales hoarded coin before hibernation. "
        "Humans copied the party—until the nests closed and buyers vanished.",
    ),
    "b04": (
        "かむと「キュッ」と鳴るグミが品切れ続き。会社は工場を一気に3つ増設した。"
        "新工場が動き出すころにはブームが終わり、倉庫に鳴らないグミが山積みになった。",
        "Squeaky gummies kept selling out, so three new plants went up at once. "
        "By the time they ran, the fad was over—and silent gummies filled the warehouses.",
    ),
    "b05": (
        "初競りで1玉100万円がついたメロン。"
        "落札した社長が「普通のメロンの味でした」と正直に言うと、話題はそこで終わった。",
        "Opening auction: one melon for a million yen. "
        "The buyer-CEO said flatly, 'It tasted like ordinary melon'—and the story ended there.",
    ),
    "b06": (
        "SNSで「固すぎてうまい」とバズったプリン屋。"
        "翌週、隣にもっと固い競合店ができ、行列はそちらへ移った。",
        "Buzz: pudding too firm, somehow perfect. "
        "Next week a firmer rival opened next door—and the line moved with it.",
    ),
    "b07": (
        "目標は「すべての駅に1店舗」。借金を重ねて出店を続けた。"
        "ブームが去り返済日を迎えた朝、全店のシャッターに同じ貼り紙がかかった。",
        "Goal: one shop in every station. Debt funded the expansion. "
        "When the boom faded and repayment day came, every shutter wore the same notice.",
    ),
    "b08": (
        "「丸い家は運気が上がる」と評判になり、ドーム住宅の注文が急増した。"
        "銀行のローン審査が厳しくなると、モデルハウスは満員なのに契約だけが減っていった。",
        "Rumor said round homes bring luck, and dome orders surged. "
        "When mortgages tightened, show homes stayed crowded—signed contracts did not.",
    ),
    "b09": (
        "キックボード人気ランキング1位。会社は記念キャンペーンを打ち、投資家も期待で買った。"
        "順位は一週間限定の投票だった。全社員通勤を始めた翌日、半分が筋肉痛で休んだ。",
        "Kickboard poll #1. Promo launched; investors piled in. "
        "It was a one-week online vote. Staff commute day two: half called in sore.",
    ),
    "b10": (
        "「飲むと10歳若返る」と宣伝していた健康茶。"
        "成分を調べると特別な根拠はなく、中身はほぼ麦茶だった。",
        "Marketed as tea that 'takes ten years off.' "
        "Lab work found no special proof—mostly barley tea.",
    ),
    "b11": (
        "ネコ乗せモードは大ヒットし、公園でも見かけるようになった。"
        "条例で禁止されると売れ行きは落ち、例外申請の書類だけが残った。",
        "Cat-ride mode was a hit—even in parks. "
        "Banned by ordinance, sales fell; only exemption paperwork remained.",
    ),
    "b12": (
        "鳴るグミのブームが終わり、会社は在庫を「学習用の無音グミ」として売り直した。"
        "図書館では予想外に好評。ただし糖分は工場の想定よりずっと多かった。",
        "When the squeak fad died, leftover stock became 'silent study gummies.' "
        "Libraries loved them. Sugar levels were far above the plant's plan.",
    ),
    "e01": (
        "2周目では全キャラが逆立ちで歩き始め、「哲学かバグか」で動画が大バズりした。"
        "公式が不具合と直すと、話題の逆立ちが消え、配信も売上も一気に冷えた。",
        "New Game+: everyone walked on their hands. Philosophy-or-bug videos exploded. "
        "Official patched it as a bug—the gimmick vanished, and streams and sales went cold.",
    ),
    "e02": (
        "補償を半額にして立て直した屋外イベント保険。"
        "予想外の地域まで豪雨が広がり、半額でも支払いが契約件数を上回ってしまった。",
        "Outdoor cover rebuilt on half rainy-day payouts. "
        "Rain spread farther than planned—even half the claims outran the contracts.",
    ),
    "e03": (
        "時速20キロののんびり鉄道は、景色目当てで乗客を増やしていた。"
        "「来月から運賃3倍」と発表した一文で、乗客も株もいっせいに逃げた。",
        "A 20 km/h scenic railway kept filling cars. "
        "One announcement—'fares triple next month'—emptied both trains and the stock.",
    ),
    "e04": (
        "暖冬で、倉庫に積んだ雪だるまが全部溶けかけていた。"
        "最後の週に寒波が来ると、注文が一気に殺到した。",
        "A warm winter nearly melted every warehouse snowman. "
        "A late cold snap brought orders in a rush.",
    ),
    "e05": (
        "在庫が減って冷凍ミカンの値段が戻ると、会社は契約を増やして安心した。"
        "翌年も予想外の豊作で、倉庫の前にまた箱が積み上がった。",
        "Stock thinned, prices recovered, and the company signed more farm deals. "
        "Next year another bumper crop stacked boxes outside the warehouse again.",
    ),
    "e06": (
        "試作ロボが工場から歩いて出ていき、戻ってこなかった。会社は倒産寸前だった。"
        "山奥の災害現場で物資を運んでいると分かり、銀行は追加融資を決めた。",
        "The prototype walked out of the plant and never came back. Bankruptcy loomed. "
        "It was found hauling relief in the mountains—and the bank added credit.",
    ),
    "e07": (
        "人口80人の島が独立を宣言し、島のコインは乱高下した。"
        "「コイン10枚でサザエ一皿」と交換が決まると、初めてちゃんとした使い道ができた。",
        "An island of eighty declared independence; its coin thrashed. "
        "Ten coins for a plate of turban shell—suddenly the coin had a real use.",
    ),
    "e08": (
        "散歩もトイレもいらないAIペットが大ブームになった。"
        "半年後、飼い主の多くは電源を切ったまま忘れ、追加課金も止まった。",
        "AI pets that need no walks and no litter took over. "
        "Half a year later most sat powered off and forgotten—and the upsells dried up.",
    ),
    "e09": (
        "隣の競合に客を取られたプリン屋が、閉店セールを出した。"
        "開店のときより長い行列のあと、最終日を過ぎると店は空になった。",
        "Beaten by the neighbor, the pudding shop ran a closing sale. "
        "A longer line than opening day—then, after the last day, an empty shop.",
    ),
    "e10": (
        "駅前の株価ボードが数分間、全銘柄を「1円」と表示した。製造元の株も巻き添えで売られた。"
        "表示ソフトの不具合と判明したあと、「珍表示」を求める注文がむしろ増えた。",
        "The station board priced every ticker at ¥1 for a few minutes—maker stock got hit too. "
        "It was a display bug. Afterward, orders for the odd lookout actually rose.",
    ),
    "e11": (
        "金箔の仕入れが止まり、普通のたい焼きを同じ値段で売り続けた。"
        "写真映えがなくなると、並ぶ理由もなくなり行列は消えた。",
        "Gold leaf ran out, so plain taiyaki stayed at the gold price. "
        "Without the shine, nobody lined up.",
    ),
    "e12": (
        "赤字を埋めるため、倉庫の商品を9割引きで放出した。"
        "荷物はどんどん動いたが利益は出ず、会社の株も安売りに付き合った。",
        "To plug the red, the warehouse went ninety percent off. "
        "Goods moved fast; profit did not—and the stock joined the clearance sale.",
    ),
    "e13": (
        "お茶はこぼす、ドアには挟まる、欠点だらけのロボ執事。"
        "「不器用でかわいい」で予約が殺到したが、修理が追いつかず納期延期が出た。",
        "A butler-bot that spills tea and pinches doors. "
        "'Clumsy-cute' sold out the waitlist—then repairs lagged and ship dates slipped.",
    ),
    "e14": (
        "冬の入り口に始まったドングリコイン祭りは、大晦日に転機を迎えた。"
        "「もう十分」と考えたリスたちが一斉に売り、祭りの前よりは高いまま急落した。",
        "The early-winter Acorn Coin party turned on New Year's Eve. "
        "Squirrels sold together saying 'enough'—a plunge, still above where the party began.",
    ),
    "e15": (
        "限定版は予約開始から数分で完売。中身はまだ誰も知らない。"
        "特典は増産しないと発表され、予約権の転売まで値が跳ねた。",
        "The limited edition sold out minutes after preorders opened. Nobody had played it. "
        "Extras would not be reprinted—and even reservation rights spiked.",
    ),
}


def main() -> None:
    with META.open(encoding="utf-8-sig", newline="") as f:
        reader = csv.DictReader(f)
        fieldnames = list(reader.fieldnames or [])
        rows = list(reader)

    missing = sorted({r["stage_id"] for r in rows} - set(FLAVOR))
    extra = sorted(set(FLAVOR) - {r["stage_id"] for r in rows})
    if missing or extra:
        raise SystemExit(f"missing={missing} extra={extra}")

    lens: list[tuple[str, int]] = []
    for row in rows:
        sid = row["stage_id"]
        jp, en = FLAVOR[sid]
        row["flavor_text"] = jp
        row["flavor_text_en"] = en
        lens.append((sid, len(jp)))

    with META.open("w", encoding="utf-8", newline="") as f:
        w = csv.DictWriter(f, fieldnames=fieldnames, lineterminator="\n")
        w.writeheader()
        w.writerows(rows)

    lens.sort(key=lambda x: -x[1])
    avg = sum(n for _, n in lens) // len(lens)
    print(f"rewrote {len(FLAVOR)} flavors avg={avg} max={lens[0]} min={lens[-1]}")


if __name__ == "__main__":
    main()
