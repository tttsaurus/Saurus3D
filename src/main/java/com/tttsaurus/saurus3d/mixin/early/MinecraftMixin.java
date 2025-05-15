package com.tttsaurus.saurus3d.mixin.early;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.tttsaurus.saurus3d.Saurus3D;
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
import net.minecraft.client.Minecraft;
import net.minecraftforge.common.config.Configuration;
import org.lwjgl.LWJGLException;
import org.lwjgl.opengl.ContextAttribs;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.PixelFormat;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
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
        Method addFeatureMethod = null;
        Method checkAvailabilityMethod = null;
        try
        {
            addFeatureMethod = GLFeatureManager.class.getDeclaredMethod("addFeature", String.class, Class.class);
            checkAvailabilityMethod = GLFeatureManager.class.getDeclaredMethod("checkAvailability", String.class, Class.class);
        }
        catch (Exception ignored) { }

        if (addFeatureMethod != null && checkAvailabilityMethod != null)
        {
            addFeatureMethod.setAccessible(true);
            checkAvailabilityMethod.setAccessible(true);

            Saurus3D.LOGGER.info("");
            Saurus3D.LOGGER.info("Start checking Saurus3D GL feature availabilities.");
            for (Class<? extends IGLFeature> featureClass: Saurus3DGLFeatureConfig.FEATURE_CLASSES)
            {
                try
                {
                    String featureName = featureClass.getAnnotation(Saurus3DGLFeature.class).value();
                    addFeatureMethod.invoke(null, new Object[]{featureName, featureClass});
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
            Saurus3D.LOGGER.info("- GL feature " + entry.getKey() + " is " + (entry.getValue() ? "" : "not ") + "available.");
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
    }
}
