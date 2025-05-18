package com.tttsaurus.saurus3d.test;

import com.tttsaurus.saurus3d.Saurus3D;
import com.tttsaurus.saurus3d.common.core.RenderUtils;
import com.tttsaurus.saurus3d.common.core.mcpatches.IRenderChunkExtra;
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

public class MyVboRenderList extends ChunkRenderContainer
{
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
                BufferBuilder bufferBuilder = ((IRenderChunkExtra)renderChunk).getBufferBuilders()[layer.ordinal()];


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
