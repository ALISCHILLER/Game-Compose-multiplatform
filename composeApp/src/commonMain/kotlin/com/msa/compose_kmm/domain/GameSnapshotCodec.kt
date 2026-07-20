package com.msa.compose_kmm.domain

/**
 * Codec کوچک، نسخه‌دار و بدون وابستگی برای Key/Value storage هر پلتفرم.
 *
 * نسخه 4 علاوه بر checksum، رکورد ابتدای دور را ذخیره می‌کند تا تشخیص New Record
 * پس از Process Recreation دقیق بماند. نسخه‌های 1، 2 و 3 به‌صورت امن مهاجرت می‌شوند.
 */
object GameSnapshotCodec {
    private const val FIELD_SEPARATOR = '|'
    private const val PIPE_SEPARATOR = ';'
    private const val PIPE_FIELD_SEPARATOR = ','
    private const val MAX_ENCODED_LENGTH = 32_768
    private const val MAX_PIPE_COUNT = 32
    private const val MAX_SCORE = 10_000_000
    private const val LEGACY_SCHEMA_VERSION_1 = 1
    private const val LEGACY_SCHEMA_VERSION_2 = 2
    private const val LEGACY_SCHEMA_VERSION_3 = 3

    fun encode(snapshot: GameSnapshot): String {
        val pipes = snapshot.pipePairs.joinToString(PIPE_SEPARATOR.toString()) { pipe ->
            listOf(
                pipe.x,
                pipe.topHeight,
                pipe.gapHeight,
                pipe.width,
                if (pipe.scored) 1 else 0
            ).joinToString(PIPE_FIELD_SEPARATOR.toString())
        }

        val payload = listOf(
            GameSnapshot.CURRENT_SCHEMA_VERSION,
            snapshot.status.name,
            snapshot.score,
            snapshot.bestScore,
            snapshot.roundStartBestScore,
            snapshot.beeVelocity,
            snapshot.bee.x,
            snapshot.bee.y,
            snapshot.bee.radius,
            snapshot.randomState,
            pipes
        ).joinToString(FIELD_SEPARATOR.toString())

        return "$payload$FIELD_SEPARATOR${checksumHex(payload)}"
    }

    fun decode(value: String?): GameSnapshot? {
        if (value.isNullOrBlank() || value.length > MAX_ENCODED_LENGTH) return null

        return runCatching {
            val schemaVersion = value.substringBefore(FIELD_SEPARATOR).toInt()
            val snapshot = when (schemaVersion) {
                LEGACY_SCHEMA_VERSION_1 -> decodeLegacyV1(value)
                LEGACY_SCHEMA_VERSION_2 -> decodeLegacyV2(value)
                LEGACY_SCHEMA_VERSION_3 -> decodeLegacyV3(value)
                GameSnapshot.CURRENT_SCHEMA_VERSION -> decodeCurrent(value)
                else -> error("Unsupported game snapshot schema: $schemaVersion")
            }
            snapshot.takeIf(::isValid)
        }.getOrNull()
    }

    private fun decodeCurrent(value: String): GameSnapshot {
        val fields = value.split(FIELD_SEPARATOR, limit = 12)
        require(fields.size == 12)
        require(fields[0].toInt() == GameSnapshot.CURRENT_SCHEMA_VERSION)

        val payload = fields.take(11).joinToString(FIELD_SEPARATOR.toString())
        require(constantTimeEquals(checksumHex(payload), fields[11].lowercase()))

        return createSnapshot(
            fields = fields,
            roundStartBestScore = fields[4].toInt(),
            velocityIndex = 5,
            randomState = fields[9].toLong(),
            encodedPipes = fields[10]
        )
    }

    /** مهاجرت نسخه 3 که checksum داشت اما رکورد ابتدای دور را ذخیره نمی‌کرد. */
    private fun decodeLegacyV3(value: String): GameSnapshot {
        val fields = value.split(FIELD_SEPARATOR, limit = 11)
        require(fields.size == 11)
        require(fields[0].toInt() == LEGACY_SCHEMA_VERSION_3)

        val payload = fields.take(10).joinToString(FIELD_SEPARATOR.toString())
        require(constantTimeEquals(checksumHex(payload), fields[10].lowercase()))

        return createSnapshot(
            fields = fields,
            roundStartBestScore = inferLegacyRoundStartBestScore(
                status = GameStatus.valueOf(fields[1]),
                score = fields[2].toInt(),
                bestScore = fields[3].toInt()
            ),
            velocityIndex = 4,
            randomState = fields[8].toLong(),
            encodedPipes = fields[9]
        )
    }

    /** مهاجرت Snapshot نسخه 2 که State مولد تصادفی داشت ولی checksum نداشت. */
    private fun decodeLegacyV2(value: String): GameSnapshot {
        val fields = value.split(FIELD_SEPARATOR, limit = 10)
        require(fields.size == 10)
        require(fields[0].toInt() == LEGACY_SCHEMA_VERSION_2)

        return createSnapshot(
            fields = fields,
            roundStartBestScore = inferLegacyRoundStartBestScore(
                status = GameStatus.valueOf(fields[1]),
                score = fields[2].toInt(),
                bestScore = fields[3].toInt()
            ),
            velocityIndex = 4,
            randomState = fields[8].toLong(),
            encodedPipes = fields[9]
        )
    }

