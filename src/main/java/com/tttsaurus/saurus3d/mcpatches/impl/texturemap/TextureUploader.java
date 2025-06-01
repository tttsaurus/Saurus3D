package com.tttsaurus.saurus3d.mcpatches.impl.texturemap;

import com.tttsaurus.saurus3d.Saurus3D;
import java.util.*;

public final class TextureUploader
{
    private final static Map<Integer, List<int[]>> mipmapData = new HashMap<>();
    private final static Map<Integer, List<TexRect>> mipmapRect = new HashMap<>();

    public static void reset()
    {
        mipmapData.clear();
        mipmapRect.clear();
    }

    public static void planTexUpload(int level, int[] data, TexRect rect)
    {
        if (!mipmapData.containsKey(level))
            mipmapData.put(level, new ArrayList<>());
        if (!mipmapRect.containsKey(level))
            mipmapRect.put(level, new ArrayList<>());

        mipmapData.get(level).add(data);
        mipmapRect.get(level).add(new TexRect(rect.x >> level, rect.y >> level, rect.width >> level, rect.height >> level));
    }

    public static void batchUpload(TexRect rect)
    {
        for (Map.Entry<Integer, List<int[]>> entry: mipmapData.entrySet())
        {
            int level = entry.getKey();
            List<int[]> datas = entry.getValue();
            List<TexRect> rects = mipmapRect.get(level);

            // todo: delete test
            assert RectMergeAlgorithm.isPerfectRect(rects);


        }
    }
}
