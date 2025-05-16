package com.tttsaurus.saurus3d.test;

import com.tttsaurus.saurus3d.Saurus3D;
import com.tttsaurus.saurus3d.common.core.shader.Shader;
import com.tttsaurus.saurus3d.common.core.shader.ShaderManager;
import com.tttsaurus.saurus3d.common.core.shader.ShaderProgram;
import net.minecraft.client.renderer.ChunkRenderContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.chunk.RenderChunk;
import net.minecraft.client.renderer.vertex.VertexBuffer;
import net.minecraft.util.BlockRenderLayer;
import org.lwjgl.opengl.GL13;

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

            program.setUniform("texture", OpenGlHelper.defaultTexUnit - GL13.GL_TEXTURE0);
            program.setUniform("lightmap", OpenGlHelper.lightmapTexUnit - GL13.GL_TEXTURE0);

            for (RenderChunk renderChunk : renderChunks)
            {
                VertexBuffer vertexbuffer = renderChunk.getVertexBufferByLayer(layer.ordinal());

                GlStateManager.pushMatrix();

                preRenderChunk(renderChunk);
                renderChunk.multModelviewMatrix();

                vertexbuffer.bindBuffer();
                GlStateManager.glVertexPointer(3, 5126, 28, 0);
                GlStateManager.glColorPointer(4, 5121, 28, 12);
                GlStateManager.glTexCoordPointer(2, 5126, 28, 16);
                OpenGlHelper.setClientActiveTexture(OpenGlHelper.lightmapTexUnit);
                GlStateManager.glTexCoordPointer(2, 5122, 28, 24);
                OpenGlHelper.setClientActiveTexture(OpenGlHelper.defaultTexUnit);
                vertexbuffer.drawArrays(7);

                GlStateManager.popMatrix();
            }

            OpenGlHelper.glBindBuffer(OpenGlHelper.GL_ARRAY_BUFFER, 0);
            GlStateManager.resetColor();
            renderChunks.clear();

            program.unuse();
        }
    }
}
