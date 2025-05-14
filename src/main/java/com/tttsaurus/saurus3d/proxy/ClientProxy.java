package com.tttsaurus.saurus3d.proxy;

import com.tttsaurus.saurus3d.Saurus3D;
import com.tttsaurus.saurus3d.common.core.gl.GLResourceManager;
import com.tttsaurus.saurus3d.common.core.shutdown.ShutdownHooks;
import com.tttsaurus.saurus3d.test.Test;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

public class ClientProxy extends CommonProxy
{
    @Override
    public void preInit(FMLPreInitializationEvent event)
    {
        super.preInit(event);
    }

    @Override
    public void init(FMLInitializationEvent event)
    {
        super.init(event);

        ShutdownHooks.hooks.add(() ->
        {
            Saurus3D.LOGGER.info("Starts disposing OpenGL resources");
            GLResourceManager.disposeAll();
            Saurus3D.LOGGER.info("OpenGL resources disposed");
        });

        MinecraftForge.EVENT_BUS.register(Test.class);
    }
}
