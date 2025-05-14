package com.tttsaurus.saurus3d.common.core.gl;

import com.tttsaurus.saurus3d.Saurus3D;
import org.lwjgl.opengl.GL11;
import java.util.PriorityQueue;

public final class GLResourceManager
{
    private static final PriorityQueue<GLDisposable> disposables = new PriorityQueue<>();

    public static void addDisposable(GLDisposable disposable)
    {
        disposables.add(disposable);
    }
    public static void removeDisposable(GLDisposable disposable)
    {
        disposables.remove(disposable);
    }

    public static void disposeAll()
    {
        while (!disposables.isEmpty())
        {
            GLDisposable disposable = disposables.poll();
            Saurus3D.LOGGER.info("Disposing " + disposable.getClass().getSimpleName());
            disposable.dispose();
            checkGlError();
        }
    }

    private static void checkGlError()
    {
        int error;
        while ((error = GL11.glGetError()) != GL11.GL_NO_ERROR)
            Saurus3D.LOGGER.warn("OpenGL Error: " + getErrorString(error));
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
