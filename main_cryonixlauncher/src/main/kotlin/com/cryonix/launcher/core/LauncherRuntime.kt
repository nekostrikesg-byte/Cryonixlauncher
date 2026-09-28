package com.cryonix.launcher.core

import com.cryonix.launcher.backends.CryonixBackends

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
    fun backendName(): String = CryonixBackends.NAME

    @JvmStatic
    fun backendId(): String = CryonixBackends.selected().id

    @JvmStatic
    fun describe(): String =
        "Cryonix Kotlin runtime active; backend: ${backendName()} (${backendId()}); native status: ${nativeStatus()}"
}
