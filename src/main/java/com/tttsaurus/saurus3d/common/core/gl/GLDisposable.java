package com.tttsaurus.saurus3d.common.core.gl;

public abstract class GLDisposable implements Comparable<GLDisposable>
{
    // bigger first
    public int priority() { return 0; }

    public abstract void dispose();

    @Override
    public int compareTo(GLDisposable other)
    {
        return -Integer.compare(this.priority(), other.priority());
    }
}
