package com.tttsaurus.saurus3d.common.core.render;

import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.shader.Framebuffer;
import org.lwjgl.opengl.GL11;

public final class RenderUtils
{
    public static void renderFbo(ScaledResolution resolution, Framebuffer fbo, boolean useTexture)
    {
        double width = resolution.getScaledWidth_double();
        double height = resolution.getScaledHeight_double();

        GlStateManager.enableTexture2D();
        GlStateManager.disableLighting();
        GlStateManager.enableBlend();
        GlStateManager.disableAlpha();
        GlStateManager.disableDepth();
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);

        if (useTexture)
            fbo.bindFramebufferTexture();

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        if (useTexture)
            buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
        else
            buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION);
        buffer.pos(0, height, 0.0).tex(0, 0).endVertex();
        buffer.pos(width, height, 0.0).tex(1, 0).endVertex();
        buffer.pos(width, 0, 0.0).tex(1, 1).endVertex();
        buffer.pos(0, 0, 0.0).tex(0, 1).endVertex();
        tessellator.draw();
    }
}
