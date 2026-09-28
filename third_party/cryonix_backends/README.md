# Cryonix Backends

Cryonix Backends is the backend adapter boundary used by Cryonix Launcher.

It provides adapters for:
- MojoLauncher
- PojavLauncher-compatible runtime

Upstream projects:
- https://github.com/MojoLauncher/MojoLauncher
- https://github.com/PojavLauncherTeam/PojavLauncher

Both upstream launcher projects are licensed under LGPL-3.0. Their source, native runtime components, JREs, LWJGL/GLFW/SDL components and other third-party dependencies are **not copied into this adapter directory**.

The current Cryonix adapter converts Cryonix launch requests into backend-compatible launch plans. The full upstream runtime must be integrated separately with its native/JRE/dependency tree before an actual Minecraft process can be launched.

Name used by Cryonix: **Cryonix Backends**.
