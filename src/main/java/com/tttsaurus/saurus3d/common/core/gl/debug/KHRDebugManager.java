package com.tttsaurus.saurus3d.common.core.gl.debug;

import com.tttsaurus.saurus3d.Saurus3D;
import com.tttsaurus.saurus3d.config.Saurus3DGLDebugConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import org.lwjgl.opengl.*;
import java.nio.IntBuffer;
import java.util.List;

public final class KHRDebugManager
{
    private static boolean enable = false;
    public static boolean isEnable() { return enable; }

    // call by reflection
    private static void enable(List<DebugMessageFilter> messageFilters)
    {
        if (enable) return;

        GL11.glEnable(GL43.GL_DEBUG_OUTPUT);
        GL11.glEnable(GL43.GL_DEBUG_OUTPUT_SYNCHRONOUS);

        GLDebugMessageCallback callback = GLDebugMessageCallback.create((source, type, id, severity, length, message, userParam) ->
        {
            String msg = GLDebugMessageCallback.getMessage(length, message);
            log(source, type, id, severity, msg);
        });

        GL43.glDebugMessageCallback(callback, 0);

        // disable all
        GL43.glDebugMessageControl(GL11.GL_DONT_CARE, GL11.GL_DONT_CARE, GL11.GL_DONT_CARE, (IntBuffer)null, false);

        for (DebugMessageFilter filter: messageFilters)
            GL43.glDebugMessageControl(filter.getSource().glValue, filter.getType().glValue, filter.getSeverity().glValue, (IntBuffer)null, true);

        enable = true;
    }

    public static void disable()
    {
        if (!enable) return;

        GL11.glDisable(GL43.GL_DEBUG_OUTPUT);
        GL11.glDisable(GL43.GL_DEBUG_OUTPUT_SYNCHRONOUS);

        GL43.glDebugMessageCallback(null, 0);

        GL43.glDebugMessageControl(GL11.GL_DONT_CARE, GL11.GL_DONT_CARE, GL11.GL_DONT_CARE, (IntBuffer)null, false);

        enable = false;
    }

    private static void log(int source, int type, int id, int severity, String message)
    {
        StringBuilder builder = new StringBuilder();

        DebugMsgSource source1 = DebugMsgSource.parse(source);
        DebugMsgType type1 = DebugMsgType.parse(type);
        DebugMsgSeverity severity1 = DebugMsgSeverity.parse(severity);

        builder
                .append("OpenGL Debug: ")
                .append(String.format("(Source=%s, ", source1.toString()))
                .append(String.format("Type=%s, ", type1.toString()))
                .append(String.format("Severity=%s, ", severity1.toString()))
                .append(String.format("ID=%d) ", id))
                .append(message).append(" Stack Trace:\n");

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

        Saurus3D.LOGGER.info(builder.toString());

        if (Saurus3DGLDebugConfig.ENABLE_INGAME_DEBUG)
        {
            EntityPlayerSP player = Minecraft.getMinecraft().player;
            if (player != null)
                player.sendMessage(new TextComponentString(TextFormatting.BLUE + "[Saurus3D GL Debug] " + TextFormatting.RESET + builder));
        }
    }
}
