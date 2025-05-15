package com.tttsaurus.saurus3d.common.core.gl.feature;

import com.google.common.collect.ImmutableMap;
import com.tttsaurus.saurus3d.Saurus3D;
import org.objectweb.asm.*;
import javax.annotation.Nullable;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class GLFeatureManager
{
    // key: module name
    private static final Map<String, Class<? extends IGLFeature>> features = new HashMap<>();
    private static final Map<String, Boolean> availability = new HashMap<>();

    public static ImmutableMap<String, Boolean> getAvailability() { return ImmutableMap.copyOf(availability); }
    public static List<String> getFeatures() { return new ArrayList<>(features.keySet()); }

    // make 'isSupported' method static
    @SuppressWarnings("all")
    @Nullable
    private static Class<?> generateFeatureHelperClass(Class<? extends IGLFeature> featureClass)
    {
        Class<?> featureHelperClass = null;

        String oldClassName = featureClass.getName();
        String newClassName = oldClassName.replace(featureClass.getSimpleName(), featureClass.getSimpleName() + "Helper");

        ClassLoader classLoader = featureClass.getClassLoader();

        try
        {
            ClassReader reader = new ClassReader(classLoader.getResourceAsStream(oldClassName.replace('.', '/') + ".class"));
            ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);

            ClassVisitor visitor = new ClassVisitor(Opcodes.ASM5, writer)
            {
                @Override
                public void visit(int version, int access, String name, String signature, String superName, String[] interfaces)
                {
                    super.visit(version, access, newClassName.replace('.', '/'), signature, superName, interfaces);
                }

                @Override
                public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions)
                {
                    if (name.equals("isSupported") && (access & Opcodes.ACC_STATIC) == 0)
                    {
                        access = (access | Opcodes.ACC_STATIC);
                        return super.visitMethod(access, name, descriptor, signature, exceptions);
                    }
                    return null;
                }
            };

            reader.accept(visitor, 0);

            byte[] bytes = writer.toByteArray();

            Method defineClassMethod = ClassLoader.class.getDeclaredMethod("defineClass", String.class, byte[].class, int.class, int.class);
            defineClassMethod.setAccessible(true);
            featureHelperClass = (Class<?>)defineClassMethod.invoke(classLoader, newClassName, bytes, 0, bytes.length);
        }
        catch (Throwable throwable)
        {
            Saurus3D.LOGGER.throwing(throwable);
        }

        return featureHelperClass;
    }

    // call by reflection
    private static void addFeature(String featureName, Class<? extends IGLFeature> featureClass)
    {
        if (!features.containsKey(featureName))
            features.put(featureName, featureClass);
    }

    // call by reflection
    private static void checkAvailability()
    {
        Saurus3D.LOGGER.info("Some GL feature helper classes will be generated at this stage.");
        //Saurus3D.LOGGER.info("If the game hard crashed, then that is probably related to the improper implementation of the method 'isSupported' from existing GL feature classes.");

        for (Map.Entry<String, Class<? extends IGLFeature>> entry: features.entrySet())
        {
            try
            {
                Class<? extends IGLFeature> featureClass = entry.getValue();

                Saurus3D.LOGGER.info("Generating a helper class for " + featureClass.getSimpleName() + " to check its availability.");
                Class<?> featureHelperClass = generateFeatureHelperClass(featureClass);
                if (featureHelperClass == null)
                {
                    availability.put(entry.getKey(), false);
                    Saurus3D.LOGGER.error("Failed to generate the helper class for " + featureClass.getSimpleName() + ".");
                    continue;
                }
                Saurus3D.LOGGER.info("Successfully generated " + featureHelperClass.getSimpleName() + ".");

                Method method = featureHelperClass.getDeclaredMethod("isSupported");
                boolean res = (boolean)method.invoke(null, new Object[]{});
                availability.put(entry.getKey(), res);
            }
            catch (Exception e)
            {
                availability.put(entry.getKey(), false);
                Saurus3D.LOGGER.throwing(e);
            }
        }
    }

    public static boolean isAvailable(String featureName)
    {
        Boolean res = availability.get(featureName);
        if (res == null)
            return false;
        else
            return res;
    }
}
