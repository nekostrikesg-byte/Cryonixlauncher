package com.cryonix.launcher.runtime
import android.app.Activity
import android.os.Bundle
import android.view.Gravity
import android.widget.LinearLayout
import android.widget.TextView
class MinecraftEngineActivity : Activity() {
 override fun onCreate(savedInstanceState: Bundle?) {
  super.onCreate(savedInstanceState)
  val root=LinearLayout(this).apply { orientation=LinearLayout.VERTICAL; gravity=Gravity.CENTER; setPadding(32,32,32,32) }
  val native=runCatching { NativeMinecraftEngine(this) }.getOrNull()
  val text=TextView(this).apply { text=native?.status() ?: "Cryonix native engine unavailable"; textSize=16f; gravity=Gravity.CENTER }
  root.addView(text); setContentView(root)
 }
}
