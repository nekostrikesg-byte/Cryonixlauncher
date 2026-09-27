package com.cryonix.launcher.minecraft

import android.app.Activity
import android.os.Bundle

class MinecraftActivity : Activity() {
    override fun onCreate(state: Bundle?) {
        super.onCreate(state)
        setContentView(R.layout.screen_minecraft)
    }
}
