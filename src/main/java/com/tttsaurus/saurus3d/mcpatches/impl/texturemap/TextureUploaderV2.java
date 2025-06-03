package com.tttsaurus.saurus3d.mcpatches.impl.texturemap;

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
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;
import java.util.*;

public final class TextureUploaderV2 implements ITextureUploader
{
    private ByteBuffer pboByteBuffer;
    private final List<List<PBO>> pboLists = new ArrayList<>();
    private int bufferSize;
    private int bufferingNum;
    private int bufferingIndex;
    private boolean firstCycle;

    private final Map<Integer, List<int[]>> mipmapData = new TreeMap<>();
    private final Map<Integer, List<TexRect>> mipmapRect = new TreeMap<>();

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
        this.bufferSize = bufferSize;
        this.bufferingNum = bufferingNum;

        pboByteBuffer = ByteBuffer.allocateDirect(bufferSize).order(ByteOrder.nativeOrder());

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

            int[] merged = mergedDataContainer.computeIfAbsent(level, k -> new int[bigRect.width * bigRect.height]);
            TextureMerger.mergeTexs(merged, bigRect, rects, datas);

            if (pboLists.size() < len && index > pboLists.size() - 1) extendPboList(level);
            List<PBO> pbos = pboLists.get(index);

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

            pboByteBuffer.position(0);
            pboByteBuffer.clear();
            IntBuffer intView = pboByteBuffer.asIntBuffer();
            intView.put(merged);
            pboByteBuffer.position(0);
            pboByteBuffer.limit(merged.length * Integer.BYTES);

            pboToUpload.uploadByMappedBuffer(0, pboByteBuffer.remaining(), 0, pboByteBuffer,
                    MapBufferAccessBit.WRITE_BIT,
                    MapBufferAccessBit.INVALIDATE_BUFFER_BIT,
                    MapBufferAccessBit.UNSYNCHRONIZED_BIT);

            index++;
        }

        if (!firstCycle) bufferingIndex++;

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
