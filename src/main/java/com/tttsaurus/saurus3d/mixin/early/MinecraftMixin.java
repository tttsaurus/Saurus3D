package com.tttsaurus.saurus3d.mixin.early;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.tttsaurus.saurus3d.Saurus3D;
import com.tttsaurus.saurus3d.common.core.function.IAction;
import com.tttsaurus.saurus3d.common.core.gl.debug.khr.KHRDebugManager;
import com.tttsaurus.saurus3d.common.core.gl.debug.khr.DebugMessageFilter;
import com.tttsaurus.saurus3d.common.core.shutdown.ShutdownHooks;
import com.tttsaurus.saurus3d.config.ConfigFileHelper;
import com.tttsaurus.saurus3d.config.Saurus3DDebugConfig;
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

@Mixin(Minecraft.class)
public class MinecraftMixin
{
    @Inject(method = "shutdown", at = @At("HEAD"))
    private void beforeShutdown(CallbackInfo ci)
    {
        for (IAction action: ShutdownHooks.hooks)
            action.invoke();
    }

    @Inject(method = "init", at = @At("HEAD"))
    private void beforeInit(CallbackInfo info)
    {
        Saurus3DDebugConfig.CONFIG = new Configuration(ConfigFileHelper.makeFile("debug"));
        Saurus3DDebugConfig.loadConfig();
    }

    @WrapOperation(method = "createDisplay", at = @At(value = "INVOKE", target = "Lorg/lwjgl/opengl/Display;create(Lorg/lwjgl/opengl/PixelFormat;)V", remap = false))
    private void createDisplayInTry(PixelFormat pixelFormat, Operation<Void> original) throws LWJGLException
    {
        if (Saurus3DDebugConfig.ENABLE_AUTO_DEBUG)
            Display.create(pixelFormat, new ContextAttribs(1, 0, 0, ContextAttribs.CONTEXT_DEBUG_BIT_ARB));
        else
            original.call(pixelFormat);
    }

    @WrapOperation(method = "createDisplay", at = @At(value = "INVOKE", target = "Lorg/lwjgl/opengl/Display;create()V", remap = false))
    private void createDisplayInCatch(Operation<Void> original) throws LWJGLException
    {
        if (Saurus3DDebugConfig.ENABLE_AUTO_DEBUG)
            Display.create(new PixelFormat(), new ContextAttribs(1, 0, 0, ContextAttribs.CONTEXT_DEBUG_BIT_ARB));
        else
            original.call();
    }

    @Inject(method = "init", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Minecraft;createDisplay()V", shift = At.Shift.AFTER))
    private void afterCreateDisplay(CallbackInfo info)
    {
        if (!KHRDebugManager.isSupported()) Saurus3DDebugConfig.ENABLE_AUTO_DEBUG = false;
        if (Saurus3DDebugConfig.ENABLE_AUTO_DEBUG)
        {
            try
            {
                Method method = KHRDebugManager.class.getDeclaredMethod("enable", DebugMessageFilter.class);
                method.setAccessible(true);
                method.invoke(null, new Object[]{Saurus3DDebugConfig.AUTO_DEBUG_MSG_FILTER});
            }
            catch (Exception ignored) { }
        }

        if (KHRDebugManager.isEnable())
            Saurus3D.LOGGER.info("KHR Auto Debug is enabled.");
        else
            Saurus3D.LOGGER.info("KHR Auto Debug is disabled.");
    }
}
