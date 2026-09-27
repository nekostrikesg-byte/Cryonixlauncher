# Cryonix Launcher

Cryonix Launcher is an Android launcher focused on a clean interface, low overhead, and Minecraft Java support.

## Performance

Cryonix is designed to keep the launcher lightweight and responsive.

- Fast app launching
- Low background overhead
- Simple Android UI
- Efficient version and installation management
- Separate Minecraft runtime configuration
- Native code support for performance-critical components
- Renderer selection for Android graphics backends
- Memory and device compatibility checks

The goal is to keep the launcher itself lightweight while moving heavy work, such as game runtime and native rendering, away from the main Android UI thread.

## How Cryonix is made

Cryonix uses three main languages:

- Kotlin — Android application and UI components
- Java — Minecraft launcher logic, version management, accounts, loaders, and runtime management
- C++ — native performance components, renderer bridges, and Android native integrations

Kotlin and Java handle the Android and launcher layers. C++ is used where native performance or graphics integration is required.

## Minecraft Launcher

Cryonix is being developed as a Minecraft Java launcher for Android.

The launcher architecture includes:

- Minecraft version selection
- Official version metadata
- Vanilla support
- Fabric support
- Microsoft account integration
- Local launcher profiles
- Java runtime management
- Renderer selection
- OpenGL
- LTW
- Holy GL4ES
- Mobile Glues
- Vulkan
- Installation and file verification
- Per-version configuration

Local profiles are for launcher configuration and local workflows. They do not bypass Minecraft authentication or ownership requirements.

## Project Structure

main_cryonixlauncher/ contains the Android launcher.

Minecraft launcher code is located in:

main_cryonixlauncher/src/main/java/com/cryonix/launcher/minecraft/

Android layouts are located in:

main_cryonixlauncher/src/main/res/layout/

Native C++ components can be added under the Android native source tree when required.

## Current Status

Cryonix currently contains the Android launcher UI and the foundation for Minecraft version, account, loader, renderer, and launch management.

Full Minecraft execution still requires the runtime, library and asset downloader, Java runtime integration, authentication, native renderer integrations, and the final game process bridge.

## Goals

1. Keep the launcher lightweight.
2. Keep startup and navigation fast.
3. Avoid unnecessary background work.
4. Use native code where it provides a real performance benefit.
5. Support multiple Minecraft versions and loaders.
6. Provide clear errors and useful logs.
7. Keep downloaded game files verified.
8. Keep the launcher modular and easy to maintain.

## Development

Build with Android Studio and JDK 17.

The project is currently under active development.

## License

See the repository license and project terms.
