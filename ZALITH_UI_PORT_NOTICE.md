# Zalith 2+ UI source port

Cryonix Launcher includes a port of the relevant **Zalith Launcher 2+ Kotlin UI source files** under:

`third_party/zalith_ui_port/src/main/java/com/movtery/zalithlauncher/`

The port currently contains the home-screen, accounts, settings, settings-layout, custom-home, and related view-model Kotlin sources used by those screens.

Upstream sources:
- https://github.com/Star1xr/ZalithLauncher2Plus
- https://github.com/ZalithLauncher/ZalithLauncher2

These upstream sources are retained in an isolated third-party directory because Zalith 2+ is a Jetpack Compose application with a substantially different dependency graph from Cryonix's current native Android/XML architecture. The live Cryonix screens are implemented separately in:

- `ui/home/ZalithHomeScreen.kt`
- `ui/accounts/ZalithAccountsScreen.kt`
- `ui/settings/ZalithSettingsScreen.kt`

Those live controllers adapt the same home/account/settings interaction model to Cryonix's existing architecture rather than importing the full Zalith/Pojav runtime.

## License and attribution

The imported upstream source remains under its original GPL-3.0 licensing and retains its upstream copyright headers. See `third_party/zalith_ui_port` and the upstream LICENSE.

Zalith Launcher 2+ identifies itself as an unofficial modified version of Zalith Launcher 2 and documents its GPL-3.0 inheritance. Cryonix is independently branded **Cryonix Launcher** and the home screen visibly identifies this build as an **Unofficial Modified Version**.

This port does **not** copy Zalith/Pojav game-launching, authentication, downloader, renderer, or runtime services into the live Cryonix implementation.
