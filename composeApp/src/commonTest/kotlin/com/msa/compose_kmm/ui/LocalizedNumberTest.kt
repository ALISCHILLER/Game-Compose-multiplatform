package com.msa.compose_kmm.ui

import kotlin.test.Test
import kotlin.test.assertEquals

class LocalizedNumberTest {
    @Test
    fun keepsLatinDigitsForLtrLayouts() {
        assertEquals("-12045", localizeDigits(value = -12045, usePersianDigits = false))
    }

    @Test
    fun convertsOnlyDigitsForRtlLayouts() {
        assertEquals("-۱۲۰۴۵", localizeDigits(value = -12045, usePersianDigits = true))
    }
}
