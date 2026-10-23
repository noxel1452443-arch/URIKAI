"""指摘対応: 物語因果・チャート整合のメタ／キーフレーム修正。"""
from __future__ import annotations

import csv
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
META = ROOT / "content" / "stages_meta.csv"
KF = ROOT / "content" / "keyframes.csv"

# stage_id -> field updates (partial)
PATCH: dict[str, dict[str, str]] = {
    # --- ドングリ: 時系列を冬→春／次の冬／大晦日で明示 ---
    "v03": {
        "brand_name": "ドングリコイン、冬の売りから春の買い",
        "brand_name_en": "Acorn Coin: winter sell, spring buy",
        "anchor_when": "春にリスが買い戻す",
        "anchor_when_en": "Spring buyback by squirrels",
        "flavor_text": "真冬、森のリスはエサ代のためドングリコインを売り続け、値段は二度底を打った。春になって余ったドングリを換金し、同じコインを買い戻した。",
        "flavor_text_en": "In midwinter, squirrels sold Acorn Coin for food money and the price bottomed twice. In spring they cashed leftover acorns and bought the same coin back.",
        "history_comment": "売ったあとに買うなら、季節の区切りを見る。",
        "history_comment_en": "If they sell then buy, watch the season change.",
    },
    "b03": {
        "brand_name": "ドングリコイン、次の冬のご祝儀相場",
        "brand_name_en": "Acorn Coin's next-winter frenzy",
        "anchor_when": "リスが巣ごもり開始",
        "anchor_when_en": "Squirrels start hibernating",
        "flavor_text": "翌年の冬の入り口。リスが冬ごもり前にコインを買い集め、人間もまねて高値がついた。巣にこもると買う人が消え、お祭りは終わった。",
        "flavor_text_en": "Early the next winter, squirrels hoarded coin before hibernating and humans copied them. Once they holed up, buyers vanished and the party ended.",
        "history_comment": "買う側がいなくなった瞬間が、天井。",
        "history_comment_en": "The top is when the buyers disappear.",
    },
    "e14": {
        "brand_name": "ドングリコイン、大晦日の一斉売り",
        "brand_name_en": "Acorn Coin: New Year's Eve dump",
        "anchor_when": "大晦日にリスが一斉売却",
        "anchor_when_en": "Squirrels dump on New Year's Eve",
        "flavor_text": "同じ冬の大晦日。「もう十分」とリスが一斉に売ると値段は急落した。それでも、冬の入り口のお祭り前よりは高かった。",
        "flavor_text_en": "Same winter, New Year's Eve. Squirrels sold together saying 'enough' and the price plunged—still above where the early-winter party began.",
        "history_comment": "お祭り後の売りは、そろって来ることが多い。",
        "history_comment_en": "After a party, selling often arrives together.",
    },
    # --- 火山島 ep2: 期待→失望の形に合わせる（メタ）。KFは z09 からコピー ---
    "v14": {
        "brand_name": "火山島リゾート、噴火予定が外れる",
        "brand_name_en": "Volcano resort: eruption schedule misses",
        "scenario_type": "WHIPSAW",
        "macro_shape": "N_RALLY",
        "pivot_timing": "MID",
        "trick": "BULL_TRAP",
        "anchor_when": "予定日に噴火せず",
        "anchor_when_en": "No eruption on schedule",
        "anchor_band_start": "56",
        "anchor_band_end": "56",
        "anchor_label": "期待のあと急落",
        "anchor_label_en": "Hope, then a plunge",
        "flavor_text": "溶岩ピザで客が戻った島は、「噴火が見える日」をカレンダーに載せた。予定日に噴火せず、予約客のキャンセルが続き、株は期待のあと下がった。",
        "flavor_text_en": "After lava pizza brought guests back, the island put 'eruption viewing days' on a calendar. No eruption on the date, cancellations piled up, and the stock fell after the hype.",
        "history_comment": "予定を売りにすると、外れがそのまま材料になる。",
        "history_comment_en": "Sell a schedule, and a miss becomes the headline.",
    },
    # --- 冷凍ミカン ep2: 戻り→再崩壊を文でも明示 ---
    "e05": {
        "flavor_text": "在庫が減って値段が戻った翌年、また裏山が豊作になった。倉庫の前にミカンの箱が積み上がり、値段は再び崩れた。",
        "flavor_text_en": "Prices recovered as stock cleared—then the hills had another bumper crop. Boxes stacked outside the warehouse again, and prices broke once more.",
    },
    # --- 自販機 ---
    "v09": {
        "flavor_text": "全国の自販機が「おつりを考え中」のまま止まった。原因は、1台のボタンを全部押した小学生。復旧すると、止まっていた売上もまとめて戻った。",
        "flavor_text_en": "Vending machines nationwide froze on 'calculating change'—one kid had mashed every button on one unit. When they restarted, the lost sales came back at once.",
    },
    "v13": {
        "flavor_text": "復旧後、おつり確認が丁寧すぎて1人あたり1分かかった。駅前に行列ができて不満が出たので、確認を短縮する更新を入れると普通に売れた。",
        "flavor_text_en": "After the fix, change checks took a full minute per person and station lines formed. A patch that shortened the check restored normal sales.",
    },
    # --- 激辛: ep2は「同じ辛さのシール替え」で納得感 ---
    "z01": {
        "flavor_text": "「辛さ100倍」ポテト。半分は泣いて諦め、半分は泣きながらおかわりした。控えめ版は両方から怒られ、会社はすぐ元の辛さに戻した。",
        "flavor_text_en": "'100x spicy' chips: half quit crying, half cried and asked for seconds. A milder bag angered both sides, so the company restored the original heat.",
    },
    "z14": {
        "brand_name": "激辛ポテト、限定パッケージの中身",
        "brand_name_en": "Hot chips: what's in the limited bag",
        "anchor_when": "中身が同じと発覚",
        "anchor_when_en": "Same chips inside revealed",
        "flavor_text": "「さらに辛い限定」と売り出したら株が跳ねた。開けてみると中身は前と同じ100倍で、シールだけ新しかった。返品が続き、株も戻した。",
        "flavor_text_en": "A 'even hotter limited' bag sent the stock up. Inside it was the same 100x chips—only the sticker was new. Returns followed, and the stock gave it back.",
        "history_comment": "名前だけ新しい材料は、長く持たない。",
        "history_comment_en": "A new name on old stock doesn't hold.",
    },
    # --- 保険: 「晴れ予報の日だけ」をやめ、分かりやすい失望に ---
    "z09": {
        "brand_name": "屋外イベント保険、立て直しの中身",
        "brand_name_en": "Outdoor event insurance: rescue details",
        "anchor_when": "補償半額と判明",
        "anchor_when_en": "Half payout revealed",
        "flavor_text": "「雨でも安心」で売れた屋外イベント保険。立て直し策の発表では、雨の日の保険金が半額になると分かった。期待で上がっていた株が急落した。",
        "flavor_text_en": "Outdoor event cover sold as 'safe even in rain.' The rescue plan cut rainy-day payouts in half. The stock, pumped on hope, plunged on the details.",
        "history_comment": "立て直しの中身が出た瞬間が、天井になりやすい。",
        "history_comment_en": "The top often arrives when rescue details land.",
    },
    "e02": {
        "brand_name": "屋外イベント保険、半額でも払いきれず",
        "brand_name_en": "Outdoor insurance: half payout still breaks them",
        "anchor_when": "ゲリラ豪雨で請求殺到",
        "anchor_when_en": "Storm claims flood in",
        "flavor_text": "半額でも契約は残っていた。予報外のゲリラ豪雨が続き請求が殺到し、会社はまた会見で頭を下げた。株は立て直し後の安値を更新した。",
        "flavor_text_en": "Even at half payout, policies remained. Surprise storm claims piled up and the company apologized again. The stock made a new low after the 'rescue.'",
        "history_comment": "一度失望したあとの追撃は、容赦ない。",
        "history_comment_en": "A second hit after disappointment is merciless.",
    },
    # --- インコ: 噂↔株の対応を明示 ---
    "z10": {
        "flavor_text": "社長のインコが逃げた。SNSに「見た」と出るたびに株が上がり、「別人でした」と訂正が出るたびに下がった。中身のない噂でも、株は動いた。",
        "flavor_text_en": "The CEO's parrot escaped. Every 'I saw it' post lifted the stock; every 'wrong bird' correction dropped it. Empty rumors still moved the price.",
    },
    "z13": {
        "brand_name": "迷子のインコ、誤認捕獲でまた動く",
        "brand_name_en": "Lost parrot: wrong bird moves the stock again",
        "anchor_when": "捕獲鳥が別人と判明",
        "anchor_when_en": "Caught bird is the wrong one",
        "flavor_text": "「捕まえた」速報で株が跳ねた。連れて帰った鳥は羽の色が違い、訂正が出ると株は元の位置へ戻った。前回と同じ上がって下がるだけだった。",
        "flavor_text_en": "A 'we caught it' flash sent the stock flying. The bird's colors were wrong; the correction sent the price back. Same up-then-down as before.",
        "history_comment": "誤報でも一度動けば、訂正で戻る。",
        "history_comment_en": "Even a false alarm moves—and the correction moves it back.",
    },
    # --- たい焼き: 紙巻きではなく「箔なし同額」 ---
    "e11": {
        "brand_name": "金のたい焼き、箔なしでも同額",
        "brand_name_en": "Golden taiyaki: same price, no gold",
        "anchor_when": "金箔なしを同額で販売",
        "anchor_when_en": "Sold without gold at same price",
        "flavor_text": "金箔の仕入れが止まり、普通のたい焼きを同じ値段で売った。写真映えしなくなり、並ぶ理由がなくなって行列は消えた。",
        "flavor_text_en": "Gold leaf ran out, so they sold plain taiyaki at the same price. No photo bait, no reason to wait—the line vanished.",
        "history_comment": "見た目の材料が消えると、熱狂も消える。",
        "history_comment_en": "Lose the look that sold it, and the frenzy goes too.",
    },
    # --- 通販の星: タイトルと因果を明確に ---
    "b02": {
        "brand_name": "10分配送、売れるほど大赤字",
        "brand_name_en": "10-minute delivery: more sales, more losses",
        "anchor_when": "決算で赤字が発覚",
        "anchor_when_en": "Loss revealed on earnings",
        "flavor_text": "なんでも10分で届く通販。利用者と株価は伸びていたが、決算で「1件届けるたびに赤字」と分かった。伸びるほど損する話に、株は急落した。",
        "flavor_text_en": "Anything in 10 minutes. Users and the stock grew—until earnings showed each delivery lost money. Growth that deepens losses crashed the shares.",
        "history_comment": "売上が増えるほど赤字なら、伸びは危険信号。",
        "history_comment_en": "If more sales mean more losses, growth is a warning.",
    },
    "e12": {
        "brand_name": "10分配送、在庫の投げ売り",
        "brand_name_en": "10-minute delivery: inventory fire sale",
        "anchor_when": "倉庫在庫を9割引き放出",
        "anchor_when_en": "Warehouse dumped at 90% off",
        "flavor_text": "赤字を埋めるため、倉庫の商品を9割引きで放出した。荷物は動いたが利益は出ず、会社の株も安売りに付き合った。",
        "flavor_text_en": "To plug losses they dumped warehouse goods at 90% off. Packages moved, profits did not—and the stock joined the fire sale.",
        "history_comment": "穴埋めの安売りは、株の安売りでもある。",
        "history_comment_en": "A fire sale to plug a hole is often a fire sale of the stock.",
    },
    # --- プリン: 隣＝競合。ep2は本店の閉店 ---
    "b06": {
        "flavor_text": "SNSで「固すぎてうまい」とバズったプリン屋。翌週、競合がすぐ隣にもっと固いプリン屋を出し、行列はそちらへ移った。",
        "flavor_text_en": "A pudding shop went viral for being 'so firm it's great.' Next week a rival opened next door even firmer, and the line moved there.",
        "anchor_when": "隣に競合の固い店",
        "anchor_when_en": "Firmer rival opens next door",
    },
    "e09": {
        "brand_name": "プリン屋本店、閉店セール",
        "brand_name_en": "Pudding shop: closing sale",
        "anchor_when": "本店が閉店セール",
        "anchor_when_en": "Main shop closing sale",
        "flavor_text": "隣の競合に客を取られた本店は、閉店セールを出した。開店のときより長い行列ができたが、最終日が過ぎると店は空になった。",
        "flavor_text_en": "Losing customers to the rival next door, the original shop held a closing sale. The line beat opening day—then the last day ended and the shop was empty.",
        "history_comment": "最後のにぎわいは、終わりの合図。",
        "history_comment_en": "The final rush is the signal that it's over.",
    },
    # --- ドーム住宅: 表現を明確に ---
    "b08": {
        "brand_name": "ドーム住宅、噂で値上がりが止まる",
        "brand_name_en": "Dome homes: rumor-driven rise stalls",
        "anchor_when": "ローン審査が厳格化",
        "anchor_when_en": "Mortgage rules tighten",
        "flavor_text": "「丸い家は運気が上がる」という噂で、ドーム住宅の売値が毎月上がった。銀行のローン審査が厳しくなると買い手が減り、値上がりは止まった。",
        "flavor_text_en": "A rumor that round homes bring luck pushed dome-home asking prices up each month. When banks tightened mortgages, buyers thinned and the rise stopped.",
        "history_comment": "噂で上がった値段は、お金の蛇口が閉まると止まる。",
        "history_comment_en": "Prices lifted by rumor stall when the money tap closes.",
    },
}

# v14 のキーフレームを z09（N_RALLY: 期待→失望）からコピー
KF_COPY = {
    "v14": "z09",
}


def main() -> None:
    with META.open(encoding="utf-8", newline="") as f:
        rows = list(csv.DictReader(f))
    fields = list(rows[0].keys())

    for r in rows:
        sid = r["stage_id"]
        if sid in PATCH:
            r.update(PATCH[sid])
            # anchor length check hint
            if len(r["anchor_when"]) > 14:
                raise SystemExit(f"{sid}: anchor_when too long ({len(r['anchor_when'])}): {r['anchor_when']}")

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

    # 既存 v14 行を削除してからコピー
    keep = [r for r in kf_rows if r["stage_id"] not in KF_COPY]
    for dest, src in KF_COPY.items():
        for r in by_src[src]:
            nr = dict(r)
            nr["stage_id"] = dest
            keep.append(nr)

    with KF.open("w", encoding="utf-8", newline="") as f:
        w = csv.DictWriter(f, fieldnames=kf_fields, lineterminator="\n")
        w.writeheader()
        w.writerows(keep)

    print(f"patched meta={len(PATCH)} kf_copy={KF_COPY}")


if __name__ == "__main__":
    main()
