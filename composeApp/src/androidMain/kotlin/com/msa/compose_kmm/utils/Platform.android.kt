package com.msa.compose_kmm.utils

//class AndroidPlatform : Platform {
//    override val name: String = "Android ${Build.VERSION.SDK_INT}"
//}

actual fun getPlatform(): Platform = Platform.Android