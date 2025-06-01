package com.tttsaurus.saurus3d.mixin.early;

import com.tttsaurus.saurus3d.common.core.gl.resource.GLResourceManager;
import com.tttsaurus.saurus3d.mcpatches.api.IRenderChunkExtra;
import com.tttsaurus.saurus3d.common.core.mesh.Mesh;
import com.tttsaurus.saurus3d.common.core.buffer.VBO;
import net.minecraft.client.renderer.chunk.RenderChunk;
import net.minecraft.util.BlockRenderLayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import java.nio.ByteBuffer;

@Mixin(RenderChunk.class)
public class RenderChunkMixin implements IRenderChunkExtra
{
    @Unique
    private ByteBuffer[] saurus3D$vboByteBuffers;

    @Unique
    private ByteBuffer[] saurus3D$eboByteBuffers;

    @Unique
    private Mesh[] saurus3D$meshes;

    @Override
    public ByteBuffer[] getVboByteBuffers()
    {
        if (saurus3D$vboByteBuffers == null)
            saurus3D$vboByteBuffers = new ByteBuffer[BlockRenderLayer.values().length];

        return saurus3D$vboByteBuffers;
    }

    @Override
    public ByteBuffer[] getEboByteBuffers()
    {
        if (saurus3D$eboByteBuffers == null)
            saurus3D$eboByteBuffers = new ByteBuffer[BlockRenderLayer.values().length];

        return saurus3D$eboByteBuffers;
    }

    @Override
    public Mesh[] getMeshes()
    {
        if (saurus3D$meshes == null)
            saurus3D$meshes = new Mesh[BlockRenderLayer.values().length];

        return saurus3D$meshes;
    }

    @Inject(method = "deleteGlResources", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/chunk/RenderChunk;stopCompileTask()V", shift = At.Shift.AFTER))
    private void deleteExtraGlResources(CallbackInfo ci)
    {
        if (saurus3D$meshes == null) return;
        for (int i = 0; i < BlockRenderLayer.values().length; ++i)
        {
            Mesh mesh = saurus3D$meshes[i];
            if (mesh != null)
            {
                mesh.dispose();
                mesh.getEbo().dispose();
                GLResourceManager.removeDisposable(mesh);
                GLResourceManager.removeDisposable(mesh.getEbo());
                for (VBO vbo: mesh.getVbos())
                {
                    vbo.dispose();
                    GLResourceManager.removeDisposable(vbo);
                }
            }
        }
    }
}
