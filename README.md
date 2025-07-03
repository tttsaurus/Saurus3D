A WIP general purpose render lib for Minecraft 1.12.2

## Todo List
- G-Buffer pipeline
- Shader-Graph editor
- A lot

## Credits
- Created using [CleanroomModTemplate](https://github.com/CleanroomMC/CleanroomModTemplate/tree/mixin)
- Bundled [JOML](https://github.com/JOML-CI/JOML) licensed under MIT

## Rough Benchmark

> Disclaimer: on my machine

### `TextureMap.updateAnimations()`

**Environment**: Cleanroom-0.3.7a + 210 mods installed<br>
**Time**: running for 30 seconds<br>
**Repeated for 3 times**

- Vanilla: `1284ms`, `1200ms`, `1252ms`
- Vanilla Avg: `1245.3ms`
- Saurus3D: `432ms`, `600ms`, `472ms`
- Saurus3D Avg: `501.3ms`

**Boost**: `1245.3ms` -> `501.3ms` (↓59.7%, 2.48× faster)
