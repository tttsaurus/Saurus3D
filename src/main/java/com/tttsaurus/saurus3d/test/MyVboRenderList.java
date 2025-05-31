package com.tttsaurus.saurus3d.test;

import com.tttsaurus.saurus3d.Saurus3D;
import com.tttsaurus.saurus3d.common.core.RenderUtils;
import com.tttsaurus.saurus3d.common.core.mcpatches.IRenderChunkExtra;
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
