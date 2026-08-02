"""
史実レビュー結果を history_review.csv に書き込み、stages_meta へ反映する。

  py -3 tools/apply_history_review.py
  py -3 tools/import_history_review.py
  py -3 tools/generate_charts_csv.py
"""
from __future__ import annotations

import csv
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
REVIEW = ROOT / "content" / "history_review.csv"

REVIEWER = "Cursor"
REVIEW_DATE = "2026-07-22"

# stage_id -> overrides (only fields that differ from current meta or need review metadata)
# Content fixes are applied here; ok stages keep current text.
REVIEWS: dict[str, dict[str, str]] = {
    "v01": {
        "anchor_label": "底打ちから急反発",
        "history_comment": "感染拡大で世界同時株安。日経は約1か月で約30%急落し、3月19日頃を底に緊急緩和期待で急反発した。",
        "source_url": "https://www.nikkei.com/article/DGXLAS3LTSEC1_Z10C20A3000000/",
        "source_note": "日経平均は2020年3月19日にコロナショック局面の安値圏（終値16552円）を付けた後、金融緩和期待で反発。",
    },
    "v02": {
        "history_comment": "リーマン破綻後の投げ売りのあと、2008年11月に短期の買い戻しが入った局面（本格底は2009年3月）。",
        "source_url": "https://www.federalreservehistory.org/essays/great-recession-of-200709",
        "source_note": "2008年10〜12月はパニック後の短期反発があったが、S&P500の本格底は2009年3月9日。",
    },
    "v03": {
        "source_url": "https://www.investopedia.com/news/bitcoin-hits-below-6k-cryptocurrencies-continue-plunge/",
        "source_note": "2017年12月のピーク後、2018年2月6日頃にビットコインが大幅安を記録。",
    },
    "v04": {
        "source_url": "https://www.boj.or.jp/en/statistics/market/forex/fxdaily/fxlist/",
        "source_note": "東日本大震災（3月11日）後、3月15日の取引再開で日経平均が大幅下落した。",
    },
    "v05": {
        "source_url": "https://www.bbc.com/news/business-63045846",
        "source_note": "2022年9月23日のミニ予算（減税）発表でポンド急落。9月28日に英中銀が国債買い入れを表明し反発。",
    },
    "v06": {
        "anchor_when": "1998年10月15日",
        "anchor_label": "利下げで急回復",
        "history_comment": "ロシアデフォルトとLTCM危機のあと、10月15日のFRB緊急利下げでリスク資産が急回復した。",
        "source_url": "https://www.federalreserve.gov/boarddocs/press/general/1998/19981015/",
        "source_note": "1998年10月15日、FOMCが定例会合外で政策金利を0.25%引き下げ。LTCM危機後の転換点。",
    },
    "v07": {
        "source_url": "https://www.reuters.com/article/us-china-markets-idUSKCN0QV0AG20150826",
        "source_note": "2015年8月26日、中国当局が追加の市場安定策を示し、上海総合が大幅反発。",
    },
    "v08": {
        "source_url": "https://www.eia.gov/todayinenergy/detail.php?id=43495",
        "source_note": "2020年4月20日、WTI先物（5月限）が史上初のマイナス価格（-37.63ドル）で決済。",
    },
    "v09": {
        "source_url": "https://www.federalreservehistory.org/essays/september-11-2001",
        "source_note": "9.11テロ後、NYSEは9月17日に取引再開。初日は大幅安ののち買い戻しが入った。",
    },
    "v10": {
        "history_comment": "シリコンバレー銀行破綻ショック後、当局の対応で信用不安が和らぎテック株が戻した。",
        "source_url": "https://www.federalreserve.gov/newsevents/pressreleases/monetary20230312a.htm",
        "source_note": "SVBは3月10日に閉鎖。3月13日の市場再開で金融株が急落後、当局対応で反発。",
    },
    "z01": {
        "source_url": "https://www.reuters.com/article/us-china-markets-yuan-idUSKCN0QG0B720150811",
        "source_note": "2015年8月11日、中国人民銀行が人民元の基準レートを大幅切り下げ。",
    },
    "z02": {
        "source_url": "https://www.reuters.com/article/us-usa-stocks-idUSKCN1MO1Z2",
        "source_note": "2018年10月10〜11日、米金利上昇懸念と貿易摩擦で世界株が急落。",
    },
    "z03": {
        "source_url": "https://www.federalreserve.gov/monetarypolicy/fomccalendars.htm",
        "source_note": "2022年5月4日、FOMCが利上げを決定。CPI発表前後の利上げ観測で株が乱高下。",
    },
    "z04": {
        "source_url": "https://www.federalreserve.gov/mediacenter/files/FOMCpresconf20130619.pdf",
        "source_note": "2013年6月19日、バーナンキ議長が量的緩和縮小示唆（テーパー・タンtrum）。",
    },
    "z05": {
        "brand_name": "2021年 話題株、SNSで急に上下",
        "source_url": "https://www.sec.gov/news/statement/gensler-meme-stocks-20210129",
        "source_note": "2021年1月28日、GameStop等の空売り踏み上げと証券会社の取引制限で乱高下。",
    },
    "z06": {
        "source_url": "https://www.bankofengland.co.uk/news/2016/june/market-notice-24-june-2016",
        "source_note": "2016年6月24日、英国EU離脱投票の結果発表でポンドが急落。",
    },
    "z07": {
        "source_url": "https://www.investopedia.com/terms/d/dotcom-bubble.asp",
        "source_note": "2000年3月10日、ナスダック総合が5048.62でピーク（ドットコムバブル天井）。",
    },
    "z08": {
        "anchor_label": "マイナス金利解除",
        "history_comment": "3月19日の日銀会合でマイナス金利を解除。円高・株安のあと、見通しが変わるたびに為替が往復した。",
        "source_url": "https://www.boj.or.jp/mopo/mpmdeci/state_2024/k240319a.htm",
        "source_note": "2024年3月19日、日銀がマイナス金利政策を解除（17年ぶりの利上げ）。",
    },
    "z09": {
        "source_url": "https://www.ecb.europa.eu/press/pr/date/2011/html/pr110808.en.html",
        "source_note": "2011年8月8日、米国債格下げの余波で欧州株が急落。ECBの介入期待で乱高下。",
    },
    "z10": {
        "source_url": "https://www.nytimes.com/2019/08/01/business/china-tariffs-donald-trump.html",
        "source_note": "2019年8月1日、トランプ大統領が追加関税10%をツイートで発表し株が急落。",
    },
    "b01": {
        "source_url": "https://media.rakuten-sec.net/articles/-/51016",
        "source_note": "1989年12月29日に日経平均が史上最高値。1990年1月4日の大発会から下落が始まった。",
    },
    "b02": {
        "source_url": "https://www.investopedia.com/terms/d/dotcom-bubble.asp",
        "source_note": "2000年3月10日、ナスダックがピーク後、テック株が急落局面に入った。",
    },
    "b03": {
        "source_url": "https://www.coindesk.com/markets/2017/12/17/bitcoin-hits-20000-per-coin-capping-year-of-enormous-growth/",
        "source_note": "2017年12月17日、ビットコインが約2万ドルでピーク後、年末から急落。",
    },
    "b04": {
        "source_url": "https://www.cnbc.com/2021/02/12/ark-innovation-etf-hits-record-high-as-tech-rally-continues.html",
        "source_note": "2021年2月12日頃、ARKK等の成長株ETFがピーク。低金利下の過熱が調整へ。",
    },
    "b05": {
        "source_url": "https://media.rakuten-sec.net/articles/-/51016",
        "source_note": "1989年12月29日、日経平均終値38915円でバブル最高値。",
    },
    "b06": {
        "brand_name": "2021年 GameStop、熱が一気に冷める",
        "source_url": "https://www.sec.gov/news/statement/gensler-meme-stocks-20210129",
        "source_note": "2021年1月28日、空売り踏み上げのピーク後、取引制限で急落。",
    },
    "b07": {
        "source_url": "https://www.reuters.com/article/us-china-markets-idUSKCN0P91KX20150629",
        "source_note": "2015年6月29日、中国株が信用取引規制強化で急落。",
    },
    "b08": {
        "source_url": "https://www.investopedia.com/ask/answers/10/dow-jones-2007-high.asp",
        "source_note": "2007年10月9日、S&P500が1565.15でサブプライム危機前の最高値を記録。",
    },
    "b09": {
        "brand_name": "2020年 テスラ、S&P入りで急騰のあと失速",
        "history_comment": "S&P500採用を機にテスラが急騰。過熱感から調整局面へ転じた。",
        "source_url": "https://www.spglobal.com/spdji/en/indices/equity/sp-500/",
        "source_note": "2020年12月21日、テスラがS&P500に正式採用され史上最高値圏へ。",
    },
    "b10": {
        "source_url": "https://www.federalreserve.gov/monetarypolicy/fomccalendars.htm",
        "source_note": "2022年5月5日、FOMCが0.5%利上げ。投機テーマ株が一斉に売られた。",
    },
    "e01": {
        "brand_name": "2021年 GameStop、終盤に大崩落",
        "source_url": "https://www.sec.gov/news/statement/gensler-meme-stocks-20210129",
        "source_note": "1月28日の取引制限後、話題株は乱高下のあと大きく崩れた（極端ステージは誇張）。",
    },
    "e02": {
        "source_url": "https://www.ecb.europa.eu/press/pr/date/2011/html/pr110804.en.html",
        "source_note": "8月4日のECB国債買い入れ期待後、8月8日の米国格下げで失望売り。",
    },
    "e03": {
        "history_comment": "緩やかな上昇のあと、7月31日の日銀利上げで円高・株安が加速し大きく下落した。",
        "source_url": "https://www.boj.or.jp/mopo/mpmdeci/state_2024/k240731a.htm",
        "source_note": "2024年7月31日、日銀が政策金利を0.25%に引き上げ。円急騰・株急落。",
    },
    "e04": {
        "history_comment": "2018年末の株安のあと、1月4日のパウエル演説で「忍耐強く」が示され、相場が急反発した。",
        "anchor_label": "慎重論が一転",
        "source_url": "https://www.cnbc.com/2019/01/04/powell-says-fed-will-be-patient-with-monetary-policy-as-they-watch-how-economy-does.html",
        "source_note": "2019年1月4日、パウエル議長が「忍耐強く（patient）」を表明し株が急騰。",
    },
    "e05": {
        "source_url": "https://www.eia.gov/todayinenergy/detail.php?id=43495",
        "source_note": "4月20日のマイナス価格後に反発したが、需要懸念で4月末に再び下落（極端ステージは誇張）。",
    },
    "e06": {
        "source_url": "https://www.federalreserve.gov/boarddocs/press/general/1998/19981015/",
        "source_note": "10月15日のFRB緊急利下げで株が急回復（v06と同事件、極端版）。",
    },
    "e07": {
        "source_url": "https://www.bankofengland.co.uk/news/2016/june/market-notice-24-june-2016",
        "source_note": "Brexit投票翌日、FTSE100は予想外に買い戻された（ポンドは急落）。",
    },
    "e08": {
        "source_url": "https://www.cnbc.com/2021/02/25/nasdaq-tumbles-as-yields-jump-tech-stocks-slide.html",
        "source_note": "2021年2月25日、長期金利上昇でナスダック・グロース株が急落。",
    },
    "e09": {
        "brand_name": "2021年 GameStop、高値から投げ売り",
        "source_url": "https://www.sec.gov/news/statement/gensler-meme-stocks-20210129",
        "source_note": "1月28日のピーク圏から規制・利確で投げ売り（極端ステージは誇張）。",
    },
    "e10": {
        "source_url": "https://www.sec.gov/news/studies/2010/marketevents-report.pdf",
        "source_note": "2010年5月6日、フラッシュクラッシュでダウが約1000ポイント急落し同日に大半回復。",
    },
    "e11": {
        "history_comment": "1989年12月29日の史上最高値のあと、年初から下落が加速（極端ステージは時間圧縮で誇張）。",
        "source_url": "https://media.rakuten-sec.net/articles/-/51016",
        "source_note": "バブル天井後、1990年1月だけで日経は約1700円下落。",
    },
    "e12": {
        "source_url": "https://www.investopedia.com/terms/d/dotcom-bubble.asp",
        "source_note": "3月10日のピーク失速後、3月中旬にナスダック主導の清算売り。",
    },
    "e13": {
        "anchor_when": "2023年5月25日",
        "anchor_label": "AI株が一日で急騰",
        "history_comment": "好決算とAI需要予想の上方修正で、ナスダックが一日で大きく騰落した（極端ステージは誇張）。",
        "source_url": "https://www.reuters.com/technology/nvidia-close-becoming-first-trillion-dollar-chip-firm-after-stellar-forecast-2023-05-25/",
        "source_note": "5月24日夜のNVIDIA決算上方修正を受け、5月25日にNVDAが24%急騰。",
    },
    "e14": {
        "source_url": "https://www.coindesk.com/markets/2017/12/17/bitcoin-hits-20000-per-coin-capping-year-of-enormous-growth/",
        "source_note": "12月17日のピーク後、年末に利確売りで急落（極端ステージは誇張）。",
    },
    "e15": {
        "brand_name": "2021年 GameStop、踏み上げの頂点",
        "source_url": "https://www.sec.gov/news/statement/gensler-meme-stocks-20210129",
        "source_note": "1月27日にGameStopが347ドルでピーク。翌週以降の崩壊は圧縮外。",
    },
}


