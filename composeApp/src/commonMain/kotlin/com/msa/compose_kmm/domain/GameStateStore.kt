package com.msa.compose_kmm.domain

/**
 * سطح دوام موردنیاز برای نوشتن Snapshot.
 *
 * [Deferred] برای ذخیره‌های دوره‌ای مناسب است و اجازه می‌دهد پلتفرم نوشتن را
 * غیرمسدودکننده انجام دهد. [Immediate] برای خروج، Background، تغییر امتیاز و
 * Game Over استفاده می‌شود؛ یعنی نقاطی که از دست‌رفتن State قابل قبول نیست.
 */
enum class SaveDurability {
    Deferred,
    Immediate
}

/** قرارداد ذخیره‌سازی کوچک برای Snapshot بازی؛ پیاده‌سازی در هر Target قرار دارد. */
interface GameStateStore {
    fun load(): GameSnapshot?
    fun save(snapshot: GameSnapshot, durability: SaveDurability = SaveDurability.Deferred): Boolean
    fun clear(durability: SaveDurability = SaveDurability.Deferred): Boolean
}
