package com.tttsaurus.saurus3d.test;

import com.tttsaurus.saurus3d.Saurus3D;
import com.tttsaurus.saurus3d.common.core.RenderUtils;
import com.tttsaurus.saurus3d.common.core.mcpatches.IRenderChunkExtra;
import com.tttsaurus.saurus3d.common.core.mcpatches.IVertexBufferExtra;
import com.tttsaurus.saurus3d.common.core.mesh.Mesh;
import com.tttsaurus.saurus3d.common.core.mesh.attribute.AttributeLayout;
import com.tttsaurus.saurus3d.common.core.mesh.attribute.Slot;
import com.tttsaurus.saurus3d.common.core.mesh.attribute.Stride;
import com.tttsaurus.saurus3d.common.core.mesh.attribute.Type;
import com.tttsaurus.saurus3d.common.core.mesh.buffer.BufferID;
import com.tttsaurus.saurus3d.common.core.mesh.buffer.BufferType;
import com.tttsaurus.saurus3d.common.core.mesh.buffer.EBO;
import com.tttsaurus.saurus3d.common.core.mesh.buffer.VBO;
import com.tttsaurus.saurus3d.common.core.shader.Shader;
import com.tttsaurus.saurus3d.common.core.shader.ShaderManager;
import com.tttsaurus.saurus3d.common.core.shader.ShaderProgram;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.chunk.RenderChunk;
import net.minecraft.util.BlockRenderLayer;
import java.nio.ByteBuffer;

public class MyVboRenderList extends ChunkRenderContainer
{
    private static final AttributeLayout BLOCK_ATTRIBUTE_LAYOUT = new AttributeLayout();
    static
    {
        BLOCK_ATTRIBUTE_LAYOUT.push(new Stride(28)
                .push(new Slot(Type.FLOAT, 3))
                .push(new Slot(Type.UNSIGNED_BYTE, 4).setNormalize(true))
                .push(new Slot(Type.FLOAT, 2))
                .push(new Slot(Type.SHORT, 2)));
    }

    private ShaderProgram program = null;

    // <https://github.com/Laike-Endaril/Luminous/blob/1.12.2/src/main/java/com/fantasticsource/luminous/shaders/VboRenderListEdit.java>
    @Override
    public void renderChunkLayer(BlockRenderLayer layer)
    {
        if (initialized)
        {
            if (program == null)
            {
                Shader frag = ShaderManager.loadShader("saurus3d:shaders/chunk_frag.glsl", Shader.ShaderType.FRAGMENT);
                Shader vert = ShaderManager.loadShader("saurus3d:shaders/chunk_vert.glsl", Shader.ShaderType.VERTEX);
                program = new ShaderProgram(frag, vert);
                program.setup();
                Saurus3D.LOGGER.info(program.getSetupDebugReport());
            }

            program.use();
            program.setUniform("modelView", RenderUtils.getModelViewMatrix());
            program.setUniform("projection", RenderUtils.getProjectionMatrix());
            program.setUniform("tex", 0);
            program.setUniform("lightmap", 1);
            program.setUniform("camPos", RenderUtils.getCameraPos().x, RenderUtils.getCameraPos().y, RenderUtils.getCameraPos().z);
            program.unuse();

            for (RenderChunk renderChunk : renderChunks)
            {
                IRenderChunkExtra renderChunkExtra = ((IRenderChunkExtra)renderChunk);

                Mesh mesh;
                ByteBuffer vboByteBuffer = renderChunkExtra.getVboByteBuffers()[layer.ordinal()];
                //ByteBuffer eboByteBuffer;

                // init mesh and ebo byte buffer
                if (renderChunkExtra.getMeshes()[layer.ordinal()] == null)
                {
                    int vboID = ((IVertexBufferExtra)renderChunk.getVertexBufferByLayer(layer.ordinal())).getBufferID();
                    VBO vbo = new VBO();
                    vbo.setAutoRebindToOldVbo(true);
                    vbo.setVboID(new BufferID(vboID, BufferType.VBO));

                    // sync
                    if (vbo.getVboSize() != vboByteBuffer.remaining())
                        vbo.directUpload(vboByteBuffer);

                    // todo: move to uploadChunk
                    //renderChunkExtra.getEboByteBuffers()[layer.ordinal()] = ByteBuffer.allocateDirect(449390 * 4).order(ByteOrder.nativeOrder());
                    EBO ebo = new EBO();
                    ebo.setAutoRebindToOldEbo(true);
                    ebo.setEboID(EBO.genEboID());

                    mesh = new Mesh(BLOCK_ATTRIBUTE_LAYOUT, ebo, vbo);
                    mesh.setup();

                    renderChunkExtra.getMeshes()[layer.ordinal()] = mesh;
                }
                mesh = renderChunkExtra.getMeshes()[layer.ordinal()];
                //eboByteBuffer = renderChunkExtra.getEboByteBuffers()[layer.ordinal()];

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

                renderChunkExtra.getMeshes()[layer.ordinal()].getEbo().directUpload(indices);

                program.use();
                program.setUniform("offset", renderChunk.getPosition().getX(), renderChunk.getPosition().getY(), renderChunk.getPosition().getZ());
                mesh.render();
                program.unuse();
            }

            GlStateManager.resetColor();
            renderChunks.clear();
        }
    }
}
