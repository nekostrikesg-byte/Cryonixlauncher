# Cryonix Phase 2 runtime package contract

Cryonix never treats a normal desktop JRE as an Android Minecraft runtime.

A runtime package installed under:

`files/minecraft/runtimes/<runtime-id>/`

must contain:

- `bin/java` — Android-compatible executable
- `cryonix-runtime.json` — verified package metadata

Example metadata:

```json
{
  "id": "jre17-arm64",
  "major": 17,
  "abis": ["arm64-v8a"],
  "java": "/absolute/path/to/bin/java"
}
```

The Java executable and native libraries must come from a compatible Android OpenJDK build and must be verified before installation.

The next engine integration step is the Android LWJGL/GLFW bridge and renderer implementation used by Mojo/Pojav-compatible launchers. Cryonix keeps this behind `MinecraftGameEngine` so the launcher UI and Mojang installer remain independent.

No Minecraft client files, Microsoft credentials, or proprietary runtime binaries belong in this source repository.
