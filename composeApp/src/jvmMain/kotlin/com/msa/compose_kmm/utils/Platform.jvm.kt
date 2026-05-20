package com.msa.compose_kmm.utils

//class JVMPlatform : Platform {
//    override val name: String = "Java ${System.getProperty("java.version")}"
//}

actual fun getPlatform(): Platform = Platform.Desktop