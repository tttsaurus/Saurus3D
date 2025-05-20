package com.tttsaurus.saurus3d.common.core.mesh.buffer;

public class BufferID
{
    protected final int id;
    protected final BufferType type;

    public int getId() { return id; }
    public BufferType getType() { return type; }

    protected BufferID(int id, BufferType type)
    {
        this.id = id;
        this.type = type;
    }
}
