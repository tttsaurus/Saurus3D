package com.tttsaurus.saurus3d.config;

import com.tttsaurus.saurus3d.common.core.gl.debug.KHRDebugManager;
import com.tttsaurus.saurus3d.common.core.gl.feature.IGLFeature;
import com.tttsaurus.saurus3d.common.core.gl.feature.Saurus3DGLFeature;
import com.tttsaurus.saurus3d.common.core.mesh.Mesh;
import java.io.File;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public final class Saurus3DGLFeatureConfig
{
    public static final List<Class<? extends IGLFeature>> FEATURE_CLASSES = new CopyOnWriteArrayList<>();

    public static File CONFIG;

    public static boolean containsClass(Class<? extends IGLFeature> clazz)
    {
        for (Class<? extends IGLFeature> featureClass: FEATURE_CLASSES)
            if (featureClass.getName().equals(clazz.getName()))
                return true;
        return false;
    }

    public static void loadConfig()
    {
        FEATURE_CLASSES.clear();

        // default feature classes
        FEATURE_CLASSES.add(KHRDebugManager.class);
        FEATURE_CLASSES.add(Mesh.class);

        // read feature classes
        List<String> rawClasses = new ArrayList<>();
        try
        {
            RandomAccessFile raf = new RandomAccessFile(CONFIG, "rw");

            String line = raf.readLine();
            while (line != null)
            {
                rawClasses.add(line);
                line = raf.readLine();
            }

            raf.close();
        }
        catch (Exception ignored) { }

        // parse feature classes
        for (String rawClass: rawClasses)
        {
            try
            {
                Class<?> clazz = Class.forName(rawClass);
                if (IGLFeature.class.isAssignableFrom(clazz))
                {
                    Class<? extends IGLFeature> featureClass = clazz.asSubclass(IGLFeature.class);
                    if (!containsClass(featureClass))
                        FEATURE_CLASSES.add(featureClass);
                }
            }
            catch (ClassNotFoundException ignored) { }
        }

        // annotation check
        for (Class<? extends IGLFeature> featureClass: FEATURE_CLASSES)
        {
            Saurus3DGLFeature annotation = featureClass.getAnnotation(Saurus3DGLFeature.class);
            if (annotation == null)
                FEATURE_CLASSES.remove(featureClass);
        }
    }

    public static void updateToLocal()
    {
        try
        {
            RandomAccessFile raf = new RandomAccessFile(CONFIG, "rw");

            raf.setLength(0);
            raf.seek(0);
            for (int i = 0; i < FEATURE_CLASSES.size(); i++)
            {
                StringBuilder builder = new StringBuilder();
                builder.append(FEATURE_CLASSES.get(i).getName());
                if (i != FEATURE_CLASSES.size() - 1)
                    builder.append("\n");
                raf.writeBytes(builder.toString());
            }

            raf.close();
        }
        catch (Exception ignored) { }
    }
}
