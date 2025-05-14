package com.tttsaurus.saurus3d.common.core.gl.debug.khr;

import com.tttsaurus.saurus3d.Saurus3D;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL43;
import org.lwjgl.opengl.KHRDebugCallback;
import javax.annotation.Nullable;

public final class KHRDebugManager
{
    private static boolean enable = false;
    public static boolean isEnable() { return enable; }

    // call by reflection
    private static void enable(@Nullable KHRMessageFilter messageFilter)
    {
        GL11.glEnable(GL43.GL_DEBUG_OUTPUT);
        GL11.glEnable(GL43.GL_DEBUG_OUTPUT_SYNCHRONOUS);

        GL43.glDebugMessageCallback(new KHRDebugCallback(KHRDebugManager::log));

        if (messageFilter == null || messageFilter.getIdentifier().equals(KHRMessageFilter.IDENTIFIR_DEFAULT))
            GL43.glDebugMessageControl(GL11.GL_DONT_CARE, GL11.GL_DONT_CARE, GL11.GL_DONT_CARE, null, true);
        else
            GL43.glDebugMessageControl(messageFilter.getSource().glValue, messageFilter.getType().glValue, messageFilter.getSeverity().glValue, null, true);

        enable = true;
    }

    public static void disable()
    {
        GL11.glDisable(GL43.GL_DEBUG_OUTPUT);
        GL11.glDisable(GL43.GL_DEBUG_OUTPUT_SYNCHRONOUS);

        GL43.glDebugMessageCallback(null);

        GL43.glDebugMessageControl(GL11.GL_DONT_CARE, GL11.GL_DONT_CARE, GL11.GL_DONT_CARE, null, false);

        enable = false;
    }

    private static void log(int source, int type, int id, int severity, String message)
    {
        StringBuilder builder = new StringBuilder();

        KHRMsgSource source1 = KHRMsgSource.parse(source);
        KHRMsgType type1 = KHRMsgType.parse(type);
        KHRMsgSeverity severity1 = KHRMsgSeverity.parse(severity);

        builder
                .append("OpenGL Debug: ")
                .append(String.format("[Source: %s] ", source1.toString()))
                .append(String.format("[Type: %s] ", type1.toString()))
                .append(String.format("[Severity: %s] ", severity1.toString()))
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

        EntityPlayerSP player = Minecraft.getMinecraft().player;
        if (player != null)
            player.sendMessage(new TextComponentString(TextFormatting.BLUE + "[Saurus3D GL Debug] " + TextFormatting.RESET + builder));
    }
}
