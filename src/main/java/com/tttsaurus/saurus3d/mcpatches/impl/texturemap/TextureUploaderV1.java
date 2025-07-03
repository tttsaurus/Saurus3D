package com.tttsaurus.saurus3d.mcpatches.impl.texturemap;

import com.tttsaurus.saurus3d.mcpatches.api.texturemap.ITextureUploader;
import com.tttsaurus.saurus3d.mcpatches.api.texturemap.TexRect;
import net.minecraft.client.renderer.GlStateManager;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public final class TextureUploaderV1 implements ITextureUploader
{
    private ByteBuffer byteBuffer;

    private final Map<Integer, List<int[]>> mipmapData = new TreeMap<>();
    private final Map<Integer, List<TexRect>> mipmapRect = new TreeMap<>();
    private List<int[]> mipmapDataCache0;
    private List<int[]> mipmapDataCache1;
    private List<int[]> mipmapDataCache2;
    private List<int[]> mipmapDataCache3;
    private List<int[]> mipmapDataCache4;
    private List<TexRect> mipmapRectCache0;
    private List<TexRect> mipmapRectCache1;
    private List<TexRect> mipmapRectCache2;
    private List<TexRect> mipmapRectCache3;
    private List<TexRect> mipmapRectCache4;

    private final Map<Integer, CompletableFuture<?>> mergingProcesses = new HashMap<>();
    private boolean skipFirstTick;

    public void init(int bufferSize)
    {
        byteBuffer = ByteBuffer.allocateDirect(bufferSize).order(ByteOrder.nativeOrder());
        skipFirstTick = true;
    }

    public void reset()
    {
        mipmapData.clear();
        mipmapRect.clear();
        mipmapDataCache0 = null;
        mipmapDataCache1 = null;
        mipmapDataCache2 = null;
        mipmapDataCache3 = null;
        mipmapDataCache4 = null;
        mipmapRectCache0 = null;
        mipmapRectCache1 = null;
        mipmapRectCache2 = null;
        mipmapRectCache3 = null;
        mipmapRectCache4 = null;
    }

    public void planTexUpload(int level, int[] data, TexRect rect)
    {
        boolean putMipmapData;
        if (mipmapDataCache0 != null && level == 0)
            putMipmapData = false;
        else if (mipmapDataCache1 != null && level == 1)
            putMipmapData = false;
        else if (mipmapDataCache2 != null && level == 2)
            putMipmapData = false;
        else if (mipmapDataCache3 != null && level == 3)
            putMipmapData = false;
        else if (mipmapDataCache4 != null && level == 4)
            putMipmapData = false;
        else
            putMipmapData = !mipmapData.containsKey(level);

        if (putMipmapData)
            mipmapData.put(level, new ArrayList<>());

        boolean putMipmapRect;
        if (mipmapRectCache0 != null && level == 0)
            putMipmapRect = false;
        else if (mipmapRectCache1 != null && level == 1)
            putMipmapRect = false;
        else if (mipmapRectCache2 != null && level == 2)
            putMipmapRect = false;
        else if (mipmapRectCache3 != null && level == 3)
            putMipmapRect = false;
        else if (mipmapRectCache4 != null && level == 4)
            putMipmapRect = false;
        else
            putMipmapRect = !mipmapRect.containsKey(level);

        if (putMipmapRect)
            mipmapRect.put(level, new ArrayList<>());

        List<int[]> d;
        List<TexRect> r;

        switch (level)
        {
            case 0 ->
            {
                if (mipmapDataCache0 == null)
                    mipmapDataCache0 = mipmapData.get(level);
                d = mipmapDataCache0;
                if (mipmapRectCache0 == null)
                    mipmapRectCache0 = mipmapRect.get(level);
                r = mipmapRectCache0;
            }
            case 1 ->
            {
                if (mipmapDataCache1 == null)
                    mipmapDataCache1 = mipmapData.get(level);
                d = mipmapDataCache1;
                if (mipmapRectCache1 == null)
                    mipmapRectCache1 = mipmapRect.get(level);
                r = mipmapRectCache1;
            }
            case 2 ->
            {
                if (mipmapDataCache2 == null)
                    mipmapDataCache2 = mipmapData.get(level);
                d = mipmapDataCache2;
                if (mipmapRectCache2 == null)
                    mipmapRectCache2 = mipmapRect.get(level);
                r = mipmapRectCache2;
            }
            case 3 ->
            {
                if (mipmapDataCache3 == null)
                    mipmapDataCache3 = mipmapData.get(level);
                d = mipmapDataCache3;
                if (mipmapRectCache3 == null)
                    mipmapRectCache3 = mipmapRect.get(level);
                r = mipmapRectCache3;
            }
            case 4 ->
            {
                if (mipmapDataCache4 == null)
                    mipmapDataCache4 = mipmapData.get(level);
                d = mipmapDataCache4;
                if (mipmapRectCache4 == null)
                    mipmapRectCache4 = mipmapRect.get(level);
                r = mipmapRectCache4;
            }
            default ->
            {
                d = mipmapData.get(level);
                r = mipmapRect.get(level);
            }
        }

        d.add(data);
        r.add(new TexRect(rect.x >> level, rect.y >> level, rect.width >> level, rect.height >> level));
    }

    private final Map<Integer, int[]> mergedDataContainer = new HashMap<>();

    public void batchUpload(TexRect rect, boolean setTexParam, Executor executor)
    {
        boolean mipmap = mipmapData.size() > 1;

        if (setTexParam)
        {
            GlStateManager.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, mipmap ? GL11.GL_NEAREST_MIPMAP_LINEAR : GL11.GL_NEAREST);
            GlStateManager.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);
            GlStateManager.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL11.GL_REPEAT);
            GlStateManager.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL11.GL_REPEAT);
        }

        for (Map.Entry<Integer, List<int[]>> entry: mipmapData.entrySet())
        {
            int level = entry.getKey();

            TexRect bigRect = new TexRect(rect.x >> level, rect.y >> level, rect.width >> level, rect.height >> level);
            List<TexRect> rects = mipmapRect.get(level);
            List<int[]> datas = entry.getValue();

            // complete mergedDataContainer in the first tick
            int[] merged = mergedDataContainer.computeIfAbsent(level, k -> new int[bigRect.width * bigRect.height]);

            boolean mergedReady = false;
            CompletableFuture<?> process = mergingProcesses.get(level);
            if (process == null)
            {
                // first tick
                mergingProcesses.put(level, CompletableFuture.runAsync(() ->
                {
                    TextureMerger.mergeTexs(merged, bigRect, rects, datas);
                }, executor));
            }
            else if (process.isDone())
                mergedReady = true;

            if (!skipFirstTick)
            {
                if (!mergedReady)
                    process.join();

                byteBuffer.position(0);
                byteBuffer.clear();
                IntBuffer intView = byteBuffer.asIntBuffer();
                intView.put(merged);
                intView.flip();

                GlStateManager.glTexSubImage2D(GL11.GL_TEXTURE_2D, level, bigRect.x, bigRect.y, bigRect.width, bigRect.height, GL12.GL_BGRA, GL12.GL_UNSIGNED_INT_8_8_8_8_REV, intView);

                // merging textures for the next tick
                mergingProcesses.put(level, CompletableFuture.runAsync(() ->
                {
                    TextureMerger.mergeTexs(merged, bigRect, rects, datas);
                }, executor));
            }
        }

        if (skipFirstTick)
            skipFirstTick = false;
    }

    @Override
    public void dispose()
    {

    }
}
