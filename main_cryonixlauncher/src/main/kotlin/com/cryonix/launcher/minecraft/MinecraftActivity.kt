package com.cryonix.launcher.minecraft

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.WindowManager
import android.widget.TextView
import android.widget.Toast
import com.cryonix.launcher.R
import com.cryonix.launcher.minecraft.model.GameAccount
import com.cryonix.launcher.minecraft.model.LoaderProfile
import com.cryonix.launcher.minecraft.model.MinecraftVersion
import com.cryonix.launcher.minecraft.model.RendererProfile
import com.cryonix.launcher.accounts.AccountsActivity
import kotlin.concurrent.thread

class MinecraftActivity : Activity() {
    private lateinit var store: MinecraftSettingsStore
    private lateinit var versionText: TextView
    private lateinit var profileText: TextView
    private lateinit var statusText: TextView

    override fun onCreate(state: Bundle?) {
        super.onCreate(state)
        setContentView(R.layout.screen_minecraft)

        store = MinecraftSettingsStore(this)
        versionText = findViewById(R.id.minecraft_selected_version)
        profileText = findViewById(R.id.minecraft_selected_profile)
        statusText = findViewById(R.id.minecraft_status)

        if (store.keepScreenOn) {
            window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }

        findViewById<android.widget.Button>(R.id.minecraft_play).setOnClickListener {
            prepareLaunch()
        }
        findViewById<android.widget.Button>(R.id.minecraft_versions).setOnClickListener {
            showVersions()
        }
        findViewById<android.widget.Button>(R.id.minecraft_accounts).setOnClickListener {
            showAccounts()
        }
        findViewById<android.widget.Button>(R.id.minecraft_loader).setOnClickListener {
            showLoader()
        }
        findViewById<android.widget.Button>(R.id.minecraft_renderer).setOnClickListener {
            showRenderer()
        }
        findViewById<android.widget.Button>(R.id.minecraft_back).setOnClickListener {
            finish()
        }

        renderState()

        when {
            intent.getBooleanExtra(EXTRA_LAUNCH_NOW, false) -> prepareLaunch()
            intent.getStringExtra(EXTRA_SECTION) == "versions" -> showVersions()
            intent.getStringExtra(EXTRA_SECTION) == "accounts" -> showAccounts()
            intent.getStringExtra(EXTRA_SECTION) == "loader" -> showLoader()
            intent.getStringExtra(EXTRA_SECTION) == "renderer" -> showRenderer()
        }
    }

    private fun renderState() {
        versionText.text = "Version: " + (store.selectedVersionId ?: "not selected")
        profileText.text = "Profile: " + store.profileName
        statusText.text = "Loader: " + store.loader.lowercase() +
            " · Renderer: " + store.renderer.name.lowercase()
    }

