"""物語用 saga_ep 確認・フレーバー推敲・続き4本追加。一度きりの編集ヘルパー。"""
from __future__ import annotations

import csv
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
META = ROOT / "content" / "stages_meta.csv"
KF = ROOT / "content" / "keyframes.csv"

# フレーバー推敲（未指定は現状維持）
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
    "v08": (
        "豊作で冷凍庫が足りず、倉庫のドアが内側から開いた。値段はほぼタダ。町の「冷凍ミカン祭り」で山は一夜で消えた。",
        "Too big a harvest, not enough freezers—the warehouse door burst open and prices hit near zero. A town festival cleared the mountain overnight.",
    ),
    "v09": (
        "全国の自販機が「おつりを考え中」で止まった。原因は、1台のボタンを全部押した小学生。復旧の瞬間、止まっていた売上もまとめて戻った。",
        "Every vending machine froze on 'calculating change'—one kid had mashed every button on one unit. When they restarted, the lost sales came back at once.",
    ),
    "z05": (
        "中身ほぼ非公開の新作。体験版を遊んだ大物が「神ゲー」と言った翌日、別の配信者が「クソゲー」と言った。",
        "A barely revealed new game. A star streamer called the demo a masterpiece. The next day another called it trash.",
    ),
    "z09": (
        "「雨でも安心」の屋外イベント保険。立て直しプランの中身は、「晴れ予報の日だけ引き受ける」だった。",
        "Outdoor event insurance sold as 'covered even in rain.' The rescue plan: only cover days the forecast says sunny.",
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
    "b06": (
        "SNSで「固すぎて逆にうまい」とバズったプリン屋。翌週、隣にもっと固い店ができた。",
        "A pudding shop went viral for being 'so firm it's great.' The next week, a firmer shop opened next door.",
    ),
    "e01": (
        "2周目では全キャラが逆立ちで歩く。「深い」派と「バグ」派が騒いだ末、公式が「バグです」と発表した。",
        "In New Game+, every character walks on their hands. Deep vs bug arguments ended when the developer said: 'It's a bug.'",
    ),
    "e02": (
        "晴れ予報の日だけ引き受けたはずが、予報がことごとく外れ、ゲリラ豪雨で野外イベントが全滅。2度目の会見は傘の下だった。",
        "They only covered sunny-forecast days. Then every forecast failed and outdoor events drowned. The second presser was under umbrellas.",
    ),
    "e05": (
        "在庫がやっと消えたと思ったら、翌年も裏山が大豊作。倉庫の前に、またミカンの山ができた。",
        "Just as stock cleared, the hills had another bumper crop. A new mountain of mandarins rose outside the warehouse.",
    ),
    "e09": (
        "隣の固い店に負け、2号店は閉店セール。開店日より長い行列ができたが、最終日が過ぎると何も残らなかった。",
        "Beaten by the firmer neighbor, shop #2 held a closing sale. The line beat opening day—then the last day ended, and nothing was left.",
    ),
    "e11": (
        "金箔が足りなくなり、金色の紙を巻いて売り始めた。気づいた客から順に、来なくなった。",
        "Gold leaf ran short, so they wrapped taiyaki in gold paper. Customers stopped coming as they noticed, one by one.",
    ),
    "e12": (
        "赤字穴埋めのため、倉庫在庫を9割引きで放出。会社の株も、同じくらい値引きされた。",
        "To plug losses they dumped warehouse stock at 90% off. The company's stock got about the same discount.",
    ),
    "e14": (
        "年越し直前、リスたちが「もう十分」と一斉に売った。それでも、お祭り前よりはまだ高かった。",
        "Right before New Year the squirrels decided they'd had enough and sold together. It still sat above where the party began.",
    ),
    "e15": (
        "限定版が予約だけで全国品切れ。発売前で中身は誰も知らないのに、予約の権利が高値で転売された。",
        "The limited edition sold out on preorders alone. It isn't out and nobody knows what's inside—but preorder rights sell high.",
    ),
}

