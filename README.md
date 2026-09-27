# ❄️ Cryonix Launcher

> **A modern Android launcher and Minecraft Java launcher built with the Cryonix identity.**

Cryonix Launcher combines a clean Android home experience with a dedicated Minecraft Java launcher architecture for Android.

## ✦ Cryonix

- ⚡ Fast, focused launcher UI
- 🎨 Cryonix dark visual system
- 📱 Android-first design
- 🔎 App and Minecraft version search
- 🧩 Modular launcher architecture
- ⛏️ Minecraft Java installation management
- 🧵 Vanilla and Fabric support foundation
- 🎮 Renderer profiles for OpenGL, LTW, Holy GL4ES, Mobile Glues and Vulkan

## ⛏️ Minecraft Launcher

Cryonix is being built to manage Minecraft Java installations instead of shipping a fixed, outdated version list.

### Versions

The launcher resolves Mojang's official Java version manifest at runtime. This is designed to cover:

- Latest release
- Previous releases
- Snapshots
- Older versions exposed by the official manifest
- Per-version metadata
- SHA-1 integrity information

The selected version can become an installation profile with its own loader, renderer, runtime and mods.

### Accounts

**Microsoft account**
- Browser/device authentication integration
- Ownership validation
- Online services when the account is authorized

**Local profile**
- Local launcher profile for configuration and offline/local workflows
- Useful for launcher testing and local configuration
- Not an authentication or ownership bypass

Minecraft requires a Microsoft account for normal play and ownership-based access, so Cryonix does not implement cracked/offline authentication bypasses.

### Loaders

**Vanilla**
- Default Minecraft installation mode
- Uses official version metadata

**Fabric**
- Fabric metadata lookup
- Compatible loader selection per Minecraft version
- Per-installation mods directory

Planned loader adapters: Forge, NeoForge and Quilt.

### Renderers

Cryonix uses a renderer adapter layer so the game runtime is not permanently tied to one graphics backend.

Available profiles in the architecture:

- System / OpenGL
- LTW
- Holy GL4ES
- Mobile Glues
- Vulkan

Each renderer must provide compatible Android native libraries, ABI support, graphics initialization and Minecraft-version compatibility. Unsupported combinations should fall back safely or be rejected with a useful error.

### Java Runtime

A real Minecraft launcher needs a runtime manager that can:

- Detect the Java version required by an installation
- Download/install a compatible runtime
- Select the correct runtime per Minecraft version
- Handle Android CPU ABI differences
- Validate runtime files
- Keep separate runtimes when versions require them

### Installation Pipeline

1. Select Minecraft version
2. Check device compatibility
3. Select Microsoft/local profile
4. Select Vanilla or Fabric
5. Select renderer
6. Select compatible Java runtime
7. Resolve version metadata
8. Download required libraries/assets
9. Verify downloaded files
10. Prepare game arguments
11. Start the selected runtime
12. Show logs and launch errors

## 📂 Minecraft Code

Minecraft launcher code lives under:

main_cryonixlauncher/src/main/java/com/cryonix/launcher/minecraft/

Current foundation:

- MinecraftVersion — version model
- GameAccount — Microsoft/local profile model
- LoaderProfile — Vanilla/Fabric model
- RendererProfile — graphics backend model
- VersionManifestService — Mojang version metadata
- FabricMetaService — Fabric metadata
- RendererRegistry — renderer profiles
- LaunchRequest — installation launch configuration
- LaunchPlan — validated launch arguments
- LaunchPlanBuilder — launch-plan builder
- LocalProfileStore — local profile storage

Minecraft UI resources are under main_cryonixlauncher/src/main/res/layout/ and include the Minecraft home, version, account, loader, renderer and version-item layouts.

Architecture details are documented in MINECRAFT_ARCHITECTURE.md.

## 🏠 Android Launcher

Cryonix also provides:

- Home screen
- App drawer
- Search
- Settings
- Personalization
- Widgets
- Wallpaper
- App information
- Custom dock
- Cryonix branding

## 🛣️ Roadmap

### Phase 1 — Cryonix UI
- [x] Cryonix home UI
- [x] Settings and personalization UI
- [x] Minecraft launcher UI
- [x] Version selector UI
- [x] Account selector UI
- [x] Loader selector UI
- [x] Renderer selector UI

### Phase 2 — Minecraft Core
- [x] Mojang version manifest integration
- [x] Fabric metadata integration
- [x] Vanilla/Fabric installation model
- [x] Renderer adapter model
- [x] Launch-plan model
- [ ] Full version metadata parser
- [ ] Library downloader
- [ ] Asset downloader
- [ ] SHA-1 verification
- [ ] Installation manager
- [ ] Java runtime manager
- [ ] Real game process/runtime bridge

### Phase 3 — Accounts & Compatibility
- [ ] Microsoft OAuth/device authentication
- [ ] Secure token storage
- [ ] Ownership verification
- [ ] Account switching
- [ ] Device/ABI compatibility checks
- [ ] RAM and storage checks
- [ ] Java-version compatibility checks

### Phase 4 — Renderers
- [ ] System OpenGL runtime
- [ ] LTW runtime adapter
- [ ] Holy GL4ES runtime adapter
- [ ] Mobile Glues runtime adapter
- [ ] Vulkan runtime adapter
- [ ] Renderer capability detection
- [ ] Automatic fallback
- [ ] Per-version renderer compatibility

### Phase 5 — Mods & Advanced Features
- [ ] Fabric mod installation
- [ ] Mod profile management
- [ ] Resource packs
- [ ] Shader packs
- [ ] Worlds management
- [ ] Separate game instances
- [ ] Import/export installations
- [ ] Crash/log viewer
- [ ] Automatic recovery

## 🔐 Safety & Licensing

Cryonix does not bundle proprietary Minecraft game files.

The launcher architecture is designed to download required files from appropriate official metadata/download sources and verify them before use.

The project will not:
- Store Microsoft passwords
- Bypass Microsoft authentication
- Bypass Minecraft ownership checks
- Ship pirated Minecraft assets
- Execute unverified downloaded binaries

## 🧊 Design

| Element | Cryonix Style |
|---|---|
| Background | Deep dark |
| Surfaces | Layered dark panels |
| Accent | Soft violet |
| Primary text | Bright neutral |
| Secondary text | Muted neutral |
| UI | Clean and minimal |

## 🤝 Contributing

1. Keep changes focused.
2. Follow the Cryonix visual language.
3. Keep Minecraft runtime code modular.
4. Do not commit proprietary Minecraft files.
5. Do not commit account tokens or secrets.
6. Verify downloaded resources before execution.
7. Document new runtime, loader or renderer integrations.

## 📜 License

See the repository for the current license and project terms.

---

# ❄️ Cryonix Launcher

**Android launcher. Minecraft Java launcher architecture. One Cryonix experience.**

> **Cryonix — Make your space yours.**
