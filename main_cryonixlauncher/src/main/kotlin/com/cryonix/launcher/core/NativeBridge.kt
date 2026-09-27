package com.cryonix.launcher.core

/**
 * Small JNI boundary for native Cryonix functionality.
 *
 * Keep platform/native implementation in src/main/cpp.
 */
internal object NativeBridge {
    init {
        System.loadLibrary("cryonix_native")
    }

    external fun status(): String
}
