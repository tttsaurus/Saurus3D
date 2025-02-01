package com.tttsaurus.saurus3d.proxy;

import com.tttsaurus.saurus3d.Saurus3D;
import com.tttsaurus.saurus3d.common.impl.model.Test;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

public class CommonProxy
{
    public void preInit(FMLPreInitializationEvent event)
    {

    }

    public void init(FMLInitializationEvent event)
    {
        Saurus3D.LOGGER.info("Saurus3D starts initializing.");
        MinecraftForge.EVENT_BUS.register(Test.class);
    }
}
