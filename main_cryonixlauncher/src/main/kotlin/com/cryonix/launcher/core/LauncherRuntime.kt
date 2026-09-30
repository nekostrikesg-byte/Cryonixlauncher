package com.cryonix.launcher.core

/**
 * Kotlin-owned launcher runtime state.
 * The launcher UI and download pipeline stay independent from the native engine.
 */
object LauncherRuntime {
    @JvmStatic
    fun nativeStatus(): String = NativeBridge.status()

    @JvmStatic
    fun backendName(): String = "Pojav/Mojo-compatible Android runtime"

    @JvmStatic
    fun backendId(): String = "pojav"

    @JvmStatic
    fun describe(): String =
        "Cryonix Android runtime active; backend: " + backendName() + "; native status: " + nativeStatus()
}
