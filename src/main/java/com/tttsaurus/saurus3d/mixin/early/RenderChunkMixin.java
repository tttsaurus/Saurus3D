package com.tttsaurus.saurus3d.mixin.early;

import com.tttsaurus.saurus3d.common.core.mcpatches.IRenderChunkExtra;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.chunk.RenderChunk;
import net.minecraft.util.BlockRenderLayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(RenderChunk.class)
public class RenderChunkMixin implements IRenderChunkExtra
{
    @Unique
    private final BufferBuilder[] saurus3D$bufferBuilders = new BufferBuilder[BlockRenderLayer.values().length];

    @Override
    public BufferBuilder[] getBufferBuilders()
    {
        return saurus3D$bufferBuilders;
    }
}
