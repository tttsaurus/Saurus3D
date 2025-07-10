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
**Time**: running for 30 seconds (about 610 ticks)<br>
**Overall Animated Texture Size**: 816384 bytes<br>

**Repeated for 3 times**

- Vanilla: `1284ms`, `1200ms`, `1252ms`
- Vanilla Avg: `1245.3ms`
- Saurus3D: `432ms`, `600ms`, `472ms`
- Saurus3D Avg: `501.3ms`


- Vanilla Upload Speed: ~`380 MB/s`
- Saurus3D Upload Speed: ~`950 MB/s`

**Boost**: `380 MB/s` -> `950 MB/s` (150% faster)
