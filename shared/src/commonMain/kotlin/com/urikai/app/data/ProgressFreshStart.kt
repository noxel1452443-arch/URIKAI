package com.urikai.app.data

import com.urikai.app.platform.KeyValueStore

/**
 * クローズドテスト用: 進捗エポックが上がったときだけセーブを初期化する。
 *
 * versionCode とは独立。このビルドでテスターに初回からやり直してもらうため
 * [PROGRESS_EPOCH] を上げた。次回以降の通常アップデートではエポックを据え置けばデータは残る。
 *
 * - 記録エポック < [PROGRESS_EPOCH] → ワイプして初回状態
 * - すでに同じエポック → 何もしない（version を上げても引き継ぎ）
 */
object ProgressFreshStart {
    /**
     * 意図的に全消ししたいリリースのときだけインクリメントする。
     * （閉じたテストでチュートリアル〜すみか〜切手を最初から見てもらう用 = 2）
     */
    const val PROGRESS_EPOCH: Int = 2

    private const val META_PREFS = "urikai_meta"
    private const val KEY_PROGRESS_EPOCH = "progress_epoch"

    private val PROGRESS_STORES = listOf(
        "urikai_settings",
        "urikai_scrapbook",
        "urikai_lifetime_pnl",
    )

    /**
     * ViewModel / Repository 生成より前に呼ぶこと。
     * @return true なら今回ワイプした
     */
    fun applyIfNeeded(openStore: (String) -> KeyValueStore): Boolean {
        val meta = openStore(META_PREFS)
        val seen = meta.getInt(KEY_PROGRESS_EPOCH, 0)
        val wiped = seen < PROGRESS_EPOCH
        if (wiped) {
            PROGRESS_STORES.forEach { name ->
                openStore(name).edit().clear().commit()
            }
        }
        meta.edit().putInt(KEY_PROGRESS_EPOCH, PROGRESS_EPOCH).commit()
        return wiped
    }
}
