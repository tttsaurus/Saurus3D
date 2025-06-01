package com.tttsaurus.saurus3d.common.core.mcpatches;

public class Rect
{
    public int x;
    public int y;
    public int width;
    public int height;

    public Rect(int x, int y, int width, int height)
    {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public boolean contains(Rect r)
    {
        return r.x >= x && r.x + r.width <= x + width && r.y >= y && r.y + r.height <= y + height;
    }
}
