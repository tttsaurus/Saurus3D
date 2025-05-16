package com.tttsaurus.saurus3d.mixin.early;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.tttsaurus.saurus3d.Saurus3D;
import com.tttsaurus.saurus3d.common.core.RenderUtils;
import com.tttsaurus.saurus3d.common.core.function.Action;
import com.tttsaurus.saurus3d.common.core.gl.debug.KHRDebugManager;
import com.tttsaurus.saurus3d.common.core.gl.debug.DebugMessageFilter;
import com.tttsaurus.saurus3d.common.core.gl.feature.GLFeatureManager;
import com.tttsaurus.saurus3d.common.core.gl.feature.IGLFeature;
import com.tttsaurus.saurus3d.common.core.gl.feature.Saurus3DGLFeature;
import com.tttsaurus.saurus3d.common.core.gl.version.GLVersionHelper;
import com.tttsaurus.saurus3d.common.core.shutdown.ShutdownHooks;
import com.tttsaurus.saurus3d.config.ConfigFileHelper;
import com.tttsaurus.saurus3d.config.Saurus3DGLDebugConfig;
import com.tttsaurus.saurus3d.config.Saurus3DGLFeatureConfig;
import com.tttsaurus.saurus3d.test.MyVboRenderList;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraftforge.common.config.Configuration;
import org.lwjgl.LWJGLException;
import org.lwjgl.opengl.ContextAttribs;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.PixelFormat;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Map;

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
        Saurus3DGLDebugConfig.CONFIG = new Configuration(ConfigFileHelper.makeFile("gl_debug"));
        Saurus3DGLDebugConfig.loadConfig();
        Saurus3D.LOGGER.info("Saurus3D GL Debug Config loaded.");

        Saurus3DGLFeatureConfig.CONFIG = ConfigFileHelper.makeFile("gl_feature_classes");
        Saurus3DGLFeatureConfig.loadConfig();
        Saurus3D.LOGGER.info("Saurus3D GL Feature Config loaded.");
    }

    //<editor-fold desc="GL debug context">
    @WrapOperation(method = "createDisplay", at = @At(value = "INVOKE", target = "Lorg/lwjgl/opengl/Display;create(Lorg/lwjgl/opengl/PixelFormat;)V", remap = false))
    private void createDisplayInTry(PixelFormat pixelFormat, Operation<Void> original) throws LWJGLException
    {
        if (Saurus3DGLDebugConfig.ENABLE_AUTO_DEBUG)
            Display.create(pixelFormat, new ContextAttribs(1, 0, 0, ContextAttribs.CONTEXT_DEBUG_BIT_ARB));
        else
            original.call(pixelFormat);
    }

    @WrapOperation(method = "createDisplay", at = @At(value = "INVOKE", target = "Lorg/lwjgl/opengl/Display;create()V", remap = false))
    private void createDisplayInCatch(Operation<Void> original) throws LWJGLException
    {
        if (Saurus3DGLDebugConfig.ENABLE_AUTO_DEBUG)
            Display.create(new PixelFormat(), new ContextAttribs(1, 0, 0, ContextAttribs.CONTEXT_DEBUG_BIT_ARB));
        else
            original.call();
    }
    //</editor-fold>

    // just created GL context
    @Inject(method = "init", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Minecraft;createDisplay()V", shift = At.Shift.AFTER))
    private void afterCreateDisplay(CallbackInfo info)
    {
        //<editor-fold desc="GL version">
        int majorGLVersion = GLVersionHelper.getMajorGLVersion();
        int minorGLVersion = GLVersionHelper.getMinorGLVersion();
        Saurus3D.LOGGER.info(String.format("OpenGL Version: %d.%d", majorGLVersion, minorGLVersion));
        //</editor-fold>

        //<editor-fold desc="GL features initialization">
        Method checkAvailabilityMethod = null;
        try
        {
            checkAvailabilityMethod = GLFeatureManager.class.getDeclaredMethod("checkAvailability", String.class, Class.class);
        }
        catch (Exception ignored) { }

        if (checkAvailabilityMethod != null)
        {
            checkAvailabilityMethod.setAccessible(true);

            Saurus3D.LOGGER.info("");
            Saurus3D.LOGGER.info("Start checking Saurus3D GL feature availabilities.");
            for (Class<? extends IGLFeature> featureClass: Saurus3DGLFeatureConfig.FEATURE_CLASSES)
            {
                try
                {
                    String featureName = featureClass.getAnnotation(Saurus3DGLFeature.class).value();
                    checkAvailabilityMethod.invoke(null, new Object[]{featureName, featureClass});
                }
                catch (Throwable throwable)
                {
                    Saurus3D.LOGGER.throwing(throwable);
                }
            }
            Saurus3D.LOGGER.info("Finished checking Saurus3D GL feature availabilities.");
        }

        Saurus3D.LOGGER.info("");
        Saurus3D.LOGGER.info("Saurus3D GL features: ");
        for (Map.Entry<String, Boolean> entry: GLFeatureManager.getAvailability().entrySet())
            Saurus3D.LOGGER.info("- Feature " + entry.getKey() + " is " + (entry.getValue() ? "" : "not ") + "available.");
        Saurus3D.LOGGER.info("");
        //</editor-fold>

        //<editor-fold desc="GL auto debug">
        GLFeatureManager.require("KHR_DEBUG").run(() ->
        {
            if (Saurus3DGLDebugConfig.ENABLE_AUTO_DEBUG)
            {
                try
                {
                    Method method = KHRDebugManager.class.getDeclaredMethod("enable", DebugMessageFilter.class);
                    method.setAccessible(true);
                    method.invoke(null, new Object[]{Saurus3DGLDebugConfig.AUTO_DEBUG_MSG_FILTER});
                }
                catch (Exception ignored) { }
            }
        });

        if (KHRDebugManager.isEnable())
            Saurus3D.LOGGER.info("GL Auto Debug is enabled.");
        else
            Saurus3D.LOGGER.info("GL Auto Debug is disabled.");
        //</editor-fold>

        //<editor-fold desc="init RenderUtils">
        Saurus3D.LOGGER.info("");
        Saurus3D.LOGGER.info("Start initializing RenderUtils.");
        try
        {
            RenderUtils.getModelViewMatrix();
            Saurus3D.LOGGER.info("RenderUtils.getModelViewMatrix() is ready.");
        }
        catch (Throwable throwable)
        {
            Saurus3D.LOGGER.warn("RenderUtils.getModelViewMatrix() is not ready.");
            Saurus3D.LOGGER.throwing(throwable);
        }
        try
        {
            RenderUtils.getProjectionMatrix();
            Saurus3D.LOGGER.info("RenderUtils.getProjectionMatrix() is ready.");
        }
        catch (Throwable throwable)
        {
            Saurus3D.LOGGER.warn("RenderUtils.getProjectionMatrix() is not ready.");
            Saurus3D.LOGGER.throwing(throwable);
        }
        try
        {
            RenderUtils.getPartialTick();
            Saurus3D.LOGGER.info("RenderUtils.getPartialTick() is ready.");
        }
        catch (Throwable throwable)
        {
            Saurus3D.LOGGER.warn("RenderUtils.getPartialTick() is not ready.");
            Saurus3D.LOGGER.throwing(throwable);
        }
        Saurus3D.LOGGER.info("Finished initializing RenderUtils.");
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
                renderContainerField.set(Minecraft.getMinecraft().renderGlobal, new MyVboRenderList());
                Saurus3D.LOGGER.info("Set renderContainer to MyVboRenderList.");
            }
            catch (Exception ignored) { }
        }
    }
}
