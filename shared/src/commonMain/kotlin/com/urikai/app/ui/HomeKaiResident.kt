package com.urikai.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.input.pointer.util.VelocityTracker
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.urikai.app.game.HomeKaiLines
import com.urikai.app.game.Sumika
import com.urikai.app.game.UiStrings
import com.urikai.app.platform.currentTimeMillis
import com.urikai.app.ui.theme.Ink
import com.urikai.app.ui.theme.LcdBackground
import com.urikai.app.ui.theme.LocalDisplayLanguage
import com.urikai.app.ui.theme.PixelFont
import kotlin.concurrent.Volatile
import kotlin.math.abs
import kotlin.math.hypot
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.random.Random
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * ホーム出現スポット。タイトルカード中央帯を避け、四隅〜左右端に置く。
 * ランダム = ホーム入場ごとの抽選（滞在中の消滅ではない）。
 */
private data class HomeKaiSpot(val x: Float, val y: Float)

private val HomeKaiSpots = listOf(
    HomeKaiSpot(0.03f, 0.05f),
    HomeKaiSpot(0.84f, 0.05f),
    HomeKaiSpot(0.02f, 0.20f),
    HomeKaiSpot(0.86f, 0.22f),
    HomeKaiSpot(0.02f, 0.58f),
    HomeKaiSpot(0.86f, 0.58f),
    HomeKaiSpot(0.06f, 0.78f),
    HomeKaiSpot(0.80f, 0.78f),
)

private enum class KaiAct { Idle, Walk, Held, Thrown, Landing, Poke, Sit, Sleep, Inside }

/**
 * ホーム横断のカイ状態（占用・位置・再配置）。
 * Composition 破棄でもプロセス内では残す。
 * 位置のランダム再配置は「1プレイ開始時」と「コールド起動」のみ。
 */
private object HomeKaiSumikaMemory {
    enum class Persist { None, Sit, Sleep, Inside }

    @Volatile
    var persist: Persist = Persist.None

    /** この時刻まで自律／近接の巣接近をロック。 */
    @Volatile
    var approachLockUntilMs: Long = 0L

    @Volatile
    private var reshufflePending: Boolean = false

    @Volatile
    var hasSavedPos: Boolean = false
        private set

    @Volatile
    private var savedNormX: Float = 0.5f

    @Volatile
    private var savedNormY: Float = 0.4f

    @Volatile
    var savedFacingLeft: Boolean = true
        private set

    const val ApproachLockMs = 60_000L
    const val SitMinMs = 30_000L
    const val SitMaxMs = 48_000L
    const val SleepMinMs = 60_000L
    const val SleepMaxMs = 90_000L
    /** 入室後、のぞき穴フェードインまでの待ち。 */
    const val PeekEnterDelayMs = 1_000L
    /** のぞき穴フェード時間（UI と揃える）。 */
    const val PeekFadeMs = 350L
    /** フェードアウト完了後、入口に出現するまでの待ち。 */
    const val PeekExitGapMs = 1_000L
    /** 巣中心からの近接判定（カイサイズ比）。 */
    const val NearNestKaiMult = 1.55f
    /** 再配置時に最初から占用する低確率。 */
    const val ResumeSpawnChance = 0.14f

    fun nowMs(): Long = currentTimeMillis()

    fun approachLocked(): Boolean = nowMs() < approachLockUntilMs

    fun markApproachLock() {
        approachLockUntilMs = nowMs() + ApproachLockMs
    }

    fun clearPersist() {
        persist = Persist.None
    }

    /** デバッグワイプ用。プロセス内メモリも初回相当に戻す。 */
    fun wipeAll() {
        persist = Persist.None
        approachLockUntilMs = 0L
        reshufflePending = false
        hasSavedPos = false
        savedNormX = 0.5f
        savedNormY = 0.4f
        savedFacingLeft = true
    }

    fun captureFromAct(act: KaiAct) {
        if (reshufflePending) return
        persist = when (act) {
            KaiAct.Sleep -> Persist.Sleep
            KaiAct.Inside -> Persist.Inside
            KaiAct.Sit -> Persist.Sit
            else -> Persist.None
        }
    }

    /** 1プレイ開始など：次のホーム入場でスポーンを振り直す。 */
    fun markReshuffle() {
        reshufflePending = true
        hasSavedPos = false
        clearPersist()
    }

    /** true なら今回の boot でランダム配置する。フラグは消費する。 */
    fun consumeReshuffle(): Boolean {
        val v = reshufflePending || !hasSavedPos
        reshufflePending = false
        return v
    }

    fun savePos(
        x: Float,
        y: Float,
        travelW: Float,
        minY: Float,
        travelH: Float,
        facingLeft: Boolean,
    ) {
        // プレイ開始後の破棄では保存しない（markReshuffle を上書きしない）
        if (reshufflePending) return
        if (travelW <= 1f || travelH <= 1f) return
        savedNormX = (x / travelW).coerceIn(0f, 1f)
        savedNormY = ((y - minY) / travelH).coerceIn(0f, 1f)
        savedFacingLeft = facingLeft
        hasSavedPos = true
    }

    fun restoredPos(travelW: Float, minY: Float, travelH: Float): Pair<Float, Float>? {
        if (!hasSavedPos) return null
        val x = (savedNormX * travelW)
        val y = minY + savedNormY * travelH
        return x to y
    }
}

/** 1プレイ開始時に呼び、次回ホーム入場でカイ位置を振り直す。 */
internal fun homeKaiMarkReshuffle() {
    HomeKaiSumikaMemory.markReshuffle()
}

/** デバッグ全消去時にカイ常駐メモリも落とす。 */
internal fun homeKaiWipeMemory() {
    HomeKaiSumikaMemory.wipeAll()
}

/** 拡縮の原点 = 足元中央（体幹下端がずれない）。 */
private val FeetOrigin = TransformOrigin(0.5f, 1f)

/** ドラッグ傾きの原点 = 頭付近（引っ張られる見え方）。 */
private val HeadOrigin = TransformOrigin(0.5f, 0.12f)

private val IdleMoodCycle = listOf(
    GuideBuddyMood.SMILE,
    GuideBuddyMood.TALK,
    GuideBuddyMood.THINK,
    GuideBuddyMood.WINK,
    GuideBuddyMood.SMILE
)

/**
 * フリック判定の下限 (px/s)。
 * 低すぎると軽いドラッグでも MAX 相当になる。スマホは別途ドラッグ差分推定で補う。
 */
private const val FlingSpeedPxPerSec = 640f

/** 投擲速度の底上げ（指フリック向け。PC マウスで飛びすぎない程度）。 */
private const val FlingVelocityGain = 1.35f

