package com.tttsaurus.saurus3d.common.core.gl;

import com.tttsaurus.saurus3d.Saurus3D;
import org.lwjgl.opengl.GL11;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public final class GlResourceManager
{
    private static final List<IGlDisposable> disposables = new CopyOnWriteArrayList<>();

    public static void addDisposable(IGlDisposable disposable)
    {
        disposables.add(disposable);
    }
    public static void removeDisposable(IGlDisposable disposable)
    {
        disposables.remove(disposable);
    }

    public static void disposeAll()
    {
        for (IGlDisposable disposable: disposables)
        {
            Saurus3D.LOGGER.info("Disposing " + disposable.getClass().getSimpleName());
            disposable.dispose();
            checkGLError();
        }
    }

    private static void checkGLError()
    {
        int error;
        while ((error = GL11.glGetError()) != GL11.GL_NO_ERROR)
            Saurus3D.LOGGER.info("[OpenGL Error] " + getErrorString(error));
    }

    private static String getErrorString(int error)
    {
        return switch (error)
        {
            case GL11.GL_INVALID_ENUM -> "GL_INVALID_ENUM";
            case GL11.GL_INVALID_VALUE -> "GL_INVALID_VALUE";
            case GL11.GL_INVALID_OPERATION -> "GL_INVALID_OPERATION";
            case GL11.GL_STACK_OVERFLOW -> "GL_STACK_OVERFLOW";
            case GL11.GL_STACK_UNDERFLOW -> "GL_STACK_UNDERFLOW";
            case GL11.GL_OUT_OF_MEMORY -> "GL_OUT_OF_MEMORY";
            default -> "Unknown Error (code: " + error + ")";
        };
    }
}
