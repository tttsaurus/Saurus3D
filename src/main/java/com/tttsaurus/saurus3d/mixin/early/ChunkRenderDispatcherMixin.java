package com.tttsaurus.saurus3d.mixin.early;

import com.google.common.util.concurrent.ListenableFuture;
import com.tttsaurus.saurus3d.common.core.mcpatches.IBufferBuilderExtra;
import com.tttsaurus.saurus3d.common.core.mcpatches.IRenderChunkExtra;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.chunk.ChunkRenderDispatcher;
import net.minecraft.client.renderer.chunk.CompiledChunk;
import net.minecraft.client.renderer.chunk.RenderChunk;
import net.minecraft.util.BlockRenderLayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ChunkRenderDispatcher.class)
public class ChunkRenderDispatcherMixin
{
    @Inject(method = "uploadChunk", at = @At("HEAD"))
    public void beforeUploadChunk(BlockRenderLayer p_188245_1_, BufferBuilder p_188245_2_, RenderChunk p_188245_3_, CompiledChunk p_188245_4_, double p_188245_5_, CallbackInfoReturnable<ListenableFuture<Object>> cir)
    {
        ((IRenderChunkExtra) p_188245_3_).getVboByteBuffers()[p_188245_1_.ordinal()] = ((IBufferBuilderExtra) p_188245_2_).getByteBuffer();
    }
}