/** playThrow 内の px/s → 1フレーム変位への除数（小さいほど遠くまで飛ぶ）。 */
private const val ThrowVelocityDivisor = 48f

/** 静かにドロップしたときの「地面」オフセット（リリース位置より下）。 */
private const val SoftDropFloorPx = 48f

/** 下端押し込み: この距離超で痛そう → さらに超で泣き。 */
private const val FloorPressOuchPx = 144f
private const val FloorPressCryPx = 330f

/**
 * ホームのカイ。
 *
 * - 歩行・呼吸・定期表情（視認できる長さ）
 * - タップ: 表情変化＋大きめスカッシュ（約1秒）
 * - ドラッグ: つかまれ伸び＋速度に応じた揺れ／上下変形、ドロップで着地演出
 * - スワイプ離し: 簡易物理で投擲→着地演出（着地床はリリース位置基準）
 */
@Composable
fun HomeKaiResident(
    notebookHasUnread: Boolean,
    balance: Long,
    peakBalance: Long,
    playCount: Int,
    bestCombo: Int = 0,
    allowanceYen: Long = 0L,
    onCollectAllowance: () -> Unit = {},
    sumikaLevel: Int = 0,
    sumikaPayPending: Boolean = false,
    /** g4+ 入室中ののぞき穴。pose は穴の中のカイ。 */
    onInsidePeekChanged: (show: Boolean, pose: GuideBuddyPose?) -> Unit = { _, _ -> },
    onOccupyingChanged: (Boolean) -> Unit = {},
    /** 内観ピークタップでインクリメント → 起床／会話。 */
    wakeInsideNonce: Int = 0,
    /** 入室中のセリフ（のぞき穴左上表示用）。非入室時は null。 */
    onInsideSpeechChanged: (String?) -> Unit = {},
    /** シート等で隠す（破棄しない＝位置・占用を維持）。 */
    hidden: Boolean = false,
    modifier: Modifier = Modifier
) {
    val lang = LocalDisplayLanguage.current
    val density = LocalDensity.current
    val scope = rememberCoroutineScope()
    val lineTable = remember(playCount, peakBalance, balance, notebookHasUnread, bestCombo, lang) {
        HomeKaiLines.buildActiveTable(
            playCount = playCount,
            peakBalance = peakBalance,
            balance = balance,
            notebookHasUnread = notebookHasUnread,
            lang = lang,
            bestCombo = bestCombo,
        )
    }

    val kaiDp = 64.dp
    val topReserveDp = 28.dp
    /** ホーム本文エリア下端＝START真上付近。大きすぎると宙に浮いて見える。 */
    val bottomReserveDp = 18.dp

    var posXpx by remember { mutableFloatStateOf(0f) }
    var posYpx by remember { mutableFloatStateOf(0f) }
    var scaleX by remember { mutableFloatStateOf(1f) }
    var scaleY by remember { mutableFloatStateOf(1f) }
    var heldTilt by remember { mutableFloatStateOf(0f) }
    var dragVx by remember { mutableFloatStateOf(0f) }
    var dragVy by remember { mutableFloatStateOf(0f) }
    /** 下端を超えて下に押した累積距離（ペタンこ変形・表情用）。 */
    var floorPressPx by remember { mutableFloatStateOf(0f) }
    /** 原画は左向き。true=そのまま、false=左右反転で右向き。 */
    var facingLeft by remember { mutableStateOf(true) }
    var act by remember { mutableStateOf(KaiAct.Idle) }
    var idleMood by remember { mutableStateOf(GuideBuddyMood.SMILE) }
    var reactMood by remember { mutableStateOf<GuideBuddyMood?>(null) }

    var bubbleVisible by remember { mutableStateOf(false) }
    var lineIndex by remember { mutableIntStateOf(0) }
    var forcedLine by remember { mutableStateOf<String?>(null) }
    var allowanceFlying by remember { mutableStateOf(false) }
    var localAllowanceVisible by remember(allowanceYen) { mutableStateOf(allowanceYen > 0L) }

    LaunchedEffect(allowanceYen) {
        if (allowanceYen > 0L) {
            localAllowanceVisible = true
            allowanceFlying = false
        } else if (!allowanceFlying) {
            localAllowanceVisible = false
        }
    }

    var maxX by remember { mutableFloatStateOf(1f) }
    /** プレイ領域の下端（これより下には落ちない上限）。 */
    var travelMaxY by remember { mutableFloatStateOf(1f) }
    var minY by remember { mutableFloatStateOf(0f) }
    /** 今回の落下／投擲で使う着地床（画面下部固定ではない）。 */
    var landFloorY by remember { mutableFloatStateOf(1f) }

    var walkJob by remember { mutableStateOf<Job?>(null) }
    var reactJob by remember { mutableStateOf<Job?>(null) }
    var physicsJob by remember { mutableStateOf<Job?>(null) }
    var occupyJob by remember { mutableStateOf<Job?>(null) }
    var bootDone by remember { mutableStateOf(false) }
    /** g4–g5 入室中: のぞき穴が sleep ポーズか（タップ分岐用）。 */
    var insideSleeping by remember { mutableStateOf(false) }

    fun busy(): Boolean =
        act != KaiAct.Idle && act != KaiAct.Walk &&
            act != KaiAct.Sit && act != KaiAct.Sleep && act != KaiAct.Inside

    fun occupying(): Boolean =
        act == KaiAct.Sit || act == KaiAct.Sleep || act == KaiAct.Inside

    LaunchedEffect(act) {
        onOccupyingChanged(occupying())
        if (act != KaiAct.Inside) {
            onInsidePeekChanged(false, null)
            onInsideSpeechChanged(null)
        }
    }

    LaunchedEffect(act, bubbleVisible, forcedLine) {
        if (act == KaiAct.Inside && bubbleVisible && !forcedLine.isNullOrBlank()) {
            onInsideSpeechChanged(forcedLine)
        } else if (act != KaiAct.Inside || !bubbleVisible) {
            onInsideSpeechChanged(null)
        }
    }

    // 隠している間は Peek UI を親側で消すが、Inside 状態自体は維持
    LaunchedEffect(hidden, act) {
        if (hidden) {
            onInsidePeekChanged(false, null)
            onInsideSpeechChanged(null)
        } else if (act == KaiAct.Inside) {
            onInsidePeekChanged(
                true,
                if (insideSleeping) GuideBuddyPose.SLEEP else GuideBuddyPose.SIT
            )
        }
    }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .graphicsLayer { alpha = if (hidden) 0f else 1f }
    ) {
        val kaiPx = with(density) { kaiDp.roundToPx() }.toFloat()
        val topPx = with(density) { topReserveDp.roundToPx() }.toFloat()
        val bottomPx = with(density) { bottomReserveDp.roundToPx() }.toFloat()
        val travelW = (constraints.maxWidth - kaiPx).coerceAtLeast(1f)
        val travelH = (constraints.maxHeight - kaiPx - topPx - bottomPx).coerceAtLeast(1f)
        val parentW = constraints.maxWidth.toFloat()
        val parentH = constraints.maxHeight.toFloat()

        val posXUpdated = rememberUpdatedState(posXpx)
        val posYUpdated = rememberUpdatedState(posYpx)
        val facingUpdated = rememberUpdatedState(facingLeft)
        val actUpdated = rememberUpdatedState(act)
        val travelWUpdated = rememberUpdatedState(travelW)
        val travelHUpdated = rememberUpdatedState(travelH)
        val topPxUpdated = rememberUpdatedState(topPx)
        DisposableEffect(Unit) {
            onDispose {
                HomeKaiSumikaMemory.captureFromAct(actUpdated.value)
                HomeKaiSumikaMemory.savePos(
                    x = posXUpdated.value,
                    y = posYUpdated.value,
                    travelW = travelWUpdated.value,
                    minY = topPxUpdated.value,
                    travelH = travelHUpdated.value,
                    facingLeft = facingUpdated.value,
                )
                onInsidePeekChanged(false, null)
                onOccupyingChanged(false)
            }
        }

        fun spotOffset(spot: HomeKaiSpot): Pair<Float, Float> =
            (travelW * spot.x).snapPx() to (topPx + travelH * spot.y).snapPx()

        fun clampPos(x: Float, y: Float): Pair<Float, Float> =
            x.coerceIn(0f, travelW).snapPx() to y.coerceIn(topPx, topPx + travelH).snapPx()

        fun grade(): Int = Sumika.gradeIndex(sumikaLevel).coerceIn(1, 5)

        fun seatTarget(xBias: Float = 0f): Pair<Float, Float> {
            val g = grade()
            val (x0, y0) = sumikaOccupyKaiTopLeftPx(
                grade = g,
                parentW = parentW,
                parentH = parentH,
                kaiPx = kaiPx,
                density = density,
                seatFromTop = sumikaOccupySeatFromTop(g),
                xBias = xBias,
            )
            // g4–g5 入口: 家具基準よりやや下・少し左（半身長より浅め）
            return if (g >= 4) {
                (x0 - kaiPx * 0.18f) to (y0 + kaiPx * 0.32f)
            } else {
                x0 to y0
            }
        }

        fun nestCenter(): Pair<Float, Float> =
            sumikaNestCenterPx(grade(), parentW, parentH, density)

        fun nearNest(): Boolean {
            if (sumikaLevel <= 0) return false
            val (cx, cy) = nestCenter()
            val feetX = posXpx + kaiPx * 0.5f
            val feetY = posYpx + kaiPx
            return hypot(feetX - cx, feetY - cy) <= kaiPx * HomeKaiSumikaMemory.NearNestKaiMult
        }

        fun leaveOccupyToIdle(lock: Boolean) {
            insideSleeping = false
            onInsidePeekChanged(false, null)
            if (lock) HomeKaiSumikaMemory.markApproachLock()
            HomeKaiSumikaMemory.clearPersist()
            // 巣シート座標に残ると歩行床より下に見える → ドラッグ上限へ戻す
            val (cx, cy) = clampPos(posXpx, posYpx)
            posXpx = cx
            posYpx = cy
            act = KaiAct.Idle
            scaleX = 1f
            scaleY = 1f
        }

        /**
         * @param allowBelowFloor true のとき巣シートなど歩行床より下の Y をそのまま許可。
         * 床 clamp／nestCap などの追加制限は上下ずれの原因になるので付けない。
         */
        suspend fun walkStraightTo(
            target: Pair<Float, Float>,
            allowBelowFloor: Boolean = false,
        ): Boolean {
            val floorMax = topPx + travelH
            val toX = target.first.coerceIn(0f, travelW).snapPx()
            val toY = if (allowBelowFloor) {
                target.second.snapPx()
            } else {
                target.second.coerceIn(topPx, floorMax).snapPx()
            }
            act = KaiAct.Walk
            var ok = false
            walkJob?.cancel()
            walkJob = scope.launch {
                walkTo(
                    fromX = { posXpx },
                    fromY = { posYpx },
                    setX = { posXpx = it },
                    setY = { posYpx = it },
                    toX = toX,
                    toY = toY,
                    setFacingLeft = { facingLeft = it },
                    setScale = { sx, sy ->
                        scaleX = sx
                        scaleY = sy
                    },
                    stillWalking = { act == KaiAct.Walk }
                )
                ok = act == KaiAct.Walk
                if (ok) {
                    posXpx = toX
                    posYpx = toY
                    scaleX = 1f
                    scaleY = 1f
                }
            }
            walkJob?.join()
            return ok && act == KaiAct.Walk
        }

        /** すみかへは直線移動のみ。着座座標は家具計算値をそのまま使う。 */
        suspend fun approachSumika(): Boolean =
            walkStraightTo(seatTarget(0f), allowBelowFloor = true)

        /**
         * 退出: のぞき穴フェード → 待ち → 席に出現 → 歩行床へ戻して Idle。
         */
        suspend fun exitFromEntrance() {
            val seat = seatTarget(0f)
            insideSleeping = false
            onInsideSpeechChanged(null)
            onInsidePeekChanged(false, null)
            delay(HomeKaiSumikaMemory.PeekFadeMs + HomeKaiSumikaMemory.PeekExitGapMs)
            if (act != KaiAct.Inside) return
            posXpx = seat.first
            posYpx = seat.second
            scaleX = 1f
            scaleY = 1f
            leaveOccupyToIdle(lock = true)
        }

        suspend fun runOccupySession(fromResume: Boolean, preferSleep: Boolean) {
            if (sumikaLevel <= 0) return
            val canEnter = Sumika.supportsInside(sumikaLevel)
            if (!fromResume) {
                if (!approachSumika()) {
                    if (act == KaiAct.Walk) act = KaiAct.Idle
                    return
                }
                val seat = seatTarget(0f)
                posXpx = seat.first
                posYpx = seat.second
            } else {
                val seat = seatTarget(0f)
                posXpx = seat.first
                posYpx = seat.second
                scaleX = 1f
                scaleY = 1f
            }
            facingLeft = true
            reactMood = null
            idleMood = GuideBuddyMood.SMILE

            if (canEnter) {
                // 入口到着＝カイ消える → 1秒後にのぞき穴フェードイン
                act = KaiAct.Inside
                HomeKaiSumikaMemory.persist = HomeKaiSumikaMemory.Persist.Inside
                insideSleeping = false
                delay(HomeKaiSumikaMemory.PeekEnterDelayMs)
                if (act != KaiAct.Inside) return
                onInsidePeekChanged(true, GuideBuddyPose.SIT)
                delay(
                    Random.nextLong(
                        HomeKaiSumikaMemory.SitMinMs,
                        HomeKaiSumikaMemory.SitMaxMs + 1
                    )
                )
                if (act != KaiAct.Inside) return
                val payBlocking = sumikaPayPending || localAllowanceVisible
                if (!payBlocking) {
                    insideSleeping = true
                    onInsidePeekChanged(true, GuideBuddyPose.SLEEP)
                    delay(
                        Random.nextLong(
                            HomeKaiSumikaMemory.SleepMinMs,
                            HomeKaiSumikaMemory.SleepMaxMs + 1
                        )
                    )
                }
                if (act == KaiAct.Inside) {
                    exitFromEntrance()
                }
                return
            }

            // g1–g3
            if (fromResume && preferSleep) {
                act = KaiAct.Sleep
            } else {
                act = KaiAct.Sit
                delay(
                    Random.nextLong(
                        HomeKaiSumikaMemory.SitMinMs,
                        HomeKaiSumikaMemory.SitMaxMs + 1
                    )
                )
                if (act != KaiAct.Sit) return
                val payBlocking = sumikaPayPending || localAllowanceVisible
                val goSleep = preferSleep || (!payBlocking && Random.nextFloat() < 0.45f)
                if (!goSleep || payBlocking) {
                    // 給料中の中断はクールダウンしない（すぐまた入れる）
                    leaveOccupyToIdle(lock = payBlocking.not())
                    return
                }
                act = KaiAct.Sleep
            }
            HomeKaiSumikaMemory.persist = HomeKaiSumikaMemory.Persist.Sleep
            if (act == KaiAct.Sleep) {
                val sleepSeat = seatTarget(0f)
                posXpx = sleepSeat.first
                posYpx = sleepSeat.second
            }
            delay(
                Random.nextLong(
                    HomeKaiSumikaMemory.SleepMinMs,
                    HomeKaiSumikaMemory.SleepMaxMs + 1
                )
            )
            if (act == KaiAct.Sleep) {
                leaveOccupyToIdle(lock = true)
            }
        }

        fun startOccupy(fromResume: Boolean, preferSleep: Boolean) {
            if (sumikaLevel <= 0) return
            if (!fromResume && HomeKaiSumikaMemory.approachLocked()) return
            if (occupying() || act == KaiAct.Walk || busy()) return
            // 着地物理と競合すると席Yが床に上書きされて上ずれする
            physicsJob?.cancel()
            occupyJob?.cancel()
            occupyJob = scope.launch {
                runOccupySession(fromResume = fromResume, preferSleep = preferSleep)
            }
        }

        fun tryNearApproach() {
            if (sumikaLevel <= 0) return
            if (HomeKaiSumikaMemory.approachLocked()) return
            if (occupying() || busy() || act == KaiAct.Walk) return
            if (!nearNest()) return
            startOccupy(fromResume = false, preferSleep = false)
        }

        LaunchedEffect(constraints.maxWidth, constraints.maxHeight) {
            maxX = travelW
            minY = topPx
            travelMaxY = topPx + travelH
            landFloorY = travelMaxY
            if (bootDone) return@LaunchedEffect
            bootDone = true

            val mem = HomeKaiSumikaMemory.persist
            val reshuffle = HomeKaiSumikaMemory.consumeReshuffle()
            when {
                // プレイ後／起動時のみランダム。低確率で占用（sleep 含む）
                reshuffle -> {
                    val (x, y) = spotOffset(HomeKaiSpots.random(Random))
                    posXpx = x
                    posYpx = y
                    facingLeft = Random.nextBoolean()
                    if (sumikaLevel > 0 &&
                        !HomeKaiSumikaMemory.approachLocked() &&
                        Random.nextFloat() < HomeKaiSumikaMemory.ResumeSpawnChance
                    ) {
                        startOccupy(
                            fromResume = false,
                            preferSleep = Random.nextFloat() < 0.35f
                        )
                    }
                }
                // 画面切替戻り: 占用を再開
                sumikaLevel > 0 && mem == HomeKaiSumikaMemory.Persist.Sleep -> {
                    HomeKaiSumikaMemory.clearPersist()
                    startOccupy(fromResume = true, preferSleep = true)
                }
                sumikaLevel > 0 && mem == HomeKaiSumikaMemory.Persist.Inside &&
                    Sumika.supportsInside(sumikaLevel) -> {
                    HomeKaiSumikaMemory.clearPersist()
                    startOccupy(fromResume = true, preferSleep = true)
                }
                sumikaLevel > 0 && mem == HomeKaiSumikaMemory.Persist.Sit -> {
                    HomeKaiSumikaMemory.clearPersist()
                    startOccupy(fromResume = true, preferSleep = false)
                }
                else -> {
                    val restored = HomeKaiSumikaMemory.restoredPos(travelW, topPx, travelH)
                    if (restored != null) {
                        val (cx, cy) = clampPos(restored.first, restored.second)
                        posXpx = cx
                        posYpx = cy
                        facingLeft = HomeKaiSumikaMemory.savedFacingLeft
                    } else {
                        val (x, y) = spotOffset(HomeKaiSpots.random(Random))
                        posXpx = x
                        posYpx = y
                    }
                }
            }
        }

        // アイドル／占用呼吸
        LaunchedEffect(Unit) {
            var t = 0f
            while (true) {
                delay(50L)
                when (act) {
                    KaiAct.Idle, KaiAct.Sit -> {
                        t += 0.050f
                        val b = sin(t * 1.15f)
                        val amp = if (act == KaiAct.Sit) 0.7f else 1f
                        scaleY = 1f + 0.012f * b * amp
                        scaleX = 1f - 0.007f * b * amp
                        heldTilt = 0f
                    }
                    KaiAct.Sleep -> {
                        t += 0.050f
                        val b = sin(t * 0.55f)
                        scaleY = 1f + 0.006f * b
                        scaleX = 1f - 0.004f * b
                        heldTilt = 0f
                    }
                    else -> Unit
                }
            }
        }

        // 掴み中: 速度が落ちても傾きがばね戻りでプランプランする
        LaunchedEffect(Unit) {
            while (true) {
                delay(16L)
                if (act != KaiAct.Held) continue
                dragVx *= 0.88f
                dragVy *= 0.88f
                if (floorPressPx > 0.5f) {
                    // 画面基準: 右へ=+時計回り / 左へ=-反時計回り
                    val targetTilt = (dragVx * 1.25f).coerceIn(-36f, 36f)
                    heldTilt += (targetTilt - heldTilt) * 0.32f
                    applyFloorPetankoDrag(floorPressPx, dragVx) { sx, sy ->
                        scaleX = sx
                        scaleY = sy
                    }
                    continue
                }
                // 空中掴みも同じ画面基準
                val targetTilt = (dragVx * 0.9f).coerceIn(-24f, 24f)
                heldTilt += (targetTilt - heldTilt) * 0.28f
                heldTilt *= 0.96f
                applyHeldDeform(dragVx, dragVy) { sx, sy ->
                    scaleX = sx
                    scaleY = sy
                }
            }
        }

        // 表情定期切替
        LaunchedEffect(Unit) {
            var i = 0
            while (true) {
                delay(Random.nextLong(5000L, 8000L))
                if (act != KaiAct.Idle || bubbleVisible) continue
                i = (i + 1) % IdleMoodCycle.size
                idleMood = IdleMoodCycle[i]
                delay(Random.nextLong(2200L, 3200L))
                if (act == KaiAct.Idle && !bubbleVisible) {
                    idleMood = GuideBuddyMood.SMILE
                }
            }
        }

        // 徒歩（操作中・占用中・接近ロック中の巣狙いは別ループ）
        LaunchedEffect(Unit) {
            while (true) {
                delay(Random.nextLong(6000L, 10000L))
                if (hidden) continue
                if (bubbleVisible || busy() || occupying() || act == KaiAct.Walk) continue

                val here = posXpx to posYpx
                val target = HomeKaiSpots.map { spotOffset(it) }
                    .map { clampPos(it.first, it.second) }
                    .filter { hypot(it.first - here.first, it.second - here.second) > kaiPx * 1.2f }
                    .minByOrNull { hypot(it.first - here.first, it.second - here.second) }
                    ?: continue

                act = KaiAct.Walk
                walkJob = scope.launch {
                    walkTo(
                        fromX = { posXpx },
                        fromY = { posYpx },
                        setX = { posXpx = it },
                        setY = { posYpx = it },
                        toX = target.first,
                        toY = target.second,
                        setFacingLeft = { facingLeft = it },
                        setScale = { sx, sy ->
                            scaleX = sx
                            scaleY = sy
                        },
                        stillWalking = { act == KaiAct.Walk }
                    )
                    if (act == KaiAct.Walk) {
                        val (cx, cy) = clampPos(posXpx, posYpx)
                        posXpx = cx
                        posYpx = cy
                        scaleX = 1f
                        scaleY = 1f
                        act = KaiAct.Idle
                    }
                }
                walkJob?.join()
                delay(Random.nextLong(2200L, 4000L))
            }
        }

        // 自律で巣へ（クールダウン中は行かない）
        LaunchedEffect(sumikaLevel) {
            if (sumikaLevel <= 0) return@LaunchedEffect
            while (true) {
                delay(Random.nextLong(14_000L, 22_000L))
                if (sumikaLevel <= 0) continue
                if (hidden) continue
                if (HomeKaiSumikaMemory.approachLocked()) continue
                if (bubbleVisible || busy() || occupying() || act == KaiAct.Walk) continue
                val chance = (0.28f + sumikaLevel * 0.04f).coerceAtMost(0.65f)
                if (Random.nextFloat() > chance) continue
                startOccupy(fromResume = false, preferSleep = false)
            }
        }

        // のぞき穴タップ: 座り＝会話／睡眠＝セリフ終了→穴フェードアウト→1秒→退出
        LaunchedEffect(wakeInsideNonce) {
            if (wakeInsideNonce <= 0) return@LaunchedEffect
            if (act != KaiAct.Inside) return@LaunchedEffect
            val sleeping = insideSleeping
            if (sleeping) {
                occupyJob?.cancel()
                insideSleeping = false
                onInsidePeekChanged(true, GuideBuddyPose.SIT)
                forcedLine = UiStrings.kaiSumikaWakeLine(lang)
                bubbleVisible = true
                reactMood = GuideBuddyMood.THINK
                reactJob?.cancel()
                delay(4200L)
                bubbleVisible = false
                onInsideSpeechChanged(null)
                // セリフ終了 → フェードアウト → 1秒 → 退出（exitFromEntrance 内）
                if (act == KaiAct.Inside) {
                    exitFromEntrance()
                }
                if (reactMood == GuideBuddyMood.THINK) reactMood = null
            } else {
                forcedLine = UiStrings.kaiSumikaSitTapLine(lang, avoid = forcedLine)
                bubbleVisible = true
                reactMood = GuideBuddyMood.WINK
                reactJob?.cancel()
                reactJob = scope.launch {
                    delay(700L)
                    if (act == KaiAct.Inside && reactMood == GuideBuddyMood.WINK) {
                        reactMood = null
                    }
                }
            }
        }

        LaunchedEffect(bubbleVisible, lineIndex) {
            if (!bubbleVisible) return@LaunchedEffect
            delay(4000L)
            bubbleVisible = false
        }

        fun playLanding(fromThrow: Boolean, wallHits: Int = 0) {
            physicsJob?.cancel()
            physicsJob = scope.launch {
                act = KaiAct.Landing
                heldTilt = 0f
                dragVx = 0f
                dragVy = 0f
                // 着地スカッシュ
                scaleX = 1.18f
                scaleY = 0.78f
                reactMood = when {
                    wallHits >= 3 -> GuideBuddyMood.CRY
                    wallHits == 2 -> GuideBuddyMood.OUCH
                    fromThrow -> GuideBuddyMood.THINK
                    else -> GuideBuddyMood.WINK
                }
                delay(140L)
                scaleX = 0.94f
                scaleY = 1.08f
                delay(100L)
                scaleX = 1.06f
                scaleY = 0.94f
                delay(120L)
                scaleX = 1f
                scaleY = 1f
                // 壁多めは着地後も表情を少し残す
                delay(if (wallHits >= 2) 1_100L else 700L)
                reactMood = null
                idleMood = GuideBuddyMood.SMILE
                act = KaiAct.Idle
                tryNearApproach()
            }
        }

        fun playThrow(vx0Raw: Float, vy0Raw: Float, floor: Float) {
            val vx0 = vx0Raw * FlingVelocityGain
            val vy0 = vy0Raw * FlingVelocityGain
            walkJob?.cancel()
            reactJob?.cancel()
            physicsJob?.cancel()
            bubbleVisible = false
            landFloorY = floor
            physicsJob = scope.launch {
                act = KaiAct.Thrown
                reactMood = GuideBuddyMood.TALK
                heldTilt = (-vx0 / 400f).coerceIn(-18f, 18f)
                scaleX = 0.92f
                scaleY = 1.10f
                var vx = vx0 / ThrowVelocityDivisor
                var vy = vy0 / ThrowVelocityDivisor
                val gravity = 0.48f
                val damp = 0.985f
                val bounce = 0.42f
                var wallHits = 0
                var frames = 0
                while (frames < 180) {
                    frames++
                    vy += gravity
                    vx *= damp
                    var nx = posXpx + vx
                    var ny = posYpx + vy
                    if (nx < 0f) {
                        nx = 0f
                        vx = -vx * bounce
                        facingLeft = false // 左壁→右へ跳ねる＝右向き
                        wallHits++
                        reactMood = moodForWallHits(wallHits)
                    } else if (nx > maxX) {
                        nx = maxX
                        vx = -vx * bounce
                        facingLeft = true // 右壁→左へ跳ねる＝左向き
                        wallHits++
                        reactMood = moodForWallHits(wallHits)
                    }
                    if (ny >= landFloorY) {
                        ny = landFloorY
                        posXpx = nx.snapPx()
                        posYpx = ny.snapPx()
                        heldTilt = 0f
                        playLanding(fromThrow = true, wallHits = wallHits)
                        return@launch
                    }
                    if (ny < minY) {
                        ny = minY
                        vy = abs(vy) * bounce
                    }
                    posXpx = nx.snapPx()
                    posYpx = ny.snapPx()
                    scaleX = 0.96f
                    scaleY = 1.06f
                    heldTilt *= 0.92f
                    if (abs(vx) > 0.4f) facingLeft = vx < 0f
                    delay(16L)
                    if (abs(vx) < 0.8f && abs(vy) < 0.8f && posYpx >= landFloorY - 2f) {
                        heldTilt = 0f
                        playLanding(fromThrow = true, wallHits = wallHits)
                        return@launch
                    }
                }
                heldTilt = 0f
                playLanding(fromThrow = true, wallHits = wallHits)
            }
        }

        fun resolveLandFloor(releaseY: Float, vx: Float, vy: Float, flung: Boolean): Float {
            val room = (travelMaxY - releaseY).coerceAtLeast(0f)
            if (!flung) {
                // 静かに離す: 手放した位置から数十px下が地面
                return (releaseY + SoftDropFloorPx).coerceIn(minY, travelMaxY)
            }
            // 投擲: 速さと下向き成分で着地床を遠くへ伸ばす（上限はプレイ領域下端）
            val speed = hypot(vx, vy)
            val t = ((speed - FlingSpeedPxPerSec) / (FlingSpeedPxPerSec * 2.2f)).coerceIn(0f, 1f)
            val down = (vy.coerceAtLeast(0f) / (FlingSpeedPxPerSec * 1.6f)).coerceIn(0f, 1f)
            val blend = (t * 0.65f + down * 0.55f).coerceIn(0f, 1f)
            val reach = SoftDropFloorPx + room * (0.22f + 0.78f * blend)
            return (releaseY + reach).coerceIn(minY, travelMaxY)
        }

        fun playPoke() {
            if (act == KaiAct.Sleep) {
                walkJob?.cancel()
                reactJob?.cancel()
                physicsJob?.cancel()
                occupyJob?.cancel()
                leaveOccupyToIdle(lock = true)
                forcedLine = UiStrings.kaiSumikaWakeLine(lang)
                bubbleVisible = true
                reactMood = GuideBuddyMood.THINK
                reactJob = scope.launch {
                    scaleX = 1.06f
                    scaleY = 0.94f
                    delay(120L)
                    scaleX = 0.96f
                    scaleY = 1.06f
                    delay(140L)
                    scaleX = 1f
                    scaleY = 1f
                    delay(900L)
                    if (reactMood == GuideBuddyMood.THINK) reactMood = null
                }
                return
            }
            if (act == KaiAct.Sit) {
                reactJob?.cancel()
                forcedLine = UiStrings.kaiSumikaSitTapLine(lang, avoid = forcedLine)
                bubbleVisible = true
                reactMood = GuideBuddyMood.WINK
                reactJob = scope.launch {
                    scaleX = 1.10f
                    scaleY = 0.88f
                    delay(110L)
                    scaleX = 0.96f
                    scaleY = 1.06f
                    delay(120L)
                    scaleX = 1f
                    scaleY = 1f
                    delay(700L)
                    if (act == KaiAct.Sit) reactMood = null
                }
                return
            }
            if (busy() && act != KaiAct.Walk) return
            walkJob?.cancel()
            if (act == KaiAct.Walk) {
                scaleX = 1f
                scaleY = 1f
            }
            reactJob?.cancel()
            physicsJob?.cancel()
            bubbleVisible = false
            reactJob = scope.launch {
                act = KaiAct.Poke
                reactMood = listOf(
                    GuideBuddyMood.WINK,
                    GuideBuddyMood.TALK,
                    GuideBuddyMood.THINK
                ).random(Random)
                // 控えめスカッシュ（足元原点）
                scaleX = 1.08f
                scaleY = 0.90f
                delay(100L)
                scaleX = 0.94f
                scaleY = 1.08f
                delay(120L)
                scaleX = 1.04f
                scaleY = 0.96f
                delay(140L)
                scaleX = 1f
                scaleY = 1f
                delay(580L)
                reactMood = null
                act = KaiAct.Idle
            }
            // ついでに一言
            forcedLine = null
            bubbleVisible = true
            lineIndex += 1
        }

        fun onGrab() {
            if (act == KaiAct.Inside) return
            val wasOccupy = occupying()
            walkJob?.cancel()
            reactJob?.cancel()
            physicsJob?.cancel()
            occupyJob?.cancel()
            bubbleVisible = false
            onInsidePeekChanged(false, null)
            if (wasOccupy) {
                HomeKaiSumikaMemory.markApproachLock()
                HomeKaiSumikaMemory.clearPersist()
            }
            act = KaiAct.Held
            reactMood = GuideBuddyMood.SMILE
            dragVx = 0f
            dragVy = 0f
            heldTilt = 0f
            floorPressPx = 0f
            scaleX = 0.86f
            scaleY = 1.22f
        }

        fun onDropOrFling(vx: Float, vy: Float) {
            val speed = hypot(vx, vy)
            val releaseY = posYpx
            val pressed = floorPressPx
            val pressHits = floorPressToHitCount(pressed)
            floorPressPx = 0f
            val flung = speed >= FlingSpeedPxPerSec
            val floor = resolveLandFloor(releaseY, vx, vy, flung)
            landFloorY = floor
            heldTilt = 0f
            dragVx = 0f
            dragVy = 0f
            if (flung) {
                facingLeft = vx < 0f
                playThrow(vx, vy, floor)
            } else if (pressed > 0.5f && posYpx >= travelMaxY - 1f) {
                // 下端に押し付けたまま離す → その場で着地（表情は押し込み量）
                physicsJob?.cancel()
                physicsJob = scope.launch {
                    playLanding(fromThrow = false, wallHits = pressHits)
                }
            } else {
                // 手を離した付近の少し下が地面
                physicsJob?.cancel()
                physicsJob = scope.launch {
                    act = KaiAct.Thrown
                    reactMood = GuideBuddyMood.TALK
                    if (posYpx >= floor - 1f) {
                        posYpx = floor.snapPx()
                        playLanding(fromThrow = false)
                        return@launch
                    }
                    var vyFall = 2.2f
                    while (posYpx < floor) {
                        vyFall += 0.65f
                        posYpx = (posYpx + vyFall).coerceAtMost(floor).snapPx()
                        scaleX = 0.94f
                        scaleY = 1.08f
                        delay(16L)
                    }
                    posYpx = floor.snapPx()
                    playLanding(fromThrow = false)
                }
            }
        }

        val line = forcedLine ?: HomeKaiLines.pickFromTable(lineTable, lineIndex, lang)
        val mood = reactMood
            ?: when {
                bubbleVisible && forcedLine != null -> GuideBuddyMood.THINK
                bubbleVisible && notebookHasUnread -> GuideBuddyMood.TALK
                bubbleVisible && balance < 10_000L -> GuideBuddyMood.THINK
                bubbleVisible -> GuideBuddyMood.TALK
                else -> idleMood
            }

        // 入室中のセリフはのぞき穴側（HomeStartScene）に出す
        val showBubble = bubbleVisible &&
            act != KaiAct.Held && act != KaiAct.Thrown && act != KaiAct.Inside
        var bubbleWpx by remember { mutableIntStateOf(0) }
        var bubbleHpx by remember { mutableIntStateOf(0) }
        val screenW = constraints.maxWidth
        val screenH = constraints.maxHeight
        val marginPx = with(density) { 8.dp.roundToPx() }
        val gapPx = with(density) { 6.dp.roundToPx() }
        val bubbleMaxW = with(density) {
            minOf(168.dp, maxWidth - 16.dp).coerceAtLeast(96.dp)
        }
        val showAllowancePop = localAllowanceVisible && allowanceYen > 0L && !allowanceFlying &&
            act != KaiAct.Held && act != KaiAct.Thrown

        // g4+ 入室中は本体非表示（内観ピーク側）。吹き出しだけ入口位置に残す。
        val kaiVisible = act != KaiAct.Inside
        Box(
            modifier = Modifier.offset { IntOffset(posXpx.roundToInt(), posYpx.roundToInt()) }
        ) {
            if (showAllowancePop && kaiVisible) {
                SumikaPayPop(
                    amount = allowanceYen,
                    onClick = {
                        allowanceFlying = true
                        localAllowanceVisible = false
                        forcedLine = UiStrings.kaiAllowanceLine(lang)
                        bubbleVisible = true
                        reactMood = GuideBuddyMood.THINK
                        onCollectAllowance()
                        scope.launch {
                            delay(420L)
                            allowanceFlying = false
                            delay(4200L)
                            if (forcedLine != null) {
                                bubbleVisible = false
                                forcedLine = null
                                reactMood = null
                            }
                        }
                    },
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .offset(y = (-10).dp)
                )
            }
            if (kaiVisible) Box(
                modifier = Modifier
                    .size(kaiDp)
                    .then(
                        if (hidden) Modifier
                        else Modifier.pointerInput(maxX, travelMaxY, minY) {
                        awaitEachGesture {
                            val down = awaitFirstDown(requireUnconsumed = false)
                            val tracker = VelocityTracker()
                            tracker.addPosition(down.uptimeMillis, down.position)

                            var dragged = false
                            var pointerId = down.id
                            while (true) {
                                val event = awaitPointerEvent()
                                val change = event.changes.firstOrNull { it.id == pointerId }
                                    ?: event.changes.firstOrNull()
                                    ?: break
                                pointerId = change.id
                                tracker.addPosition(change.uptimeMillis, change.position)

                                if (change.pressed) {
                                    val dist = hypot(
                                        change.position.x - down.position.x,
                                        change.position.y - down.position.y
                                    )
                                    if (!dragged && dist > viewConfiguration.touchSlop) {
                                        dragged = true
                                        onGrab()
                                    }
                                    if (dragged) {
                                        val dx = change.positionChange().x
                                        val dy = change.positionChange().y
                                        val (cx, cy) = clampPos(posXpx + dx, posYpx + dy)
                                        posXpx = cx
                                        posYpx = cy
                                        // 下端より下へ動かそうとした分を押し込み距離に積む
                                        if (cy >= travelMaxY - 0.5f) {
                                            if (dy > 0f) {
                                                floorPressPx += dy
                                            } else {
                                                floorPressPx = (floorPressPx + dy).coerceAtLeast(0f)
                                            }
                                        } else {
                                            floorPressPx = 0f
                                        }
                                        dragVx = dragVx * 0.45f + dx * 0.55f
                                        dragVy = dragVy * 0.45f + dy * 0.55f
                                        if (floorPressPx > 0.5f) {
                                            applyFloorPetankoDrag(floorPressPx, dragVx) { sx, sy ->
                                                scaleX = sx
                                                scaleY = sy
                                            }
                                            // 画面基準: 右=+時計回り / 左=-反時計回り
                                            val targetTilt = (dragVx * 1.35f).coerceIn(-38f, 38f)
                                            heldTilt += (targetTilt - heldTilt) * 0.48f
                                            reactMood = moodForFloorPress(floorPressPx)
                                        } else {
                                            applyHeldDeform(dragVx, dragVy) { sx, sy ->
                                                scaleX = sx
                                                scaleY = sy
                                            }
                                            val targetTilt = (dragVx * 0.95f).coerceIn(-26f, 26f)
                                            heldTilt += (targetTilt - heldTilt) * 0.42f
                                            reactMood = GuideBuddyMood.SMILE
                                        }
                                        if (dx < -0.5f) facingLeft = true
                                        if (dx > 0.5f) facingLeft = false
                                    }
                                    change.consume()
                                } else {
                                    if (dragged) {
                                        val v = tracker.calculateVelocity()
                                        // VelocityTracker が弱い端末向けに、直近ドラッグ差分からも推定して強い方を採用
                                        val fromDragVx = dragVx * 60f * 1.1f
                                        val fromDragVy = dragVy * 60f * 1.1f
                                        val useDrag =
                                            hypot(fromDragVx, fromDragVy) > hypot(v.x, v.y) * 1.15f
                                        if (useDrag) {
                                            onDropOrFling(fromDragVx, fromDragVy)
                                        } else {
                                            onDropOrFling(v.x, v.y)
                                        }
                                    } else {
                                        playPoke()
                                    }
                                    break
                                }
                            }
                        }
                        }
                    )
                    // 外側: 頭原点で傾き（空中・地面ドラッグ共通）
                    .graphicsLayer {
                        transformOrigin = HeadOrigin
                        rotationZ = heldTilt
                    }
            ) {
                // 内側: 足元原点で拡縮・左右反転
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer {
                            transformOrigin = FeetOrigin
                            this.scaleX = scaleX * (if (facingLeft) 1f else -1f)
                            this.scaleY = scaleY
                        }
                ) {
                    when (act) {
                        // sit はセル内でやや左上寄り・大きい → 縮小＋右下へ微補正
                        KaiAct.Sit -> Box(Modifier.offset(x = 2.dp, y = 3.dp)) {
                            GuideBuddyPose(pose = GuideBuddyPose.SIT, size = kaiDp * 0.90f)
                        }
                        // sleep は扁平で小さく・上に見えやすい → わずかに拡大＋下へ
                        KaiAct.Sleep -> Box(Modifier.offset(y = 5.dp)) {
                            GuideBuddyPose(pose = GuideBuddyPose.SLEEP, size = kaiDp * 1.06f)
                        }
                        else -> GuideBuddyFace(mood = mood, size = kaiDp)
                    }
                }
            }
        }

        // 吹き出しは画面座標でクランプ（端にいるときのはみ出し防止）。キャラより前面。
        if (showBubble) {
            Text(
                text = line,
                color = Ink,
                fontSize = 11.sp,
                fontFamily = PixelFont,
                lineHeight = 14.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .zIndex(2f)
                    .widthIn(max = bubbleMaxW)
                    .onSizeChanged {
                        bubbleWpx = it.width
                        bubbleHpx = it.height
                    }
                    .offset {
                        val bw = bubbleWpx.coerceAtLeast(1)
                        val bh = bubbleHpx.coerceAtLeast(1)
                        val preferX = (posXpx + kaiPx / 2f - bw / 2f).roundToInt()
                        val x = preferX.coerceIn(
                            marginPx,
                            (screenW - bw - marginPx).coerceAtLeast(marginPx)
                        )
                        val above = (posYpx - bh - gapPx).roundToInt()
                        val below = (posYpx + kaiPx + gapPx).roundToInt()
                        val y = when {
                            above >= marginPx -> above
                            below + bh <= screenH - marginPx -> below
                            else -> marginPx
                        }.coerceIn(marginPx, (screenH - bh - marginPx).coerceAtLeast(marginPx))
                        IntOffset(x, y)
                    }
                    .lcdDoubleBorder()
                    .background(LcdBackground)
                    .padding(horizontal = 8.dp, vertical = 5.dp)
            )
        }
    }
}

