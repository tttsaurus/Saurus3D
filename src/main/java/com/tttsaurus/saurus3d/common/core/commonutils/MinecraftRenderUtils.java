package com.tttsaurus.saurus3d.common.core.commonutils;

import com.tttsaurus.saurus3d.common.core.function.Func;
import com.tttsaurus.saurus3d.common.core.function.Func_1Param;
import com.tttsaurus.saurus3d.common.core.reflection.AccessorUnreflector;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Vec3d;
import org.joml.Vector2f;
import org.joml.Vector3f;
import java.nio.FloatBuffer;

public final class MinecraftRenderUtils
{
    //<editor-fold desc="active render info">
    // inspired by <https://github.com/Laike-Endaril/Fantastic-Lib/blob/669c3306bbebca9de1c3959e6dd4203b5b7215d4/src/main/java/com/fantasticsource/mctools/Render.java>
    private static boolean isActiveRenderInfoGettersInit = false;
    private static Func<FloatBuffer> modelViewMatrixGetter = null;
    private static Func<FloatBuffer> projectionMatrixGetter = null;

    public static FloatBuffer getModelViewMatrix()
    {
        if (!isActiveRenderInfoGettersInit) initActiveRenderInfoGetters();
        return modelViewMatrixGetter.invoke();
    }
    public static FloatBuffer getProjectionMatrix()
    {
        if (!isActiveRenderInfoGettersInit) initActiveRenderInfoGetters();
        return projectionMatrixGetter.invoke();
    }

    @SuppressWarnings("all")
    private static void initActiveRenderInfoGetters()
    {
        isActiveRenderInfoGettersInit = true;
        modelViewMatrixGetter = (Func<FloatBuffer>) AccessorUnreflector.getDeclaredFieldGetter(ActiveRenderInfo.class, "MODELVIEW", "field_178812_b");
        projectionMatrixGetter = (Func<FloatBuffer>) AccessorUnreflector.getDeclaredFieldGetter(ActiveRenderInfo.class, "PROJECTION", "field_178813_c");
    }
    //</editor-fold>

    //<editor-fold desc="partial ticks">
    // inspired by <https://github.com/Laike-Endaril/Fantastic-Lib/blob/669c3306bbebca9de1c3959e6dd4203b5b7215d4/src/main/java/com/fantasticsource/mctools/Render.java>
    private static boolean isPartialTickGetterInit = false;
    private static Func_1Param<Float, Minecraft> partialTickGetter = null;

    public static double getPartialTick()
    {
        if (!isPartialTickGetterInit) initPartialTickGetter();
        Minecraft minecraft = Minecraft.getMinecraft();
        return minecraft.isGamePaused() ? partialTickGetter.invoke(minecraft) : minecraft.getRenderPartialTicks();
    }

    @SuppressWarnings("all")
    private static void initPartialTickGetter()
    {
        isPartialTickGetterInit = true;
        partialTickGetter = (Func_1Param<Float, Minecraft>) AccessorUnreflector.getDeclaredFieldGetter(Minecraft.class, "renderPartialTicksPaused", "field_193996_ah");
        partialTickGetter.invoke(Minecraft.getMinecraft());
    }
    //</editor-fold>

    //<editor-fold desc="camera">
    public static Vector3f getWorldOffset()
    {
        Entity camera = Minecraft.getMinecraft().getRenderViewEntity();
        if (camera == null) camera = Minecraft.getMinecraft().player;
        double partialTicks = getPartialTick();

        double camX = camera.lastTickPosX + (camera.posX - camera.lastTickPosX) * partialTicks;
        double camY = camera.lastTickPosY + (camera.posY - camera.lastTickPosY) * partialTicks;
        double camZ = camera.lastTickPosZ + (camera.posZ - camera.lastTickPosZ) * partialTicks;

        return new Vector3f((float)camX, (float)camY, (float)camZ);
    }
    public static Vector3f getCameraPos()
    {
        Entity camera = Minecraft.getMinecraft().getRenderViewEntity();
        if (camera == null) camera = Minecraft.getMinecraft().player;
        double partialTicks = getPartialTick();

        double camX = camera.lastTickPosX + (camera.posX - camera.lastTickPosX) * partialTicks;
        double camY = camera.lastTickPosY + (camera.posY - camera.lastTickPosY) * partialTicks + camera.getEyeHeight();
        double camZ = camera.lastTickPosZ + (camera.posZ - camera.lastTickPosZ) * partialTicks;

        return new Vector3f((float)camX, (float)camY, (float)camZ);
    }
    public static Vector2f getCameraRotationInDegree()
    {
        Entity viewEntity = Minecraft.getMinecraft().getRenderViewEntity();
        if (viewEntity == null) return new Vector2f(0, 0);
        return new Vector2f(viewEntity.rotationYaw, viewEntity.rotationPitch);
    }
    public static Vector2f getCameraRotationInRadian()
    {
        Entity viewEntity = Minecraft.getMinecraft().getRenderViewEntity();
        if (viewEntity == null) return new Vector2f(0, 0);
        return new Vector2f((float)Math.toRadians(viewEntity.rotationYaw), (float)Math.toRadians(viewEntity.rotationPitch));
    }
    //</editor-fold>
}
