package com.urikai.app.game

/**
 * カイのノートに載る項目。
 * 強制吹き出しで見たものは既読で収録、サイレントは解禁時に未読で追加。
 */
enum class NotebookEntryId {
    /** G1 初回プレイ */
    HOW_TO_PLAY,
    /** G2 LIFE 導入 */
    LIFE,
    /** G5 口座 */
    BALANCE,
    /** G7 図鑑 */
    SCRAPBOOK,
    /** G8-2 切手帳裏面 */
    STAMPS,
    /** S-LAG */
    LAG,
    /** S-AWAY */
    AWAY,
    /** S-LOOK */
    LOOK,
    /** S-RESULT */
    RESULT,
    /** S-COMBO */
    COMBO,
    /** S-BANK */
    BANK;

    companion object {
        fun fromPersisted(name: String): NotebookEntryId? =
            entries.find { it.name == name }
    }
}

/** ノート一覧の表示順（解禁済みのうちこの順）。 */
val NotebookDisplayOrder: List<NotebookEntryId> = listOf(
    NotebookEntryId.HOW_TO_PLAY,
    NotebookEntryId.LIFE,
    NotebookEntryId.LAG,
    NotebookEntryId.BALANCE,
    NotebookEntryId.AWAY,
    NotebookEntryId.RESULT,
    NotebookEntryId.SCRAPBOOK,
    NotebookEntryId.STAMPS,
    NotebookEntryId.LOOK,
    NotebookEntryId.COMBO,
    NotebookEntryId.BANK
)
