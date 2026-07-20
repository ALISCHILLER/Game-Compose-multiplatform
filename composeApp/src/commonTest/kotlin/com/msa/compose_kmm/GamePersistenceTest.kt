package com.msa.compose_kmm

import com.msa.compose_kmm.domain.Game
import com.msa.compose_kmm.domain.GameSnapshot
import com.msa.compose_kmm.domain.GameSnapshotCodec
import com.msa.compose_kmm.domain.GameConfig
import com.msa.compose_kmm.domain.GameStateStore
import com.msa.compose_kmm.domain.GameStatus
import com.msa.compose_kmm.domain.SaveDurability
import com.msa.compose_kmm.presentation.GameController
import com.msa.compose_kmm.presentation.GameInputResult
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class GamePersistenceTest {
    @Test
    fun snapshotRoundTripPreservesCompleteState() {
        val game = Game(Random(7)).apply {
            start()
            jump()
            repeat(40) { updateNanos(8_333_333L) }
        }

        val encoded = GameSnapshotCodec.encode(game.snapshot())
        val decoded = GameSnapshotCodec.decode(encoded)

        assertEquals(game.snapshot(), decoded)
    }

    @Test
    fun newRecordBaselineSurvivesSnapshotRoundTrip() {
        val snapshot = GameSnapshot(
            status = GameStatus.Over,
            score = 6,
            bestScore = 6,
            roundStartBestScore = 5,
            beeVelocity = 0f,
            bee = com.msa.compose_kmm.domain.Bee(
                x = GameConfig.BEE_START_X,
                y = 200f,
                radius = GameConfig.BEE_RADIUS
            ),
            pipePairs = listOf(
                com.msa.compose_kmm.domain.PipePair(
                    x = 400f,
                    topHeight = 100f,
                    gapHeight = GameConfig.PIPE_GAP,
                    width = GameConfig.PIPE_WIDTH
                )
            ),
            randomState = 42L
        )

        val restored = assertNotNull(GameSnapshotCodec.decode(GameSnapshotCodec.encode(snapshot)))

        assertEquals(snapshot, restored)
        assertTrue(restored.isNewRecord)
    }

    @Test
    fun malformedSnapshotIsRejected() {
        assertNull(GameSnapshotCodec.decode("not-a-valid-snapshot"))
        assertNull(GameSnapshotCodec.decode(""))
    }

    @Test
    fun restoreResetsTimingDebtAndContinuesSafely() {
        val original = Game(Random(4)).apply {
            start()
            jump()
            repeat(20) { updateNanos(8_333_333L) }
        }
        val snapshot = original.snapshot()
        val restored = Game(Random(4))

        assertTrue(restored.restore(snapshot))
        assertEquals(snapshot, restored.snapshot())
        assertFalse(restored.updateNanos(1L))
        assertTrue(restored.updateNanos(8_333_333L))
    }


    @Test
    fun restoredGameContinuesWithTheSameFuturePipeSequence() {
        val checkpoint = GameSnapshot(
            status = GameStatus.Started,
            score = 0,
            bestScore = 0,
            beeVelocity = 0f,
            bee = com.msa.compose_kmm.domain.Bee(
                x = GameConfig.BEE_START_X,
                y = 200f,
                radius = GameConfig.BEE_RADIUS
            ),
            pipePairs = listOf(
                com.msa.compose_kmm.domain.PipePair(
                    x = -83f,
                    topHeight = 100f,
                    gapHeight = GameConfig.PIPE_GAP,
                    width = GameConfig.PIPE_WIDTH
                )
            ),
            randomState = 42L
        )
        val original = Game(seed = 1L)
        val restored = Game(seed = 999L)
        assertTrue(original.restore(checkpoint))
        assertTrue(restored.restore(checkpoint))

        original.updateNanos(GameConfig.FIXED_TIME_STEP_NANOS)
        restored.updateNanos(GameConfig.FIXED_TIME_STEP_NANOS)

        assertEquals(original.snapshot(), restored.snapshot())
        assertTrue(original.snapshot().randomState != checkpoint.randomState)
        assertEquals(1, original.pipePairs.size)
        assertTrue(original.pipePairs.single().x > GameConfig.WORLD_WIDTH)
    }


    @Test
    fun checksumRejectsAWellFormedButModifiedSnapshot() {
        val game = Game(Random(17)).apply {
            start()
            jump()
            repeat(12) { updateNanos(GameConfig.FIXED_TIME_STEP_NANOS) }
        }
        val encoded = GameSnapshotCodec.encode(game.snapshot())
        val fields = encoded.split('|').toMutableList()
        fields[4] = (fields[4].toFloat() + 1f).toString()
        val modifiedPayloadWithOriginalChecksum = fields.joinToString("|")

        assertNull(GameSnapshotCodec.decode(modifiedPayloadWithOriginalChecksum))
    }

    @Test
    fun legacyV3SnapshotMigratesConservativelyToTheCurrentSchema() {
        val legacy = "3|Started|0|0|-120.0|100.0|240.0|20.0|42|530.0,120.0,176.0,72.0,0|b83f011695056750"

        val migrated = assertNotNull(GameSnapshotCodec.decode(legacy))

        assertEquals(GameSnapshot.CURRENT_SCHEMA_VERSION, migrated.schemaVersion)
        assertEquals(migrated.bestScore, migrated.roundStartBestScore)
        assertFalse(migrated.isNewRecord)
    }

    @Test
    fun legacyV2SnapshotMigratesToTheCurrentSchema() {
        val legacy = "2|Started|0|0|-120.0|100.0|240.0|20.0|42|530.0,120.0,176.0,72.0,0"

        val migrated = assertNotNull(GameSnapshotCodec.decode(legacy))

        assertEquals(GameSnapshot.CURRENT_SCHEMA_VERSION, migrated.schemaVersion)
        assertEquals(42L, migrated.randomState)
        assertEquals(GameStatus.Started, migrated.status)
    }

    @Test
    fun legacyV1SnapshotMigratesToTheCurrentSchema() {
        val legacy = "1|Started|0|0|-120.0|100.0|240.0|20.0|530.0,120.0,176.0,72.0,0"

        val migrated = assertNotNull(GameSnapshotCodec.decode(legacy))

        assertEquals(GameSnapshot.CURRENT_SCHEMA_VERSION, migrated.schemaVersion)
        assertTrue(migrated.randomState != 0L)
        assertEquals(GameStatus.Started, migrated.status)
    }

    @Test
    fun controllerLoadsSavedGameAndPersistsUserActions() {
        val store = InMemoryGameStateStore()
        val initialController = GameController(store, Game(Random(7)))

        assertEquals(GameInputResult.Started, initialController.startOrJump())
        repeat(30) { initialController.updateNanos(8_333_333L) }
        initialController.persist()
        val saved = assertNotNull(store.snapshot)

        val restoredController = GameController(store, Game(Random(99)))

        assertEquals(saved, restoredController.state.value)
        assertEquals(GameStatus.Started, restoredController.state.value.status)
    }


    @Test
    fun controllerClearsAnInvalidStoredSnapshot() {
        val invalid = GameSnapshot(
            status = GameStatus.Started,
            score = 5,
            bestScore = 1,
            beeVelocity = 0f,
            bee = com.msa.compose_kmm.domain.Bee(x = 100f, y = 200f, radius = 20f),
            pipePairs = listOf(
                com.msa.compose_kmm.domain.PipePair(
                    x = 400f,
                    topHeight = 100f,
                    gapHeight = 176f,
                    width = 72f
                )
            )
        )
        val store = InMemoryGameStateStore().apply { snapshot = invalid }

        val controller = GameController(store, Game(Random(1)))

        assertNull(store.snapshot)
        assertEquals(GameStatus.Idle, controller.state.value.status)
        assertEquals(0, controller.state.value.score)
    }

    @Test
    fun controllerCreatesADeferredCheckpointForEveryJump() {
        val store = InMemoryGameStateStore()
        val controller = GameController(store = store)

        controller.startOrJump()
        store.savedDurabilities.clear()

        controller.startOrJump()

        assertEquals(listOf(SaveDurability.Deferred), store.savedDurabilities)
    }

    @Test
    fun controllerPersistsScoreChangesImmediately() {
        val checkpoint = GameSnapshot(
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
        val store = InMemoryGameStateStore().apply { snapshot = checkpoint }
        val controller = GameController(store, Game(seed = 5L))
        store.savedDurabilities.clear()

        controller.updateNanos(GameConfig.FIXED_TIME_STEP_NANOS)

        assertEquals(1, controller.state.value.score)
        assertEquals(listOf(SaveDurability.Immediate), store.savedDurabilities)
    }

    @Test
    fun resetProgressClearsSavedStateAndBestScore() {
        val savedProgress = GameSnapshot(
            status = GameStatus.Idle,
            score = 0,
            bestScore = 5,
            roundStartBestScore = 5,
            beeVelocity = 0f,
            bee = com.msa.compose_kmm.domain.Bee(
                x = GameConfig.BEE_START_X,
                y = GameConfig.BEE_START_Y,
                radius = GameConfig.BEE_RADIUS
            ),
            pipePairs = listOf(
                com.msa.compose_kmm.domain.PipePair(
                    x = GameConfig.WORLD_WIDTH + GameConfig.FIRST_PIPE_OFFSET,
                    topHeight = 100f,
                    gapHeight = GameConfig.PIPE_GAP,
                    width = GameConfig.PIPE_WIDTH
                )
            ),
            randomState = 31L
        )
        val store = InMemoryGameStateStore().apply { snapshot = savedProgress }
        val controller = GameController(store, Game(Random(31)))
        assertEquals(5, controller.state.value.bestScore)

        assertTrue(controller.resetProgress())
        assertNull(store.snapshot)
        assertEquals(GameStatus.Idle, controller.state.value.status)
        assertEquals(0, controller.state.value.score)
        assertEquals(0, controller.state.value.bestScore)
    }

    @Test
    fun controllerContainsStorageFailuresWithoutCrashingTheGame() {
        val controller = GameController(store = FailingStore())

        assertEquals(GameInputResult.Started, controller.startOrJump())
        assertEquals(GameStatus.Started, controller.state.value.status)
        assertEquals(false, controller.persist(SaveDurability.Immediate))
        assertEquals(false, controller.clearSavedState(SaveDurability.Immediate))
    }

    @Test
    fun controllerUsesImmediateDurabilityForCriticalTransitions() {
        val store = InMemoryGameStateStore()
        val controller = GameController(store, Game(Random(27)))

        controller.startOrJump()

        assertEquals(SaveDurability.Immediate, store.lastSaveDurability)
    }

    private class FailingStore : GameStateStore {
        override fun load(): GameSnapshot? = throw IllegalStateException("load failure")
        override fun save(snapshot: GameSnapshot, durability: SaveDurability): Boolean =
            throw IllegalStateException("save failure")
        override fun clear(durability: SaveDurability): Boolean =
            throw IllegalStateException("clear failure")
    }

    private class InMemoryGameStateStore : GameStateStore {
        var snapshot: GameSnapshot? = null
        var lastSaveDurability: SaveDurability? = null
        var lastClearDurability: SaveDurability? = null
        val savedDurabilities = mutableListOf<SaveDurability>()

        override fun load(): GameSnapshot? = snapshot

        override fun save(snapshot: GameSnapshot, durability: SaveDurability): Boolean {
            this.snapshot = snapshot
            lastSaveDurability = durability
            savedDurabilities += durability
            return true
        }

        override fun clear(durability: SaveDurability): Boolean {
            snapshot = null
            lastClearDurability = durability
            return true
        }
    }
}
