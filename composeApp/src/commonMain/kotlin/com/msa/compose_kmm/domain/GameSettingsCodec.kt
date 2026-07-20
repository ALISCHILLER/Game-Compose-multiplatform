package com.msa.compose_kmm.domain

/** نسخه‌دار، کوچک و دارای checksum برای ذخیره تنظیمات در Key/Value storage. */
object GameSettingsCodec {
    private const val SEPARATOR = '|'
    private const val FIELD_COUNT = 9
    private const val MAX_ENCODED_LENGTH = 256

    fun encode(settings: GameSettings): String {
        val normalized = settings.normalized()
        val payload = listOf(
            GameSettings.CURRENT_SCHEMA_VERSION,
            normalized.soundEnabled.toFlag(),
            normalized.musicEnabled.toFlag(),
            normalized.effectsEnabled.toFlag(),
            normalized.musicVolume,
            normalized.effectsVolume,
            normalized.reduceMotion.toFlag(),
            normalized.showGameplayHints.toFlag()
        ).joinToString(SEPARATOR.toString())
        return "$payload$SEPARATOR${checksumHex(payload)}"
    }

    fun decode(value: String?): GameSettings? {
        if (value.isNullOrBlank() || value.length > MAX_ENCODED_LENGTH) return null
        return runCatching {
            val fields = value.split(SEPARATOR)
            require(fields.size == FIELD_COUNT)
            require(fields[0].toInt() == GameSettings.CURRENT_SCHEMA_VERSION)
            val payload = fields.take(FIELD_COUNT - 1).joinToString(SEPARATOR.toString())
            require(constantTimeEquals(checksumHex(payload), fields.last().lowercase()))

            GameSettings(
                soundEnabled = fields[1].toFlag(),
                musicEnabled = fields[2].toFlag(),
                effectsEnabled = fields[3].toFlag(),
                musicVolume = fields[4].toInt(),
                effectsVolume = fields[5].toInt(),
                reduceMotion = fields[6].toFlag(),
                showGameplayHints = fields[7].toFlag()
            ).normalized().takeIf(::isValid) ?: error("Invalid settings")
        }.getOrNull()
    }

    private fun isValid(settings: GameSettings): Boolean =
        settings.schemaVersion == GameSettings.CURRENT_SCHEMA_VERSION &&
            settings.musicVolume in GameSettings.MIN_VOLUME..GameSettings.MAX_VOLUME &&
            settings.effectsVolume in GameSettings.MIN_VOLUME..GameSettings.MAX_VOLUME

    private fun Boolean.toFlag(): Int = if (this) 1 else 0

    private fun String.toFlag(): Boolean {
        require(this == "0" || this == "1")
        return this == "1"
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
        return if (hash == 0L) 1L else hash
    }

    private const val FNV_OFFSET_BASIS: Long = -3750763034362895579L
    private const val FNV_PRIME: Long = 1099511628211L
}
