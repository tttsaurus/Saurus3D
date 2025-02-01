package com.tttsaurus.saurus3d;

import com.tttsaurus.saurus3d.proxy.CommonProxy;
import net.minecraft.block.Block;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(
        modid = Tags.MODID,
        version = Tags.VERSION,
        name = Tags.MODNAME,
        acceptedMinecraftVersions = "[1.12.2]")
public class Saurus3D
{
    public static final Logger LOGGER = LogManager.getLogger(Tags.MODID);

    @SidedProxy(
            clientSide = "com.tttsaurus.saurus3d.proxy.ClientProxy",
            serverSide = "com.tttsaurus.saurus3d.proxy.ServerProxy")
    private static CommonProxy proxy;

    @EventHandler
    public void preInit(FMLPreInitializationEvent event)
    {
        proxy.preInit(event);
    }

    @EventHandler
    public void init(FMLInitializationEvent event)
    {
        proxy.init(event);
        Saurus3D.LOGGER.info("Saurus3D initialized.");
    }

    @SubscribeEvent
    public void registerBlocks(RegistryEvent.Register<Block> event)
    {

    }
}
