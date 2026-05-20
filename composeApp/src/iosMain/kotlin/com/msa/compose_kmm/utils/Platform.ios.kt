package com.msa.compose_kmm.utils

//class IOSPlatform : Platform {
//    override val name: String = UIDevice.currentDevice.systemName() + " " + UIDevice.currentDevice.systemVersion
//}

actual fun getPlatform(): Platform = Platform.Ios