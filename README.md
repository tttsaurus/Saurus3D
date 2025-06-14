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

***

**Environment**: Forge + 26 mods installed<br>
**Time**: running for 30 seconds<br>
**Repeated for 5 times**

- Vanilla: `128ms`, `168ms`, `112ms`, `148ms`, `144ms`
- Vanilla Avg: `140.0ms`
- Saurus3D: `48ms`, `72ms`, `60ms`, `84ms`, `68ms`
- Saurus3D Avg: `66.4ms`

**Boost**: `140.0ms` -> `66.4ms` (↓52.6%, 2.1× faster)

***

**Environment**: Cleanroom + 209 mods installed<br>
**Time**: running for 30 seconds<br>
**Repeated for 5 times**

- Vanilla: `724ms`, `832ms`, `872ms`, `976ms`, `936ms`
- Vanilla Avg: `868.0ms`
- Saurus3D: `824ms`, `696ms`, `812ms`, `796ms`, `772ms`
- Saurus3D Avg: `780.0ms`

**Boost**: `868.0ms` -> `780.0ms` (↓10.1%, 1.1× faster)

***