    /**
     * مهاجرت Snapshot نسخه 1. چون نسخه قدیمی State مولد تصادفی نداشت، یک Seed
     * پایدار از محتوای همان Snapshot مشتق می‌شود.
     */
    private fun decodeLegacyV1(value: String): GameSnapshot {
        val fields = value.split(FIELD_SEPARATOR, limit = 9)
        require(fields.size == 9)
        require(fields[0].toInt() == LEGACY_SCHEMA_VERSION_1)

        return createSnapshot(
            fields = fields,
            roundStartBestScore = inferLegacyRoundStartBestScore(
                status = GameStatus.valueOf(fields[1]),
                score = fields[2].toInt(),
                bestScore = fields[3].toInt()
            ),
            velocityIndex = 4,
            randomState = stableHash(value),
            encodedPipes = fields[8]
        )
    }

    private fun createSnapshot(
        fields: List<String>,
        roundStartBestScore: Int,
        velocityIndex: Int,
        randomState: Long,
        encodedPipes: String
    ): GameSnapshot {
        require(encodedPipes.isNotBlank())
        val pipeParts = encodedPipes.split(PIPE_SEPARATOR)
        require(pipeParts.size in 1..MAX_PIPE_COUNT)

        return GameSnapshot(
            schemaVersion = GameSnapshot.CURRENT_SCHEMA_VERSION,
            status = GameStatus.valueOf(fields[1]),
            score = fields[2].toInt(),
            bestScore = fields[3].toInt(),
            roundStartBestScore = roundStartBestScore,
            beeVelocity = fields[velocityIndex].toFloat(),
            bee = Bee(
                x = fields[velocityIndex + 1].toFloat(),
                y = fields[velocityIndex + 2].toFloat(),
                radius = fields[velocityIndex + 3].toFloat()
            ),
            pipePairs = pipeParts.map(::decodePipe),
            randomState = normalizeHash(randomState)
        )
    }

    @Suppress("UNUSED_PARAMETER")
    private fun inferLegacyRoundStartBestScore(
        status: GameStatus,
        score: Int,
        bestScore: Int
    ): Int {
        // Older schemas did not retain the pre-round baseline. Choosing bestScore is
        // deliberately conservative: migration never announces a false new record.
        return bestScore.coerceAtLeast(0)
    }

    private fun decodePipe(encodedPipe: String): PipePair {
        val pipeFields = encodedPipe.split(PIPE_FIELD_SEPARATOR)
        require(pipeFields.size == 5)
        require(pipeFields[4] == "0" || pipeFields[4] == "1")

        return PipePair(
            x = pipeFields[0].toFloat(),
            topHeight = pipeFields[1].toFloat(),
            gapHeight = pipeFields[2].toFloat(),
            width = pipeFields[3].toFloat(),
            scored = pipeFields[4] == "1"
        )
    }

    private fun isValid(snapshot: GameSnapshot): Boolean {
        if (snapshot.schemaVersion != GameSnapshot.CURRENT_SCHEMA_VERSION) return false
        if (snapshot.score !in 0..MAX_SCORE) return false
        if (snapshot.bestScore !in snapshot.score..MAX_SCORE) return false
        if (snapshot.roundStartBestScore !in 0..snapshot.bestScore) return false
        if (!snapshot.beeVelocity.isFinite()) return false
        if (snapshot.beeVelocity !in -GameConfig.MAX_VERTICAL_VELOCITY..GameConfig.MAX_VERTICAL_VELOCITY) {
            return false
        }
        if (!snapshot.bee.x.isFinite() || snapshot.bee.x !in 0f..GameConfig.WORLD_WIDTH) return false
        if (!snapshot.bee.y.isFinite() || snapshot.bee.y !in 0f..GameConfig.WORLD_HEIGHT) return false
        if (!snapshot.bee.radius.isFinite() || snapshot.bee.radius !in 1f..GameConfig.WORLD_WIDTH) return false
        if (snapshot.randomState == 0L) return false
        if (snapshot.pipePairs.size !in 1..MAX_PIPE_COUNT) return false

        val playableBottom = GameConfig.WORLD_HEIGHT - GameConfig.GROUND_HEIGHT
        val minimumPipeX = -GameConfig.WORLD_WIDTH * 3f
        val maximumPipeX = GameConfig.WORLD_WIDTH + GameConfig.PIPE_SPACING * MAX_PIPE_COUNT

        return snapshot.pipePairs.all { pipe ->
            pipe.x.isFinite() && pipe.x in minimumPipeX..maximumPipeX &&
                pipe.topHeight.isFinite() && pipe.topHeight in 0f..playableBottom &&
                pipe.gapHeight.isFinite() && pipe.gapHeight > 0f &&
                pipe.width.isFinite() && pipe.width > 0f &&
                pipe.gapBottom <= playableBottom
        }
    }

    private fun checksumHex(payload: String): String =
        stableHash(payload).toULong().toString(radix = 16).padStart(16, '0')

    private fun constantTimeEquals(expected: String, actual: String): Boolean {
        if (expected.length != actual.length) return false
        var difference = 0
        expected.indices.forEach { index ->
            difference = difference or (expected[index].code xor actual[index].code)
        }
        return difference == 0
    }

    private fun stableHash(value: String): Long {
        var hash = FNV_OFFSET_BASIS
        value.forEach { character ->
            hash = hash xor character.code.toLong()
            hash *= FNV_PRIME
        }
        return normalizeHash(hash)
    }

    private fun normalizeHash(value: Long): Long =
        if (value == 0L) GameSnapshot.DEFAULT_RANDOM_STATE else value

    private const val FNV_OFFSET_BASIS: Long = -3750763034362895579L
    private const val FNV_PRIME: Long = 1099511628211L
}
