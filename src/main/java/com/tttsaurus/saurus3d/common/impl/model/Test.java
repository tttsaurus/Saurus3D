package com.tttsaurus.saurus3d.common.impl.model;

import com.tttsaurus.saurus3d.Saurus3D;
import com.tttsaurus.saurus3d.common.api.model.Mesh;
import com.tttsaurus.saurus3d.common.api.shader.Shader;
import com.tttsaurus.saurus3d.common.api.shader.ShaderProgram;
import com.tttsaurus.saurus3d.common.impl.shader.ShaderLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector3f;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

public final class Test
{

    //<editor-fold desc="gl states">
    private static int textureID = 0;
    private static float r = 0, g = 0, b = 0, a = 0;
    private static boolean blend = false;
    private static boolean lighting = false;
    private static boolean texture2D = false;
    private static boolean alphaTest = false;
    private static int shadeModel = 0;
    private static boolean depthTest = false;
    private static boolean cullFace = false;

    private static final IntBuffer intBuffer = ByteBuffer.allocateDirect(16 << 2).order(ByteOrder.nativeOrder()).asIntBuffer();
    private static final FloatBuffer floatBuffer = ByteBuffer.allocateDirect(16 << 2).order(ByteOrder.nativeOrder()).asFloatBuffer();
    //</editor-fold>

    private static void storeCommonGlStates()
    {
        GL11.glGetInteger(GL11.GL_TEXTURE_BINDING_2D, intBuffer);
        textureID = intBuffer.get(0);
        GL11.glGetFloat(GL11.GL_CURRENT_COLOR, floatBuffer);
        r = floatBuffer.get(0);
        g = floatBuffer.get(1);
        b = floatBuffer.get(2);
        a = floatBuffer.get(3);
        blend = GL11.glIsEnabled(GL11.GL_BLEND);
        lighting = GL11.glIsEnabled(GL11.GL_LIGHTING);
        texture2D = GL11.glIsEnabled(GL11.GL_TEXTURE_2D);
        alphaTest = GL11.glIsEnabled(GL11.GL_ALPHA_TEST);
        GL11.glGetInteger(GL11.GL_SHADE_MODEL, intBuffer);
        shadeModel = intBuffer.get(0);
        depthTest = GL11.glIsEnabled(GL11.GL_DEPTH_TEST);
        cullFace = GL11.glIsEnabled(GL11.GL_CULL_FACE);
    }
    private static void restoreCommonGlStates()
    {
        if (cullFace)
            GlStateManager.enableCull();
        else
            GlStateManager.disableCull();
        if (depthTest)
            GlStateManager.enableDepth();
        else
            GlStateManager.disableDepth();
        GlStateManager.shadeModel(shadeModel);
        if (alphaTest)
            GlStateManager.enableAlpha();
        else
            GlStateManager.disableAlpha();
        if (texture2D)
            GlStateManager.enableTexture2D();
        else
            GlStateManager.disableTexture2D();
        if (lighting)
            GlStateManager.enableLighting();
        else
            GlStateManager.disableLighting();
        if (blend)
            GlStateManager.enableBlend();
        else
            GlStateManager.disableBlend();
        GlStateManager.color(r, g, b, a);
        GlStateManager.bindTexture(textureID);
    }

