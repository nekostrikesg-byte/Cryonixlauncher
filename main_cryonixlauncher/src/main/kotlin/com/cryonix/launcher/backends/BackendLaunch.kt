package com.cryonix.launcher.backends

data class BackendLaunch(
    val backendId: String,
    val backendName: String,
    val arguments: List<String>,
    val ready: Boolean,
    val message: String
)