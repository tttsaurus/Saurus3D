package com.tttsaurus.saurus3d.mcpatches.impl.texturemap;

public class TexUpdatePlan
{
    public TexRect rect;
    public int[][] data;

    public TexUpdatePlan(TexRect rect, int[][] data)
    {
        this.rect = rect;
        this.data = data;
    }
}
