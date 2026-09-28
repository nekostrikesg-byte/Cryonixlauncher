package com.cryonix.launcher.minecraft

class MinecraftTaskControl {
    @Volatile var paused: Boolean = false
        private set

    @Volatile var stopped: Boolean = false
        private set

    fun pause() {
        if (!stopped) paused = true
    }

    fun resume() {
        if (!stopped) synchronized(this) {
            paused = false
            (this as java.lang.Object).notifyAll()
        }
    }

    fun stop() {
        stopped = true
        paused = false
        synchronized(this) {
            (this as java.lang.Object).notifyAll()
        }
    }

    fun awaitIfPaused() {
        synchronized(this) {
            while (paused && !stopped) {
                (this as java.lang.Object).wait(250L)
            }
        }
        if (stopped) throw InterruptedException("Task stopped")
    }
}
