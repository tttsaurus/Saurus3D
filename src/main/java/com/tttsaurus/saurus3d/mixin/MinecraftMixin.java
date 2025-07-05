package com.tttsaurus.saurus3d.mixin;

import com.tttsaurus.saurus3d.Saurus3D;
import com.tttsaurus.saurus3d.common.core.commonutils.MinecraftRenderUtils;
import com.tttsaurus.saurus3d.common.core.function.Action;
import com.tttsaurus.saurus3d.common.core.gl.debug.KHRDebugManager;
import com.tttsaurus.saurus3d.common.core.gl.version.GLVersionHelper;
import com.tttsaurus.saurus3d.common.core.shutdown.ShutdownHooks;
import com.tttsaurus.saurus3d.config.ConfigFileHelper;
import com.tttsaurus.saurus3d.config.Saurus3DGLDebugConfig;
import com.tttsaurus.saurus3d.config.Saurus3DMCPatchesConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraftforge.common.config.Configuration;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.*;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;

@Mixin(Minecraft.class)
public class MinecraftMixin
{
    @Inject(method = "shutdown", at = @At("HEAD"))
    private void beforeShutdown(CallbackInfo ci)
    {
        for (Action action: ShutdownHooks.hooks)
            action.invoke();
    }

    @Inject(method = "init", at = @At("HEAD"))
    private void beforeInit(CallbackInfo info)
    {
        if (Saurus3D.isCleanroom())
            Saurus3D.LOGGER.info("Saurus3D is running under Cleanroom.");

        Saurus3DGLDebugConfig.CONFIG = new Configuration(ConfigFileHelper.makeFile("gl_debug"));
        Saurus3DGLDebugConfig.loadConfig();
        Saurus3D.LOGGER.info("Saurus3D GL Debug Config loaded.");

        Saurus3DMCPatchesConfig.CONFIG = new Configuration(ConfigFileHelper.makeFile("mc_patches"));
        Saurus3DMCPatchesConfig.loadConfig();
        Saurus3D.LOGGER.info("Saurus3D MC Patches Config loaded.");
    }

    // just before creating display
    @Inject(method = "init", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Minecraft;setInitialDisplayMode()V", shift = At.Shift.AFTER))
    private void beforeCreateDisplay(CallbackInfo ci)
    {
        if (Saurus3DGLDebugConfig.ENABLE_AUTO_DEBUG)
        {
            GLFW.glfwWindowHint(GLFW.GLFW_OPENGL_DEBUG_CONTEXT, GLFW.GLFW_TRUE);
            Saurus3D.LOGGER.info("GLFW debug context hint enabled.");
        }
    }

    // just created display
    @Inject(method = "init", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Minecraft;createDisplay()V", shift = At.Shift.AFTER))
    private void afterCreateDisplay(CallbackInfo info)
    {
        GL.createCapabilities();
        Saurus3D.glCap = GL.getCapabilities();

        //<editor-fold desc="GL version">
        int majorGLVersion = GLVersionHelper.getMajorGLVersion();
        int minorGLVersion = GLVersionHelper.getMinorGLVersion();
        Saurus3D.LOGGER.info(String.format("OpenGL Version: %d.%d", majorGLVersion, minorGLVersion));
        //</editor-fold>

        //<editor-fold desc="GL auto debug">
        if (Saurus3D.glCap.GL_KHR_debug || GLVersionHelper.supported(4, 3))
        {
            if (Saurus3DGLDebugConfig.ENABLE_AUTO_DEBUG)
            {
                try
                {
                    Method method = KHRDebugManager.class.getDeclaredMethod("enable", List.class);
                    method.setAccessible(true);
                    method.invoke(null, new Object[]{Saurus3DGLDebugConfig.AUTO_DEBUG_MSG_FILTERS});
                }
                catch (Exception ignored) { }
            }
        }

        if (KHRDebugManager.isEnable())
            Saurus3D.LOGGER.info("GL Auto Debug is enabled.");
        else
            Saurus3D.LOGGER.info("GL Auto Debug is disabled.");
        //</editor-fold>

        //<editor-fold desc="init RenderUtils">
        Saurus3D.LOGGER.info("Start initializing MinecraftRenderUtils.");
        boolean successful = true;
        try
        {
            MinecraftRenderUtils.getModelViewMatrix();
            Saurus3D.LOGGER.info("MinecraftRenderUtils.getModelViewMatrix() is ready.");
        }
        catch (Throwable throwable)
        {
            successful = false;
            Saurus3D.LOGGER.warn("MinecraftRenderUtils.getModelViewMatrix() is not ready.");
            Saurus3D.LOGGER.throwing(throwable);
        }
        try
        {
            MinecraftRenderUtils.getProjectionMatrix();
            Saurus3D.LOGGER.info("MinecraftRenderUtils.getProjectionMatrix() is ready.");
        }
        catch (Throwable throwable)
        {
            successful = false;
            Saurus3D.LOGGER.warn("MinecraftRenderUtils.getProjectionMatrix() is not ready.");
            Saurus3D.LOGGER.throwing(throwable);
        }
        try
        {
            MinecraftRenderUtils.getPartialTick();
            Saurus3D.LOGGER.info("MinecraftRenderUtils.getPartialTick() is ready.");
        }
        catch (Throwable throwable)
        {
            successful = false;
            Saurus3D.LOGGER.warn("MinecraftRenderUtils.getPartialTick() is not ready.");
            Saurus3D.LOGGER.throwing(throwable);
        }
        Saurus3D.LOGGER.info("Finished initializing MinecraftRenderUtils. The module is " + (successful ? "READY." : "NOT READY."));
        //</editor-fold>
    }

    // just set render global
    @Inject(method = "init", at = @At(value = "FIELD", target = "Lnet/minecraft/client/Minecraft;renderGlobal:Lnet/minecraft/client/renderer/RenderGlobal;", opcode = Opcodes.PUTFIELD, shift = At.Shift.AFTER))
    private void afterSetRenderGlobal(CallbackInfo ci)
    {
        Field renderContainerField = null;
        try
        {
            renderContainerField = RenderGlobal.class.getDeclaredField("renderContainer");
        }
        catch (Exception ignored)
        {
            try
            {
                renderContainerField = RenderGlobal.class.getDeclaredField("field_174996_N");
            }
            catch (Exception ignored2) { }
        }

        if (renderContainerField != null)
        {
            renderContainerField.setAccessible(true);
            try
            {
//                renderContainerField.set(Minecraft.getMinecraft().renderGlobal, new MyVboRenderList());
//                Saurus3D.LOGGER.info("Set renderContainer to MyVboRenderList.");
            }
            catch (Exception ignored) { }
        }
    }
}
