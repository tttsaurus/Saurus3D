package com.tttsaurus.saurus3d.mcpatches.impl.texturemap;

import com.tttsaurus.saurus3d.Saurus3D;
import com.tttsaurus.saurus3d.common.core.buffer.BufferUploadHint;
import com.tttsaurus.saurus3d.common.core.buffer.MapBufferAccessBit;
import com.tttsaurus.saurus3d.common.core.buffer.PBO;
import com.tttsaurus.saurus3d.common.core.gl.resource.GLResourceManager;
import com.tttsaurus.saurus3d.mcpatches.api.texturemap.ITextureUploader;
import com.tttsaurus.saurus3d.mcpatches.api.texturemap.TexRect;
import net.minecraft.client.renderer.GlStateManager;
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
    }

    public void planTexUpload(int level, int[] data, TexRect rect)
    {
        if (!mipmapData.containsKey(level))
            mipmapData.put(level, new ArrayList<>());
        if (!mipmapRect.containsKey(level))
            mipmapRect.put(level, new ArrayList<>());

        mipmapData.get(level).add(data);
        mipmapRect.get(level).add(new TexRect(rect.x >> level, rect.y >> level, rect.width >> level, rect.height >> level));
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

                if (mergedReady)
                {
                    pboToUpload.uploadByMappedBuffer(0, merged.length * Integer.BYTES, 0, merged,
                            MapBufferAccessBit.WRITE_BIT,
                            MapBufferAccessBit.INVALIDATE_BUFFER_BIT,
                            MapBufferAccessBit.UNSYNCHRONIZED_BIT);
                }
                else
                {
                    // 1 tick is not enough to finish merging textures
                    process.cancel(true);
                    Saurus3D.LOGGER.warn("Didn't finish merging textures async. Some texture animation updates will be skipped on this tick.");
                }

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
