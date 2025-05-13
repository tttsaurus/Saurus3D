package com.tttsaurus.saurus3d.test;

import com.tttsaurus.saurus3d.Saurus3D;
import com.tttsaurus.saurus3d.common.core.mesh.Mesh;
import com.tttsaurus.saurus3d.common.core.shader.Shader;
import com.tttsaurus.saurus3d.common.core.shader.ShaderLoadingUtils;
import com.tttsaurus.saurus3d.common.core.shader.ShaderProgram;
import com.tttsaurus.saurus3d.common.impl.model.ObjModelLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.vector.Matrix4f;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;
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

    static MethodHandles.Lookup lookup = MethodHandles.lookup();
    static MethodHandle handle1 = null;
    static MethodHandle handle2 = null;

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

            Shader vertex = ShaderLoadingUtils.load("saurus3d:obj/test/shader_vertex.glsl", Shader.ShaderType.VERTEX);
            Shader frag = ShaderLoadingUtils.load("saurus3d:obj/test/shader_frag.glsl", Shader.ShaderType.FRAGMENT);

            ShaderProgram shaderProgram = new ShaderProgram(vertex, frag);
            shaderProgram.setup();

            Matrix4f transform = new Matrix4f();
            transform.setIdentity();
            //transform.translate(new Vector3f(0.5f, -0.5f, -1.0f));
            //transform.scale(new Vector3f(0.2f, 0.2f, 0.2f));
            FloatBuffer matrixBuffer = BufferUtils.createFloatBuffer(16);
            transform.store(matrixBuffer);
            matrixBuffer.flip();

            shaderProgram.use();
            shaderProgram.setUniform("u_transform", matrixBuffer);
            shaderProgram.setUniform("u_color", 1.0f, 0.5f, 0.5f, 1.0f);

//            FloatBuffer projection = BufferUtils.createFloatBuffer(16);
//            FloatBuffer modelView = BufferUtils.createFloatBuffer(16);
//
//            GL11.glGetFloat(GL11.GL_PROJECTION_MATRIX, projection);
//            GL11.glGetFloat(GL11.GL_MODELVIEW_MATRIX, modelView);

            FloatBuffer modelView;
            FloatBuffer projection;

            if (handle1 == null)
            {
                try
                {
                    Field field = ActiveRenderInfo.class.getDeclaredField("field_178812_b");
                    field.setAccessible(true);
                    handle1 = lookup.unreflectGetter(field);
                }
                catch (Exception e)
                {
                    throw new RuntimeException(e);
                }
            }
            if (handle2 == null)
            {
                try
                {
                    Field field = ActiveRenderInfo.class.getDeclaredField("field_178813_c");
                    field.setAccessible(true);
                    handle2 = lookup.unreflectGetter(field);
                }
                catch (Exception e)
                {
                    throw new RuntimeException(e);
                }
            }

            try { modelView = (FloatBuffer)handle1.invoke(); }
            catch (Throwable e) { throw new RuntimeException(e); }

            try { projection = (FloatBuffer)handle2.invoke(); }
            catch (Throwable e) { throw new RuntimeException(e); }

            shaderProgram.setUniform("projection", projection);
            shaderProgram.setUniform("modelView", modelView);

            RenderManager renderManager = Minecraft.getMinecraft().getRenderManager();
            shaderProgram.setUniform("camPos", (float)renderManager.viewerPosX, (float)renderManager.viewerPosY, (float)renderManager.viewerPosZ);

            GlStateManager.disableAlpha();
            GlStateManager.enableBlend();
            GlStateManager.disableAlpha();
            GlStateManager.enableDepth();
            GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);

            if (flag2)
            {
                flag2 = false;
                Saurus3D.LOGGER.info(shaderProgram.getSetupDebugReport());
            }

            mesh.render();

            shaderProgram.unuse();
            shaderProgram.dispose();

            GL11.glPopMatrix();
        }

        restoreCommonGlStates();
    }

    static boolean flag = true;
    static boolean flag2 = true;
}
