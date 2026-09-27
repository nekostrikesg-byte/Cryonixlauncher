package com.cryonix.launcher.minecraft

import com.cryonix.launcher.minecraft.model.GameAccount
import com.cryonix.launcher.minecraft.model.LoaderProfile
import com.cryonix.launcher.minecraft.model.MinecraftVersion
import com.cryonix.launcher.minecraft.model.RendererProfile

data class LaunchRequest(
    val version: MinecraftVersion,
    val account: GameAccount,
    val loader: LoaderProfile,
    val renderer: RendererProfile,
    val memoryMb: Int
)
