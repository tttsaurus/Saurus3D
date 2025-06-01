package com.tttsaurus.saurus3d.mcpatches.impl.texturemap;

public class TexUpdatePlan
{
    public TexRect texRect;
    public int[] data;

    public TexUpdatePlan(TexRect texRect, int[] data)
    {
        this.texRect = texRect;
        this.data = data;
    }
}
