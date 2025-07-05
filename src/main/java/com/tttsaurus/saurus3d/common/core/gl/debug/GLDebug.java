package com.tttsaurus.saurus3d.common.core.gl.debug;

import com.tttsaurus.saurus3d.Saurus3D;
import com.tttsaurus.saurus3d.config.Saurus3DGLDebugConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import org.lwjgl.opengl.GL11;

public final class GLDebug
{
    public static void checkError()
    {
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        StackTraceElement caller = null;
        if (stackTrace.length > 2) caller = stackTrace[2];

        int error;
        while ((error = GL11.glGetError()) != GL11.GL_NO_ERROR)
        {
            StringBuilder builder = new StringBuilder();
            builder.append("OpenGL Error: ").append(getErrorString(error)).append(".\n").append("\tThis debug is executed manually at: ");

            if (caller == null)
                builder.append("Can't find");
            else
                builder.append(String.format("%s (%s:%d)", caller.getMethodName(), caller.getFileName(), caller.getLineNumber()));

            Saurus3D.LOGGER.warn(builder.toString());

            if (Saurus3DGLDebugConfig.ENABLE_INGAME_DEBUG)
            {
                EntityPlayerSP player = Minecraft.getMinecraft().player;
                if (player != null)
                    player.sendMessage(new TextComponentString(TextFormatting.BLUE + "[Saurus3D GL Debug] " + TextFormatting.RESET + builder));
            }
        }
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
