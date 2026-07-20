package com.msa.compose_kmm.presentation

import com.msa.compose_kmm.domain.Game
import com.msa.compose_kmm.domain.GameSnapshot
import com.msa.compose_kmm.domain.GameStateStore
import com.msa.compose_kmm.domain.GameStatus
import com.msa.compose_kmm.domain.SaveDurability
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * State holder مشترک بازی با جریان یک‌طرفه داده.
 *
 * موتور Mutable داخل این کلاس محصور است و UI فقط Snapshotهای immutable دریافت می‌کند.
 * ذخیره‌های حیاتی مثل تغییر امتیاز، Game Over، Restart و خروج به‌صورت Immediate نوشته
 * می‌شوند؛ ذخیره‌ی دوره‌ای حین بازی Deferred است تا Frame اصلی مسدود نشود.
 */
class GameController(
    private val store: GameStateStore,
    private val game: Game = Game()
) {
    private val _state = MutableStateFlow(restoreOrCreateInitialState())
    val state: StateFlow<GameSnapshot> = _state.asStateFlow()

    fun updateNanos(deltaNanos: Long): Boolean {
        val previousScore = game.score
        val previousStatus = game.status
        val changed = game.updateNanos(deltaNanos)
        if (!changed) return false

        publish()

        val scoreChanged = game.score != previousScore
        val statusChanged = game.status != previousStatus
        if (scoreChanged || statusChanged) {
            persist(
                durability = SaveDurability.Immediate
            )
        }
        return true
    }

    fun startOrJump(): GameInputResult {
        return when (game.status) {
            GameStatus.Idle -> {
                game.start()
                game.jump()
                publishAndPersist(SaveDurability.Immediate)
                GameInputResult.Started
            }

            GameStatus.Started -> {
                game.jump()
                publishAndPersist(SaveDurability.Deferred)
                GameInputResult.Jumped
            }

            GameStatus.Over -> GameInputResult.Ignored
        }
    }

    fun restart(): GameInputResult {
        game.restart()
        game.jump()
        publishAndPersist(SaveDurability.Immediate)
        return GameInputResult.Restarted
    }

    fun resetProgress(): Boolean {
        game.resetProgress()
        publish()
        return clearSavedState(SaveDurability.Immediate)
    }

    fun persist(durability: SaveDurability = SaveDurability.Deferred): Boolean =
        runCatching { store.save(game.snapshot(), durability) }.getOrDefault(false)

    fun clearSavedState(durability: SaveDurability = SaveDurability.Immediate): Boolean =
        runCatching { store.clear(durability) }.getOrDefault(false)

    private fun restoreOrCreateInitialState(): GameSnapshot {
        val restored = runCatching { store.load() }.getOrNull()
        if (restored == null || !game.restore(restored)) {
            clearSavedState(SaveDurability.Immediate)
        }
        return game.snapshot()
    }

    private fun publish() {
        _state.value = game.snapshot()
    }

    private fun publishAndPersist(durability: SaveDurability) {
        publish()
        persist(durability)
    }
}

enum class GameInputResult {
    Started,
    Jumped,
    Restarted,
    Ignored
}
