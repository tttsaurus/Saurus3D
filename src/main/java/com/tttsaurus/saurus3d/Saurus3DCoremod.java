package com.tttsaurus.saurus3d;

import net.minecraftforge.common.ForgeVersion;
import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin;
import org.jetbrains.annotations.Nullable;
import zone.rong.mixinbooter.IEarlyMixinLoader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@IFMLLoadingPlugin.Name(Tags.MODNAME + " Core")
@IFMLLoadingPlugin.MCVersion(ForgeVersion.mcVersion)
public class Saurus3DCoremod implements IFMLLoadingPlugin, IEarlyMixinLoader
{
    @Override
    public String[] getASMTransformerClass()
    {
        return new String[0];
    }

    @Override
    public String getModContainerClass()
    {
        return null;
    }

    @Override
    public @Nullable String getSetupClass()
    {
        return null;
    }

    @Override
    public void injectData(Map<String, Object> data)
    {

    }

    @Override
    public String getAccessTransformerClass()
    {
        return null;
    }

    @Override
    public List<String> getMixinConfigs()
    {
        List<String> list = new ArrayList<>();

        list.add("mixins.saurus3d.early.json");

        return list;
    }
}
