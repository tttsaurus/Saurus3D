package com.tttsaurus.saurus3d.mcpatches.impl.texturemap;

import com.tttsaurus.saurus3d.Saurus3D;
import com.tttsaurus.saurus3d.common.core.buffer.BufferUploadHint;
import com.tttsaurus.saurus3d.common.core.buffer.MapBufferAccessBit;
import com.tttsaurus.saurus3d.common.core.buffer.PBO;
import com.tttsaurus.saurus3d.common.core.gl.resource.GLResourceManager;
import com.tttsaurus.saurus3d.mcpatches.api.texturemap.ITextureUploader;
import com.tttsaurus.saurus3d.mcpatches.api.texturemap.TexRect;
import net.minecraft.client.renderer.GlStateManager;
import org.apache.commons.lang3.time.StopWatch;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL21;
import java.util.*;
import java.util.concurrent.CompletableFuture;

public final class TextureUploaderV2 implements ITextureUploader
{
    private final List<List<PBO>> pboLists = new ArrayList<>();
    private int bufferSize;
    private int bufferingNum;
    private int bufferingIndex;
    private boolean firstCycle;

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

    private static void rotateLeftByOne(List<PBO> list)
    {
        PBO first = list.get(0);
        for (int i = 1; i < list.size(); i++)
            list.set(i - 1, list.get(i));
        list.set(list.size() - 1, first);
    }

    private void extendPboList(int level)
    {
        int d = (int)Math.pow(2, level);
        d *= d;
        int size = bufferSize;
        if (level != 0) size = size / d + 128;

        List<PBO> list = new ArrayList<>();
        for (int i = 0; i < bufferingNum; i++)
        {
            PBO pbo = new PBO();
            pbo.setPboID(PBO.genPboID());
            pbo.allocNewGpuMem(size, BufferUploadHint.STREAM_DRAW);
            list.add(pbo);
        }
        pboLists.add(list);
        GL15.glBindBuffer(GL21.GL_PIXEL_UNPACK_BUFFER, 0);
    }

    public void init(int bufferSize, int bufferingNum)
    {
        bufferingIndex = 0;
        firstCycle = false;
        skipFirstTick = true;
        this.bufferSize = bufferSize;
        this.bufferingNum = bufferingNum;

        extendPboList(0);
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

    public void batchUpload(TexRect rect, boolean setTexParam)
    {
        boolean mipmap = mipmapData.size() > 1;

        if (setTexParam)
        {
            GlStateManager.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, mipmap ? GL11.GL_NEAREST_MIPMAP_LINEAR : GL11.GL_NEAREST);
            GlStateManager.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);
            GlStateManager.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL11.GL_REPEAT);
            GlStateManager.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL11.GL_REPEAT);
        }

        int index = 0;
        int len = mipmapData.size();
        for (Map.Entry<Integer, List<int[]>> entry: mipmapData.entrySet())
        {
            int level = entry.getKey();

            TexRect bigRect = new TexRect(rect.x >> level, rect.y >> level, rect.width >> level, rect.height >> level);
            List<TexRect> rects = mipmapRect.get(level);
            List<int[]> datas = entry.getValue();

            // complete pboLists and mergedDataContainer in the first tick
            if (pboLists.size() < len && index > pboLists.size() - 1) extendPboList(level);
            List<PBO> pbos = pboLists.get(index);
            int[] merged = mergedDataContainer.computeIfAbsent(level, k -> new int[bigRect.width * bigRect.height]);

            boolean mergedReady = false;
            CompletableFuture<?> process = mergingProcesses.get(level);
            if (process == null)
            {
                // first tick
                mergingProcesses.put(level, CompletableFuture.runAsync(() ->
                {
                    TextureMerger.mergeTexs(merged, bigRect, rects, datas);
                }));
            }
            else if (process.isDone())
                mergedReady = true;

            if (!skipFirstTick)
            {
                PBO pboToUpload;
                if (!firstCycle)
                {
                    pboToUpload = pbos.get(bufferingIndex);
                    if (bufferingIndex >= bufferingNum - 1) firstCycle = true;
                }
                else
                {
                    PBO pboToUse = pbos.get(0);
                    GL15.glBindBuffer(GL21.GL_PIXEL_UNPACK_BUFFER, pboToUse.getPboID().getID());
                    GL11.glTexSubImage2D(GL11.GL_TEXTURE_2D, level, bigRect.x, bigRect.y, bigRect.width, bigRect.height, GL12.GL_BGRA, GL12.GL_UNSIGNED_INT_8_8_8_8_REV, 0);
                    rotateLeftByOne(pbos);
                    pboToUpload = pbos.get(bufferingNum - 1);
                }

                if (!mergedReady)
                    process.join();

                pboToUpload.uploadByMappedBuffer(0, merged.length * Integer.BYTES, 0, merged,
                        MapBufferAccessBit.WRITE_BIT,
                        MapBufferAccessBit.INVALIDATE_BUFFER_BIT,
                        MapBufferAccessBit.UNSYNCHRONIZED_BIT);

                // merging textures for the next tick
                mergingProcesses.put(level, CompletableFuture.runAsync(() ->
                {
                    TextureMerger.mergeTexs(merged, bigRect, rects, datas);
                }));
            }

            index++;
        }

        if (skipFirstTick)
            skipFirstTick = false;
        else if (!firstCycle)
            bufferingIndex++;

        GL15.glBindBuffer(GL21.GL_PIXEL_UNPACK_BUFFER, 0);
    }

    @Override
    public void dispose()
    {
        for (List<PBO> pbos: pboLists)
        {
            for (PBO pbo: pbos)
            {
                pbo.dispose();
                GLResourceManager.removeDisposable(pbo);
            }
        }
    }
}
