package com.tttsaurus.saurus3d.proxy;

import com.tttsaurus.saurus3d.Saurus3D;
import com.tttsaurus.saurus3d.common.core.gl.feature.IGLFeature;
import com.tttsaurus.saurus3d.common.core.gl.feature.Saurus3DGLFeature;
import com.tttsaurus.saurus3d.config.Saurus3DGLFeatureConfig;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

public class CommonProxy
{
    public void preInit(FMLPreInitializationEvent event)
    {
        //<editor-fold desc="GL features update">
        Saurus3D.asmDataTable.getAll(Saurus3DGLFeature.class.getCanonicalName()).forEach(data ->
        {
            String className = data.getClassName();
            try
            {
                Class<?> clazz = Class.forName(className);
                if (IGLFeature.class.isAssignableFrom(clazz))
                {
                    Class<? extends IGLFeature> featureClass = clazz.asSubclass(IGLFeature.class);
                    if (!Saurus3DGLFeatureConfig.containsClass(featureClass))
                        Saurus3DGLFeatureConfig.FEATURE_CLASSES.add(featureClass);
                }
            }
            catch (ClassNotFoundException e)
            {
                Saurus3D.LOGGER.throwing(e);
            }
        });
        Saurus3DGLFeatureConfig.updateToLocal();
        //</editor-fold>
    }

    public void init(FMLInitializationEvent event)
    {

    }
}
