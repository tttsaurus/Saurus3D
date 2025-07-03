package com.tttsaurus.saurus3d.proxy;

import com.tttsaurus.saurus3d.Saurus3D;
import com.tttsaurus.saurus3d.common.core.function.Action;
import com.tttsaurus.saurus3d.common.core.gl.resource.GLResourceManager;
import com.tttsaurus.saurus3d.common.core.shutdown.ShutdownHooks;
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

        //<editor-fold desc="shutdown hooks">
        ShutdownHooks.hooks.add(new Action()
        {
            @Override
            public void invoke()
            {
                Saurus3D.LOGGER.info("Start disposing OpenGL resources");
                GLResourceManager.disposeAll();
                Saurus3D.LOGGER.info("OpenGL resources disposed");
            }
        });
        //</editor-fold>

        //MinecraftForge.EVENT_BUS.register(Test.class);
    }
}
