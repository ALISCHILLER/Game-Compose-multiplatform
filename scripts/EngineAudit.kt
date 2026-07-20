package com.msa.audit

import com.msa.compose_kmm.domain.Game
import com.msa.compose_kmm.domain.GameSnapshot
import com.msa.compose_kmm.domain.GameSnapshotCodec
import com.msa.compose_kmm.domain.GameStatus

private data class Result(
    val beeY: Float,
    val pipeX: Float,
    val status: GameStatus,
    val score: Int
)

private fun simulateOneSecond(refreshRate: Int): Result {
    val game = Game(seed = 42L).apply {
        start()
        jump()
    }
    var previousFrameNanos = 0L
    for (frame in 1..refreshRate) {
        val currentFrameNanos = frame * 1_000_000_000L / refreshRate
        game.updateNanos(currentFrameNanos - previousFrameNanos)
        previousFrameNanos = currentFrameNanos
    }
    val snapshot = game.snapshot()
    return Result(
        beeY = snapshot.bee.y,
        pipeX = snapshot.pipePairs.first().x,
        status = snapshot.status,
        score = snapshot.score
    )
}

fun main() {
    val baseline = simulateOneSecond(120)
    listOf(30, 60, 90, 120, 144, 165, 240).forEach { refreshRate ->
        check(simulateOneSecond(refreshRate) == baseline) {
            "Fixed timestep mismatch at ${refreshRate}Hz"
        }
    }

    val original = Game(seed = 1234L).apply {
        start()
        jump()
        repeat(80) { updateNanos(8_333_333L) }
    }
    val encoded = GameSnapshotCodec.encode(original.snapshot())
    val decoded = checkNotNull(GameSnapshotCodec.decode(encoded))
    check(decoded.schemaVersion == GameSnapshot.CURRENT_SCHEMA_VERSION)

    val checksumFields = encoded.split('|').toMutableList()
    checksumFields[5] = (checksumFields[5].toFloat() + 1f).toString()
    val checksumCorruption = checksumFields.joinToString("|")
    check(GameSnapshotCodec.decode(checksumCorruption) == null)

    val legacyV3 = "3|Started|0|0|-120.0|100.0|240.0|20.0|42|530.0,120.0,176.0,72.0,0|b83f011695056750"
    val migratedV3 = checkNotNull(GameSnapshotCodec.decode(legacyV3))
    check(migratedV3.schemaVersion == GameSnapshot.CURRENT_SCHEMA_VERSION)
    check(migratedV3.roundStartBestScore == migratedV3.bestScore)

    val legacyV2 = "2|Started|0|0|-120.0|100.0|240.0|20.0|42|530.0,120.0,176.0,72.0,0"
    check(checkNotNull(GameSnapshotCodec.decode(legacyV2)).schemaVersion == GameSnapshot.CURRENT_SCHEMA_VERSION)

    val restoredA = Game(seed = 1L).also { check(it.restore(decoded)) }
    val restoredB = Game(seed = 999L).also { check(it.restore(decoded)) }
    repeat(300) {
        restoredA.updateNanos(8_333_333L)
        restoredB.updateNanos(8_333_333L)
    }
    check(restoredA.snapshot() == restoredB.snapshot())

    println("engine-runtime-audit-ok baseline=$baseline schema=${decoded.schemaVersion}")
}
