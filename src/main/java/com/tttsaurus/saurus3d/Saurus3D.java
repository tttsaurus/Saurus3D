package com.tttsaurus.saurus3d;

import com.tttsaurus.saurus3d.proxy.CommonProxy;
import net.minecraft.block.Block;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.discovery.ASMDataTable;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(
        modid = Tags.MODID,
        version = Tags.VERSION,
        name = Tags.MODNAME,
        acceptedMinecraftVersions = "[1.12.2]",
        dependencies = "required-after:mixinbooter@[10.0,)")
public class Saurus3D
{
    public static final Logger LOGGER = LogManager.getLogger(Tags.MODNAME);

    @SidedProxy(
            clientSide = "com.tttsaurus.saurus3d.proxy.ClientProxy",
            serverSide = "com.tttsaurus.saurus3d.proxy.ServerProxy")
    private static CommonProxy proxy;

    public static ASMDataTable asmDataTable;

    private static Boolean isCleanroom = null;

    public static boolean isCleanroom()
    {
        if (isCleanroom == null)
        {
            try
            {
                Class.forName("com.cleanroommc.boot.Main");
                isCleanroom = true;
                return true;
            }
            catch (ClassNotFoundException e)
            {
                isCleanroom = false;
                return false;
            }
        }
        else
            return isCleanroom;
    }

    @EventHandler
    public void preInit(FMLPreInitializationEvent event)
    {
        asmDataTable = event.getAsmData();
        MinecraftForge.EVENT_BUS.register(this);
        proxy.preInit(event);
    }

    @EventHandler
    public void init(FMLInitializationEvent event)
    {
        proxy.init(event);
    }

    @SubscribeEvent
    public void registerBlocks(RegistryEvent.Register<Block> event)
    {

    }
}
