package com.tttsaurus.saurus3d.common.impl.model;

import com.tttsaurus.saurus3d.common.api.shader.Shader;
import com.tttsaurus.saurus3d.common.api.shader.ShaderProgram;
import com.tttsaurus.saurus3d.common.impl.shader.ShaderLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.*;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector3f;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

public final class Test2
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

    @SubscribeEvent
    public static void onRenderWorldLast(RenderWorldLastEvent event)
    {
        storeCommonGlStates();

        int prevVao = GL11.glGetInteger(GL30.GL_VERTEX_ARRAY_BINDING);
        int prevVbo = GL11.glGetInteger(GL15.GL_ARRAY_BUFFER_BINDING);
        int prevEbo = GL11.glGetInteger(GL15.GL_ELEMENT_ARRAY_BUFFER_BINDING);

        int vao;
        int vbo;
        int ebo;

        int instanceVBO;

        float[] vertices = new float[]
        {
                // positions         // texcoords   // normals
                -0.5f, -0.5f, 0.0f,  0.0f, 0.0f,    0.0f, 0.0f, 1.0f,  // bottom-left
                0.5f, -0.5f, 0.0f,   1.0f, 0.0f,    0.0f, 0.0f, 1.0f,  // bottom-right
                0.0f, 0.5f, 0.0f,    0.5f, 1.0f,    0.0f, 0.0f, 1.0f   // top
        };

        int[] indices = new int[]
        {
                // triangle: bottom-left, bottom-right, top
                0, 1, 2
        };

        float[] instanceOffsets = new float[]
                {
                        -0.1f, 0.0f, 0.0f,  // First instance: Shift left
                        0.1f, 0.0f, 0.0f   // Second instance: Shift right
                };

        vao = GL30.glGenVertexArrays();
        GL30.glBindVertexArray(vao);

        FloatBuffer vertexBuffer = ByteBuffer.allocateDirect(vertices.length * Float.BYTES)
                .order(ByteOrder.nativeOrder()).asFloatBuffer();
        vertexBuffer.put(vertices).flip();

        IntBuffer indexBuffer = ByteBuffer.allocateDirect(indices.length * Integer.BYTES)
                .order(ByteOrder.nativeOrder()).asIntBuffer();
        indexBuffer.put(indices).flip();

        vbo = GL15.glGenBuffers();
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vbo);
        GL15.glBufferData(GL15.GL_ARRAY_BUFFER, vertexBuffer, GL15.GL_STATIC_DRAW);

        ebo = GL15.glGenBuffers();
        GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, ebo);
        GL15.glBufferData(GL15.GL_ELEMENT_ARRAY_BUFFER, indexBuffer, GL15.GL_STATIC_DRAW);

        // first 3 floats for position
        GL20.glVertexAttribPointer(0, 3, GL11.GL_FLOAT, false, 8 * Float.BYTES, 0);
        GL20.glEnableVertexAttribArray(0);

        // next 2 floats for texCoord
        GL20.glVertexAttribPointer(1, 2, GL11.GL_FLOAT, false, 8 * Float.BYTES, 3 * Float.BYTES);
        GL20.glEnableVertexAttribArray(1);

        // last 3 floats for normal
        GL20.glVertexAttribPointer(2, 3, GL11.GL_FLOAT, false, 8 * Float.BYTES, 5 * Float.BYTES);
        GL20.glEnableVertexAttribArray(2);

        instanceVBO = GL15.glGenBuffers();
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, instanceVBO);
        FloatBuffer instanceBuffer = ByteBuffer.allocateDirect(instanceOffsets.length * Float.BYTES)
                .order(ByteOrder.nativeOrder()).asFloatBuffer();
        instanceBuffer.put(instanceOffsets).flip();
        GL15.glBufferData(GL15.GL_ARRAY_BUFFER, instanceBuffer, GL15.GL_STATIC_DRAW);

        GL20.glVertexAttribPointer(3, 3, GL11.GL_FLOAT, false, 3 * Float.BYTES, 0);
        GL20.glEnableVertexAttribArray(3);
        GL33.glVertexAttribDivisor(3, 1);

        // trying to be safe so we unbind
        GL30.glBindVertexArray(prevVao);
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, prevVbo);
        GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, prevEbo);

        // render part
        useShader(); // we don't care what shaders are for now

        GL30.glBindVertexArray(vao);
        GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, ebo);

        //GL11.glDrawElements(GL11.GL_TRIANGLES, indices.length, GL11.GL_UNSIGNED_INT, 0);
        GL31.glDrawElementsInstanced(GL11.GL_TRIANGLES, indices.length, GL11.GL_UNSIGNED_INT, 0, 2);

        GL30.glBindVertexArray(0);

        unuseShader(); // we don't care what shaders are for now

        restoreCommonGlStates();
    }


    static ShaderProgram shaderProgram;
    static void useShader()
    {
        ShaderLoader shaderLoader = new ShaderLoader();
        Shader vertex = shaderLoader.load("saurus3d:obj/test/shader_vertex.glsl", Shader.ShaderType.VERTEX);
        Shader frag = shaderLoader.load("saurus3d:obj/test/shader_frag.glsl", Shader.ShaderType.FRAGMENT);

        shaderProgram = new ShaderProgram(vertex, frag);
        shaderProgram.setup();

        Matrix4f transform = new Matrix4f();
        transform.setIdentity();
        //transform.translate(new Vector3f(0.5f, -0.5f, -1.0f));
        transform.scale(new Vector3f(1f, 1f, 1f));
        FloatBuffer matrixBuffer = BufferUtils.createFloatBuffer(16);
        transform.store(matrixBuffer);
        matrixBuffer.flip();

        shaderProgram.use();
        shaderProgram.setUniform("u_transform", matrixBuffer);
        shaderProgram.setUniform("u_color", 1.0f, 0.5f, 0.5f, 0.5f);

        GlStateManager.enableBlend();
        GlStateManager.disableAlpha();
        GlStateManager.disableDepth();
        GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
    }
    static void unuseShader()
    {
        shaderProgram.unuse();
        shaderProgram.dispose();
    }
}
