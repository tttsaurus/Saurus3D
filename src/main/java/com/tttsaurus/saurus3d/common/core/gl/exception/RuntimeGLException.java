package com.tttsaurus.saurus3d.common.core.gl.exception;

import com.tttsaurus.saurus3d.Saurus3D;

public class RuntimeGLException extends RuntimeException
{
    public RuntimeGLException(String message)
    {
        super(message);

        StringBuilder builder = new StringBuilder();

        builder.append("Runtime GL Related Exception - ").append(this.getClass().getSimpleName()).append(": ");
        builder.append(message).append(" Stack Trace:\n");

        for (StackTraceElement stackTraceElement : this.getStackTrace())
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

        Saurus3D.LOGGER.error(builder.toString());
    }
}
