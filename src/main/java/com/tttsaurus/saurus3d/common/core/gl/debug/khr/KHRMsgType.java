package com.tttsaurus.saurus3d.common.core.gl.debug.khr;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL43;

public enum KHRMsgType
{
    ANY(GL11.GL_DONT_CARE),
    ERROR(GL43.GL_DEBUG_TYPE_ERROR),
    DEPRECATED_BEHAVIOR(GL43.GL_DEBUG_TYPE_DEPRECATED_BEHAVIOR),
    UNDEFINED_BEHAVIOR(GL43.GL_DEBUG_TYPE_UNDEFINED_BEHAVIOR),
    PORTABILITY(GL43.GL_DEBUG_TYPE_PORTABILITY),
    PERFORMANCE(GL43.GL_DEBUG_TYPE_PERFORMANCE),
    MARKER(GL43.GL_DEBUG_TYPE_MARKER),
    OTHER(GL43.GL_DEBUG_TYPE_OTHER);

    public final int glValue;
    KHRMsgType(int glValue)
    {
        this.glValue = glValue;
    }

    public static KHRMsgType parse(int value)
    {
        return switch (value)
        {
            case GL11.GL_DONT_CARE -> ANY;
            case GL43.GL_DEBUG_TYPE_ERROR -> ERROR;
            case GL43.GL_DEBUG_TYPE_DEPRECATED_BEHAVIOR -> DEPRECATED_BEHAVIOR;
            case GL43.GL_DEBUG_TYPE_UNDEFINED_BEHAVIOR -> UNDEFINED_BEHAVIOR;
            case GL43.GL_DEBUG_TYPE_PORTABILITY -> PORTABILITY;
            case GL43.GL_DEBUG_TYPE_PERFORMANCE -> PERFORMANCE;
            case GL43.GL_DEBUG_TYPE_MARKER -> MARKER;
            case GL43.GL_DEBUG_TYPE_OTHER -> OTHER;
            default -> ANY;
        };
    }
}
