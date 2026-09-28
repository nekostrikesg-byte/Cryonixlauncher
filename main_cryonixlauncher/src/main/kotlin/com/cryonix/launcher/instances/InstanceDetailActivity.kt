package com.cryonix.launcher.instances

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import com.cryonix.launcher.R
import com.cryonix.launcher.minecraft.MinecraftSettingsStore

class InstanceDetailActivity : Activity() {
    private lateinit var store: MinecraftSettingsStore
    private lateinit var name: String

    override fun onCreate(state: Bundle?) {
        super.onCreate(state)
        setContentView(R.layout.screen_instance_detail)
        store = MinecraftSettingsStore(this)
        name = intent.getStringExtra(EXTRA_NAME).orEmpty()
        findViewById<View>(R.id.instance_detail_back).setOnClickListener { finish() }
        findViewById<View>(R.id.instance_detail_edit).setOnClickListener {
            startActivity(Intent(this, NewInstanceActivity::class.java))
        }
        findViewById<View>(R.id.instance_detail_duplicate).setOnClickListener { duplicate() }
        findViewById<View>(R.id.instance_detail_delete).setOnClickListener { delete() }
        findViewById<View>(R.id.instance_detail_servers).setOnClickListener {
            startActivity(Intent(this, ServerHubActivity::class.java).putExtra(ServerHubActivity.EXTRA_INSTANCE, name))
        }
        findViewById<View>(R.id.instance_detail_launch).setOnClickListener {
            AlertDialog.Builder(this).setTitle("Launch unavailable")
                .setMessage("The current branch has no connected Minecraft process/backend launcher. The instance configuration is saved and ready for the launch adapter.")
                .setPositiveButton("OK", null).show()
        }
        render()
    }

    override fun onResume() {
        super.onResume()
        if (::store.isInitialized) render()
    }

    private fun render() {
        val c = store.instanceConfig(name)
        findViewById<TextView>(R.id.instance_detail_title).text = name
        findViewById<TextView>(R.id.instance_detail_summary).text =
            "Version: " + (c.versionId ?: "Not selected") +
            "\nLoader: " + c.loader + if (c.loaderVersion.isBlank()) "" else " " + c.loaderVersion +
            "\nJava: " + c.javaRuntime + "   •   RAM: " + c.memoryMb + " MB" +
            "\nResolution: " + c.resolutionRule +
            "\nServers: " + store.servers(name).size
    }

    private fun duplicate() {
        val input = android.widget.EditText(this).apply { hint = "New instance name"; setSingleLine(true) }
        AlertDialog.Builder(this).setTitle("Duplicate instance").setView(input)
            .setPositiveButton("Duplicate") { _, _ ->
                if (store.duplicateInstance(name, input.text.toString())) render()
                else input.error = "Name already exists or is invalid"
            }.setNegativeButton("Cancel", null).show()
    }

    private fun delete() {
        AlertDialog.Builder(this).setTitle("Delete instance?").setMessage(name)
            .setPositiveButton("Delete") { _, _ -> store.removeInstance(name); finish() }
            .setNegativeButton("Cancel", null).show()
    }

    companion object { const val EXTRA_NAME = "instance_name" }
}