# 新規続き（ep2）: (stage_id, copy_kf_from, meta_dict without stage_id)
NEW = [
    (
        "v11",
        "v01",
        {
            "brand_name": "ネコ缶、リニューアルでまた回収",
            "brand_name_en": "Cat food recall, again after relaunch",
            "period": "食品",
            "period_en": "Food",
            "market_kind": "STOCK",
            "instrument_name": "にゃんフーズHD",
            "instrument_name_en": "Nyan Foods HD",
            "base_price": "2450",
            "calendar_start": "食品",
            "calendar_start_en": "Food",
            "calendar_end": "食品",
            "calendar_end_en": "Food",
            "anchor_when": "社長がまた試食",
            "anchor_when_en": "CEO tastes it again",
            "anchor_band_start": "30",
            "anchor_band_end": "30",
            "scenario_type": "DROP_REBOUND",
            "macro_shape": "V_RECOVER",
            "pivot_timing": "MID",
            "trick": "",
            "flavor_text": "好感度が戻ったあと出した「安全な新味」も、ネコには不評だった。社長はまた会見で一缶食べたが、今度は誰も泣かなかった。",
            "flavor_text_en": "After goodwill returned, the 'safe new flavor' flopped with cats too. The CEO ate another can on camera—this time nobody cried.",
            "anchor_label": "底から戻るが弱く",
            "anchor_label_en": "Weak bounce from the bottom",
            "history_comment": "一度戻ったあとの再下落は、戻りが弱い。",
            "history_comment_en": "A second slide after a rebound often comes back weaker.",
            "seed": "111",
            "saga_ep": "2",
            "tier": "",
        },
    ),
    (
        "v12",
        "v02",
        {
            "brand_name": "ぬか床、元気すぎて近所迷惑",
            "brand_name_en": "Pickle bed too lively for neighbors",
            "period": "食品",
            "period_en": "Food",
            "market_kind": "STOCK",
            "instrument_name": "ぬか福本舗",
            "instrument_name_en": "Nukafuku Honpo",
            "base_price": "1280",
            "calendar_start": "食品",
            "calendar_start_en": "Food",
            "calendar_end": "食品",
            "calendar_end_en": "Food",
            "anchor_when": "営業時間を短縮",
            "anchor_when_en": "Hours cut short",
            "anchor_band_start": "30",
            "anchor_band_end": "30",
            "scenario_type": "DROP_REBOUND",
            "macro_shape": "V_RECOVER",
            "pivot_timing": "MID",
            "trick": "",
            "flavor_text": "かき混ぜすぎて元気になったぬか床は、夜中も泡を立てた。近所の苦情で営業時間を縮めると、落ち着きを取り戻した。",
            "flavor_text_en": "The over-stirred pickle bed bubbled all night. Neighbors complained, hours were cut, and it finally calmed down.",
            "anchor_label": "騒ぎのあと安定",
            "anchor_label_en": "Settles after the fuss",
            "history_comment": "元気すぎも、調整すれば戻る。",
            "history_comment_en": "Even 'too lively' can settle with a tweak.",
            "seed": "112",
            "saga_ep": "2",
            "tier": "",
        },
    ),
    (
        "v13",
        "v09",
        {
            "brand_name": "自販機、おつりを考えすぎる",
            "brand_name_en": "Vending machines overthink change",
            "period": "家電",
            "period_en": "Gadgets",
            "market_kind": "STOCK",
            "instrument_name": "街角ベンディング",
            "instrument_name_en": "Machikado Vending",
            "base_price": "1920",
            "calendar_start": "家電",
            "calendar_start_en": "Gadgets",
            "calendar_end": "家電",
            "calendar_end_en": "Gadgets",
            "anchor_when": "考え中を短縮更新",
            "anchor_when_en": "Faster 'thinking' update",
            "anchor_band_start": "10",
            "anchor_band_end": "10",
            "scenario_type": "DROP_REBOUND",
            "macro_shape": "EARLY_SHOCK",
            "pivot_timing": "EARLY",
            "trick": "",
            "flavor_text": "復旧のあと、今度はおつりの計算が丁寧すぎて行列ができた。ソフト更新で「考え中」が短くなると、また普通に売れた。",
            "flavor_text_en": "After the fix, change calculation got too careful and lines formed. A patch shortened 'thinking,' and sales went normal again.",
            "anchor_label": "止まったあと戻る",
            "anchor_label_en": "Stops, then returns",
            "history_comment": "直しすぎも、また直せる。",
            "history_comment_en": "Over-fixing can be fixed again.",
            "seed": "113",
            "saga_ep": "2",
            "tier": "",
        },
    ),
    (
        "z11",
        "z04",
        {
            "brand_name": "人気Vtuber、本当の大事なお知らせ",
            "brand_name_en": "Top VTuber: real big news this time",
            "period": "芸能",
            "period_en": "Entertainment",
            "market_kind": "STOCK",
            "instrument_name": "ネオライブ・プロ",
            "instrument_name_en": "NeoLive Pro",
            "base_price": "3850",
            "calendar_start": "芸能",
            "calendar_start_en": "Entertainment",
            "calendar_end": "芸能",
            "calendar_end_en": "Entertainment",
            "anchor_when": "卒業配信を発表",
            "anchor_when_en": "Graduation stream announced",
            "anchor_band_start": "35",
            "anchor_band_end": "35",
            "scenario_type": "WHIPSAW",
            "macro_shape": "RANGE",
            "pivot_timing": "EARLY",
            "trick": "",
            "flavor_text": "また「大事なお知らせ」と言ったので、みんなが新衣装だと思った。発表は卒業だった。考察は少なかったが、チャットは長かった。",
            "flavor_text_en": "Another 'big news' tease—fans assumed a new outfit. It was a graduation. Fewer theories, longer chat.",
            "anchor_label": "発表で動く",
            "anchor_label_en": "Moves on the reveal",
            "history_comment": "同じ言い回しでも、中身が違えば動きは違う。",
            "history_comment_en": "Same wording, different news—different move.",
            "seed": "211",
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
        # 既存チェーンの ep1 明示
        if sid in ("v01", "v02", "v09", "z04"):
            r["saga_ep"] = "1"

    existing = {r["stage_id"] for r in rows}
    for sid, _src, meta in NEW:
        if sid in existing:
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

    # keyframes copy
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

    print(f"meta stages={len(rows)} keyframes_rows={len(kf_rows)}")
    print("new:", [s for s, _, _ in NEW])


if __name__ == "__main__":
    main()
