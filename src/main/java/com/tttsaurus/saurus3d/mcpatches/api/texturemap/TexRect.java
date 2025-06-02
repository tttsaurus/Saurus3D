package com.tttsaurus.saurus3d.mcpatches.api.texturemap;

public class TexRect
{
    public int x;
    public int y;
    public int width;
    public int height;

    public TexRect(int x, int y, int width, int height)
    {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public boolean contains(TexRect r)
    {
        return r.x >= x && r.x + r.width <= x + width && r.y >= y && r.y + r.height <= y + height;
    }
}
