package com.msa.audit

import com.msa.compose_kmm.domain.Game
import com.msa.compose_kmm.domain.GameConfig
import com.msa.compose_kmm.domain.GameSnapshot
import com.msa.compose_kmm.domain.GameStateStore
import com.msa.compose_kmm.domain.GameStatus
import com.msa.compose_kmm.domain.SaveDurability
import com.msa.compose_kmm.presentation.GameController
import com.msa.compose_kmm.presentation.GameInputResult

private class AuditStore : GameStateStore {
    var snapshot: GameSnapshot? = null
    var saveDurability: SaveDurability? = null
    var clearDurability: SaveDurability? = null

    override fun load(): GameSnapshot? = snapshot

    override fun save(snapshot: GameSnapshot, durability: SaveDurability): Boolean {
        this.snapshot = snapshot
        saveDurability = durability
        return true
    }

    override fun clear(durability: SaveDurability): Boolean {
        snapshot = null
        clearDurability = durability
        return true
    }
}

fun main() {
    val store = AuditStore()
    val controller = GameController(store, Game(seed = 42L))

    check(controller.state.value.status == GameStatus.Idle)
    check(controller.startOrJump() == GameInputResult.Started)
    check(store.saveDurability == SaveDurability.Immediate)

    store.saveDurability = null
    check(controller.startOrJump() == GameInputResult.Jumped)
    check(store.saveDurability == SaveDurability.Deferred)

    val scoringCheckpoint = GameSnapshot(
        status = GameStatus.Started,
        score = 0,
        bestScore = 0,
        roundStartBestScore = 0,
        beeVelocity = 0f,
        bee = com.msa.compose_kmm.domain.Bee(
            x = GameConfig.BEE_START_X,
            y = 200f,
            radius = GameConfig.BEE_RADIUS
        ),
        pipePairs = listOf(
            com.msa.compose_kmm.domain.PipePair(
                x = 20f,
                topHeight = 100f,
                gapHeight = GameConfig.PIPE_GAP,
                width = GameConfig.PIPE_WIDTH
            )
        ),
        randomState = 42L
    )
    val scoreStore = AuditStore().apply { snapshot = scoringCheckpoint }
    val scoreController = GameController(scoreStore, Game(seed = 11L))
    scoreStore.saveDurability = null
    check(scoreController.updateNanos(GameConfig.FIXED_TIME_STEP_NANOS))
    check(scoreController.state.value.score == 1)
    check(scoreStore.saveDurability == SaveDurability.Immediate)

    store.saveDurability = null
    repeat(2_000) {
        if (controller.state.value.status == GameStatus.Started) {
            controller.updateNanos(GameConfig.FIXED_TIME_STEP_NANOS)
        }
    }
    check(controller.state.value.status == GameStatus.Over)
    check(store.saveDurability == SaveDurability.Immediate)

    val persisted = checkNotNull(store.snapshot)
    val restored = GameController(store, Game(seed = 999L))
    check(restored.state.value == persisted)

    check(restored.clearSavedState())
    check(store.clearDurability == SaveDurability.Immediate)
    check(store.snapshot == null)

    val failingStore = object : GameStateStore {
        override fun load(): GameSnapshot? = throw IllegalStateException("load failure")
        override fun save(snapshot: GameSnapshot, durability: SaveDurability): Boolean =
            throw IllegalStateException("save failure")
        override fun clear(durability: SaveDurability): Boolean =
            throw IllegalStateException("clear failure")
    }
    val resilientController = GameController(failingStore, Game(seed = 77L))
    check(resilientController.startOrJump() == GameInputResult.Started)
    check(resilientController.state.value.status == GameStatus.Started)
    check(!resilientController.persist(SaveDurability.Immediate))
    check(!resilientController.clearSavedState(SaveDurability.Immediate))

    println("controller-runtime-audit-ok status=${persisted.status} score=${persisted.score}")
}
