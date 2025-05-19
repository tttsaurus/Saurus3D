package com.tttsaurus.saurus3d.test;

import com.tttsaurus.saurus3d.Saurus3D;
import com.tttsaurus.saurus3d.common.core.RenderUtils;
import com.tttsaurus.saurus3d.common.core.mcpatches.IBufferBuilderExtra;
import com.tttsaurus.saurus3d.common.core.mcpatches.IRenderChunkExtra;
import com.tttsaurus.saurus3d.common.core.mesh.attribute.AttributeLayout;
import com.tttsaurus.saurus3d.common.core.mesh.attribute.Slot;
import com.tttsaurus.saurus3d.common.core.mesh.attribute.Stride;
import com.tttsaurus.saurus3d.common.core.mesh.attribute.Type;
import com.tttsaurus.saurus3d.common.core.shader.Shader;
import com.tttsaurus.saurus3d.common.core.shader.ShaderManager;
import com.tttsaurus.saurus3d.common.core.shader.ShaderProgram;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.chunk.ChunkRenderDispatcher;
import net.minecraft.client.renderer.chunk.RenderChunk;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.vertex.VertexBuffer;
import net.minecraft.util.BlockRenderLayer;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL20;

import java.nio.ByteBuffer;

public class MyVboRenderList extends ChunkRenderContainer
{
    private ShaderProgram program = null;

    private boolean flag = true;

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
                if (flag)
                {
                    AttributeLayout layout = new AttributeLayout();
                    layout.push(new Stride(32)
                                .push(new Slot(Type.FLOAT, 3))
                                .push(new Slot(Type.FLOAT, 2))
                                .push(new Slot(Type.FLOAT, 3)))
                          .push(new Stride(4)
                                .push(new Slot(Type.UNSIGNED_BYTE, 4).setNormalize(true).setDivisor(1)));
                    Saurus3D.LOGGER.info(layout.getDebugReport());

                    BufferBuilder bufferBuilder = ((IRenderChunkExtra)renderChunk).getBufferBuilders()[layer.ordinal()];
                    ByteBuffer buffer = ((IBufferBuilderExtra)bufferBuilder).getByteBuffer();

                    int vertexSize = 28;
                    int vertexCount = buffer.limit() / vertexSize;

                    buffer.rewind();

                    for (int i = 0; i < vertexCount; i++)
                    {
                        int base = i * vertexSize;

                        float x = buffer.getFloat(base);
                        float y = buffer.getFloat(base + 4);
                        float z = buffer.getFloat(base + 8);

                        int r = buffer.get(base + 12) & 0xFF;
                        int g = buffer.get(base + 13) & 0xFF;
                        int b = buffer.get(base + 14) & 0xFF;
                        int a = buffer.get(base + 15) & 0xFF;

                        float u0 = buffer.getFloat(base + 16);
                        float v0 = buffer.getFloat(base + 20);

                        int u1 = buffer.getShort(base + 24) & 0xFFFF;
                        int v1 = buffer.getShort(base + 26) & 0xFFFF;

                        Saurus3D.LOGGER.info(String.format("Vertex %d: Pos(%.2f, %.2f, %.2f), Color(%d, %d, %d, %d), UV0(%.2f, %.2f), UV1(%d, %d)", i, x, y, z, r, g, b, a, u0, v0, u1, v1));
                    }

                    flag = false;
                }

//                VertexBuffer vertexbuffer = renderChunk.getVertexBufferByLayer(layer.ordinal());
//
////                GlStateManager.pushMatrix();
////
////                preRenderChunk(renderChunk);
////                renderChunk.multModelviewMatrix();
//
//                program.use();
//                program.setUniform("offset", renderChunk.getPosition().getX(), renderChunk.getPosition().getY(), renderChunk.getPosition().getZ());
//                program.unuse();
//
//                vertexbuffer.bindBuffer();
//
//                // position
//                GL20.glVertexAttribPointer(0, 3, GL11.GL_FLOAT, false, 28, 0);
//                GL20.glEnableVertexAttribArray(0);
//                // color
//                GL20.glVertexAttribPointer(1, 4, GL11.GL_UNSIGNED_BYTE, true, 28, 12);
//                GL20.glEnableVertexAttribArray(1);
//                // texcoord0
//                GL20.glVertexAttribPointer(2, 2, GL11.GL_FLOAT, false, 28, 16);
//                GL20.glEnableVertexAttribArray(2);
//                // texcoord1
//                GL20.glVertexAttribPointer(3, 2, GL11.GL_UNSIGNED_SHORT, true, 28, 24);
//                GL20.glEnableVertexAttribArray(3);
//
//                program.use();
//                vertexbuffer.drawArrays(GL11.GL_TRIANGLES);
//                program.unuse();
//
//                //vertexbuffer.drawArrays(GL11.GL_TRIANGLES);
////
////                GlStateManager.popMatrix();
            }

            OpenGlHelper.glBindBuffer(OpenGlHelper.GL_ARRAY_BUFFER, 0);
            GlStateManager.resetColor();
            renderChunks.clear();
        }
    }
}
