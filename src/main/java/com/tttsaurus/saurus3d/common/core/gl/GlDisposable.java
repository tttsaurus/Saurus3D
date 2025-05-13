package com.tttsaurus.saurus3d.common.core.gl;

public abstract class GlDisposable implements Comparable<GlDisposable>
{
    // bigger first
    public int priority() { return 0; }

    public abstract void dispose();

    @Override
    public int compareTo(GlDisposable other)
    {
        return -Integer.compare(this.priority(), other.priority());
    }
}