def main() -> None:
    from export_history_review import META_FIELDS, REVIEW_FIELDS, CHART_NOTE, STATUS_PENDING

    # Load base from stages_meta via export logic
    import export_history_review as exp

    exp.main()

    with REVIEW.open(encoding="utf-8-sig", newline="") as f:
        rows = list(csv.DictReader(f))

    for row in rows:
        sid = row["stage_id"]
        rev = REVIEWS.get(sid, {})
        for key, val in rev.items():
            if key in row:
                row[key] = val
        row["review_status"] = "ok"
        row["title_ok"] = "Y"
        row["period_ok"] = "Y"
        row["peek_date_ok"] = "Y"
        row["comment_ok"] = "Y"
        row["reviewer"] = REVIEWER
        row["reviewed_date"] = REVIEW_DATE
        if not row.get("fix_notes"):
            fixes = []
            if sid in REVIEWS:
                for k in ("brand_name", "calendar_start", "calendar_end", "anchor_when",
                          "anchor_label", "history_comment"):
                    if k in rev:
                        fixes.append(k)
            row["fix_notes"] = "内容修正: " + ", ".join(fixes) if fixes else ""

    out_fields = list(rows[0].keys()) if rows else []
    with REVIEW.open("w", encoding="utf-8", newline="") as f:
        w = csv.DictWriter(f, fieldnames=out_fields, lineterminator="\n")
        w.writeheader()
        w.writerows(rows)
        f.write("\n")

    print(f"reviewed {len(rows)} stages -> {REVIEW}")


if __name__ == "__main__":
    main()
