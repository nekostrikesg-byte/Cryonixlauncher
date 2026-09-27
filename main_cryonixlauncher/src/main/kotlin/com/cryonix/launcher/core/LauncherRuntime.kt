package com.cryonix.launcher.core

/**
 * Kotlin-owned launcher runtime state.
 *
 * Kotlin is responsible for new launcher logic, state, configuration,
 * and orchestration. Existing Java classes remain in the Java source set
 * for Android/JVM compatibility.
 */
object LauncherRuntime {
    @JvmStatic
    fun nativeStatus(): String = NativeBridge.status()

    @JvmStatic
    fun describe(): String =
        "Cryonix Kotlin runtime active; native status: ${nativeStatus()}"
}
