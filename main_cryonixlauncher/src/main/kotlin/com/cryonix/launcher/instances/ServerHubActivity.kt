package com.cryonix.launcher.instances

import android.app.Activity
import android.app.AlertDialog
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import com.cryonix.launcher.R
import com.cryonix.launcher.minecraft.MinecraftSettingsStore
import java.net.InetSocketAddress
import java.net.Socket
import java.util.UUID
import kotlin.concurrent.thread

class ServerHubActivity : Activity() {
    private lateinit var store: MinecraftSettingsStore
    private lateinit var instance: String
    private lateinit var list: LinearLayout

    override fun onCreate(state: Bundle?) {
        super.onCreate(state)
        setContentView(R.layout.screen_server_hub)
        store = MinecraftSettingsStore(this)
        instance = intent.getStringExtra(EXTRA_INSTANCE).orEmpty()
        list = findViewById(R.id.server_hub_list)
        findViewById<View>(R.id.server_hub_back).setOnClickListener { finish() }
        findViewById<View>(R.id.server_hub_add).setOnClickListener { editServer(null) }
        findViewById<TextView>(R.id.server_hub_title).text = "Server Hub • " + instance
        render()
    }

    private fun render() {
        list.removeAllViews()
        store.servers(instance).forEach { server ->
            val row = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
                setPadding(dp(12), dp(8), dp(8), dp(8))
                setBackgroundResource(R.drawable.bg_cryonix_row)
            }
            val info = TextView(this).apply {
                layoutParams = LinearLayout.LayoutParams(0, dp(58), 1f)
                text = server.name + "\n" + server.address + ":" + server.port + "\nChecking…"
                textSize = 11f
                setTextColor(getColor(R.color.cryonix_text))
                gravity = Gravity.CENTER_VERTICAL
            }
            val refresh = TextView(this).apply {
                layoutParams = LinearLayout.LayoutParams(dp(42), dp(42)).apply { marginStart = dp(6) }
                text = "↻"; gravity = Gravity.CENTER; textSize = 18f
                setTextColor(getColor(R.color.cryonix_text_secondary)); setBackgroundResource(R.drawable.bg_cryonix_row)
                setOnClickListener { check(info, server.address, server.port) }
            }
            val edit = TextView(this).apply {
                layoutParams = LinearLayout.LayoutParams(dp(42), dp(42)).apply { marginStart = dp(5) }
                text = "⋯"; gravity = Gravity.CENTER; textSize = 18f
                setTextColor(getColor(R.color.cryonix_text_secondary)); setBackgroundResource(R.drawable.bg_cryonix_row)
                setOnClickListener { editServer(server) }
            }
            val remove = TextView(this).apply {
                layoutParams = LinearLayout.LayoutParams(dp(42), dp(42)).apply { marginStart = dp(5) }
                text = "×"; gravity = Gravity.CENTER; textSize = 18f
                setTextColor(getColor(R.color.cryonix_text_secondary)); setBackgroundResource(R.drawable.bg_cryonix_row)
                setOnClickListener { store.removeServer(instance, server.id); render() }
            }
            row.addView(info); row.addView(refresh); row.addView(edit); row.addView(remove)
            list.addView(row, LinearLayout.LayoutParams(-1, dp(74)).apply { topMargin = dp(7) })
            check(info, server.address, server.port)
        }
    }

    private fun editServer(existing: MinecraftSettingsStore.ServerEntry?) {
        val box = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(dp(8), 0, dp(8), 0) }
        val name = EditText(this).apply { hint = "Server name"; setSingleLine(true); setText(existing?.name.orEmpty()) }
        val address = EditText(this).apply { hint = "Address"; setSingleLine(true); setText(existing?.address.orEmpty()) }
        val port = EditText(this).apply { hint = "Port"; setSingleLine(true); inputType = android.text.InputType.TYPE_CLASS_NUMBER; setText((existing?.port ?: 25565).toString()) }
        box.addView(name); box.addView(address); box.addView(port)
        AlertDialog.Builder(this).setTitle(if (existing == null) "Add server" else "Edit server").setView(box)
            .setPositiveButton("Save") { _, _ ->
                val cleanName = name.text.toString().trim()
                val cleanAddress = address.text.toString().trim()
                val cleanPort = port.text.toString().toIntOrNull() ?: 25565
                if (cleanName.isNotBlank() && cleanAddress.isNotBlank() && cleanPort in 1..65535) {
                    store.addServer(instance, MinecraftSettingsStore.ServerEntry(existing?.id ?: UUID.randomUUID().toString(), cleanName, cleanAddress, cleanPort))
                    render()
                }
            }.setNegativeButton("Cancel", null).show()
    }

    private fun check(view: TextView, host: String, port: Int) {
        view.text = view.text.toString().substringBeforeLast("\n") + "\n" + host + ":" + port + "\nChecking…"
        thread {
            val ok = runCatching {
                Socket().use { socket -> socket.connect(InetSocketAddress(host, port), 1800) }
                true
            }.getOrDefault(false)
            runOnUiThread {
                view.text = view.text.toString().substringBeforeLast("\n") + "\n" + host + ":" + port + "\n" + if (ok) "Reachable" else "Offline / blocked"
            }
        }
    }

    private fun dp(v: Int) = (v * resources.displayMetrics.density).toInt()
    companion object { const val EXTRA_INSTANCE = "instance_name" }
}
