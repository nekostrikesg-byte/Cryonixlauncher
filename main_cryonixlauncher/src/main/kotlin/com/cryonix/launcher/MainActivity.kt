package com.cryonix.launcher

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import com.cryonix.launcher.minecraft.MinecraftActivity

class MainActivity : Activity() {
    override fun onCreate(state: Bundle?) {
        super.onCreate(state)
        setContentView(R.layout.activity_launcher)

        findViewById<Button>(R.id.all_apps_button).apply {
            text = "Minecraft"
            setOnClickListener {
                startActivity(Intent(this@MainActivity, MinecraftActivity::class.java))
            }
        }
    }
}
