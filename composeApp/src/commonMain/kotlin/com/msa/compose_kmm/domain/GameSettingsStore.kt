package com.msa.compose_kmm.domain

/** ذخیره دائمی تنظیمات کاربر؛ هر تغییر باید پیش از بازگشت ثبت شود. */
interface GameSettingsStore {
    fun load(): GameSettings?
    fun save(settings: GameSettings): Boolean
    fun clear(): Boolean
}
