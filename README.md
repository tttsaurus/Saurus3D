A WIP general purpose render lib for Minecraft 1.12.2

## Todo List
- G-Buffer pipeline
- Shader-Graph editor
- A lot

## Rough Benchmark

> Disclaimer: on my machine

### TextureMap.updateAnimations()

**Environment**: Forge + 26 mods installed<br>
**Time**: running for 30 seconds<br>
**Repeated for 5 times**

- Vanilla: `128ms`, `168ms`, `112ms`, `148ms`, `144ms`
- Vanilla Avg: `140.0ms`
- Saurus3D: `48ms`, `72ms`, `60ms`, `84ms`, `68ms`
- Saurus3D Avg: `66.4ms`

**Boost**: `140.0ms` -> `66.4ms` (↓52.6%, 2.1× faster)
