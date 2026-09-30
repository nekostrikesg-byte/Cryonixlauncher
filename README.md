# Cryonix Launcher

Cryonix Launcher is an Android Minecraft Launcher For Mobile user Developed By NekoDev aka (NekoStrikeSG)

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

Cryonix is **Kotlin-first**.

- **Kotlin** — primary Android application, UI, launcher systems, Minecraft management, configuration, networking, and orchestration
- **Java** — reserved for compatibility code or external Minecraft/JVM libraries when Kotlin should not replace an existing Java integration
- **C/C++** — native performance code, graphics/renderer bridges, JNI, and Android native integrations

The current launcher implementation is Kotlin. Java is not used as the primary application layer.

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

Android launcher code is split by language and responsibility:

`main_cryonixlauncher/src/main/kotlin/` — **primary Kotlin application layer**

`main_cryonixlauncher/src/main/java/` — Java compatibility layer; use only when Java/JVM integration is required

`main_cryonixlauncher/src/main/cpp/` — C/C++ native code and JNI implementations

Minecraft launcher models, services, launch planning, and Android activities are now implemented under the Kotlin source tree.

Android layouts are under:

`main_cryonixlauncher/src/main/res/layout/`

## UI

Cryonix includes a UI-only port inspired by the public Zalith Launcher 2 / Zalith Launcher 2+ interface structure. The port covers the Cryonix home screen, renderer settings layout, navigation treatment, colors, rounded surfaces, and launcher icons.

The Zalith/Pojav backend is **not** included in this UI port. Cryonix does not import Zalith authentication, game downloading, Java runtime management, game launching, renderer service implementations, or other backend services as part of this work.

See [ZALITH_UI_PORT_NOTICE.md](ZALITH_UI_PORT_NOTICE.md) for the upstream source and license notice.

## Current Status

Cryonix now uses Kotlin as the main launcher implementation. The launcher activities, Minecraft models, metadata services, local profiles, renderer registry, and launch planning are Kotlin.

Java remains available for compatibility-only integrations. C/C++ remains isolated behind the native/JNI layer.

Phase 2 now adds the Android Java runtime selector, runtime preflight, and a stable Mojo-compatible game-engine boundary. Full gameplay still requires the Android JRE package plus the native LWJGL/GLFW/renderer implementation and authenticated game-session bridge; the launcher will not fake-launch when those components are absent.

## Goals

1. Keep Kotlin as the main launcher language.
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
