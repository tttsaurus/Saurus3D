package com.tttsaurus.saurus3d.common.core.buffer;

public class BufferID
{
    protected final int id;
    protected final BufferType type;

    public int getID() { return id; }
    public BufferType getType() { return type; }

    public BufferID(int id, BufferType type)
    {
        this.id = id;
        this.type = type;
    }
}
