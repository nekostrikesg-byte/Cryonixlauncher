# Cryonix Launcher — Minecraft Architecture

## Scope

Cryonix Launcher is being prepared as an Android Minecraft Java launcher layer. The launcher should resolve official version metadata at runtime instead of shipping a frozen list, allowing new releases and snapshots to appear without an app update.

## Version support

The version subsystem uses Mojang's public Java version manifest and is designed for releases, snapshots and older versions exposed by that manifest. Each selected version should resolve its own metadata URL and SHA-1 information. Downloads should be verified before use.

## Accounts

### Microsoft account

Production authentication should use Microsoft's supported browser/device authentication flow. Access and refresh tokens belong in Android secure storage. Ownership should be checked before normal Minecraft play. Cryonix must never collect or store a Microsoft password.

### Local/offline profile

Cryonix supports a local profile object for launcher configuration, testing and offline/local workflows. It is not an authentication bypass and must not be used to access online Minecraft services without a valid account/license.

## Loaders

### Vanilla

The default loader. It uses the selected Minecraft version's official metadata.

### Fabric

Fabric metadata is resolved for the selected Minecraft version. The launcher can install the compatible loader and expose a per-installation mods directory.

Future adapters can be added without changing the UI: Forge, NeoForge and Quilt.

## Renderers

Renderer support is an adapter layer rather than a hard-coded game dependency.

Reserved Cryonix profiles:

- System/OpenGL
- LTW
- Holy GL4ES
- Mobile Glues
- Vulkan

Each backend needs Android ABI compatibility, native libraries, any required Java/native bridge, graphics-context initialization, Minecraft compatibility rules and graceful fallback to the system backend. Cryonix should never assume every renderer works with every Minecraft version or device.

## Launch pipeline

UI -> installation configuration -> version manifest resolver -> version metadata resolver -> library/asset downloader -> account session provider -> loader installer -> renderer adapter -> Java runtime selector -> argument builder -> game surface/process.

## Java runtimes

A runtime manager is required for version-specific Java compatibility. It should map Minecraft versions to the required Java major version and select an installed compatible runtime. Runtime packages must be ABI/device aware and verified before use.

## Storage layout

Recommended private app storage:

Android/data/<package>/files/cryonix/

- versions/
- libraries/
- assets/
- runtimes/
- accounts/
- installations/
- mods/
- resourcepacks/
- shaderpacks/
- logs/

## Integrity and safety

- Verify downloads using hashes supplied by trusted metadata.
- Never execute an unverified download.
- Keep account tokens out of logs.
- Never log passwords or authorization codes.
- Use HTTPS only.
- Do not bundle proprietary Minecraft game files in this repository.
- Do not bypass ownership or authentication checks.

## Android compatibility

Supporting all Minecraft versions does not mean every version can run on every phone. Java runtime requirements, Android API level, RAM, CPU architecture, graphics drivers, native renderer compatibility and mod-loader compatibility can all affect launchability.

The launcher should therefore run a compatibility check before Play and explain why a selected version/backend combination is unsupported.


## Phase 2 runtime boundary

Cryonix now contains a runtime-selection and engine boundary under `runtime/`.

- `JavaRuntimeManager` selects only verified Android-compatible runtimes installed in private app storage.
- Minecraft version metadata determines the required Java major version.
- `MinecraftRuntimeBridge` performs launch preflight without pretending that a desktop JVM can execute Minecraft on Android.
- `MinecraftGameEngine` is the stable boundary for the Mojo/Pojav-compatible Android engine.
- The current `CryonixMojoEngine` intentionally refuses to launch until the Android JRE plus the native LWJGL/GLFW/renderer engine are actually present.

This separation is deliberate: Phase 1 downloads the official Mojang game files; Phase 2 connects those files to an Android Java/native engine. Proprietary Minecraft files are never committed to the repository.
