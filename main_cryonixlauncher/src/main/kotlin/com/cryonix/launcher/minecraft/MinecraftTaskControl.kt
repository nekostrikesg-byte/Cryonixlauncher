package com.cryonix.launcher.minecraft

class MinecraftTaskControl {
    private val lock = Any()

    @Volatile var paused: Boolean = false
        private set

    @Volatile var stopped: Boolean = false
        private set

    fun pause() {
        if (!stopped) paused = true
    }

    fun resume() {
        if (!stopped) {
            paused = false
            synchronized(lock) {
                lock.notifyAll()
            }
        }
    }

    fun stop() {
        stopped = true
        paused = false
        synchronized(lock) {
            lock.notifyAll()
        }
    }

    fun awaitIfPaused() {
        synchronized(lock) {
            while (paused && !stopped) {
                lock.wait(250L)
            }
        }
        if (stopped) throw InterruptedException("Task stopped")
    }
}
