package com.cryonix.launcher.core

/**
 * Kotlin-owned application information and runtime capabilities.
 */
data class LauncherInfo(
    val name: String = "Cryonix Launcher",
    val version: String = "0.1.0",
    val kotlinLayer: Boolean = true,
    val nativeLayer: Boolean = true
)
