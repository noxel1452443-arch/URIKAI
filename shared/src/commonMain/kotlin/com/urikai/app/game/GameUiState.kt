package com.urikai.app.game

/**
 * Phase 1 固定。この6変数以外は追加しない。
 */
data class GameUiState(
    val gameState: GameState = GameState.START,
    val currentSecond: Int = 0,
    val currentPriceDisplay: Int = 1_000,
    val totalAsset: Int = 10_000,
    val hasStock: Boolean = false,
    val isOrderProcessing: Boolean = false
)
