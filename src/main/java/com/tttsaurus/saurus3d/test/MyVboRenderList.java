package com.tttsaurus.saurus3d.test;

import com.tttsaurus.saurus3d.Saurus3D;
import com.tttsaurus.saurus3d.common.core.commonutils.MinecraftRenderUtils;
import com.tttsaurus.saurus3d.mcpatches.api.extra.IRenderChunkExtra;
import com.tttsaurus.saurus3d.common.core.mesh.Mesh;
import com.tttsaurus.saurus3d.common.core.mesh.attribute.AttributeLayout;
import com.tttsaurus.saurus3d.common.core.mesh.attribute.Slot;
import com.tttsaurus.saurus3d.common.core.mesh.attribute.Stride;
import com.tttsaurus.saurus3d.common.core.mesh.attribute.Type;
import com.tttsaurus.saurus3d.common.core.shader.Shader;
import com.tttsaurus.saurus3d.common.core.shader.ShaderManager;
import com.tttsaurus.saurus3d.common.core.shader.ShaderProgram;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.chunk.RenderChunk;
import net.minecraft.util.BlockRenderLayer;

public class MyVboRenderList extends ChunkRenderContainer
{
    public static final AttributeLayout BLOCK_ATTRIBUTE_LAYOUT = new AttributeLayout();
    static
    {
        BLOCK_ATTRIBUTE_LAYOUT.push(new Stride(28)
                .push(new Slot(Type.FLOAT, 3))
                .push(new Slot(Type.UNSIGNED_BYTE, 4).setNormalize(true))
                .push(new Slot(Type.FLOAT, 2))
                .push(new Slot(Type.SHORT, 2)));
    }

    private ShaderProgram program = null;

    @Override
    public void renderChunkLayer(BlockRenderLayer layer)
    {
        if (initialized)
        {
            if (program == null)
            {
                ShaderManager.loadShader("saurus3d:shaders/chunk_frag.glsl", Shader.ShaderType.FRAGMENT);
                ShaderManager.loadShader("saurus3d:shaders/chunk_vert.glsl", Shader.ShaderType.VERTEX);
                Shader frag = ShaderManager.getShader("saurus3d:shaders/chunk_frag.glsl");
                Shader vert = ShaderManager.getShader("saurus3d:shaders/chunk_frag.glsl");
                program = ShaderManager.createShaderProgram("test_program", frag, vert);
                program.setup();
                Saurus3D.LOGGER.info(program.getSetupDebugReport());
            }

            program.use();
            program.setUniform("modelView", MinecraftRenderUtils.getModelViewMatrix());
            program.setUniform("projection", MinecraftRenderUtils.getProjectionMatrix());
            program.setUniform("tex", 0);
            program.setUniform("lightmap", 1);
            program.setUniform("camPos", MinecraftRenderUtils.getWorldOffset().x, MinecraftRenderUtils.getWorldOffset().y, MinecraftRenderUtils.getWorldOffset().z);
            program.unuse();

            for (RenderChunk renderChunk : renderChunks)
            {
                IRenderChunkExtra renderChunkExtra = ((IRenderChunkExtra)renderChunk);
                Mesh mesh = renderChunkExtra.getMeshes()[layer.ordinal()];

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
