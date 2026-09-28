package com.cryonix.launcher.instances

import android.app.Activity
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import com.cryonix.launcher.R
import com.cryonix.launcher.minecraft.FabricMetaService
import com.cryonix.launcher.minecraft.MinecraftSettingsStore
import com.cryonix.launcher.minecraft.VersionManifestService
import kotlin.concurrent.thread

class NewInstanceActivity : Activity() {
    private lateinit var store: MinecraftSettingsStore
    private lateinit var version: Spinner
    private lateinit var loader: Spinner
    private lateinit var loaderVersion: Spinner
    private lateinit var memory: Spinner
    private lateinit var resolution: Spinner
    private lateinit var status: TextView
    private var editingName: String? = null

    override fun onCreate(state: Bundle?) {
        super.onCreate(state)
        setContentView(R.layout.screen_new_instance)
        store = MinecraftSettingsStore(this)
        version = findViewById(R.id.new_instance_version)
        loader = findViewById(R.id.new_instance_loader)
        loaderVersion = findViewById(R.id.new_instance_loader_version)
        memory = findViewById(R.id.new_instance_memory)
        resolution = findViewById(R.id.new_instance_resolution)
        status = findViewById(R.id.new_instance_status)
        editingName = intent.getStringExtra(EXTRA_NAME)
        editingName?.let {
            findViewById<EditText>(R.id.new_instance_name).setText(it)
            findViewById<EditText>(R.id.new_instance_name).isEnabled = false
        }

        findViewById<View>(R.id.new_instance_back).setOnClickListener { finish() }
        findViewById<View>(R.id.new_instance_create).setOnClickListener { create() }
        loader.onItemSelectedListener = object : android.widget.AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: android.widget.AdapterView<*>?) = Unit
            override fun onItemSelected(parent: android.widget.AdapterView<*>?, view: View?, position: Int, id: Long) {
                if (position == 1) loadFabricLoaders(version.selectedItem?.toString().orEmpty())
                else setSpinner(loaderVersion, listOf("Not required"))
            }
        }
        setSpinner(loader, listOf("Vanilla", "Fabric"))
        setSpinner(memory, listOf("1024 MB", "1536 MB", "2048 MB", "3072 MB", "4096 MB"))
        setSpinner(resolution, listOf("Percentage · 100%", "Percentage · 75%", "Percentage · 50%"))
        loadVersions()
    }

    private fun loadVersions() {
        status.text = "Loading Mojang versions…"
        thread {
            val result = runCatching { VersionManifestService().fetchVersions().filter { it.stable }.map { it.id } }
            runOnUiThread {
                result.onSuccess {
                    val values = if (it.isEmpty()) listOf(store.selectedVersionId ?: "No versions found") else it
                    setSpinner(version, values)
                    store.selectedVersionId?.let { selected -> version.setSelection(values.indexOf(selected).coerceAtLeast(0)) }
                    status.text = if (it.isEmpty()) "No release versions returned" else "Version metadata ready"
                }.onFailure { status.text = "Could not load versions: " + (it.message ?: "network error") }
            }
        }
    }

    private fun loadFabricLoaders(gameVersion: String) {
        if (gameVersion.isBlank() || gameVersion == "No versions found") return
        status.text = "Loading Fabric loaders…"
        thread {
            val result = runCatching { FabricMetaService().fetchLoaderVersions(gameVersion) }
            runOnUiThread {
                result.onSuccess {
                    setSpinner(loaderVersion, if (it.isEmpty()) listOf("No compatible loader") else it)
                    status.text = "Fabric metadata ready"
                }.onFailure {
                    setSpinner(loaderVersion, listOf("Unavailable"))
                    status.text = "Fabric metadata unavailable"
                }
            }
        }
    }

    private fun create() {
        val name = findViewById<EditText>(R.id.new_instance_name).text.toString().trim()
        if (name.isEmpty()) {
            findViewById<EditText>(R.id.new_instance_name).error = "Instance name required"
            return
        }
        if (editingName == null && !store.addInstance(name)) {
            findViewById<EditText>(R.id.new_instance_name).error = "Name is empty or already exists"
            return
        }
        val selectedLoader = if (loader.selectedItemPosition == 1) "FABRIC" else "VANILLA"
        val selectedLoaderVersion = loaderVersion.selectedItem?.toString().orEmpty().takeUnless {
            it == "Not required" || it == "Unavailable" || it == "No compatible loader"
        } ?: ""
        val selectedMemory = memory.selectedItem.toString().substringBefore(" ").toIntOrNull() ?: 2048
        val selectedResolution = resolution.selectedItem.toString()
        store.saveInstanceConfig(
            store.instanceConfig(name).copy(
                versionId = version.selectedItem?.toString()?.takeUnless { it == "No versions found" },
                loader = selectedLoader,
                loaderVersion = selectedLoaderVersion,
                javaRuntime = "Automatic",
                memoryMb = selectedMemory,
                resolutionRule = selectedResolution
            )
        )
        setResult(RESULT_OK)
        finish()
    }

    companion object { const val EXTRA_NAME = "edit_instance_name" }

    private fun setSpinner(spinner: Spinner, values: List<String>) {
        spinner.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, values)
    }
}