/** 直線距離で歩く（横→縦の L 字ではない）。 */
private suspend fun walkTo(
    fromX: () -> Float,
    fromY: () -> Float,
    setX: (Float) -> Unit,
    setY: (Float) -> Unit,
    toX: Float,
    toY: Float,
    setFacingLeft: (Boolean) -> Unit,
    setScale: (Float, Float) -> Unit,
    stillWalking: () -> Boolean
) {
    if (!stillWalking()) return
    val startX = fromX()
    val startY = fromY()
    val dx = toX - startX
    val dy = toY - startY
    val dist = hypot(dx, dy)
    if (dist < 3f) {
        setX(toX.snapPx())
        setY(toY.snapPx())
        setScale(1f, 1f)
        return
    }
    if (abs(dx) >= 1.5f) setFacingLeft(dx < 0f)

    val stepPx = 4f
    val steps = (dist / stepPx).toInt().coerceAtLeast(1)
    val pauseEvery = Random.nextInt(4, 7)
    var i = 0
    while (stillWalking() && i < steps) {
        i++
        val t = i.toFloat() / steps.toFloat()
        setScale(1.03f, 0.96f)
        delay(55L)
        if (!stillWalking()) return
        setX((startX + dx * t).snapPx())
        setY((startY + dy * t).snapPx())
        if (abs(dx) >= 1.5f) setFacingLeft(dx < 0f)
        setScale(0.98f, 1.04f)
        delay(100L)
        if (!stillWalking()) return
        setScale(1f, 1f)
        if (i % pauseEvery == 0) {
            delay(Random.nextLong(160L, 260L))
        }
    }
    if (stillWalking()) {
        setX(toX.snapPx())
        setY(toY.snapPx())
        setScale(1f, 1f)
    }
}

