# Cryonix Launcher

Cryonix Launcher is an Android launcher focused on low overhead, fast navigation, and Minecraft Java support on mobile.

## Performance

Cryonix is designed to keep the launcher lightweight and responsive.

- Fast startup and app launching
- Low background overhead
- Lightweight Android UI
- Efficient version and installation management
- Separate Minecraft runtime configuration
- Native C/C++ for performance-critical components
- Renderer selection for Android graphics backends
- Memory and device compatibility checks

Heavy work should stay away from the main Android UI thread. Native code is used where it can provide useful performance or graphics benefits.

## Languages

Cryonix is primarily built with **Kotlin**.

- **Kotlin** — main Android application, UI, launcher systems, Minecraft management, configuration, networking, and most new code
- **Java** — used only where Java/JVM compatibility or existing Minecraft libraries require it
- **C/C++** — native performance code, graphics/renderer bridges, JNI, and Android native integrations

Kotlin is the main development language for the mobile launcher. Java is kept to a smaller compatibility layer instead of being the main language.

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

Android launcher code is under:

`main_cryonixlauncher/src/main/`

Kotlin code should be placed under:

`main_cryonixlauncher/src/main/java/` or a dedicated Kotlin source directory.

Existing Minecraft launcher Java code is under:

`main_cryonixlauncher/src/main/java/com/cryonix/launcher/minecraft/`

Native C/C++ code should be placed under the Android native source tree and connected through JNI when required.

Android layouts are under:

`main_cryonixlauncher/src/main/res/layout/`

## Current Status

Cryonix currently contains the Android launcher UI and the foundation for Minecraft version, account, loader, renderer, and launch management.

The architecture is being moved toward Kotlin-first development, with Java kept for compatibility and C/C++ used for native performance and graphics integration.

Full Minecraft execution still requires the runtime, library and asset downloader, Java runtime integration, authentication, native renderer integrations, and the final game process bridge.

## Goals

1. Make Kotlin the main launcher language.
2. Keep Java usage small and focused on compatibility.
3. Use C/C++ for native performance and graphics work.
4. Keep startup and navigation fast.
5. Avoid unnecessary background work.
6. Support multiple Minecraft versions and loaders.
7. Provide clear errors and useful logs.
8. Keep downloaded game files verified.
9. Keep the launcher modular and easy to maintain.

## Development

Build with Android Studio and JDK 17.

The project is currently under active development.

## License

See the repository license and project terms.
