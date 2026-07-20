package com.msa.compose_kmm.domain

/**
 * وضعیت‌های اصلی بازی.
 */
enum class GameStatus {

    /** بازی هنوز شروع نشده است. */
    Idle,

    /** بازی در حال اجراست. */
    Started,

    /** بازی تمام شده است. */
    Over
}