private fun Float.snapPx(): Float = this.roundToInt().toFloat()

/** 投擲中の壁ヒット数→表情。 */
private fun moodForWallHits(hits: Int): GuideBuddyMood = when {
    hits >= 3 -> GuideBuddyMood.CRY
    hits >= 2 -> GuideBuddyMood.OUCH
    else -> GuideBuddyMood.TALK
}

/** 下端押し込み距離→表情（楽しい→痛い→泣き）。 */
private fun moodForFloorPress(press: Float): GuideBuddyMood = when {
    press >= FloorPressCryPx -> GuideBuddyMood.CRY
    press >= FloorPressOuchPx -> GuideBuddyMood.OUCH
    else -> GuideBuddyMood.SMILE
}

/** 着地演出へ渡すヒット相当（表情の閾値と揃える）。 */
private fun floorPressToHitCount(press: Float): Int = when {
    press >= FloorPressCryPx -> 3
    press >= FloorPressOuchPx -> 2
    else -> 0
}

/** 掴み中の拡縮: 左右速度で横伸び＋揺れ用、上下で伸び／つぶれ。 */
private fun applyHeldDeform(vx: Float, vy: Float, setScale: (Float, Float) -> Unit) {
    val sxSpeed = vx.coerceIn(-36f, 36f)
    val sySpeed = vy.coerceIn(-36f, 36f)
    var sx = 0.86f + abs(sxSpeed) * 0.0055f
    var sy = 1.22f - abs(sxSpeed) * 0.0025f
    // 上へ引く → さらに縦伸び／下へ押す → つぶれ気味
    sy += (-sySpeed) * 0.007f
    sx += sySpeed * 0.0035f
    setScale(sx.coerceIn(0.68f, 1.18f), sy.coerceIn(0.92f, 1.48f))
}

/**
 * ペタンこ＋左右引きずり専用拡縮。
 * 押し込みで横広がり、左右速度でさらに進行方向へ引き伸ばす。
 */
private fun applyFloorPetankoDrag(press: Float, vx: Float, setScale: (Float, Float) -> Unit) {
    val t = (press / FloorPressCryPx).coerceIn(0f, 1f)
    val slide = (abs(vx) / 26f).coerceIn(0f, 1f)
    val sx = 1.02f + 0.48f * t + 0.32f * slide
    val sy = 0.98f - 0.18f * t - 0.05f * slide
    setScale(sx.coerceIn(1f, 1.75f), sy.coerceIn(0.74f, 1f))
}
