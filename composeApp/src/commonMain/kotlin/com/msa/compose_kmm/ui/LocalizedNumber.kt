package com.msa.compose_kmm.ui

import androidx.compose.runtime.Composable
import compose_kmm.composeapp.generated.resources.Res
import compose_kmm.composeapp.generated.resources.use_persian_digits
import org.jetbrains.compose.resources.stringResource

private const val LATIN_DIGITS = "0123456789"
private const val PERSIAN_DIGITS = "۰۱۲۳۴۵۶۷۸۹"

/** Pure digit-localization helper, independently testable outside Compose. */
fun localizeDigits(value: Int, usePersianDigits: Boolean): String {
    val raw = value.toString()
    if (!usePersianDigits) return raw

    return buildString(raw.length) {
        raw.forEach { character ->
            val index = LATIN_DIGITS.indexOf(character)
            append(if (index >= 0) PERSIAN_DIGITS[index] else character)
        }
    }
}

/**
 * Uses the active string-resource locale rather than layout direction.
 * This prevents unrelated RTL locales from receiving Persian digits.
 */
@Composable
fun localizedNumber(value: Int): String = localizeDigits(
    value = value,
    usePersianDigits = stringResource(Res.string.use_persian_digits).toBoolean()
)
