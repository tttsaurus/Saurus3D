package com.tttsaurus.saurus3d.common.core.gl.feature;

import com.google.common.collect.ImmutableMap;
import com.tttsaurus.saurus3d.Saurus3D;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

public final class GLFeatureManager
{
    // key: feature name
    private static final Map<String, Boolean> availability = new HashMap<>();

    public static ImmutableMap<String, Boolean> getAvailability() { return ImmutableMap.copyOf(availability); }

    // call by reflection
    @SuppressWarnings("all")
    private static void checkAvailability(String featureName, Class<? extends IGLFeature> featureClass)
    {
        try
        {
            Class<?> featureHelperClass = GLFeatureASMHelper.generateFeatureHelperClass(featureClass);
            if (featureHelperClass == null)
            {
                availability.put(featureName, false);
                Saurus3D.LOGGER.error("Failed to convert " + featureClass.getSimpleName() + ".isSupported() to static and call it to check " + featureName +  " availability.");
                return;
            }
            Saurus3D.LOGGER.info(featureClass.getSimpleName() + ".isSupported() is converted to " + featureHelperClass.getSimpleName() + ".isSupported() to check " + featureName + " availability.");
            Saurus3D.LOGGER.info("Now invoking " + featureHelperClass.getSimpleName() + ".isSupported() (may throw InvocationTargetException)");

            Method method = featureHelperClass.getDeclaredMethod("isSupported");
            boolean res = (boolean)method.invoke(null, new Object[]{});
            availability.put(featureName, res);
        }
        catch (Exception ignored) { }
    }

    public static boolean isAvailable(String featureName)
    {
        Boolean res = availability.get(featureName);
        if (res == null)
            return false;
        else
            return res;
    }

    public static void hardRequire(String featureName)
    {
        if (!isAvailable(featureName))
            throw new GLFeatureNotFoundException("Saurus3D GL feature " + featureName + " does not exist or is not available.");
    }
    public static void hardRequire(String... featureNames)
    {
        for (String featureName: featureNames)
            hardRequire(featureName);
    }

    public static GLFeatureScope require(String featureName)
    {
        return new GLFeatureScope(featureName);
    }
    public static GLFeatureScope require(String... featureNames)
    {
        return new GLFeatureScope(featureNames);
    }
}
