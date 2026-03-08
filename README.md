Pixel RPG - LibGDX version
==========================

Overview
- Gradle multi-module LibGDX project (`core`, `desktop`).
- Runtime map setup is data-driven through `core/assets/config/maps.json`.
- Desktop window setup is data-driven through `core/assets/config/desktop.properties`.

Run (desktop)
1. From repo root:
   ```bash
   gradle desktop:run
   ```
   Windows:
   ```bash
   gradlew.bat desktop:run
   ```

Controls
- Movement: `WASD` or arrow keys
- Attack: mouse left button
- Dash: `Space`
- Special dash attack: `E`
- Reload current map: `F5`
- Switch to next map from catalog: `F6`
- Force daytime: `F7`
- Force nighttime: `F8`
- Day/night slower: `[`
- Day/night faster: `]`
- Day/night speed reset: `\`

Map Configuration
- File: `core/assets/config/maps.json`
- `defaultMap`: id of the map loaded at startup
- `maps[]`: ordered list used for `F6` cycling
- Map entry fields:
  - `id`: stable map id
  - `path`: TMX path in assets (for example `maps/village_finite.tmx`)
  - `collisionLayers`: layer names used for wall collision (object layers)
  - `spawnLayers`: first available object layer used for player spawn
  - `padding`: extra camera/map boundary padding in world units

Current Active Map
- `maps/village_finite.tmx` (generated from `DSA GAME/village.tmx`)
- Collision uses `WALL` (with compatibility fallback available through config)

Desktop Configuration
- File: `core/assets/config/desktop.properties`
- Supported keys:
  - `title`
  - `width`
  - `height`
  - `resizable`
  - `vsync`
  - `foregroundFps`

Notes
- Assets are loaded from `core/assets`.
- Infinite map bounds are computed from TMX chunk coordinates so camera clamping matches true extents.
- Polygon collision objects are supported (triangulated internally for overlap checks).
- Build includes `core:validateMapCatalog` to fail fast on bad map ids/default/paths.
- `src/pixelrpg` contains legacy non-LibGDX code and is not part of the `core`/`desktop` Gradle modules.
- Day/night cycle is automatic with smooth dawn/dusk transitions and night tint applied to world rendering.
- Night now also increases vignette strength and reduces character saturation.

TMX Conversion
- LibGDX `TmxMapLoader` does not support infinite/chunked TMX layers.
- To convert a chunked map to finite TMX:
  ```bash
  powershell -ExecutionPolicy Bypass -File tools/convert_tmx_infinite_to_finite.ps1 -InputPath core/assets/maps/village.tmx -OutputPath core/assets/maps/village_finite.tmx
  ```