    static Mesh mesh = null;
    @SubscribeEvent
    public static void onRenderWorldLast(RenderWorldLastEvent event)
    {
        storeCommonGlStates();

        if (flag)
        {
            flag = false;
            ObjModelLoader loader = new ObjModelLoader();
            mesh = loader.load("saurus3d:obj/test/model.obj");

//            float[] vertices = new float[]{
//                    // Positions       // TexCoords // Normals
//                    -0.5f, -0.5f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f,  // Bottom-left
//                    0.5f, -0.5f, 0.0f, 1.0f, 0.0f, 0.0f, 0.0f, 1.0f,  // Bottom-right
//                    0.0f, 0.5f, 0.0f, 0.5f, 1.0f, 0.0f, 0.0f, 1.0f   // Top
//            };
//
//            int[] indices = new int[]{
//                    0, 1, 2  // Triangle: Bottom-left, Bottom-right, Top
//            };
//
//            mesh = new Mesh(vertices, indices);

            Saurus3D.LOGGER.info("V len: " + mesh.getVerticesLength());
            Saurus3D.LOGGER.info("I len: " + mesh.getIndicesLength());
            if (Minecraft.getMinecraft().player != null)
                Minecraft.getMinecraft().player.sendChatMessage("finish");

            mesh.setup();
        }

        if (mesh != null)
        {
            GL11.glPushMatrix();

            GlStateManager.disableCull();
            GlStateManager.enableDepth();
            GlStateManager.disableTexture2D();
            GlStateManager.disableLighting();
            GlStateManager.disableBlend();

            EntityPlayerSP player = Minecraft.getMinecraft().player;
            if (player == null) return;

            // Get the current world
            double playerX = player.prevPosX + (player.posX - player.prevPosX) * event.getPartialTicks();
            double playerY = player.prevPosY + (player.posY - player.prevPosY) * event.getPartialTicks();
            double playerZ = player.prevPosZ + (player.posZ - player.prevPosZ) * event.getPartialTicks();

            // Set up the OpenGL rendering state
            GlStateManager.translate(-(float)(playerX), -(float)(playerY) + 100, -(float)(playerZ));

            //GlStateManager.translate(10f, 10f, 0f);

            GlStateManager.scale(10f, 10f, 10f);
            GlStateManager.glLineWidth(3.0F);

            // Start drawing the box (a simple cube in wireframe mode)
            Tessellator tessellator = Tessellator.getInstance();
            BufferBuilder buffer = tessellator.getBuffer();

            // Begin drawing with GL_LINES
            buffer.begin(GL11.GL_LINES, DefaultVertexFormats.POSITION_COLOR);

            // Define the 8 corners of the box (adjust size and position as needed)
            double size = 1.0; // Box size

            buffer.pos(0, 0, 0).color(1.0F, 1.0F, 1.0F, 1.0F).endVertex(); // Corner 1
            buffer.pos(size, 0, 0).color(1.0F, 1.0F, 1.0F, 1.0F).endVertex(); // Corner 2
            buffer.pos(size, 0, 0).color(1.0F, 1.0F, 1.0F, 1.0F).endVertex(); // Corner 2
            buffer.pos(size, size, 0).color(1.0F, 1.0F, 1.0F, 1.0F).endVertex(); // Corner 3

            buffer.pos(size, size, 0).color(1.0F, 1.0F, 1.0F, 1.0F).endVertex(); // Corner 3
            buffer.pos(0, size, 0).color(1.0F, 1.0F, 1.0F, 1.0F).endVertex(); // Corner 4
            buffer.pos(0, size, 0).color(1.0F, 1.0F, 1.0F, 1.0F).endVertex(); // Corner 4
            buffer.pos(0, 0, 0).color(1.0F, 1.0F, 1.0F, 1.0F).endVertex(); // Corner 1

            buffer.pos(0, 0, size).color(1.0F, 1.0F, 1.0F, 1.0F).endVertex(); // Corner 5
            buffer.pos(size, 0, size).color(1.0F, 1.0F, 1.0F, 1.0F).endVertex(); // Corner 6

            buffer.pos(size, 0, size).color(1.0F, 1.0F, 1.0F, 1.0F).endVertex(); // Corner 6
            buffer.pos(size, size, size).color(1.0F, 1.0F, 1.0F, 1.0F).endVertex(); // Corner 7

            buffer.pos(size, size, size).color(1.0F, 1.0F, 1.0F, 1.0F).endVertex(); // Corner 7
            buffer.pos(0, size, size).color(1.0F, 1.0F, 1.0F, 1.0F).endVertex(); // Corner 8

            buffer.pos(0, size, size).color(1.0F, 1.0F, 1.0F, 1.0F).endVertex(); // Corner 8
            buffer.pos(0, 0, size).color(1.0F, 1.0F, 1.0F, 1.0F).endVertex(); // Corner 5

            buffer.pos(0, 0, 0).color(1.0F, 1.0F, 1.0F, 1.0F).endVertex(); // Corner 1
            buffer.pos(0, 0, size).color(1.0F, 1.0F, 1.0F, 1.0F).endVertex(); // Corner 5

            buffer.pos(size, 0, 0).color(1.0F, 1.0F, 1.0F, 1.0F).endVertex(); // Corner 2
            buffer.pos(size, 0, size).color(1.0F, 1.0F, 1.0F, 1.0F).endVertex(); // Corner 6

            buffer.pos(size, size, 0).color(1.0F, 1.0F, 1.0F, 1.0F).endVertex(); // Corner 3
            buffer.pos(size, size, size).color(1.0F, 1.0F, 1.0F, 1.0F).endVertex(); // Corner 7

            buffer.pos(0, size, 0).color(1.0F, 1.0F, 1.0F, 1.0F).endVertex(); // Corner 4
            buffer.pos(0, size, size).color(1.0F, 1.0F, 1.0F, 1.0F).endVertex(); // Corner 8

            tessellator.draw();

            ShaderLoader shaderLoader = new ShaderLoader();
            Shader vertex = shaderLoader.load("saurus3d:obj/test/shader_vertex.glsl", Shader.ShaderType.VERTEX);
            Shader frag = shaderLoader.load("saurus3d:obj/test/shader_frag.glsl", Shader.ShaderType.FRAGMENT);

            ShaderProgram shaderProgram = new ShaderProgram(vertex, frag);
            shaderProgram.setup();

            if (flag2)
            {
                flag2 = false;
                Saurus3D.LOGGER.info(shaderProgram.getSetupDebugReport());
            }

            Matrix4f transform = new Matrix4f();
            transform.setIdentity(); // Start with identity
            transform.translate(new Vector3f(0.5f, -0.5f, -1.0f)); // Translate
            transform.scale(new Vector3f(0.5f, 0.5f, 0.5f)); // Scale

            FloatBuffer matrixBuffer = BufferUtils.createFloatBuffer(16);
            transform.store(matrixBuffer);
            matrixBuffer.flip();

            shaderProgram.use();
            GL20.glUniformMatrix4(shaderProgram.getUniformLocation("u_transform"), false, matrixBuffer);

            mesh.render();

            shaderProgram.dispose();

            GL20.glUseProgram(0);

            GL11.glPopMatrix();
        }

        restoreCommonGlStates();
    }

    static boolean flag = true;
    static boolean flag2 = true;
}
