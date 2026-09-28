package com.cryonix.launcher.runtime
import android.content.Context
class NativeMinecraftEngine(context: Context) {
 companion object { init { System.loadLibrary("cryonix_engine") } }
 fun status(): String = nativeGetStatus()
 fun isReady(): Boolean = nativeIsReady()
 private external fun nativeGetStatus(): String
 private external fun nativeIsReady(): Boolean
}
