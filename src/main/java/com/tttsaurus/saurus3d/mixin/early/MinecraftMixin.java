package com.tttsaurus.saurus3d.mixin.early;

import com.tttsaurus.saurus3d.common.core.function.IAction;
import com.tttsaurus.saurus3d.common.core.shutdown.ShutdownHooks;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public class MinecraftMixin
{
    @Inject(method = "shutdown", at = @At("HEAD"))
    public void shutdown(CallbackInfo ci)
    {
        for (IAction action: ShutdownHooks.hooks)
            action.invoke();
    }
}
