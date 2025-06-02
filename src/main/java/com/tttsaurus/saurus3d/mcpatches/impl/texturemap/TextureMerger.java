package com.tttsaurus.saurus3d.mcpatches.impl.texturemap;

import com.tttsaurus.saurus3d.mcpatches.api.texturemap.TexRect;
import java.util.List;

public final class TextureMerger
{
    public static int[] mergeTexs(TexRect bigRect, List<TexRect> rects, List<int[]> datas)
    {
        int[] merged = new int[bigRect.width * bigRect.height];

        for (int i = 0; i < rects.size(); i++)
        {
            TexRect rect = rects.get(i);
            int[] data = datas.get(i);

            for (int y = 0; y < rect.height; y++)
            {
                for (int x = 0; x < rect.width; x++)
                {
                    int srcIndex = y * rect.width + x;
                    int dstX = rect.x + x - bigRect.x;
                    int dstY = rect.y + y - bigRect.y;
                    int dstIndex = dstY * bigRect.width + dstX;
                    merged[dstIndex] = data[srcIndex];
                }
            }
        }

        return merged;
    }
}
