package com.cryonix.launcher.minecraft

import java.io.File

data class MinecraftInstallResult(
    val versionId: String,
    val instanceName: String,
    val gameDirectory: File,
    val versionJson: File,
    val clientJar: File,
    val libraryCount: Int,
    val assetCount: Int,
    val totalBytes: Long
)
