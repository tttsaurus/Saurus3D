package com.tttsaurus.saurus3d.common.core.buffer;

import org.lwjgl.opengl.GL30;

public enum MapBufferAccessBit
{
    READ_BIT(GL30.GL_MAP_READ_BIT),
    WRITE_BIT(GL30.GL_MAP_WRITE_BIT),
    INVALIDATE_RANGE_BIT(GL30.GL_MAP_INVALIDATE_RANGE_BIT),
    INVALIDATE_BUFFER_BIT(GL30.GL_MAP_INVALIDATE_BUFFER_BIT),
    FLUSH_EXPLICIT_BIT(GL30.GL_MAP_FLUSH_EXPLICIT_BIT),
    UNSYNCHRONIZED_BIT(GL30.GL_MAP_UNSYNCHRONIZED_BIT);

    public final int glValue;
    MapBufferAccessBit(int glValue)
    {
        this.glValue = glValue;
    }
}
