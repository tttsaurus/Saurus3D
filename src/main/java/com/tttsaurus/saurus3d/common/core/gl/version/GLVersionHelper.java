package com.tttsaurus.saurus3d.common.core.gl.version;

import org.lwjgl.opengl.GL11;

public final class GLVersionHelper
{
    private static boolean glVersionParsed = false;
    private static int majorGLVersion = -1;
    private static int minorGLVersion = -1;
    private static String rawGLVersion = "";

    public static int getMajorGLVersion()
    {
        if (!glVersionParsed) parseGLVersion();
        return majorGLVersion;
    }
    public static int getMinorGLVersion()
    {
        if (!glVersionParsed) parseGLVersion();
        return minorGLVersion;
    }
    public static String getRawGLVersion()
    {
        if (!glVersionParsed) parseGLVersion();
        return rawGLVersion;
    }
    private static void parseGLVersion()
    {
        glVersionParsed = true;
        rawGLVersion = GL11.glGetString(GL11.GL_VERSION);

        if (rawGLVersion != null)
        {
            String[] parts = rawGLVersion.split("\\s+")[0].split("\\.");
            if (parts.length >= 2)
            {
                try
                {
                    majorGLVersion = Integer.parseInt(parts[0]);
                    minorGLVersion = Integer.parseInt(parts[1]);
                }
                catch (NumberFormatException ignored) { }
            }
        }
        else
            rawGLVersion = "";

        if (rawGLVersion.isEmpty() || majorGLVersion == -1 || minorGLVersion == -1)
            throw new RuntimeException("GLVersionHelper failed to parse GL version.");
    }

    public static boolean supported(int major, int minor)
    {
        if (major < getMajorGLVersion())
            return true;
        if (major <= getMajorGLVersion() && minor <= getMinorGLVersion())
            return true;
        return false;
    }
}
