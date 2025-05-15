package com.tttsaurus.saurus3d.common.core.gl.feature;

import com.tttsaurus.saurus3d.common.core.gl.version.GLVersionHelper;
import org.lwjgl.opengl.ContextCapabilities;
import org.lwjgl.opengl.GLContext;

public final class GLFeatures
{
    @Saurus3DGLFeature("KHR_DEBUG")
    public static class KHRDebug implements IGLFeature
    {
        @Override
        public boolean isSupported()
        {
            boolean supported = false;

            ContextCapabilities capabilities = GLContext.getCapabilities();
            if (capabilities.GL_KHR_debug) supported = true;
            if (GLVersionHelper.supported(4, 3)) supported = true;

            return supported;
        }
    }

    @Saurus3DGLFeature("SHADER")
    public static class Shader implements IGLFeature
    {
        @Override
        public boolean isSupported()
        {
            return GLVersionHelper.supported(2, 0);
        }
    }

    @Saurus3DGLFeature("MESH")
    public static class Mesh implements IGLFeature
    {
        @Override
        public boolean isSupported()
        {
            return GLVersionHelper.supported(3, 0);
        }
    }
}
