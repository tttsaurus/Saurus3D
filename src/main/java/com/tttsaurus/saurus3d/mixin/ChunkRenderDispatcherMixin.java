package com.tttsaurus.saurus3d.mixin;

import com.google.common.util.concurrent.ListenableFuture;
import com.tttsaurus.saurus3d.mcpatches.api.extra.IBufferBuilderExtra;
import com.tttsaurus.saurus3d.mcpatches.api.extra.IRenderChunkExtra;
import com.tttsaurus.saurus3d.mcpatches.api.extra.IVertexBufferExtra;
import com.tttsaurus.saurus3d.common.core.mesh.Mesh;
import com.tttsaurus.saurus3d.common.core.buffer.BufferID;
import com.tttsaurus.saurus3d.common.core.buffer.BufferType;
import com.tttsaurus.saurus3d.common.core.buffer.EBO;
import com.tttsaurus.saurus3d.common.core.buffer.VBO;
import com.tttsaurus.saurus3d.test.MyVboRenderList;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.chunk.ChunkRenderDispatcher;
import net.minecraft.client.renderer.chunk.CompiledChunk;
import net.minecraft.client.renderer.chunk.RenderChunk;
import net.minecraft.util.BlockRenderLayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;

@Mixin(ChunkRenderDispatcher.class)
public class ChunkRenderDispatcherMixin
{
    @Inject(method = "uploadChunk", at = @At("HEAD"))
    public void beforeUploadChunk(BlockRenderLayer p_188245_1_, BufferBuilder p_188245_2_, RenderChunk p_188245_3_, CompiledChunk p_188245_4_, double p_188245_5_, CallbackInfoReturnable<ListenableFuture<Object>> cir)
    {
        if (!Minecraft.getMinecraft().isCallingFromMinecraftThread()) return;

        IRenderChunkExtra renderChunkExtra = ((IRenderChunkExtra) p_188245_3_);
        int layerIndex = p_188245_1_.ordinal();

        // get vbo byte buffer
        ByteBuffer vboByteBuffer = ((IBufferBuilderExtra) p_188245_2_).getByteBuffer();
        renderChunkExtra.getVboByteBuffers()[layerIndex] = vboByteBuffer;

        // init ebo byte buffer
        ByteBuffer eboByteBuffer;
        if (renderChunkExtra.getEboByteBuffers()[layerIndex] == null)
            renderChunkExtra.getEboByteBuffers()[layerIndex] = ByteBuffer.allocateDirect(449390 * 4).order(ByteOrder.nativeOrder());
        eboByteBuffer = renderChunkExtra.getEboByteBuffers()[layerIndex];

        // init mesh
        Mesh mesh;
        if (renderChunkExtra.getMeshes()[layerIndex] == null)
        {
            int vboID = ((IVertexBufferExtra)p_188245_3_.getVertexBufferByLayer(layerIndex)).getBufferID();
            VBO vbo = new VBO();
            vbo.setAutoUnbind(true);
            vbo.setVboID(new BufferID(vboID, BufferType.VBO));

            EBO ebo = new EBO();
            ebo.setAutoUnbind(true);
            ebo.setEboID(EBO.genEboID());

            mesh = new Mesh(MyVboRenderList.BLOCK_ATTRIBUTE_LAYOUT, ebo, vbo);
            mesh.setup();

            renderChunkExtra.getMeshes()[layerIndex] = mesh;
        }
        mesh = renderChunkExtra.getMeshes()[layerIndex];

        int vertexCount = vboByteBuffer.remaining() / 28;
        int quadCount = vertexCount / 4;

        int[] indices = new int[quadCount * 6];
        for (int i = 0; i < quadCount; i++)
        {
            indices[i * 6] = i * 4;
            indices[i * 6 + 1] = i * 4 + 1;
            indices[i * 6 + 2] = i * 4 + 2;
            indices[i * 6 + 3] = i * 4;
            indices[i * 6 + 4] = i * 4 + 2;
            indices[i * 6 + 5] = i * 4 + 3;
        }

        eboByteBuffer.position(0);
        eboByteBuffer.clear();
        IntBuffer intView = eboByteBuffer.asIntBuffer();
        intView.clear();
        intView.put(indices);
        eboByteBuffer.position(0);
        eboByteBuffer.limit(indices.length * 4);

        mesh.getEbo().directUpload(eboByteBuffer);
    }
}