    private fun showVersions() {
        statusText.text = "Loading official versions…"
        thread {
            val result = runCatching { VersionManifestService().fetchVersions() }
            runOnUiThread {
                result.onSuccess { versions ->
                    val releases = versions.filter { it.stable }
                    if (releases.isEmpty()) {
                        statusText.text = "No release versions found"
                        return@onSuccess
                    }
                    val names = releases.map { it.id }.toTypedArray()
                    val checked = releases.indexOfFirst { it.id == store.selectedVersionId }
                    AlertDialog.Builder(this)
                        .setTitle("Minecraft versions")
                        .setSingleChoiceItems(names, checked) { dialog, which ->
                            store.selectedVersionId = releases[which].id
                            statusText.text = "Selected " + releases[which].id
                            renderState()
                            dialog.dismiss()
                        }
                        .setNegativeButton("Cancel", null)
                        .show()
                }.onFailure {
                    statusText.text = "Could not load official metadata"
                    Toast.makeText(this, "Version metadata unavailable", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun showAccounts() {
        startActivity(Intent(this, AccountsActivity::class.java))
    }

    private fun showLoader() {
        val choices = arrayOf("Vanilla", "Fabric")
        val current = if (store.loader == "FABRIC") 1 else 0
        AlertDialog.Builder(this)
            .setTitle("Loader")
            .setSingleChoiceItems(choices, current) { dialog, which ->
                if (which == 0) {
                    store.loader = "VANILLA"
                    store.loaderVersion = ""
                    statusText.text = "Vanilla selected"
                    renderState()
                    dialog.dismiss()
                } else {
                    val gameVersion = store.selectedVersionId
                    if (gameVersion == null) {
                        Toast.makeText(this, "Select a Minecraft version first", Toast.LENGTH_SHORT).show()
                        return@setSingleChoiceItems
                    }
                    dialog.dismiss()
                    loadFabricVersions(gameVersion)
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun loadFabricVersions(gameVersion: String) {
        statusText.text = "Loading Fabric versions…"
        thread {
            val result = runCatching { FabricMetaService().fetchLoaderVersions(gameVersion) }
            runOnUiThread {
                result.onSuccess { versions ->
                    if (versions.isEmpty()) {
                        statusText.text = "No Fabric loader found for " + gameVersion
                        return@onSuccess
                    }
                    val names = versions.toTypedArray()
                    AlertDialog.Builder(this)
                        .setTitle("Fabric loader")
                        .setSingleChoiceItems(names, 0) { dialog, which ->
                            store.loader = "FABRIC"
                            store.loaderVersion = versions[which]
                            statusText.text = "Fabric " + versions[which] + " selected"
                            renderState()
                            dialog.dismiss()
                        }
                        .setNegativeButton("Cancel", null)
                        .show()
                }.onFailure {
                    statusText.text = "Fabric metadata unavailable"
                    Toast.makeText(this, "Could not load Fabric versions", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun showRenderer() {
        val renderers = RendererRegistry.builtIns()
        val names = renderers.map { it.backend.name.replace('_', ' ') }.toTypedArray()
        val current = renderers.indexOfFirst { it.backend == store.renderer }.coerceAtLeast(0)

        AlertDialog.Builder(this)
            .setTitle("Renderer")
            .setSingleChoiceItems(names, current) { dialog, which ->
                store.renderer = renderers[which].backend
                statusText.text = "Renderer: " + renderers[which].backend.name.lowercase()
                renderState()
                dialog.dismiss()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun prepareLaunch() {
        val versionId = store.selectedVersionId
        if (versionId == null) {
            statusText.text = "Select a Minecraft version first"
            showVersions()
            return
        }

        val version = MinecraftVersion(
            id = versionId,
            type = "release",
            url = "",
            sha1 = ""
        )
        val account = GameAccount(
            id = store.profileId ?: "local",
            name = store.profileName,
            type = GameAccount.Type.LOCAL,
            authenticated = false,
            ownsMinecraft = false
        )
        val loader = if (store.loader == "FABRIC") {
            LoaderProfile.fabric(store.loaderVersion)
        } else {
            LoaderProfile.vanilla()
        }
        val renderer = RendererProfile(
            backend = store.renderer,
            libraryId = "",
            version = "",
            enabled = true
        )

        val plan = runCatching {
            LaunchPlanBuilder().build(
                LaunchRequest(
                    version = version,
                    account = account,
                    loader = loader,
                    renderer = renderer,
                    memoryMb = 2048
                )
            )
        }

        plan.onSuccess {
            statusText.text = "Launch plan prepared"
            AlertDialog.Builder(this)
                .setTitle("Launch ready")
                .setMessage(
                    "Version: " + version.id + "\n" +
                        "Profile: " + account.name + "\n" +
                        "Loader: " + loader.type.name.lowercase() + "\n" +
                        "Renderer: " + renderer.backend.name.lowercase() + "\n\n" +
                        "Arguments:\n" + it.arguments.joinToString(" ") + "\n\n" +
                        "The native game-process/runtime integration is the next launch-engine stage."
                )
                .setPositiveButton("OK", null)
                .show()
        }.onFailure {
            statusText.text = "Launch plan failed"
            Toast.makeText(this, it.message ?: "Launch failed", Toast.LENGTH_SHORT).show()
        }
    }

    companion object {
        const val EXTRA_SECTION = "open_section"
        const val EXTRA_LAUNCH_NOW = "launch_now"
    }
}
