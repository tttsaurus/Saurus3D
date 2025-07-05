package com.tttsaurus.saurus3d.common.core.gl.resource;

public abstract class GLDisposable implements Comparable<GLDisposable>
{
    public final String getName()
    {
        return getResourceType() + "@" + this.hashCode();
    }

    public String getResourceType()
    {
        return this.getClass().getSimpleName();
    }

    // bigger first
    public int disposePriority() { return 0; }

    public abstract void dispose();

    @Override
    public int compareTo(GLDisposable other)
    {
        return -Integer.compare(this.disposePriority(), other.disposePriority());
    }
}
