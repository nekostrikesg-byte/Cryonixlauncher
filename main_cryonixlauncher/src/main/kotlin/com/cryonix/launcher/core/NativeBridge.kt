package com.cryonix.launcher.core

/**
 * Small JNI boundary for native Cryonix functionality.
 *
 * Native code is optional for launcher startup. A missing native library
 * must not prevent the Kotlin launcher UI from opening.
 */
internal object NativeBridge {
    private var loaded = false

    private fun ensureLoaded() {
        if (loaded) return
        runCatching {
            System.loadLibrary("cryonix_native")
            loaded = true
        }
    }

    fun status(): String {
        ensureLoaded()
        if (!loaded) return "Native layer unavailable"

        return runCatching { nativeStatus() }
            .getOrElse { "Native layer unavailable" }
    }

    private external fun nativeStatus(): String
}
