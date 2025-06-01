package com.tttsaurus.saurus3d.mcpatches.impl.texturemap;

import net.minecraft.client.renderer.GLAllocation;
import net.minecraft.client.renderer.GlStateManager;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
import java.nio.IntBuffer;
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

    private static final IntBuffer DATA_BUFFER = GLAllocation.createDirectIntBuffer(4194304);

    public static void batchUpload(TexRect rect)
    {
        boolean mipmap = mipmapData.size() > 1;
        GlStateManager.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, mipmap ? GL11.GL_NEAREST_MIPMAP_LINEAR : GL11.GL_NEAREST);
        GlStateManager.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);
        GlStateManager.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL11.GL_REPEAT);
        GlStateManager.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL11.GL_REPEAT);

        for (Map.Entry<Integer, List<int[]>> entry: mipmapData.entrySet())
        {
            int level = entry.getKey();
            TexRect bigRect = new TexRect(rect.x >> level, rect.y >> level, rect.width >> level, rect.height >> level);
            List<TexRect> rects = mipmapRect.get(level);
            List<int[]> datas = entry.getValue();

            int[] mergedData = mergeTexs(bigRect, rects, datas);

            DATA_BUFFER.clear();
            DATA_BUFFER.put(mergedData);
            DATA_BUFFER.flip();

            GlStateManager.glTexSubImage2D(GL11.GL_TEXTURE_2D, level, bigRect.x, bigRect.y, bigRect.width, bigRect.height, GL12.GL_BGRA, GL12.GL_UNSIGNED_INT_8_8_8_8_REV, DATA_BUFFER);
        }
    }

    private static int[] mergeTexs(TexRect bigRect, List<TexRect> rects, List<int[]> datas)
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
