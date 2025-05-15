package com.tttsaurus.saurus3d.common.core.gl.feature;

import com.tttsaurus.saurus3d.Saurus3D;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class GLFeatureScope
{
    private final List<String> featureNames = new ArrayList<>();

    protected GLFeatureScope(String featureName)
    {
        featureNames.add(featureName);
    }
    protected GLFeatureScope(String... featureNames)
    {
        this.featureNames.addAll(Arrays.asList(featureNames));
    }

    public void run(Runnable runnable)
    {
        boolean flag = true;
        StringBuilder builder = null;
        for (String featureName: featureNames)
        {
            if (!GLFeatureManager.isAvailable(featureName))
            {
                flag = false;
                if (builder == null)
                {
                    builder = new StringBuilder();
                    builder.append("A GLFeatureScope ran into a problem.\n");
                }
                builder.append("Saurus3D GL feature ").append(featureName).append(" does not exist or is not available.\n");
            }
        }

        if (flag)
            runnable.run();
        else
        {
            builder.append("This block of code will be skipped.\n");
            for (StackTraceElement stackTraceElement : new Exception().getStackTrace())
                builder
                        .append('\t').append("at")
                        .append(' ')
                        .append(stackTraceElement.getClassName())
                        .append('.')
                        .append(stackTraceElement.getMethodName())
                        .append('(')
                        .append(stackTraceElement.getFileName())
                        .append(':')
                        .append(stackTraceElement.getLineNumber())
                        .append(')')
                        .append('\n');
            Saurus3D.LOGGER.warn(builder.toString());
        }
    }
}
